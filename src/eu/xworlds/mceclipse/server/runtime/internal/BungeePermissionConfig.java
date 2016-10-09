/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

/**
 * @author mepeisen
 *
 */
public class BungeePermissionConfig
{
    
    /** the permissions. */
    private Map<String, List<String>> permissions = new HashMap<>();
    
    /**
     * Constructor to create with defaults.
     */
    public BungeePermissionConfig()
    {
        this.permissions.put("default", Arrays.asList(new String[]{"bungeecord.command.server", "bungeecord.command.list"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.permissions.put("admin", Arrays.asList(new String[]{"bungeecord.command.alert", "bungeecord.command.end", "bungeecord.command.ip", "bungeecord.command.reload"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
    
    /**
     * Constructor to read from yml map.
     * @param ymlMap
     * @throws CoreException 
     */
    @SuppressWarnings("unchecked")
    public BungeePermissionConfig(Map<String, Object> ymlMap) throws CoreException
    {
        this();
        if (!ymlMap.isEmpty())
        {
            this.permissions.clear();
            for (final Map.Entry<String, Object> entry : ymlMap.entrySet())
            {
                this.permissions.put(entry.getKey(), (List<String>) entry.getValue());
            }
        }
    }
    
    /**
     * Converts values to yml map.
     * @return yml map.
     */
    public Map<String, Object> toYmlMap()
    {
        return new HashMap<>(this.permissions);
    }

    /**
     * @return the permissions
     */
    public Map<String, List<String>> getPermissions()
    {
        return this.permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(Map<String, List<String>> permissions)
    {
        this.permissions = permissions;
    }
    
    
    
}
