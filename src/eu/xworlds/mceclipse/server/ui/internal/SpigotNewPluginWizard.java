/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import eu.xworlds.mceclipse.MceProjectTool;

/**
 * @author mepeisen
 *
 */
public class SpigotNewPluginWizard extends AbstractNewPluginWizard
{
    
    @Override
    protected void prepareSimpleModel(final Model model)
    {
        MceProjectTool.prepareModelForSpigotPlugin(model);
    }
    
    @Override
    protected void prepareSimpleProject(final IProject project, Model model, IProgressMonitor monitor) throws CoreException
    {
        MceProjectTool.enableSpigotPluginFacet(project, monitor);
        MceProjectTool.createSpigotPluginFiles(project, model, monitor);
    }
    
    @Override
    protected void prepareArchetypeProject(IProgressMonitor monitor, final IProject prj) throws CoreException
    {
        MceProjectTool.enableSpigotPluginFacet(prj, monitor);
    }

    @Override
    protected IStatus validateModel(Model model)
    {
        return MceProjectTool.validateSpigotModel(model);
    }
    
}
