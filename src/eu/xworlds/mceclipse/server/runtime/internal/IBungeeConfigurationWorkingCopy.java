/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import eu.xworlds.mceclipse.server.IMinecraftConfigurationWorkingCopy;

/**
 * @author mepeisen
 *
 */
public interface IBungeeConfigurationWorkingCopy extends IBungeeConfiguration, IMinecraftConfigurationWorkingCopy<BungeePlugin, BungeeLibrary>
{
    
    /**
     * @param value ip_forward option
     */
    void setIpForward(boolean value);
    
    /**
     * @param value network_compression_threshold option
     */
    void setNetworkCompressionThreshold(int value);
    
    /**
     * @param value stats option
     */
    void setStats(String value);
    
    /**
     * @param value permissions
     */
    void setPermissions(BungeePermissionConfig value);
    
    /**
     * @param value groups
     */
    void setGroups(BungeeGroupsConfig value);
    
    /**
     * Adds a server
     * @param value
     */
    void addOrChangeServer(BungeeServerConfig value);
    
    /**
     * Removes a server
     * @param name
     */
    void removeServer(String name);
    
    /**
     * @param value timeout option
     */
    void setTimeout(int value);
    
    /**
     * Adds a listener
     * @param value
     */
    void addOrChangeListener(BungeeListenerConfig value);
    
    /**
     * Removes a listener
     * @param index
     */
    void removeListener(int index);
    
    /**
     * @param value player_limit option
     */
    void setPlayerLimit(int value);
    
    /**
     * @param value online_mode option
     */
    void setOnlineMode(boolean value);
    
    /**
     * @param value log_commands option
     */
    void setLogCommands(boolean value);
    
    /**
     * Adds disabled command
     * @param command
     */
    void addDisabledCommand(String command);
    
    /**
     * Removes disabled command
     * @param command
     */
    void removeDisabledCommand(String command);
    
    /**
     * @param value connection_throttle option
     */
    void setConnectionThrottle(int value);
    
}
