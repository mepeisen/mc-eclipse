/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import eu.xworlds.mceclipse.server.IMinecraftConfigurationWorkingCopy;

/**
 * @author mepeisen
 *
 */
public interface ISpigotConfigurationWorkingCopy extends ISpigotConfiguration, IMinecraftConfigurationWorkingCopy<SpigotPlugin, SpigotLibrary>
{
    
    /**
     * Property to listen for port modifications
     */
    String MODIFY_PORT_PROPERTY = "modifyPort"; //$NON-NLS-1$
    
    /**
     * Sets the current server port.
     * 
     * @param port
     *            new port.
     */
    void setServerPort(int port);
    
}
