package org.pdnk.canvaprocessor.Data;

import java.nio.ByteBuffer;

/**
 * Created by pnovodon on 8/09/2016.
 */
public class DataDescriptor implements Cloneable
{
    protected ByteBuffer data;

    public ByteBuffer getData()
    {
        return data;
    }

    public void setData(ByteBuffer data)
    {
        this.data = data;
    }


    public DataDescriptor clone()
    {
        try
        {
            return (DataDescriptor) super.clone();
        } catch (CloneNotSupportedException e)
        {
            return null;
        }
    }
}
