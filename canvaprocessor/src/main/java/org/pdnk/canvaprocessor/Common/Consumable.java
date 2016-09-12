package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Consumable type denotes node capable of cosuming data. Typically such nodes are transform pipes
 * and sinks (renderers)
 */
public interface Consumable
{
    /**
     * Pass data instance for the node to consume. This call can be synchronous or asynchronous as per
     * the implementation decision.
     * NOTE: The calling node of this method should report completion before propagation.
     * @param data data to consume
     */
    void consume(DataDescriptor data);
}
