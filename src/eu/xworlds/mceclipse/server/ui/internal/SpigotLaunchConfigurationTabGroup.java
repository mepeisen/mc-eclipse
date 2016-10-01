/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.wst.server.ui.ServerLaunchConfigurationTab;

/**
 * @author mepeisen
 *
 */
public class SpigotLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup
{
    
    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
        final ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[6];
        tabs[0] = new ServerLaunchConfigurationTab(new String[] { "eu.xworlds.mceclipse.server.spigot" }); //$NON-NLS-1$
        tabs[0].setLaunchConfigurationDialog(dialog);
        tabs[1] = new JavaArgumentsTab();
        tabs[1].setLaunchConfigurationDialog(dialog);
        tabs[2] = new JavaClasspathTab();
        tabs[2].setLaunchConfigurationDialog(dialog);
        tabs[3] = new SourceLookupTab();
        tabs[3].setLaunchConfigurationDialog(dialog);
        tabs[4] = new EnvironmentTab();
        tabs[4].setLaunchConfigurationDialog(dialog);
        tabs[5] = new CommonTab();
        tabs[5].setLaunchConfigurationDialog(dialog);
        setTabs(tabs);
    }
    
}
