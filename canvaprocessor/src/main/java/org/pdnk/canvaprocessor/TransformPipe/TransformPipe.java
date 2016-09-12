package org.pdnk.canvaprocessor.TransformPipe;

import org.pdnk.canvaprocessor.Common.Consumable;
import org.pdnk.canvaprocessor.Common.Node;
import org.pdnk.canvaprocessor.Common.Produceable;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Transform pipes combine both {@link Consumable} and {@link Produceable} traits, which means they
 * exists in-between source and sink nodes and usually form the main pipeline of the graph. <br/>
 * Transform pipes can run synchronously (within upstream calling node's thread) or asynchronously.
 */
public interface TransformPipe extends Consumable, Produceable, Node
{
}
