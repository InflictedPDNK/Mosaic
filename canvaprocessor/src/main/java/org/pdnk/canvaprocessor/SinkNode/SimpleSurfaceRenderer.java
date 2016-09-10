package org.pdnk.canvaprocessor.SinkNode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;

import java.io.IOException;

/**
 * Created by pnovodon on 10/09/2016.
 */
public class SimpleSurfaceRenderer extends BaseSinkNode<ImageDataDescriptor>
{
    SurfaceView surfaceView;
    public SimpleSurfaceRenderer(@NonNull SurfaceView targetSurface)
    {
        this.surfaceView = targetSurface;
    }

    @Override
    ImageDataDescriptor renderData(ImageDataDescriptor data) throws IOException
    {
        Canvas canvas = surfaceView.getHolder().lockCanvas();


        Bitmap b = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);

        b.copyPixelsFromBuffer(data.getData());
        canvas.drawBitmap(b, 0, 0, null);
        surfaceView.getHolder().unlockCanvasAndPost(canvas);

        return data;
    }

    @Override
    public void prepare()
    {

    }
}
