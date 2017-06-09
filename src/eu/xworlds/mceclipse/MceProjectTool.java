/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
public class MceProjectTool
{

    /**
     * @param project
     * @param m
     * @throws CoreException
     */
    public static void enableSpigotPluginFacet(IProject project, IProgressMonitor m) throws CoreException
    {
        final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
        final IFacetedProject faceted = ProjectFacetsManager.create(project, true, monitor);
        
        addJavaFacet(monitor, faceted);
        
        final IProjectFacet facet = ProjectFacetsManager.getProjectFacet("spigot.plugin"); //$NON-NLS-1$
        final IProjectFacetVersion pfv = facet.getVersion("1.0"); //$NON-NLS-1$
        faceted.installProjectFacet(pfv, null, monitor);
        
        enableTargetRuntime(monitor, faceted, pfv);
    }

    /**
     * @param project
     * @param m
     * @throws CoreException
     */
    public static void enableBungeePluginFacet(IProject project, IProgressMonitor m) throws CoreException
    {
        final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
        final IFacetedProject faceted = ProjectFacetsManager.create(project, true, monitor);
        
        addJavaFacet(monitor, faceted);
        
        final IProjectFacet facet = ProjectFacetsManager.getProjectFacet("bungee.plugin"); //$NON-NLS-1$
        final IProjectFacetVersion pfv = facet.getVersion("1.0"); //$NON-NLS-1$
        faceted.installProjectFacet(pfv, null, monitor);
        
        enableTargetRuntime(monitor, faceted, pfv);
    }

    /**
     * @param project
     * @param m
     * @throws CoreException
     */
    public static void enableSpigotLibraryFacet(IProject project, IProgressMonitor m) throws CoreException
    {
        final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
        final IFacetedProject faceted = ProjectFacetsManager.create(project, true, monitor);
        
        addJavaFacet(monitor, faceted);
        
        final IProjectFacet facet = ProjectFacetsManager.getProjectFacet("spigot.lib"); //$NON-NLS-1$
        final IProjectFacetVersion pfv = facet.getVersion("1.0"); //$NON-NLS-1$
        faceted.installProjectFacet(pfv, null, monitor);
        
        enableTargetRuntime(monitor, faceted, pfv);
    }

    /**
     * @param project
     * @param m
     * @throws CoreException
     */
    public static void enableBungeeLibraryFacet(IProject project, IProgressMonitor m) throws CoreException
    {
        final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
        final IFacetedProject faceted = ProjectFacetsManager.create(project, true, monitor);
        
        addJavaFacet(monitor, faceted);
        
        final IProjectFacet facet = ProjectFacetsManager.getProjectFacet("bungee.lib"); //$NON-NLS-1$
        final IProjectFacetVersion pfv = facet.getVersion("1.0"); //$NON-NLS-1$
        faceted.installProjectFacet(pfv, null, monitor);
        
        enableTargetRuntime(monitor, faceted, pfv);
    }

    /**
     * @param monitor
     * @param faceted
     * @param pfv 
     * @throws CoreException
     */
    private static void enableTargetRuntime(final IProgressMonitor monitor, final IFacetedProject faceted, IProjectFacetVersion pfv) throws CoreException
    {
        final Set<IRuntime> runtimes = RuntimeManager.getRuntimes(Collections.singleton(pfv));
        // TODO synchronize with new facet (spigot.version / bungee.version)
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
        final IProjectFacet javaFacet = ProjectFacetsManager.getProjectFacet("jst.java"); //$NON-NLS-1$
        final IProjectFacetVersion javaVersion = javaFacet.getVersion("1.8"); //$NON-NLS-1$
        faceted.installProjectFacet(javaVersion, null, monitor);
    }

    /**
     * @param model
     */
    private static void prepareSpigotModel(Model model)
    {
        model.getProperties().setProperty("maven.compiler.source", "1.8"); //$NON-NLS-1$ //$NON-NLS-2$
        model.getProperties().setProperty("maven.compiler.target", "1.8"); //$NON-NLS-1$ //$NON-NLS-2$
        model.getProperties().setProperty("spigot.version", "1.12-R0.1-SNAPSHOT"); //$NON-NLS-1$ //$NON-NLS-2$
        
        final Dependency dep1 = new Dependency();
        dep1.setGroupId("org.spigotmc"); //$NON-NLS-1$
        dep1.setArtifactId("spigot-api"); //$NON-NLS-1$
        dep1.setVersion("${spigot.version}"); //$NON-NLS-1$
        dep1.setScope("provided"); //$NON-NLS-1$
        model.getDependencies().add(dep1);
        
        final Repository rep1 = new Repository();
        rep1.setId("spigot-repo"); //$NON-NLS-1$
        rep1.setUrl("https://hub.spigotmc.org/nexus/content/repositories/snapshots"); //$NON-NLS-1$
        model.getRepositories().add(rep1);
//        final Repository rep2 = new Repository();
//        rep2.setId("mce-repo"); //$NON-NLS-1$
//        rep2.setUrl("http://nexus.xworlds.eu/nexus/content/groups/mce"); //$NON-NLS-1$
//        model.getRepositories().add(rep2);
    }

    /**
     * @param model
     */
    private static void prepareBungeeModel(Model model)
    {
        model.getProperties().setProperty("maven.compiler.source", "1.8"); //$NON-NLS-1$ //$NON-NLS-2$
        model.getProperties().setProperty("maven.compiler.target", "1.8"); //$NON-NLS-1$ //$NON-NLS-2$
        model.getProperties().setProperty("bungee.version", "1.12-SNAPSHOT"); //$NON-NLS-1$ //$NON-NLS-2$
        
        final Dependency dep1 = new Dependency();
        dep1.setGroupId("net.md-5"); //$NON-NLS-1$
        dep1.setArtifactId("bungeecord-api"); //$NON-NLS-1$
        dep1.setVersion("${bungee.version}"); //$NON-NLS-1$
        dep1.setScope("provided"); //$NON-NLS-1$
        model.getDependencies().add(dep1);
        
        final Repository rep1 = new Repository();
        rep1.setId("oss-sonatype"); //$NON-NLS-1$
        rep1.setUrl("https://oss.sonatype.org/content/repositories/snapshots"); //$NON-NLS-1$
        model.getRepositories().add(rep1);
//        final Repository rep2 = new Repository();
//        rep2.setId("mce-repo"); //$NON-NLS-1$
//        rep2.setUrl("http://nexus.xworlds.eu/nexus/content/groups/mce"); //$NON-NLS-1$
//        model.getRepositories().add(rep2);
    }

    /**
     * @param model
     */
    public static void prepareModelForSpigotPlugin(Model model)
    {
        prepareSpigotModel(model);
    }

    /**
     * @param model
     */
    public static void prepareModelForSpigotLibrary(Model model)
    {
        prepareSpigotModel(model);
    }

    /**
     * @param model
     */
    public static void prepareModelForBungeePlugin(Model model)
    {
        prepareBungeeModel(model);
    }

    /**
     * @param model
     */
    public static void prepareModelForBungeeLibrary(Model model)
    {
        prepareBungeeModel(model);
    }

    /**
     * @param project
     * @param model
     * @param monitor
     * @throws CoreException 
     */
    public static void createSpigotPluginFiles(IProject project, Model model, IProgressMonitor monitor) throws CoreException
    {
        final String pkg = model.getGroupId();
        final String mainClassName = model.getArtifactId().substring(0, 1).toUpperCase() + model.getArtifactId().substring(1) + "Plugin"; //$NON-NLS-1$
        final String author = System.getProperty("user.name"); //$NON-NLS-1$
        
        final StringBuilder pluginYmlBuilder = new StringBuilder();
        pluginYmlBuilder.append("name: ").append(project.getName()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        pluginYmlBuilder.append("main: ").append(pkg).append(".").append(mainClassName).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pluginYmlBuilder.append("version: ${project.version}\n"); //$NON-NLS-1$
        pluginYmlBuilder.append("author: [").append(author).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
        pluginYmlBuilder.append("softdepend: []\n"); //$NON-NLS-1$
        pluginYmlBuilder.append("depend: []\n"); //$NON-NLS-1$
        pluginYmlBuilder.append("commands:\n"); //$NON-NLS-1$
        
        final StringBuilder mainClassBuilder = new StringBuilder();
        mainClassBuilder.append("package ").append(pkg).append(";\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
        mainClassBuilder.append("import org.bukkit.plugin.java.JavaPlugin;\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("public class ").append(mainClassName).append(" extends JavaPlugin\n"); //$NON-NLS-1$ //$NON-NLS-2$
        mainClassBuilder.append("{\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("    public ").append(mainClassName).append("()\n"); //$NON-NLS-1$ //$NON-NLS-2$
        mainClassBuilder.append("    {\n"); //$NON-NLS-1$
        mainClassBuilder.append("        // TODO Put in some initialization code.\n"); //$NON-NLS-1$
        mainClassBuilder.append("    }\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("    @Override\n"); //$NON-NLS-1$
        mainClassBuilder.append("    public void onEnable()\n"); //$NON-NLS-1$
        mainClassBuilder.append("    {\n"); //$NON-NLS-1$
        mainClassBuilder.append("        // TODO Put in your activation code.\n"); //$NON-NLS-1$
        mainClassBuilder.append("    }\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("    @Override\n"); //$NON-NLS-1$
        mainClassBuilder.append("    public void onDisable()\n"); //$NON-NLS-1$
        mainClassBuilder.append("    {\n"); //$NON-NLS-1$
        mainClassBuilder.append("        // TODO Put in your deactivation code.\n"); //$NON-NLS-1$
        mainClassBuilder.append("    }\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("}\n\n"); //$NON-NLS-1$
        
        final IFile pluginYml = project.getFile("src/main/resources/plugin.yml"); //$NON-NLS-1$
        final IFile mainClass = project.getFile("src/main/java/" + pkg.replace(".", "/") + "/" + mainClassName + ".java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        try (final ByteArrayInputStream baisPluginYml = new ByteArrayInputStream(pluginYmlBuilder.toString().getBytes()))
        {
            try (final ByteArrayInputStream baisMainClass = new ByteArrayInputStream(mainClassBuilder.toString().getBytes()))
            {
                pluginYml.create(baisPluginYml, true, monitor);
                createFolder((IFolder)mainClass.getParent(), monitor);
                mainClass.create(baisMainClass, true, monitor);
            }
        }
        catch (IOException e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "erros generating files.", e));
        }
    }

    /**
     * @param project
     * @param model
     * @param monitor
     * @throws CoreException 
     */
    public static void createBungeePluginFiles(IProject project, Model model, IProgressMonitor monitor) throws CoreException
    {
        final String pkg = model.getGroupId();
        final String mainClassName = model.getArtifactId().substring(0, 1).toUpperCase() + model.getArtifactId().substring(1) + "Plugin"; //$NON-NLS-1$
        final String author = System.getProperty("user.name"); //$NON-NLS-1$
        
        final StringBuilder pluginYmlBuilder = new StringBuilder();
        pluginYmlBuilder.append("name: ").append(project.getName()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        pluginYmlBuilder.append("main: ").append(pkg).append(".").append(mainClassName).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pluginYmlBuilder.append("version: ${project.version}\n"); //$NON-NLS-1$
        pluginYmlBuilder.append("author: [").append(author).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
        
        final StringBuilder mainClassBuilder = new StringBuilder();
        mainClassBuilder.append("package ").append(pkg).append(";\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
        mainClassBuilder.append("import net.md_5.bungee.api.plugin.Plugin;\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("public class ").append(mainClassName).append(" extends Plugin\n"); //$NON-NLS-1$ //$NON-NLS-2$
        mainClassBuilder.append("{\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("    public ").append(mainClassName).append("()\n"); //$NON-NLS-1$ //$NON-NLS-2$
        mainClassBuilder.append("    {\n"); //$NON-NLS-1$
        mainClassBuilder.append("        // TODO Put in some initialization code.\n"); //$NON-NLS-1$
        mainClassBuilder.append("    }\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("    @Override\n"); //$NON-NLS-1$
        mainClassBuilder.append("    public void onEnable()\n"); //$NON-NLS-1$
        mainClassBuilder.append("    {\n"); //$NON-NLS-1$
        mainClassBuilder.append("        // TODO Put in your activation code.\n"); //$NON-NLS-1$
        mainClassBuilder.append("    }\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("    @Override\n"); //$NON-NLS-1$
        mainClassBuilder.append("    public void onDisable()\n"); //$NON-NLS-1$
        mainClassBuilder.append("    {\n"); //$NON-NLS-1$
        mainClassBuilder.append("        // TODO Put in your deactivation code.\n"); //$NON-NLS-1$
        mainClassBuilder.append("    }\n\n"); //$NON-NLS-1$
        mainClassBuilder.append("}\n\n"); //$NON-NLS-1$
        
        final IFile pluginYml = project.getFile("src/main/resources/plugin.yml"); //$NON-NLS-1$
        final IFile mainClass = project.getFile("src/main/java/" + pkg.replace(".", "/") + "/" + mainClassName + ".java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        try (final ByteArrayInputStream baisPluginYml = new ByteArrayInputStream(pluginYmlBuilder.toString().getBytes()))
        {
            try (final ByteArrayInputStream baisMainClass = new ByteArrayInputStream(mainClassBuilder.toString().getBytes()))
            {
                pluginYml.create(baisPluginYml, true, monitor);
                createFolder((IFolder)mainClass.getParent(), monitor);
                mainClass.create(baisMainClass, true, monitor);
            }
        }
        catch (IOException e)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "erros generating files.", e));
        }
    }
    
    /**
     * Creates a folder and parents if needed
     * @param folder
     * @param monitor
     * @throws CoreException
     */
    private static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException
    {
        if (folder.getParent() instanceof IFolder && !folder.getParent().exists())
        {
            createFolder((IFolder) folder.getParent(), monitor);
        }
        folder.create(true, true, monitor);
    }

    /**
     * @param model
     * @return status
     */
    public static IStatus validateSpigotModel(Model model)
    {
        return validateModel(model);
    }

    /**
     * @param model
     * @return status
     */
    public static IStatus validateBungeeModel(Model model)
    {
        return validateModel(model);
    }

    /**
     * @param model
     * @return status
     */
    private static IStatus validateModel(Model model)
    {
        final String groupId = model.getGroupId();
        if (groupId == null || groupId.length() == 0)
        {
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Group id must not be empty.");
        }
        final String[] splitted = groupId.split("[\\.]"); //$NON-NLS-1$
        if (splitted.length == 0)
        {
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Group id must not be empty.");
        }
        for (String part : splitted)
        {
            if (!checkCharacters(part))
            {
                return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Group id must be valid java package.");
            }
        }
        final String artifactId = model.getArtifactId();
        if (artifactId == null || artifactId.length() == 0)
        {
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Artifact id must not be empty.");
        }
        if (!checkCharacters(artifactId))
        {
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Artifact id must be valid java identifier.");
        }
        
        if (!"jar".equals(model.getPackaging())) //$NON-NLS-1$
        {
            return new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Only jar packaging is allowed.");
        }
        
        return Status.OK_STATUS;
    }

    /**
     * @param part
     * @return success
     */
    private static boolean checkCharacters(String part)
    {
        final CharacterIterator iter = new StringCharacterIterator(part);
        char c = iter.first();
        if (c == CharacterIterator.DONE)
        {
            return false;
        }
        if (!Character.isJavaIdentifierStart(c))
        {
            return false;
        }
        c = iter.next();
        while (c != CharacterIterator.DONE)
        {
            if (!Character.isJavaIdentifierPart(c))
            {
                return false;
            }
            c = iter.next();
        }
        return true;
    }
    
}
