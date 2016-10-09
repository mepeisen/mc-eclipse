/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.SocketUtil;

import eu.xworlds.mceclipse.McEclipsePlugin;
import eu.xworlds.mceclipse.server.IMinecraftConfiguration;
import eu.xworlds.mceclipse.server.IMinecraftLibrary;
import eu.xworlds.mceclipse.server.IMinecraftPlugin;
import eu.xworlds.mceclipse.server.IMinecraftRuntime;
import eu.xworlds.mceclipse.server.IMinecraftServer;

/**
 * @author mepeisen
 */
public abstract class AbstractServerBehaviour extends ServerBehaviourDelegate
{
    /** the ping thread. */
    protected transient AbstractPingThread     ping = null;
    
    /** the debug event listener. */
    protected transient IDebugEventSetListener processListener;
    
    /**
     * Creates a ping thread for given server.
     * 
     * @param server
     * @return ping thread.
     */
    protected abstract AbstractPingThread createPingThread(IServer server);
    
    @Override
    public void initialize(IProgressMonitor monitor)
    {
        // do nothing
    }

    @Override
    public IPath getTempDirectory()
    {
        return super.getTempDirectory();
    }
    
    /**
     * Returns the minecraft runtime.
     * 
     * @return minecraft runtime.
     */
    public abstract IMinecraftRuntime getMinecraftRuntime();
    
    /**
     * Returns the server config.
     * 
     * @return server config.
     * @throws CoreException
     */
    public abstract IMinecraftConfiguration<? extends IMinecraftPlugin, ? extends IMinecraftLibrary> getMinecraftConfiguration() throws CoreException;
    
    /**
     * Returns the minecraft server.
     * 
     * @return minecraft server.
     */
    public abstract IMinecraftServer getMinecraftServer();
    
    /**
     * Return the runtime class name.
     *
     * @return the class name
     */
    public abstract String getRuntimeClass();
    
    /**
     * Returns the runtime base path for relative paths in the server configuration.
     * 
     * @return the base path
     */
    public IPath getRuntimeBaseDirectory()
    {
        return getMinecraftServer().getRuntimeBaseDirectory();
    }
    
    /**
     * Return the program's runtime arguments to start or stop.
     *
     * @param starting
     *            true if starting
     * @return an array of runtime program arguments
     */
    protected abstract String[] getRuntimeProgramArguments(boolean starting);
    
    /**
     * Returns the expluded program's runtime arguments.
     * 
     * @param starting
     * @return an array of runtime program arguments
     */
    protected abstract String[] getExcludedRuntimeProgramArguments(boolean starting);
    
    /**
     * Return the runtime (VM) arguments.
     *
     * @return an array of runtime arguments
     */
    protected String[] getRuntimeVMArguments()
    {
        IPath installPath = getServer().getRuntime().getLocation();
        // If installPath is relative, convert to canonical path and hope for the best
        if (!installPath.isAbsolute())
        {
            try
            {
                String installLoc = (new File(installPath.toOSString())).getCanonicalPath();
                installPath = new Path(installLoc);
            }
            catch (@SuppressWarnings("unused") IOException e)
            {
                // Ignore if there is a problem
            }
        }
        IPath configPath = getRuntimeBaseDirectory();
        IPath deployPath = getRuntimeBaseDirectory().append(this.getServerDeployDirectory());
        return this.getRuntimeVMArguments(installPath, configPath, deployPath);
    }
    
    /**
     * Gets the startup VM arguments for the Spigot server.
     * 
     * @param installPath
     *            installation path for the server
     * @param configPath
     *            configuration path for the server
     * @param deployPath
     *            deploy path for the server
     * @return array of VM arguments for starting the server
     */
    protected abstract String[] getRuntimeVMArguments(IPath installPath, IPath configPath, IPath deployPath);
    
    /**
     * Adds process listener.
     * @param newProcess
     */
    protected void addProcessListener(final IProcess newProcess)
    {
        if (this.processListener != null || newProcess == null)
            return;
        
        this.processListener = new IDebugEventSetListener() {
            @Override
            public void handleDebugEvents(DebugEvent[] events)
            {
                if (events != null)
                {
                    int size = events.length;
                    for (int i = 0; i < size; i++)
                    {
                        if (newProcess.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE)
                        {
                            stopImpl();
                        }
                    }
                }
            }
        };
        DebugPlugin.getDefault().addDebugEventListener(this.processListener);
    }
    
    /**
     * Sets the server started.
     */
    protected void setServerStarted()
    {
        setServerState(IServer.STATE_STARTED);
    }
    
    /**
     * Stops the server
     */
    protected void stopImpl()
    {
        if (this.ping != null)
        {
            this.ping.stop();
            this.ping = null;
        }
        if (this.processListener != null)
        {
            DebugPlugin.getDefault().removeDebugEventListener(this.processListener);
            this.processListener = null;
        }
        setServerState(IServer.STATE_STOPPED);
    }
    
    @Override
    protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException
    {
        if (getServer().getRuntime() == null)
            return;
        
        IPath installDir = getServer().getRuntime().getLocation();
        IPath confDir = getRuntimeBaseDirectory();
        IStatus status = prepareRuntimeDirectory(confDir);
        if (status != null && !status.isOK())
            throw new CoreException(status);
        status = this.prepareDeployDirectory(getServerDeployDirectory());
        if (status != null && !status.isOK())
            throw new CoreException(status);
            
        // monitor = ProgressUtil.getMonitorFor(monitor);
        // monitor.beginTask("publish spigot server", 600);
        
        // status = getSpigotConfiguration().cleanupServer(confDir, installDir, !getSpigotServer().isSaveSeparateContextFiles(), ProgressUtil.getSubMonitorFor(monitor, 100));
        // if (status != null && !status.isOK())
        // throw new CoreException(status);
        
        // status = getSpigotConfiguration().backupAndPublish(confDir, !getSpigotServer().isTestEnvironment(), ProgressUtil.getSubMonitorFor(monitor, 400));
        // if (status != null && !status.isOK())
        // throw new CoreException(status);
        
        // status = getSpigotConfiguration().localizeConfiguration(confDir, getServerDeployDirectory(), getSpigotServer(), ProgressUtil.getSubMonitorFor(monitor, 100));
        // if (status != null && !status.isOK())
        // throw new CoreException(status);
        
        // monitor.done();
        
        setServerPublishState(IServer.PUBLISH_STATE_NONE);
    }
    /**
     * Prepares the runtime directory.
     * @param confDir
     * @return status result
     */
    protected abstract IStatus prepareRuntimeDirectory(IPath confDir);
    
    /**
     * prepares the deploy directory.
     * @param deployDir
     * @return status result.
     */
    protected abstract IStatus prepareDeployDirectory(IPath deployDir);
    
    @Override
    protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor) throws CoreException
    {
        if (getServer().getServerState() != IServer.STATE_STOPPED)
        {
            if (deltaKind == ServerBehaviourDelegate.ADDED || deltaKind == ServerBehaviourDelegate.REMOVED)
                setServerRestartState(true);
        }
    }
    
    @Override
    protected void publishFinish(IProgressMonitor monitor) throws CoreException
    {
        // do nothing
    }
    
    /**
     * Setup for starting the server.
     * 
     * @param launch
     *            ILaunch
     * @param launchMode
     *            String
     * @param monitor
     *            IProgressMonitor
     * @throws CoreException
     *             if anything goes wrong
     */
    public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException
    {
        // if (getSpigotRuntime() == null)
        // throw new CoreException();
        
        IStatus status = getMinecraftRuntime().validate();
        if (status != null && status.getSeverity() == IStatus.ERROR)
            throw new CoreException(status);
        
        // setRestartNeeded(false);
        IMinecraftConfiguration<? extends IMinecraftPlugin, ? extends IMinecraftLibrary> configuration = getMinecraftConfiguration();
        
        // check that ports are free
        final ServerPort[] ports = configuration.getServerPorts();
        for (final ServerPort sp : ports)
        {
            if (sp.getPort() < 0)
                throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "invalid port")); //$NON-NLS-1$
            if (SocketUtil.isPortInUse(sp.getPort(), 5))
            {
                throw new CoreException(new Status(IStatus.ERROR, McEclipsePlugin.PLUGIN_ID, "port in use")); //$NON-NLS-1$
            }
        }
        
        setServerRestartState(false);
        setServerState(IServer.STATE_STARTING);
        setMode(launchMode);
        
        // ping server to check for startup
        try
        {
            this.ping = this.createPingThread(getServer());
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // Trace.trace(Trace.SEVERE, "Can't ping for Spigot startup.");
        }
    }
    
    /**
     * Returns the stop command for console.
     * @return stop command.
     */
    protected abstract String getStopCommand();
    
    @Override
    public void stop(boolean force)
    {
        if (force)
        {
            terminate();
            return;
        }
        int state = getServer().getServerState();
        // If stopped or stopping, no need to run stop command again
        if (state == IServer.STATE_STOPPED || state == IServer.STATE_STOPPING)
            return;
        else if (state == IServer.STATE_STARTING)
        {
            terminate();
            return;
        }
        
        try
        {
            // if (Trace.isTraceEnabled())
            // Trace.trace(Trace.FINER, "Stopping Spigot");
            if (state != IServer.STATE_STOPPED)
                setServerState(IServer.STATE_STOPPING);
            
            for (final IProcess process : getServer().getLaunch().getProcesses())
            {
                process.getStreamsProxy().write(this.getStopCommand() + "\n"); //$NON-NLS-1$
            }
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // Trace.trace(Trace.SEVERE, "Error stopping Spigot", e);
        }
    }
    
    /**
     * Terminates the server.
     */
    protected void terminate()
    {
        if (getServer().getServerState() == IServer.STATE_STOPPED)
            return;
        
        try
        {
            setServerState(IServer.STATE_STOPPING);
            // if (Trace.isTraceEnabled())
            // Trace.trace(Trace.FINER, "Killing the Spigot process");
            ILaunch launch = getServer().getLaunch();
            if (launch != null)
            {
                launch.terminate();
                stopImpl();
            }
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // Trace.trace(Trace.SEVERE, "Error killing the process", e);
        }
    }
    
    /**
     * @param s
     * @param start
     * @return next token
     */
    protected static int getNextToken(String s, int start)
    {
        int i = start;
        int length = s.length();
        char lookFor = ' ';
        
        while (i < length)
        {
            char c = s.charAt(i);
            if (lookFor == c)
            {
                if (lookFor == '"')
                    return i + 1;
                return i;
            }
            if (c == '"')
                lookFor = '"';
            i++;
        }
        return -1;
    }
    
    /**
     * Merge the given arguments into the original argument string, replacing invalid values if they have been changed. Special handling is provided if the keepActionLast argument is true and the last
     * vmArg is a simple string. The vmArgs will be merged such that the last vmArg is guaranteed to be the last argument in the merged string.
     * 
     * @param origArg
     *            String of original arguments.
     * @param vmArgs
     *            Arguments to merge into the original arguments string
     * @param excludeArgs
     *            Arguments to exclude from the original arguments string
     * @param keepActionLast
     *            If <b>true</b> the vmArguments are assumed to be Spigot program arguments, the last of which is the action to perform which must remain the last argument. This only has an impact if
     *            the last vmArg is a simple string argument, like &quot;start&quot;.
     * @return merged argument string
     */
    public static String mergeArguments(String origArg, String[] vmArgs, String[] excludeArgs, boolean keepActionLast)
    {
        String originalArg = origArg == null ? "" : origArg; //$NON-NLS-1$
        if (vmArgs == null)
            return originalArg;
        
        // replace and null out all vmargs that already exist
        int size = vmArgs.length;
        for (int i = 0; i < size; i++)
        {
            int ind = vmArgs[i].indexOf(" "); //$NON-NLS-1$
            int ind2 = vmArgs[i].indexOf("="); //$NON-NLS-1$
            if (ind >= 0 && (ind2 == -1 || ind < ind2))
            { // -a bc style
                int index = originalArg.indexOf(vmArgs[i].substring(0, ind + 1));
                if (index == 0 || (index > 0 && Character.isWhitespace(originalArg.charAt(index - 1))))
                {
                    // replace
                    String s = originalArg.substring(0, index);
                    int index2 = getNextToken(originalArg, index + ind + 1);
                    if (index2 >= 0)
                        originalArg = s + vmArgs[i] + originalArg.substring(index2);
                    else
                        originalArg = s + vmArgs[i];
                    vmArgs[i] = null;
                }
            }
            else if (ind2 >= 0)
            { // a=b style
                int index = originalArg.indexOf(vmArgs[i].substring(0, ind2 + 1));
                if (index == 0 || (index > 0 && Character.isWhitespace(originalArg.charAt(index - 1))))
                {
                    // replace
                    String s = originalArg.substring(0, index);
                    int index2 = getNextToken(originalArg, index);
                    if (index2 >= 0)
                        originalArg = s + vmArgs[i] + originalArg.substring(index2);
                    else
                        originalArg = s + vmArgs[i];
                    vmArgs[i] = null;
                }
            }
            else
            { // abc style
                int index = originalArg.indexOf(vmArgs[i]);
                if (index == 0 || (index > 0 && Character.isWhitespace(originalArg.charAt(index - 1))))
                {
                    // replace
                    String s = originalArg.substring(0, index);
                    int index2 = getNextToken(originalArg, index);
                    if (!keepActionLast || i < (size - 1))
                    {
                        if (index2 >= 0)
                            originalArg = s + vmArgs[i] + originalArg.substring(index2);
                        else
                            originalArg = s + vmArgs[i];
                        vmArgs[i] = null;
                    }
                    else
                    {
                        // The last VM argument needs to remain last,
                        // remove original arg and append the vmArg later
                        if (index2 >= 0)
                            originalArg = s + originalArg.substring(index2);
                        else
                            originalArg = s;
                    }
                }
            }
        }
        
        // remove excluded arguments
        if (excludeArgs != null && excludeArgs.length > 0)
        {
            for (int i = 0; i < excludeArgs.length; i++)
            {
                int ind = excludeArgs[i].indexOf(" "); //$NON-NLS-1$
                int ind2 = excludeArgs[i].indexOf("="); //$NON-NLS-1$
                if (ind >= 0 && (ind2 == -1 || ind < ind2))
                { // -a bc style
                    int index = originalArg.indexOf(excludeArgs[i].substring(0, ind + 1));
                    if (index == 0 || (index > 0 && Character.isWhitespace(originalArg.charAt(index - 1))))
                    {
                        // remove
                        String s = originalArg.substring(0, index);
                        int index2 = getNextToken(originalArg, index + ind + 1);
                        if (index2 >= 0)
                        {
                            // If remainder will become the first argument, remove leading blanks
                            while (index2 < originalArg.length() && Character.isWhitespace(originalArg.charAt(index2)))
                                index2 += 1;
                            originalArg = s + originalArg.substring(index2);
                        }
                        else
                            originalArg = s;
                    }
                }
                else if (ind2 >= 0)
                { // a=b style
                    int index = originalArg.indexOf(excludeArgs[i].substring(0, ind2 + 1));
                    if (index == 0 || (index > 0 && Character.isWhitespace(originalArg.charAt(index - 1))))
                    {
                        // remove
                        String s = originalArg.substring(0, index);
                        int index2 = getNextToken(originalArg, index);
                        if (index2 >= 0)
                        {
                            // If remainder will become the first argument, remove leading blanks
                            while (index2 < originalArg.length() && Character.isWhitespace(originalArg.charAt(index2)))
                                index2 += 1;
                            originalArg = s + originalArg.substring(index2);
                        }
                        else
                            originalArg = s;
                    }
                }
                else
                { // abc style
                    int index = originalArg.indexOf(excludeArgs[i]);
                    if (index == 0 || (index > 0 && Character.isWhitespace(originalArg.charAt(index - 1))))
                    {
                        // remove
                        String s = originalArg.substring(0, index);
                        int index2 = getNextToken(originalArg, index);
                        if (index2 >= 0)
                        {
                            // Remove leading blanks
                            while (index2 < originalArg.length() && Character.isWhitespace(originalArg.charAt(index2)))
                                index2 += 1;
                            originalArg = s + originalArg.substring(index2);
                        }
                        else
                            originalArg = s;
                    }
                }
            }
        }
        
        // add remaining vmargs to the end
        for (int i = 0; i < size; i++)
        {
            if (vmArgs[i] != null)
            {
                if (originalArg.length() > 0 && !originalArg.endsWith(" ")) //$NON-NLS-1$
                    originalArg += " "; //$NON-NLS-1$
                originalArg += vmArgs[i];
            }
        }
        
        return originalArg;
    }
    
    /**
     * Replace the current JRE container classpath with the given entry.
     * 
     * @param cp
     * @param entry
     */
    public static void replaceJREContainer(List<IRuntimeClasspathEntry> cp, IRuntimeClasspathEntry entry)
    {
        int size = cp.size();
        for (int i = 0; i < size; i++)
        {
            IRuntimeClasspathEntry entry2 = cp.get(i);
            if (entry2.getPath().uptoSegment(2).isPrefixOf(entry.getPath()))
            {
                cp.set(i, entry);
                return;
            }
        }
        
        cp.add(0, entry);
    }
    
    /**
     * Merge a single classpath entry into the classpath list.
     * 
     * @param cp
     * @param entry
     */
    public static void mergeClasspath(List<IRuntimeClasspathEntry> cp, IRuntimeClasspathEntry entry)
    {
        Iterator<IRuntimeClasspathEntry> iterator = cp.iterator();
        while (iterator.hasNext())
        {
            IRuntimeClasspathEntry entry2 = iterator.next();
            
            if (entry2.getPath().equals(entry.getPath()))
                return;
        }
        
        cp.add(entry);
    }
    
    @Override
    public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException
    {
        String existingProgArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                mergeArguments(existingProgArgs, getRuntimeProgramArguments(true), getExcludedRuntimeProgramArguments(true), true));
        
        String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
//        String[] parsedVMArgs = null;
//        if (null != existingVMArgs)
//        {
//            parsedVMArgs = DebugPlugin.parseArguments(existingVMArgs);
//        }
        String[] configVMArgs = getRuntimeVMArguments();
        
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, mergeArguments(existingVMArgs, configVMArgs, null, false));
        
        IMinecraftRuntime runtime = getMinecraftRuntime();
        IVMInstall vmInstall = runtime.getVMInstall();
        if (vmInstall != null)
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
        
        // update classpath
        IRuntimeClasspathEntry[] originalClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(workingCopy);
        int size = originalClasspath.length;
        List<IRuntimeClasspathEntry> oldCp = new ArrayList<>(originalClasspath.length + 2);
        for (int i = 0; i < size; i++)
            oldCp.add(originalClasspath[i]);
        
        List<IRuntimeClasspathEntry> cp2 = runtime.getRuntimeClasspath();
        Iterator<IRuntimeClasspathEntry> iterator = cp2.iterator();
        while (iterator.hasNext())
        {
            IRuntimeClasspathEntry entry = iterator.next();
            mergeClasspath(oldCp, entry);
        }
        
        if (vmInstall != null)
        {
            try
            {
                String typeId = vmInstall.getVMInstallType().getId();
                replaceJREContainer(oldCp,
                        JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER).append(typeId).append(vmInstall.getName()), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
            }
            catch (@SuppressWarnings("unused") Exception e)
            {
                // ignore
            }
        }
        
        for (final IMinecraftLibrary lib : getMinecraftConfiguration().getLibraries())
        {
            mergeClasspath(oldCp, JavaRuntime.newProjectRuntimeClasspathEntry(JavaCore.create(lib.getProject())));
        }
        
        iterator = oldCp.iterator();
        List<String> list = new ArrayList<>();
        while (iterator.hasNext())
        {
            IRuntimeClasspathEntry entry = iterator.next();
            try
            {
                list.add(entry.getMemento());
            }
            catch (@SuppressWarnings("unused") Exception e)
            {
                // Trace.trace(Trace.SEVERE, "Could not resolve classpath entry: " + entry, e);
            }
        }
        
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, list);
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    }
    
    /**
     * Gets the directory to which modules should be deployed for this server.
     * 
     * @return full path to deployment directory for the server
     */
    public IPath getServerDeployDirectory()
    {
        final String instanceDir = getMinecraftServer().getInstanceDirectory();
        if (instanceDir == null)
        {
            return getServer().getServerConfiguration().getLocation().append(getMinecraftServer().getDeployDirectory());
        }
        return new Path(instanceDir).append(getMinecraftServer().getDeployDirectory());
    }
    
}
