package org.pdnk.canvaprocessor.Feedback;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Progress update feedback. Can be sent by nodes or graph to report the progress
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

    /**
     *
     * @return Completion percentage in range [0f, 1f]
     */
    public float getCompletion()
    {
        return completion;
    }

    /**
     *
     * @return time in milliseconds elapsed from the beginning of the operation
     */
    public long getElapsed()
    {
        return elapsedMs;
    }
}
