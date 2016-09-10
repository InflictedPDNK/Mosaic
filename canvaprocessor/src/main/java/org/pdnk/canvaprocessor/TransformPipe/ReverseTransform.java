package org.pdnk.canvaprocessor.TransformPipe;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
public class ReverseTransform extends BaseTransformPipe<DataDescriptor>
{
    @Override
    DataDescriptor transformData(DataDescriptor data) throws IOException
    {
        byte[] in = data.getData().array();
        int endIndex = in.length - 1;

        for(int i = 0; i < in.length >> 1; i++)
        {
            in[i] ^= in[endIndex - i];
            in[endIndex - i] ^= in[i];
            in[i] ^= in[endIndex - i];
        }

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

    }
}
