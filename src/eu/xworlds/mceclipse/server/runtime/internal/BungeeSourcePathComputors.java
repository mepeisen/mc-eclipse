/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author mepeisen
 *
 */
public class BungeeSourcePathComputors implements ISourcePathComputerDelegate
{

    @Override
    public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException
    {
        final IServer server = ServerUtil.getServer(configuration);
        final List<IRuntimeClasspathEntry> runtimeClasspath = new ArrayList<>();
        for (final IModule module : server.getModules())
        {
            final IProject project = module.getProject();
            if (project.hasNature(JavaCore.NATURE_ID))
            {
                final IJavaProject javaProject = JavaCore.create(project);
                runtimeClasspath.add(JavaRuntime.newDefaultProjectClasspathEntry(javaProject)); 
            }
        }
        runtimeClasspath.addAll(Arrays.asList(JavaRuntime.computeUnresolvedSourceLookupPath(configuration))); 
        IRuntimeClasspathEntry[] entries = runtimeClasspath.toArray(new IRuntimeClasspathEntry[runtimeClasspath.size()]);
        IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
        return JavaRuntime.getSourceContainers(resolved);
    }
    
}
