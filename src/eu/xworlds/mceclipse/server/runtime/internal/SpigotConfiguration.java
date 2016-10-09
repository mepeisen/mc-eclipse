/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.wst.server.core.ServerPort;

/**
 * @author mepeisen
 *
 */
public class SpigotConfiguration extends AbstractConfiguration<SpigotPlugin, SpigotLibrary> implements ISpigotConfigurationWorkingCopy
{
    
    /** the server properties. */
    private Properties properties = new Properties();
    
    /**
     * Constructor.
     * 
     * @param folder
     */
    public SpigotConfiguration(IFolder folder)
    {
        super(folder);
    }
    
    @Override
    protected FolderLoader[] getFolderLoadSteps()
    {
        return new FolderLoader[] { (f, monitor) -> {
            final IFile serverProperties = f.getFile("server.properties"); //$NON-NLS-1$
            this.loadPropertiesFromFile(this.properties, serverProperties, monitor);
            this.portNumber = Integer.parseInt(this.properties.getProperty("server-port", "25565")); //$NON-NLS-1$ //$NON-NLS-2$
        } };
    }
    
    @Override
    protected PathLoader[] getPathLoadSteps()
    {
        return new PathLoader[] { (path, monitor) -> {
            final File serverProperties = path.append("server.properties").toFile(); //$NON-NLS-1$
            this.loadPropertiesFromFile(this.properties, serverProperties);
            this.portNumber = Integer.parseInt(this.properties.getProperty("server-port", "25565")); //$NON-NLS-1$ //$NON-NLS-2$
        } };
    }
    
    @Override
    protected FolderSaver[] getFolderSaveSteps()
    {
        return new FolderSaver[] { (f, monitor) -> {
            final IFile serverProperties = f.getFile("server.properties"); //$NON-NLS-1$
            this.savePropertiesToFile(this.properties, serverProperties, monitor);
        } };
    }
    
    @Override
    protected PathSaver[] getPathSaveSteps()
    {
        return new PathSaver[] { (path, monitor) -> {
            final File serverProperties = path.append("server.properties").toFile(); //$NON-NLS-1$
            this.savePropertiesToFile(this.properties, serverProperties);
        } };
    }
    
    @Override
    protected SpigotPlugin createPlugin()
    {
        return new SpigotPlugin();
    }
    
    @Override
    protected SpigotLibrary createLibrary()
    {
        return new SpigotLibrary();
    }
    
    @Override
    public ServerPort getServerPort()
    {
        return new ServerPort("core", "core", this.portNumber, "spigot"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    @Override
    public void setServerPort(int port)
    {
        this.portNumber = port;
        this.properties.put("server-port", String.valueOf(this.portNumber)); //$NON-NLS-1$
        firePropertyChangeEvent(MODIFY_PORT_PROPERTY, "core", new Integer(port)); //$NON-NLS-1$
    }

    @Override
    public ServerPort[] getServerPorts()
    {
        return new ServerPort[]{this.getServerPort()};
    }

    @Override
    public void setServerPort(String id, int port)
    {
        if ("core".equals(id)) //$NON-NLS-1$
        {
            this.setServerPort(port);
        }
    }
    
}
