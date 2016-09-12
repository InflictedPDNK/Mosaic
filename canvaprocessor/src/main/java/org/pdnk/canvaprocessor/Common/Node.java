package org.pdnk.canvaprocessor.Common;

import org.pdnk.canvaprocessor.Data.DataDescriptor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Describes a node of the graph
 * Node is a main entity which can serve as a source, a transform pipe or sink
 */
public interface Node extends Reportable
{
    /**
     * Set the node running. Not all nodes can be set as running. For example, pure consumers (such
     * as sinks) can only respond to consume() call, but never start running on their own
     */
    void run();

    /**
     * Set the node idle. It is not guaranteed that the node will stop immediately, however it is
     * expected for node to cease any processing, data propagation and reporting.
     */
    void stop();

    /**
     * Do any internal initialisation of node. The implementation is optional for nodes.
     * NOTE: this is a synchronous method
     */
    void prepare();

    /**
     * Enabe partial completion.
     * @param enablePartial false to prevent node completing when data is processed partially
     */
    void setEnablePartial(boolean enablePartial);

    /**
     * Get result data of last successful run
     * @return data instance or null if no data exists or {@link #canCacheOutput()} set to false
     */
    DataDescriptor readOutput();

    /**
     *
     * @return true if node can cache the result of last successful run
     */
    boolean canCacheOutput();

    /**
     *
     * @return true if node can cache the incoming (unmodified) data
     */
    boolean canCacheInput();

    /**
     *
     * @return true if incoming cached data exists
     */
    boolean isInputCacheValid();

    /**
     *
     * @return true if result cached data exists
     */
    boolean isOutputCacheValid();

    /**
     *
     * @return true if node is allowed to complete partially
     */
    boolean isPartialCompletionEnabled();

    /**
     * Test if node is in the running state
     * @return atomic state of the node
     */
    AtomicBoolean isRunning();
}
