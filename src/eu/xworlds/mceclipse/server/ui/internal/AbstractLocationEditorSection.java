/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftServer;
import eu.xworlds.mceclipse.server.runtime.internal.AbstractServer;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetDeployDirectoryCommand;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetInstanceDirectoryCommand;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetTestEnvironmentCommand;

/**
 * @author mepeisen
 *
 */
public abstract class AbstractLocationEditorSection extends ServerEditorSection
{
    /** section. */
    protected Section                 section;
    /** the abstract server. */
    protected AbstractServer<?, ?, ?> abstractServer;
    
    /** default deploy dir hyperlink. */
    protected Hyperlink               setDefaultDeployDir;
    
    /** default deploy dir flag. */
    protected boolean                 defaultDeployDirIsSet;
    
    /** button. */
    protected Button                  serverDirMetadata;
    /** button. */
    protected Button                  serverDirInstall;
    /** button. */
    protected Button                  serverDirCustom;
    
    /** server dir browser. */
    protected Text                    serverDir;
    /** server dir browser. */
    protected Button                  serverDirBrowse;
    
    /** deploy dir browser. */
    protected Text                    deployDir;
    /** deploy dir browser. */
    protected Button                  deployDirBrowse;
    
    /** updating flag. */
    protected boolean                 updating;
    
    /** server property change listener. */
    protected PropertyChangeListener  listener;
    
    /** publish listener. */
    protected IPublishListener        publishListener;
    
    /** workspace path. */
    protected IPath                   workspacePath;
    /** default deploy dir. */
    protected IPath                   defaultDeployPath;
    
    /** flag to allow editing */
    protected boolean                 allowRestrictedEditing;
    
    /** temporary dir. */
    protected IPath                   tempDirPath;
    /** install dir path. */
    protected IPath                   installDirPath;
    
    // TODO Avoid hardcoding this at some point
    /** metadir of eclipse */
    private final static String       METADATADIR = ".metadata"; //$NON-NLS-1$
    
    /**
     * ServerGeneralEditorPart constructor comment.
     */
    public AbstractLocationEditorSection()
    {
        // do nothing
    }
    
    /**
     * @param srv
     * @return server instance
     */
    protected abstract AbstractServer<?, ?, ?> getServer(IServerWorkingCopy srv);
    
    /**
     * @return section description
     */
    protected abstract String getSectionDescription();
    
    /**
     * @return does not modify installation text.
     */
    protected abstract String getDoesNotModifyText();
    
    /**
     * @return does controls installation text.
     */
    protected abstract String takesControlText();
    
    /**
     * @return uses installation text.
     */
    protected abstract String useInstallationText();
    
    /**
     * Add listeners to detect undo changes and publishing of the server.
     */
    protected void addChangeListeners()
    {
        this.listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                if (AbstractLocationEditorSection.this.updating)
                    return;
                AbstractLocationEditorSection.this.updating = true;
                if (IMinecraftServer.PROPERTY_INSTANCE_DIR.equals(event.getPropertyName()) || IMinecraftServer.PROPERTY_TEST_ENVIRONMENT.equals(event.getPropertyName()))
                {
                    updateServerDirButtons();
                    updateServerDirFields();
                    validate();
                }
                else if (IMinecraftServer.PROPERTY_DEPLOY_DIR.equals(event.getPropertyName()))
                {
                    String s = (String) event.getNewValue();
                    AbstractLocationEditorSection.this.deployDir.setText(s);
                    updateDefaultDeployLink();
                    validate();
                }
                AbstractLocationEditorSection.this.updating = false;
            }
        };
        this.server.addPropertyChangeListener(this.listener);
        
        this.publishListener = new PublishAdapter() {
            @Override
            public void publishFinished(IServer server2, IStatus status)
            {
                boolean flag = false;
                if (status.isOK() && server2.getModules().length == 0)
                    flag = true;
                if (flag != AbstractLocationEditorSection.this.allowRestrictedEditing)
                {
                    AbstractLocationEditorSection.this.allowRestrictedEditing = flag;
                    // Update the state of the fields
                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run()
                        {
                            boolean customServerDir = false;
                            if (!AbstractLocationEditorSection.this.serverDirCustom.isDisposed())
                                customServerDir = AbstractLocationEditorSection.this.serverDirCustom.getSelection();
                            if (!AbstractLocationEditorSection.this.serverDirMetadata.isDisposed())
                                AbstractLocationEditorSection.this.serverDirMetadata.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing);
                            if (!AbstractLocationEditorSection.this.serverDirInstall.isDisposed())
                                AbstractLocationEditorSection.this.serverDirInstall.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing);
                            if (!AbstractLocationEditorSection.this.serverDirCustom.isDisposed())
                                AbstractLocationEditorSection.this.serverDirCustom.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing);
                            if (!AbstractLocationEditorSection.this.serverDir.isDisposed())
                                AbstractLocationEditorSection.this.serverDir.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing && customServerDir);
                            if (!AbstractLocationEditorSection.this.serverDirBrowse.isDisposed())
                                AbstractLocationEditorSection.this.serverDirBrowse.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing && customServerDir);
                            if (!AbstractLocationEditorSection.this.setDefaultDeployDir.isDisposed())
                                AbstractLocationEditorSection.this.setDefaultDeployDir.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing);
                            if (!AbstractLocationEditorSection.this.deployDir.isDisposed())
                                AbstractLocationEditorSection.this.deployDir.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing);
                            if (!AbstractLocationEditorSection.this.deployDirBrowse.isDisposed())
                                AbstractLocationEditorSection.this.deployDirBrowse.setEnabled(AbstractLocationEditorSection.this.allowRestrictedEditing);
                        }
                    });
                }
            }
        };
        this.server.getOriginal().addPublishListener(this.publishListener);
    }
    
    /**
     * Creates the SWT controls for this workbench part.
     *
     * @param parent
     *            the parent control
     */
    @Override
    public void createSection(Composite parent)
    {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());
        
        this.section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        this.section.setText("Server locations");
        this.section.setDescription("Specify the server and deploy paths.");
        this.section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
        
        Composite composite = toolkit.createComposite(this.section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
        // IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
        // whs.setHelp(composite, ContextIds.SERVER_EDITOR);
        // whs.setHelp(section, ContextIds.SERVER_EDITOR);
        toolkit.paintBordersFor(composite);
        this.section.setClient(composite);
        
        this.serverDirMetadata = toolkit.createButton(composite, NLS.bind("Use workspace metadata {0}", this.getDoesNotModifyText()), SWT.RADIO);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        this.serverDirMetadata.setLayoutData(data);
        this.serverDirMetadata.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (AbstractLocationEditorSection.this.updating || !AbstractLocationEditorSection.this.serverDirMetadata.getSelection())
                    return;
                AbstractLocationEditorSection.this.updating = true;
                execute(new SetTestEnvironmentCommand(AbstractLocationEditorSection.this.abstractServer, true));
                updateServerDirFields();
                AbstractLocationEditorSection.this.updating = false;
                validate();
            }
        });
        
        this.serverDirInstall = toolkit.createButton(composite, NLS.bind(this.useInstallationText(), this.takesControlText()), SWT.RADIO);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        this.serverDirInstall.setLayoutData(data);
        this.serverDirInstall.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (AbstractLocationEditorSection.this.updating || !AbstractLocationEditorSection.this.serverDirInstall.getSelection())
                    return;
                AbstractLocationEditorSection.this.updating = true;
                execute(new SetTestEnvironmentCommand(AbstractLocationEditorSection.this.abstractServer, false));
                updateServerDirFields();
                AbstractLocationEditorSection.this.updating = false;
                validate();
            }
        });
        
        this.serverDirCustom = toolkit.createButton(composite, NLS.bind("Use custom location {0}", this.getDoesNotModifyText()), SWT.RADIO);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        this.serverDirCustom.setLayoutData(data);
        this.serverDirCustom.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (AbstractLocationEditorSection.this.updating || !AbstractLocationEditorSection.this.serverDirCustom.getSelection())
                    return;
                AbstractLocationEditorSection.this.updating = true;
                execute(new SetTestEnvironmentCommand(AbstractLocationEditorSection.this.abstractServer, true));
                updateServerDirFields();
                AbstractLocationEditorSection.this.updating = false;
                validate();
            }
        });
        
        // server directory
        Label label = createLabel(toolkit, composite, "Server path:");
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(data);
        
        this.serverDir = toolkit.createText(composite, null, SWT.SINGLE);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 75;
        this.serverDir.setLayoutData(data);
        this.serverDir.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (AbstractLocationEditorSection.this.updating)
                    return;
                AbstractLocationEditorSection.this.updating = true;
                execute(new SetInstanceDirectoryCommand(AbstractLocationEditorSection.this.abstractServer, getServerDir()));
                AbstractLocationEditorSection.this.updating = false;
                validate();
            }
        });
        
        this.serverDirBrowse = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        this.serverDirBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                DirectoryDialog dialog = new DirectoryDialog(AbstractLocationEditorSection.this.serverDir.getShell());
                dialog.setMessage("Select a server directory.");
                dialog.setFilterPath(AbstractLocationEditorSection.this.serverDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null && !selectedDirectory.equals(AbstractLocationEditorSection.this.serverDir.getText()))
                {
                    AbstractLocationEditorSection.this.updating = true;
                    // Make relative if relative to the workspace
                    IPath path = new Path(selectedDirectory);
                    if (AbstractLocationEditorSection.this.workspacePath.isPrefixOf(path))
                    {
                        int cnt = path.matchingFirstSegments(AbstractLocationEditorSection.this.workspacePath);
                        path = path.removeFirstSegments(cnt).setDevice(null);
                        selectedDirectory = path.toOSString();
                    }
                    execute(new SetInstanceDirectoryCommand(AbstractLocationEditorSection.this.abstractServer, selectedDirectory));
                    updateServerDirButtons();
                    updateServerDirFields();
                    AbstractLocationEditorSection.this.updating = false;
                    validate();
                }
            }
        });
        this.serverDirBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        // deployment directory link
        this.setDefaultDeployDir = toolkit.createHyperlink(composite, NLS.bind("Set deploy path to the default value", ""), SWT.WRAP);
        this.setDefaultDeployDir.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e)
            {
                AbstractLocationEditorSection.this.updating = true;
                execute(new SetDeployDirectoryCommand(AbstractLocationEditorSection.this.abstractServer, IMinecraftServer.DEFAULT_DEPLOYDIR));
                AbstractLocationEditorSection.this.deployDir.setText(IMinecraftServer.DEFAULT_DEPLOYDIR);
                updateDefaultDeployLink();
                AbstractLocationEditorSection.this.updating = false;
                validate();
            }
        });
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        this.setDefaultDeployDir.setLayoutData(data);
        
        // deployment directory
        label = createLabel(toolkit, composite, "Deploy path:");
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(data);
        
        this.deployDir = toolkit.createText(composite, null);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        this.deployDir.setLayoutData(data);
        this.deployDir.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (AbstractLocationEditorSection.this.updating)
                    return;
                AbstractLocationEditorSection.this.updating = true;
                execute(new SetDeployDirectoryCommand(AbstractLocationEditorSection.this.abstractServer, AbstractLocationEditorSection.this.deployDir.getText().trim()));
                updateDefaultDeployLink();
                AbstractLocationEditorSection.this.updating = false;
                validate();
            }
        });
        
        this.deployDirBrowse = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        this.deployDirBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                DirectoryDialog dialog = new DirectoryDialog(AbstractLocationEditorSection.this.deployDir.getShell());
                dialog.setMessage("Select a deploy directory.");
                dialog.setFilterPath(AbstractLocationEditorSection.this.deployDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null && !selectedDirectory.equals(AbstractLocationEditorSection.this.deployDir.getText()))
                {
                    AbstractLocationEditorSection.this.updating = true;
                    execute(new SetDeployDirectoryCommand(AbstractLocationEditorSection.this.abstractServer, selectedDirectory));
                    AbstractLocationEditorSection.this.deployDir.setText(selectedDirectory);
                    AbstractLocationEditorSection.this.updating = false;
                    validate();
                }
            }
        });
        this.deployDirBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        initialize();
    }

    /**
     * Creates a new label
     * @param toolkit
     * @param parent
     * @param text
     * @return label
     */
    protected Label createLabel(FormToolkit toolkit, Composite parent, String text)
    {
        Label label = toolkit.createLabel(parent, text);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        return label;
    }
    
    @Override
    public void dispose()
    {
        if (this.server != null)
        {
            this.server.removePropertyChangeListener(this.listener);
            if (this.server.getOriginal() != null)
                this.server.getOriginal().removePublishListener(this.publishListener);
        }
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input)
    {
        super.init(site, input);
        
        // Cache workspace and default deploy paths
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        this.workspacePath = root.getLocation();
        this.defaultDeployPath = new Path(IMinecraftServer.DEFAULT_DEPLOYDIR);
        
        if (this.server != null)
        {
            this.abstractServer = this.getServer(this.server);
            addChangeListeners();
        }
        initialize();
    }
    
    /**
     * Initialize the fields in this editor.
     */
    protected void initialize()
    {
        if (this.serverDir == null || this.abstractServer == null)
            return;
        this.updating = true;
        
        IRuntime runtime = this.server.getRuntime();
        if (runtime != null)
        {
            this.section.setDescription(this.getSectionDescription());
            this.installDirPath = runtime.getLocation();
        }
        
        // determine if editing of locations is allowed
        this.allowRestrictedEditing = false;
        IPath basePath = this.abstractServer.getRuntimeBaseDirectory();
        if (!this.readOnly)
        {
            // If server has not been published, or server is published with no modules, allow editing
            // TODO Find better way to determine if server hasn't been published
            if ((basePath != null && !basePath.append("plugins").toFile().exists()) //$NON-NLS-1$
                    || (this.server.getOriginal().getServerPublishState() == IServer.PUBLISH_STATE_NONE && this.server.getOriginal().getModules().length == 0))
            {
                this.allowRestrictedEditing = true;
            }
        }
        
        // Update server related fields
        updateServerDirButtons();
        updateServerDirFields();
        
        this.serverDirMetadata.setEnabled(this.allowRestrictedEditing);
        this.serverDirInstall.setEnabled(this.allowRestrictedEditing);
        this.serverDirCustom.setEnabled(this.allowRestrictedEditing);
        
        // Update deployment related fields
        updateDefaultDeployLink();
        
        this.deployDir.setText(this.abstractServer.getDeployDirectory());
        
        this.setDefaultDeployDir.setEnabled(this.allowRestrictedEditing);
        this.deployDir.setEnabled(this.allowRestrictedEditing);
        this.deployDirBrowse.setEnabled(this.allowRestrictedEditing);
        
        this.updating = false;
        validate();
    }

    /**
     * @return server dir
     */
    protected String getServerDir()
    {
        String dir = null;
        if (this.serverDir != null)
        {
            dir = this.serverDir.getText().trim();
            IPath path = new Path(dir);
            // Adjust if the temp dir is known and has been entered
            if (this.tempDirPath != null && this.tempDirPath.equals(path))
                dir = null;
            // If under the workspace, make relative
            else if (this.workspacePath.isPrefixOf(path))
            {
                int cnt = path.matchingFirstSegments(this.workspacePath);
                path = path.removeFirstSegments(cnt).setDevice(null);
                dir = path.toOSString();
            }
        }
        return dir;
    }
    
    /**
     * updates button states
     */
    protected void updateServerDirButtons()
    {
        if (this.abstractServer.getInstanceDirectory() == null)
        {
            IPath path = this.abstractServer.getRuntimeBaseDirectory();
            if (path != null && path.equals(this.installDirPath))
            {
                this.serverDirInstall.setSelection(true);
                this.serverDirMetadata.setSelection(false);
                this.serverDirCustom.setSelection(false);
            }
            else
            {
                this.serverDirMetadata.setSelection(true);
                this.serverDirInstall.setSelection(false);
                this.serverDirCustom.setSelection(false);
            }
        }
        else
        {
            this.serverDirCustom.setSelection(true);
            this.serverDirMetadata.setSelection(false);
            this.serverDirInstall.setSelection(false);
        }
    }
    
    /**
     * update server dir fields.
     */
    protected void updateServerDirFields()
    {
        updateServerDir();
        boolean customServerDir = this.serverDirCustom.getSelection();
        this.serverDir.setEnabled(this.allowRestrictedEditing && customServerDir);
        this.serverDirBrowse.setEnabled(this.allowRestrictedEditing && customServerDir);
    }
    
    /**
     * updates server dir.
     */
    protected void updateServerDir()
    {
        IPath path = this.abstractServer.getRuntimeBaseDirectory();
        if (path == null)
            this.serverDir.setText(""); //$NON-NLS-1$
        else if (this.workspacePath.isPrefixOf(path))
        {
            int cnt = path.matchingFirstSegments(this.workspacePath);
            path = path.removeFirstSegments(cnt).setDevice(null);
            this.serverDir.setText(path.toOSString());
            // cache the relative temp dir path if that is what we have
            if (this.tempDirPath == null)
            {
                if (this.abstractServer.isTestEnvironment() && this.abstractServer.getInstanceDirectory() == null)
                    this.tempDirPath = path;
            }
        }
        else
            this.serverDir.setText(path.toOSString());
    }
    
    /**
     * update default deploy link
     */
    protected void updateDefaultDeployLink()
    {
        boolean newState = this.defaultDeployPath.equals(new Path(this.abstractServer.getDeployDirectory()));
        if (newState != this.defaultDeployDirIsSet)
        {
            this.setDefaultDeployDir.setText(newState ? "Set deploy path to the default value (currently set)" : "Set deploy path to the default value");
            this.defaultDeployDirIsSet = newState;
        }
    }
    
    @Override
    public IStatus[] getSaveStatus()
    {
        if (this.abstractServer != null)
        {
            // Check the instance directory
            String dir = this.abstractServer.getInstanceDirectory();
            if (dir != null)
            {
                IPath path = new Path(dir);
                // Must not be the same as the workspace location
                if (dir.length() == 0 || this.workspacePath.equals(path))
                {
                    return new IStatus[] { new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "The server path may not be set to the the root of your workspace.") };
                }
                // User specified value may not be under the ".metadata" folder of the workspace
                else if (this.workspacePath.isPrefixOf(path) || (!path.isAbsolute() && METADATADIR.equals(path.segment(0))))
                {
                    int cnt = path.matchingFirstSegments(this.workspacePath);
                    if (METADATADIR.equals(path.segment(cnt)))
                    {
                        return new IStatus[] { new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                                NLS.bind("The server path may not be under the \"{0}\" folder of your workspace unless it is the workspace metadata location.", METADATADIR)) };
                    }
                }
                else if (path.equals(this.installDirPath))
                    return new IStatus[] { new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                            NLS.bind("Only the \"{0}\" selection may set the server path to the installation.", NLS.bind(this.useInstallationText(), "").trim())) };
            }
            else
            {
                IPath path = this.abstractServer.getRuntimeBaseDirectory();
                // If non-custom instance dir is not the install and metadata isn't the selection, return error
                if (!path.equals(this.installDirPath) && !this.serverDirMetadata.getSelection())
                {
                    return new IStatus[] { new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                            NLS.bind("Only the \"{0}\" selection may set the server path to the workspace metadata location.", NLS.bind("Use workspace metadata {0}", "").trim())) };
                }
            }
            
            // Check the deployment directory
            dir = this.abstractServer.getDeployDirectory();
            // Deploy directory must be set
            if (dir == null || dir.length() == 0)
            {
                return new IStatus[] { new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "A deploy path must be specified.") };
            }
        }
        // use default implementation to return success
        return super.getSaveStatus();
    }
    
    /**
     * validate settings
     */
    protected void validate()
    {
        if (this.abstractServer != null)
        {
            // Validate instance directory
            String dir = this.abstractServer.getInstanceDirectory();
            if (dir != null)
            {
                IPath path = new Path(dir);
                // Must not be the same as the workspace location
                if (dir.length() == 0 || this.workspacePath.equals(path))
                {
                    setErrorMessage("The server path may not be set to the the root of your workspace.");
                    return;
                }
                // User specified value may not be under the ".metadata" folder of the workspace
                else if (this.workspacePath.isPrefixOf(path) || (!path.isAbsolute() && METADATADIR.equals(path.segment(0))))
                {
                    int cnt = path.matchingFirstSegments(this.workspacePath);
                    if (METADATADIR.equals(path.segment(cnt)))
                    {
                        setErrorMessage(NLS.bind("The server path may not be under the \"{0}\" folder of your workspace unless it is the workspace metadata location.", METADATADIR));
                        return;
                    }
                }
                else if (path.equals(this.installDirPath))
                {
                    setErrorMessage(NLS.bind("Only the \"{0}\" selection may set the server path to the installation.", NLS.bind(this.useInstallationText(), "").trim()));
                    return;
                }
            }
            else
            {
                IPath path = this.abstractServer.getRuntimeBaseDirectory();
                // If non-custom instance dir is not the install and metadata isn't the selection, return error
                if (path != null && !path.equals(this.installDirPath) && !this.serverDirMetadata.getSelection())
                {
                    setErrorMessage(NLS.bind("Only the \"{0}\" selection may set the server path to the workspace metadata location.", NLS.bind("Use workspace metadata {0}", "").trim()));
                }
            }
            
            // Check the deployment directory
            dir = this.abstractServer.getDeployDirectory();
            // Deploy directory must be set
            if (dir == null || dir.length() == 0)
            {
                setErrorMessage("A deploy path must be specified.");
                return;
            }
        }
        // All is okay, clear any previous error
        setErrorMessage(null);
    }
    
}
