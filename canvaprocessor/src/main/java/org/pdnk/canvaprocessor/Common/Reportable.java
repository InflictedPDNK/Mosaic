package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

/**
 * Created by pnovodon on 8/09/2016.
 */
public interface Reportable
{
    void setCompletedFeedbackListener(ParametricRunnable<CompletedFeedback> completedFeedbackListener);
}
