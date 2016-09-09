package org.pdnk.canvaprocessor.Data;

/**
 * Created by pnovodon on 8/09/2016.
 */
public class DataDescriptor implements Cloneable
{
    protected byte[] data;

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
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
