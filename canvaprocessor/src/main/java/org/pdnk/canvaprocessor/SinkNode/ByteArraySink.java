package org.pdnk.canvaprocessor.SinkNode;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.io.IOException;

/**
 * Created by pnovodon on 9/09/2016.
 */
public class ByteArraySink extends BaseSinkNode<DataDescriptor>
{


    @Override
    DataDescriptor renderData(DataDescriptor data) throws IOException
    {
        DataDescriptor output = new DataDescriptor();
        output.setData(data.getData().clone());
        return output;
    }

    @Override
    public void prepare()
    {
        //nothing to prepare
    }
}
