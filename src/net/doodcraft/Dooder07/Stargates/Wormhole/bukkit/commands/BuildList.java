package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateHelper;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildList implements CommandExecutor {

    private static boolean listBuilds(final Player player, final String[] args) {
        if (!SGPermissions.checkPermission(player, PermissionType.CONFIG)) {
            return false;
        }

        int gateID = 1;
        StringBuilder shapeNames = new StringBuilder();
        for (String shapeName : StargateHelper.getShapeNames()) {
            shapeNames.append(ChatColor.GREEN + "(" + gateID + ")").append(ChatColor.GRAY + shapeName).append(", ");
            gateID++;
        }
        shapeNames.delete(shapeNames.length()-2, shapeNames.length());

        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Available Shapes: " + shapeNames);
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (CommandUtilities.playerCheck(sender)) {
            final String[] arguments = CommandUtilities.commandEscaper(args);
            final Player player = (Player) sender;
            return listBuilds(player, arguments);
        }

        return true;
    }
}
