/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 */
public class SpigotPublishTask extends PublishTaskDelegate
{
    
    @Override
    public PublishOperation[] getTasks(IServer server, int kind, List modules, List kindList)
    {
        if (modules == null)
        {
            return null;
        }
        
        final SpigotServerBehaviour beh = (SpigotServerBehaviour) server.loadAdapter(SpigotServerBehaviour.class, null);
        
        final List<PublishOperation> tasks = new ArrayList<>();
        final int size = modules.size();
        for (int i = 0; i < size; i++)
        {
            final IModule[] module = (IModule[]) modules.get(i);
            final Integer in = (Integer) kindList.get(i);
            tasks.add(new PublishOperation2(beh, kind, module, in.intValue()));
        }
        return tasks.toArray(new PublishOperation[tasks.size()]);
    }
    
    private static final class PublishOperation2 extends PublishOperation
    {
        protected SpigotServerBehaviour server;
        protected IModule[]             module;
        protected int                   kind;
        protected int                   deltaKind;
        private IPath base;
        
        /**
         * Construct the operation object to publish the specified module to the specified server.
         * 
         * @param server
         *            server to which the module will be published
         * @param kind
         *            kind of publish
         * @param module
         *            module to publish
         * @param deltaKind
         *            kind of change
         */
        public PublishOperation2(SpigotServerBehaviour server, int kind, IModule[] module, int deltaKind)
        {
            super("Publish to server", "Publish Spigot plugin to Spigot server");
            this.server = server;
            this.module = module;
            this.kind = kind;
            this.deltaKind = deltaKind;
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
            
            final IPath pluginsDir = this.base.append(this.server.getServerDeployDirectory());
            // ensure the dir exists
            if (!pluginsDir.toFile().exists())
            {
                pluginsDir.toFile().mkdir();
            }
            
            for (final IModule mod : this.module)
            {
                // generate jars and deploy to plugins dir
                final IPath jarPath = pluginsDir.append(mod.getProject().getName() + ".jar"); //$NON-NLS-1$
                
                // generate Jar
                final JarPackageData jpd = new JarPackageData();
                jpd.setJarLocation(jarPath);
                final List<Object> elements = new ArrayList<>();
                final IJavaProject javaProject = JavaCore.create(mod.getProject());
                for (final IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots())
                {
                    if (root.getKind() == IPackageFragmentRoot.K_SOURCE)
                    {
                        elements.add(root);
                    }
                }
                jpd.setElements(elements.toArray(new Object[elements.size()]));
                
                final IJarExportRunnable runnable = jpd.createJarExportRunnable(null /*PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()*/);
                try
                {
                    runnable.run(monitor);
                }
                catch (InvocationTargetException | InterruptedException e)
                {
                    throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "problems while publishing plugins", e));
                }
            }
            
            // this.server.setModulePublishState2(module, IServer.PUBLISH_STATE_NONE);
        }
        
    }
    
}
