/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

/**
 * @author mepeisen
 *
 */
public class Bungee111VersionHandler extends BungeeVersionHandler
{

    @Override
    protected String getPomVersion()
    {
        return "1.11-SNAPSHOT"; //$NON-NLS-1$
    }

    @Override
    protected String getBungeeToolsName()
    {
        return "1.11"; //$NON-NLS-1$
    }
    
}
