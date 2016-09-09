package org.pdnk.canvaprocessor.Data;

/**
 * Created by pnovodon on 9/09/2016.
 */
public class ImageDataDescriptor extends DataDescriptor
{
    private int height;
    private int width;
    private int strideWidth;
    private int strideHeight;

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getStrideWidth()
    {
        return strideWidth;
    }

    public void setStrideWidth(int strideWidth)
    {
        this.strideWidth = strideWidth;
    }

    public int getStrideHeight()
    {
        return strideHeight;
    }

    public void setStrideHeight(int strideHeight)
    {
        this.strideHeight = strideHeight;
    }
}
