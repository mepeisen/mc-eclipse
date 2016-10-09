/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import eu.xworlds.mceclipse.server.runtime.internal.AbstractServer;
import eu.xworlds.mceclipse.server.runtime.internal.SpigotServer;

/**
 * @author mepeisen
 *
 */
public class SpigotLocationEditorSection extends AbstractLocationEditorSection
{
    
    /**
     * ServerGeneralEditorPart constructor comment.
     */
    public SpigotLocationEditorSection()
    {
        // do nothing
    }

    @Override
    protected AbstractServer<?, ?, ?> getServer(IServerWorkingCopy srv)
    {
        return (SpigotServer) srv.loadAdapter(SpigotServer.class, null);
    }

    @Override
    protected String getSectionDescription()
    {
        return "Specify the server path (containing spigot.jar) and deploy path.";
    }

    @Override
    protected String getDoesNotModifyText()
    {
        return "(does not modify Spigot installation)";
    }

    @Override
    protected String takesControlText()
    {
        return "(takes control of Spigot installation)";
    }

    @Override
    protected String useInstallationText()
    {
        return "Use Spigot installation {0}";
    }
    
}
