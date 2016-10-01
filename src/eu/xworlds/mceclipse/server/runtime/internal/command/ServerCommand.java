/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal.command;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServerWorkingCopy;
import eu.xworlds.mceclipse.server.runtime.internal.SpigotServer;

/**
 * @author mepeisen
 *
 */
public abstract class ServerCommand extends AbstractOperation
{
    
    /** spigot server. */
    protected SpigotServer server;
    
    /**
     * ServerCommand constructor comment.
     * 
     * @param server
     *            a Spigot server
     * @param label
     *            a label
     */
    public ServerCommand(ISpigotServerWorkingCopy server, String label)
    {
        super(label);
        this.server = (SpigotServer) server;
    }
    
    @Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        return execute(monitor, info);
    }
    
    /**
     * perform
     */
    public abstract void execute();
    
    @Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        execute();
        return null;
    }
    
    /**
     * undo
     */
    public abstract void undo();
    
    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        undo();
        return null;
    }
}
