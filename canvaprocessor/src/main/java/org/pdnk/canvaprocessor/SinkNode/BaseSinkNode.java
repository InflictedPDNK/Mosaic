package org.pdnk.canvaprocessor.SinkNode;

import android.util.Log;

import org.pdnk.canvaprocessor.Common.BaseNode;
import org.pdnk.canvaprocessor.Common.Constants;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
abstract class BaseSinkNode<T extends DataDescriptor> extends BaseNode implements SinkNode
{
    @Override
    public final void consume(final DataDescriptor data)
    {
        Log.d(Constants.MAIN_TAG, "\tRendering data by " + getClass().getSimpleName());

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
                        if(!Thread.currentThread().isInterrupted())
                            cachedOutputData = renderData(data).clone();

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
        procThread.start();
    }

    @Override
    public final void run()
    {
        //not capable of running
    }


    @Override
    public final DataDescriptor readOutput()
    {
        return cachedOutputData;
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


    abstract T renderData(DataDescriptor data) throws IOException;
}
