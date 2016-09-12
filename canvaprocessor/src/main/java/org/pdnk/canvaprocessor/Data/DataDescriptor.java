package org.pdnk.canvaprocessor.Data;

import java.nio.ByteBuffer;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Base type of data which is propagated between nodes. All custom types must derive from this node.
 */
public class DataDescriptor implements Cloneable
{
    protected ByteBuffer data;

    /**
     * Get raw data associated with this descriptor
     * @return raw data isntance or null
     */
    public ByteBuffer getData()
    {
        return data;
    }

    /**
     * Set raw data buffer.
     * @param data data buffer or null
     */
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
