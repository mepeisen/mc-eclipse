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
public class BungeeGroupsConfig
{
    
    /** the permissions. */
    private Map<String, List<String>> groups = new HashMap<>();
    
    /**
     * Constructor to create with defaults.
     */
    public BungeeGroupsConfig()
    {
        this.groups.put("md_5", Arrays.asList(new String[]{"admin"})); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Constructor to read from yml map.
     * @param ymlMap
     * @throws CoreException 
     */
    @SuppressWarnings("unchecked")
    public BungeeGroupsConfig(Map<String, Object> ymlMap) throws CoreException
    {
        this();
        if (!ymlMap.isEmpty())
        {
            this.groups.clear();
            for (final Map.Entry<String, Object> entry : ymlMap.entrySet())
            {
                this.groups.put(entry.getKey(), (List<String>) entry.getValue());
            }
        }
    }
    
    /**
     * Converts values to yml map.
     * @return yml map.
     */
    public Map<String, Object> toYmlMap()
    {
        return new HashMap<>(this.groups);
    }

    /**
     * @return the groups
     */
    public Map<String, List<String>> getGroups()
    {
        return this.groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(Map<String, List<String>> groups)
    {
        this.groups = groups;
    }
    
    
    
}
