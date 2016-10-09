/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.wst.server.core.IServer;

/**
 * @author mepeisen
 *
 */
public abstract class AbstractPingThread implements IStreamListener
{
    /** delay before pinging starts */
    private static final int      PING_DELAY    = 2000;
    
    /** delay between pings */
    private static final int      PING_INTERVAL = 250;
    
    /** maximum number of pings before giving up */
    private int                   maxPings;
    
    /** stop flag. */
    private boolean               stop          = false;
    /** the server. */
    private IServer               server;
    /** the server behaviour. */
    private AbstractServerBehaviour behaviour;

    /** success flag. */
    private boolean success;
    
    /**
     * Create a new PingThread.
     * 
     * @param server
     * @param maxPings
     *            the maximum number of times to try pinging, or -1 to continue forever
     * @param behaviour
     */
    public AbstractPingThread(IServer server, int maxPings, AbstractServerBehaviour behaviour)
    {
        this.server = server;
        this.maxPings = maxPings;
        this.behaviour = behaviour;
        Thread t = new Thread("Minecraft Ping Thread") { //$NON-NLS-1$
            @Override
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
        catch (@SuppressWarnings("unused") Exception e)
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
        while (!this.stop)
        {
            if (count == this.maxPings)
            {
                try
                {
                    this.server.stop(false);
                }
                catch (@SuppressWarnings("unused") Exception e)
                {
                    // Trace.trace(Trace.FINEST, "Ping: could not stop server");
                }
                this.stop = true;
                break;
            }
            count++;
            
            if (this.success)
            {
                // Trace.trace(Trace.FINEST, "Ping: success");
                try
                {
                    Thread.sleep(200);
                }
                catch (@SuppressWarnings("unused") InterruptedException e)
                {
                    // ignore
                }
                this.behaviour.setServerStarted();
                this.stop = true;
            }
            
            // pinging failed
            if (!this.stop)
            {
                try
                {
                    Thread.sleep(PING_INTERVAL);
                }
                catch (@SuppressWarnings("unused") InterruptedException e2)
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
        this.stop = true;
    }
    
    /**
     * Checks console text for successful starting.
     * @param text console text
     * @return {@code true} for success
     */
    protected abstract boolean checkForSuccess(String text);

    @Override
    public void streamAppended(String text, IStreamMonitor paramIStreamMonitor)
    {
        this.success |= this.checkForSuccess(text);
    }
    
}
