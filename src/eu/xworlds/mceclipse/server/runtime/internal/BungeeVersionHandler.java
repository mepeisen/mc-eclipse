/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 *
 */
public abstract class BungeeVersionHandler extends AbstractVersionHandler<BungeeServer> implements IBungeeVersionHandler
{
    
    /**
     * Returns the pom version string
     * @return pom version string
     */
    protected abstract String getPomVersion();
    
    /**
     * Returns the spigot tools filename
     * @return spigot tools filename
     */
    protected abstract String getBungeeToolsName();
    
    @Override
    protected IStatus checkJar(JarFile jar) throws IOException
    {
        final ZipEntry entry = jar.getEntry("META-INF/maven/net.md-5/bungeecord-proxy/pom.properties"); //$NON-NLS-1$
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
        return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "no pom.properties found");
    }

    @Override
    protected AbstractServerBehaviour getBehaviour(BungeeServer server)
    {
        return (BungeeServerBehaviour)server.getServer().loadAdapter(BungeeServerBehaviour.class, null);
    }
    
    @Override
    public String getRuntimeClass()
    {
        // return "net.md_5.bungee.Bootstrap"; //$NON-NLS-1$
        return "eu.xworlds.mceclipse.bungee.Main"; //$NON-NLS-1$
    }
    
    @Override
    public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath)
    {
        final List<IRuntimeClasspathEntry> result = super.getRuntimeClasspath(installPath);
        
        // add tools jar as first entry
        result.add(0, JavaRuntime.newArchiveRuntimeClasspathEntry(McEclipsePlugin.getBungeeToolsJar(this.getBungeeToolsName())));
        
        return result;
    }
    
    @Override
    public IStatus canAddModule(IModule module)
    {
        if ("bungee.plugin".equals(module.getModuleType().getId())) //$NON-NLS-1$
        {
            if ("1.0".equals(module.getModuleType().getVersion())) //$NON-NLS-1$
            {
                return Status.OK_STATUS;
            }
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Unsupported plugin version");
        }
        if ("bungee.library".equals(module.getModuleType().getId())) //$NON-NLS-1$
        {
            if ("1.0".equals(module.getModuleType().getVersion())) //$NON-NLS-1$
            {
                return Status.OK_STATUS;
            }
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Unsupported library version");
        }
        return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Unsupported module type");
    }
    
}
