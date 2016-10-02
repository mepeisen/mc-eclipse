/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;

/**
 * @author mepeisen
 *
 */
public class SpigotModuleFactory extends ProjectModuleFactoryDelegate
{
    
    protected List<ModuleDelegate> moduleDelegates = new ArrayList<ModuleDelegate>();
    
    @Override
    public ModuleDelegate getModuleDelegate(IModule module)
    {
        for (Iterator<ModuleDelegate> iter = moduleDelegates.iterator(); iter.hasNext();)
        {
            ModuleDelegate element = iter.next();
            if (module == element.getModule())
                return element;
        }
        return null;
    }
    
    @Override
    protected IModule[] createModules(IProject project)
    {
        SpigotModule moduleDelegate = null;
        IModule module = null;
        try
        {
            if (isValidPlugin(project))
            {
                moduleDelegate = new SpigotModule(project);
                module = createModule(project.getName(), project.getName(), "spigot.plugin", "1.0", project);
                moduleDelegate.initialize(module);
            }
            if (isValidLib(project))
            {
                moduleDelegate = new SpigotModule(project);
                module = createModule(project.getName(), project.getName(), "spigot.library", "1.0", project);
                moduleDelegate.initialize(module);
            }
        }
        catch (Exception e)
        {
            // TODO logging
        }
        finally
        {
            if (module != null)
            {
                if (getModuleDelegate(module) == null)
                    moduleDelegates.add(moduleDelegate);
            }
        }
        if (module == null)
            return null;
        return new IModule[] { module };
    }
    
    @Override
    protected IPath[] getListenerPaths()
    {
        return new IPath[] { new Path(".project"), // nature //$NON-NLS-1$
                // new Path(StructureEdit.MODULE_META_FILE_NAME), // component
                new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets //$NON-NLS-1$
        };
    }
    
    /**
     * Returns true if the project represents a deployable project of this type.
     * 
     * @param project
     *            org.eclipse.core.resources.IProject
     * @return boolean
     */
    protected boolean isValidPlugin(IProject project) {
        try {
            IFacetedProject facetedProject = ProjectFacetsManager.create(project);
            if (facetedProject == null)
                return false;
            IProjectFacet spigotPlugin = ProjectFacetsManager.getProjectFacet("spigot.plugin"); //$NON-NLS-1$
            return facetedProject.hasProjectFacet(spigotPlugin);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Returns true if the project represents a deployable project of this type.
     * 
     * @param project
     *            org.eclipse.core.resources.IProject
     * @return boolean
     */
    protected boolean isValidLib(IProject project) {
        try {
            IFacetedProject facetedProject = ProjectFacetsManager.create(project);
            if (facetedProject == null)
                return false;
            IProjectFacet spigotLib = ProjectFacetsManager.getProjectFacet("spigot.lib"); //$NON-NLS-1$
            return facetedProject.hasProjectFacet(spigotLib);
        } catch (Exception e) {
            return false;
        }
    }

    
}
