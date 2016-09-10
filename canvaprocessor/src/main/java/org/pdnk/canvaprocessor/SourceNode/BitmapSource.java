package org.pdnk.canvaprocessor.SourceNode;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by pnovodon on 10/09/2016.
 */
public class BitmapSource extends BaseSourceNode<ImageDataDescriptor>
{
    private Bitmap inputBitmap;

    public BitmapSource(@NonNull Bitmap bitmap)
    {
        inputBitmap = bitmap;
    }

    @Override
    protected ImageDataDescriptor processSourceData() throws IOException
    {
        ImageDataDescriptor data = new ImageDataDescriptor();

        ByteBuffer bb = ByteBuffer.allocate(inputBitmap.getAllocationByteCount());
        inputBitmap.copyPixelsToBuffer(bb);

        bb.rewind();
        data.setData(bb);
        data.setWidth(inputBitmap.getWidth());
        data.setHeight(inputBitmap.getHeight());

        return data;
    }


    @Override
    public void prepare()
    {
        //nothign to prepare
    }
}
