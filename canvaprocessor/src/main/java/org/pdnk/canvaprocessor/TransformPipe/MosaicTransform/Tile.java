package org.pdnk.canvaprocessor.TransformPipe.MosaicTransform;

/**
 * Created by pnovodon on 12/09/2016.
 */
class Tile
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
