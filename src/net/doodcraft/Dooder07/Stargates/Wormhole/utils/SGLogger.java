package net.doodcraft.Dooder07.Stargates.Wormhole.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SGLogger {
    private static Level logLevel = Level.INFO;
    private static Logger logger = null;
    private static String logPluginName = null;
    private static String logPluginVersion = null;

    public static Level getLogLevel() {
        return logLevel;
    }
    
    public static String getName() {
        return logPluginName;
    }
    
    public static String getVersion() {
        return logPluginVersion;
    }
    
    public static void initLogger(String pluginName, String pluginVersion, Level logLevel) {
        if (SGLogger.logger == null) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                SGLogger.logger = plugin.getLogger();
            }
            
            SGLogger.logLevel = logLevel;
            SGLogger.logger.setLevel(logLevel);
            SGLogger.logPluginName = pluginName;
            SGLogger.logPluginVersion = pluginVersion;
            
            try {
                plugin.getDataFolder().mkdirs();
                
				FileHandler handler = new FileHandler(plugin.getDataFolder() + "/SG.log");
				handler.setLevel(Level.ALL);
				handler.setFormatter(new SimpleFormatter());
				SGLogger.logger.addHandler(handler);
			}
            catch (IOException e) {
				prettyLog(Level.SEVERE, true, "Unable to initialize log file. SG logs will not be saved to file.");
			}
        }
    }
    
    public static void prettyLog(final Level logLevel, final boolean version, final String message) {
        final String prettyVersion = ("[v" + getVersion() + "]");
        String prettyLogLine = "";
        if (version) {
            prettyLogLine += prettyVersion;
        }
        
        logger.log(logLevel, prettyLogLine + " " + message);
    }
    
    public static void setLogLevel(Level logLevel) {
        SGLogger.logLevel = logLevel;
        SGLogger.logger.setLevel(logLevel);
    }
}
