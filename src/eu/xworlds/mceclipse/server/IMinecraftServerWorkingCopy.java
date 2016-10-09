/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server;

/**
 * @author mepeisen
 */
public interface IMinecraftServerWorkingCopy extends IMinecraftServer
{
    
    /**
     * Sets this server to test environment mode.
     * 
     * @param b
     *            boolean
     */
    void setTestEnvironment(boolean b);
    
    /**
     * Sets the instance directory for the server. If set to null, the instance directory is derived from the testEnvironment setting.'
     * 
     * @param instanceDir
     *            absolule path to the instance directory.
     */
    void setInstanceDirectory(String instanceDir);
    
    /**
     * Set the deployment directory for the server. May be absolute or relative to runtime base directory.
     * 
     * @param deployDir
     *            deployment directory for the server
     */
    void setDeployDirectory(String deployDir);
    
    /**
     * Set this server to serve modules without publishing.
     * 
     * @param b
     *            true if modules should be served without publishing
     */
    void setServeModulesWithoutPublish(boolean b);
    
    /**
     * Sets this process to debug mode.
     *
     * @param b
     *            boolean
     */
    void setDebug(boolean b);
    
}
