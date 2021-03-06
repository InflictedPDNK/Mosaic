package org.pdnk.canvaprocessor.Data;

/**
 * Created by pnovodon on 9/09/2016.
 */

/**
 * Image data descriptor. Used in image processing nodes.
 */
public class ImageDataDescriptor extends DataDescriptor
{
    private int height;
    private int width;


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


}
