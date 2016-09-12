package org.pdnk.canvaprocessor.Graph;

import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;
import org.pdnk.canvaprocessor.Feedback.ProgressFeedback;
import org.pdnk.canvaprocessor.SinkNode.SinkNode;
import org.pdnk.canvaprocessor.SourceNode.SourceNode;
import org.pdnk.canvaprocessor.TransformPipe.TransformPipe;

import java.util.LinkedList;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Core descriptor of Graph. Contains default values data required for any graph operations.
 * Can be used in cloning or graph duplication
 */
class GraphDescriptor
{
    SourceNode source;
    SinkNode sink;
    LinkedList<TransformPipe> transforms;
    boolean outputCacheEnabled = false;
    boolean enablePartialCompletion = false;
    ParametricRunnable<ProgressFeedback> progressFeedbackListener;
    ParametricRunnable<CompletedFeedback> completionFeedbackListener;
}
