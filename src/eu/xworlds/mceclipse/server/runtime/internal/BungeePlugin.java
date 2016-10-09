/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import eu.xworlds.mceclipse.server.IMinecraftPlugin;

/**
 * @author mepeisen
 *
 */
public class BungeePlugin implements IMinecraftPlugin
{

    /** plugin id */
    private String id;
    /** project reference */
    private IProject project;

    /**
     * @param id
     * @param project
     */
    public BungeePlugin(String id, IProject project)
    {
        this.id = id;
        this.project = project;
    }

    /**
     * 
     */
    public BungeePlugin()
    {
        // empty
    }

    @Override
    public String getMemento()
    {
        return this.id;
    }
    
    @Override
    public IProject getProject()
    {
        return this.project;
    }

    @Override
    public void saveConfig(Properties pluginProperties, int key)
    {
        pluginProperties.setProperty("plugin" + key + ".id", this.id); //$NON-NLS-1$ //$NON-NLS-2$
        pluginProperties.setProperty("plugin" + key + ".type", "project"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pluginProperties.setProperty("plugin" + key + ".name", this.project.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void readConfig(Properties pluginProperties, int key)
    {
        this.id = pluginProperties.getProperty("plugin" + key + ".id"); //$NON-NLS-1$ //$NON-NLS-2$
        final String type = pluginProperties.getProperty("plugin" + key + ".type"); //$NON-NLS-1$ //$NON-NLS-2$
        switch (type)
        {
            case "project": //$NON-NLS-1$
                this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(pluginProperties.getProperty("plugin" + key + ".name")); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            default:
                throw new IllegalStateException("Unknown plugin type: " + type); //$NON-NLS-1$
        }
    }
    
}
