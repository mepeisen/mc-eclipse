/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.ui.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.ui.internal.MavenImages;
import org.eclipse.m2e.core.ui.internal.Messages;
import org.eclipse.m2e.core.ui.internal.actions.SelectionUtil;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractCreateMavenProjectJob;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenProjectWizard;
import org.eclipse.m2e.core.ui.internal.wizards.MappingDiscoveryJob;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypePage;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypeParametersPage;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArtifactPage;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardLocationPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;

/**
 * @author mepeisen
 *
 */
@SuppressWarnings("restriction")
public abstract class AbstractNewPluginWizard extends AbstractMavenProjectWizard implements INewWizard
{
    
    /** location page */
    protected MavenProjectWizardLocationPage            locationPage;
    /** archetype page */
    protected MavenProjectWizardArchetypePage           archetypePage;
    /** artifact page */
    protected MavenProjectWizardArtifactPage            artifactPage;
    /** parameters page */
    protected MavenProjectWizardArchetypeParametersPage parametersPage;
    /** simple project button */
    protected Button                                    simpleProject;
    
    /**
     * Constructor
     */
    public AbstractNewPluginWizard()
    {
        this.setWindowTitle(Messages.wizardProjectTitle);
        this.setDefaultPageImageDescriptor(MavenImages.WIZ_NEW_PROJECT);
        this.setNeedsProgressMonitor(true);
    }
    
    @Override
    public void addPages()
    {
        this.locationPage = new MavenProjectWizardLocationPage(this.importConfiguration, Messages.wizardProjectPageProjectTitle, Messages.wizardProjectPageProjectDescription, this.workingSets) {
            @Override
            protected void createAdditionalControls(Composite container)
            {
                AbstractNewPluginWizard.this.simpleProject = new Button(container, 32);
                AbstractNewPluginWizard.this.simpleProject.setText(Messages.wizardProjectPageProjectSimpleProject);
                // TODO does not work (finish not active) AbstractNewPluginWizard.this.simpleProject.setSelection(true);
                AbstractNewPluginWizard.this.simpleProject.setLayoutData(new GridData(4, 128, false, false, 3, 1));
                AbstractNewPluginWizard.this.simpleProject.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        validate();
                    }
                });
                Label label = new Label(container, 0);
                GridData labelData = new GridData(4, 128, false, false, 3, 1);
                labelData.heightHint = 10;
                label.setLayoutData(labelData);
            }
            
            @Override
            public IWizardPage getNextPage()
            {
                return AbstractNewPluginWizard.this.getPage(AbstractNewPluginWizard.this.simpleProject.getSelection() ? "MavenProjectWizardArtifactPage" : "MavenProjectWizardArchetypePage"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        this.locationPage.setLocationPath(SelectionUtil.getSelectedLocation(this.selection));
        this.archetypePage = new MavenProjectWizardArchetypePage(this.importConfiguration);
        this.parametersPage = new MavenProjectWizardArchetypeParametersPage(this.importConfiguration);
        this.artifactPage = new MavenProjectWizardArtifactPage(this.importConfiguration);
        this.addPage(this.locationPage);
        this.addPage(this.archetypePage);
        this.addPage(this.parametersPage);
        this.addPage(this.artifactPage);
    }
    
    @Override
    public void createPageControls(Composite pageContainer)
    {
        super.createPageControls(pageContainer);
        this.simpleProject.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean isSimpleproject = AbstractNewPluginWizard.this.simpleProject.getSelection();
                AbstractNewPluginWizard.this.archetypePage.setUsed(!isSimpleproject);
                AbstractNewPluginWizard.this.parametersPage.setUsed(!isSimpleproject);
                AbstractNewPluginWizard.this.artifactPage.setUsed(isSimpleproject);
                AbstractNewPluginWizard.this.getContainer().updateButtons();
            }
        });
        this.archetypePage.addArchetypeSelectionListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent selectionchangedevent)
            {
                AbstractNewPluginWizard.this.parametersPage.setArchetype(AbstractNewPluginWizard.this.archetypePage.getArchetype());
                AbstractNewPluginWizard.this.getContainer().updateButtons();
            }
        });
    }
    
    /**
     * @return maven model
     */
    public Model getModel()
    {
        return this.simpleProject.getSelection() ? this.artifactPage.getModel() : this.parametersPage.getModel();
    }
    
    @Override
    public boolean performFinish()
    {
        final Model model = this.getModel();
        final String projectName = this.importConfiguration.getProjectName(model);
        IStatus nameStatus = this.importConfiguration.validateProjectName(model);
        if (!nameStatus.isOK())
        {
            MessageDialog.openError(this.getShell(), NLS.bind(Messages.wizardProjectJobFailed, projectName), nameStatus.getMessage());
            return false;
        }
        
        nameStatus = this.validateModel(model);
        if (!nameStatus.isOK())
        {
            MessageDialog.openError(this.getShell(), NLS.bind(Messages.wizardProjectJobFailed, projectName), nameStatus.getMessage());
            return false;
        }
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IPath location = this.locationPage.isInWorkspace() ? null : this.locationPage.getLocationPath();
        IWorkspaceRoot root = workspace.getRoot();
        final IProject project = this.importConfiguration.getProject(root, model);
        boolean pomExists = (this.locationPage.isInWorkspace() ? root.getLocation().append(project.getName()) : location).append("pom.xml").toFile().exists();
        if (pomExists)
        {
            MessageDialog.openError(this.getShell(), NLS.bind(Messages.wizardProjectJobFailed, projectName), Messages.wizardProjectErrorPomAlreadyExists);
            return false;
        }
        
        final AbstractCreateMavenProjectJob job;
        if (this.simpleProject.getSelection())
        {
            final String[] archetype = this.artifactPage.getFolders();
            job = new AbstractCreateMavenProjectJob(NLS.bind(Messages.wizardProjectJobCreatingProject, projectName), this.workingSets) {
                @Override
                protected List<IProject> doCreateMavenProjects(IProgressMonitor monitor) throws CoreException
                {
                    prepareSimpleModel(model);
                    MavenPlugin.getProjectConfigurationManager().createSimpleProject(project, location, model, archetype, AbstractNewPluginWizard.this.importConfiguration, monitor);
                    prepareSimpleProject(project, model, monitor);
                    return Arrays.asList(new IProject[] { project });
                }
            };
        }
        else
        {
            final Archetype archetype1 = this.archetypePage.getArchetype();
            final String groupId = model.getGroupId();
            final String artifactId = model.getArtifactId();
            final String version = model.getVersion();
            final String javaPackage = this.parametersPage.getJavaPackage();
            final Properties properties = this.parametersPage.getProperties();
            job = new AbstractCreateMavenProjectJob(NLS.bind(Messages.wizardProjectJobCreating, archetype1.getArtifactId()), this.workingSets) {
                @Override
                protected List<IProject> doCreateMavenProjects(IProgressMonitor monitor) throws CoreException
                {
                    List<IProject> projects = MavenPlugin.getProjectConfigurationManager().createArchetypeProjects(location, archetype1, groupId, artifactId, version, javaPackage, properties,
                            AbstractNewPluginWizard.this.importConfiguration, monitor);
                    for (final IProject prj : projects)
                    {
                        // TODO Enable on all modules?
                        prepareArchetypeProject(monitor, prj);
                    }
                    return projects;
                }
            };
        }
        
        job.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent event)
            {
                final IStatus result = event.getResult();
                if (!result.isOK())
                {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run()
                        {
                            MessageDialog.openError(AbstractNewPluginWizard.this.getShell(), NLS.bind(Messages.wizardProjectJobFailed, projectName), result.getMessage());
                        }
                    });
                }
                
                MappingDiscoveryJob discoveryJob = new MappingDiscoveryJob(job.getCreatedProjects());
                discoveryJob.schedule();
            }
        });
        job.setRule(MavenPlugin.getProjectConfigurationManager().getRule());
        job.schedule();
        return true;
    }
    
    /**
     * @param model
     * @return status of validation.
     */
    protected abstract IStatus validateModel(final Model model);
    
    /**
     * @param model
     */
    protected abstract void prepareSimpleModel(final Model model);
    
    /**
     * @param project
     * @param model
     * @param monitor
     * @throws CoreException
     */
    protected abstract void prepareSimpleProject(final IProject project, Model model, IProgressMonitor monitor) throws CoreException;
    
    /**
     * @param monitor
     * @param prj
     * @throws CoreException
     */
    protected abstract void prepareArchetypeProject(IProgressMonitor monitor, final IProject prj) throws CoreException;
    
}
