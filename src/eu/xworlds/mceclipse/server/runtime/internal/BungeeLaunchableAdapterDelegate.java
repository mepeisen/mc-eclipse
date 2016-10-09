/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.runtime.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;

/**
 * @author mepeisen
 *
 */
public class BungeeLaunchableAdapterDelegate extends LaunchableAdapterDelegate
{

    @Override
    public Object getLaunchable(IServer server, IModuleArtifact moduleObject) throws CoreException
    {
        if (server.getAdapter(SpigotServer.class) == null)
            return null;
        if (!(moduleObject instanceof BungeePlugin) &&
            !(moduleObject instanceof BungeeLibrary))
            return null;
        // TODO
//        if (moduleObject.getModule().loadAdapter(IWebModule.class, null) == null)
//            return null;
        
//        try {
//            // URL url = ((IURLProvider) server.loadAdapter(IURLProvider.class, null)).getModuleRootURL(moduleObject.getModule());
//            
//            // Trace.trace(Trace.FINER, "root: " + url);
//            
//            if (moduleObject instanceof Servlet) {
//                Servlet servlet = (Servlet) moduleObject;
//                if (servlet.getAlias() != null) {
//                    String path = servlet.getAlias();
//                    if (path.startsWith("/"))
//                        path = path.substring(1);
//                    url = new URL(url, path);
//                } else
//                    url = new URL(url, "servlet/" + servlet.getServletClassName());
//            } else if (moduleObject instanceof WebResource) {
//                WebResource resource = (WebResource) moduleObject;
//                String path = resource.getPath().toString();
//                Trace.trace(Trace.FINER, "path: " + path);
//                if (path != null && path.startsWith("/") && path.length() > 0)
//                    path = path.substring(1);
//                if (path != null && path.length() > 0)
//                    url = new URL(url, path);
//            }
//            return new HttpLaunchable(url);
//        } catch (Exception e) {
//            Trace.trace(Trace.SEVERE, "Error getting URL for " + moduleObject, e);
//            return null;
//        }
        return null;

    }

}
