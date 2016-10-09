/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate;

/**
 * @author mepeisen
 *
 */
public class BungeeRuntimeLocator extends RuntimeLocatorDelegate
{
    
    /** runtime types. */
    protected static final String[] runtimeTypes = new String[] { "eu.xworlds.mceclipse.server.runtime.bungee.1_10", //$NON-NLS-1$
    };
    
    @Override
    public void searchForRuntimes(IPath path, IRuntimeSearchListener listener, IProgressMonitor monitor)
    {
        searchForRuntimes2(path, listener, monitor == null ? new NullProgressMonitor() : monitor);
    }
    
    /**
     * Searches for runtimes
     * 
     * @param path
     * @param listener
     * @param monitor
     */
    protected static void searchForRuntimes2(IPath path, IRuntimeSearchListener listener, IProgressMonitor monitor)
    {
        File[] files = null;
        if (path != null)
        {
            File f = path.toFile();
            if (f.exists())
                files = f.listFiles();
            else
                return;
        }
        else
            files = File.listRoots();
        
        if (files != null)
        {
            int size = files.length;
            int work = 100 / size;
            int workLeft = 100 - (work * size);
            for (int i = 0; i < size; i++)
            {
                if (monitor.isCanceled())
                    return;
                if (files[i] != null && files[i].isDirectory())
                    searchDir(listener, files[i], 4, monitor);
                monitor.worked(work);
            }
            monitor.worked(workLeft);
        }
        else
            monitor.worked(100);
    }
    
    /**
     * Searches a directory.
     * 
     * @param listener
     * @param dir
     * @param depth
     * @param monitor
     */
    protected static void searchDir(IRuntimeSearchListener listener, File dir, int depth, IProgressMonitor monitor)
    {
        final File configYml = new File(dir, "config.yml"); //$NON-NLS-1$
        final File modulesYml = new File(dir, "modules.yml"); //$NON-NLS-1$
        if (modulesYml.exists() && modulesYml.isFile() && configYml.exists() && configYml.isFile())
        {
            // check it
            IRuntimeWorkingCopy runtime = getRuntimeFromDir(dir.getParentFile(), monitor);
            if (runtime != null)
            {
                listener.runtimeFound(runtime);
                return;
            }
        }
        
        if (depth == 0)
            return;
        
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });
        if (files != null)
        {
            int size = files.length;
            for (int i = 0; i < size; i++)
            {
                if (monitor.isCanceled())
                    return;
                searchDir(listener, files[i], depth - 1, monitor);
            }
        }
    }
    
    /**
     * Returns a runtime from dir.
     * @param dir
     * @param monitor
     * @return runtime from dir.
     */
    protected static IRuntimeWorkingCopy getRuntimeFromDir(File dir, IProgressMonitor monitor)
    {
        File[] jarfiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file)
            {
                return file.isFile() && file.getName().endsWith(".jar"); //$NON-NLS-1$
            }
        });
        if (jarfiles != null && jarfiles.length > 0)
        {
            for (int i = 0; i < runtimeTypes.length; i++)
            {
                try
                {
                    IRuntimeType runtimeType = ServerCore.findRuntimeType(runtimeTypes[i]);
                    String absolutePath = dir.getAbsolutePath();
                    String id = absolutePath.replace(File.separatorChar, '_').replace(':', '-');
                    IRuntimeWorkingCopy runtime = runtimeType.createRuntime(id, monitor);
                    runtime.setName(dir.getName());
                    runtime.setLocation(new Path(absolutePath));
                    ISpigotRuntimeWorkingCopy wc = (ISpigotRuntimeWorkingCopy) runtime.loadAdapter(ISpigotRuntimeWorkingCopy.class, null);
                    wc.setVMInstall(JavaRuntime.getDefaultVMInstall());
                    IStatus status = runtime.validate(monitor);
                    if (status == null || status.getSeverity() != IStatus.ERROR)
                        return runtime;
                    
                    // Trace.trace(Trace.FINER, "False runtime found at " + dir.getAbsolutePath() + ": " + status.getMessage());
                }
                catch (@SuppressWarnings("unused") Exception e)
                {
                    // Trace.trace(Trace.SEVERE, "Could not find runtime", e);
                }
            }
        }
        return null;
    }
    
}
