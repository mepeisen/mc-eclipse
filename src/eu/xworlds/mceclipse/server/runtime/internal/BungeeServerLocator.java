/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.provisional.ServerLocatorDelegate;

/**
 * @author mepeisen
 *
 */
public class BungeeServerLocator extends ServerLocatorDelegate
{
    
    @Override
    public void searchForServers(String host, final IServerSearchListener listener, final IProgressMonitor monitor)
    {
        BungeeRuntimeLocator.IRuntimeSearchListener listener2 = new BungeeRuntimeLocator.IRuntimeSearchListener() {
            @Override
            public void runtimeFound(IRuntimeWorkingCopy runtime)
            {
                String runtimeTypeId = runtime.getRuntimeType().getId();
                String serverTypeId = runtimeTypeId.substring(0, runtimeTypeId.length() - 8);
                IServerType serverType = ServerCore.findServerType(serverTypeId);
                try
                {
                    IServerWorkingCopy server = serverType.createServer(serverTypeId, null, runtime, monitor);
                    listener.serverFound(server);
                }
                catch (Exception e)
                {
                    // Trace.trace(Trace.WARNING, "Could not create Spigot server", e);
                }
            }
        };
        BungeeRuntimeLocator.searchForRuntimes2(null, listener2, monitor);
    }
    
}
