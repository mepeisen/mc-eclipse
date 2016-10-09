/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.wst.server.core.IServer;

/**
 * @author mepeisen
 *
 */
public class BungeePublishTask extends AbstractPublishTask
{

    @Override
    protected AbstractServerBehaviour getBehaviour(IServer server)
    {
        return (BungeeServerBehaviour) server.loadAdapter(BungeeServerBehaviour.class, null);
    }
    
}
