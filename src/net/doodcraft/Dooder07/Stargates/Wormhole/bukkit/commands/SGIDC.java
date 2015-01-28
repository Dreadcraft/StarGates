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

public class SGIDC implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String[] a = CommandUtilities.commandEscaper(args);
        if (a.length >= 1) {

            if (StargateManager.isStargate(a[0])) {
                final Stargate s = StargateManager.getStargate(a[0]);
                if (!s.isGateSignPowered() && (s.getGateIrisLeverBlock() != null)) {
                    if (!CommandUtilities.playerCheck(sender) || (SGPermissions.checkPermission((Player) sender, PermissionType.CONFIG) || ((s.getGateOwner() != null) && s.getGateOwner().equals(((Player) sender).getName())))) {
               
                        if (a.length >= 2) {
                            if (a[1].equals("-clear")) {

                                StargateManager.removeBlockIndex(s.getGateIrisLeverBlock());

                                s.setIrisDeactivationCode("");
                            } else {

                                s.setIrisDeactivationCode(a[1]);

                                StargateManager.addBlockIndex(s.getGateIrisLeverBlock(), s);
                            }
                        }

                        sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "IDC for gate: " + s.getGateName() + " is: " + s.getGateIrisDeactivationCode());
                    } else {
                        sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    }
                } else {
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Iris is not enabled for sign powered stargates or gates without an iris activation block.");
                }
            } else {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid Stargate: " + a[0]);

            }
            return true;
        }
        return false;
    }
}
