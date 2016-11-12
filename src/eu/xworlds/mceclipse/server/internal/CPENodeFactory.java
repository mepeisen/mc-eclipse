/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.internal;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.jdt.IClasspathManager;
import org.eclipse.m2e.jdt.MavenJdtPlugin;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;

import eu.xworlds.mceclipse.McEclipsePlugin;

/**
 * @author mepeisen
 *
 */
public class CPENodeFactory
{
    
    /**
     * Creates a class path node for given project.
     * @param project
     * @return class path node.
     * @throws CoreException 
     */
    public static CPENode create(IProject project) throws CoreException
    {
        final Set<IProject> knownProjects = new HashSet<>();
        final Set<String> knownJars = new HashSet<>();
        try
        {
            return createCPENodeForProject(project, knownProjects, knownJars);
        }
        catch (IOException ex)
        {
            throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "problems calculating cpe nodes", ex));
        }
    }

    /**
     * @param project
     * @param knownProjects
     * @param knownJars 
     * @return node
     * @throws CoreException
     * @throws IOException
     */
    private static CPENode createCPENodeForProject(IProject project, final Set<IProject> knownProjects, Set<String> knownJars) throws CoreException, IOException
    {
        if (knownProjects.add(project))
        {
            final IJavaProject javaProject = JavaCore.create(project);
            if (javaProject != null)
            {
                CPEModuleType type = CPEModuleType.Library;
                if (FacetedProjectFramework.isFacetedProject(javaProject.getProject()))
                {
                    // check for library or plugin
                    if (FacetedProjectFramework.hasProjectFacet(javaProject.getProject(), "spigot.plugin")) //$NON-NLS-1$
                    {
                        type = CPEModuleType.SpigotPlugin;
                    }
                    else if (FacetedProjectFramework.hasProjectFacet(javaProject.getProject(), "spigot.library")) //$NON-NLS-1$
                    {
                        type = CPEModuleType.SpigotLibrary;
                    }
                    else if (FacetedProjectFramework.hasProjectFacet(javaProject.getProject(), "bungee.plugin")) //$NON-NLS-1$
                    {
                        type = CPEModuleType.BungeePlugin;
                    }
                    else if (FacetedProjectFramework.hasProjectFacet(javaProject.getProject(), "bungee.library")) //$NON-NLS-1$
                    {
                        type = CPEModuleType.SpigotLibrary;
                    }
                    else if (hasPluginYml(javaProject))
                    {
                        type = CPEModuleType.UnknownPlugin;
                    }
                }
                else if (hasPluginYml(javaProject))
                {
                    type = CPEModuleType.UnknownPlugin;
                }
                final CPENode result = new CPENode(CPENodeType.JavaProject, type, project, null);
                result.getCpFolders().add(ResourcesPlugin.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation()).getLocation().toOSString());
                
                // dependencies
                final IMavenProjectFacade mp = MavenPlugin.getMavenProjectRegistry().getProject(javaProject.getProject());
                if (mp == null)
                {
                    // classic java project
                    for (final IClasspathEntry cpe : javaProject.getRawClasspath())
                    {
                        if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
                        {
                            final String lib = cpe.getPath().toFile().getAbsolutePath();
                            // Check for plugin.yml, create library entry
                            if (!knownJars.contains(lib))
                            {
                                knownJars.add(lib);
                                switch (detectType(lib))
                                {
                                    case BukkitJar:
                                        result.getChildren().add(new CPENode(CPENodeType.JarFile, CPEModuleType.BukkitJar, null, lib));
                                        break;
                                    case BungeeJar:
                                        result.getChildren().add(new CPENode(CPENodeType.JarFile, CPEModuleType.BungeeJar, null, lib));
                                        break;
                                    case SpigotJar:
                                        result.getChildren().add(new CPENode(CPENodeType.JarFile, CPEModuleType.SpigotJar, null, lib));
                                        break;
                                    case Plugin:
                                        result.getChildren().add(new CPENode(CPENodeType.JarFile, CPEModuleType.UnknownPlugin, null, lib));
                                        break;
                                    default:
                                    case Jar:
                                        result.getChildren().add(new CPENode(CPENodeType.JarFile, CPEModuleType.Library, null, lib));
                                        break;
                                }
                            }
                        }
                        else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                        {
                            final IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
                            final CPENode child = createCPENodeForProject(prj, knownProjects, knownJars);
                            if (child != null)
                            {
                                result.getChildren().add(child);
                            }
                        }
                    }
                }
                else
                {
                    // maven project; get classpath
                    try
                    {
                        final IClasspathManager cpm = MavenJdtPlugin.getDefault().getBuildpathManager();
                        final IClasspathEntry[] entries = cpm.getClasspath(javaProject.getProject(), 1, true, new NullProgressMonitor()); // 1 == runtime || provided || system
                        for (final IClasspathEntry cpe : entries)
                        {
                            // check for provided entry
                            boolean isProvided = false;
                            boolean isRuntime = false;
                            for (final IClasspathAttribute attribute : cpe.getExtraAttributes())
                            {
                                if ("maven.scope".equals(attribute.getName())) //$NON-NLS-1$
                                {
                                    isProvided = "provided".equals(attribute.getValue()); //$NON-NLS-1$
                                    isRuntime = "runtime".equals(attribute.getValue()); //$NON-NLS-1$
                                    break;
                                }
                            }
                            
                            if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                            {
                                final IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
                                final CPENode child = createCPENodeForProject(prj, knownProjects, knownJars);
                                if (child != null)
                                {
                                    child.setProvided(isProvided);
                                    child.setRuntime(isRuntime);
                                    result.getChildren().add(child);
                                }
                            }
                            else if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
                            {
                                final String lib = cpe.getPath().toFile().getAbsolutePath();
                                // Check for plugin.yml, create library entry
                                if (!knownJars.contains(lib))
                                {
                                    knownJars.add(lib);
                                    CPENode child = null;
                                    switch (detectType(lib))
                                    {
                                        case BukkitJar:
                                            child = new CPENode(CPENodeType.JarFile, CPEModuleType.BukkitJar, null, lib);
                                            break;
                                        case BungeeJar:
                                            child = new CPENode(CPENodeType.JarFile, CPEModuleType.BungeeJar, null, lib);
                                            break;
                                        case SpigotJar:
                                            child = new CPENode(CPENodeType.JarFile, CPEModuleType.SpigotJar, null, lib);
                                            break;
                                        case Plugin:
                                            child = new CPENode(CPENodeType.JarFile, CPEModuleType.UnknownPlugin, null, lib);
                                            break;
                                        default:
                                        case Jar:
                                            child = new CPENode(CPENodeType.JarFile, CPEModuleType.Library, null, lib);
                                            break;
                                    }
                                    child.setProvided(isProvided);
                                    child.setRuntime(isRuntime);
                                    result.getChildren().add(child);
                                }
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "Error fetching maven classpath", ex));
                    }
                }
                
                return result;
            }
        }
        return null;
    }
    
    /**
     * @param javaProject
     * @return true if plugin.yml was found
     * @throws JavaModelException 
     */
    private static boolean hasPluginYml(IJavaProject javaProject) throws JavaModelException
    {
        return ResourcesPlugin.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation()).getFile("plugin.yml").exists(); //$NON-NLS-1$
    }

    /** library type. */
    private enum LibType
    {
        /** jar file */
        Jar,
        /** plugin of unknown type. */
        Plugin,
        /** spigot jar. */
        SpigotJar,
        /** bukkit jar. */
        BukkitJar,
        /** bungee jar. */
        BungeeJar
    }

    /**
     * Checks main spigot/bungee jar files and other plugins
     * @param lib
     * @return type of the library
     * @throws IOException 
     */
    private static LibType detectType(String lib) throws IOException
    {
        try (final JarFile jar = new JarFile(lib))
        {
            // check for plugin.yml
            ZipEntry entry = jar.getEntry("plugin.yml"); //$NON-NLS-1$
            if (entry != null && !entry.isDirectory())
            {
                return LibType.Plugin;
            }
            // check for spigot.jar
            entry = jar.getEntry("META-INF/maven/org.spigotmc/spigot/pom.properties"); //$NON-NLS-1$
            if (entry != null && !entry.isDirectory())
            {
                return LibType.SpigotJar;
            }
            // check for bungee.jar
            entry = jar.getEntry("META-INF/maven/net.md-5/bungeecord-proxy/pom.properties"); //$NON-NLS-1$
            if (entry != null && !entry.isDirectory())
            {
                return LibType.BungeeJar;
            }
            // check for bukkit.jar
            entry = jar.getEntry("org/bukkit/"); //$NON-NLS-1$
            if (entry != null && entry.isDirectory())
            {
                return LibType.BukkitJar;
            }
        }
        // normal jar file/ library
        return LibType.Jar;
    }
    
}
