package eu.xworlds.mceclipse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;

public class McEclipsePlugin extends AbstractUIPlugin
{
    
    private static URL ICON_BASE_URL;
    
    protected static McEclipsePlugin singleton;
    
    public static final String PLUGIN_ID = "mc-eclipse-plugin";
    
    public static final String IMG_WIZ_SPIGOT = "wizSpigot";
    
    private static final String URL_WIZBAN = "wizban/";
    
    protected Map imageDescriptors = new HashMap();
    
    /**
     * 
     */
    public McEclipsePlugin()
    {
        singleton = this;
    }
    
   protected ImageRegistry createImageRegistry() {
        ImageRegistry registry = new ImageRegistry();
        registerImage(registry, IMG_WIZ_SPIGOT, URL_WIZBAN + "spigot_wiz.png");
        return registry;
    }
    
    public static Image getImage(String key) {
        return getInstance().getImageRegistry().get(key);
    }
    
    public static ImageDescriptor getImageDescriptor(String key)
    { 
        try {
            getInstance().getImageRegistry();
            return (ImageDescriptor) getInstance().imageDescriptors.get(key);
        } catch (Exception e) {
            return null;
        }
    } 
    
    public static McEclipsePlugin getInstance() {
        return singleton;
    }
    
    /**
     * Register an image with the registry.
     * @param key java.lang.String
     * @param partialURL java.lang.String
     */
    private void registerImage(ImageRegistry registry, String key, String partialURL) {
        if (ICON_BASE_URL == null) {
            String pathSuffix = "icons/";
            ICON_BASE_URL = singleton.getBundle().getEntry(pathSuffix);
        }

        try {
            ImageDescriptor id = ImageDescriptor.createFromURL(new URL(ICON_BASE_URL, partialURL));
            registry.put(key, id);
            imageDescriptors.put(key, id);
        } catch (Exception e) {
            //Trace.trace(Trace.WARNING, "Error registering image", e);
        }
    }
    
}
