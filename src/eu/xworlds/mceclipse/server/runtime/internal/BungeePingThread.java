/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import java.util.regex.Pattern;

import org.eclipse.wst.server.core.IServer;

/**
 * @author mepeisen
 *
 */
public class BungeePingThread extends AbstractPingThread
{
    /**
     * Create a new PingThread.
     * 
     * @param server
     * @param maxPings
     *            the maximum number of times to try pinging, or -1 to continue forever
     * @param behaviour
     */
    public BungeePingThread(IServer server, int maxPings, BungeeServerBehaviour behaviour)
    {
        super(server, maxPings, behaviour);
    }
    
    private static final Pattern PATTERN = Pattern.compile(".*Listening on.*");

    @Override
    protected boolean checkForSuccess(String text)
    {
        // System.out.println("streamAppended " + text);
        // TODO seems to not like regex :-(
        return PATTERN.matcher(text.trim()).matches() || text.contains("Listening on");
    }
    
}
