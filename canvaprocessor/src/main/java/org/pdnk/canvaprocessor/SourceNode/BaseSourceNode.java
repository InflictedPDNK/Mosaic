package org.pdnk.canvaprocessor.SourceNode;

import org.pdnk.canvaprocessor.Common.Consumable;
import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
abstract class BaseSourceNode<T extends DataDescriptor> implements SourceNode
{
    Consumable consumer;
    ParametricRunnable<CompletedFeedback> completedFeedbackListener;
    Thread procThread;

    @Override
    public final void run()
    {
        procThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                T data;

                try
                {
                    data = processSourceData();

                    if(!Thread.currentThread().isInterrupted())
                        completedFeedbackListener.run(new CompletedFeedback(true, false, null ));
                } catch (IOException e)
                {
                    if(!Thread.currentThread().isInterrupted())
                        completedFeedbackListener.run(new CompletedFeedback(false, false, e.getMessage()));

                    return;
                }

                consumer.consume(data);

            }
        });
        procThread.run();
    }

    @Override
    public final void stop()
    {
        if(procThread != null && procThread.isAlive() && !procThread.isInterrupted())
            procThread.interrupt();

        procThread = null;
    }

    @Override
    public final void addConsumer(Consumable consumableNode)
    {
        consumer = consumableNode;
    }

    @Override
    public DataDescriptor readOutput()
    {
        return null;
    }

    @Override
    public boolean canCacheInput()
    {
        return false;
    }

    @Override
    public boolean canCacheOutput()
    {
        return false;
    }

    @Override
    public final void setCompletedFeedbackListener(ParametricRunnable<CompletedFeedback> completedFeedbackListener)
    {
        this.completedFeedbackListener = completedFeedbackListener;

    }

    protected abstract T processSourceData() throws IOException;
}
