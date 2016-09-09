package org.pdnk.canvaprocessor.SourceNode;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
public class ByteArraySource extends BaseSourceNode<DataDescriptor>
{
    byte[] inputData;
    public ByteArraySource(byte[] inputData)
    {
        this.inputData = inputData;
    }
    @Override
    protected DataDescriptor processSourceData() throws IOException
    {
        DataDescriptor data = new DataDescriptor();
        data.setData(inputData);

        return data;
    }

    @Override
    public void prepare()
    {
        //nothing to prepare
    }
}
