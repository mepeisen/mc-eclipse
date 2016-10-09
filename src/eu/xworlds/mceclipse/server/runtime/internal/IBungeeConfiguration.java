/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import eu.xworlds.mceclipse.server.IMinecraftConfiguration;

/**
 * @author mepeisen
 *
 */
public interface IBungeeConfiguration extends IMinecraftConfiguration<BungeePlugin, BungeeLibrary>
{
    
    /**
     * @return ip_forward option
     */
    boolean isIpForward();
    
    /**
     * @return network_compression_threshold option
     */
    int getNetworkCompressionThreshold();
    
    /**
     * @return stats option
     */
    String getStats();
    
    /**
     * @return permissions
     */
    BungeePermissionConfig getPermissions();
    
    /**
     * @return groups
     */
    BungeeGroupsConfig getGroups();
    
    /**
     * @return servers
     */
    List<BungeeServerConfig> getServers();
    
    /**
     * @return timeout option
     */
    int getTimeout();
    
    /**
     * @return listeners
     */
    List<BungeeListenerConfig> getListeners();
    
    /**
     * @return player_limit option
     */
    int getPlayerLimit();
    
    /**
     * @return online_mode option
     */
    boolean isOnlineMode();
    
    /**
     * @return log_commands option
     */
    boolean isLogCommands();
    
    /**
     * @return disabled commands
     */
    List<String> getDisabledCommands();
    
    /**
     * @return connection_throttle option
     */
    int getConnectionThrottle();
    
}
