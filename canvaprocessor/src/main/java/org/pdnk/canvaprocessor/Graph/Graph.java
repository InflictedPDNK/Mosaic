package org.pdnk.canvaprocessor.Graph;

import android.support.annotation.NonNull;
import android.util.Log;

import org.pdnk.canvaprocessor.Common.Constants;
import org.pdnk.canvaprocessor.Common.Node;
import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Common.Produceable;
import org.pdnk.canvaprocessor.Common.Consumable;
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

/**
 * Graph is a construct containing nodes attached to each other in the pipeline and performing
 * operation on data of specific types. Nodes can be of three types: {@link Produceable Producing} (source),
 * {@link Consumable Consuming} (sink) and Transforming (both consuming and producing).<br/>
 * The pipeline once built can be sequentially modified by adding more tranform nodes on top (push) or
 * removing them (pop). Existing nodes in the pipeline can be individually tweaked even after graph
 * has been constructed.<br/>
 * Data flow is based on the push model. It means that the data originates in Producing node and
 * is propagated downstream to Consuming node all the way until it meets pure Consumer (sink). <br/>
 * Graph execution is asynchronous.<br/>
 * Graph can have two states: running and idle, and so can nodes. Graph is considered completed when
 * it was requested to run and all nodes in the pipeline completed successfully.
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

    /**
     * Add new or duplicate instance of a transform pipe. Can be used after the graph has been built.
     * @param pipe new or existing instance of transform pipe
     * @throws IllegalStateException if adding while running
     */
    public void pushTransform(@NonNull TransformPipe pipe) throws IllegalStateException
    {
        failIfRunning();

        if(descriptor.transforms == null)
            descriptor.transforms = new LinkedList<>();

        descriptor.transforms.add(pipe);

        if(path != null)
            connectNodes();
    }

    /**
     * Get reference to the last (downmost) transform pipe in the pipeline. Useful for tweaking
     * parameters of the last transform node for repeated run.
     * @return instance of last transform pipe or null if there are no transformations
     * @throws IllegalStateException if peeking while running
     */
    public TransformPipe peekTransform() throws IllegalStateException
    {
        failIfRunning();

        if(descriptor.transforms != null && !descriptor.transforms.isEmpty())
            return descriptor.transforms.getLast();

        return null;
    }

    /**
     * Remove last transform pipe from the pipeline
     * @return previous pipe before the removed one or null if there are no more transformations
     * @throws IllegalStateException if popping while running
     */
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

    /**
     * Run the graph.
     * This call is asynchronous.
     * @throws IllegalStateException if graph is already running
     */
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

    /**
     * Try to run graph partially to minimise processing time.<br/>
     * Graph will scan from the last transform all the way upstream until it meets a transform with
     * valid cached input. If such node is encountered, graph will run starting from it all the way
     * down to sink node. If there are no nodes with cached input data or graph has not been fully
     * run before, a full run cycle will be executed.
     * @throws IllegalStateException if graph is already running
     */
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

    /**
     * Cancel graph running.<br/>
     * There is no guarantee that the nodes will stop immediately.<br/>
     * Graph will be set to Idle state and must be ready for next run.<br/>
     * A caller must not rely on any feedback coming after this call. Ideally, there should be no
     * feedback at all.
     */
    public void cancel()
    {
        if(myState == State.RUNNING)
        {
            stopAllNodes();
        }
    }

    /**
     * Get last successful result of last cached transformation. Can be used if data is modified by
     * a sink, but caller wants to obtain transformed only (non-rendered) data.
     * NOTE: it is not guaranteed that the data will relate to the actual last node in the pipeline
     * as it depends on the ability of transform pipe to cache its result.
     * @return cached data of the last transform or null
     * @throws IllegalStateException if graph is running
     */
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

    /**
     * Get last successful result of graph run. This data is normally a cached output of graph sink
     * (renderer)
     * @return cached data of the last graph run or null
     * @throws IllegalStateException if graph is running
     */
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
        {
            node.setEnablePartial(descriptor.enablePartialCompletion);
            node.prepare();
        }
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
                currentNode.setConsumer(transformPipe);
                currentNode = transformPipe;

                path.add(transformPipe);
            }
        }

        currentNode.setConsumer(descriptor.sink);
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
                if(myState == State.IDLE)
                    return;

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
        myState = State.IDLE;
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

    /**
     * Graph builder for guaranteed creation of the minimal graph.<br/>
     * Uses default values for all parameters, unless explicitly set by respective methods.
     */
    public static class Builder
    {
        GraphDescriptor descriptor;

        public Builder()
        {
            descriptor = new GraphDescriptor();
        }

        /**
         * Set {@link SourceNode} in the graph. There can be only one source.<br/>
         * Any graph requires at least a source and a sink to run.
         * @param source instance of source node
         * @return this builder instance
         */
        public Builder setSourceNode(@NonNull SourceNode source)
        {
            descriptor.source = source;
            return this;
        }

        /**
         * Set {@link SinkNode} in the graph. There can be only one sink.<br/>
         * Any graph requires at least a source and a sink to run.
         * @param sink instance of sink node
         * @return this builder instance
         */
        public Builder setSinkNode(@NonNull SinkNode sink)
        {
            descriptor.sink = sink;
            return this;
        }

        /**
         * Add {@link TransformPipe} to the graph. The order of calling this method is important.
         * There can be no transformations in the pipeline at all. Duplicate instances are allowed.
         * @param transform instance of transform pipe
         * @return this builder instance
         */
        public Builder addTransformPipe(@NonNull TransformPipe transform)
        {
            if(descriptor.transforms == null)
                descriptor.transforms = new LinkedList<>();

            descriptor.transforms.add(transform);
            return this;
        }

        /**
         * Enable caching of last successful graph run
         * @param outputCacheEnabled true to enable
         * @return this builder instance
         * @see Graph#readGraphOutput()
         */
        public Builder setEnableCacheOutput(boolean outputCacheEnabled)
        {
            descriptor.outputCacheEnabled = outputCacheEnabled;
            return this;
        }

        /**
         * Allow nodes to complete partially
         * @param enablePartialCompletion true to allow
         * @return this builder instance
         */
        public Builder setEnablePartialCompletion(boolean enablePartialCompletion)
        {
            descriptor.enablePartialCompletion = enablePartialCompletion;
            return this;
        }

        /**
         * Install a listener for {@link ProgressFeedback}. Overall graph progress is reported.
         * @param progressFeedbackListener instance of {@link ParametricRunnable}
         * @return this builder instance
         */
        public Builder setOnProgressFeedback(ParametricRunnable<ProgressFeedback> progressFeedbackListener)
        {
            descriptor.progressFeedbackListener = progressFeedbackListener;
            return this;
        }

        /**
         * Install a listener for {@link CompletedFeedback}. Feedback is send upon graph run is completed.
         * <br/>
         * NOTE: if graph is cancelled, this feedback will not be sent
         * @param completionFeedbackListener
         * @return this builder instance
         */
        public Builder setOnCompletionFeedback(ParametricRunnable<CompletedFeedback> completionFeedbackListener)
        {
            descriptor.completionFeedbackListener = completionFeedbackListener;
            return this;
        }

        /**
         * Construct graph
         * @return new graph instance
         */
        public @NonNull Graph buildGraph()
        {
            if(descriptor.source == null || descriptor.sink == null)
                throw new IllegalStateException("Graph must contain at least a source and a sink");

            return new Graph(descriptor);

        }
    }

}
