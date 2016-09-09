package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

/**
 * Created by pnovodon on 9/09/2016.
 */
public abstract class BaseNode implements Node
{
    protected ParametricRunnable<CompletedFeedback> completedFeedbackListener;
    protected Thread procThread;
    protected DataDescriptor cachedOutputData;
    protected DataDescriptor cachedInputData;

    @Override
    public final void stop()
    {
        if(procThread != null && procThread.isAlive() && !procThread.isInterrupted())
            procThread.interrupt();

        procThread = null;
    }

    @Override
    public final void setCompletedFeedbackListener(ParametricRunnable<CompletedFeedback> completedFeedbackListener)
    {
        this.completedFeedbackListener = completedFeedbackListener;
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
}
