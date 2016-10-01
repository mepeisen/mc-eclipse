/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 *
 */
public abstract class SpigotVersionHandler implements ISpigotVersionHandler
{
    
    /**
     * Returns the pom version string
     * @return pom version string
     */
    protected abstract String getPomVersion();
    
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
                        final ZipEntry entry = jar.getEntry("/META-INF/maven/org.spigotmc/spigot/pom.properties"); //$NON-NLS-1$
                        if (entry != null && !entry.isDirectory())
                        {
                            final Properties props = new Properties();
                            try (final InputStream is = jar.getInputStream(entry))
                            {
                                props.load(is);
                            }
                            final String version = props.getProperty("version"); //$NON-NLS-1$
                            if (version.equals(getPomVersion()))
                            {
                                return Status.OK_STATUS;
                            }
                            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "wrong version");
                        }
                    }
                }
            }
            catch (@SuppressWarnings("unused") Exception ex)
            {
                // silently ignore
            }
        }
        return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "no spigot jar found");
    }
    
    @Override
    public String getRuntimeClass()
    {
        return "org.bukkit.craftbukkit.Main"; //$NON-NLS-1$
    }
    
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
    
    @Override
    public IStatus canAddModule(IModule module)
    {
        if ("1.0".equals(module.getModuleType().getVersion())) //$NON-NLS-1$
        {
            return Status.OK_STATUS;
        }
        return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Unsupported plugin version");
    }
    
    @Override
    public IPath getRuntimeBaseDirectory(SpigotServer server)
    {
        if (server.isTestEnvironment())
        {
            String baseDir = server.getInstanceDirectory();
            // If test mode and no instance directory specified, use temporary directory
            if (baseDir == null)
            {
                SpigotServerBehaviour tsb = (SpigotServerBehaviour)server.getServer().loadAdapter(SpigotServerBehaviour.class, null);
                return tsb.getTempDirectory();
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
