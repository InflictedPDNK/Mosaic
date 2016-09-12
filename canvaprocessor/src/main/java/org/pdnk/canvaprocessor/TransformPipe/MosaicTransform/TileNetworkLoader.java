package org.pdnk.canvaprocessor.TransformPipe.MosaicTransform;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.nio.ByteBuffer;

/**
 * Created by pnovodon on 12/09/2016.
 */

/**
 * Network mosaic image loader for Universal Image Loader library
 */
class TileNetworkLoader extends SimpleImageLoadingListener
{
    private final MosaicNetworkTransform mosaicNetworkTransform;
    private final Integer loadingColor;

    TileNetworkLoader(MosaicNetworkTransform mosaicNetworkTransform, Integer color)
    {
        this.mosaicNetworkTransform = mosaicNetworkTransform;
        loadingColor = color;
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
    {
        ByteBuffer buffer = ByteBuffer.allocate(loadedImage.getAllocationByteCount());
        loadedImage.copyPixelsToBuffer(buffer);
        buffer.rewind();

        mosaicNetworkTransform.cachedBitmaps.put(loadingColor, buffer);
        if (mosaicNetworkTransform.downloadRequestCounter.decrementAndGet() == 0 &&
                !mosaicNetworkTransform.analysing.get() &&
                mosaicNetworkTransform.isRunning().get())
        {
            mosaicNetworkTransform.buildMosaic();
        }

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason)
    {
        //remove color record so the equest is re-issues in the future
        mosaicNetworkTransform.cachedBitmaps.remove(loadingColor);

        if (mosaicNetworkTransform.isPartialCompletionEnabled())
        {
            mosaicNetworkTransform.setPartiallyCompleted();

            if (mosaicNetworkTransform.downloadRequestCounter.decrementAndGet() == 0 &&
                    !mosaicNetworkTransform.analysing.get() &&
                    mosaicNetworkTransform.isRunning().get())
            {
                mosaicNetworkTransform.buildMosaic();
            }
        }
        else
        {
            //signal end of transform is failed
            mosaicNetworkTransform.endTransformData(null);
        }
    }
}
