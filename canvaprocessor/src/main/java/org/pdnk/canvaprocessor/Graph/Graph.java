package org.pdnk.canvaprocessor.Graph;

import android.support.annotation.NonNull;
import android.util.Log;

import org.pdnk.canvaprocessor.Common.Constants;
import org.pdnk.canvaprocessor.Common.Node;
import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Common.Produceable;
import org.pdnk.canvaprocessor.Common.Reportable;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;
import org.pdnk.canvaprocessor.Feedback.ProgressFeedback;
import org.pdnk.canvaprocessor.SinkNode.SinkNode;
import org.pdnk.canvaprocessor.SourceNode.SourceNode;
import org.pdnk.canvaprocessor.TransformPipe.TransformPipe;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by pnovodon on 8/09/2016.
 */
public class Graph
{
    private GraphDescriptor descriptor;
    private LinkedList<Node> path;
    private float completionStep;
    private long prevTimestamp;
    private volatile boolean completedInFull;
    private volatile float currentCompletion;

    private final Object reportingLock = new Object();

    private enum State
    {
        IDLE,
        RUNNING
    }

    private volatile State myState;

    private Graph(@NonNull GraphDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public void pushTransform(@NonNull TransformPipe pipe) throws IllegalStateException
    {
        failIfRunning();

        if(descriptor.transforms == null)
            descriptor.transforms = new LinkedList<>();

        descriptor.transforms.add(pipe);

        if(path != null)
            connectNodes();
    }

    public TransformPipe peekTransform() throws IllegalStateException
    {
        if(descriptor.transforms != null && !descriptor.transforms.isEmpty())
            return descriptor.transforms.getLast();

        return null;
    }

    public TransformPipe popTransform() throws IllegalStateException
    {
        failIfRunning();

        if(descriptor.transforms == null || descriptor.transforms.isEmpty())
            return null;

        descriptor.transforms.removeLast();

        if(path != null)
            connectNodes();

        return peekTransform();
    }

    public void run() throws IllegalStateException
    {
        failIfRunning();

        myState = State.RUNNING;

        Thread executingThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                runProc();
            }
        });
        executingThread.start();
    }

    public void runLast() throws IllegalStateException
    {
        failIfRunning();

        if(path == null || path.isEmpty())
        {
            run();
        }
        else
        {

            myState = State.RUNNING;

            Thread executingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    int nodeCount = 0;
                    TransformPipe startingNode = null;
                    Iterator<TransformPipe> it = descriptor.transforms.descendingIterator();
                    while(it.hasNext())
                    {
                        ++nodeCount;
                        TransformPipe pipe = it.next();
                        if(pipe.canCacheInput() && pipe.isInputCacheValid())
                        {
                            startingNode = pipe;
                            break;
                        }
                    }

                    if(startingNode == null)
                    {
                        runProc();
                    }else
                    {
                        runLastProc(startingNode, nodeCount + 1);
                    }
                }
            });
            executingThread.start();

        }

    }

    public void cancel()
    {
        if(myState == State.RUNNING)
        {
            stopAllNodes();
        }
    }

    public DataDescriptor readTransformOutput() throws IllegalStateException
    {
        failIfRunning();

        if(descriptor.transforms != null && !descriptor.transforms.isEmpty())
        {
            TransformPipe transformPipe = descriptor.transforms.getLast();
            if(transformPipe.canCacheOutput() && transformPipe.isOutputCacheValid())
                return transformPipe.readOutput();
        }

        return null;
    }

    public DataDescriptor readGraphOutput() throws IllegalStateException
    {
        failIfRunning();

        if(descriptor.outputCacheEnabled && descriptor.sink.isOutputCacheValid())
            return descriptor.sink.readOutput();

        return null;
    }

    private void runProc()
    {
        Log.d(Constants.MAIN_TAG, "Running full graph");

        connectNodes();
        setupReporting();
        prepareNodes();

        completedInFull = true;
        completionStep = 1.f/path.size();
        currentCompletion = 0.f;

        prevTimestamp = System.currentTimeMillis();

        descriptor.source.run();
    }

    private void runLastProc(TransformPipe startingNode, int nodeCount)
    {
        Log.d(Constants.MAIN_TAG, "Running graph from last completed transform node");

        prepareNodes();
        setupReporting();

        completedInFull = true;
        completionStep = 1.f/(float)nodeCount;
        currentCompletion = 0.f;

        prevTimestamp = System.currentTimeMillis();

        startingNode.run();
    }

    private void prepareNodes()
    {
        for(Node node : path)
            node.prepare();
    }

    private void connectNodes()
    {
        //TODO: think I need to recreate it loosing the previous connection?
        path = new LinkedList<>();
        path.add(descriptor.source);

        Produceable currentNode = descriptor.source;

        if(descriptor.transforms != null)
        {
            for (TransformPipe transformPipe : descriptor.transforms)
            {
                currentNode.addConsumer(transformPipe);
                currentNode = transformPipe;

                path.add(transformPipe);
            }
        }

        currentNode.addConsumer(descriptor.sink);
        path.add(descriptor.sink);
    }

    private void setupReporting()
    {
        for(Reportable node : path)
        {
            node.setCompletedFeedbackListener(nodeProgressListener);
        }
    }

    private ParametricRunnable<CompletedFeedback> nodeProgressListener = new ParametricRunnable<CompletedFeedback>()
    {
        @Override
        public void run(CompletedFeedback param)
        {
            synchronized (reportingLock)
            {
                if(!param.isSuccessful() || (param.isPartial() && !descriptor.enablePartialCompletion))
                {
                    myState = State.IDLE;
                    stopAllNodes();

                    sendCompletionFeedback(false, param.isPartial(), param.getErrorDescription());
                }
                else
                {
                    //if at least one node partially completed, update the general flag
                    if(param.isPartial())
                        completedInFull = false;

                    currentCompletion += completionStep;


                    sendProgressFeedback(currentCompletion, System.currentTimeMillis() - prevTimestamp);

                    if(currentCompletion == 1.f)
                    {
                        myState = State.IDLE;
                        stopAllNodes();

                        sendCompletionFeedback(true, !completedInFull, null);
                    }

                }
            }
        }
    };

    private void stopAllNodes()
    {
        for(Node node : path)
        {
            node.stop();
        }
    }

    private void sendCompletionFeedback(boolean successful, boolean partial, String errorDescription)
    {
        if(descriptor.completionFeedbackListener != null)
            descriptor.completionFeedbackListener.run(new CompletedFeedback(successful, partial, errorDescription));
    }

    private void sendProgressFeedback(float completion, long elapsedMs)
    {
        if (descriptor.progressFeedbackListener != null)
            descriptor.progressFeedbackListener.run(new ProgressFeedback(completion, elapsedMs));
    }

    private void failIfRunning() throws IllegalStateException
    {
        if(myState == State.RUNNING)
            throw new IllegalStateException("Can't modify graph while running");
    }

    public static class Builder
    {
        GraphDescriptor descriptor;

        public Builder()
        {
            descriptor = new GraphDescriptor();
        }

        public Builder setSourceNode(@NonNull SourceNode source)
        {
            descriptor.source = source;
            return this;
        }

        public Builder setSinkNode(@NonNull SinkNode sink)
        {
            descriptor.sink = sink;
            return this;
        }

        public Builder addTransformPipe(@NonNull TransformPipe transform)
        {
            if(descriptor.transforms == null)
                descriptor.transforms = new LinkedList<>();

            descriptor.transforms.add(transform);
            return this;
        }

        public Builder setEnableCacheOutput(boolean outputCacheEnabled)
        {
            descriptor.outputCacheEnabled = outputCacheEnabled;
            return this;
        }

        public Builder setEnablePartialCompletion(boolean enablePartialCompletion)
        {
            descriptor.enablePartialCompletion = enablePartialCompletion;
            return this;
        }

        public Builder setOnProgressFeedback(ParametricRunnable<ProgressFeedback> progressFeedbackListener)
        {
            descriptor.progressFeedbackListener = progressFeedbackListener;
            return this;
        }

        public Builder setOnCompletionFeedback(ParametricRunnable<CompletedFeedback> completionFeedbackListener)
        {
            descriptor.completionFeedbackListener = completionFeedbackListener;
            return this;
        }

        public @NonNull Graph buildGraph()
        {
            if(descriptor.source == null || descriptor.sink == null)
                throw new IllegalStateException("Graph must contain at least a source and a sink");

            return new Graph(descriptor);

        }
    }

}
