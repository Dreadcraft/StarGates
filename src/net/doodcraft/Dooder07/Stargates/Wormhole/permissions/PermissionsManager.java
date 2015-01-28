package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateDBManager;

import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PermissionsManager {

    public enum PermissionLevel {

        NO_PERMISSION_SET,
        WORMHOLE_FULL_PERMISSION,
        WORMHOLE_CREATE_PERMISSION,
        WORMHOLE_USE_PERMISSION,
        WORMHOLE_NO_PERMISSION
    }

    private static ConcurrentHashMap<String, PermissionLevel> player_general_permission = new ConcurrentHashMap<String, PermissionLevel>();
    private static PermissionLevel getIndividualPermissionLevel(final String player) {
        final String pl_lower = player.toLowerCase();
        if (player_general_permission.containsKey(pl_lower)) {
            return player_general_permission.get(pl_lower);
        } else {
            return PermissionLevel.NO_PERMISSION_SET;
        }
    }

    protected static PermissionLevel getPermissionLevel(final Player p, final Stargate s) {
        if (!ConfigManager.getBuiltInPermissionsEnabled()) {
            return PermissionLevel.WORMHOLE_FULL_PERMISSION;
        }

        if (s != null) {
        }

        final PermissionLevel lvl = getIndividualPermissionLevel(p.getName());
        if (lvl != PermissionLevel.NO_PERMISSION_SET) {
            return lvl;
        }

        if (s != null) {
        }

        if (s != null) {
        }

        if (p.isOp()) {
            return PermissionLevel.WORMHOLE_FULL_PERMISSION;
        } else {
            return ConfigManager.getBuiltInDefaultPermissionLevel();
        }
    }

    public static void handlePermissionRequest(final Player p, final String[] message_parts) {
        p.sendMessage("This system is currently under development and thus disabled");

//        if (p.isOp()) {
//            if (message_parts.length > 2) {
//                if (message_parts[2].equalsIgnoreCase("active")) {
//                    if (message_parts.length == 4) {
//                        try {
//                            final boolean active = Boolean.parseBoolean(message_parts[3]);
//                            ConfigManager.setConfigValue(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, active);
//                        } catch (final Exception e) {
//                            p.sendMessage("Invalid format - only true and false allowed.");
//                        }
//                    }
//                    p.sendMessage("Permissions active is: " + ConfigManager.getBuiltInPermissionsEnabled());
//                } else if (message_parts[2].equalsIgnoreCase("indiv")) {
//                    if (message_parts.length == 5) {
//                        try {
//                            PermissionsManager.setIndividualPermissionLevel(message_parts[3].toLowerCase(), PermissionsManager.PermissionLevel.valueOf(message_parts[4]));
//                        } catch (final Exception e) {
//                            p.sendMessage("Invalid format - /wormhole perms indiv <username> <perm>.");
//                            p.sendMessage("Valid Permission Levels: ");
//                            for (final PermissionsManager.PermissionLevel level : PermissionsManager.PermissionLevel.values()) {
//                                p.sendMessage(" " + level.toString());
//                            }
//                        }
//                    }
//
//                    p.sendMessage("Permissions for " + message_parts[3] + ": " + PermissionsManager.getIndividualPermissionLevel(message_parts[3].toLowerCase()));
//                } else if (message_parts[2].equalsIgnoreCase("default")) {
//                    if (message_parts.length == 4) {
//                        try {
//                            ConfigManager.setConfigValue(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, PermissionLevel.valueOf(message_parts[3]));
//                            p.sendMessage("Default Permission is now: " + ConfigManager.getBuiltInDefaultPermissionLevel());
//                        } catch (final NullPointerException e) {
//                            p.sendMessage("Invalid format - /wormhole perms default <perm>");
//                            p.sendMessage("Valid Permission Levels: ");
//                            for (final PermissionsManager.PermissionLevel level : PermissionsManager.PermissionLevel.values()) {
//                                p.sendMessage(" " + level.toString());
//                            }
//                        }
//                    }
//                }
//            } else {
//                // /stargate perms indiv    <USERNAME>     <OPTIONAL_SET> (else its a get)
//                // /stargate perms group    <GROUPNAME>    <OPTIONAL_SET> (else its a get)
//                // /stargate perms default <OPTIONAL_SET> (else a get)
//                // /stargate perms active  <OPTIONAL_SET> (else a get)
//                p.sendMessage("/wormhole perms indiv    <USERNAME>     <OPTIONAL_SET>");
//                //p.sendMessage("/stargate perms indiv    <USERNAME>     <OPTIONAL_SET>");
//                p.sendMessage("/wormhole perms default <OPTIONAL_SET>");
//                p.sendMessage("/wormhole perms active  <OPTIONAL_SET> (else a get)");
//            }
//        } else {
//            p.sendMessage("Unable to set permissions unless you are OP. Try \"op <name>\"");
//        }
    }

    public static void loadPermissions() {
        player_general_permission = StargateDBManager.getAllIndividualPermissions();
    }

    @SuppressWarnings("unused")
	private static void setIndividualPermissionLevel(final String player, final PermissionLevel lvl) {
        final String pl_lower = player.toLowerCase();
        player_general_permission.put(pl_lower, lvl);
        StargateDBManager.storeIndividualPermissionInDB(pl_lower, lvl);
    }
}
