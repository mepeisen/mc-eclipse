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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftServer;
import eu.xworlds.mceclipse.server.runtime.internal.AbstractServer;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetDebugModeCommand;
import eu.xworlds.mceclipse.server.runtime.internal.command.SetServeModulesWithoutPublishCommand;

/**
 * @author mepeisen
 *
 */
public abstract class AbstractGeneralEditorSection extends ServerEditorSection
{
    
    /** the server reference. */
    protected AbstractServer<?, ?, ?> abstractServer;
    
    /** no publish button. */
    protected Button                  noPublish;
    /** debug button. */
    protected Button                  debug;
    /** updating button. */
    protected boolean                 updating;
    
    /** the property changed listener. */
    protected PropertyChangeListener  listener;
    
    /** flag for publishing mode changed. */
    protected boolean                 noPublishChanged;
    
    /**
     * AbstractGeneralEditorSection constructor comment.
     */
    public AbstractGeneralEditorSection()
    {
        // do nothing
    }
    
    /**
     * @param srv
     * @return server instance
     */
    protected abstract AbstractServer<?, ?, ?> getServer(IServerWorkingCopy srv);
    
    /**
     * 
     */
    protected void addChangeListener()
    {
        this.listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                if (AbstractGeneralEditorSection.this.updating)
                    return;
                AbstractGeneralEditorSection.this.updating = true;
                if (IMinecraftServer.PROPERTY_DEBUG.equals(event.getPropertyName()))
                {
                    Boolean b = (Boolean) event.getNewValue();
                    AbstractGeneralEditorSection.this.debug.setSelection(b.booleanValue());
                }
                else if (IMinecraftServer.PROPERTY_SERVE_MODULES_WITHOUT_PUBLISH.equals(event.getPropertyName()))
                {
                    Boolean b = (Boolean) event.getNewValue();
                    AbstractGeneralEditorSection.this.noPublish.setSelection(b.booleanValue());
                    // Indicate this setting has changed
                    AbstractGeneralEditorSection.this.noPublishChanged = true;
                }
                AbstractGeneralEditorSection.this.updating = false;
            }
        };
        this.server.addPropertyChangeListener(this.listener);
    }
    
    @Override
    public void createSection(Composite parent)
    {
        super.createSection(parent);
        FormToolkit toolkit = getFormToolkit(parent.getDisplay());
        
        Section section = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
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
        // IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
        // whs.setHelp(composite, ContextIds.SERVER_EDITOR);
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        
        // serve modules without publish
        this.noPublish = toolkit.createButton(composite, NLS.bind("Serve modules without publishing {0}", ""), SWT.CHECK);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 3;
        this.noPublish.setLayoutData(data);
        this.noPublish.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                if (AbstractGeneralEditorSection.this.updating)
                    return;
                AbstractGeneralEditorSection.this.updating = true;
                execute(new SetServeModulesWithoutPublishCommand(AbstractGeneralEditorSection.this.abstractServer, AbstractGeneralEditorSection.this.noPublish.getSelection()));
                // Indicate this setting has changed
                AbstractGeneralEditorSection.this.noPublishChanged = true;
                AbstractGeneralEditorSection.this.updating = false;
            }
        });
        // TODO Address help
        // whs.setHelp(noPublish, ContextIds.SERVER_EDITOR_SECURE);
        
        // debug mode
        this.debug = toolkit.createButton(composite, NLS.bind("Enable Spigot debug logging {0}", ""), SWT.CHECK);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 3;
        this.debug.setLayoutData(data);
        this.debug.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                if (AbstractGeneralEditorSection.this.updating)
                    return;
                AbstractGeneralEditorSection.this.updating = true;
                execute(new SetDebugModeCommand(AbstractGeneralEditorSection.this.abstractServer, AbstractGeneralEditorSection.this.debug.getSelection()));
                AbstractGeneralEditorSection.this.updating = false;
            }
        });
        // whs.setHelp(debug, ContextIds.SERVER_EDITOR_DEBUG_MODE);
        
        initialize();
    }
    
    @Override
    public void dispose()
    {
        if (this.server != null)
        {
            this.server.removePropertyChangeListener(this.listener);
        }
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input)
    {
        super.init(site, input);
        
        if (this.server != null)
        {
            this.abstractServer = getServer(this.server);
            addChangeListener();
        }
        initialize();
    }
    
    /**
     * Initialize the fields in this editor.
     */
    protected void initialize()
    {
        if (this.abstractServer == null)
        {
            return;
        }
        if (this.noPublish == null)
        {
            return;
        }
        this.updating = true;
        // IVersionHandler tvh = this.abstractServer.getVersionHandler();
        
        String label = NLS.bind("Serve modules without publishing {0}", "");
        this.noPublish.setText(label);
        this.noPublish.setSelection(this.abstractServer.isServeModulesWithoutPublish());
        if (this.readOnly)
            this.noPublish.setEnabled(false);
        else
            this.noPublish.setEnabled(true);
        
        label = NLS.bind("Enable Spigot debug logging {0}", "");
        this.debug.setText(label);
        if (this.readOnly)
            this.debug.setEnabled(false);
        else
        {
            this.debug.setEnabled(true);
            this.debug.setSelection(this.abstractServer.isDebug());
        }
        
        this.updating = false;
    }
    
    @Override
    public IStatus[] getSaveStatus()
    {
        // If serve modules without publishing has changed, request clean publish to be safe
        if (this.noPublishChanged)
        {
            // If server is running, abort the save since clean publish won't succeed
            if (this.abstractServer.getServer().getServerState() != IServer.STATE_STOPPED)
            {
                return new IStatus[] { new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID,
                        NLS.bind("The server must be stopped before a change to the \"{0}\" setting can be saved.", NLS.bind("Serve modules without publishing {0}", "").trim())) };
            }
            // Force a clean publish
            this.abstractServer.getServer().publish(IServer.PUBLISH_CLEAN, null, null, null);
            this.noPublishChanged = false;
        }
        // use default implementation to return success
        return super.getSaveStatus();
    }
    
}
