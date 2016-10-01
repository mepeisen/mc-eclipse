/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServerWorkingCopy;

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
    public SetTestEnvironmentCommand(ISpigotServerWorkingCopy server, boolean te)
    {
        super(server, "Spigot Server Path Change");
        this.te = te;
    }
    
    /**
     * Execute the command.
     */
    public void execute()
    {
        oldTe = server.isTestEnvironment();
        // save old instance directory
        oldInstanceDir = server.getInstanceDirectory();
        server.setTestEnvironment(te);
        // ensure instance directory is cleared
        server.setInstanceDirectory(null);
    }
    
    /**
     * Undo the command.
     */
    public void undo()
    {
        server.setTestEnvironment(oldTe);
        server.setInstanceDirectory(oldInstanceDir);
    }
}
