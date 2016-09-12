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

/**
 * Sink node for rendering image into a SurfaceView. Uses scanline animation for the first render.
 * Maintains surface update after render has been completed.</br>
 * Scales incoming image to fit into the surface preserving original aspect ratio of the image.
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

                //define scaling rectangle preserving aspect ratio
                if(srcR == null)
                {
                    dstR = getScaledRect(canvas);

                    heightRatio = (double)dstR.height() / (double) renderBitmap.getHeight();
                    srcR = new Rect(0, 0, data.getWidth(), effectHeight);
                    dstR.bottom = (int) (dstR.top + effectHeight * heightRatio);
                }

                //clear canvas
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                //draw
                canvas.drawBitmap(renderBitmap, srcR, dstR, null);

                holder.unlockCanvasAndPost(canvas);

                //step to next mosaic scanling
                srcR.bottom += effectHeight;
                dstR.bottom += effectHeight * heightRatio;

                try
                {
                    //16 ms is attempt to achieve 60 ms, although not guaranteed :)
                    Thread.sleep(16);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            firstRender = false;
        }else
        {
            //if it is a subsequent render, simply draw bitmap without animation
            Canvas canvas = holder.lockCanvas();
            if (canvas != null)
            {
                canvas.drawBitmap(renderBitmap, null, getScaledRect(canvas), null);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private Rect getScaledRect(Canvas canvas)
    {
        double ar = (double)renderBitmap.getHeight()/ (double)renderBitmap.getWidth();
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

        return new Rect(x, y, targetWidth + x, targetHeight + y);
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
