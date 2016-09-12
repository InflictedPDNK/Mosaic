package org.pdnk.canvaprocessor.TransformPipe.MosaicTransform;

import android.content.Context;
import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;
import org.pdnk.canvaprocessor.TransformPipe.BaseAsyncTransformPipe;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pnovodon on 11/09/2016.
 */
public class MosaicNetworkTransform extends BaseAsyncTransformPipe<ImageDataDescriptor>
{
    private final Context context;
    private final String apiQuery = "%s:8765/color/%d/%d/%06x";
    private int tileWidth;
    private int tileHeight;
    private String apiEndpointIP;

    AtomicBoolean analysing = new AtomicBoolean(false);
    private int mosaicMatrixWidth;
    private int mosaicMatrixHeight;
    AtomicInteger downloadRequestCounter;
    HashMap<Integer, ByteBuffer> cachedBitmaps;

    private int[] tileColors;
    ImageDataDescriptor inputData;


    public MosaicNetworkTransform(Context context, int tileWidth, int tileHeight, String apiEndpointIP)
    {
        this.context = context;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.apiEndpointIP = apiEndpointIP;
    }

    @Override
    protected void startTransformData(ImageDataDescriptor data)
    {
        analyseImage(data);
    }

    private void analyseImage(ImageDataDescriptor data)
    {
        analysing.set(true);
        inputData = data;

        mosaicMatrixWidth = data.getWidth()/tileWidth;
        if(data.getWidth() % tileWidth != 0)
            ++mosaicMatrixWidth;
        mosaicMatrixHeight = data.getHeight()/tileHeight;
        if(data.getHeight() % tileHeight != 0)
            ++mosaicMatrixHeight;

        int stride = data.getWidth() * 4;

        tileColors = new int[mosaicMatrixWidth*mosaicMatrixHeight];
        Tile[] tileRow = new Tile[mosaicMatrixWidth];

        byte[] input = data.getData().array();
        int avgIndex;

        Tile tile;
        int scanline = 0, currentScanline = 0;

        downloadRequestCounter.set(0);

        for(int i = 0; i< input.length - 4  && running.get(); i += 4)
        {
            scanline = i/stride / tileHeight;

            if(currentScanline != scanline)
            {
                avgIndex = (currentScanline * mosaicMatrixWidth );
                mapAverageColors(tileRow, avgIndex);
                currentScanline = scanline;
            }
            avgIndex = ((i >> 2) % data.getWidth() ) / tileWidth;

            tile = tileRow[avgIndex];
            if(tile == null)
            {
                tile = new Tile();
                tileRow[avgIndex] = tile;
            }

            tile.r += input[i] & 0xff;
            tile.g += input[i + 1] & 0xff;
            tile.b += input[i + 2] & 0xff;
            ++tile.tilePixelCount;
        }

        mapAverageColors(tileRow, scanline * mosaicMatrixWidth);

        if(downloadRequestCounter.get() == 0  && running.get())
            buildMosaic();

        analysing.set(false);
    }

    private void mapAverageColors(Tile[] tileRow, int tileColorsStride)
    {
        int avgColor;
        for(int j = 0; j< mosaicMatrixWidth && running.get(); ++j)
        {
            avgColor = (tileRow[j].r/tileRow[j].tilePixelCount) << 16 |
                    (tileRow[j].g/tileRow[j].tilePixelCount) << 8 |
                    (tileRow[j].b/tileRow[j].tilePixelCount);

            tileColors[tileColorsStride + j] = avgColor;
            Integer color = avgColor;

            if(!cachedBitmaps.containsKey(color))
            {
                cachedBitmaps.put(color, null);
                downloadRequestCounter.incrementAndGet();
                ImageLoader.getInstance().loadImage(String.format(apiQuery,
                                                                  apiEndpointIP,
                                                                  tileWidth,
                                                                  tileHeight,
                                                                  avgColor), new TileNetworkLoader(this, color));
            }
            tileRow[j].reset();
        }
    }

    void buildMosaic()
    {
        ByteBuffer target = ByteBuffer.allocate(inputData.getData().capacity());

        int x;
        for (int i = 0; i < mosaicMatrixWidth* mosaicMatrixHeight && running.get(); ++i)
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

        if(running.get())
        {
            inputData.setData(target);

            endTransformData(inputData);
        }
    }

    @Override
    public void prepare()
    {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPoolSize(15).build();
        ImageLoader.getInstance().init(config);

        downloadRequestCounter = new AtomicInteger(0);
        cachedBitmaps = new HashMap<>();

    }

    public void setTileSize(int width, int height)
    {
        tileWidth = width;
        tileHeight = height;
    }

    public void setAPIendpoint(@NonNull String APIendpoint)
    {
        this.apiEndpointIP = APIendpoint;
    }
}
