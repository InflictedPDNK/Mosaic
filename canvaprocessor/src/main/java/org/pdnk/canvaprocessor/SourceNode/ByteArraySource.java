package org.pdnk.canvaprocessor.SourceNode;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;

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
        ByteBuffer bb = ByteBuffer.allocate(inputData.length);
        bb.put(inputData);

        bb.rewind();
        data.setData(bb);

        return data;
    }

    @Override
    public void prepare()
    {
        //nothing to prepare
    }
}
