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
public class SpigotRuntime extends AbstractRuntime implements ISpigotRuntimeWorkingCopy
{
    
    /**
     * @return spigot version handler.
     */
    public ISpigotVersionHandler getVersionHandler()
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
    private ISpigotVersionHandler getHandlerFromId(String id)
    {
        if (id.contains("1_8_3")) //$NON-NLS-1$
            return new Spigot183VersionHandler();
        if (id.contains("1_8_4")) //$NON-NLS-1$
            return new Spigot184VersionHandler();
        if (id.contains("1_8_5")) //$NON-NLS-1$
            return new Spigot185VersionHandler();
        if (id.contains("1_8_6")) //$NON-NLS-1$
            return new Spigot186VersionHandler();
        if (id.contains("1_8_7")) //$NON-NLS-1$
            return new Spigot187VersionHandler();
        if (id.contains("1_8_8")) //$NON-NLS-1$
            return new Spigot188VersionHandler();
        if (id.contains("1_8")) //$NON-NLS-1$
            return new Spigot18VersionHandler();
        if (id.contains("1_9_2")) //$NON-NLS-1$
            return new Spigot192VersionHandler();
        if (id.contains("1_9_4")) //$NON-NLS-1$
            return new Spigot194VersionHandler();
        if (id.contains("1_9")) //$NON-NLS-1$
            return new Spigot19VersionHandler();
        if (id.contains("1_10_2")) //$NON-NLS-1$
            return new Spigot1102VersionHandler();
        if (id.contains("1_10")) //$NON-NLS-1$
            return new Spigot110VersionHandler();
        if (id.contains("1_11_2")) //$NON-NLS-1$
            return new Spigot1112VersionHandler();
        if (id.contains("1_11")) //$NON-NLS-1$
            return new Spigot111VersionHandler();
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
