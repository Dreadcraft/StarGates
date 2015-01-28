package net.doodcraft.Dooder07.Stargates.Wormhole;

import de.luricos.bukkit.WormholeXTreme.Worlds.handler.WorldHandler;

import net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands.*;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.Configuration;
import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholeNotAvailable;
import net.doodcraft.Dooder07.Stargates.Wormhole.listeners.*;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateHelper;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateDBManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionBackend;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.backends.BukkitSupport;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.backends.PermissionsExSupport;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.plugin.WormholeWorldsSupport;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.DBUpdateUtil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

public class StarGates extends JavaPlugin {

    private static final StarGatesPlayerListener playerListener = new StarGatesPlayerListener();
    private static final StarGatesBlockListener blockListener = new StarGatesBlockListener();
    private static final StarGatesVehicleListener vehicleListener = new StarGatesVehicleListener();
    private static final StarGatesEntityListener entityListener = new StarGatesEntityListener();
    private static final StarGatesServerListener serverListener = new StarGatesServerListener();
    private static final StarGatesRedstoneListener redstoneListener = new StarGatesRedstoneListener();

    protected PermissionManager permissionManager;
    protected ConfigManager configManager;
    
    private static WorldHandler worldHandler = null;
    
    private static BukkitScheduler scheduler = null;

    private boolean blockPluginExecution = false;

    @Override
    public void onLoad() {
        SGLogger.initLogger(this.getDescription().getName(), this.getDescription().getVersion(), ConfigManager.getLogLevel());

        SGLogger.prettyLog(Level.INFO, true, "Loading StarGates...");
        
        StarGates.setScheduler(this.getServer().getScheduler());
        
        ConfigManager.setupConfigs(this.getDescription());

        this.configManager = null;
        
        SGLogger.setLogLevel(ConfigManager.getLogLevel());

        if (!DBUpdateUtil.updateDB()) {
            SGLogger.prettyLog(Level.SEVERE, false, "Something went wrong during DBUpdate. Please check your server logs for details. Disabling StarGates for safety precautions.");
            blockPluginExecution = true;
            return;
        }

        StargateHelper.loadShapes();
        
        if(!(new File(this.getDataFolder()," config.yml").exists())) {
        	this.saveDefaultConfig();
        }

        SGLogger.prettyLog(Level.INFO, true, "Load complete");
    }
    
    public boolean reloadPlugin() {
        SGLogger.prettyLog(Level.INFO, true, "Reload in progress...");
        
        try {
            Configuration.writeFile(getDescription());
            final ArrayList<Stargate> gates = StargateManager.getAllGates();
            for (final Stargate gate : gates) {
                if (gate.isGateActive() || gate.isGateLightsActive()) {
                    gate.shutdownStargate(false);
                }
                StargateDBManager.stargateToSQL(gate);
            }

            SGLogger.prettyLog(Level.INFO, true, "Configuration written and stargates saved.");
        } catch (final Exception e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Caught exception while reloading: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
        StargateDBManager.shutdown();
        
        WormholePlayerManager.unregisterAllPlayers();
        
        WormholeWorldsSupport.disableWormholeWorlds();
        
        ConfigManager.setupConfigs(this.getDescription());
        
        SGLogger.setLogLevel(ConfigManager.getLogLevel());
        
        StargateHelper.reloadShapes();
        
        if (!ConfigManager.isWormholeWorldsSupportEnabled()) {
            SGLogger.prettyLog(Level.INFO, true, "Wormhole Worlds support disabled in settings.txt, loading stargates and worlds ourself.");
            StargateDBManager.loadStargates(this.getServer());
        }

        this.permissionManager.reset();

        WormholeWorldsSupport.enableWormholeWorlds(true);
        
        WormholePlayerManager.registerAllOnlinePlayers();
        
        
        SGLogger.prettyLog(Level.INFO, true, "Reloading complete.");
        return true;
    }    
    
    @Override
    public void onEnable() {
        if (blockPluginExecution) {
            SGLogger.prettyLog(Level.INFO, true, "Startup is blocked because of a previous database error. Check your server.log");
            return;
        }

        SGLogger.prettyLog(Level.INFO, true, "Boot sequence initiated...");
        
        getLogger().info("Reticulating splines...");
        getLogger().info("Cleaning up the dead babies...");
        getLogger().info("Activating trypophobia...");

        if (!ConfigManager.isWormholeWorldsSupportEnabled()) {
            SGLogger.prettyLog(Level.INFO, true, "Wormhole Worlds support disabled in settings.txt, loading stargates and worlds by our self.");
            StargateDBManager.loadStargates(this.getServer());
        }

        try {
        	PermissionBackend.registerBackendAlias("pex", PermissionsExSupport.class);
            PermissionBackend.registerBackendAlias("bukkit", BukkitSupport.class);

            this.resolvePermissionBackends();

            if (this.permissionManager == null) {
                this.permissionManager = new PermissionManager(this.configManager);
            }

            if (ConfigManager.isWormholeWorldsSupportEnabled()) {
                WormholeWorldsSupport.enableWormholeWorlds();
            }
        } catch (final Exception e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Caught Exception while trying to load support plugins. {" + e.getMessage() + "}");
            e.printStackTrace();
        }
        
        registerEvents(true);
        if (!ConfigManager.isWormholeWorldsSupportEnabled()) {
            registerEvents(false);
            registerCommands();
        }
        
        WormholePlayerManager.registerAllOnlinePlayers();
        
        SGLogger.prettyLog(Level.INFO, true, "Boot sequence completed");
    }   
    
    @Override
    public void onDisable() {
        if (blockPluginExecution) {
            SGLogger.prettyLog(Level.INFO, true, "Disable Functions skipped because of a previous error.");
            return;
        }

        SGLogger.prettyLog(Level.INFO, true, "Shutdown sequence initiated...");
        getLogger().info("Killing all these stupid babies...");
        
        try {
            Configuration.writeFile(getDescription());
            final ArrayList<Stargate> gates = StargateManager.getAllGates();

            for (final Stargate gate : gates) {
                if (gate.isGateActive() || gate.isGateLightsActive()) {
                    gate.shutdownStargate(false);
                }

                SGLogger.prettyLog(Level.FINE, false, "Saving gate: '" + gate.getGateName() + "', GateFace: '" + gate.getGateFacing().name() + "'");

                StargateDBManager.stargateToSQL(gate);
            }

            StargateDBManager.shutdown();

            if (this.permissionManager != null) {
                this.permissionManager.end();
            }

            WormholePlayerManager.unregisterAllPlayers();            
            
            SGLogger.prettyLog(Level.INFO, true, "Successfully shutdown StarGates.");
        } catch (final Exception e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Caught exception while shutting down: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static PermissionManager getPermissionManager() {
        try {
            if (!isPluginAvailable()) {
                if (SGLogger.getLogLevel().intValue() < Level.WARNING.intValue())
                    throw new WormholeNotAvailable("The plugin is not ready yet." + ((!getThisPlugin().isEnabled()) ? " Loading sequence is still in progress." : ""));
            }
        } catch (WormholeNotAvailable e) {
            SGLogger.prettyLog(Level.WARNING, false, e.getMessage());
        }

        return ((StarGates) getThisPlugin()).permissionManager;
    }

    private void resolvePermissionBackends() {
        for (String providerAlias : PermissionBackend.getRegisteredAliases()) {
            String pluginName = PermissionBackend.getBackendPluginName(providerAlias);
            SGLogger.prettyLog(Level.INFO, false, "Attempting to use supported permissions plugin '" + pluginName + "'");

            Plugin permToLoad = Bukkit.getPluginManager().getPlugin(pluginName);
            if ((pluginName.equals(PermissionBackend.getDefaultBackend().getProviderName())) || ((permToLoad != null) && (permToLoad.isEnabled()))) {
                ConfigManager.setPermissionBackend(providerAlias);
                SGLogger.prettyLog(Level.INFO, false, "Config node PERMISSIONS_BACKEND changed to '" + providerAlias + "'");
                return;
            } else {
                SGLogger.prettyLog(Level.FINE, false, "Permission backend '" + providerAlias + "' was not found as plugin or not enabled!");
            }
        }
    }

    public static BukkitScheduler getScheduler() {
        return scheduler;
    }

    public static StarGates getThisPlugin() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("StarGates");
        if (plugin == null || !(plugin instanceof StarGates)) {
            throw new RuntimeException("'StarGates' not found. 'StarGates' plugin disabled?");
        }

        return ((StarGates) plugin);
    }

    public static WorldHandler getWorldHandler() {
        return worldHandler;
    }

    public static void registerCommands() {
        final StarGates tp = getThisPlugin();
        
        tp.getCommand("sgforce").setExecutor(new Force());
        tp.getCommand("sgidc").setExecutor(new SGIDC());
        tp.getCommand("sgcompass").setExecutor(new Compass());
        tp.getCommand("sgcomplete").setExecutor(new Complete());
        tp.getCommand("sgremove").setExecutor(new SGRemove());
        tp.getCommand("sglist").setExecutor(new SGList());
        tp.getCommand("sggo").setExecutor(new Go());
        tp.getCommand("dial").setExecutor(new Dial());
        tp.getCommand("sgbuild").setExecutor(new Build());
        tp.getCommand("sgbuildlist").setExecutor(new BuildList());
        tp.getCommand("wormhole").setExecutor(new Wormhole());
        tp.getCommand("sgreload").setExecutor(new SGReload());
        tp.getCommand("sgstatus").setExecutor(new SGStatus());    
    }

    public static void registerEvents(boolean critical) {
        StarGates wxt = getThisPlugin();
        if (critical) {
            Bukkit.getServer().getPluginManager().registerEvents(serverListener, wxt);
        } else {
            Bukkit.getServer().getPluginManager().registerEvents(blockListener, wxt);

            Bukkit.getServer().getPluginManager().registerEvents(playerListener, wxt);

            Bukkit.getServer().getPluginManager().registerEvents(redstoneListener, wxt);

            Bukkit.getServer().getPluginManager().registerEvents(vehicleListener, wxt);

            Bukkit.getServer().getPluginManager().registerEvents(entityListener, wxt);
        }
    }

    protected static void setScheduler(BukkitScheduler scheduler) {
        StarGates.scheduler = scheduler;
    }

    public static void setWorldHandler(WorldHandler worldHandler) {
        StarGates.worldHandler = worldHandler;
    }

    public static boolean isPluginAvailable() {
        Plugin plugin = getThisPlugin();

        return (plugin instanceof StarGates) && ((StarGates) plugin).permissionManager != null;
    }
}
