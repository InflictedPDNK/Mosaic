package org.pdnk.canvaprocessor.SinkNode;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by pnovodon on 9/09/2016.
 */
public class NullCachingSink extends BaseSinkNode<DataDescriptor>
{


    @Override
    DataDescriptor renderData(DataDescriptor data) throws IOException
    {
        DataDescriptor output = new DataDescriptor();
        ByteBuffer bb = ByteBuffer.allocate(data.getData().capacity());
        bb.put(data.getData().array());

        bb.rewind();

        output.setData(bb);
        return output;
    }

    @Override
    public void prepare()
    {
        //nothing to prepare
    }
}
