/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftConfigurationWorkingCopy;
import eu.xworlds.mceclipse.server.IMinecraftLibrary;
import eu.xworlds.mceclipse.server.IMinecraftPlugin;
import eu.xworlds.mceclipse.server.IMinecraftRuntime;
import eu.xworlds.mceclipse.server.IMinecraftServerWorkingCopy;

/**
 * @author mepeisen
 * @param <Server> 
 * @param <Plugin> 
 * @param <Lib> 
 */
public abstract class AbstractServer<Server extends AbstractServer<Server, Plugin, Lib>, Plugin extends IMinecraftPlugin, Lib extends IMinecraftLibrary> extends ServerDelegate implements IMinecraftServerWorkingCopy
{
    
    /** version handler. */
    protected transient IVersionHandler<Server> versionHandler;
    
    /** config. */
    protected transient IMinecraftConfigurationWorkingCopy<Plugin, Lib> configuration;
    
    /** version. */
    private int currentVersion;
    
    /** version. */
    private int loadedVersion;
    
    /** lock. */
    private Object versionLock = new Object(); 
    
    /**
     * Get the Spigot runtime for this server.
     * 
     * @return Spigot runtime for this server
     */
    public abstract IMinecraftRuntime getRuntime();
    
    /**
     * Gets the Spigot version handler for this server.
     * 
     * @return version handler for this server
     */
    public abstract IVersionHandler<Server> getVersionHandler();
    
    /**
     * Returns the spigot configuration.
     * @return spigot configuration.
     * @throws CoreException
     */
    public IMinecraftConfigurationWorkingCopy<Plugin, Lib> getServerConfiguration() throws CoreException
    {
        int current;
        IMinecraftConfigurationWorkingCopy<Plugin, Lib> spigotConfig;
        // Grab current state
        synchronized (this.versionLock)
        {
            current = this.currentVersion;
            spigotConfig = this.configuration;
        }
        // If configuration needs loading
        if (spigotConfig == null || this.loadedVersion != current)
        {
            IFolder folder = getServer().getServerConfiguration();
            if (folder == null || !folder.exists())
            {
                String path = null;
                if (folder != null)
                {
                    path = folder.getFullPath().toOSString();
                    IProject project = folder.getProject();
                    if (project != null && project.exists() && !project.isOpen())
                    {
                        throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Configuration project is closed: " + path, null));
                    }
                }
                throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "No configuration found", null));
            }
            // If not yet loaded
            if (spigotConfig == null)
            {
                String id = getServer().getServerType().getId();
                spigotConfig = getConfigFromId(folder, id);
            }
            try
            {
                spigotConfig.load(folder, null);
                // Update loaded version
                synchronized (this.versionLock)
                {
                    // If newer version not already loaded, update version
                    if (this.configuration == null || this.loadedVersion < current)
                    {
                        this.configuration = spigotConfig;
                        this.loadedVersion = current;
                    }
                }
            }
            catch (CoreException ce)
            {
                // Ignore
                throw ce;
            }
        }
        return spigotConfig;
    }

    /**
     * @param folder
     * @param id
     * @return config
     * @throws CoreException
     */
    protected abstract IMinecraftConfigurationWorkingCopy<Plugin, Lib> getConfigFromId(IFolder folder, String id) throws CoreException;
    
    @Override
    public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException
    {
        // Initialize state
        synchronized (this.versionLock)
        {
            this.configuration = null;
            this.currentVersion = 0;
            this.loadedVersion = 0;
        }
        if (runtime == null)
        {
            return;
        }
        IPath path = runtime.getLocation();
        
        String id = getServer().getServerType().getId();
        IFolder folder = getServer().getServerConfiguration();
        final IMinecraftConfigurationWorkingCopy<Plugin, Lib> spigotConfig = getConfigFromId(folder, id);
        
        try
        {
            spigotConfig.importFromPath(path, monitor);
        }
        catch (CoreException ce)
        {
            throw ce;
        }
        // Update version
        synchronized (this.versionLock)
        {
            // If not already initialized by some other thread, save the configuration
            if (this.configuration == null)
            {
                this.configuration = spigotConfig;
            }
        }
    }
    
    @Override
    public void saveConfiguration(IProgressMonitor monitor) throws CoreException
    {
        IMinecraftConfigurationWorkingCopy<Plugin, Lib> spigotConfig = this.configuration;
        if (spigotConfig == null)
        {
            return;
        }
        spigotConfig.save(getServer().getServerConfiguration(), monitor);
    }
    
    @Override
    public void configurationChanged()
    {
        synchronized (this.versionLock)
        {
            // Alter the current version
            this.currentVersion++;
        }
    }
    
    @Override
    public boolean isDebug()
    {
        return getAttribute(PROPERTY_DEBUG, false);
    }
    
    @Override
    public boolean isTestEnvironment()
    {
        return getAttribute(PROPERTY_TEST_ENVIRONMENT, false);
    }

    @Override
    public String getInstanceDirectory()
    {
        return getAttribute(PROPERTY_INSTANCE_DIR, (String) null);
    }
    
    @Override
    public String getDeployDirectory()
    {
        // Default to value used by prior WTP versions
        return getAttribute(PROPERTY_DEPLOY_DIR, DEFAULT_DEPLOYDIR);
    }
    
    @Override
    public boolean isServeModulesWithoutPublish()
    {
        // If feature is supported, return current setting
        IVersionHandler<Server> tvh = getVersionHandler();
        if (tvh != null)
            return getAttribute(PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH, false);
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public IPath getRuntimeBaseDirectory()
    {
        IVersionHandler<Server> tvh = getVersionHandler();
        if (tvh != null)
            return tvh.getRuntimeBaseDirectory((Server) this);
        return null;
    }
    
    @Override
    public ServerPort[] getServerPorts()
    {
        if (getServer().getServerConfiguration() == null)
            return new ServerPort[0];
        
        try
        {
            ServerPort[] sp = getServerConfiguration().getServerPorts();
            return sp;
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            return new ServerPort[0];
        }
    }
    
    @Override
    public void setDefaults(IProgressMonitor monitor)
    {
//        setTestEnvironment(true);
//        setAttribute("auto-publish-setting", 2);
//        setAttribute("auto-publish-time", 1);
        // setDeployDirectory(DEFAULT_DEPLOYDIR);
    }
    
    @Override
    public void setDebug(boolean b)
    {
        setAttribute(PROPERTY_DEBUG, b);
    }
//    

    @Override
    public void setTestEnvironment(boolean b)
    {
        setAttribute(PROPERTY_TEST_ENVIRONMENT, b);
    }
    
    @Override
    public void setInstanceDirectory(String instanceDir)
    {
        setAttribute(PROPERTY_INSTANCE_DIR, instanceDir);
    }
    
    @Override
    public void setDeployDirectory(String deployDir)
    {
        // Remove attribute if setting to legacy value assumed in prior versions of WTP.
        // Allowing values that differ only in case is asking for more trouble that it is worth.
        if (DEFAULT_DEPLOYDIR.equalsIgnoreCase(deployDir))
            setAttribute(PROPERTY_DEPLOY_DIR, (String) null);
        else
            setAttribute(PROPERTY_DEPLOY_DIR, deployDir);
    }
    
    @Override
    public void setServeModulesWithoutPublish(boolean b)
    {
        setAttribute(PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH, b);
    }
    
}
