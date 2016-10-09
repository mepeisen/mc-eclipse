/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server;

import java.util.Properties;

import org.eclipse.core.resources.IProject;

/**
 * @author mepeisen
 *
 */
public interface IMinecraftLibrary
{

    /**
     * Returns the id.
     * @return id
     */
    String getMemento();
    
    /**
     * Returns the project
     * @return project reference
     */
    IProject getProject();

    /**
     * @param libraryProperties
     * @param key
     */
    void saveConfig(Properties libraryProperties, int key);

    /**
     * @param libraryProperties
     * @param key
     */
    void readConfig(Properties libraryProperties, int key);
}
