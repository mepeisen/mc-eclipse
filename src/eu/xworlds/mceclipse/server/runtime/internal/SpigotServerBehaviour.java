/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;

import eu.xworlds.mceclipse.server.IMinecraftConfiguration;
import eu.xworlds.mceclipse.server.IMinecraftLibrary;
import eu.xworlds.mceclipse.server.IMinecraftPlugin;
import eu.xworlds.mceclipse.server.IMinecraftRuntime;
import eu.xworlds.mceclipse.server.IMinecraftServer;

/**
 * @author mepeisen
 *
 */
public class SpigotServerBehaviour extends AbstractServerBehaviour
{

    @Override
    protected AbstractPingThread createPingThread(IServer server)
    {
        return new SpigotPingThread(server, 50*4, this);
    }

    @Override
    public IMinecraftRuntime getMinecraftRuntime()
    {
        if (getServer().getRuntime() == null)
        {
            return null;
        }
        
        return (SpigotRuntime) getServer().getRuntime().loadAdapter(SpigotRuntime.class, null);
    }

    @Override
    public IMinecraftConfiguration<? extends IMinecraftPlugin, ? extends IMinecraftLibrary> getMinecraftConfiguration() throws CoreException
    {
        return getSpigotServer().getServerConfiguration();
    }

    @Override
    public IMinecraftServer getMinecraftServer()
    {
        return (SpigotServer) getServer().loadAdapter(SpigotServer.class, null);
    }

    @Override
    protected String[] getRuntimeVMArguments(IPath installPath, IPath configPath, IPath deployPath)
    {
        return getSpigotVersionHandler().getRuntimeVMArguments(installPath, configPath, deployPath);
    }

    @Override
    protected IStatus prepareRuntimeDirectory(IPath confDir)
    {
        return getSpigotVersionHandler().prepareRuntimeDirectory(confDir);
    }

    @Override
    protected IStatus prepareDeployDirectory(IPath deployDir)
    {
        return getSpigotVersionHandler().prepareDeployDirectory(deployDir);
    }

    @Override
    protected String getStopCommand()
    {
        return "stop"; //$NON-NLS-1$
    }
    
    @Override
    public String toString()
    {
        return "SpigotServer"; //$NON-NLS-1$
    }
    
    /**
     * @return version handler
     */
    public IVersionHandler<SpigotServer> getSpigotVersionHandler()
    {
        return getSpigotServer().getVersionHandler();
    }
    
    /**
     * @return spigot server
     */
    public SpigotServer getSpigotServer()
    {
        return (SpigotServer) getServer().loadAdapter(SpigotServer.class, null);
    }
    
    @Override
    public String getRuntimeClass()
    {
        return getSpigotVersionHandler().getRuntimeClass();
    }
    
    @Override
    protected String[] getRuntimeProgramArguments(boolean starting)
    {
        IPath configPath = getRuntimeBaseDirectory();
        return getSpigotVersionHandler().getRuntimeProgramArguments(configPath, getSpigotServer().isDebug(), starting);
    }
    
    @Override
    protected String[] getExcludedRuntimeProgramArguments(boolean starting)
    {
        return getSpigotVersionHandler().getExcludedRuntimeProgramArguments(getSpigotServer().isDebug(), starting);
    }
    
}
