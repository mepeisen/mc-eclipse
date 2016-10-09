/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.IInstallableRuntime;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.ui.internal.SWTUtil;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.internal.wizard.fragment.LicenseWizardFragment;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

import eu.xworlds.mceclipse.server.IMinecraftRuntimeWorkingCopy;

/**
 * @author mepeisen
 *
 */
public abstract class AbstractRuntimeComposite extends Composite
{
    /** est runtime. */
    protected IRuntimeWorkingCopy          runtimeWC;
    /** minecraft runtime. */
    protected IMinecraftRuntimeWorkingCopy runtime;
    
    /** wizard. */
    protected IWizardHandle                wizard;
    
    /** install dir. */
    protected Text                         installDir;
    /** name. */
    protected Text                         name;
    /** combo. */
    protected Combo                        combo;
    /** installed jres list. */
    protected List<IVMInstall>             installedJREs;
    /** jre names. */
    protected String[]                     jreNames;
    /** installable runtime. */
    protected IInstallableRuntime          ir;
    /** install job. */
    protected Job                          installRuntimeJob;
    /** job changed listener. */
    protected IJobChangeListener           jobListener;
    /** install label. */
    protected Label                        installLabel;
    /** install button. */
    protected Button                       install;
    
    /**
     * TomcatRuntimeWizardPage constructor comment.
     * 
     * @param wizardTitle
     *            title for wizard
     * @param wizImage
     *            wizard image.
     * @param parent
     *            the parent composite
     * @param wizard
     *            the wizard handle
     */
    protected AbstractRuntimeComposite(String wizardTitle, ImageDescriptor wizImage, Composite parent, IWizardHandle wizard)
    {
        super(parent, SWT.NONE);
        this.wizard = wizard;
        
        wizard.setTitle(wizardTitle);
        wizard.setDescription("Specify the installation directory");
        wizard.setImageDescriptor(wizImage);
        
        createControl();
    }
    
    /**
     * Sets the runtime.
     * 
     * @param newRuntime
     */
    protected void setRuntime(IRuntimeWorkingCopy newRuntime)
    {
        if (newRuntime == null)
        {
            this.runtimeWC = null;
            this.runtime = null;
        }
        else
        {
            this.runtimeWC = newRuntime;
            this.runtime = getRuntime(this.runtimeWC);
        }
        
        if (this.runtimeWC == null)
        {
            this.ir = null;
            this.install.setEnabled(false);
            this.installLabel.setText("");
        }
        else
        {
            this.ir = ServerPlugin.findInstallableRuntime(this.runtimeWC.getRuntimeType().getId());
            if (this.ir != null)
            {
                this.install.setEnabled(true);
                this.installLabel.setText(this.ir.getName());
            }
        }
        
        init();
        validate();
    }
    
    /**
     * @param runtimeWC2
     * @return minecraft runtime.
     */
    protected abstract IMinecraftRuntimeWorkingCopy getRuntime(IRuntimeWorkingCopy runtimeWC2);
    
    @Override
    public void dispose()
    {
        super.dispose();
        if (this.installRuntimeJob != null)
        {
            this.installRuntimeJob.removeJobChangeListener(this.jobListener);
        }
    }
    
    /**
     * Provide a wizard page to change the Tomcat installation directory.
     */
    protected void createControl()
    {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ContextIds.RUNTIME_COMPOSITE);
        
        Label label = new Label(this, SWT.NONE);
        label.setText("Na&me:");
        GridData data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        
        this.name = new Text(this, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.name.setLayoutData(data);
        this.name.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                AbstractRuntimeComposite.this.runtimeWC.setName(AbstractRuntimeComposite.this.name.getText());
                validate();
            }
        });
        
        label = new Label(this, SWT.NONE);
        label.setText("Installation &directory:");
        data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        
        this.installDir = new Text(this, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.installDir.setLayoutData(data);
        this.installDir.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                AbstractRuntimeComposite.this.runtimeWC.setLocation(new Path(AbstractRuntimeComposite.this.installDir.getText()));
                validate();
            }
        });
        
        Button browse = SWTUtil.createButton(this, "B&rowse...");
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                DirectoryDialog dialog = new DirectoryDialog(AbstractRuntimeComposite.this.getShell());
                dialog.setMessage("Select installation directory.");
                dialog.setFilterPath(AbstractRuntimeComposite.this.installDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null)
                    AbstractRuntimeComposite.this.installDir.setText(selectedDirectory);
            }
        });
        
        this.installLabel = new Label(this, SWT.RIGHT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = 10;
        this.installLabel.setLayoutData(data);
        
        this.install = SWTUtil.createButton(this, "Download and Install...");
        this.install.setEnabled(false);
        this.install.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                String license = null;
                try
                {
                    license = AbstractRuntimeComposite.this.ir.getLicense(new NullProgressMonitor());
                }
                catch (CoreException e)
                {
                    // Trace.trace(Trace.SEVERE, "Error getting license", e);
                }
                TaskModel taskModel = new TaskModel();
                taskModel.putObject(LicenseWizardFragment.LICENSE, license);
                TaskWizard wizard2 = new TaskWizard("Download and Install", new WizardFragment() {
                    @Override
                    protected void createChildFragments(List<WizardFragment> list)
                    {
                        list.add(new LicenseWizardFragment());
                    }
                }, taskModel);
                
                WizardDialog dialog2 = new WizardDialog(getShell(), wizard2);
                if (dialog2.open() == Window.CANCEL)
                    return;
                
                DirectoryDialog dialog = new DirectoryDialog(AbstractRuntimeComposite.this.getShell());
                dialog.setMessage("Select installation directory.");
                dialog.setFilterPath(AbstractRuntimeComposite.this.installDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null)
                {
                    // ir.install(new Path(selectedDirectory));
                    final IPath installPath = new Path(selectedDirectory);
                    AbstractRuntimeComposite.this.installRuntimeJob = new Job("Installing server runtime environment") {
                        @Override
                        public boolean belongsTo(Object family)
                        {
                            return ServerPlugin.PLUGIN_ID.equals(family);
                        }
                        
                        @Override
                        protected IStatus run(IProgressMonitor monitor)
                        {
                            try
                            {
                                AbstractRuntimeComposite.this.ir.install(installPath, monitor);
                            }
                            catch (CoreException ce)
                            {
                                return ce.getStatus();
                            }
                            
                            return Status.OK_STATUS;
                        }
                    };
                    
                    AbstractRuntimeComposite.this.installDir.setText(selectedDirectory);
                    AbstractRuntimeComposite.this.jobListener = new JobChangeAdapter() {
                        @Override
                        public void done(IJobChangeEvent event)
                        {
                            AbstractRuntimeComposite.this.installRuntimeJob.removeJobChangeListener(this);
                            AbstractRuntimeComposite.this.installRuntimeJob = null;
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run()
                                {
                                    if (!isDisposed())
                                    {
                                        validate();
                                    }
                                }
                            });
                        }
                    };
                    AbstractRuntimeComposite.this.installRuntimeJob.addJobChangeListener(AbstractRuntimeComposite.this.jobListener);
                    AbstractRuntimeComposite.this.installRuntimeJob.schedule();
                }
            }
        });
        
        updateJREs();
        
        // JDK location
        label = new Label(this, SWT.NONE);
        label.setText("&JRE");
        data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        
        this.combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.combo.setItems(this.jreNames);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        this.combo.setLayoutData(data);
        
        this.combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                int sel = AbstractRuntimeComposite.this.combo.getSelectionIndex();
                IVMInstall vmInstall = null;
                if (sel > 0)
                    vmInstall = AbstractRuntimeComposite.this.installedJREs.get(sel - 1);
                
                AbstractRuntimeComposite.this.runtime.setVMInstall(vmInstall);
                validate();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                widgetSelected(e);
            }
        });
        
        Button button = SWTUtil.createButton(this, "&Installed JREs...");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                String currentVM = AbstractRuntimeComposite.this.combo.getText();
                if (showPreferencePage())
                {
                    updateJREs();
                    AbstractRuntimeComposite.this.combo.setItems(AbstractRuntimeComposite.this.jreNames);
                    AbstractRuntimeComposite.this.combo.setText(currentVM);
                    if (AbstractRuntimeComposite.this.combo.getSelectionIndex() == -1)
                        AbstractRuntimeComposite.this.combo.select(0);
                    validate();
                }
            }
        });
        
        init();
        validate();
        
        Dialog.applyDialogFont(this);
        
        this.name.forceFocus();
    }
    
    /**
     * update jres.
     */
    protected void updateJREs()
    {
        // get all installed JVMs
        this.installedJREs = new ArrayList<>();
        IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
        int size = vmInstallTypes.length;
        for (int i = 0; i < size; i++)
        {
            IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
            int size2 = vmInstalls.length;
            for (int j = 0; j < size2; j++)
            {
                this.installedJREs.add(vmInstalls[j]);
            }
        }
        
        // get names
        size = this.installedJREs.size();
        this.jreNames = new String[size + 1];
        this.jreNames[0] = "Workbench default JRE";
        for (int i = 0; i < size; i++)
        {
            IVMInstall vmInstall = this.installedJREs.get(i);
            this.jreNames[i + 1] = vmInstall.getName();
        }
    }
    
    /**
     * @return true for ok
     */
    protected boolean showPreferencePage()
    {
        String id = "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage"; //$NON-NLS-1$
        
        // should be using the following API, but it only allows a single preference page instance.
        // see bug 168211 for details
        // PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null);
        // return (dialog.open() == Window.OK);
        
        PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode node = manager.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage").findSubNode(id); //$NON-NLS-1$
        PreferenceManager manager2 = new PreferenceManager();
        manager2.addToRoot(node);
        PreferenceDialog dialog = new PreferenceDialog(getShell(), manager2);
        dialog.create();
        return (dialog.open() == Window.OK);
    }
    
    /**
     * init
     */
    protected void init()
    {
        if (this.name == null || this.runtime == null)
            return;
        
        if (this.runtimeWC.getName() != null)
            this.name.setText(this.runtimeWC.getName());
        else
            this.name.setText(""); //$NON-NLS-1$
        
        if (this.runtimeWC.getLocation() != null)
            this.installDir.setText(this.runtimeWC.getLocation().toOSString());
        else
            this.installDir.setText(""); //$NON-NLS-1$
        
        // set selection
        if (this.runtime.isUsingDefaultJRE())
            this.combo.select(0);
        else
        {
            boolean found = false;
            int size = this.installedJREs.size();
            for (int i = 0; i < size; i++)
            {
                IVMInstall vmInstall = this.installedJREs.get(i);
                if (vmInstall.equals(this.runtime.getVMInstall()))
                {
                    this.combo.select(i + 1);
                    found = true;
                }
            }
            if (!found)
                this.combo.select(0);
        }
    }
    
    /**
     * validate
     */
    protected void validate()
    {
        if (this.runtime == null)
        {
            this.wizard.setMessage("", IMessageProvider.ERROR); //$NON-NLS-1$
            return;
        }
        
        IStatus status = this.runtimeWC.validate(null);
        if (status == null || status.isOK())
            this.wizard.setMessage(null, IMessageProvider.NONE);
        else if (status.getSeverity() == IStatus.WARNING)
            this.wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
        else
            this.wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
        this.wizard.update();
    }
    
}
