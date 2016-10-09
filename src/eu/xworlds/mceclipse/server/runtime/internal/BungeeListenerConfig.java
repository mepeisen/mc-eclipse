/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * Configuration for a bungee listener.
 * 
 * @author mepeisen
 */
public class BungeeListenerConfig
{
    
    /**
     * listener index
     */
    private int index;
    
    /**
     * motd option.
     */
    private String motd;
    
    /**
     * max_players option.
     */
    private int maxPlayers;
    
    /**
     * force_default_server option.
     */
    private boolean isForceDefaults;
    
    /**
     * host option.
     */
    private InetSocketAddress host;
    
    /**
     * tab_size option.
     */
    private int tabSize;
    
    /**
     * forced_hosts option.
     */
    private Map<String, String> forcedHosts;
    
    /**
     * tab_list option.
     */
    private String tabListName;
    
    /**
     * bind_local_address option.
     */
    private boolean isBindLocalAddress;
    
    /**
     * ping_passthrough option.
     */
    private boolean isPingPassthrough;
    
    /**
     * query_enabled option.
     */
    private boolean isQueryEnabled;
    
    /**
     * query_port option.
     */
    private int queryPort;
    
    /**
     * priorities option.
     */
    private List<String> serverPriorities;
    
    /**
     * Constructor to create with defaults.
     * @param index
     */
    public BungeeListenerConfig(int index)
    {
        this.index = index;
        this.motd = "&1Another Bungee server"; //$NON-NLS-1$
        this.maxPlayers = 1;
        this.isForceDefaults = false;
        this.host = new InetSocketAddress(25577);
        this.tabSize = 60;
        this.forcedHosts = new HashMap<>();
        this.forcedHosts.put("pvp.md-5.net", "pvp"); //$NON-NLS-1$ //$NON-NLS-2$
        this.tabListName = "GLOBAL_PING"; //$NON-NLS-1$
        this.isBindLocalAddress = true;
        this.isPingPassthrough = false;
        this.isQueryEnabled = false;
        this.queryPort = 25577;
        this.serverPriorities = new ArrayList<>();
        this.serverPriorities.add("lobby"); //$NON-NLS-1$
    }
    
    /**
     * Constructor to read from yml map.
     * @param index
     * @param ymlMap
     * @throws CoreException 
     */
    @SuppressWarnings("unchecked")
    public BungeeListenerConfig(int index, Map<String, Object> ymlMap) throws CoreException
    {
        // init with defaults
        this(index);
        this.motd = (String) ymlMap.getOrDefault("motd", this.motd); //$NON-NLS-1$
        this.maxPlayers = (Integer) ymlMap.getOrDefault("max_players", this.maxPlayers); //$NON-NLS-1$
        this.isForceDefaults = (Boolean) ymlMap.getOrDefault("force_default_server", this.isForceDefaults); //$NON-NLS-1$
        if (ymlMap.containsKey("host")) this.host = McEclipsePlugin.getAdressFromConfig((String) ymlMap.get("host")); //$NON-NLS-1$ //$NON-NLS-2$
        if (ymlMap.containsKey("forced_hosts")) this.forcedHosts = (Map<String, String>) ymlMap.get("forced_hosts"); //$NON-NLS-1$ //$NON-NLS-2$
        this.tabListName = (String) ymlMap.getOrDefault("tab_list", this.tabListName); //$NON-NLS-1$
        this.isBindLocalAddress = (Boolean) ymlMap.getOrDefault("bind_local_address", this.isBindLocalAddress); //$NON-NLS-1$
        this.isPingPassthrough = (Boolean) ymlMap.getOrDefault("ping_passthrough", this.isPingPassthrough); //$NON-NLS-1$
        this.isQueryEnabled = (Boolean) ymlMap.getOrDefault("query_enabled", this.isQueryEnabled); //$NON-NLS-1$
        this.queryPort = (Integer) ymlMap.getOrDefault("query_port", this.queryPort); //$NON-NLS-1$
        final List<String> prio = (List<String>) ymlMap.getOrDefault("priorities", new ArrayList<>()); //$NON-NLS-1$
        if (ymlMap.containsKey("default_server")) prio.add((String) ymlMap.get("default_server")); //$NON-NLS-1$ //$NON-NLS-2$
        if (ymlMap.containsKey("fallback_server")) prio.add((String) ymlMap.get("fallback_server")); //$NON-NLS-1$ //$NON-NLS-2$
        if (prio.isEmpty()) prio.addAll(this.serverPriorities);
        this.serverPriorities = prio;
    }
    
    /**
     * Converts values to yml map.
     * @return yml map.
     */
    public Map<String, Object> toYmlMap()
    {
        final Map<String, Object> result = new HashMap<>();
        result.put("motd", this.motd); //$NON-NLS-1$
        result.put("max_players", this.maxPlayers); //$NON-NLS-1$
        result.put("force_default_server", this.isForceDefaults); //$NON-NLS-1$
        result.put("host", McEclipsePlugin.toConfig(this.host)); //$NON-NLS-1$
        result.put("forced_hosts", this.forcedHosts); //$NON-NLS-1$
        result.put("tab_list", this.tabSize); //$NON-NLS-1$
        result.put("bind_local_address", this.isBindLocalAddress); //$NON-NLS-1$
        result.put("ping_passthrough", this.isPingPassthrough); //$NON-NLS-1$
        result.put("query_enabled", this.isQueryEnabled); //$NON-NLS-1$
        result.put("query_port", this.queryPort); //$NON-NLS-1$
        result.put("priorities", this.serverPriorities); //$NON-NLS-1$
        return result;
    }

    /**
     * @return the index
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * @return the motd
     */
    public String getMotd()
    {
        return this.motd;
    }

    /**
     * @param motd the motd to set
     */
    public void setMotd(String motd)
    {
        this.motd = motd;
    }

    /**
     * @return the maxPlayers
     */
    public int getMaxPlayers()
    {
        return this.maxPlayers;
    }

    /**
     * @param maxPlayers the maxPlayers to set
     */
    public void setMaxPlayers(int maxPlayers)
    {
        this.maxPlayers = maxPlayers;
    }

    /**
     * @return the isForceDefaults
     */
    public boolean isForceDefaults()
    {
        return this.isForceDefaults;
    }

    /**
     * @param isForceDefaults the isForceDefaults to set
     */
    public void setForceDefaults(boolean isForceDefaults)
    {
        this.isForceDefaults = isForceDefaults;
    }

    /**
     * @return the host
     */
    public InetSocketAddress getHost()
    {
        return this.host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(InetSocketAddress host)
    {
        this.host = host;
    }

    /**
     * @return the tabSize
     */
    public int getTabSize()
    {
        return this.tabSize;
    }

    /**
     * @param tabSize the tabSize to set
     */
    public void setTabSize(int tabSize)
    {
        this.tabSize = tabSize;
    }

    /**
     * @return the forcedHosts
     */
    public Map<String, String> getForcedHosts()
    {
        return this.forcedHosts;
    }

    /**
     * @param forcedHosts the forcedHosts to set
     */
    public void setForcedHosts(Map<String, String> forcedHosts)
    {
        this.forcedHosts = forcedHosts;
    }

    /**
     * @return the tabListName
     */
    public String getTabListName()
    {
        return this.tabListName;
    }

    /**
     * @param tabListName the tabListName to set
     */
    public void setTabListName(String tabListName)
    {
        this.tabListName = tabListName;
    }

    /**
     * @return the isBindLocalAddress
     */
    public boolean isBindLocalAddress()
    {
        return this.isBindLocalAddress;
    }

    /**
     * @param isBindLocalAddress the isBindLocalAddress to set
     */
    public void setBindLocalAddress(boolean isBindLocalAddress)
    {
        this.isBindLocalAddress = isBindLocalAddress;
    }

    /**
     * @return the isPingPassthrough
     */
    public boolean isPingPassthrough()
    {
        return this.isPingPassthrough;
    }

    /**
     * @param isPingPassthrough the isPingPassthrough to set
     */
    public void setPingPassthrough(boolean isPingPassthrough)
    {
        this.isPingPassthrough = isPingPassthrough;
    }

    /**
     * @return the isQueryEnabled
     */
    public boolean isQueryEnabled()
    {
        return this.isQueryEnabled;
    }

    /**
     * @param isQueryEnabled the isQueryEnabled to set
     */
    public void setQueryEnabled(boolean isQueryEnabled)
    {
        this.isQueryEnabled = isQueryEnabled;
    }

    /**
     * @return the queryPort
     */
    public int getQueryPort()
    {
        return this.queryPort;
    }

    /**
     * @param queryPort the queryPort to set
     */
    public void setQueryPort(int queryPort)
    {
        this.queryPort = queryPort;
    }

    /**
     * @return the serverPriorities
     */
    public List<String> getServerPriorities()
    {
        return this.serverPriorities;
    }

    /**
     * @param serverPriorities the serverPriorities to set
     */
    public void setServerPriorities(List<String> serverPriorities)
    {
        this.serverPriorities = serverPriorities;
    }
    
}
