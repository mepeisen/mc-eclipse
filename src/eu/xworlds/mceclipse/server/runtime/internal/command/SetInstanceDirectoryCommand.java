/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServerWorkingCopy;

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
    public SetInstanceDirectoryCommand(ISpigotServerWorkingCopy server, String instanceDir) {
        super(server, "Spigot Server Path Change");
        this.instanceDir = instanceDir;
    }

    /**
     * Execute setting the deploy directory
     */
    public void execute() {
        oldTestEnvironment = server.isTestEnvironment();
        oldInstanceDir = server.getInstanceDirectory();
        if (!oldTestEnvironment)
            server.setTestEnvironment(true);
        server.setInstanceDirectory(instanceDir);
    }

    /**
     * Restore prior deploy directory
     */
    public void undo() {
        if (!oldTestEnvironment)
            server.setTestEnvironment(false);
        server.setInstanceDirectory(oldInstanceDir);
    }

}
