/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.wst.server.core.IModule;

/**
 * @author mepeisen
 *
 */
public interface ISpigotVersionHandler
{
    /**
     * Verifies if the specified path points to a a Spigot
     * installation of this version.
     * 
     * @param installPath an installation path 
     * @return OK status if a valid installation
     * exists at the location.  If not valid, the IStatus
     * contains an indication of why.
     */
    public IStatus verifyInstallPath(IPath installPath);
    
    /**
     * Gets the startup class for the Spigot server.
     * 
     * @return server startup class
     */
    public String getRuntimeClass();
    
    /**
     * Gets the startup classpath for the Spigot server.
     * 
     * @param installPath an installation path
     * @return list of classpath entries required to
     * start the Spigot server.
     */
    public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath);
    
    /**
     * Return the program's runtime arguments.
     * 
     * @param configPath a config path
     * @param debug <code>true</code> if debug mode is on
     * @param starting <code>true</code> if the server is starting
     * @return a string array of program arguments
     */
    public String[] getRuntimeProgramArguments(IPath configPath, boolean debug, boolean starting);

    /**
     * Arguments that should not appear in the runtime arguments based on
     * the specified configuration.
     * 
     * @param debug <code>true</code> if debug mode is on
     * @param starting <code>true</code> if the server is starting
     * @return array of excluded arguments
     */
    public String[] getExcludedRuntimeProgramArguments(boolean debug, boolean starting);
    
    /**
     * Gets the startup VM arguments for the Spigot server.
     * 
     * @param installPath installation path for the server
     * @param configPath configuration path for the server
     * @param deployPath deploy path for the server
     * @return array of VM arguments for starting the server
     */
    public String[] getRuntimeVMArguments(IPath installPath, IPath configPath, IPath deployPath);

    /**
     * Returns true if the given project is supported by this
     * server, and false otherwise.
     *
     * @param module a web module
     * @return the status
     */
    public IStatus canAddModule(IModule module);

    /**
     * Returns the runtime base path for relative paths in the server
     * configuration.
     * 
     * @param server SpigotServer instance from which to determine
     * the base path.
     * @return path to Spigot instance directory
     */
    public IPath getRuntimeBaseDirectory(SpigotServer server);

    /**
     * Prepare server runtime directory. Create spigot instance set of
     * directories.
     * 
     * @param baseDir Spigot instance directory to prepare
     * @return result of creation operation 
     */
    public IStatus prepareRuntimeDirectory(IPath baseDir);
    
    /**
     * Prepares the specified directory by making sure it exists and is
     * initialized appropriately.
     * 
     * @param deployPath path to the deployment directory
     *  being prepared
     * @return status result of the operation
     */
    public IStatus prepareDeployDirectory(IPath deployPath);

}
