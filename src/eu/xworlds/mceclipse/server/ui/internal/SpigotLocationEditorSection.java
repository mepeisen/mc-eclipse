/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.HyperlinkEvent;

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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServer;
import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServerWorkingCopy;
import eu.xworlds.mceclipse.server.runtime.internal.SpigotServer;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetDeployDirectoryCommand;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetInstanceDirectoryCommand;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetTestEnvironmentCommand;

/**
 * @author mepeisen
 *
 */
public class SpigotLocationEditorSection extends ServerEditorSection {
    protected Section section;
    protected SpigotServer spigotServer;

    protected Hyperlink setDefaultDeployDir;
    
    protected boolean defaultDeployDirIsSet;
    
    protected Button serverDirMetadata;
    protected Button serverDirInstall;
    protected Button serverDirCustom;
    
    protected Text serverDir;
    protected Button serverDirBrowse;
    protected Text deployDir;
    protected Button deployDirBrowse;
    protected boolean updating;

    protected PropertyChangeListener listener;
    protected IPublishListener publishListener;
    protected IPath workspacePath;
    protected IPath defaultDeployPath;
    
    protected boolean allowRestrictedEditing;
    protected IPath tempDirPath;
    protected IPath installDirPath;

    // Avoid hardcoding this at some point
    private final static String METADATADIR = ".metadata";
    /**
     * ServerGeneralEditorPart constructor comment.
     */
    public SpigotLocationEditorSection() {
        // do nothing
    }

    /**
     * Add listeners to detect undo changes and publishing of the server.
     */
    protected void addChangeListeners() {
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (updating)
                    return;
                updating = true;
                if (ISpigotServer.PROPERTY_INSTANCE_DIR.equals(event.getPropertyName())
                        || ISpigotServer.PROPERTY_TEST_ENVIRONMENT.equals(event.getPropertyName())) {
                    updateServerDirButtons();
                    updateServerDirFields();
                    validate();
                }
                else if (ISpigotServer.PROPERTY_DEPLOY_DIR.equals(event.getPropertyName())) {
                    String s = (String) event.getNewValue();
                    SpigotLocationEditorSection.this.deployDir.setText(s);
                    updateDefaultDeployLink();                  
                    validate();
                }
                updating = false;
            }
        };
        server.addPropertyChangeListener(listener);
        
        publishListener = new PublishAdapter() {
            public void publishFinished(IServer server2, IStatus status) {
                boolean flag = false;
                if (status.isOK() && server2.getModules().length == 0)
                    flag = true;
                if (flag != allowRestrictedEditing) {
                    allowRestrictedEditing = flag;
                    // Update the state of the fields
                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            boolean customServerDir = false;
                            if (!SpigotLocationEditorSection.this.serverDirCustom.isDisposed())
                                customServerDir = SpigotLocationEditorSection.this.serverDirCustom.getSelection();
                            if (!SpigotLocationEditorSection.this.serverDirMetadata.isDisposed())
                                SpigotLocationEditorSection.this.serverDirMetadata.setEnabled(allowRestrictedEditing);
                            if (!SpigotLocationEditorSection.this.serverDirInstall.isDisposed())
                                SpigotLocationEditorSection.this.serverDirInstall.setEnabled(allowRestrictedEditing);
                            if (!SpigotLocationEditorSection.this.serverDirCustom.isDisposed())
                                SpigotLocationEditorSection.this.serverDirCustom.setEnabled(allowRestrictedEditing);
                            if (!SpigotLocationEditorSection.this.serverDir.isDisposed())
                                SpigotLocationEditorSection.this.serverDir.setEnabled(allowRestrictedEditing && customServerDir);
                            if (!SpigotLocationEditorSection.this.serverDirBrowse.isDisposed())
                                SpigotLocationEditorSection.this.serverDirBrowse.setEnabled(allowRestrictedEditing && customServerDir);
                            if (!SpigotLocationEditorSection.this.setDefaultDeployDir.isDisposed())
                                SpigotLocationEditorSection.this.setDefaultDeployDir.setEnabled(allowRestrictedEditing);
                            if (!SpigotLocationEditorSection.this.deployDir.isDisposed())
                                SpigotLocationEditorSection.this.deployDir.setEnabled(allowRestrictedEditing);
                            if (!SpigotLocationEditorSection.this.deployDirBrowse.isDisposed())
                                SpigotLocationEditorSection.this.deployDirBrowse.setEnabled(allowRestrictedEditing);
                        }
                    });
                }
            }
        };
        server.getOriginal().addPublishListener(publishListener);
    }
    
    /**
     * Creates the SWT controls for this workbench part.
     *
     * @param parent the parent control
     */
    public void createSection(Composite parent) {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
            | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText("Server locations");
        section.setDescription("Specify the server and deploy paths.");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
//        IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
//        whs.setHelp(composite, ContextIds.SERVER_EDITOR);
//        whs.setHelp(section, ContextIds.SERVER_EDITOR);
        toolkit.paintBordersFor(composite);
        section.setClient(composite);

        serverDirMetadata = toolkit.createButton(composite,
                NLS.bind("Use workspace metadata {0}", "(does not modify Spigot installation)"), SWT.RADIO);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        serverDirMetadata.setLayoutData(data);
        serverDirMetadata.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (updating || !serverDirMetadata.getSelection())
                    return;
                updating = true;
                execute(new SetTestEnvironmentCommand(spigotServer, true));
                updateServerDirFields();
                updating = false;
                validate();
            }
        });

        serverDirInstall = toolkit.createButton(composite,
                NLS.bind("Use Spigot installation {0}", "takes control of Spigot installation)"), SWT.RADIO);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        serverDirInstall.setLayoutData(data);
        serverDirInstall.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (updating || !serverDirInstall.getSelection())
                    return;
                updating = true;
                execute(new SetTestEnvironmentCommand(spigotServer, false));
                updateServerDirFields();
                updating = false;
                validate();
            }
        });

        serverDirCustom = toolkit.createButton(composite,
                NLS.bind("Use custom location {0}", "(does not modify Spigot installation)"), SWT.RADIO);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        serverDirCustom.setLayoutData(data);
        serverDirCustom.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (updating || !serverDirCustom.getSelection())
                    return;
                updating = true;
                execute(new SetTestEnvironmentCommand(spigotServer, true));
                updateServerDirFields();
                updating = false;
                validate();
            }
        });

        // server directory
        Label label = createLabel(toolkit, composite, "Server path:");
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(data);

        serverDir = toolkit.createText(composite, null, SWT.SINGLE);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 75;
        serverDir.setLayoutData(data);
        serverDir.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (updating)
                    return;
                updating = true;
                execute(new SetInstanceDirectoryCommand(spigotServer, getServerDir()));
                updating = false;
                validate();
            }
        });

        serverDirBrowse = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        serverDirBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                DirectoryDialog dialog = new DirectoryDialog(serverDir.getShell());
                dialog.setMessage("Select a server directory.");
                dialog.setFilterPath(serverDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null && !selectedDirectory.equals(serverDir.getText())) {
                    updating = true;
                    // Make relative if relative to the workspace
                    IPath path = new Path(selectedDirectory);
                    if (workspacePath.isPrefixOf(path)) {
                        int cnt = path.matchingFirstSegments(workspacePath);
                        path = path.removeFirstSegments(cnt).setDevice(null);
                        selectedDirectory = path.toOSString();
                    }
                    execute(new SetInstanceDirectoryCommand(spigotServer, selectedDirectory));
                    updateServerDirButtons();
                    updateServerDirFields();
                    updating = false;
                    validate();
                }
            }
        });
        serverDirBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        // deployment directory link
        setDefaultDeployDir = toolkit.createHyperlink(composite,
                NLS.bind("Set deploy path to the default value", ""), SWT.WRAP);
        setDefaultDeployDir.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                updating = true;
                execute(new SetDeployDirectoryCommand(spigotServer, ISpigotServerWorkingCopy.DEFAULT_DEPLOYDIR));
                deployDir.setText(ISpigotServerWorkingCopy.DEFAULT_DEPLOYDIR);
                updateDefaultDeployLink();
                updating = false;
                validate();
            }
        });
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 3;
        setDefaultDeployDir.setLayoutData(data);

        // deployment directory
        label = createLabel(toolkit, composite, "Deploy path:");
        data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(data);

        deployDir = toolkit.createText(composite, null);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        deployDir.setLayoutData(data);
        deployDir.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (updating)
                    return;
                updating = true;
                execute(new SetDeployDirectoryCommand(spigotServer, deployDir.getText().trim()));
                updateDefaultDeployLink();
                updating = false;
                validate();
            }
        });

        deployDirBrowse = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        deployDirBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                DirectoryDialog dialog = new DirectoryDialog(deployDir.getShell());
                dialog.setMessage("Select a deploy directory.");
                dialog.setFilterPath(deployDir.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null && !selectedDirectory.equals(deployDir.getText())) {
                    updating = true;
                    execute(new SetDeployDirectoryCommand(spigotServer, selectedDirectory));
                    deployDir.setText(selectedDirectory);
                    updating = false;
                    validate();
                }
            }
        });
        deployDirBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        initialize();
    }

    protected Label createLabel(FormToolkit toolkit, Composite parent, String text) {
        Label label = toolkit.createLabel(parent, text);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        return label;
    }

    /**
     * @see ServerEditorSection#dispose()
     */
    public void dispose() {
        if (server != null) {
            server.removePropertyChangeListener(listener);
            if (server.getOriginal() != null)
                server.getOriginal().removePublishListener(publishListener);
        }
    }

    /**
     * @see ServerEditorSection#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        
        // Cache workspace and default deploy paths
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        workspacePath = root.getLocation();
        defaultDeployPath = new Path(ISpigotServerWorkingCopy.DEFAULT_DEPLOYDIR);

        if (server != null) {
            spigotServer = (SpigotServer) server.loadAdapter(SpigotServer.class, null);
            addChangeListeners();
        }
        initialize();
    }

    /**
     * Initialize the fields in this editor.
     */
    protected void initialize() {
        if (serverDir== null || spigotServer == null)
            return;
        updating = true;

        IRuntime runtime = server.getRuntime();
        // If not Tomcat 3.2, update description to mention catalina.base
        if (runtime != null && runtime.getRuntimeType().getId().indexOf("32") < 0)
            section.setDescription("Specify the server path (continaing spigot.jar) and deploy path.");
        if (runtime != null)
            installDirPath = runtime.getLocation();

        // determine if editing of locations is allowed
        allowRestrictedEditing = false;
        IPath basePath = spigotServer.getRuntimeBaseDirectory();
        if (!readOnly) {
            // If server has not been published, or server is published with no modules, allow editing
            // TODO Find better way to determine if server hasn't been published
            if ((basePath != null && !basePath.append("conf").toFile().exists())
                    || (server.getOriginal().getServerPublishState() == IServer.PUBLISH_STATE_NONE
                            && server.getOriginal().getModules().length == 0)) {
                allowRestrictedEditing = true;
            }
        }
        
        // Update server related fields
        updateServerDirButtons();
        updateServerDirFields();

        serverDirMetadata.setEnabled(allowRestrictedEditing);
        serverDirInstall.setEnabled(allowRestrictedEditing);
        serverDirCustom.setEnabled(allowRestrictedEditing);

        // Update deployment related fields
        updateDefaultDeployLink();
        
        deployDir.setText(spigotServer.getDeployDirectory());

        setDefaultDeployDir.setEnabled(allowRestrictedEditing);
        deployDir.setEnabled(allowRestrictedEditing);
        deployDirBrowse.setEnabled(allowRestrictedEditing);

        updating = false;
        validate();
    }
    
    protected String getServerDir() {
        String dir = null;
        if (serverDir != null) {
            dir = serverDir.getText().trim();
            IPath path = new Path(dir);
            // Adjust if the temp dir is known and has been entered
            if (tempDirPath != null && tempDirPath.equals(path))
                dir = null;
            // If under the workspace, make relative
            else if (workspacePath.isPrefixOf(path)) {
                int cnt = path.matchingFirstSegments(workspacePath);
                path = path.removeFirstSegments(cnt).setDevice(null);
                dir = path.toOSString();
            }
        }
        return dir;
    }
    
    protected void updateServerDirButtons() {
        if (spigotServer.getInstanceDirectory() == null) {
            IPath path = spigotServer.getRuntimeBaseDirectory();
            if (path != null && path.equals(installDirPath)) {
                serverDirInstall.setSelection(true);
                serverDirMetadata.setSelection(false);
                serverDirCustom.setSelection(false);
            } else {
                serverDirMetadata.setSelection(true);
                serverDirInstall.setSelection(false);
                serverDirCustom.setSelection(false);
            }
        } else {
            serverDirCustom.setSelection(true);
            serverDirMetadata.setSelection(false);
            serverDirInstall.setSelection(false);
        }
    }
    
    protected void updateServerDirFields() {
        updateServerDir();
        boolean customServerDir = serverDirCustom.getSelection();
        serverDir.setEnabled(allowRestrictedEditing && customServerDir);
        serverDirBrowse.setEnabled(allowRestrictedEditing && customServerDir);
    }
    
    protected void updateServerDir() {
        IPath path = spigotServer.getRuntimeBaseDirectory();
        if (path == null)
            serverDir.setText("");
        else if (workspacePath.isPrefixOf(path)) {
            int cnt = path.matchingFirstSegments(workspacePath);
            path = path.removeFirstSegments(cnt).setDevice(null);
            serverDir.setText(path.toOSString());
            // cache the relative temp dir path if that is what we have
            if (tempDirPath == null) {
                if (spigotServer.isTestEnvironment() && spigotServer.getInstanceDirectory() == null)
                    tempDirPath = path;
            }
        } else
            serverDir.setText(path.toOSString());
    }
    
    protected void updateDefaultDeployLink() {
        boolean newState = defaultDeployPath.equals(new Path(spigotServer.getDeployDirectory()));
        if (newState != defaultDeployDirIsSet) {
            setDefaultDeployDir.setText(
                    newState ? "Set deploy path to the default value (currently set)"
                            : "Set deploy path to the default value");
            defaultDeployDirIsSet = newState;
        }
    }
    
    /**
     * @see ServerEditorSection#getSaveStatus()
     */
    public IStatus[] getSaveStatus() {
        if (spigotServer != null) {
            // Check the instance directory
            String dir = spigotServer.getInstanceDirectory();
            if (dir != null) {
                IPath path = new Path(dir);
                // Must not be the same as the workspace location
                if (dir.length() == 0 || workspacePath.equals(path)) {
                    return new IStatus [] {
                            new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "The server path may not be set to the the root of your workspace.")};
                }
                // User specified value may not be under the ".metadata" folder of the workspace 
                else if (workspacePath.isPrefixOf(path)
                        || (!path.isAbsolute() && METADATADIR.equals(path.segment(0)))) {
                    int cnt = path.matchingFirstSegments(workspacePath);
                    if (METADATADIR.equals(path.segment(cnt))) {
                        return new IStatus [] {
                                new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, NLS.bind("The server path may not be under the \"{0}\" folder of your workspace unless it is the workspace metadata location.", METADATADIR))};
                    }
                }
                else if (path.equals(installDirPath))
                    return new IStatus [] {
                        new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                                NLS.bind("Only the \"{0}\" selection may set the server path to the Spigot installation.",
                                        NLS.bind("Use Spigot installation {0}", "").trim()))};
            }
            else {
                IPath path = spigotServer.getRuntimeBaseDirectory();
                // If non-custom instance dir is not the install and metadata isn't the selection, return error
                if (!path.equals(installDirPath) && !serverDirMetadata.getSelection()) {
                    return new IStatus [] {
                            new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                                    NLS.bind("Only the \"{0}\" selection may set the server path to the workspace metadata location.", 
                                            NLS.bind("Use workspace metadata {0}", "").trim()))};
                }
            }

            // Check the deployment directory
            dir = spigotServer.getDeployDirectory();
            // Deploy directory must be set
            if (dir == null || dir.length() == 0) {
                return new IStatus [] {
                        new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "A deploy path must be specified.")};
            }
        }
        // use default implementation to return success
        return super.getSaveStatus();
    }
    
    protected void validate() {
        if (spigotServer != null) {
            // Validate instance directory
            String dir = spigotServer.getInstanceDirectory();
            if (dir != null) {
                IPath path = new Path(dir);
                // Must not be the same as the workspace location
                if (dir.length() == 0 || workspacePath.equals(path)) {
                    setErrorMessage("The server path may not be set to the the root of your workspace.");
                    return;
                }
                // User specified value may not be under the ".metadata" folder of the workspace 
                else if (workspacePath.isPrefixOf(path)
                        || (!path.isAbsolute() && METADATADIR.equals(path.segment(0)))) {
                    int cnt = path.matchingFirstSegments(workspacePath);
                    if (METADATADIR.equals(path.segment(cnt))) {
                        setErrorMessage(NLS.bind("The server path may not be under the \"{0}\" folder of your workspace unless it is the workspace metadata location.", METADATADIR));
                        return;
                    }
                }
                else if (path.equals(installDirPath)) {
                    setErrorMessage(NLS.bind("Only the \"{0}\" selection may set the server path to the Spigot installation.",
                            NLS.bind("Use Spigot installation {0}", "").trim()));
                    return;
                }
            }
            else {
                IPath path = spigotServer.getRuntimeBaseDirectory();
                // If non-custom instance dir is not the install and metadata isn't the selection, return error
                if (path != null && !path.equals(installDirPath) && !serverDirMetadata.getSelection()) {
                    setErrorMessage(NLS.bind("Only the \"{0}\" selection may set the server path to the workspace metadata location.", 
                            NLS.bind("Use workspace metadata {0}", "").trim()));
                }
            }

            // Check the deployment directory
            dir = spigotServer.getDeployDirectory();
            // Deploy directory must be set
            if (dir == null || dir.length() == 0) {
                setErrorMessage("A deploy path must be specified.");
                return;
            }
        }
        // All is okay, clear any previous error
        setErrorMessage(null);
    }
    
}
