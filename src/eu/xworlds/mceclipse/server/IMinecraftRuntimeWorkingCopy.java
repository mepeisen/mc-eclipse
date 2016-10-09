/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server;

import org.eclipse.jdt.launching.IVMInstall;

/**
 * Base runtime for minecraft servers.
 * 
 * @author mepeisen
 */
public interface IMinecraftRuntimeWorkingCopy extends IMinecraftRuntime
{
    
    /**
     * Sets the vm to use.
     * 
     * @param defaultVMInstall
     *            vm or {@code null} if the default jvm should be used.
     */
    void setVMInstall(IVMInstall defaultVMInstall);
    
}
