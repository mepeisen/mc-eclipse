/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.ServerPort;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 *
 */
public class SpigotConfiguration implements ISpigotConfigurationWorkingCopy
{

    /** runtime folder. */
    private IFolder folder;
    
    /** the port number to be used. */
    private int portNumber = 65535;

    /** the server properties. */
    private Properties properties = new Properties();
    
    /** the spigot plugin properties. */
    private Properties pluginProperties = new Properties();
    
    private transient List<PropertyChangeListener> propertyListeners;

    /**
     * Constructor.
     * @param folder
     */
    public SpigotConfiguration(IFolder folder)
    {
        this.folder = folder;
    }

    /**
     * @param f
     * @param m
     * @throws CoreException 
     */
    public void load(IFolder f, IProgressMonitor m) throws CoreException
    {
        load(f.getFullPath(), m);
    }

    /**
     * @param path
     * @param m
     * @throws CoreException 
     */
    public void load(IPath path, IProgressMonitor m) throws CoreException
    {
        try
        {
            final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
            monitor.beginTask("Loading", 3);
            if (path == null)
            {
                monitor.done();
                return;
            }
            
            // load server.properties
            this.properties = new Properties();
            try (final InputStream in = new FileInputStream(path.append("server.properties").toFile())) //$NON-NLS-1$
            {
                this.properties.load(in);
            }
            monitor.worked(1);
            
            // load eclipse.pluginproperties
            this.pluginProperties = new Properties();
            try (final InputStream in = new FileInputStream(path.append("eclipse.plugin.properties").toFile())) //$NON-NLS-1$
            {
                this.pluginProperties.load(in);
            }
            monitor.worked(1);
            
            // fetch port
            this.portNumber = Integer.parseInt(this.properties.getProperty("server-port")); //$NON-NLS-1$
            
            monitor.done();
        }
        catch (Exception e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Cannot load config", e)); 
        }
    }

    /**
     * @param path
     * @param monitor
     * @throws CoreException 
     */
    public void importFromPath(IPath path, IProgressMonitor monitor) throws CoreException
    {
        load(path, monitor);
    }

    /**
     * @param serverConfiguration
     * @param monitor
     * @throws CoreException 
     */
    public void save(IFolder serverConfiguration, IProgressMonitor monitor) throws CoreException
    {
        save(serverConfiguration.getFullPath(), monitor);
    }

    /**
     * @param path
     * @param m
     * @throws CoreException 
     */
    public void save(IPath path, IProgressMonitor m) throws CoreException
    {
        try
        {
            final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
            monitor.beginTask("Saving", 3);
            
            if (!path.toFile().exists())
            {
                path.toFile().mkdir();
            }
            monitor.worked(1);
            
            // save server.properties
            try (final FileOutputStream fos = new FileOutputStream(path.append("server.properties").toFile())) //$NON-NLS-1$
            {
                this.properties.store(fos, null);
            }
            monitor.worked(1);
            
            // save eclipse.plugin.properties
            try (final FileOutputStream fos = new FileOutputStream(path.append("eclipse.plugin.properties").toFile())) //$NON-NLS-1$
            {
                this.pluginProperties.store(fos, null);
            }
            
            monitor.done();
        }
        catch (Exception e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Cannot save config", e)); 
        }
    }

    @Override
    public void addSpigotPlugin(int i, SpigotPlugin module2)
    {
        final List<SpigotPlugin> plugins = getPluginsFromConfig();
        plugins.add(i, module2);
        toPluginsConfig(plugins);
    }

    /**
     * @param plugins
     */
    private void toPluginsConfig(List<SpigotPlugin> plugins)
    {
        this.pluginProperties.clear();
        this.pluginProperties.setProperty("size", String.valueOf(plugins.size())); //$NON-NLS-1$
        for (int i = 0; i < plugins.size(); i++)
        {
            plugins.get(i).saveConfig(this.pluginProperties, i);
        }
    }

    /**
     * fetches plugins from config.
     * @return plugins from config.
     */
    private List<SpigotPlugin> getPluginsFromConfig()
    {
        final List<SpigotPlugin> result = new ArrayList<>();
        if (this.pluginProperties.containsKey("size")) //$NON-NLS-1$
        {
            final int size = Integer.parseInt(this.pluginProperties.getProperty("size")); //$NON-NLS-1$
            for (int i = 0; i < size; i++)
            {
                final SpigotPlugin plugin = new SpigotPlugin();
                plugin.readConfig(this.pluginProperties, i);
                result.add(plugin);
            }
        }
        return result;
    }

    @Override
    public void removeSpigotPlugin(int i)
    {
        final List<SpigotPlugin> plugins = getPluginsFromConfig();
        plugins.remove(i);
        toPluginsConfig(plugins);
    }

    @Override
    public ServerPort getServerPort()
    {
        return new ServerPort("core", "core", this.portNumber, "spigot"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public List<SpigotPlugin> getSpigotPlugins()
    {
        return Collections.unmodifiableList(this.getPluginsFromConfig());
    }

    @Override
    public void setServerPort(int port)
    {
        this.portNumber = port;
        this.properties.put("server-port", String.valueOf(this.portNumber)); //$NON-NLS-1$
        firePropertyChangeEvent(MODIFY_PORT_PROPERTY, "core", new Integer(port));
    }
    
    protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        if (propertyListeners == null)
            return;
        
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        try {
            Iterator<PropertyChangeListener> iterator = propertyListeners.iterator();
            while (iterator.hasNext()) {
                try {
                    PropertyChangeListener listener = iterator.next();
                    listener.propertyChange(event);
                } catch (Exception e) {
                    // Trace.trace(Trace.SEVERE, "Error firing property change event", e);
                }
            }
        } catch (Exception e) {
            // Trace.trace(Trace.SEVERE, "Error in property event", e);
        }
    } 


    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (propertyListeners == null)
            propertyListeners = new ArrayList<>();
        propertyListeners.add(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (propertyListeners != null)
            propertyListeners.remove(listener);
    }
    
}
