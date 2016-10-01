/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.jdt.launching.IVMInstall;

/**
 * @author mepeisen
 *
 */
public interface ISpigotRuntimeWorkingCopy extends ISpigotRuntime
{

    /**
     * @param defaultVMInstall
     */
    void setVMInstall(IVMInstall defaultVMInstall);
    
}
