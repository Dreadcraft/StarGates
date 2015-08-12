package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;

public class SGReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtilities.playerCheck(sender) || SGPermissions.checkPermission((Player) sender, PermissionType.CONFIG)) {
            final String[] a = CommandUtilities.commandEscaper(args);
            if ((a.length > 4) || (a.length == 0))
                return false;
            
            if ((a[0].equalsIgnoreCase("n")) || (a[0].equalsIgnoreCase("now"))) {
                if (StarGates.getThisPlugin().reloadPlugin()) {
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "Reloading complete");
                } else {
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "Error during reload. See console logs..");
                }
            }
        } else {
            sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        
        return true;
    }
    
}
