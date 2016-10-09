/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.wst.server.core.ServerPort;

import eu.xworlds.mceclipse.server.IMinecraftConfiguration;

/**
 * @author mepeisen
 *
 */
public interface ISpigotConfiguration extends IMinecraftConfiguration<SpigotPlugin, SpigotLibrary>
{
    
    /**
     * Returns the current server port.
     * @return server port.
     */
    ServerPort getServerPort();
    
}
