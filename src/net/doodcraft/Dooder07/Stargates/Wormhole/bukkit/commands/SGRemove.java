package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SGRemove implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String[] a = CommandUtilities.commandEscaper(args);
        if ((a.length >= 1) && (a.length <= 2)) {
            if (a[0].equalsIgnoreCase("-all")) {
                return false;
            }
            
            final Stargate s = StargateManager.getStargate(a[0]);

            if (s != null) {
                if (!CommandUtilities.playerCheck(sender) || SGPermissions.checkPermission((Player) sender, s, PermissionType.REMOVE)) {
                    boolean destroy = false;
                    if ((a.length == 2) && a[1].equalsIgnoreCase("-all")) {
                        destroy = true;
                    }
                    CommandUtilities.gateRemove(s, destroy);

                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Wormhole removed: " + s.getGateName());
                } else {
                    sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                }

            } else {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate does not exist: " + a[0] + ". Names are case-sensitive.");
            }
        } else {
            return false;
        }
        return true;
    }
}
