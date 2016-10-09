/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftRuntimeWorkingCopy;
import eu.xworlds.mceclipse.server.runtime.internal.ISpigotRuntimeWorkingCopy;

/**
 * @author mepeisen
 *
 */
public class SpigotRuntimeComposite extends AbstractRuntimeComposite
{
    
    /**
     * TomcatRuntimeWizardPage constructor comment.
     * 
     * @param parent
     *            the parent composite
     * @param wizard
     *            the wizard handle
     */
    protected SpigotRuntimeComposite(Composite parent, IWizardHandle wizard)
    {
        super("Spigot server", McEclipsePlugin.getImageDescriptor(McEclipsePlugin.IMG_WIZ_SPIGOT), parent, wizard);
    }
    
    @Override
    protected IMinecraftRuntimeWorkingCopy getRuntime(IRuntimeWorkingCopy newRuntime)
    {
        return (ISpigotRuntimeWorkingCopy) newRuntime.loadAdapter(ISpigotRuntimeWorkingCopy.class, null);
    }
    
}
