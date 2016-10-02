/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServerWorkingCopy;

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
    public SetServeModulesWithoutPublishCommand(ISpigotServerWorkingCopy server, boolean smwp) {
        super(server, "Changing public modules");
        this.smwp = smwp;
    }

    /**
     * Execute the command.
     */
    public void execute() {
        oldSmwp = server.isServeModulesWithoutPublish();
        server.setServeModulesWithoutPublish(smwp);
    }

    /**
     * Undo the command.
     */
    public void undo() {
        server.setServeModulesWithoutPublish(oldSmwp);
    }
}
