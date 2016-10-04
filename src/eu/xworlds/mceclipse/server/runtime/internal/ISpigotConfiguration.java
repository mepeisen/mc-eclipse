/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import org.eclipse.wst.server.core.ServerPort;

/**
 * @author mepeisen
 *
 */
public interface ISpigotConfiguration
{
    
    public static final String MODIFY_PORT_PROPERTY = "modifyPort";
    
    /**
     * Returns the current server port.
     * @return server port.
     */
    ServerPort getServerPort();
    
    /**
     * Returns an immutable list of added spigot plugins.
     * 
     * @return immutable list of plugins.
     */
    List<SpigotPlugin> getSpigotPlugins();
    
    /**
     * Returns an immutable list of added spigot libraries.
     * 
     * @return immutable list of libraries.
     */
    List<SpigotLibrary> getSpigotLibraries();
    
}
