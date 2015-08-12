package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateDBManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.plugin.WormholeWorldsSupport;

public class SGStatus implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtilities.playerCheck(sender) || SGPermissions.checkPermission((Player) sender, PermissionType.CONFIG)) {
            final String[] a = CommandUtilities.commandEscaper(args);
            if ((a.length > 4) || (a.length == 0))
                return false;
            
            if ((args[0].equalsIgnoreCase("a")) || (args[0].equalsIgnoreCase("all"))) {
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "\u00A76----------------------------");
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "System status");
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "\u00A76----------------------------");
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "DBConnection: " + ((StargateDBManager.isConnected()) ? "\u00A72ready" : "\u00A74failed"));
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader + "WXW-link: " + ((WormholeWorldsSupport.isEnabled()) ? "\u00A72ready" : "\u00A74failed"));
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString());
            }
        } else {
            sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        
        return true;
    }
    
}
