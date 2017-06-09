/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IRuntimeType;

/**
 * @author mepeisen
 *
 */
public class BungeeRuntime extends AbstractRuntime implements IBungeeRuntimeWorkingCopy
{
    
    /**
     * @return bungee version handler.
     */
    public IBungeeVersionHandler getVersionHandler()
    {
        final IRuntimeType type = getRuntime().getRuntimeType();
        return getHandlerFromId(type.getId());
    }
    
    /**
     * Returns handler from id.
     * 
     * @param id
     * @return handler
     */
    private IBungeeVersionHandler getHandlerFromId(String id)
    {
        if (id.contains("1_10")) //$NON-NLS-1$
            return new Bungee110VersionHandler();
        if (id.contains("1_11")) //$NON-NLS-1$
            return new Bungee111VersionHandler();
        if (id.contains("1_12")) //$NON-NLS-1$
            return new Bungee112VersionHandler();
        throw new IllegalStateException("Unknown runtime version: " + id);
    }
    
    @Override
    public IStatus verifyLocation()
    {
        return getVersionHandler().verifyInstallPath(getRuntime().getLocation());
    }
    
    @Override
    protected List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath)
    {
        return getVersionHandler().getRuntimeClasspath(installPath);
    }
    
}
