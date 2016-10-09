/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftServer;

/**
 * @author mepeisen
 * @param <Server> 
 *
 */
public abstract class AbstractVersionHandler<Server extends IMinecraftServer> implements IVersionHandler<Server>
{
    
    @Override
    public IStatus verifyInstallPath(IPath installPath)
    {
        File[] jarfiles = installPath.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file)
            {
                return file.isFile() && file.getName().endsWith(".jar"); //$NON-NLS-1$
            }
        });
        if (jarfiles != null && jarfiles.length > 0)
        {
            try
            {
                for (final File f : jarfiles)
                {
                    try (final JarFile jar = new JarFile(f))
                    {
                        final IStatus status = checkJar(jar);
                        if (status.isOK())
                        {
                            return status;
                        }
                    }
                }
            }
            catch (@SuppressWarnings("unused") Exception ex)
            {
                // silently ignore
            }
        }
        return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "no runtime jar found");
    }

    /**
     * Checks given jar file for the correct version of the runtime.
     * @param jar
     * @return status
     * @throws IOException
     */
    protected abstract IStatus checkJar(final JarFile jar) throws IOException;
    
    @Override
    public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath)
    {
        File[] jarfiles = installPath.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file)
            {
                return file.isFile() && file.getName().endsWith(".jar"); //$NON-NLS-1$
            }
        });
        final List<IRuntimeClasspathEntry> result = new ArrayList<>();
        if (jarfiles != null && jarfiles.length > 0)
        {
            for (final File jarFile : jarfiles)
            {
                result.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(jarFile.getAbsolutePath())));
            }
        }
        return result;
    }
    
    @Override
    public String[] getRuntimeProgramArguments(IPath configPath, boolean debug, boolean starting)
    {
        return new String[0];
    }
    
    @Override
    public String[] getExcludedRuntimeProgramArguments(boolean debug, boolean starting)
    {
        return null;
    }
    
    @Override
    public String[] getRuntimeVMArguments(IPath installPath, IPath configPath, IPath deployPath)
    {
        return new String[0];
    }
    
    /**
     * Returns behaviour for given server.
     * @param server
     * @return behaviour
     */
    protected abstract AbstractServerBehaviour getBehaviour(Server server);
    
    @Override
    public IPath getRuntimeBaseDirectory(Server server)
    {
        if (server.isTestEnvironment())
        {
            String baseDir = server.getInstanceDirectory();
            // If test mode and no instance directory specified, use temporary directory
            if (baseDir == null)
            {
                return getBehaviour(server).getTempDirectory();
            }
            
            IPath path = new Path(baseDir);
            if (!path.isAbsolute())
            {
                IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
                path = rootPath.append(path);
            }
            return path;
        } 

        return server.getServer().getRuntime().getLocation();
    }
    
    @Override
    public IStatus prepareRuntimeDirectory(IPath baseDir)
    {
        return Status.OK_STATUS;
    }
    
    @Override
    public IStatus prepareDeployDirectory(IPath deployPath)
    {
        return Status.OK_STATUS;
    }
    
}
