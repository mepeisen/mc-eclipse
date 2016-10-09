/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * Configuration for a server.
 * 
 * @author mepeisen
 */
public class BungeeServerConfig
{
    
    /**
     * server name.
     */
    private String name;
    
    /**
     * motd option.
     */
    private String motd;
    
    /**
     * address option.
     */
    private InetSocketAddress address;
    
    /**
     * restricted option.
     */
    private boolean isRestricted;
    
    /**
     * Constructor to create with defaults.
     * @param name server name
     */
    public BungeeServerConfig(String name)
    {
        this.name = name;
        this.motd = "&1Just another BungeeCord - Forced Host"; //$NON-NLS-1$
        this.address = new InetSocketAddress(25565);
        this.isRestricted = false;
    }
    
    /**
     * Constructor to read from yml map.
     * @param name server name
     * @param ymlMap
     * @throws CoreException 
     */
    public BungeeServerConfig(String name, Map<String, Object> ymlMap) throws CoreException
    {
        // init with defaults
        this(name);
        this.motd = (String) ymlMap.getOrDefault("motd", this.motd); //$NON-NLS-1$
        if (ymlMap.containsKey("address")) this.address = McEclipsePlugin.getAdressFromConfig((String) ymlMap.get("address")); //$NON-NLS-1$ //$NON-NLS-2$
        this.isRestricted = (Boolean) ymlMap.getOrDefault("restricted", this.isRestricted); //$NON-NLS-1$
    }
    
    /**
     * Converts values to yml map.
     * @return yml map.
     */
    public Map<String, Object> toYmlMap()
    {
        final Map<String, Object> result = new HashMap<>();
        result.put("motd", this.motd); //$NON-NLS-1$
        result.put("address", McEclipsePlugin.toConfig(this.address)); //$NON-NLS-1$
        result.put("restricted", this.isRestricted); //$NON-NLS-1$
        return result;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
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
     * @return the address
     */
    public InetSocketAddress getAddress()
    {
        return this.address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(InetSocketAddress address)
    {
        this.address = address;
    }

    /**
     * @return the isRestricted
     */
    public boolean isRestricted()
    {
        return this.isRestricted;
    }

    /**
     * @param isRestricted the isRestricted to set
     */
    public void setRestricted(boolean isRestricted)
    {
        this.isRestricted = isRestricted;
    }
    
}
