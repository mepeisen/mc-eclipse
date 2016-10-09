/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.IMinecraftServerWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class SetInstanceDirectoryCommand extends ServerCommand
{
    protected String instanceDir;
    protected String oldInstanceDir;
    protected boolean oldTestEnvironment;

    /**
     * Constructs command to set the instance directory. Setting
     * the instance directory also sets testEnvironment true;
     * 
     * @param server a spigot server
     * @param instanceDir instance directory to set
     */
    public SetInstanceDirectoryCommand(IMinecraftServerWorkingCopy server, String instanceDir) {
        super(server, "Spigot Server Path Change");
        this.instanceDir = instanceDir;
    }

    /**
     * Execute setting the deploy directory
     */
    @Override
    public void execute() {
        this.oldTestEnvironment = this.server.isTestEnvironment();
        this.oldInstanceDir = this.server.getInstanceDirectory();
        if (!this.oldTestEnvironment)
            this.server.setTestEnvironment(true);
        this.server.setInstanceDirectory(this.instanceDir);
    }

    /**
     * Restore prior deploy directory
     */
    @Override
    public void undo() {
        if (!this.oldTestEnvironment)
            this.server.setTestEnvironment(false);
        this.server.setInstanceDirectory(this.oldInstanceDir);
    }

}
