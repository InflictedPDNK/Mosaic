package org.pdnk.canvaprocessor.TransformPipe;

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
public abstract class BaseTransformPipe<T extends DataDescriptor> extends BaseNode implements TransformPipe
{
    Consumable consumer;

    boolean partiallyCompleted;

    @Override
    public void consume(final DataDescriptor data)
    {
        Log.d(Constants.MAIN_TAG, "\tTransforming data by " + getClass().getSimpleName());


        if(data == null)
        {
            completedFeedbackListener.run(new CompletedFeedback(false,
                                                                false,
                                                                "No data to consume"));
        }else
        {
            if(!Thread.currentThread().isInterrupted())
            {
                if (canCacheInput())
                {
                    cachedInputData = data.clone();
                }

                process(data);
            }
        }
    }



    @Override
    public void run()
    {
        Log.d(Constants.MAIN_TAG,
              String.format("\tTransforming data by %s", this.getClass().getSimpleName()));


        if(!canCacheInput() || cachedInputData == null)
        {
            completedFeedbackListener.run(new CompletedFeedback(false,
                                                                false,
                                                                "Can't run transform because input data is empty or caching disabled"));
        }else
        {


            procThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    T dataToTransform = (T) cachedInputData.clone();

                    process(dataToTransform);
                }
            });
            procThread.start();
        }
    }

    protected void process(DataDescriptor data)
    {
        try
        {
            partiallyCompleted = false;


            T dataToSend = null;

            if(!Thread.currentThread().isInterrupted())
            {
                running.set(true);

                dataToSend = transformData((T) data);

                running.set(false);
            }

            if (canCacheOutput())
            {
                cachedOutputData = dataToSend.clone();
            }

            if (!Thread.currentThread().isInterrupted())
            {
                consumer.consume(dataToSend);
            }

            if (!Thread.currentThread().isInterrupted())
            {
                completedFeedbackListener.run(new CompletedFeedback(true, partiallyCompleted, null));
            }
        } catch (IOException | ClassCastException e)
        {
            if (!Thread.currentThread().isInterrupted())
            {
                completedFeedbackListener.run(new CompletedFeedback(false,
                                                                    partiallyCompleted,
                                                                    e.getMessage()));
                running.set(false);
            }

        }
    }

    @Override
    public DataDescriptor readOutput()
    {
        return cachedOutputData;
    }

    @Override
    public boolean canCacheOutput()
    {
        return false;
    }

    @Override
    public boolean canCacheInput()
    {
        return false;
    }

    @Override
    public void addConsumer(Consumable consumableNode)
    {
        consumer = consumableNode;
    }

    public void setPartiallyCompleted()
    {
        partiallyCompleted = true;
    }

    protected abstract T transformData(T data) throws IOException;


}
