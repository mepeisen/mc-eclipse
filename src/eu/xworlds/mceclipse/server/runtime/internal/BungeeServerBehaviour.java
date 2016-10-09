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
public class BungeeServerBehaviour extends AbstractServerBehaviour
{

    @Override
    protected AbstractPingThread createPingThread(IServer server)
    {
        return new BungeePingThread(server, 50*4, this);
    }

    @Override
    public IMinecraftRuntime getMinecraftRuntime()
    {
        if (getServer().getRuntime() == null)
        {
            return null;
        }
        
        return (BungeeRuntime) getServer().getRuntime().loadAdapter(BungeeRuntime.class, null);
    }

    @Override
    public IMinecraftConfiguration<? extends IMinecraftPlugin, ? extends IMinecraftLibrary> getMinecraftConfiguration() throws CoreException
    {
        return getBungeeServer().getServerConfiguration();
    }

    @Override
    public IMinecraftServer getMinecraftServer()
    {
        return (BungeeServer) getServer().loadAdapter(BungeeServer.class, null);
    }

    @Override
    protected String[] getRuntimeVMArguments(IPath installPath, IPath configPath, IPath deployPath)
    {
        return getBungeeVersionHandler().getRuntimeVMArguments(installPath, configPath, deployPath);
    }

    @Override
    protected IStatus prepareRuntimeDirectory(IPath confDir)
    {
        return getBungeeVersionHandler().prepareRuntimeDirectory(confDir);
    }

    @Override
    protected IStatus prepareDeployDirectory(IPath deployDir)
    {
        return getBungeeVersionHandler().prepareDeployDirectory(deployDir);
    }

    @Override
    protected String getStopCommand()
    {
        return "stop"; //$NON-NLS-1$
    }
    
    @Override
    public String toString()
    {
        return "BungeeServer"; //$NON-NLS-1$
    }
    
    /**
     * @return version handler
     */
    public IVersionHandler<BungeeServer> getBungeeVersionHandler()
    {
        return getBungeeServer().getVersionHandler();
    }
    
    /**
     * @return spigot server
     */
    public BungeeServer getBungeeServer()
    {
        return (BungeeServer) getServer().loadAdapter(BungeeServer.class, null);
    }
    
    @Override
    public String getRuntimeClass()
    {
        return getBungeeVersionHandler().getRuntimeClass();
    }
    
    @Override
    protected String[] getRuntimeProgramArguments(boolean starting)
    {
        IPath configPath = getRuntimeBaseDirectory();
        return getBungeeVersionHandler().getRuntimeProgramArguments(configPath, getBungeeServer().isDebug(), starting);
    }
    
    @Override
    protected String[] getExcludedRuntimeProgramArguments(boolean starting)
    {
        return getBungeeVersionHandler().getExcludedRuntimeProgramArguments(getBungeeServer().isDebug(), starting);
    }
    
}
