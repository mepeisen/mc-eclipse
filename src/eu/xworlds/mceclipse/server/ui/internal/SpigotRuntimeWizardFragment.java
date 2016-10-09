/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

/**
 * @author mepeisen
 *
 */
public class SpigotRuntimeWizardFragment extends WizardFragment
{
    /**
     * 
     */
    protected SpigotRuntimeComposite comp;
    
    /**
     * 
     */
    public SpigotRuntimeWizardFragment()
    {
        // do nothing
    }
    
    @Override
    public boolean hasComposite()
    {
        return true;
    }
    
    @Override
    public boolean isComplete()
    {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
        
        if (runtime == null)
            return false;
        IStatus status = runtime.validate(null);
        return (status == null || status.getSeverity() != IStatus.ERROR);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.wst.server.ui.task.WizardFragment#createComposite()
     */
    @Override
    public Composite createComposite(Composite parent, IWizardHandle wizard)
    {
        this.comp = new SpigotRuntimeComposite(parent, wizard);
        return this.comp;
    }
    
    @Override
    public void enter()
    {
        if (this.comp != null)
        {
            IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
            this.comp.setRuntime(runtime);
        }
    }
    
    @Override
    public void exit()
    {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
        IPath path = runtime.getLocation();
        // if (runtime.validate(null).getSeverity() != IStatus.ERROR)
        // TomcatPlugin.setPreference("location" + runtime.getRuntimeType().getId(), path.toString());
    }
    
}
