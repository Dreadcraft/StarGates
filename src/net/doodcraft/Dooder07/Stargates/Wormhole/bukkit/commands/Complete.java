package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.StargateRestrictions;

public class Complete implements CommandExecutor {

    private static boolean doComplete(final Player player, final String[] args) {
        final String name = args[0].trim().replace("\n", "").replace("\r", "");

        if (name.length() < 12) {
            String idc = "";
            String network = "Public";

            for (int i = 1; i < args.length; i++) {
                final String[] key_value_string = args[i].split("=");
                if (key_value_string[0].equals("idc")) {
                    idc = key_value_string[1];
                } else if (key_value_string[0].equals("net")) {
                    network = key_value_string[1];
                }
            }
            if (SGPermissions.checkPermission(player, network, PermissionType.BUILD)) {
                if (!StargateRestrictions.isPlayerBuildRestricted(player)) {
                    if (StargateManager.getStargate(name) == null) {
                        if (StargateManager.completeStargate(player, name, idc, network)) {
                            player.sendMessage(ConfigManager.MessageStrings.constructSuccess.toString());
                        } else {
                            player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Construction Failed!?");
                        }
                    } else {
                        player.sendMessage(ConfigManager.MessageStrings.constructNameTaken.toString() + "\"" + name + "\"");
                    }
                } else {
                    player.sendMessage(ConfigManager.MessageStrings.playerBuildCountRestricted.toString());
                }
            } else {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
        } else {
            player.sendMessage(ConfigManager.MessageStrings.constructNameTooLong.toString() + "\"" + name + "\"");
        }
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String[] arguments = CommandUtilities.commandEscaper(args);
        if ((arguments.length <= 3) && (arguments.length > 0)) {
            return !CommandUtilities.playerCheck(sender) || doComplete((Player) sender, arguments);
        }
        return false;
    }
}
