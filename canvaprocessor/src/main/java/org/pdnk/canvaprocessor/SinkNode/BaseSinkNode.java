package org.pdnk.canvaprocessor.SinkNode;

import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
abstract class BaseSinkNode<T extends DataDescriptor> implements SinkNode
{
    ParametricRunnable<CompletedFeedback> completedFeedbackListener;
    Thread procThread;
    T cachedData;

    @Override
    public final void consume(final DataDescriptor data)
    {
        procThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if(data == null)
                {
                    completedFeedbackListener.run(new CompletedFeedback(false,
                                                                        false,
                                                                        "No data to consume"));
                }
                else
                {
                    try
                    {
                        if(canCacheOutput())
                            cachedData = (T) renderData(data).clone();

                        if (!Thread.currentThread().isInterrupted())
                        {
                            completedFeedbackListener.run(new CompletedFeedback(true, false, null));
                        }
                    } catch (IOException e)
                    {
                        if (!Thread.currentThread().isInterrupted())
                            completedFeedbackListener.run(new CompletedFeedback(false, false, e.getMessage()));

                    }
                }


            }
        });
        procThread.run();
    }

    @Override
    public final void run()
    {
        //not capable of running
    }

    @Override
    public final void stop()
    {
        if(procThread != null && procThread.isAlive() && !procThread.isInterrupted())
            procThread.interrupt();

        procThread = null;
    }

    @Override
    public final DataDescriptor readOutput()
    {
        return cachedData;
    }

    @Override
    public final boolean canCacheOutput()
    {
        return true;
    }

    @Override
    public final boolean canCacheInput()
    {
        return false;
    }

    @Override
    public void setCompletedFeedbackListener(ParametricRunnable<CompletedFeedback> completedFeedbackListener)
    {
        this.completedFeedbackListener = completedFeedbackListener;
    }

    abstract T renderData(DataDescriptor data) throws IOException;
}
