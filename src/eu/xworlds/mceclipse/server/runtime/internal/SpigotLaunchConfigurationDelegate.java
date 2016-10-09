/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.wst.server.core.IServer;

/**
 * @author mepeisen
 *
 */
public class SpigotLaunchConfigurationDelegate extends AbstractLaunchConfigurationDelegate
{
    
    @Override
    protected AbstractServerBehaviour getServerBehaviour(IServer server)
    {
        return (SpigotServerBehaviour) server.loadAdapter(SpigotServerBehaviour.class, null);
    }
    
}
