package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Reportable denotes a node capable of reporting feedback. Feedback is a type of callback which does
 * not oblige acceptors to perform any actions, hence can serve as a notifications only
 */
public interface Reportable
{
    /**
     * Attach a listener for the {@link CompletedFeedback}. The feedback is typically sent upon the
     * completion of data processing in the node.
     * @param completedFeedbackListener listener instance
     */
    void setCompletedFeedbackListener(ParametricRunnable<CompletedFeedback> completedFeedbackListener);
}
