/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import eu.xworlds.mceclipse.server.runtime.internal.AbstractConfiguration;
import eu.xworlds.mceclipse.server.runtime.internal.ISpigotConfigurationWorkingCopy;
import eu.xworlds.mceclipse.server.runtime.internal.command.ModifyPortCommand;

/**
 * @author mepeisen
 *
 */
public abstract class AbstractPortEditorSection extends ServerEditorSection
{
    
    /** config. */
    protected AbstractConfiguration<?, ?> abstractConfiguration;
    
    /** update flag. */
    protected boolean                     updating;
    
    /** ports table. */
    protected Table                       ports;
    /** table viewer. */
    protected TableViewer                 viewer;
    
    /** property changed listener. */
    protected PropertyChangeListener      listener;
    
    /**
     * SpigotPortEditorSection constructor comment.
     */
    public AbstractPortEditorSection()
    {
        super();
    }
    
    /**
     * 
     */
    protected void addChangeListener()
    {
        this.listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                if (ISpigotConfigurationWorkingCopy.MODIFY_PORT_PROPERTY.equals(event.getPropertyName()))
                {
                    String id = (String) event.getOldValue();
                    Integer i = (Integer) event.getNewValue();
                    changePortNumber(id, i.intValue());
                }
            }
        };
        this.abstractConfiguration.addPropertyChangeListener(this.listener);
    }
    
    /**
     * 
     * @param id
     *            java.lang.String
     * @param port
     *            int
     */
    protected void changePortNumber(String id, int port)
    {
        TableItem[] items = this.ports.getItems();
        int size = items.length;
        for (int i = 0; i < size; i++)
        {
            ServerPort sp = (ServerPort) items[i].getData();
            if (sp.getId().equals(id))
            {
                items[i].setData(new ServerPort(id, sp.getName(), port, sp.getProtocol()));
                items[i].setText(1, port + ""); //$NON-NLS-1$
                /*
                 * if (i == selection) { selectPort(); }
                 */
                return;
            }
        }
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
        
        Section section = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
        section.setText("Ports");
        section.setDescription("Modify the server ports.");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
        
        // ports
        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 8;
        layout.marginWidth = 8;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
        // IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
        // whs.setHelp(composite, ContextIds.CONFIGURATION_EDITOR_PORTS);
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        
        this.ports = toolkit.createTable(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.ports.setHeaderVisible(true);
        this.ports.setLinesVisible(true);
        // whs.setHelp(ports, ContextIds.CONFIGURATION_EDITOR_PORTS_LIST);
        
        TableLayout tableLayout = new TableLayout();
        
        TableColumn col = new TableColumn(this.ports, SWT.NONE);
        col.setText("Port Name");
        ColumnWeightData colData = new ColumnWeightData(15, 150, true);
        tableLayout.addColumnData(colData);
        
        col = new TableColumn(this.ports, SWT.NONE);
        col.setText("Port Number");
        colData = new ColumnWeightData(8, 80, true);
        tableLayout.addColumnData(colData);
        
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        data.widthHint = 230;
        data.heightHint = 100;
        this.ports.setLayoutData(data);
        this.ports.setLayout(tableLayout);
        
        this.viewer = new TableViewer(this.ports);
        this.viewer.setColumnProperties(new String[] { "name", "port" });
        
        initialize();
    }
    
    /**
     * 
     */
    protected void setupPortEditors()
    {
        this.viewer.setCellEditors(new CellEditor[] { null, new TextCellEditor(this.ports) });
        
        ICellModifier cellModifier = new ICellModifier() {
            @Override
            public Object getValue(Object element, String property)
            {
                ServerPort sp = (ServerPort) element;
                if (sp.getPort() < 0)
                    return "-"; //$NON-NLS-1$
                return sp.getPort() + ""; //$NON-NLS-1$
            }
            
            @Override
            public boolean canModify(Object element, String property)
            {
                if ("port".equals(property)) //$NON-NLS-1$
                    return true;
                
                return false;
            }
            
            @Override
            public void modify(Object element, String property, Object value)
            {
                try
                {
                    Item item = (Item) element;
                    ServerPort sp = (ServerPort) item.getData();
                    int port = Integer.parseInt((String) value);
                    execute(new ModifyPortCommand(AbstractPortEditorSection.this.abstractConfiguration, sp.getId(), port));
                }
                catch (Exception ex)
                {
                    // ignore
                }
            }
        };
        this.viewer.setCellModifier(cellModifier);
        
        // preselect second column (Windows-only)
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        if (os != null && os.toLowerCase().indexOf("win") >= 0) //$NON-NLS-1$
        {
            this.ports.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event)
                {
                    try
                    {
                        int n = AbstractPortEditorSection.this.ports.getSelectionIndex();
                        AbstractPortEditorSection.this.viewer.editElement(AbstractPortEditorSection.this.ports.getItem(n).getData(), 1);
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }
                }
            });
        }
    }
    
    @Override
    public void dispose()
    {
        if (this.abstractConfiguration != null)
            this.abstractConfiguration.removePropertyChangeListener(this.listener);
    }
    
    
    /**
     * Returns the config from server.
     * @param srv
     * @return config.
     * @throws CoreException 
     */
    protected abstract AbstractConfiguration<?, ?> getConfig(IServerWorkingCopy srv) throws CoreException;
    
    @Override
    public void init(IEditorSite site, IEditorInput input)
    {
        super.init(site, input);
        
        try
        {
            this.abstractConfiguration = this.getConfig(this.server);
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // ignore
        }
        addChangeListener();
        initialize();
    }
    
    /**
     * Initialize the fields in this editor.
     */
    protected void initialize()
    {
        if (this.ports == null)
            return;
        
        this.ports.removeAll();
        
        for (final ServerPort port : this.abstractConfiguration.getServerPorts())
        {
            TableItem item = new TableItem(this.ports, SWT.NONE);
            String portStr = "-"; //$NON-NLS-1$
            if (port.getPort() >= 0)
                portStr = port.getPort() + ""; //$NON-NLS-1$
            String[] s = new String[] { port.getName(), portStr };
            item.setText(s);
            // item.setImage(TomcatUIPlugin.getImage(TomcatUIPlugin.IMG_PORT));
            item.setData(port);
        }
        
        if (this.readOnly)
        {
            this.viewer.setCellEditors(new CellEditor[] { null, null });
            this.viewer.setCellModifier(null);
        }
        else
        {
            setupPortEditors();
        }
    }
    
}
