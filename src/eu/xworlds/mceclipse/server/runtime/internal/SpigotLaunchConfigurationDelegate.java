/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */
package eu.xworlds.mceclipse.server.runtime.internal;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author mepeisen
 *
 */
public class SpigotLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate
{
    
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
    {
        IServer server = ServerUtil.getServer(configuration);
        if (server == null)
        {
            // Trace.trace(Trace.FINEST, "Launch configuration could not find server");
            return;
        }
        
        if (server.shouldPublish() && ServerCore.isAutoPublishing())
        {
            server.publish(IServer.PUBLISH_INCREMENTAL, monitor);
        }
        
        final SpigotServerBehaviour spigotServer = (SpigotServerBehaviour) server.loadAdapter(SpigotServerBehaviour.class, null);
        final String mainTypeName = spigotServer.getRuntimeClass();
        final IVMInstall vm = verifyVMInstall(configuration);
        IVMRunner runner = vm.getVMRunner(mode);
        if (runner == null)
        {
            runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
        }
        
        final File workingDir = server.getServerConfiguration().getLocation().toFile();
        String workingDirName = null;
        if (workingDir != null)
        {
            workingDirName = workingDir.getAbsolutePath();
        }
        
        // Program & VM args
        final String pgmArgs = getProgramArguments(configuration);
        final String vmArgs = getVMArguments(configuration);
        final String[] envp = getEnvironment(configuration);
        
        final ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
        
        // VM-specific attributes
        final Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);
        
        // Classpath
        final String[] classpath = getClasspath(configuration);
        
        // Create VM config
        final VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
        runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
        runConfig.setVMArguments(execArgs.getVMArgumentsArray());
        runConfig.setWorkingDirectory(workingDirName);
        runConfig.setEnvironment(envp);
        runConfig.setVMSpecificAttributesMap(vmAttributesMap);
        
        // Bootpath
        final String[] bootpath = getBootpath(configuration);
        if (bootpath != null && bootpath.length > 0)
        {
            runConfig.setBootClassPath(bootpath);
        }
        
        setDefaultSourceLocator(launch, configuration);
        
        if (ILaunchManager.PROFILE_MODE.equals(mode))
        {
            try
            {
                ServerProfilerDelegate.configureProfiling(launch, vm, runConfig, monitor);
            }
            catch (CoreException ce)
            {
                spigotServer.stopImpl();
                throw ce;
            }
        }
        
        // Launch the configuration
        spigotServer.setupLaunch(launch, mode, monitor);
        try
        {
            runner.run(runConfig, launch, monitor);
            spigotServer.addProcessListener(launch.getProcesses()[0]);
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // Ensure we don't continue to think the server is starting
            spigotServer.stopImpl();
        }
        
    }
    
}
