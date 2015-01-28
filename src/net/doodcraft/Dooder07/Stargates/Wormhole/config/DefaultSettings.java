package net.doodcraft.Dooder07.Stargates.Wormhole.config;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager.ConfigKeys;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager.PermissionLevel;

public class DefaultSettings {

    final static Setting[] config = {
        new Setting(ConfigKeys.TIMEOUT_ACTIVATE, 30, "Number of seconds after a gate is activated, but before dialing before timing out.", "StarGates"),
        new Setting(ConfigKeys.TIMEOUT_SHUTDOWN, 38, "Number of seconds after a gate is dialed before automatically shutdown. With 0 timeout a gate won't shutdown until something goes through the gate.", "StarGates"),
        new Setting(ConfigKeys.BUILD_RESTRICTION_ENABLED, false, "Enable build count restrictions. Reuires complex permissions.", "StarGates"),
        new Setting(ConfigKeys.BUILD_RESTRICTION_GROUP_ONE, 1, "Total number of stargates a member of build restriction group one can build.", "StarGates"),
        new Setting(ConfigKeys.BUILD_RESTRICTION_GROUP_TWO, 2, "Total number of stargates a member of build restriction group two can build.", "StarGates"),
        new Setting(ConfigKeys.BUILD_RESTRICTION_GROUP_THREE, 3, "Total number of stargates a member of build restriction group three can build.", "StarGates"),
        new Setting(ConfigKeys.USE_COOLDOWN_ENABLED, false, "Enable Cooldown timers on stargate usage. Timer only activates on passage through wormholes. Requires complex permissions enabled.", "StarGates"),
        new Setting(ConfigKeys.USE_COOLDOWN_GROUP_ONE, 120, "Cooldown time in seconds between stargate use for members of use cooldown group one.", "StarGates"),
        new Setting(ConfigKeys.USE_COOLDOWN_GROUP_TWO, 60, "Cooldown time in seconds between stargate use for members of use cooldown group two.", "StarGates"),
        new Setting(ConfigKeys.USE_COOLDOWN_GROUP_THREE, 30, "Cooldown time in seconds between stargate use for members of use cooldown group three.", "StarGates"),
        new Setting(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, false, "This should be set to true if you want the built in permissions enabled. This setting does nothing if you have Permissions plugin installed.", "StarGates"),
        new Setting(ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL, PermissionLevel.WORMHOLE_USE_PERMISSION, "If built in permissions are being used, this is the default level of control users (non-ops) have.", "StarGates"),
        new Setting(ConfigKeys.PERMISSIONS_SUPPORT_DISABLE, false, "If set to true, Permissions plugin will not be attached to evem if available.", "StarGates"),
        new Setting(ConfigKeys.SIMPLE_PERMISSIONS, false, "If using Permissions plugin based permissions, setting this to true switches StarGates to use an extremely simplified permissions set ('wormhole.simple.use', 'wormhole.simple.build', 'wormhole.simple.config', and 'wormhole.simple.remove').", "StarGates"),
        new Setting(ConfigKeys.WORMHOLE_USE_IS_TELEPORT, false, "The wormhole.use (or wormhole.simple.use) permission means that a user can teleport through gate. When false a user will be able to teleport but not activate a gate. When true only users with wormhole.use (or wormhole.simple.use) can even teleport.", "StarGates"),
        new Setting(ConfigKeys.HELP_SUPPORT_DISABLE, false, "If set to true, Help plugin will not be attached to even if available.", "StarGates"),
        new Setting(ConfigKeys.WORLDS_SUPPORT_ENABLED, false, "If set to true, StarGates will offload all of its Chunk and World loading functionality to Wormhole Extreme Worlds.", "StarGates"),
        new Setting(ConfigKeys.LOG_LEVEL, "INFO", "Log level to use for minecraft logging purposes. Values are SEVERE, WARNING, INFO, CONFIG, FINE, FINER, and FINEST. In order of least to most logging output.", "StarGates"),
        new Setting(ConfigKeys.SHOW_GATE_WELCOME_MESSAGE, true, "If set to true, the player will receive a welcome message after arrival", "StarGates"),
        new Setting(ConfigKeys.USE_EVENT_OR_TP_TRANSPORT, true, "If set to true, the player will be transported via event handling. If you use plugins that can overwrite onPlayerMove() then you may want to change this to false", "StarGates"),
        new Setting(ConfigKeys.WORMHOLE_KICKBACK_BLOCK_COUNT, 2, "Set the amount of blocks the player will be kicked back if the target's gate is active. Default is 2. Set to 0 to disable this feature.", "StarGates"),
        new Setting(ConfigKeys.PERMISSIONS_BACKEND, "bukkit", "Set used PermissionsBackend. See documentation for possible values", "StarGates")};
}
