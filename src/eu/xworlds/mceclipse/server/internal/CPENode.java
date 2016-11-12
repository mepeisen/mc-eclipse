/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

/**
 * @author mepeisen
 *
 */
public class CPENode
{
    
    /** the node type. */
    private final CPENodeType nodeType;
    
    /** the module type. */
    private final CPEModuleType moduleType;
    
    /** the eclipse project. */
    private final IProject project;
    
    /** the jar file. */
    private final String jarFile;
    
    /** child nodes. */
    private final List<CPENode> children = new ArrayList<>();
    
    /** classpath folder. */
    private final List<String> cpFolders = new ArrayList<>();
    
    /** provided jar. */
    private boolean isProvided;
    
    /** runtime jar. */
    private boolean isRuntime;

    /**
     * @param nodeType
     * @param moduleType
     * @param project
     * @param jarFile
     */
    public CPENode(CPENodeType nodeType, CPEModuleType moduleType, IProject project, String jarFile)
    {
        this.nodeType = nodeType;
        this.moduleType = moduleType;
        this.project = project;
        this.jarFile = jarFile;
        if (jarFile != null)
        {
            this.cpFolders.add(jarFile);
        }
    }

    /**
     * @return the isProvided
     */
    public boolean isProvided()
    {
        return this.isProvided;
    }

    /**
     * @param isProvided the isProvided to set
     */
    public void setProvided(boolean isProvided)
    {
        this.isProvided = isProvided;
    }

    /**
     * @return the cpFolders
     */
    public List<String> getCpFolders()
    {
        return this.cpFolders;
    }

    /**
     * @return the children
     */
    public List<CPENode> getChildren()
    {
        return this.children;
    }

    /**
     * @return the nodeType
     */
    public CPENodeType getNodeType()
    {
        return this.nodeType;
    }

    /**
     * @return the moduleType
     */
    public CPEModuleType getModuleType()
    {
        return this.moduleType;
    }

    /**
     * @return the project
     */
    public IProject getProject()
    {
        return this.project;
    }

    /**
     * @return the jarFile
     */
    public String getJarFile()
    {
        return this.jarFile;
    }

    /**
     * @return the isRuntime
     */
    public boolean isRuntime()
    {
        return this.isRuntime;
    }

    /**
     * @param isRuntime the isRuntime to set
     */
    public void setRuntime(boolean isRuntime)
    {
        this.isRuntime = isRuntime;
    }
    
}
