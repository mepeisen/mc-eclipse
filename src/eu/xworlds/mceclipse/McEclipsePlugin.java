package eu.xworlds.mceclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import eu.xworlds.mceclipse.server.internal.CPENodeFactory;

public class McEclipsePlugin extends AbstractUIPlugin
{
    
    private static URL               ICON_BASE_URL;
    
    protected static McEclipsePlugin singleton;
    
    public static final String       PLUGIN_ID        = "mc-eclipse-plugin";
    
    public static final String       IMG_WIZ_SPIGOT   = "wizSpigot";
    
    public static final String       IMG_WIZ_BUNGEE   = "wizSpigot"; // TODO BungeeCord Icon
    
    private static final String      URL_WIZBAN       = "wizban/";
    
    protected Map                    imageDescriptors = new HashMap();

    private CPENodeFactory nodeFactory;
    
    /**
     * 
     */
    public McEclipsePlugin()
    {
        singleton = this;
        this.nodeFactory = new CPENodeFactory();
    }
    
    @Override
    public void start(BundleContext context) throws Exception
    {
        MavenPlugin.getMavenProjectRegistry().addMavenProjectChangedListener(this.nodeFactory);
        super.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        MavenPlugin.getMavenProjectRegistry().removeMavenProjectChangedListener(this.nodeFactory);
        super.stop(context);
    }

    @Override
    protected ImageRegistry createImageRegistry()
    {
        ImageRegistry registry = new ImageRegistry();
        registerImage(registry, IMG_WIZ_SPIGOT, URL_WIZBAN + "spigot_wiz.png");
        return registry;
    }
    
    public static Image getImage(String key)
    {
        return getInstance().getImageRegistry().get(key);
    }
    
    public static ImageDescriptor getImageDescriptor(String key)
    {
        try
        {
            getInstance().getImageRegistry();
            return (ImageDescriptor) getInstance().imageDescriptors.get(key);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    public static McEclipsePlugin getInstance()
    {
        return singleton;
    }
    
    /**
     * Register an image with the registry.
     * 
     * @param key
     *            java.lang.String
     * @param partialURL
     *            java.lang.String
     */
    private void registerImage(ImageRegistry registry, String key, String partialURL)
    {
        if (ICON_BASE_URL == null)
        {
            String pathSuffix = "icons/";
            ICON_BASE_URL = singleton.getBundle().getEntry(pathSuffix);
        }
        
        try
        {
            ImageDescriptor id = ImageDescriptor.createFromURL(new URL(ICON_BASE_URL, partialURL));
            registry.put(key, id);
            this.imageDescriptors.put(key, id);
        }
        catch (Exception e)
        {
            // Trace.trace(Trace.WARNING, "Error registering image", e);
        }
    }
    
    /**
     * @param spigotToolsName
     * @return path to spigot tools
     */
    public static IPath getSpigotToolsJar(String spigotToolsName)
    {
        final Bundle bundle = singleton.getBundle();
        final String jarName = "mce-spigot-tools-" + spigotToolsName + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
        final File dataFile = bundle.getDataFile(bundle.getVersion() + "-" + jarName); //$NON-NLS-1$
        if (!dataFile.exists())
        {
            final URL fileURL = bundle.getEntry("mce-spigot-tools/mce-spigot-tools-" + spigotToolsName + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
            try
            {
                try (final InputStream is = fileURL.openStream())
                {
                    try (final ReadableByteChannel rbc = Channels.newChannel(is))
                    {
                        try (FileOutputStream fos = new FileOutputStream(dataFile))
                        {
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                if (dataFile.exists())
                {
                    dataFile.delete();
                }
                throw new IllegalStateException("Problems extracting spigot tools jar", ex); //$NON-NLS-1$
            }
        }
        return new Path(dataFile.getAbsolutePath());
    }
    
    /**
     * @param spigotToolsName
     * @return path to spigot tools
     */
    public static IPath getBungeeToolsJar(String spigotToolsName)
    {
        final Bundle bundle = singleton.getBundle();
        final String jarName = "mce-bungee-tools-" + spigotToolsName + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
        final File dataFile = bundle.getDataFile(bundle.getVersion() + "-" + jarName); //$NON-NLS-1$
        if (!dataFile.exists())
        {
            final URL fileURL = bundle.getEntry("mce-bungee-tools/mce-bungee-tools-" + spigotToolsName + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
            try
            {
                try (final InputStream is = fileURL.openStream())
                {
                    try (final ReadableByteChannel rbc = Channels.newChannel(is))
                    {
                        try (FileOutputStream fos = new FileOutputStream(dataFile))
                        {
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                if (dataFile.exists())
                {
                    dataFile.delete();
                }
                throw new IllegalStateException("Problems extracting bungee tools jar", ex); //$NON-NLS-1$
            }
        }
        return new Path(dataFile.getAbsolutePath());
    }
    
    /**
     * Converts config string to inet socket address; taken from bungeecord util.java
     * @param config
     * @return inet socket address
     * @throws CoreException for bad config strings
     */
    public static InetSocketAddress getAdressFromConfig(String config) throws CoreException
    {
        URI uri;
        try
        {
            uri = new URI("tcp://" + config); //$NON-NLS-1$
        }
        catch (URISyntaxException ex)
        {
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Bad hostline", ex));
        }
        return new InetSocketAddress(uri.getHost(), (uri.getPort()) == -1 ? 25565 : uri.getPort());
    }

    /**
     * Converts given host to config string
     * @param host
     * @return config string
     */
    public static String toConfig(InetSocketAddress host)
    {
        return host.getHostName() + ":" + host.getPort(); //$NON-NLS-1$
    }
    
    /**
     * Returns the class path entry - node factory.
     * @return class path entry - node factory
     */
    public static CPENodeFactory getNodeFactory()
    {
        return singleton.nodeFactory;
    }
    
}
