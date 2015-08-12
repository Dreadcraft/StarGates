package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;

public class Go implements CommandExecutor {

    private static boolean doGo(final Player player, final String[] args) {
        if (SGPermissions.checkPermission(player, PermissionType.GO)) {
            if (args.length == 1) {
                final String goGate = args[0].trim().replace("\n", "").replace("\r", "");
                final Stargate s = StargateManager.getStargate(goGate);
                if (s != null) {
                    player.teleport(s.getGatePlayerTeleportLocation());
                } else {
                    player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate does not exist: " + args[0]);
                }
            } else {
                return false;
            }
        } else {
            player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String[] arguments = CommandUtilities.commandEscaper(args);
        if ((arguments.length < 3) && (arguments.length > 0)) {
            return !CommandUtilities.playerCheck(sender) || doGo((Player) sender, arguments);
        }
        return false;
    }
}
