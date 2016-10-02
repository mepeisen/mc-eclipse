/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse;

import java.util.Collections;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;

/**
 * @author mepeisen
 *
 */
public class SpigotProjectTool
{

    public static void enableSpigotPluginFacet(IProject project, IProgressMonitor m) throws CoreException
    {
        final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
        final IFacetedProject faceted = ProjectFacetsManager.create(project, true, monitor);
        
        addJavaFacet(monitor, faceted);
        
        final IProjectFacet facet = ProjectFacetsManager.getProjectFacet("spigot.plugin");
        final IProjectFacetVersion pfv = facet.getVersion("1.0");
        faceted.installProjectFacet(pfv, null, monitor);
        
        enableTargetRuntime(monitor, faceted, pfv);
    }

    public static void enableSpigotLibraryFacet(IProject project, IProgressMonitor m) throws CoreException
    {
        final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
        final IFacetedProject faceted = ProjectFacetsManager.create(project, true, monitor);
        
        addJavaFacet(monitor, faceted);
        
        final IProjectFacet facet = ProjectFacetsManager.getProjectFacet("spigot.lib");
        final IProjectFacetVersion pfv = facet.getVersion("1.0");
        faceted.installProjectFacet(pfv, null, monitor);
        
        enableTargetRuntime(monitor, faceted, pfv);
    }

    /**
     * @param monitor
     * @param faceted
     * @throws CoreException
     */
    private static void enableTargetRuntime(final IProgressMonitor monitor, final IFacetedProject faceted, IProjectFacetVersion pfv) throws CoreException
    {
        final Set<IRuntime> runtimes = RuntimeManager.getRuntimes(Collections.singleton(pfv));
        // TODO synchronize with new facet (spigot.version)
        if (runtimes.size() > 0)
        {
            faceted.addTargetedRuntime(runtimes.iterator().next(), monitor);
        }
    }

    /**
     * @param monitor
     * @param faceted
     * @throws CoreException
     */
    private static void addJavaFacet(final IProgressMonitor monitor, final IFacetedProject faceted) throws CoreException
    {
        final IProjectFacet javaFacet = ProjectFacetsManager.getProjectFacet("jst.java");
        final IProjectFacetVersion javaVersion = javaFacet.getVersion("1.8");
        faceted.installProjectFacet(javaVersion, null, monitor);
    }

    /**
     * @param model
     */
    private static void prepareModel(Model model)
    {
        model.getProperties().setProperty("maven.compiler.source", "1.8");
        model.getProperties().setProperty("maven.compiler.target", "1.8");
        model.getProperties().setProperty("spigot.version", "1.10.2-R0.1-SNAPSHOT");
        
        final Dependency dep1 = new Dependency();
        dep1.setGroupId("org.spigotmc");
        dep1.setArtifactId("spigot");
        dep1.setVersion("${spigot.version}");
        dep1.setScope("provided");
        model.getDependencies().add(dep1);
        
        final Repository rep1 = new Repository();
        rep1.setId("spigot-repo");
        rep1.setUrl("https://hub.spigotmc.org/nexus/content/repositories/snapshots");
        model.getRepositories().add(rep1);
        final Repository rep2 = new Repository();
        rep2.setId("mce-repo");
        rep2.setUrl("http://nexus.xworlds.eu/nexus/content/groups/mce");
        model.getRepositories().add(rep2);
    }

    /**
     * @param model
     */
    public static void prepareModelForSpigotPlugin(Model model)
    {
        prepareModel(model);
    }

    /**
     * @param model
     */
    public static void prepareModelForSpigotLibrary(Model model)
    {
        prepareModel(model);
    }
    
}
