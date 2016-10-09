/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import eu.xworlds.mceclipse.server.IMinecraftLibrary;

/**
 * @author mepeisen
 *
 */
public class BungeeLibrary implements IMinecraftLibrary
{

    /** plugin id */
    private String id;
    /** project reference */
    private IProject project;

    /**
     * @param id
     * @param project
     */
    public BungeeLibrary(String id, IProject project)
    {
        this.id = id;
        this.project = project;
    }

    /**
     * 
     */
    public BungeeLibrary()
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
    public void saveConfig(Properties libraryProperties, int key)
    {
        libraryProperties.setProperty("library" + key + ".id", this.id); //$NON-NLS-1$ //$NON-NLS-2$
        libraryProperties.setProperty("library" + key + ".type", "project"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        libraryProperties.setProperty("library" + key + ".name", this.project.getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void readConfig(Properties libraryProperties, int key)
    {
        this.id = libraryProperties.getProperty("library" + key + ".id"); //$NON-NLS-1$ //$NON-NLS-2$
        final String type = libraryProperties.getProperty("library" + key + ".type"); //$NON-NLS-1$ //$NON-NLS-2$
        switch (type)
        {
            case "project": //$NON-NLS-1$
                this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(libraryProperties.getProperty("library" + key + ".name")); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            default:
                throw new IllegalStateException("Unknown library type: " + type); //$NON-NLS-1$
        }
    }
    
}
