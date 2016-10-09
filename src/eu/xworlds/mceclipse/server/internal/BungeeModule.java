/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.util.ProjectModule;

/**
 * @author mepeisen
 *
 */
public class BungeeModule extends ProjectModule
{

    /**
     * @param project
     */
    public BungeeModule(IProject project)
    {
        super(project);
    }

    /**
     * 
     */
    public BungeeModule()
    {
        super();
    }
    
}
