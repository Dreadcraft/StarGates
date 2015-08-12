package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import java.util.Arrays;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class Force implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String[] a = CommandUtilities.commandEscaper(args);
        if (a.length == 1) {
            if (!CommandUtilities.playerCheck(sender) || SGPermissions.checkPermission((Player) sender, PermissionType.CONFIG)) {
                if (a[0].equalsIgnoreCase("-all")) {
                    for (final Stargate gate : StargateManager.getAllGates()) {
                        CommandUtilities.closeGate(gate, true);
                    }
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "All gates have been deactivated, darkened, and have had their iris (if any) opened.");
                } else if (StargateManager.isStargate(a[0])) {
                    CommandUtilities.closeGate(StargateManager.getStargate(a[0]), true);
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + a[0] + " has been closed, darkened, and has had its iris (if any) opened.");
                } else {
                    sender.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());
                    return false;
                }

                if (CommandUtilities.playerCheck(sender)) {
                    SGLogger.prettyLog(Level.INFO, false, "Player: \"" + sender.getName() + "\" ran sgforce: " + Arrays.toString(a));
                }
            } else {
                sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
            return true;
        } else {
            return false;
        }
    }
}
