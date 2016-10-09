/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.server.core.IServerAttributes;

import eu.xworlds.mceclipse.server.runtime.internal.BungeeServer;

/**
 * @author mepeisen
 *
 */
public class BungeeConfigurationPropertyTester extends PropertyTester
{
    
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
        try
        {
            IServerAttributes server = (IServerAttributes) receiver;
            BungeeServer bungeeServer = (BungeeServer) server.loadAdapter(BungeeServer.class, null);
            if (bungeeServer != null)
                return bungeeServer.getServerConfiguration() != null;
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // ignore
        }
        return false;
    }
    
}
