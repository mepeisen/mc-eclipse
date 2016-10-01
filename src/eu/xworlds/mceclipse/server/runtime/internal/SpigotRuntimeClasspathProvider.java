/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;

/**
 * @author mepeisen
 *
 */
public class SpigotRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate
{
    
    @Override
    public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime)
    {
        IPath installPath = runtime.getLocation();
        
        if (installPath == null)
            return new IClasspathEntry[0];
        
        List<IClasspathEntry> list = new ArrayList<>();
        addLibraryEntries(list, installPath.toFile(), false);
        return list.toArray(new IClasspathEntry[list.size()]);
    }
    
}
