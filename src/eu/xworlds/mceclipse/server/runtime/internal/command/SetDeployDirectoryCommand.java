/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.IMinecraftServerWorkingCopy;

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
     * @param server a spigot server
     * @param deployDir deployment directory to set
     */

    public SetDeployDirectoryCommand(IMinecraftServerWorkingCopy server, String deployDir) {
        super(server, "Spigot Deploy Path Change");
        this.deployDir = deployDir;
    }

    /**
     * Execute setting the deploy directory
     */
    @Override
    public void execute() {
        this.oldDeployDir = this.server.getDeployDirectory();
        this.server.setDeployDirectory(this.deployDir);
    }

    /**
     * Restore prior deploy directory
     */
    @Override
    public void undo() {
        this.server.setDeployDirectory(this.oldDeployDir);
    }

}
