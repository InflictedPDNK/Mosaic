package org.pdnk.canvaprocessor.Common;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Produceable type denotes node capable of producing data. Typically such nodes are sources and
 * transform pipes.
 */
public interface Produceable
{
    /**
     * Attach a consumer node. When the node has finished producing the data, data will be propagated
     * downstream to the attached consumer.
     *
     * @param consumableNode instance of downstream node of {@link Consumable} type
     */
    void setConsumer(Consumable consumableNode);


}
