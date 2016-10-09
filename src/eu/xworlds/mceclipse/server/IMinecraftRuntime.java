/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * Base runtime for minecraft servers.
 * 
 * @author mepeisen
 */
public interface IMinecraftRuntime
{
    
    /**
     * Checks if the default jvm is used.
     * 
     * @return {@code true} for default jvm
     */
    boolean isUsingDefaultJRE();
    
    /**
     * Returns the jvm to use
     * 
     * @return the jvm to use
     */
    IVMInstall getVMInstall();
    
    /**
     * Returns the runtime classpath entries.
     * 
     * @return runtime classpath entries.
     */
    List<IRuntimeClasspathEntry> getRuntimeClasspath();
    
    /**
     * verifies the runtime location and checks for a valid installation
     * 
     * @return status of verification.
     */
    IStatus verifyLocation();
    
    /**
     * Validates the runtime.
     * 
     * @return status of verification.
     */
    IStatus validate();
    
}
