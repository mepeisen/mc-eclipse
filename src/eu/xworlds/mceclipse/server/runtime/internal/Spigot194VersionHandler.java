/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

/**
 * @author mepeisen
 *
 */
public class Spigot194VersionHandler extends SpigotVersionHandler
{

    @Override
    protected String getPomVersion()
    {
        return "1.9.4-R0.1-SNAPSHOT"; //$NON-NLS-1$
    }

    @Override
    protected String getSpigotToolsName()
    {
        return "1.9.4"; //$NON-NLS-1$
    }
    
}
