/**
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution.
 */

package eu.xworlds.mceclipse.server.internal;

/**
 * @author mepeisen
 *
 */
public enum CPEModuleType
{
    /** a common java library. */
    Library,
    /** spigot plugin. */
    SpigotPlugin,
    /** spigot library. */
    SpigotLibrary,
    /** bungee plugin. */
    BungeePlugin,
    /** bungee library. */
    BungeeLibrary,
    /** an unknown plugin type. */
    UnknownPlugin,
    /** spigot jar file. */
    SpigotJar,
    /** bungee jar file. */
    BungeeJar,
    /** bukkit jar file. */
    BukkitJar,
}
