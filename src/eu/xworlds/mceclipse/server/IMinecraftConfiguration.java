/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server;

import java.util.List;

import org.eclipse.wst.server.core.ServerPort;

/**
 * @author mepeisen
 * @param <Plugin> plugin class
 * @param <Lib> library class
 *
 */
public interface IMinecraftConfiguration<Plugin extends IMinecraftPlugin, Lib extends IMinecraftLibrary>
{
    
    /**
     * Returns an immutable list of added spigot plugins.
     * 
     * @return immutable list of plugins.
     */
    List<Plugin> getPlugins();
    
    /**
     * Returns an immutable list of added spigot libraries.
     * 
     * @return immutable list of libraries.
     */
    List<Lib> getLibraries();
    
    /**
     * Returns the server ports.
     * 
     * @return server ports.
     */
    ServerPort[] getServerPorts();
    
}
