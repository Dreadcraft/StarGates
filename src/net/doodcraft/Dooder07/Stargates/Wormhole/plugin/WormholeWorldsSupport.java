package net.doodcraft.Dooder07.Stargates.Wormhole.plugin;

import de.luricos.bukkit.WormholeXTreme.Worlds.WormholeXTremeWorlds;
import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateDBManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class WormholeWorldsSupport {

    public static void disableWormholeWorlds() {
        if (StarGates.getWorldHandler() != null) {
            StarGates.setWorldHandler(null);
            SGLogger.prettyLog(Level.INFO, false, "Detached from Wormhole Worlds plugin.");
        }
    }

    public static void enableWormholeWorlds() {
        enableWormholeWorlds(false);
    }
    
    public static void enableWormholeWorlds(boolean reload) {
        if (ConfigManager.isWormholeWorldsSupportEnabled()) {
            if (!WormholeWorldsSupport.isEnabled()) {
                final Plugin worldsTest = Bukkit.getServer().getPluginManager().getPlugin("WormholeXTremeWorlds");
                if (worldsTest != null) {
                    final String version = worldsTest.getDescription().getVersion();
                    if (checkWorldsVersion(version)) {
                        try {
                            StarGates.setWorldHandler(WormholeXTremeWorlds.getWorldHandler());
                            SGLogger.prettyLog(Level.INFO, false, "Attached to Wormhole Worlds version " + version);

                            StargateDBManager.loadStargates(Bukkit.getServer());
                            
                            if (!reload) {
                                StarGates.registerEvents(false);
                                StarGates.registerCommands();
                            }
                            
                            SGLogger.prettyLog(Level.INFO, true, "Enable Completed.");
                        } catch (final ClassCastException e) {
                            SGLogger.prettyLog(Level.WARNING, false, "Failed to get cast to Wormhole Worlds: " + e.getMessage());
                        }
                    }
                } else {
                    SGLogger.prettyLog(Level.INFO, false, "Wormhole Worlds Plugin not yet available Stargates will not load until it enables.");
                }
            } else {
                SGLogger.prettyLog(Level.INFO, false, "Wormhole Worlds Plugin not yet available Stargates will not load until it enables.");
            }
        } else {
            SGLogger.prettyLog(Level.INFO, false, "Wormhole X-Treme Worlds Plugin support disabled via settings.txt.");
        }
    }

    private static boolean checkWorldsVersion(String version) {
        if (!isSupportedVersion(version)) {
            SGLogger.prettyLog(Level.SEVERE, false, "Not a supported version of WormholeXTreme-Worlds. Recommended is > 0.507");
            return false;
        }

        return true;
    }

    public static boolean isSupportedVersion(String verIn) {
        return isSupportedVersion(verIn, 0.507);
    }

    public static boolean isSupportedVersion(String verIn, Double checkVer) {
        String comp1 = verIn.replaceAll("\\.", "");
        int subVCount = verIn.length() - comp1.length();

        if ((subVCount < 2) && (Double.parseDouble(verIn) >= checkVer))
            return true;

        if ((subVCount < 2) && (Double.parseDouble(verIn) < checkVer))
            return false;

        int firstMatch = verIn.indexOf(".");
        String verOut = verIn.substring(0, firstMatch) + "." + comp1.substring(firstMatch);

        return Double.parseDouble(verOut) >= checkVer;
    }
    
    public static boolean isEnabled() {
        return StarGates.getWorldHandler() != null;
    }
}