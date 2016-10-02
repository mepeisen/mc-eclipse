/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import java.util.Iterator;

import org.eclipse.core.internal.expressions.Messages;
import org.eclipse.wst.server.core.ServerPort;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotConfigurationWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class ModifyPortCommand extends ConfigurationCommand {
    protected String id;
    protected int port;
    protected int oldPort;

    /**
     * ModifyPortCommand constructor.
     * 
     * @param configuration a spigot configuration
     * @param id a port id
     * @param port new port number
     */
    public ModifyPortCommand(ISpigotConfigurationWorkingCopy configuration, String id, int port) {
        super(configuration, "modify port");
        this.id = id;
        this.port = port;
    }

    /**
     * Execute the command.
     */
    public void execute() {
        oldPort = configuration.getServerPort().getPort();
    
        // make the change
        configuration.setServerPort(port);
    }

    /**
     * Undo the command.
     */
    public void undo() {
        configuration.setServerPort(oldPort);
    }
    
}
