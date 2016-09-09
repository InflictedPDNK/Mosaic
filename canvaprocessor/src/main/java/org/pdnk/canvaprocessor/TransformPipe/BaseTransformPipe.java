package org.pdnk.canvaprocessor.TransformPipe;

import android.util.Log;

import org.pdnk.canvaprocessor.Common.Constants;
import org.pdnk.canvaprocessor.Common.Consumable;
import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
public abstract class BaseTransformPipe<T extends DataDescriptor> implements TransformPipe
{
    ParametricRunnable<CompletedFeedback> completedFeedbackListener;
    Thread procThread;
    Consumable consumer;
    DataDescriptor cachedOutputData;
    DataDescriptor cachedInputData;
    boolean partillyCompleted;

    @Override
    public void consume(final DataDescriptor data)
    {
        Log.d(Constants.MAIN_TAG, "\tTransforming data by " + getClass().getSimpleName());

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
                }else
                {
                    if (canCacheInput())
                    {
                        cachedInputData = data.clone();
                    }

                    process(data);
                }
            }
        });
        procThread.run();
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
            procThread.run();
        }
    }

    private void process(DataDescriptor data)
    {
        try
        {
            partillyCompleted = false;

            T dataToSend = transformData((T) data);

            if (canCacheOutput() && !Thread.currentThread().isInterrupted())
            {
                cachedOutputData = dataToSend.clone();
            }

            if (!Thread.currentThread().isInterrupted())
                consumer.consume(dataToSend);

            if (!Thread.currentThread().isInterrupted())
            {
                completedFeedbackListener.run(new CompletedFeedback(true, partillyCompleted, null));
            }
        } catch (IOException | ClassCastException e)
        {
            if (!Thread.currentThread().isInterrupted())
                completedFeedbackListener.run(new CompletedFeedback(false, partillyCompleted, e.getMessage()));

        }
    }


    @Override
    public void stop()
    {
        if(procThread != null && procThread.isAlive() && !procThread.isInterrupted())
            procThread.interrupt();

        procThread = null;
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
    public final boolean isInputCacheValid()
    {
        return cachedInputData != null;
    }

    @Override
    public final boolean isOutputCacheValid()
    {
        return cachedOutputData != null;
    }

    @Override
    public void addConsumer(Consumable consumableNode)
    {
        consumer = consumableNode;
    }

    @Override
    public void setCompletedFeedbackListener(ParametricRunnable<CompletedFeedback> completedFeedbackListener)
    {
        this.completedFeedbackListener = completedFeedbackListener;
    }

    protected void setPartiallyCompleted()
    {
        partillyCompleted = true;
    }

    abstract T transformData(T data) throws IOException;


}
