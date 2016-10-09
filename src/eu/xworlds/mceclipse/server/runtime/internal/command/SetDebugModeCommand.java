/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.IMinecraftServerWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class SetDebugModeCommand extends ServerCommand {
    protected boolean debug;
    protected boolean oldDebug;

    /**
     * SetDebugModeCommand constructor comment.
     * 
     * @param server a spigot server
     * @param debug <code>true</code> for debug mode
     */
    public SetDebugModeCommand(IMinecraftServerWorkingCopy server, boolean debug) {
        super(server, "Change debug mode");
        this.debug = debug;
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        this.oldDebug = this.server.isDebug();
        this.server.setDebug(this.debug);
    }

    /**
     * Undo the command.
     */
    @Override
    public void undo() {
        this.server.setDebug(this.oldDebug);
    }
}
