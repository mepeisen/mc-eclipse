/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.beans.PropertyChangeListener;

/**
 * @author mepeisen
 *
 */
public interface ISpigotConfigurationWorkingCopy extends ISpigotConfiguration
{
    
    /**
     * Sets the current server port.
     * 
     * @param port
     *            new port.
     */
    void setServerPort(int port);
    
    /**
     * Adds a new spigot plugin at given index
     * @param index
     * @param plugin
     */
    void addSpigotPlugin(int index, SpigotPlugin plugin);
    
    /**
     * Removes spigot plugin at given index
     * @param index
     */
    void removeSpigotPlugin(int index);
    
    /**
     * Adds a new spigot library at given index
     * @param index
     * @param library
     */
    void addSpigotLibrary(int index, SpigotLibrary library);
    
    /**
     * Removes spigot library at given index
     * @param index
     */
    void removeSpigotLibrary(int index);
    
    void addPropertyChangeListener(PropertyChangeListener listener); 
    void removePropertyChangeListener(PropertyChangeListener listener);

    
}
