/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import org.eclipse.wst.server.core.ServerPort;

import eu.xworlds.mceclipse.server.IMinecraftConfigurationWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class ModifyPortCommand extends ConfigurationCommand
{
    
    /** port id. */
    protected String id;
    /** new value. */
    protected int    port;
    /** old value. */
    protected int    oldPort;
    
    /**
     * ModifyPortCommand constructor.
     * 
     * @param configuration
     *            a spigot configuration
     * @param id
     *            a port id
     * @param port
     *            new port number
     */
    public ModifyPortCommand(IMinecraftConfigurationWorkingCopy<?, ?> configuration, String id, int port)
    {
        super(configuration, "modify port");
        this.id = id;
        this.port = port;
    }
    
    /**
     * Execute the command.
     */
    @Override
    public void execute()
    {
        for (final ServerPort p : this.configuration.getServerPorts())
        {
            if (this.id.equals(p.getId()))
            {
                this.oldPort = p.getPort();
            }
        }
        
        // make the change
        this.configuration.setServerPort(this.id, this.port);
    }
    
    /**
     * Undo the command.
     */
    @Override
    public void undo()
    {
        this.configuration.setServerPort(this.id, this.oldPort);
    }
    
}
