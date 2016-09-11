package org.pdnk.canvaprocessor.SinkNode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;

import java.io.IOException;

/**
 * Created by pnovodon on 10/09/2016.
 */
public class SimpleSurfaceRenderer extends BaseSinkNode<ImageDataDescriptor>
{
    SurfaceView surfaceView;
    private final int effectWidth;
    private final int effectHeight;
    Bitmap renderBitmap;
    boolean firstRender;
    public SimpleSurfaceRenderer(@NonNull SurfaceView targetSurface, int effectWidth, int effectHeight)
    {
        this.surfaceView = targetSurface;
        this.effectWidth = effectWidth;
        this.effectHeight = effectHeight;
    }

    @Override
    ImageDataDescriptor renderData(ImageDataDescriptor data) throws IOException
    {
        firstRender = true;
        renderBitmap = null;
        updateSurface(surfaceView.getHolder(), data);

        return data;
    }

    @Override
    public void prepare()
    {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                updateSurface(holder, (ImageDataDescriptor) cachedOutputData);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                updateSurface(holder, (ImageDataDescriptor) cachedOutputData);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {

            }
        });
    }

    private void updateSurface(SurfaceHolder holder, ImageDataDescriptor data)
    {
        if(data == null || holder.getSurface() == null)
            return;

        if(renderBitmap == null)
        {
            renderBitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);

            renderBitmap.copyPixelsFromBuffer(data.getData());
            data.getData().rewind();
        }

        if(firstRender)
        {
            int mosaicMatrixHeight = data.getHeight()/effectHeight;
            if(data.getHeight() % effectHeight != 0)
                ++mosaicMatrixHeight;

            int step = 0;

            Rect srcR = new Rect(0, 0, data.getWidth(), effectHeight);
            Rect dstR = new Rect(srcR);


            while (++step <= mosaicMatrixHeight)
            {
                Canvas canvas = holder.lockCanvas();

                if (canvas == null)
                    return;

                canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                canvas.drawBitmap(renderBitmap, srcR, dstR, null);

                holder.unlockCanvasAndPost(canvas);

                srcR.bottom += effectHeight;
                dstR.bottom += effectHeight;

                try
                {
                    Thread.sleep(16);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            firstRender = false;
        }else
        {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null)
            {
                canvas.drawBitmap(renderBitmap, 0, 0, null);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
