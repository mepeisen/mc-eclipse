/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.yaml.snakeyaml.Yaml;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftConfigurationWorkingCopy;
import eu.xworlds.mceclipse.server.IMinecraftLibrary;
import eu.xworlds.mceclipse.server.IMinecraftPlugin;

/**
 * @author mepeisen
 * @param <Plugin>
 * @param <Lib>
 *
 */
public abstract class AbstractConfiguration<Plugin extends IMinecraftPlugin, Lib extends IMinecraftLibrary> implements IMinecraftConfigurationWorkingCopy<Plugin, Lib>
{
    
    /** runtime folder. */
    protected IFolder                              runtimeFolder;
    
    /** the port number to be used. */
    protected int                                  portNumber        = 25565;
    
    /** the spigot plugin properties. */
    private Properties                             pluginProperties  = new Properties();
    
    /** the spigot library properties. */
    private Properties                             libraryProperties = new Properties();
    
    /** the registered property listeners. */
    private transient List<PropertyChangeListener> propertyListeners;
    
    /**
     * Constructor.
     * 
     * @param folder
     */
    public AbstractConfiguration(IFolder folder)
    {
        this.runtimeFolder = folder;
    }
    
    /**
     * Loads config from given folder.
     * 
     * @author mepeisen
     */
    @FunctionalInterface
    protected interface FolderLoader
    {
        /**
         * loads config from given folder
         * 
         * @param f
         * @param monitor
         * @throws CoreException
         * @throws IOException 
         */
        void load(IFolder f, IProgressMonitor monitor) throws CoreException, IOException;
    }
    
    /**
     * Steps to load from folder.
     * 
     * @return steps for loading config from folder.
     */
    protected abstract FolderLoader[] getFolderLoadSteps();
    
    /**
     * @param f
     * @param m
     * @throws CoreException
     */
    @Override
    public void load(IFolder f, IProgressMonitor m) throws CoreException
    {
        try
        {
            final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
            final FolderLoader[] loadSteps = this.getFolderLoadSteps();
            monitor.beginTask("Loading", 3 + loadSteps.length);
            if (f == null)
            {
                monitor.done();
                return;
            }
            
            // load server.properties
            for (final FolderLoader loader : loadSteps)
            {
                loader.load(f, monitor);
                monitor.worked(1);
            }
            
            // load eclipse.plugin.properties
            final IFile eclipsePluginProperties = f.getFile("eclipse.plugin.properties"); //$NON-NLS-1$
            if (eclipsePluginProperties.exists())
            {
                this.loadPropertiesFromFile(this.pluginProperties, eclipsePluginProperties, monitor);
            }
            monitor.worked(1);
            
            // load eclipse.library.properties
            final IFile eclipseLibraryProperties = f.getFile("eclipse.library.properties"); //$NON-NLS-1$
            if (eclipseLibraryProperties.exists())
            {
                this.loadPropertiesFromFile(this.libraryProperties, eclipseLibraryProperties, monitor);
            }
            monitor.worked(1);
            
            monitor.done();
        }
        catch (Exception e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Cannot load config", e));
        }
    }
    
    /**
     * Loads config from given path.
     * 
     * @author mepeisen
     */
    @FunctionalInterface
    protected interface PathLoader
    {
        /**
         * loads config from given folder
         * 
         * @param path
         * @param monitor
         * @throws CoreException
         * @throws IOException 
         */
        void load(IPath path, IProgressMonitor monitor) throws CoreException, IOException;
    }
    
    /**
     * Steps to load from folder.
     * 
     * @return steps for loading config from folder.
     */
    protected abstract PathLoader[] getPathLoadSteps();
    
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
            final PathLoader[] pathSteps = this.getPathLoadSteps();
            monitor.beginTask("Loading", 3 + pathSteps.length);
            if (path == null)
            {
                monitor.done();
                return;
            }
            
            // load server.properties
            for (final PathLoader loader : pathSteps)
            {
                loader.load(path, monitor);
                monitor.worked(1);
            }
            
            // load eclipse.plugin.properties
            final File eclipsePluginProperties = path.append("eclipse.plugin.properties").toFile(); //$NON-NLS-1$
            if (eclipsePluginProperties.exists())
            {
                this.loadPropertiesFromFile(this.pluginProperties, eclipsePluginProperties);
            }
            monitor.worked(1);
            
            // load eclipse.library.properties
            final File eclipseLibraryProperties = path.append("eclipse.library.properties").toFile(); //$NON-NLS-1$
            if (eclipseLibraryProperties.exists())
            {
                this.loadPropertiesFromFile(this.pluginProperties, eclipseLibraryProperties);
            }
            monitor.worked(1);
            
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
    @Override
    public void importFromPath(IPath path, IProgressMonitor monitor) throws CoreException
    {
        load(path, monitor);
    }
    
    /**
     * @param props
     * @param file
     * @throws IOException
     */
    protected void loadYmlFromFile(final Map<String, Object> props, File file) throws IOException
    {
        props.clear();
        final Yaml yaml = new Yaml();
        try (final InputStream in = new FileInputStream(file))
        {
            final Map<String, Object> map = (Map<String, Object>) yaml.load(in);
            props.putAll(map);
        }
    }
    
    /**
     * @param props
     * @param file
     * @throws IOException
     */
    protected void loadPropertiesFromFile(final Properties props, File file) throws IOException
    {
        props.clear();
        try (final InputStream in = new FileInputStream(file))
        {
            props.load(in);
        }
    }
    
    /**
     * @param props
     * @param file
     * @throws IOException
     */
    protected void saveYmlToFile(final Map<String, Object> props, File file) throws IOException
    {
        final Yaml yaml = new Yaml();
        try (final FileWriter fw = new FileWriter(file))
        {
            yaml.dump(props, fw);
        }
    }
    
    /**
     * @param props
     * @param file
     * @throws IOException
     */
    protected void savePropertiesToFile(final Properties props, File file) throws IOException
    {
        try (final FileOutputStream fos = new FileOutputStream(file))
        {
            props.store(fos, null);
        }
    }
    
    /**
     * @param props
     * @param file
     * @param monitor
     * @throws IOException
     * @throws CoreException
     */
    protected void loadYmlFromFile(final Map<String, Object> props, IFile file, IProgressMonitor monitor) throws IOException, CoreException
    {
        props.clear();
        final Yaml yaml = new Yaml();
        try (final InputStream in = file.getContents(true))
        {
            final Map<String, Object> map = (Map<String, Object>) yaml.load(in);
            props.putAll(map);
        }
    }
    
    /**
     * @param props
     * @param file
     * @param monitor
     * @throws IOException
     * @throws CoreException
     */
    protected void loadPropertiesFromFile(final Properties props, IFile file, IProgressMonitor monitor) throws IOException, CoreException
    {
        props.clear();
        try (final InputStream in = file.getContents(true))
        {
            props.load(in);
        }
    }
    
    /**
     * @param props
     * @param file
     * @param monitor
     * @throws IOException
     * @throws CoreException
     */
    protected void saveYmlToFile(final Map<String, Object> props, IFile file, IProgressMonitor monitor) throws IOException, CoreException
    {
        final Yaml yaml = new Yaml();
        final byte[] contents = yaml.dump(props).getBytes();
        if (file.exists())
        {
            file.setContents(new ByteArrayInputStream(contents), true, true, monitor);
        }
        else
        {
            file.create(new ByteArrayInputStream(contents), true, monitor);
        }
    }
    
    /**
     * @param props
     * @param file
     * @param monitor
     * @throws IOException
     * @throws CoreException
     */
    protected void savePropertiesToFile(final Properties props, IFile file, IProgressMonitor monitor) throws IOException, CoreException
    {
        byte[] contents = null;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            props.store(baos, null);
            contents = baos.toByteArray();
        }
        if (file.exists())
        {
            file.setContents(new ByteArrayInputStream(contents), true, true, monitor);
        }
        else
        {
            file.create(new ByteArrayInputStream(contents), true, monitor);
        }
    }
    
    /**
     * Saves config to given folder.
     * 
     * @author mepeisen
     */
    @FunctionalInterface
    protected interface FolderSaver
    {
        /**
         * saves config to given folder
         * 
         * @param f
         * @param monitor
         * @throws CoreException
         * @throws IOException 
         */
        void save(IFolder f, IProgressMonitor monitor) throws CoreException, IOException;
    }
    
    /**
     * Steps to save to folder.
     * 
     * @return steps for saving config to folder.
     */
    protected abstract FolderSaver[] getFolderSaveSteps();
    
    /**
     * @param serverConfiguration
     * @param m
     * @throws CoreException
     */
    @Override
    public void save(IFolder serverConfiguration, IProgressMonitor m) throws CoreException
    {
        try
        {
            final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
            final FolderSaver[] saveSteps = this.getFolderSaveSteps();
            monitor.beginTask("Saving", 3 + saveSteps.length);
            
            if (!serverConfiguration.exists())
            {
                serverConfiguration.create(true, true, monitor);
            }
            monitor.worked(1);
            
            // save server.properties
            for (final FolderSaver saver : saveSteps)
            {
                saver.save(serverConfiguration, monitor);
                monitor.worked(1);
            }
            
            // save eclipse.plugin.properties
            final IFile eclipsePluginProperties = serverConfiguration.getFile("eclipse.plugin.properties"); //$NON-NLS-1$
            this.savePropertiesToFile(this.pluginProperties, eclipsePluginProperties, monitor);
            monitor.worked(1);
            
            // save eclipse.library.properties
            final IFile eclipseLibraryProperties = serverConfiguration.getFile("eclipse.library.properties"); //$NON-NLS-1$
            this.savePropertiesToFile(this.libraryProperties, eclipseLibraryProperties, monitor);
            
            monitor.done();
        }
        catch (Exception e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Cannot save config", e));
        }
    }
    
    /**
     * Saves config to given path.
     * 
     * @author mepeisen
     */
    @FunctionalInterface
    protected interface PathSaver
    {
        /**
         * saves config to given path
         * 
         * @param path
         * @param monitor
         * @throws CoreException
         * @throws IOException 
         */
        void save(IPath path, IProgressMonitor monitor) throws CoreException, IOException;
    }
    
    /**
     * Steps to save to path.
     * 
     * @return steps for saving config to path.
     */
    protected abstract PathSaver[] getPathSaveSteps();
    
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
            final PathSaver[] saveSteps = this.getPathSaveSteps();
            monitor.beginTask("Saving", 3 + saveSteps.length);
            
            if (!path.toFile().exists())
            {
                path.toFile().mkdir();
            }
            monitor.worked(1);
            
            // save server.properties
            for (final PathSaver saver : saveSteps)
            {
                saver.save(path, monitor);
                monitor.worked(1);
            }
            
            // save eclipse.plugin.properties
            final File eclipsePluginProperties = path.append("eclipse.plugin.properties").toFile(); //$NON-NLS-1$
            this.savePropertiesToFile(this.pluginProperties, eclipsePluginProperties);
            monitor.worked(1);
            
            // save eclipse.library.properties
            final File eclipseLibraryProperties = path.append("eclipse.library.properties").toFile(); //$NON-NLS-1$
            this.savePropertiesToFile(this.libraryProperties, eclipseLibraryProperties);
            
            monitor.done();
        }
        catch (Exception e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, 0, "Cannot save config", e));
        }
    }
    
    @Override
    public void addPlugin(int i, Plugin module2)
    {
        final List<Plugin> plugins = getPluginsFromConfig();
        if (i == -1)
        {
            plugins.add(module2);
        }
        else
        {
            plugins.add(i, module2);
        }
        toPluginsConfig(plugins);
    }
    
    /**
     * @param plugins
     */
    private void toPluginsConfig(List<Plugin> plugins)
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
     * 
     * @return plugins from config.
     */
    private List<Plugin> getPluginsFromConfig()
    {
        final List<Plugin> result = new ArrayList<>();
        if (this.pluginProperties.containsKey("size")) //$NON-NLS-1$
        {
            final int size = Integer.parseInt(this.pluginProperties.getProperty("size")); //$NON-NLS-1$
            for (int i = 0; i < size; i++)
            {
                final Plugin plugin = createPlugin();
                plugin.readConfig(this.pluginProperties, i);
                result.add(plugin);
            }
        }
        return result;
    }
    
    /**
     * Creates a new plugin instance.
     * 
     * @return plugin
     */
    protected abstract Plugin createPlugin();
    
    /**
     * Creates a new library instance.
     * 
     * @return library
     */
    protected abstract Lib createLibrary();
    
    @Override
    public void removePlugin(int i)
    {
        final List<Plugin> plugins = getPluginsFromConfig();
        plugins.remove(i);
        toPluginsConfig(plugins);
    }
    
    /**
     * @param libraries
     */
    private void toLibraryiesConfig(List<Lib> libraries)
    {
        this.libraryProperties.clear();
        this.libraryProperties.setProperty("size", String.valueOf(libraries.size())); //$NON-NLS-1$
        for (int i = 0; i < libraries.size(); i++)
        {
            libraries.get(i).saveConfig(this.libraryProperties, i);
        }
    }
    
    /**
     * fetches libraries from config.
     * 
     * @return libraries from config.
     */
    private List<Lib> getLibrariesFromConfig()
    {
        final List<Lib> result = new ArrayList<>();
        if (this.libraryProperties.containsKey("size")) //$NON-NLS-1$
        {
            final int size = Integer.parseInt(this.libraryProperties.getProperty("size")); //$NON-NLS-1$
            for (int i = 0; i < size; i++)
            {
                final Lib lib = createLibrary();
                lib.readConfig(this.libraryProperties, i);
                result.add(lib);
            }
        }
        return result;
    }
    
    @Override
    public void addLibrary(int i, Lib module2)
    {
        final List<Lib> libs = getLibrariesFromConfig();
        if (i == -1)
        {
            libs.add(module2);
        }
        else
        {
            libs.add(i, module2);
        }
        toLibraryiesConfig(libs);
    }
    
    @Override
    public void removeLibrary(int i)
    {
        final List<Lib> libs = getLibrariesFromConfig();
        libs.remove(i);
        toLibraryiesConfig(libs);
    }
    
    @Override
    public List<Plugin> getPlugins()
    {
        return Collections.unmodifiableList(this.getPluginsFromConfig());
    }
    
    @Override
    public List<Lib> getLibraries()
    {
        return Collections.unmodifiableList(this.getLibrariesFromConfig());
    }
    
    /**
     * Fires property changed event.
     * 
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue)
    {
        if (this.propertyListeners == null)
            return;
        
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        try
        {
            Iterator<PropertyChangeListener> iterator = this.propertyListeners.iterator();
            while (iterator.hasNext())
            {
                try
                {
                    PropertyChangeListener listener = iterator.next();
                    listener.propertyChange(event);
                }
                catch (@SuppressWarnings("unused") Exception e)
                {
                    // Trace.trace(Trace.SEVERE, "Error firing property change event", e);
                }
            }
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // Trace.trace(Trace.SEVERE, "Error in property event", e);
        }
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (this.propertyListeners == null)
        {
            this.propertyListeners = new ArrayList<>();
        }
        this.propertyListeners.add(listener);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (this.propertyListeners != null)
        {
            this.propertyListeners.remove(listener);
        }
    }
    
}
