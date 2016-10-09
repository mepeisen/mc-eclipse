/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.IMinecraftServerWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class SetTestEnvironmentCommand extends ServerCommand
{
    protected boolean te;
    protected boolean oldTe;
    protected String  oldInstanceDir;
    
    /**
     * SetTestEnvironmentCommand constructor comment.
     * 
     * @param server
     *            a Spigot server
     * @param te
     *            <code>true</code> for a test environment.
     */
    public SetTestEnvironmentCommand(IMinecraftServerWorkingCopy server, boolean te)
    {
        super(server, "Spigot Server Path Change");
        this.te = te;
    }
    
    /**
     * Execute the command.
     */
    @Override
    public void execute()
    {
        this.oldTe = this.server.isTestEnvironment();
        // save old instance directory
        this.oldInstanceDir = this.server.getInstanceDirectory();
        this.server.setTestEnvironment(this.te);
        // ensure instance directory is cleared
        this.server.setInstanceDirectory(null);
    }
    
    /**
     * Undo the command.
     */
    @Override
    public void undo()
    {
        this.server.setTestEnvironment(this.oldTe);
        this.server.setInstanceDirectory(this.oldInstanceDir);
    }
}
