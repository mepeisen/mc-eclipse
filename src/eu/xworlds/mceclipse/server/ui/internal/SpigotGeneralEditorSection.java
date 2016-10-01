/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.runtime.internal.ISpigotServer;
import eu.xworlds.mceclipse.server.runtime.internal.ISpigotVersionHandler;
import eu.xworlds.mceclipse.server.runtime.internal.SpigotServer;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetDebugModeCommand;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetServeModulesWithoutPublishCommand;

/**
 * @author mepeisen
 *
 */
public class SpigotGeneralEditorSection extends ServerEditorSection {
    protected SpigotServer tomcatServer;

    protected Button noPublish;
    protected Button debug;
    protected boolean updating;

    protected PropertyChangeListener listener;
    
    protected boolean noPublishChanged;

    /**
     * ServerGeneralEditorPart constructor comment.
     */
    public SpigotGeneralEditorSection() {
        // do nothing
    }

    /**
     * 
     */
    protected void addChangeListener() {
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (updating)
                    return;
                updating = true;
                if (SpigotServer.PROPERTY_DEBUG.equals(event.getPropertyName())) {
                    Boolean b = (Boolean) event.getNewValue();
                    SpigotGeneralEditorSection.this.debug.setSelection(b.booleanValue());
                } else if (ISpigotServer.PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH.equals(event.getPropertyName())) {
                    Boolean b = (Boolean) event.getNewValue();
                    SpigotGeneralEditorSection.this.noPublish.setSelection(b.booleanValue());
                    // Indicate this setting has changed
                    noPublishChanged = true;
                }
                updating = false;
            }
        };
        server.addPropertyChangeListener(listener);
    }
    
    /**
     * Creates the SWT controls for this workbench part.
     *
     * @param parent the parent control
     */
    public void createSection(Composite parent) {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());
        
        Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
            | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText("Server Options");
        section.setDescription("Enter settings for the server.");
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
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        
        // serve modules without publish
        noPublish = toolkit.createButton(composite, NLS.bind("Serve modules without publishing {0}", ""), SWT.CHECK);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 3;
        noPublish.setLayoutData(data);
        noPublish.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                if (updating)
                    return;
                updating = true;
                execute(new SetServeModulesWithoutPublishCommand(tomcatServer, noPublish.getSelection()));
                // Indicate this setting has changed
                noPublishChanged = true;
                updating = false;
            }
        });
        // TODO Address help
//      whs.setHelp(noPublish, ContextIds.SERVER_EDITOR_SECURE);

        // debug mode
        debug = toolkit.createButton(composite, NLS.bind("Enable Spigot debug logging {0}", ""), SWT.CHECK);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 3;
        debug.setLayoutData(data);
        debug.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                if (updating)
                    return;
                updating = true;
                execute(new SetDebugModeCommand(tomcatServer, debug.getSelection()));
                updating = false;
            }
        });
//        whs.setHelp(debug, ContextIds.SERVER_EDITOR_DEBUG_MODE);
    
        initialize();
    }

    /**
     * @see ServerEditorSection#dispose()
     */
    public void dispose() {
        if (server != null)
            server.removePropertyChangeListener(listener);
    }

    /**
     * @see ServerEditorSection#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        
        if (server != null) {
            tomcatServer = (SpigotServer) server.loadAdapter(SpigotServer.class, null);
            addChangeListener();
        }
        initialize();
    }

    /**
     * Initialize the fields in this editor.
     */
    protected void initialize() {
        if (tomcatServer == null)
            return;
        updating = true;
        ISpigotVersionHandler tvh = tomcatServer.getSpigotVersionHandler();
        
        String label = NLS.bind("Serve modules without publishing {0}", "");
        noPublish.setText(label);
        noPublish.setSelection(tomcatServer.isServeModulesWithoutPublish());
        if (readOnly)
            noPublish.setEnabled(false);
        else
            noPublish.setEnabled(true);
        
        label = NLS.bind("Enable Tomcat debug logging {0}", "");
        debug.setText(label);
        if (readOnly)
            debug.setEnabled(false);
        else {
            debug.setEnabled(true);
            debug.setSelection(tomcatServer.isDebug());
        }
        
        updating = false;
    }

    /**
     * @see ServerEditorSection#getSaveStatus()
     */
    public IStatus[] getSaveStatus() {
        // If serve modules without publishing has changed, request clean publish to be safe
        if (noPublishChanged) {
            // If server is running, abort the save since clean publish won't succeed
            if (tomcatServer.getServer().getServerState() != IServer.STATE_STOPPED) {
                return new IStatus [] {
                        new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                                NLS.bind("The server must be stopped before a change to the \"{0}\" setting can be saved.",
                                        NLS.bind("Serve modules without publishing {0}", "").trim()))
                };
            }
            // Force a clean publish
            tomcatServer.getServer().publish(IServer.PUBLISH_CLEAN, null, null, null);
            noPublishChanged = false;
        }
        // use default implementation to return success
        return super.getSaveStatus();
    }
    
}
