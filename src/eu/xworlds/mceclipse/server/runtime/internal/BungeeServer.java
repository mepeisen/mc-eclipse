/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.server.core.IModule;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftConfigurationWorkingCopy;
import eu.xworlds.mceclipse.server.IMinecraftRuntime;

/**
 * @author mepeisen
 *
 */
public class BungeeServer extends AbstractServer<BungeeServer, BungeePlugin, BungeeLibrary> implements IBungeeServerWorkingCopy
{
    
    /**
     * spigot plugin
     */
    private static final String BUNGEE_PLUGIN = "bungee.plugin"; //$NON-NLS-1$
    
    /**
     * spigot libraries
     */
    private static final String BUNGEE_LIBRARY = "bungee.library"; //$NON-NLS-1$

    
    @Override
    public IMinecraftRuntime getRuntime()
    {
        if (getServer().getRuntime() == null)
        {
            return null;
        }
        
        return (BungeeRuntime) getServer().getRuntime().loadAdapter(BungeeRuntime.class, null);
    }

    @Override
    public IVersionHandler<BungeeServer> getVersionHandler()
    {
        if (this.versionHandler == null)
        {
            if (getServer().getRuntime() == null || getRuntime() == null)
            {
                return null;
            }
            
            this.versionHandler = ((BungeeRuntime)getRuntime()).getVersionHandler();
        }
        return this.versionHandler;
    }

    @Override
    protected IMinecraftConfigurationWorkingCopy<BungeePlugin, BungeeLibrary> getConfigFromId(IFolder folder, String id) throws CoreException
    {
        BungeeConfiguration spigotConfig = null;
        if (id.indexOf("1_10") > 0) //$NON-NLS-1$
            spigotConfig = new Bungee110Configuration(folder);
        else
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Unknown bungee server version", null));
        }
        return spigotConfig;
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
        if (BUNGEE_PLUGIN.equals(module.getModuleType().getId()) || BUNGEE_LIBRARY.equals(module.getModuleType().getId()))
        {
            IStatus status = canModifyModules(new IModule[] { module }, null);
            if (status == null || !status.isOK())
                throw new CoreException(status);
            return new IModule[] { module };
        }
        
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
                if (!BUNGEE_PLUGIN.equals(module.getModuleType().getId()) && !BUNGEE_LIBRARY.equals(module.getModuleType().getId()))
                    return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Only Bungee plugins and libraries are supported", null);
                
                if (getVersionHandler() == null)
                    return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "No runtime found", null);
                
                IStatus status = getVersionHandler().canAddModule(module);
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
    public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException
    {
        IStatus status = canModifyModules(add, remove);
        if (status == null || !status.isOK())
            throw new CoreException(status);
        
        IMinecraftConfigurationWorkingCopy<BungeePlugin, BungeeLibrary> config = getServerConfiguration();
        
        if (add != null)
        {
            int size = add.length;
            for (int i = 0; i < size; i++)
            {
                IModule module3 = add[i];
                if (BUNGEE_PLUGIN.equals(module3.getModuleType().getId()))
                {
                    BungeePlugin module2 = new BungeePlugin(module3.getId(), module3.getProject());
                    config.addPlugin(-1, module2);
                }
                else if (BUNGEE_LIBRARY.equals(module3.getModuleType().getId()))
                {
                    BungeeLibrary module2 = new BungeeLibrary(module3.getId(), module3.getProject());
                    config.addLibrary(-1, module2);
                }
            }
        }
        
        if (remove != null)
        {
            int size2 = remove.length;
            for (int j = 0; j < size2; j++)
            {
                IModule module3 = remove[j];
                String memento = module3.getId();
                if (BUNGEE_PLUGIN.equals(module3.getModuleType().getId()))
                {
                    List<BungeePlugin> modules = config.getPlugins();
                    int size = modules.size();
                    for (int i = 0; i < size; i++)
                    {
                        BungeePlugin module = modules.get(i);
                        if (memento.equals(module.getMemento()))
                            config.removePlugin(i);
                    }
                }
                else if (BUNGEE_LIBRARY.equals(module3.getModuleType().getId()))
                {
                    List<BungeeLibrary> modules = config.getLibraries();
                    int size = modules.size();
                    for (int i = 0; i < size; i++)
                    {
                        BungeeLibrary module = modules.get(i);
                        if (memento.equals(module.getMemento()))
                            config.removeLibrary(i);
                    }
                }
            }
        }
        // config.save(config.getFolder(), monitor);
    }
    
    @Override
    public String toString()
    {
        return "BungeeServer"; //$NON-NLS-1$
    }
    
}
