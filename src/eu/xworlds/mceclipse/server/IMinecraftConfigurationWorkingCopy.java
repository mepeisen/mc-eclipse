/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server;

import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author mepeisen
 * @param <Plugin>
 * @param <Lib>
 *
 */
public interface IMinecraftConfigurationWorkingCopy<Plugin extends IMinecraftPlugin, Lib extends IMinecraftLibrary> extends IMinecraftConfiguration<Plugin, Lib>
{
    
    /**
     * Adds a new spigot plugin at given index
     * 
     * @param index
     * @param plugin
     */
    void addPlugin(int index, Plugin plugin);
    
    /**
     * Removes spigot plugin at given index
     * 
     * @param index
     */
    void removePlugin(int index);
    
    /**
     * Adds a new spigot library at given index
     * 
     * @param index
     * @param library
     */
    void addLibrary(int index, Lib library);
    
    /**
     * Removes spigot library at given index
     * 
     * @param index
     */
    void removeLibrary(int index);
    
    /**
     * Adds property change listener.
     * 
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Removes property change listener.
     * 
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Loads configuration from given folder.
     * 
     * @param folder
     * @param monitor
     * @throws CoreException
     */
    void load(IFolder folder, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Loads configuration from given path.
     * 
     * @param path
     * @param monitor
     * @throws CoreException
     */
    void importFromPath(IPath path, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Saves config to given folder
     * 
     * @param serverConfiguration
     * @param monitor
     * @throws CoreException
     */
    void save(IFolder serverConfiguration, IProgressMonitor monitor) throws CoreException;

    /**
     * Changes the server port with given id
     * @param id
     * @param port
     */
    void setServerPort(String id, int port);
    
}
