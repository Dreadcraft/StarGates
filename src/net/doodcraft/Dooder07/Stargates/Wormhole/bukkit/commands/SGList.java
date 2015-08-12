package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import java.util.ArrayList;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;

public class SGList implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!CommandUtilities.playerCheck(sender) || SGPermissions.checkPermission((Player) sender, PermissionType.LIST)) {
            final ArrayList<Stargate> gates = StargateManager.getAllGates();
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Available gates \u00A73::");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < gates.size(); i++) {
                sb.append("\u00A77").append(gates.get(i).getGateName());
                if (i != gates.size() - 1) {
                    sb.append("\u00A78, ");
                }
                if (sb.toString().length() >= 75) {
                    sender.sendMessage(sb.toString());
                    sb = new StringBuilder();
                }
            }
            if (!sb.toString().equals("")) {
                sender.sendMessage(sb.toString());
            }

        } else {
            sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        return true;
    }
}
