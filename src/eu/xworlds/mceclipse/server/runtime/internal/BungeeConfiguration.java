/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.ServerPort;

/**
 * @author mepeisen
 *
 */
public class BungeeConfiguration extends AbstractConfiguration<BungeePlugin, BungeeLibrary> implements IBungeeConfigurationWorkingCopy
{
    
    /** the config yml. */
    private Map<String, Object> configYml = new HashMap<>();
    
    /**
     * Constructor.
     * 
     * @param folder
     */
    public BungeeConfiguration(IFolder folder)
    {
        super(folder);
    }
    
    @Override
    protected FolderLoader[] getFolderLoadSteps()
    {
        return new FolderLoader[] { (f, monitor) -> {
            final IFile config = f.getFile("config.yml"); //$NON-NLS-1$
            this.loadYmlFromFile(this.configYml, config, monitor);
        } };
    }
    
    @Override
    protected PathLoader[] getPathLoadSteps()
    {
        return new PathLoader[] { (path, monitor) -> {
            final File config = path.append("config.yml").toFile(); //$NON-NLS-1$
            this.loadYmlFromFile(this.configYml, config);
        } };
    }
    
    @Override
    protected FolderSaver[] getFolderSaveSteps()
    {
        return new FolderSaver[] { (f, monitor) -> {
            final IFile config = f.getFile("config.yml"); //$NON-NLS-1$
            this.saveYmlToFile(this.configYml, config, monitor);
        } };
    }
    
    @Override
    protected PathSaver[] getPathSaveSteps()
    {
        return new PathSaver[] { (path, monitor) -> {
            final File config = path.append("config.yml").toFile(); //$NON-NLS-1$
            this.saveYmlToFile(this.configYml, config);
        } };
    }
    
    @Override
    protected BungeePlugin createPlugin()
    {
        return new BungeePlugin();
    }
    
    @Override
    protected BungeeLibrary createLibrary()
    {
        return new BungeeLibrary();
    }

    @Override
    public ServerPort[] getServerPorts()
    {
        final List<ServerPort> result = new ArrayList<>();
        for (final BungeeListenerConfig listener : this.getListeners())
        {
            result.add(new ServerPort("listener" + listener.getIndex(), "listener" + listener.getIndex(), listener.getHost().getPort(), "spigot")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (listener.isQueryEnabled())
            {
                result.add(new ServerPort("query" + listener.getIndex(), "query" + listener.getIndex(), listener.getQueryPort(), "spigot")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        return result.toArray(new ServerPort[result.size()]);
    }

    @Override
    public void setServerPort(String id, int port)
    {
        for (final BungeeListenerConfig listener : this.getListeners())
        {
            if (id.equals("listener" + listener.getIndex())) //$NON-NLS-1$
            {
                listener.setHost(new InetSocketAddress(listener.getHost().getHostName(), port));
                this.addOrChangeListener(listener);
            }
            else if (id.equals("query" + listener.getIndex())) //$NON-NLS-1$
            {
                listener.setQueryPort(port);
                this.addOrChangeListener(listener);
            }
        }
    }

    @Override
    public boolean isIpForward()
    {
        return (Boolean) this.configYml.getOrDefault("ip_forward", false); //$NON-NLS-1$
    }

    @Override
    public int getNetworkCompressionThreshold()
    {
        return (Integer) this.configYml.getOrDefault("network_connection_threshold", 256); //$NON-NLS-1$
    }

    @Override
    public String getStats()
    {
        return (String) this.configYml.getOrDefault("stats", null); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    public BungeePermissionConfig getPermissions()
    {
        try
        {
            return new BungeePermissionConfig((Map<String, Object>) this.configYml.get("permissions")); //$NON-NLS-1$
        }
        catch (CoreException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BungeeGroupsConfig getGroups()
    {
        try
        {
            return new BungeeGroupsConfig((Map<String, Object>) this.configYml.get("groups")); //$NON-NLS-1$
        }
        catch (CoreException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<BungeeServerConfig> getServers()
    {
        final List<BungeeServerConfig> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> servers = (Map<String, Map<String, Object>>) this.configYml.get("servers"); //$NON-NLS-1$
        for (final Map.Entry<String, Map<String, Object>> entry : servers.entrySet())
        {
            try
            {
                result.add(new BungeeServerConfig(entry.getKey(), entry.getValue()));
            }
            catch (CoreException e)
            {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    @Override
    public int getTimeout()
    {
        return (Integer) this.configYml.getOrDefault("timeout", 30000); //$NON-NLS-1$
    }

    @Override
    public List<BungeeListenerConfig> getListeners()
    {
        final List<BungeeListenerConfig> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> listeners = (List<Map<String, Object>>) this.configYml.get("listeners"); //$NON-NLS-1$
        int index = 0;
        for (final Map<String, Object> entry : listeners)
        {
            try
            {
                result.add(new BungeeListenerConfig(index, entry));
            }
            catch (CoreException e)
            {
                throw new IllegalStateException(e);
            }
            index++;
        }
        return result;
    }

    @Override
    public int getPlayerLimit()
    {
        return (Integer) this.configYml.getOrDefault("player_limit", -1); //$NON-NLS-1$
    }

    @Override
    public boolean isOnlineMode()
    {
        return (Boolean) this.configYml.getOrDefault("online_mode", true); //$NON-NLS-1$
    }

    @Override
    public boolean isLogCommands()
    {
        return (Boolean) this.configYml.getOrDefault("log_commands", false); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getDisabledCommands()
    {
        return (List<String>) this.configYml.get("disabled_commands"); //$NON-NLS-1$
    }

    @Override
    public int getConnectionThrottle()
    {
        return (Integer) this.configYml.getOrDefault("connection_throttle", 4000); //$NON-NLS-1$
    }

    @Override
    public void setIpForward(boolean value)
    {
        this.configYml.put("ip_forward", value); //$NON-NLS-1$
    }

    @Override
    public void setNetworkCompressionThreshold(int value)
    {
        this.configYml.put("network_connection_threshold", value); //$NON-NLS-1$
    }

    @Override
    public void setStats(String value)
    {
        this.configYml.put("stats", value); //$NON-NLS-1$
    }

    @Override
    public void setPermissions(BungeePermissionConfig value)
    {
        this.configYml.put("permissions", value.toYmlMap()); //$NON-NLS-1$
    }

    @Override
    public void setGroups(BungeeGroupsConfig value)
    {
        this.configYml.put("groups", value.toYmlMap()); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addOrChangeServer(BungeeServerConfig value)
    {
        ((Map<String, Object>)this.configYml.get("servers")).put(value.getName(), value.toYmlMap()); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeServer(String name)
    {
        ((Map<String, Object>)this.configYml.get("servers")).remove(name); //$NON-NLS-1$
    }

    @Override
    public void setTimeout(int value)
    {
        this.configYml.put("timeout", value); //$NON-NLS-1$
    }

    @Override
    public void addOrChangeListener(BungeeListenerConfig value)
    {
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> list = (List<Map<String,Object>>) this.configYml.get("listeners"); //$NON-NLS-1$
        if (value.getIndex() == -1)
        {
            list.add(value.toYmlMap());
            value.setIndex(list.size() - 1);
        }
        else
        {
            list.set(value.getIndex(), value.toYmlMap());
        }
    }

    @Override
    public void removeListener(int index)
    {
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> list = (List<Map<String,Object>>) this.configYml.get("listeners"); //$NON-NLS-1$
        list.remove(index);
    }

    @Override
    public void setPlayerLimit(int value)
    {
        this.configYml.put("player_limit", value); //$NON-NLS-1$
    }

    @Override
    public void setOnlineMode(boolean value)
    {
        this.configYml.put("online_mode", value); //$NON-NLS-1$
    }

    @Override
    public void setLogCommands(boolean value)
    {
        this.configYml.put("log_commands", value); //$NON-NLS-1$
    }

    @Override
    public void addDisabledCommand(String command)
    {
        @SuppressWarnings("unchecked")
        final List<String> list = (List<String>)this.configYml.get("disabled_commands"); //$NON-NLS-1$
        if (!list.contains(command))
        {
            list.add(command);
        }
    }

    @Override
    public void removeDisabledCommand(String command)
    {
        @SuppressWarnings("unchecked")
        final List<String> list = (List<String>)this.configYml.get("disabled_commands"); //$NON-NLS-1$
        list.remove(command);
    }

    @Override
    public void setConnectionThrottle(int value)
    {
        this.configYml.put("connection_throttle", value); //$NON-NLS-1$
    }
    
}
