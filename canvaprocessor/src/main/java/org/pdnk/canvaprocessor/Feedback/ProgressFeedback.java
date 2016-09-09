package org.pdnk.canvaprocessor.Feedback;

/**
 * Created by pnovodon on 8/09/2016.
 */
public class ProgressFeedback
{
    private final float completion;
    private final long elapsedMs;

    public ProgressFeedback(float completion, long elapsedMs)
    {
        this.completion = completion;
        this.elapsedMs = elapsedMs;
    }

    public float getCompletion()
    {
        return completion;
    }

    public long getElapsed()
    {
        return elapsedMs;
    }
}
