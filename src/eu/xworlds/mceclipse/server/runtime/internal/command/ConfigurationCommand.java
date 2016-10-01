/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotConfigurationWorkingCopy;

/**
 * @author mepeisen
 *
 */
public abstract class ConfigurationCommand extends AbstractOperation {
    protected ISpigotConfigurationWorkingCopy configuration;

    /**
     * ConfigurationCommand constructor comment.
     * 
     * @param configuration a Tomcat configuration
     * @param label a label
     */
    public ConfigurationCommand(ISpigotConfigurationWorkingCopy configuration, String label) {
        super(label);
        this.configuration = configuration;
    }

    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        return execute(monitor, info);
    }

    public abstract void execute();

    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        execute();
        return null;
    }

    public abstract void undo();

    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        undo();
        return null;
    }
}
