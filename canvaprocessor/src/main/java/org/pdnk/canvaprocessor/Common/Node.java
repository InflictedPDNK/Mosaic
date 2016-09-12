package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pnovodon on 8/09/2016.
 */
public interface Node extends Reportable
{
    void run();
    void stop();
    void prepare();
    void setEnablePartial(boolean enablePartial);
    DataDescriptor readOutput();

    boolean canCacheOutput();
    boolean canCacheInput();
    boolean isInputCacheValid();
    boolean isOutputCacheValid();
    boolean isPartialCompletionEnabled();
    AtomicBoolean isRunning();
}
