/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import eu.xworlds.mceclipse.server.runtime.internal.AbstractServer;
import eu.xworlds.mceclipse.server.runtime.internal.BungeeServer;

/**
 * @author mepeisen
 *
 */
public class BungeeGeneralEditorSection extends AbstractGeneralEditorSection
{

    @Override
    protected AbstractServer<?, ?, ?> getServer(IServerWorkingCopy srv)
    {
        return (BungeeServer) srv.loadAdapter(BungeeServer.class, null);
    }
    
}
