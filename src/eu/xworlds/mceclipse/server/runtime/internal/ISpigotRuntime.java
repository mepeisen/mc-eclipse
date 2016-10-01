/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * @author mepeisen
 *
 */
public interface ISpigotRuntime
{
    
    boolean isUsingDefaultJRE();
    
    IVMInstall getVMInstall();
    
    List<IRuntimeClasspathEntry> getRuntimeClasspath();
    
    IStatus verifyLocation();
    
    IStatus validate();
    
}
