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
public class SpigotPingThread extends AbstractPingThread
{
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
        super(server, maxPings, behaviour);
    }
    
    private static final Pattern PATTERN = Pattern.compile(".*Done \\([0-9,\\.]*s\\)! For help, type \"help\" or \"\\?\".*");

    @Override
    protected boolean checkForSuccess(String text)
    {
        // System.out.println("streamAppended " + text);
        // TODO seems to not like regex :-(
        return PATTERN.matcher(text.trim()).matches() || (text.contains("Done") && text.contains("For help"));
    }
    
}
