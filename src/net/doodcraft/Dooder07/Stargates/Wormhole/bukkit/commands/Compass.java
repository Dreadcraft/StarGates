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

public class Compass implements CommandExecutor {

    private static boolean doCompass(final Player player) {
        if (SGPermissions.checkPermission(player, PermissionType.COMPASS)) {
            final Stargate closest = StargateManager.findClosestStargate(player.getLocation());
            if (closest != null) {
                player.setCompassTarget(closest.getGatePlayerTeleportLocation());
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Compass set to nearest stargate: " + closest.getGateName());
            } else {
                player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "No stargates found to track!");
            }
        } else {
            player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return !CommandUtilities.playerCheck(sender) || doCompass((Player) sender);
    }
}
