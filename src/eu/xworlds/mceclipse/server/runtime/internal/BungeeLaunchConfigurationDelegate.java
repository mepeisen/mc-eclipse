/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;

/**
 * @author mepeisen
 *
 */
public class BungeeLaunchConfigurationDelegate extends AbstractLaunchConfigurationDelegate
{

    @Override
    protected AbstractServerBehaviour getServerBehaviour(IServer server)
    {
        return (BungeeServerBehaviour) server.loadAdapter(BungeeServerBehaviour.class, null);
    }

    @Override
    public String getVMArguments(ILaunchConfiguration configuration) throws CoreException
    {
        final String args = "-Djline.terminal=none"; //$NON-NLS-1$
        final String res = super.getVMArguments(configuration);
        if (res.length() > 0)
        {
            return res + " " + args; //$NON-NLS-1$
        }
        return args;
    }
    
}
