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
public class BungeeLocationEditorSection extends AbstractLocationEditorSection
{
    
    /**
     * ServerGeneralEditorPart constructor comment.
     */
    public BungeeLocationEditorSection()
    {
        // do nothing
    }

    @Override
    protected AbstractServer<?, ?, ?> getServer(IServerWorkingCopy srv)
    {
        return (BungeeServer) srv.loadAdapter(BungeeServer.class, null);
    }

    @Override
    protected String getSectionDescription()
    {
        return "Specify the server path (containing bungeecord.jar) and deploy path.";
    }

    @Override
    protected String getDoesNotModifyText()
    {
        return "(does not modify BungeeCord installation)";
    }

    @Override
    protected String takesControlText()
    {
        return "(takes control of BungeeCord installation)";
    }

    @Override
    protected String useInstallationText()
    {
        return "Use BungeeCord installation {0}";
    }
    
}
