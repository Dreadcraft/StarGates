package net.doodcraft.Dooder07.Stargates.Wormhole.config;

import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager.PermissionLevel;

import org.bukkit.plugin.PluginDescriptionFile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ConfigManager {


    public enum ConfigKeys {

        BUILT_IN_PERMISSIONS_ENABLED,
        BUILT_IN_DEFAULT_PERMISSION_LEVEL,
        PERMISSIONS_SUPPORT_DISABLE,
        SIMPLE_PERMISSIONS,
        WORMHOLE_USE_IS_TELEPORT,
        TIMEOUT_ACTIVATE,
        TIMEOUT_SHUTDOWN,
        BUILD_RESTRICTION_ENABLED,
        BUILD_RESTRICTION_GROUP_ONE,
        BUILD_RESTRICTION_GROUP_TWO,
        BUILD_RESTRICTION_GROUP_THREE,
        USE_COOLDOWN_ENABLED,
        USE_COOLDOWN_GROUP_ONE,
        USE_COOLDOWN_GROUP_TWO,
        USE_COOLDOWN_GROUP_THREE,
        HELP_SUPPORT_DISABLE,
        WORLDS_SUPPORT_ENABLED,
        LOG_LEVEL,
        SHOW_GATE_WELCOME_MESSAGE,
        USE_EVENT_OR_TP_TRANSPORT,
        WORMHOLE_KICKBACK_BLOCK_COUNT,
        PERMISSIONS_BACKEND
    }

    public static enum MessageStrings {
        messageColor("\u00A77"),
        errorHeader("\u00A73:: \u00A75error \u00A73:: \u00A77"),
        normalHeader("\u00A73:: \u00A77"),
        permissionNo(errorHeader + "You lack the permissions to do this."),
        targetIsSelf(errorHeader + "Can't dial own gate without solar flare"),
        targetInvalid(errorHeader + "Invalid gate target."),
        targetIsActive(errorHeader + "Target gate %sis currently active."),
        targetIsInUseBy(errorHeader + "Target gate %s is currently in use by %s."),
        gateNotActive(errorHeader + "No gate activated to dial."),
        gateRemoteActive(errorHeader + "Gate %sremotely activated%s."),
        gateShutdown(normalHeader + "Gate %ssuccessfully shutdown."),
        gateActivated(normalHeader + "Gate %ssuccessfully activated."),
        gateDeactivated(normalHeader + "Gate %ssuccessfully deactivated."),
        gateConnected(normalHeader + "Stargates connected."),
        gateWithInvalidShape(errorHeader + "No valid Stargate shape was found."),
        gateWithInvalidShapeAssistance(normalHeader + "Type /sgbuild for build assistance."),
        constructSuccess(normalHeader + "Gate successfully constructed."),
        constructNameInvalid(errorHeader + "Gate name invalid: "),
        constructNameTooLong(errorHeader + "Gate name too long: "),
        constructNameTaken(errorHeader + "Gate name already taken: "),
        requestInvalid(errorHeader + "Invalid Request"),
        gateNotSpecified(errorHeader + "No gate name specified."),
        playerBuildCountRestricted(errorHeader + "You are at your max number of built gates."),
        playerUseCooldownRestricted(errorHeader + "You must wait longer before using a stargate."),
        playerUseCooldownWaitTime(errorHeader + "Current Wait (in seconds): "),
        playerUsedStargate(normalHeader + "You arrived at %s%s");

        private final String m;

        private MessageStrings(final String message) {
            m = message;
        }

        @Override
        public String toString() {
            return m;
        }
    }

    private static final ConcurrentHashMap<ConfigKeys, Setting> configurations = new ConcurrentHashMap<ConfigKeys, Setting>();

    public static String getPermissionBackend() {
        return isConfigurationKey(ConfigKeys.PERMISSIONS_BACKEND)
                ? getSetting(ConfigKeys.PERMISSIONS_BACKEND).getStringValue()
                : "bukkit";
    }

    public static int getBuildRestrictionGroupOne() {
        return isConfigurationKey(ConfigKeys.BUILD_RESTRICTION_GROUP_ONE)
                ? getSetting(ConfigKeys.BUILD_RESTRICTION_GROUP_ONE).getIntValue()
                : 1;
    }

    public static int getBuildRestrictionGroupThree() {
        return isConfigurationKey(ConfigKeys.BUILD_RESTRICTION_GROUP_THREE)
                ? getSetting(ConfigKeys.BUILD_RESTRICTION_GROUP_THREE).getIntValue()
                : 3;
    }

    public static int getBuildRestrictionGroupTwo() {
        return isConfigurationKey(ConfigKeys.BUILD_RESTRICTION_GROUP_TWO)
                ? getSetting(ConfigKeys.BUILD_RESTRICTION_GROUP_TWO).getIntValue()
                : 2;
    }

    public static PermissionLevel getBuiltInDefaultPermissionLevel() {
        Setting bidpl;
        if ((bidpl = ConfigManager.getConfigurations().get(ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL)) != null) {
            return bidpl.getPermissionLevel();
        } else {
            return PermissionLevel.WORMHOLE_USE_PERMISSION;
        }
    }

    public static boolean getBuiltInPermissionsEnabled() {
        Setting bipe;
        if ((bipe = ConfigManager.getConfigurations().get(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED)) != null) {
            return bipe.getBooleanValue();
        } else {
            return false;
        }
    }

    public static ConcurrentHashMap<ConfigKeys, Setting> getConfigurations() {
        return configurations;
    }

    public static boolean getHelpSupportDisable() {
        Setting hsd;
        return (hsd = ConfigManager.getConfigurations().get(ConfigKeys.HELP_SUPPORT_DISABLE)) != null && hsd.getBooleanValue();
    }

    public static Level getLogLevel() {
        Setting ll;
        if ((ll = ConfigManager.getConfigurations().get(ConfigKeys.LOG_LEVEL)) != null) {
            return ll.getLevel();
        }

        return Level.INFO;
    }

    public static boolean isGateArrivalWelcomeMessageEnabled() {
        Setting wme;
        if ((wme = ConfigManager.getConfigurations().get(ConfigKeys.SHOW_GATE_WELCOME_MESSAGE)) != null) {
            return wme.getBooleanValue();
        }
        
        return true;
    }
    
    public static void setShowGWM(final boolean g) {
        ConfigManager.setConfigValue(ConfigKeys.SHOW_GATE_WELCOME_MESSAGE, g);
    }
    
    public static boolean getGateTransportMethod() {
        Setting tm;
        if ((tm = ConfigManager.getConfigurations().get(ConfigKeys.USE_EVENT_OR_TP_TRANSPORT)) != null) {
            return tm.getBooleanValue();
        }
        
        return true;
    }
    
    public static void setGateTransportMethod(boolean tm) {
        ConfigManager.setConfigValue(ConfigKeys.USE_EVENT_OR_TP_TRANSPORT, tm);
    }
    
    public static int getWormholeKickbackBlockCount() {
        return isConfigurationKey(ConfigKeys.WORMHOLE_KICKBACK_BLOCK_COUNT)
                ? getSetting(ConfigKeys.WORMHOLE_KICKBACK_BLOCK_COUNT).getIntValue()
                : 2;
    }
    
    public static void setWormholeKickbackBlockCount(int wkbCount) {
        ConfigManager.setConfigValue(ConfigKeys.WORMHOLE_KICKBACK_BLOCK_COUNT, wkbCount);
    }

    public static boolean getPermissionsSupportDisable() {
        Setting psd;
        return (psd = ConfigManager.getConfigurations().get(ConfigKeys.PERMISSIONS_SUPPORT_DISABLE)) != null && psd.getBooleanValue();
    }

    private static Setting getSetting(final ConfigKeys configKey) {
        return getConfigurations().get(configKey);
    }

    public static boolean getSimplePermissions() {
        Setting sp;
        if ((sp = ConfigManager.getConfigurations().get(ConfigKeys.SIMPLE_PERMISSIONS)) != null) {
            return sp.getBooleanValue();
        } else {
            return false;
        }
    }

    public static int getTimeoutActivate() {
        Setting ta;
        if ((ta = ConfigManager.getConfigurations().get(ConfigKeys.TIMEOUT_ACTIVATE)) != null) {
            return ta.getIntValue();
        } else {
            return 30;
        }
    }

    public static int getTimeoutShutdown() {
        Setting ts;
        if ((ts = ConfigManager.getConfigurations().get(ConfigKeys.TIMEOUT_SHUTDOWN)) != null) {
            return ts.getIntValue();
        } else {
            return 38;
        }
    }

    public static int getUseCooldownGroupOne() {
        return isConfigurationKey(ConfigKeys.USE_COOLDOWN_GROUP_ONE)
                ? getSetting(ConfigKeys.USE_COOLDOWN_GROUP_ONE).getIntValue()
                : 120;
    }

    public static int getUseCooldownGroupThree() {
        return isConfigurationKey(ConfigKeys.USE_COOLDOWN_GROUP_THREE)
                ? getSetting(ConfigKeys.USE_COOLDOWN_GROUP_THREE).getIntValue()
                : 60;
    }

    public static int getUseCooldownGroupTwo() {
        return isConfigurationKey(ConfigKeys.USE_COOLDOWN_GROUP_TWO)
                ? getSetting(ConfigKeys.USE_COOLDOWN_GROUP_TWO).getIntValue()
                : 30;
    }

    public static boolean getWormholeUseIsTeleport() {
        Setting bipe;
        if ((bipe = ConfigManager.getConfigurations().get(ConfigKeys.WORMHOLE_USE_IS_TELEPORT)) != null) {
            return bipe.getBooleanValue();
        } else {
            return false;
        }
    }

    public static boolean isBuildRestrictionEnabled() {
        return ConfigManager.getConfigurations().get(ConfigKeys.BUILD_RESTRICTION_ENABLED) != null && ConfigManager.getConfigurations().get(ConfigKeys.BUILD_RESTRICTION_ENABLED).getBooleanValue();
    }

    private static boolean isConfigurationKey(final ConfigKeys configKey) {
        return getConfigurations().containsKey(configKey);
    }

    public static boolean isUseCooldownEnabled() {
        return ConfigManager.getConfigurations().get(ConfigKeys.USE_COOLDOWN_ENABLED) != null && ConfigManager.getConfigurations().get(ConfigKeys.USE_COOLDOWN_ENABLED).getBooleanValue();
    }

    public static boolean isWormholeWorldsSupportEnabled() {
        Setting wsd;
        if ((wsd = ConfigManager.getConfigurations().get(ConfigKeys.WORLDS_SUPPORT_ENABLED)) != null) {
            return wsd.getBooleanValue();
        } else {
            return false;
        }
    }

    public static void setBuildRestrictionEnabled(final boolean b) {
        ConfigManager.setConfigValue(ConfigKeys.BUILD_RESTRICTION_ENABLED, b);
    }

    public static void setBuildRestrictionGroupOne(final int count) {
        setConfigValue(ConfigKeys.BUILD_RESTRICTION_GROUP_ONE, count);
    }

    public static void setBuildRestrictionGroupThree(final int count) {
        setConfigValue(ConfigKeys.BUILD_RESTRICTION_GROUP_THREE, count);
    }

    public static void setBuildRestrictionGroupTwo(final int count) {
        setConfigValue(ConfigKeys.BUILD_RESTRICTION_GROUP_TWO, count);
    }

    public static void setConfigValue(final ConfigKeys key, final Object value) {
        if ((key != null) && isConfigurationKey(key) && (value != null)) {
            getConfigurations().get(key).setValue(value);
        }
    }

    public static void setSimplePermissions(final boolean b) {
        ConfigManager.setConfigValue(ConfigKeys.SIMPLE_PERMISSIONS, b);
    }

    public static void setTimeoutActivate(final int i) {
        ConfigManager.setConfigValue(ConfigKeys.TIMEOUT_ACTIVATE, i);
    }

    public static void setTimeoutShutdown(final int i) {
        ConfigManager.setConfigValue(ConfigKeys.TIMEOUT_SHUTDOWN, i);
    }

    public static void setupConfigs(final PluginDescriptionFile pdf) {
        Configuration.loadConfiguration(pdf);
    }

    public static void setUseCooldownEnabled(final boolean b) {
        ConfigManager.setConfigValue(ConfigKeys.USE_COOLDOWN_ENABLED, b);
    }

    public static void setUseCooldownGroupOne(final int time) {
        setConfigValue(ConfigKeys.USE_COOLDOWN_GROUP_ONE, time);
    }

    public static void setUseCooldownGroupThree(final int time) {
        setConfigValue(ConfigKeys.USE_COOLDOWN_GROUP_THREE, time);
    }

    public static void setUseCooldownGroupTwo(final int time) {
        setConfigValue(ConfigKeys.USE_COOLDOWN_GROUP_TWO, time);
    }
    
    public static void setDebugLevel(final String level) {
        setConfigValue(ConfigKeys.LOG_LEVEL, level.toUpperCase());
    }

    public static void setPermissionBackend(String backendName) {
        setConfigValue(ConfigKeys.PERMISSIONS_BACKEND, backendName);
    }
}