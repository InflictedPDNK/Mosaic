package org.pdnk.canvaprocessor.SourceNode;

import org.pdnk.canvaprocessor.Common.Node;
import org.pdnk.canvaprocessor.Common.Produceable;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Source node is used as a initial data producer (Pure Producer). It can convert external data into
 * internal format or simply cache (clone) data to prevent in-place modification by certain tranform
 * pipes.<br/>
 * Source nodes usually run asynchronously as they are the first nodes in the pipeline, hence their
 * thread stack can include the calls to the downstream nodes
 */
public interface SourceNode extends Produceable, Node
{
}
