/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.internal.CPENode;
import eu.xworlds.mceclipse.server.internal.CPENodeFactory;

/**
 * @author mepeisen
 */
public abstract class AbstractPublishTask extends PublishTaskDelegate
{
    
    protected abstract AbstractServerBehaviour getBehaviour(IServer server);
    
    @Override
    public PublishOperation[] getTasks(IServer server, int kind, List modules, List kindList)
    {
        final AbstractServerBehaviour beh = this.getBehaviour(server);
        
        final Set<String> knownModules = new HashSet<>();
        final File dir = beh.getServerDeployDirectory().toFile();
        if (dir.exists())
        {
            final File[] prjfiles = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile() && (file.getName().endsWith(".eclipseproject") || file.getName().endsWith(".jar")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            });
            if (prjfiles != null && prjfiles.length > 0)
            {
                for (final File file : prjfiles)
                {
                    knownModules.add(file.getName().substring(0, file.getName().lastIndexOf('.')));
                }
            }
        }
        
        final List<PublishOperation> tasks = new ArrayList<>();
        if (modules != null)
        {
            final int size = modules.size();
            
            final Set<IModule> normalized = new HashSet<>();
            for (int i = 0; i < size; i++)
            {
                for (final IModule module : (IModule[]) modules.get(i))
                {
                    normalized.add(module);
                    knownModules.remove(getName(module));
                }
            }
            tasks.add(new PublishOperation2(beh, kind, normalized.toArray(new IModule[normalized.size()])));
        }
        
        for (final String mod : knownModules)
        {
            tasks.add(new RemoveOperation2(beh, mod));
        }
        
        return tasks.toArray(new PublishOperation[tasks.size()]);
    }
    
    /**
     * @param module
     * @return normalized name
     */
    static String getName(IModule module)
    {
        return module.getName().replaceAll("[:\\\\/;]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final class RemoveOperation2 extends PublishOperation
    {
        protected AbstractServerBehaviour server;
        protected String                module;
        
        /**
         * Construct the operation object to publish the specified module to the specified server.
         * 
         * @param server
         *            server to which the module will be published
         * @param module
         *            module to publish
         */
        public RemoveOperation2(AbstractServerBehaviour server, String module)
        {
            super("Publish to server", "Publish plugin to server");
            this.server = server;
            this.module = module;
        }
        
        @Override
        public int getOrder()
        {
            return 0;
        }
        
        @Override
        public int getKind()
        {
            return REQUIRED;
        }
        
        @Override
        public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException
        {
            final IPath pluginsDir = this.server.getServerDeployDirectory();
            
            final File prjFile = new File(pluginsDir.toFile(), this.module + ".eclipseproject"); //$NON-NLS-1$
            if (prjFile.exists())
                prjFile.delete();
            final File jarFile = new File(pluginsDir.toFile(), this.module + ".jar"); //$NON-NLS-1$
            if (jarFile.exists())
                jarFile.delete();
        }
    }
    
    private static final class PublishOperation2 extends PublishOperation
    {
        protected AbstractServerBehaviour server;
        protected IModule[]             module;
        protected int                   kind;
        private IPath                   base;
        
        /**
         * Construct the operation object to publish the specified module to the specified server.
         * 
         * @param server
         *            server to which the module will be published
         * @param kind
         *            kind of publish
         * @param module
         *            module to publish
         */
        public PublishOperation2(AbstractServerBehaviour server, int kind, IModule[] module)
        {
            super("Publish to server", "Publish Spigot plugin to Spigot server");
            this.server = server;
            this.module = module;
            this.kind = kind;
            this.base = server.getRuntimeBaseDirectory();
        }
        
        @Override
        public int getOrder()
        {
            return 0;
        }
        
        @Override
        public int getKind()
        {
            return REQUIRED;
        }
        
        @Override
        public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException
        {
            
            final IPath pluginsDir = this.server.getServerDeployDirectory();
            // ensure the dir exists
            if (!pluginsDir.toFile().exists())
            {
                pluginsDir.toFile().mkdir();
            }
            
            for (final IModule mod : this.module)
            {
                // generate eclipseproject file and deploy to plugins dir
                try
                {
                    // setup props
                    if (mod.getProject() == null)
                    {
                        // java library
                        final IPath eclipseJarPath = pluginsDir.append(getName(mod) + ".jar"); //$NON-NLS-1$
                        
                        final File target = eclipseJarPath.toFile();
                        if (target.exists()) target.delete();
                        final File src = new File(mod.getName()); // TODO Find a better way, f.e. ask the module for the java location...
                        FileUtils.copyFile(src, target);
                    }
                    else
                    {
                        // java project
                        final IPath eclipseProjectPath = pluginsDir.append(getName(mod) + ".eclipseproject"); //$NON-NLS-1$
                        final Properties props = new Properties();
                        final CPENode node = CPENodeFactory.create(mod.getProject());
                        props.setProperty("classes", node.getCpFolders().get(0)); //$NON-NLS-1$
                        final List<String> additionalCP = new ArrayList<>();
                        if (node.getCpFolders().size() > 1)
                        {
                            additionalCP.addAll(node.getCpFolders().subList(1, node.getCpFolders().size()));
                        }

                        sumUpAdditionalCp(additionalCP, node);
                        props.setProperty("cpsize", String.valueOf(additionalCP.size())); //$NON-NLS-1$
                        for (int i = 0; i < additionalCP.size(); i++)
                        {
                            props.setProperty("cptype" + i, "file"); //$NON-NLS-1$ //$NON-NLS-2$
                            props.setProperty("cpfile" + i, additionalCP.get(i)); //$NON-NLS-1$
                        }
                        
                        // save props
                        try (final FileOutputStream fos = new FileOutputStream(eclipseProjectPath.toFile()))
                        {
                            props.store(fos, null);
                        }
                    }
                }
                catch (IOException e)
                {
                    throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "problems while publishing plugins", e));
                }
            }
            
            // this.server.setModulePublishState2(module, IServer.PUBLISH_STATE_NONE);
        }
        
        /**
         * sum up additional classpath entries
         * 
         * @param additionalCP
         *            target list
         * @param node
         *            classpath node
         * @throws CoreException
         * @throws IOException 
         */
        private void sumUpAdditionalCp(List<String> additionalCP, CPENode node) throws CoreException, IOException
        {
            for (final CPENode child : node.getChildren())
            {
                switch (child.getModuleType())
                {
                    default:
                    case BukkitJar:
                    case BungeeJar:
                    case SpigotJar:
                        // filter me
                        break;
                    case Library:
                        // classic java library
                        if (!child.isProvided())
                        {
                            additionalCP.addAll(child.getCpFolders());
                        }
                        break;
                    case BungeeLibrary:
                    case BungeePlugin:
                    case SpigotLibrary:
                    case SpigotPlugin:
                    case UnknownPlugin:
                        // filter me, will be added as module itself
                        break;
                }
                sumUpAdditionalCp(additionalCP, child);
            }
        }
        
    }
    
}
