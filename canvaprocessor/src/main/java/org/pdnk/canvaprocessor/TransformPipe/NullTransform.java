package org.pdnk.canvaprocessor.TransformPipe;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */

/**
 * Null transform for compliance testing
 */
public class NullTransform extends BaseTransformPipe<DataDescriptor>
{
    @Override
    protected DataDescriptor transformData(DataDescriptor data) throws IOException
    {
        //simple pass-thru
        return data;
    }

    @Override
    public boolean canCacheInput()
    {
        return true;
    }

    @Override
    public void prepare()
    {
        //nothing to prepare
    }
}
