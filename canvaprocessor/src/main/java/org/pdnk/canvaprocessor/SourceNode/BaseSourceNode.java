package org.pdnk.canvaprocessor.SourceNode;

import android.util.Log;

import org.pdnk.canvaprocessor.Common.BaseNode;
import org.pdnk.canvaprocessor.Common.Constants;
import org.pdnk.canvaprocessor.Common.Consumable;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */

/**
 * Base implementation of Source node never caches input or output data.<br/>
 * Runs in a private worker thread and maintains its existence.
 * @param <T> type of {@link DataDescriptor}
 */
abstract class BaseSourceNode<T extends DataDescriptor> extends BaseNode implements SourceNode
{
    Consumable consumer;

    @Override
    public final void run()
    {
        Log.d(Constants.MAIN_TAG, "\tProducing data by " + getClass().getSimpleName());

        running.set(true);

        procThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                T data = null;

                try
                {
                    if(!Thread.currentThread().isInterrupted())
                    {
                        data = processSourceData();

                        completedFeedbackListener.run(new CompletedFeedback(true, false, null));

                        running.set(false);
                    }

                    if(!Thread.currentThread().isInterrupted())
                        consumer.consume(data);
                } catch (IOException e)
                {
                    completedFeedbackListener.run(new CompletedFeedback(false, false, e.getMessage()));
                    running.set(false);
                }



            }
        });
        procThread.start();
    }



    @Override
    public final void setConsumer(Consumable consumableNode)
    {
        consumer = consumableNode;
    }

    @Override
    public DataDescriptor readOutput()
    {
        return null;
    }

    @Override
    public final boolean canCacheInput()
    {
        return false;
    }

    @Override
    public final boolean canCacheOutput()
    {
        return false;
    }


    protected abstract T processSourceData() throws IOException;
}
