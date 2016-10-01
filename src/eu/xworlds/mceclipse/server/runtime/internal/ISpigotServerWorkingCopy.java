/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

/**
 * @author mepeisen
 *
 */
public interface ISpigotServerWorkingCopy extends ISpigotServer
{
    
    /**
     * The default deployment directory.
     */
    public static final String DEFAULT_DEPLOYDIR = "plugins"; //$NON-NLS-1$

    /**
     * Sets this server to test environment mode.
     * 
     * @param b boolean
     */
    public void setTestEnvironment(boolean b);
    
    /**
     * Sets the instance directory for the server. If set to
     * null, the instance directory is derived from the
     * testEnvironment setting.'
     * 
     * @param instanceDir absolule path to the instance directory.
     */
    public void setInstanceDirectory(String instanceDir);

    /**
     * Set the deployment directory for the server.  May be absolute
     * or relative to runtime base directory.
     * 
     * @param deployDir deployment directory for the server
     */
    public void setDeployDirectory(String deployDir);
    
    /**
     * Set this server to serve modules without publishing.
     * 
     * @param b true if modules should be served without publishing
     */
    public void setServeModulesWithoutPublish(boolean b);
    
    /**
     * Sets this process to debug mode.
     *
     * @param b
     *            boolean
     */
    public void setDebug(boolean b);
    
}
