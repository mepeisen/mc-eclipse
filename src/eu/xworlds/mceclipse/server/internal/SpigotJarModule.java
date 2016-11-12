/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;

/**
 * @author mepeisen
 *
 */
public class SpigotJarModule extends ModuleDelegate
{
    
    /**
     * jar file.
     */
    private String jar;

    /**
     * @param jar
     */
    public SpigotJarModule(String jar)
    {
        this.jar = jar;
    }
    
    /**
     * 
     */
    public SpigotJarModule()
    {
        super();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.jar == null) ? 0 : this.jar.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpigotJarModule other = (SpigotJarModule) obj;
        if (this.jar == null)
        {
            if (other.jar != null)
                return false;
        }
        else if (!this.jar.equals(other.jar))
            return false;
        return true;
    }

    @Override
    public IStatus validate()
    {
        return null;
    }

    @Override
    public IModule[] getChildModules()
    {
        return null;
    }

    @Override
    public IModuleResource[] members() throws CoreException
    {
        return new IModuleResource[0];
    }
    
}
