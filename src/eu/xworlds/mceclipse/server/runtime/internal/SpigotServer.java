/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 *
 */
public class SpigotServer extends ServerDelegate implements ISpigotServerWorkingCopy
{
    
    /**
     * spigot plugin
     */
    private static final String SPIGOT_PLUGIN = "spigot.plugin"; //$NON-NLS-1$

    /** debug flag. */
    public static final String PROPERTY_DEBUG = "debug"; //$NON-NLS-1$
    
    /** version handler. */
    protected transient ISpigotVersionHandler versionHandler;
    
    /** config. */
    protected transient SpigotConfiguration configuration;
    
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
    public SpigotRuntime getSpigotRuntime()
    {
        if (getServer().getRuntime() == null)
        {
            return null;
        }
        
        return (SpigotRuntime) getServer().getRuntime().loadAdapter(SpigotRuntime.class, null);
    }
    
    /**
     * Gets the Spigot version handler for this server.
     * 
     * @return version handler for this server
     */
    public ISpigotVersionHandler getSpigotVersionHandler()
    {
        if (this.versionHandler == null)
        {
            if (getServer().getRuntime() == null || getSpigotRuntime() == null)
            {
                return null;
            }
            
            this.versionHandler = getSpigotRuntime().getVersionHandler();
        }
        return this.versionHandler;
    }
    
    /**
     * Returns the spigot configuration.
     * @return spigot configuration.
     * @throws CoreException
     */
    public ISpigotConfiguration getServerConfiguration() throws CoreException
    {
        return getSpigotConfiguration();
    }
    
    /**
     * Returns the configuration.
     * @return spigot configuration.
     * @throws CoreException
     */
    public SpigotConfiguration getSpigotConfiguration() throws CoreException
    {
        int current;
        SpigotConfiguration spigotConfig;
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
    private SpigotConfiguration getConfigFromId(IFolder folder, String id) throws CoreException
    {
        SpigotConfiguration spigotConfig = null;
        if (id.indexOf("1_8_3") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot183Configuration(folder);
        else if (id.indexOf("1_8_4") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot184Configuration(folder);
        else if (id.indexOf("1_8_5") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot185Configuration(folder);
        else if (id.indexOf("1_8_6") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot186Configuration(folder);
        else if (id.indexOf("1_8_7") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot187Configuration(folder);
        else if (id.indexOf("1_8_8") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot188Configuration(folder);
        else if (id.indexOf("1_8") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot18Configuration(folder);
        else if (id.indexOf("1_9_2") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot192Configuration(folder);
        else if (id.indexOf("1_9_4") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot194Configuration(folder);
        else if (id.indexOf("1_9") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot19Configuration(folder);
        else if (id.indexOf("1_10_2") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot1102Configuration(folder);
        else if (id.indexOf("1_10") > 0) //$NON-NLS-1$
            spigotConfig = new Spigot110Configuration(folder);
        else
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Unknown spigot server version", null));
        }
        return spigotConfig;
    }
    
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
        final SpigotConfiguration spigotConfig = getConfigFromId(folder, id);
        
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
        SpigotConfiguration spigotConfig = this.configuration;
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
    
//    /**
//     * Return the root URL of this module.
//     * 
//     * @param module
//     *            org.eclipse.wst.server.core.model.IModule
//     * @return java.net.URL
//     */
//    public URL getModuleRootURL(IModule module)
//    {
//        try
//        {
//            if (module == null)
//                return null;
//            
//            SpigotConfiguration config = getSpigotConfiguration();
//            if (config == null)
//                return null;
//            
//            String url = "http://" + getServer().getHost();
//            int port = config.getMainPort().getPort();
//            port = ServerUtil.getMonitoredPort(getServer(), port, "web");
//            if (port != 80)
//                url += ":" + port;
//            
//            url += config.getWebModuleURL(module);
//            
//            if (!url.endsWith("/"))
//                url += "/";
//            
//            return new URL(url);
//        }
//        catch (Exception e)
//        {
//            Trace.trace(Trace.SEVERE, "Could not get root URL", e);
//            return null;
//        }
//    }
    
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
//    
//    /**
//     * Returns true if the process is set to run in secure mode.
//     *
//     * @return boolean
//     */
//    public boolean isSecure()
//    {
//        return getAttribute(PROPERTY_SECURE, false);
//    }

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
        ISpigotVersionHandler tvh = getSpigotVersionHandler();
        if (tvh != null)
            return getAttribute(PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH, false);
        return false;
    }
    
//    /**
//     * Returns true if contexts should be saved in separate files during server publish.
//     * 
//     * @return boolean
//     */
//    public boolean isSaveSeparateContextFiles()
//    {
//        // If feature is supported, return current setting
//        ISpigotVersionHandler tvh = getSpigotVersionHandler();
//        if (tvh != null && tvh.supportsSeparateContextFiles())
//            return getAttribute(PROPERTY_SAVE_SEPARATE_CONTEXT_FILES, false);
//        return false;
//    }
    
//    /**
//     * Returns true if contexts should be made reloadable by default.
//     * 
//     * @return boolean
//     */
//    public boolean isModulesReloadableByDefault()
//    {
//        // If feature is supported, return current setting
//        ISpigotVersionHandler tvh = getSpigotVersionHandler();
//        if (tvh != null)
//            return getAttribute(PROPERTY_MODULES_RELOADABLE_BY_DEFAULT, true);
//        return true;
//    }
    
    /**
     * Gets the base directory where the server instance runs. This path can vary depending on the configuration. Null may be returned if a runtime hasn't been specified for the server.
     * 
     * @return path to base directory for the server or null if runtime hasn't been specified.
     */
    public IPath getRuntimeBaseDirectory()
    {
        ISpigotVersionHandler tvh = getSpigotVersionHandler();
        if (tvh != null)
            return tvh.getRuntimeBaseDirectory(this);
        return null;
    }
    
//    /**
//     * Gets the directory to which modules should be deployed for this server.
//     * 
//     * @return full path to deployment directory for the server
//     */
//    public IPath getServerDeployDirectory()
//    {
//        String deployDir = getDeployDirectory();
//        IPath deployPath = new Path(deployDir);
//        if (!deployPath.isAbsolute())
//        {
//            IPath base = getRuntimeBaseDirectory();
//            deployPath = base.append(deployPath);
//        }
//        return deployPath;
//    }
    
    protected static String renderCommandLine(String[] commandLine, String separator)
    {
        if (commandLine == null || commandLine.length < 1)
            return "";
        StringBuffer buf = new StringBuffer(commandLine[0]);
        for (int i = 1; i < commandLine.length; i++)
        {
            buf.append(separator);
            buf.append(commandLine[i]);
        }
        return buf.toString();
    }
    
    @Override
    public IModule[] getChildModules(IModule[] module)
    {
        if (module == null)
            return null;
        
//        IModuleType moduleType = module[0].getModuleType();
//        
//        if (module.length == 1 && moduleType != null && "jst.web".equals(moduleType.getId()))
//        {
//            IWebModule webModule = (IWebModule) module[0].loadAdapter(IWebModule.class, null);
//            if (webModule != null)
//            {
//                IModule[] modules = webModule.getModules();
//                // if (modules != null)
//                // System.out.println(modules.length);
//                return modules;
//            }
//        }
        return new IModule[0];
    }
    
    @Override
    public IModule[] getRootModules(IModule module) throws CoreException
    {
//        if ("jst.web".equals(module.getModuleType().getId()))
//        {
//            IStatus status = canModifyModules(new IModule[] { module }, null);
//            if (status == null || !status.isOK())
//                throw new CoreException(status);
//            return new IModule[] { module };
//        }
//        
//        return J2EEUtil.getWebModules(module, null);
        return new IModule[0];
    }
    
    /**
     * Returns true if the given project is supported by this server, and false otherwise.
     *
     * @param add
     *            modules
     * @param remove
     *            modules
     * @return the status
     */
    @Override
    public IStatus canModifyModules(IModule[] add, IModule[] remove)
    {
        if (add != null)
        {
            int size = add.length;
            for (int i = 0; i < size; i++)
            {
                IModule module = add[i];
                if (!SPIGOT_PLUGIN.equals(module.getModuleType().getId()))
                    return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Only Spigot plugins are supported", null);
                
                if (getSpigotVersionHandler() == null)
                    return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "No runtime found", null);
                
                IStatus status = getSpigotVersionHandler().canAddModule(module);
                if (status != null && !status.isOK())
                    return status;
                
                if (module.getProject() != null)
                {
                    status = FacetUtil.verifyFacets(module.getProject(), getServer());
                    if (status != null && !status.isOK())
                        return status;
                }
            }
        }
        
        return Status.OK_STATUS;
    }
    
    @Override
    public ServerPort[] getServerPorts()
    {
        if (getServer().getServerConfiguration() == null)
            return new ServerPort[0];
        
        try
        {
            ServerPort[] sp = new ServerPort[]{getSpigotConfiguration().getServerPort()};
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
//    /**
//     * Sets this process to secure mode.
//     * 
//     * @param b
//     *            boolean
//     */
//    public void setSecure(boolean b)
//    {
//        setAttribute(PROPERTY_SECURE, b);
//    }
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
//    
//    /**
//     * @see ISpigotServerWorkingCopy#setSaveSeparateContextFiles(boolean)
//     */
//    public void setSaveSeparateContextFiles(boolean b)
//    {
//        setAttribute(PROPERTY_SAVE_SEPARATE_CONTEXT_FILES, b);
//    }
//    
//    /**
//     * @see ISpigotServerWorkingCopy#setModulesReloadableByDefault(boolean)
//     */
//    public void setModulesReloadableByDefault(boolean b)
//    {
//        setAttribute(PROPERTY_MODULES_RELOADABLE_BY_DEFAULT, b);
//    }
    
    @Override
    public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException
    {
        IStatus status = canModifyModules(add, remove);
        if (status == null || !status.isOK())
            throw new CoreException(status);
        
        SpigotConfiguration config = getSpigotConfiguration();
        
        if (add != null)
        {
            int size = add.length;
            for (int i = 0; i < size; i++)
            {
                IModule module3 = add[i];
//                IWebModule module = (IWebModule) module3.loadAdapter(IWebModule.class, monitor);
//                String contextRoot = module.getContextRoot();
//                if (contextRoot != null && !contextRoot.startsWith("/") && contextRoot.length() > 0)
//                    contextRoot = "/" + contextRoot;
//                String docBase = config.getDocBasePrefix() + module3.getName();
                SpigotPlugin module2 = new SpigotPlugin(module3.getId(), module3.getProject());
                config.addSpigotPlugin(-1, module2);
            }
        }
        
        if (remove != null)
        {
            int size2 = remove.length;
            for (int j = 0; j < size2; j++)
            {
                IModule module3 = remove[j];
                String memento = module3.getId();
                List<SpigotPlugin> modules = getSpigotConfiguration().getSpigotPlugins();
                int size = modules.size();
                for (int i = 0; i < size; i++)
                {
                    SpigotPlugin module = modules.get(i);
                    if (memento.equals(module.getMemento()))
                        config.removeSpigotPlugin(i);
                }
            }
        }
        // config.save(config.getFolder(), monitor);
    }
    
    @Override
    public String toString()
    {
        return "SpigotServer"; //$NON-NLS-1$
    }
    
}
