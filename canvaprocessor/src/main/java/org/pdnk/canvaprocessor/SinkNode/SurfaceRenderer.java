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
public class SurfaceRenderer extends BaseSinkNode<ImageDataDescriptor>
{
    SurfaceView surfaceView;
    private final int effectWidth;
    private final int effectHeight;
    Bitmap renderBitmap;
    boolean firstRender;
    public SurfaceRenderer(@NonNull SurfaceView targetSurface, int effectWidth, int effectHeight)
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
        cachedOutputData = null;
        renderBitmap = null;

        surfaceView.getHolder().removeCallback(surfaceCallback);
        surfaceView.getHolder().addCallback(surfaceCallback);

        clearSurface(surfaceView.getHolder());
    }

    private void clearSurface(SurfaceHolder holder)
    {
        if(holder.getSurface() == null)
            return;

        Canvas canvas = holder.lockCanvas();
        if(canvas != null)
        {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            holder.unlockCanvasAndPost(canvas);
        }
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

        double ar = (double)renderBitmap.getHeight()/ (double)renderBitmap.getWidth();

        if(firstRender)
        {
            int mosaicMatrixHeight = data.getHeight()/effectHeight;
            if(data.getHeight() % effectHeight != 0)
                ++mosaicMatrixHeight;

            int step = 0;


            Rect srcR = null;
            Rect dstR = null;
            double heightRatio = 1;


            while (++step <= mosaicMatrixHeight)
            {
                Canvas canvas = holder.lockCanvas();

                if (canvas == null)
                    return;

                if(srcR == null)
                {
                    int targetWidth = canvas.getWidth();
                    int targetHeight = (int) (targetWidth * ar);
                    int y = 0;
                    int x = 0;

                    if(targetHeight <= canvas.getHeight())
                    {
                        y = (canvas.getHeight() - targetHeight) / 2;
                    }else
                    {
                        targetHeight = canvas.getHeight();
                        targetWidth = (int) (targetHeight/ ar);
                        x = (canvas.getWidth() - targetWidth) / 2;
                    }

                    heightRatio = (double)targetHeight / (double) renderBitmap.getHeight();
                    srcR = new Rect(0, 0, data.getWidth(), effectHeight);
                    dstR = new Rect(x, y, x + targetWidth, (int) (y + effectHeight * heightRatio));
                }
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);


                canvas.drawBitmap(renderBitmap, srcR, dstR, null);

                holder.unlockCanvasAndPost(canvas);

                srcR.bottom += effectHeight;
                dstR.bottom += effectHeight * heightRatio;

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
                int targetWidth = canvas.getWidth();
                int targetHeight = (int) (targetWidth * ar);
                int y = 0, x = 0;
                if(targetHeight < canvas.getHeight())
                {
                    y = (canvas.getHeight() - targetHeight) / 2;
                }else
                {
                    targetHeight = canvas.getHeight();
                    targetWidth = (int) (targetHeight/ ar);
                    x = (canvas.getWidth() - targetWidth) / 2;
                }

                Rect dstRect = new Rect(x, y, targetWidth + x, targetHeight + y);
                canvas.drawBitmap(renderBitmap, null, dstRect, null);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
    {
        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            if(cachedOutputData != null)
                updateSurface(holder, (ImageDataDescriptor) cachedOutputData);
            else
                clearSurface(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            if(cachedOutputData != null)
                updateSurface(holder, (ImageDataDescriptor) cachedOutputData);
            else
                clearSurface(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {

        }
    };
}
