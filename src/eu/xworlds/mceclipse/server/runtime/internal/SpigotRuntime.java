/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 *
 */
public class SpigotRuntime extends RuntimeDelegate implements ISpigotRuntimeWorkingCopy
{
    
    /** attribute. */
    protected static final String PROP_VM_INSTALL_TYPE_ID = "vm-install-type-id"; //$NON-NLS-1$
    /** attribute. */
    protected static final String PROP_VM_INSTALL_ID      = "vm-install-id";      //$NON-NLS-1$
    
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
        throw new IllegalStateException("Unknown runtime version: " + id);
    }
    
    /**
     * 
     * @return vm install type id
     */
    protected String getVMInstallTypeId()
    {
        return getAttribute(PROP_VM_INSTALL_TYPE_ID, (String) null);
    }
    
    /**
     * 
     * @return vm install id
     */
    protected String getVMInstallId()
    {
        return getAttribute(PROP_VM_INSTALL_ID, (String) null);
    }
    
    @Override
    public boolean isUsingDefaultJRE()
    {
        return getVMInstallTypeId() == null;
    }
    
    @Override
    public void setVMInstall(IVMInstall vmInstall)
    {
        if (vmInstall == null)
        {
            setVMInstall(null, null);
        }
        else
            setVMInstall(vmInstall.getVMInstallType().getId(), vmInstall.getId());
        
    }
    
    /**
     * Sets vm
     * 
     * @param typeId
     * @param id
     */
    protected void setVMInstall(String typeId, String id)
    {
        if (typeId == null)
            setAttribute(PROP_VM_INSTALL_TYPE_ID, (String) null);
        else
            setAttribute(PROP_VM_INSTALL_TYPE_ID, typeId);
        
        if (id == null)
            setAttribute(PROP_VM_INSTALL_ID, (String) null);
        else
            setAttribute(PROP_VM_INSTALL_ID, id);
    }
    
    @Override
    public IVMInstall getVMInstall()
    {
        if (getVMInstallTypeId() == null)
            return JavaRuntime.getDefaultVMInstall();
        try
        {
            IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(getVMInstallTypeId());
            IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
            int size = vmInstalls.length;
            String id = getVMInstallId();
            for (int i = 0; i < size; i++)
            {
                if (id.equals(vmInstalls[i].getId()))
                    return vmInstalls[i];
            }
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // ignore
        }
        return null;
    }
    
    @Override
    public List<IRuntimeClasspathEntry> getRuntimeClasspath()
    {
        IPath installPath = getRuntime().getLocation();
        // If installPath is relative, convert to canonical path and hope for the best
        if (!installPath.isAbsolute())
        {
            try
            {
                String installLoc = (new File(installPath.toOSString())).getCanonicalPath();
                installPath = new Path(installLoc);
            }
            catch (@SuppressWarnings("unused") IOException e)
            {
                // Ignore if there is a problem
            }
        }
        return getVersionHandler().getRuntimeClasspath(installPath);
    }
    
    @Override
    public IStatus verifyLocation()
    {
        return getVersionHandler().verifyInstallPath(getRuntime().getLocation());
    }
    
    @Override
    public IStatus validate()
    {
        IStatus status = super.validate();
        if (!status.isOK())
            return status;
        
        status = verifyLocation();
        if (!status.isOK())
            return status;
        
        if (getVMInstall() == null)
        {
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "No VM");
        }
        
        final IVMInstall vmInstall = getVMInstall();
        if (vmInstall instanceof IVMInstall2)
        {
            String javaVersion = ((IVMInstall2) vmInstall).getJavaVersion();
            if (javaVersion != null && !isVMMinimumVersion(javaVersion, 108))
            {
                return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Java version too old");
            }
        }
        return Status.OK_STATUS;
    }
    
    @Override
    public void setDefaults(IProgressMonitor monitor)
    {
        // IRuntimeType type = getRuntimeWorkingCopy().getRuntimeType();
        // getRuntimeWorkingCopy().setLocation(new Path(TomcatPlugin.getPreference("location" + type.getId())));
    }
    
    private static Map<String, Integer> javaVersionMap = new ConcurrentHashMap<String, Integer>();
    
    private boolean isVMMinimumVersion(String javaVersion, int minimumVersion)
    {
        Integer version = javaVersionMap.get(javaVersion);
        if (version == null)
        {
            int index = javaVersion.indexOf('.');
            if (index > 0)
            {
                try
                {
                    int major = Integer.parseInt(javaVersion.substring(0, index)) * 100;
                    index++;
                    int index2 = javaVersion.indexOf('.', index);
                    if (index2 > 0)
                    {
                        int minor = Integer.parseInt(javaVersion.substring(index, index2));
                        version = new Integer(major + minor);
                        javaVersionMap.put(javaVersion, version);
                    }
                }
                catch (NumberFormatException e)
                {
                    // Ignore
                }
            }
        }
        // If we have a version, and it's less than the minimum, fail the check
        if (version != null && version.intValue() < minimumVersion)
        {
            return false;
        }
        return true;
    }
    
}
