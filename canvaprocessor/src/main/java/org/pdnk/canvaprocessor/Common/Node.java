package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

/**
 * Created by pnovodon on 8/09/2016.
 */
public interface Node extends Reportable
{
    void run();
    void stop();
    void prepare();
    DataDescriptor readOutput();

    boolean canCacheOutput();
    boolean canCacheInput();
    boolean isInputCacheValid();
    boolean isOutputCacheValid();
}
