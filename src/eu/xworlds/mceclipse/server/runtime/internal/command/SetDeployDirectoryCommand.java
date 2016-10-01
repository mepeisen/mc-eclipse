/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServerWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class SetDeployDirectoryCommand extends ServerCommand
{
    protected String deployDir;
    protected String oldDeployDir;

    /**
     * Constructs command to set the deploy directory.
     * 
     * @param server a Tomcat server
     * @param deployDir deployment directory to set
     */

    public SetDeployDirectoryCommand(ISpigotServerWorkingCopy server, String deployDir) {
        super(server, "Spigot Deploy Path Change");
        this.deployDir = deployDir;
    }

    /**
     * Execute setting the deploy directory
     */
    public void execute() {
        oldDeployDir = server.getDeployDirectory();
        server.setDeployDirectory(deployDir);
    }

    /**
     * Restore prior deploy directory
     */
    public void undo() {
        server.setDeployDirectory(oldDeployDir);
    }

}
