package org.pdnk.canvaprocessor.SinkNode;

import org.pdnk.canvaprocessor.Common.Consumable;
import org.pdnk.canvaprocessor.Common.Node;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Type of node dedicated to final consuming of graph data, i.e. rendering.
 * Can be as simple as a buffer output or as complex as a video renderer.<br/>
 * Sink nodes are considered Pure Consumers (they do not push any data downstream)
 */
public interface SinkNode extends Consumable, Node
{

}
