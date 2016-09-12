package org.pdnk.canvaprocessor.TransformPipe;

import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.io.IOException;

/**
 * Created by pnovodon on 11/09/2016.
 */
public abstract class BaseAsyncTransformPipe<T extends DataDescriptor> extends BaseTransformPipe<T>
{
    @Override
    protected final T transformData(T data) throws IOException
    {
        return null;
    }


    @Override
    protected void process(DataDescriptor data)
    {
        partiallyCompleted = false;
        if(!Thread.currentThread().isInterrupted())
        {
            running.set(true);
            startTransformData((T) data);
        }
    }

    public void endTransformData(T data)
    {
        running.set(false);
        try
        {
            if(data == null || data.getData() == null)
            {
                completedFeedbackListener.run(new CompletedFeedback(false,
                                                                    partiallyCompleted,
                                                                    "Failed to transform"));
            }
            else
            {
                if (canCacheOutput() && !Thread.currentThread().isInterrupted())
                {
                    cachedOutputData = data.clone();
                }

                if (!Thread.currentThread().isInterrupted())
                    consumer.consume(data);

                if (!Thread.currentThread().isInterrupted())
                {
                    completedFeedbackListener.run(new CompletedFeedback(true,
                                                                        partiallyCompleted,
                                                                        null));
                }
            }
        } catch (ClassCastException e)
        {
            if (!Thread.currentThread().isInterrupted())
                completedFeedbackListener.run(new CompletedFeedback(false,
                                                                    partiallyCompleted, e.getMessage()));

        }
    }

    protected abstract void startTransformData(T data);
}
