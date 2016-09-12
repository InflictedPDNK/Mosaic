package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pnovodon on 9/09/2016.
 */

/**
 * Base implementation of the node.
 * Handles run/stop states and maintains caching.
 */
public abstract class BaseNode implements Node
{
    protected ParametricRunnable<CompletedFeedback> completedFeedbackListener;
    protected Thread procThread;
    protected DataDescriptor cachedOutputData;
    protected DataDescriptor cachedInputData;
    protected boolean enablePartialCompletion;
    protected AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public final void stop()
    {
        //interrupt processing thread if it exists
        if(procThread != null && procThread.isAlive() && !procThread.isInterrupted())
            procThread.interrupt();

        //clear running flag
        running.set(false);

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

    @Override
    public void setEnablePartial(boolean enablePartial)
    {
        enablePartialCompletion = enablePartial;
    }

    @Override
    public AtomicBoolean isRunning()
    {
        return running;
    }

    @Override
    public boolean isPartialCompletionEnabled()
    {
        return enablePartialCompletion;
    }
}
