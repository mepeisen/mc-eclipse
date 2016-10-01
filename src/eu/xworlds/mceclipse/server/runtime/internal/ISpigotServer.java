/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

/**
 * @author mepeisen
 *
 */
public interface ISpigotServer
{
    
    /**
     * Property which specifies whether this server is configured
     * for testing environment.
     */
    public static final String PROPERTY_TEST_ENVIRONMENT = "testEnvironment"; //$NON-NLS-1$
    
    /**
     * Property which specifies the directory where the server instance
     * exists.  If not specified, instance directory is derived
     * from the textEnvironment setting.
     */
    public static final String PROPERTY_INSTANCE_DIR = "instanceDir"; //$NON-NLS-1$
    /**
     * Property which specifies the directory where web applications
     * are published.
     */
    public static final String PROPERTY_DEPLOY_DIR = "deployDir"; //$NON-NLS-1$

    /**
     * Property which specifies if modules should be served without
     * publishing directly from project class files.
     */
    public static final String PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH = "serveModulesWithoutPublish"; //$NON-NLS-1$
    
    /**
     * Returns true if this is a test (publish and run code out of the
     * workbench) environment server.
     *
     * @return boolean
     */
    public boolean isTestEnvironment();
    
    /**
     * Returns true if the process is set to run in debug mode.
     *
     * @return boolean
     */
    public boolean isDebug();
    
    /**
     * Gets the directory where the server instance exists.  If not set,
     * the instance directory is derived from the testEnvironment setting.  
     * 
     * @return directory where the server instance exists. Returns null
     * if not set.
     */
    public String getInstanceDirectory();

    /**
     * Gets the directory to which web applications are to be deployed.
     * If relative, it is relative to the runtime base directory for the
     * server.
     * 
     * @return directory where web applications are deployed
     */
    public String getDeployDirectory();
    
    /**
     * Returns true if modules should be served directly from the project
     * folders without publishing.
     * 
     * @return true if modules should not be published but served directly
     */
    public boolean isServeModulesWithoutPublish();
    
}
