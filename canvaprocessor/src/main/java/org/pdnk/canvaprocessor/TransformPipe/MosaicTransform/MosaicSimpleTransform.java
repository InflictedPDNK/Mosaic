package org.pdnk.canvaprocessor.TransformPipe.MosaicTransform;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;

import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;
import org.pdnk.canvaprocessor.TransformPipe.BaseTransformPipe;

import java.io.IOException;

/**
 * Created by pnovodon on 11/09/2016.
 */
public class MosaicSimpleTransform extends BaseTransformPipe<ImageDataDescriptor>
{
    private final int tileWidth;
    private final int tileHeight;

    public MosaicSimpleTransform(int tileWidth, int tileHeight)
    {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    @Override
    protected ImageDataDescriptor transformData(ImageDataDescriptor data) throws IOException
    {
        return mosaicRGB(data);
    }

    private ImageDataDescriptor mosaicRGB(ImageDataDescriptor data)
    {
        int meanMatrixWidth = data.getWidth()/tileWidth;
        if(data.getWidth() % tileWidth != 0)
            ++meanMatrixWidth;
        int meanMatrixHeight = data.getHeight()/tileHeight;
        if(data.getHeight() % tileHeight != 0)
            ++meanMatrixHeight;

        int[] meanMatrix = new int[4*meanMatrixWidth*meanMatrixHeight];

        byte[] input = data.getData().array();
        int meanIndex;


        for(int i = 0; i< input.length - 4 ; i += 4)
        {
            meanIndex = ((i/(data.getWidth() << 2 ) / tileHeight) * meanMatrixWidth <<2)+ ((((i >> 2) % data.getWidth() ) / tileWidth) << 2);

            meanMatrix[meanIndex] += input[i] & 0xff;
            meanMatrix[meanIndex+1] += input[i + 1] & 0xff;
            meanMatrix[meanIndex+2] += input[i + 2] & 0xff;
            ++meanMatrix[meanIndex+3];
        }

        int prevMeanIndex = -1;
        byte outR = 0;
        byte outG = 0;
        byte outB = 0;

        for (int i = 0; i < input.length - 4; i += 4)
        {
            meanIndex = ((i/(data.getWidth() << 2 ) / tileHeight) * meanMatrixWidth <<2)+ ((((i >> 2) % data.getWidth() ) / tileWidth) << 2);

            if(prevMeanIndex != meanIndex)
            {
                prevMeanIndex = meanIndex;
                outR = (byte) (meanMatrix[meanIndex] / meanMatrix[meanIndex + 3]);
                outG = (byte) (meanMatrix[meanIndex + 1] / meanMatrix[meanIndex + 3]);
                outB = (byte) (meanMatrix[meanIndex + 2] / meanMatrix[meanIndex + 3]);
            }
            input[i] = outR;
            input[i + 1] = outG;
            input[i + 2] = outB;

        }

        return data;
    }

    ImageDataDescriptor mosaicLAB(ImageDataDescriptor data)
    {
        int meanMatrixWidth = data.getWidth()/tileWidth;
        if(data.getWidth() % tileWidth != 0)
            ++meanMatrixWidth;
        int meanMatrixHeight = data.getHeight()/tileHeight;
        if(data.getHeight() % tileHeight != 0)
            ++meanMatrixHeight;

        double[] meanMatrix = new double[4*meanMatrixWidth*meanMatrixHeight];

        double[] labPixel = new double[3];

        byte[] input = data.getData().array();
        int meanIndex;

        for(int i = 0; i< input.length - 4; i += 4)
        {

            meanIndex = ((i/(data.getWidth() << 2 ) / tileHeight) * meanMatrixWidth <<2)+ ((((i >> 2) % data.getWidth() ) / tileWidth) << 2);

            ColorUtils.RGBToLAB(input[i] & 0xff, input[i + 1] & 0xff, input[i + 2] & 0xff, labPixel);
            meanMatrix[meanIndex] += labPixel[0];
            meanMatrix[meanIndex+1] += labPixel[1];
            meanMatrix[meanIndex+2] += labPixel[2];

            ++meanMatrix[meanIndex+3];

        }

        int rgb = 0;
        int prevMeanIndex = -1;
        for (int i = 0; i < input.length - 4; i += 4)
        {
            meanIndex = ((i/(data.getWidth() << 2 ) / tileHeight) * meanMatrixWidth <<2)+ ((((i >> 2) % data.getWidth() ) / tileWidth) << 2);

            if(prevMeanIndex != meanIndex)
            {
                prevMeanIndex = meanIndex;
                rgb = ColorUtils.LABToColor(meanMatrix[meanIndex] / meanMatrix[meanIndex + 3],
                                            meanMatrix[meanIndex + 1] / meanMatrix[meanIndex + 3],
                                            meanMatrix[meanIndex + 2] / meanMatrix[meanIndex + 3]);

            }
            input[i] = (byte) Color.red(rgb);
            input[i + 1] = (byte) Color.green(rgb);
            input[i + 2] = (byte) Color.blue(rgb);

        }

        return data;
    }
    @Override
    public void prepare()
    {

    }
}
