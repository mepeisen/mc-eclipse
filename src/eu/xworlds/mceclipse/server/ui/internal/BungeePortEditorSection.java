/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerWorkingCopy;

import eu.xworlds.mceclipse.server.runtime.internal.AbstractConfiguration;
import eu.xworlds.mceclipse.server.runtime.internal.BungeeServer;

/**
 * @author mepeisen
 *
 */
public class BungeePortEditorSection extends AbstractPortEditorSection
{
    
    @Override
    protected AbstractConfiguration<?, ?> getConfig(IServerWorkingCopy srv) throws CoreException
    {
        BungeeServer ts = this.server.getAdapter(BungeeServer.class);
        return (AbstractConfiguration<?, ?>) ts.getServerConfiguration();
    }
    
}
