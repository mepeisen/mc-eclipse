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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.DependencyResolutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.jdt.IClasspathManager;
import org.eclipse.m2e.jdt.MavenJdtPlugin;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
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
        final SpigotServerBehaviour beh = (SpigotServerBehaviour) server.loadAdapter(SpigotServerBehaviour.class, null);
        
        final Set<String> knownModules = new HashSet<>();
        final File dir = beh.getServerDeployDirectory().toFile();
        if (dir.exists())
        {
            final File[] prjfiles = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile() && file.getName().endsWith(".eclipseproject"); //$NON-NLS-1$
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
            for (int i = 0; i < size; i++)
            {
                final IModule[] module = (IModule[]) modules.get(i);
                final Integer in = (Integer) kindList.get(i);
                tasks.add(new PublishOperation2(beh, kind, module, in.intValue()));
                for (final IModule mod : module)
                {
                    knownModules.remove(mod.getName());
                }
            }
        }
        
        for (final String mod : knownModules)
        {
            tasks.add(new RemoveOperation2(beh, mod));
        }
        
        return tasks.toArray(new PublishOperation[tasks.size()]);
    }
    
    private static final class RemoveOperation2 extends PublishOperation
    {
        protected SpigotServerBehaviour server;
        protected String                module;
        
        /**
         * Construct the operation object to publish the specified module to the specified server.
         * 
         * @param server
         *            server to which the module will be published
         * @param module
         *            module to publish
         */
        public RemoveOperation2(SpigotServerBehaviour server, String module)
        {
            super("Publish to server", "Publish Spigot plugin to Spigot server");
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
        }
    }
    
    private static final class PublishOperation2 extends PublishOperation
    {
        protected SpigotServerBehaviour server;
        protected IModule[]             module;
        protected int                   kind;
        protected int                   deltaKind;
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
            
            final IPath pluginsDir = this.server.getServerDeployDirectory();
            // ensure the dir exists
            if (!pluginsDir.toFile().exists())
            {
                pluginsDir.toFile().mkdir();
            }
            
            for (final IModule mod : this.module)
            {
                // generate eclipseproject file and deploy to plugins dir
                final IPath eclipseProjectPath = pluginsDir.append(mod.getProject().getName() + ".eclipseproject"); //$NON-NLS-1$
                final Properties props = new Properties();
                final IJavaProject javaProject = JavaCore.create(mod.getProject());
                props.setProperty("classes", ResourcesPlugin.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation()).getLocation().toOSString()); //$NON-NLS-1$
                final List<String> additionalCP = new ArrayList<>();
                sumUpAdditionalCp(additionalCP, javaProject);
                props.setProperty("cpsize", String.valueOf(additionalCP.size())); //$NON-NLS-1$
                for (int i = 0; i < additionalCP.size(); i++)
                {
                    props.setProperty("cptype" + i, "file"); //$NON-NLS-1$ //$NON-NLS-2$
                    props.setProperty("cpfile" + i, additionalCP.get(i)); //$NON-NLS-1$
                }
                try
                {
                    try (final FileOutputStream fos = new FileOutputStream(eclipseProjectPath.toFile()))
                    {
                        props.store(fos, null);
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
         * @param project
         *            the project to fetch classpath from
         * @throws CoreException
         */
        private void addProjectToCp(List<String> additionalCP, IProject project) throws CoreException
        {
            final IJavaProject javaProject = JavaCore.create(project);
            if (javaProject != null)
            {
                if (FacetedProjectFramework.isFacetedProject(javaProject.getProject()))
                {
                    // check for library or plugin
                    if (FacetedProjectFramework.hasProjectFacet(javaProject.getProject(), "spigot.plugin")) //$NON-NLS-1$
                    {
                        // silently ignore; should be added as own module
                        return;
                    }
                    if (FacetedProjectFramework.hasProjectFacet(javaProject.getProject(), "spigot.library")) //$NON-NLS-1$
                    {
                        // silently ignore; should be added as own module
                        return;
                    }
                }
                
                final String out = ResourcesPlugin.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation()).getLocation().toOSString();
                if (!additionalCP.contains(out))
                {
                    additionalCP.add(out);
                    sumUpAdditionalCp(additionalCP, javaProject);
                }
            }
        }
        
        /**
         * sum up additional classpath entries
         * 
         * @param additionalCP
         *            target list
         * @param javaProject
         *            the java project to fetch classpath from
         * @throws CoreException
         */
        private void sumUpAdditionalCp(List<String> additionalCP, IJavaProject javaProject) throws CoreException
        {
            final IMavenProjectFacade mp = MavenPlugin.getMavenProjectRegistry().getProject(javaProject.getProject());
            if (mp != null)
            {
                // get non provided classpath
                try
                {
                    final IClasspathManager cpm = MavenJdtPlugin.getDefault().getBuildpathManager();
                    final IClasspathEntry[] entries = cpm.getClasspath(javaProject.getProject(), 1, true, new NullProgressMonitor()); // 1 == runtime || provided || system
                    for (final IClasspathEntry cpe : entries)
                    {
                        // check for provided entry
                        boolean isProvided = false;
                        for (final IClasspathAttribute attribute : cpe.getExtraAttributes())
                        {
                            if ("maven.scope".equals(attribute.getName())) //$NON-NLS-1$
                            {
                                isProvided = "provided".equals(attribute.getValue()); //$NON-NLS-1$
                                break;
                            }
                        }
                        if (!isProvided)
                        {
                            if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
                            {
                                final String lib = cpe.getPath().toFile().getAbsolutePath();
                                if (!additionalCP.contains(lib))
                                {
                                    additionalCP.add(lib);
                                }
                            }
                            else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                            {
                                final IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
                                addProjectToCp(additionalCP, prj);
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Error fetching maven classpath", ex));
                }
                return;
            }
            
            // classic java project
            for (final IClasspathEntry cpe : javaProject.getRawClasspath())
            {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
                {
                    final String lib = cpe.getPath().toFile().getAbsolutePath();
                    // TODO Filter spigot jar files
                    if (!additionalCP.contains(lib))
                    {
                        additionalCP.add(lib);
                    }
                }
                else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                {
                    final IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
                    addProjectToCp(additionalCP, prj);
                }
            }
        }
        
    }
    
}
