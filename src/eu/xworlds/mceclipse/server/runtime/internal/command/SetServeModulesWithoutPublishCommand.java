/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.IMinecraftServerWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class SetServeModulesWithoutPublishCommand extends ServerCommand {
    protected boolean smwp;
    protected boolean oldSmwp;

    /**
     * SetServeModulesWithoutPublishCommand constructor comment.
     * 
     * @param server a spigot server
     * @param smwp <code>true</code> to enable serving modules without
     * publishing. Otherwise modules are served with standard publishing.
     */
    public SetServeModulesWithoutPublishCommand(IMinecraftServerWorkingCopy server, boolean smwp) {
        super(server, "Changing public modules");
        this.smwp = smwp;
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        this.oldSmwp = this.server.isServeModulesWithoutPublish();
        this.server.setServeModulesWithoutPublish(this.smwp);
    }

    /**
     * Undo the command.
     */
    @Override
    public void undo() {
        this.server.setServeModulesWithoutPublish(this.oldSmwp);
    }
}
