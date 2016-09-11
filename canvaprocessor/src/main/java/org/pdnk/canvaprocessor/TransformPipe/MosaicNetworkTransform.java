package org.pdnk.canvaprocessor.TransformPipe;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pnovodon on 11/09/2016.
 */
public class MosaicNetworkTransform extends BaseAsyncTransformPipe<ImageDataDescriptor>
{
    private final Context context;
    private final int tileWidth;
    private final int tileHeight;
    private final String apiEndpointIP;

    private volatile boolean analysing;
    private int mosaicMatrixWidth;
    private int mosaicMatrixHeight;
    private AtomicInteger downloadRequestCounter;
    private HashMap<Integer, ByteBuffer> cachedBitmaps;

    private int[] tileColors;
    ImageDataDescriptor inputData;

    private class Tile
    {
        int r;
        int g;
        int b;
        int tilePixelCount;

        void reset()
        {
            r = 0;
            g = 0;
            b = 0;
            tilePixelCount = 0;
        }
    }



    private class TileLoader extends SimpleImageLoadingListener
    {
        Integer loadingColor;
        TileLoader(Integer color)
        {
            loadingColor = color;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
        {
            ByteBuffer buffer = ByteBuffer.allocate(loadedImage.getAllocationByteCount());
            loadedImage.copyPixelsToBuffer(buffer);
            buffer.rewind();

            cachedBitmaps.put(loadingColor, buffer);
            if(downloadRequestCounter.decrementAndGet() == 0 && !analysing)
            {
                buildMosaic();
            }

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason)
        {
            setPartiallyCompleted();
            if(downloadRequestCounter.decrementAndGet() == 0 && !analysing)
            {
                buildMosaic();
            }
        }
    }

  //  private Tile[] avgMatrix;

    public MosaicNetworkTransform(Context context, int tileWidth, int tileHeight, String apiEndpointIP)
    {
        this.context = context;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.apiEndpointIP = apiEndpointIP;
    }

    @Override
    void startTransformData(ImageDataDescriptor data)
    {
        analyseImage(data);

    }

    private void analyseImage(ImageDataDescriptor data)
    {
        analysing = true;
        inputData = data;

        mosaicMatrixWidth = data.getWidth()/tileWidth;
        if(data.getWidth() % tileWidth != 0)
            ++mosaicMatrixWidth;
        mosaicMatrixHeight = data.getHeight()/tileHeight;
        if(data.getHeight() % tileHeight != 0)
            ++mosaicMatrixHeight;

        int stride = data.getWidth() * 4;

        tileColors = new int[mosaicMatrixWidth*mosaicMatrixHeight];
        Tile[] rowAnalysis = new Tile[mosaicMatrixWidth];

        //avgMatrix = new Tile[mosaicMatrixWidth*mosaicMatrixHeight];

        byte[] input = data.getData().array();
        int avgIndex;

        Tile tile;
        int scanline = 0, currentScanline = 0;
        int avgColor;

        downloadRequestCounter.set(0);

        for(int i = 0; i< input.length - 4 ; i += 4)
        {
            scanline = i/stride / tileHeight;

            if(currentScanline != scanline)
            {
                avgIndex = (currentScanline * mosaicMatrixWidth );//+ ((((i >> 2) % data.getWidth() ) / tileWidth) );
                for(int j = 0; j< mosaicMatrixWidth; ++j)
                {
                    avgColor = (rowAnalysis[j].r/rowAnalysis[j].tilePixelCount) << 16 |
                                               (rowAnalysis[j].g/rowAnalysis[j].tilePixelCount) << 8 |
                                                (rowAnalysis[j].b/rowAnalysis[j].tilePixelCount);

                    tileColors[avgIndex + j] = avgColor;
                    Integer color = avgColor;

                    if(!cachedBitmaps.containsKey(color))
                    {
                        cachedBitmaps.put(color, null);
                        downloadRequestCounter.incrementAndGet();
                        ImageLoader.getInstance().loadImage(String.format("http://%s/color/%d/%d/%06x",
                                                                          apiEndpointIP,
                                                                          tileWidth,
                                                                          tileHeight,
                                                                          avgColor), new TileLoader(color) );
                    }
                    rowAnalysis[j].reset();
                }

                currentScanline = scanline;
            }
            avgIndex = ((i >> 2) % data.getWidth() ) / tileWidth;

            tile = rowAnalysis[avgIndex];
            if(tile == null)
            {
                tile = new Tile();
                rowAnalysis[avgIndex] = tile;
            }

            tile.r += input[i] & 0xff;
            tile.g += input[i + 1] & 0xff;
            tile.b += input[i + 2] & 0xff;
            ++tile.tilePixelCount;
        }

        avgIndex = (scanline * mosaicMatrixWidth );
        for(int j = 0; j< mosaicMatrixWidth; ++j)
        {
            avgColor = (rowAnalysis[j].r/rowAnalysis[j].tilePixelCount) << 16 |
                    (rowAnalysis[j].g/rowAnalysis[j].tilePixelCount) << 8 |
                    (rowAnalysis[j].b/rowAnalysis[j].tilePixelCount);
            tileColors[avgIndex + j] = avgColor;
            Integer color = avgColor;

            if(!cachedBitmaps.containsKey(color))
            {
                cachedBitmaps.put(color, null);
                downloadRequestCounter.incrementAndGet();
                ImageLoader.getInstance().loadImage(String.format("http://%s/color/%d/%d/%06x",
                                                                  apiEndpointIP,
                                                                  tileWidth,
                                                                  tileHeight,
                                                                  avgColor), new TileLoader(color) );
            }
        }


        int prevAvgIndex = -1;
//        byte outR = 0;
//        byte outG = 0;
//        byte outB = 0;

//        for (int i = 0; i < input.length - 4; i += 4)
//        {
//            avgIndex = ((i/stride / tileHeight) * mosaicMatrixWidth)+ ((((i >> 2) % data.getWidth() ) / tileWidth) );
//
////            if(prevAvgIndex != avgIndex)
////            {
////                prevAvgIndex = avgIndex;
////                tile = avgMatrix[avgIndex];
////                outR = (byte) (tile.r / tile.tilePixelCount);
////                outG = (byte) (tile.g / tile.tilePixelCount);
////                outB = (byte) (tile.b / tile.tilePixelCount);
////            }
//            input[i] = (byte) Color.red(tileColors[avgIndex]);
//            input[i + 1] = (byte) Color.green(tileColors[avgIndex]);
//            input[i + 2] = (byte) Color.blue(tileColors[avgIndex]);
//
//        }


        analysing = false;

        if(downloadRequestCounter.get() == 0)
            buildMosaic();

    }

    private void buildMosaic()
    {
        ByteBuffer target = ByteBuffer.allocate(inputData.getData().capacity());

        int x;
        for (int i = 0; i < mosaicMatrixWidth* mosaicMatrixHeight; ++i)
        {
            int tileColor = tileColors[i];
            ByteBuffer b = cachedBitmaps.get(tileColor);
            if(b != null)
            {
                x = (inputData.getWidth() * tileHeight * (i/mosaicMatrixWidth) + tileWidth * (i % mosaicMatrixWidth)) * 4;

                for(int j = 0; j < b.array().length; j += 4)
                {
                    int line = j/4 / tileWidth;
                    int idx = (line * inputData.getWidth() * 4 + x) +  ((j/4) % tileWidth) * 4;
                    if((tileWidth * (i % mosaicMatrixWidth)  + ((j/4) % tileWidth) ) <  ((inputData.getWidth()) ) &&
                            tileHeight * (i/mosaicMatrixWidth) + line < inputData.getHeight())
                    {
                        target.array()[idx] = b.array()[j];
                        target.array()[idx + 1] = b.array()[j + 1];
                        target.array()[idx + 2] = b.array()[j + 2];
                        target.array()[idx + 3] = b.array()[j + 3];
                    }
                }


            }
        }

        inputData.setData(target);

        endTransformData(inputData);
    }

    @Override
    public void prepare()
    {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPoolSize(15).build();
        ImageLoader.getInstance().init(config);

        downloadRequestCounter = new AtomicInteger(0);
        cachedBitmaps = new HashMap<>();

    }
}
