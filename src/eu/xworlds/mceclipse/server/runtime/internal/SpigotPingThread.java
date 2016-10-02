/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.regex.Pattern;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.wst.server.core.IServer;

/**
 * @author mepeisen
 *
 */
public class SpigotPingThread implements IStreamListener
{
    // delay before pinging starts
    private static final int      PING_DELAY    = 2000;
    
    // delay between pings
    private static final int      PING_INTERVAL = 250;
    
    // maximum number of pings before giving up
    private int                   maxPings;
    
    private boolean               stop          = false;
    private IServer               server;
    private SpigotServerBehaviour behaviour;

    private boolean success;
    
    /**
     * Create a new PingThread.
     * 
     * @param server
     * @param maxPings
     *            the maximum number of times to try pinging, or -1 to continue forever
     * @param behaviour
     */
    public SpigotPingThread(IServer server, int maxPings, SpigotServerBehaviour behaviour)
    {
        this.server = server;
        this.maxPings = maxPings;
        this.behaviour = behaviour;
        Thread t = new Thread("Spigot Ping Thread") {
            public void run()
            {
                ping();
            }
        };
        t.setDaemon(true);
        t.start();
    }
    
    /**
     * Ping the server until it is started. Then set the server state to STATE_STARTED.
     */
    protected void ping()
    {
        int count = 0;
        try
        {
            Thread.sleep(PING_DELAY);
        }
        catch (Exception e)
        {
            // ignore
        }
        final ILaunch launch = this.server.getLaunch();
        if (launch != null)
        {
            final IProcess[] processes = launch.getProcesses();
            if (processes != null)
            {
                for (final IProcess process : this.server.getLaunch().getProcesses())
                {
                    // System.out.println("Attach ping thread to process " + process);
                    process.getStreamsProxy().getOutputStreamMonitor().addListener(this);
                }
            }
        }
        while (!stop)
        {
            if (count == maxPings)
            {
                try
                {
                    server.stop(false);
                }
                catch (Exception e)
                {
                    // Trace.trace(Trace.FINEST, "Ping: could not stop server");
                }
                stop = true;
                break;
            }
            count++;
            
            if (success)
            {
                // Trace.trace(Trace.FINEST, "Ping: success");
                try
                {
                    Thread.sleep(200);
                }
                catch (InterruptedException e)
                {
                    // ignore
                }
                behaviour.setServerStarted();
                stop = true;
            }
            
            // pinging failed
            if (!stop)
            {
                try
                {
                    Thread.sleep(PING_INTERVAL);
                }
                catch (InterruptedException e2)
                {
                    // ignore
                }
            }
        }

        if (launch != null)
        {
            final IProcess[] processes = launch.getProcesses();
            if (processes != null)
            {
                for (final IProcess process : processes)
                {
                    process.getStreamsProxy().getOutputStreamMonitor().removeListener(this);
                }
            }
        }
    }
    
    /**
     * Tell the pinging to stop.
     */
    public void stop()
    {
        // Trace.trace(Trace.FINEST, "Ping: stopping");
        stop = true;
    }
    
    private static final Pattern PATTERN = Pattern.compile(".*Done \\([0-9,\\.]*s\\)! For help, type \"help\" or \"\\?\".*");

    @Override
    public void streamAppended(String text, IStreamMonitor paramIStreamMonitor)
    {
        // System.out.println("streamAppended " + text);
        // TODO seems to not like regex :-(
        this.success |= PATTERN.matcher(text.trim()).matches() || (text.contains("Done") && text.contains("For help"));
    }
    
}
