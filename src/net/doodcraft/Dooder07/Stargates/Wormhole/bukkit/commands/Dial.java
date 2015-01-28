package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayer;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Dial implements CommandExecutor {

    private static boolean doDial(final Player player, final String[] args) {
        WormholePlayer wormholePlayer = WormholePlayerManager.getRegisteredWormholePlayer(player);
        Stargate sourceGate = wormholePlayer.getStargate();

        if ((sourceGate != null) && (sourceGate.isGateLightsActive())) {
            if (SGPermissions.checkPermission(player, sourceGate, PermissionType.DIALER)) {
                final String startnetwork = CommandUtilities.getGateNetwork(sourceGate);
                if (!sourceGate.getGateName().equals(args[0])) {
                    final Stargate target = StargateManager.getStargate(args[0]);

                    if (target == null) {
                        CommandUtilities.closeGate(sourceGate, false);
                        wormholePlayer.removeStargate(sourceGate);
                        
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());
                        return true;
                    }
                    
                    final String targetnetwork = CommandUtilities.getGateNetwork(target);
                    SGLogger.prettyLog(Level.FINE, false, "Dial Target - Gate: \"" + target.getGateName() + "\" Network: \"" + targetnetwork + "\"");

                    if (!startnetwork.equals(targetnetwork)) {
                        CommandUtilities.closeGate(sourceGate, false);
                        wormholePlayer.removeStargate(sourceGate);
                        
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString() + " Target is not on the same network!");
                        return true;
                    }
                    
                    if (sourceGate.isGateIrisActive()) {
                        sourceGate.toggleIrisActive(false);
                    }
                    
                    if (!target.getGateIrisDeactivationCode().equals("") && target.isGateIrisActive()) {
                        if ((args.length >= 2) && target.getGateIrisDeactivationCode().equals(args[1])) {
                            if (target.isGateIrisActive()) {
                                target.toggleIrisActive(false);
                                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "IDC accepted. Iris has been deactivated.");
                            }
                        }
                    }

                    if (sourceGate.dialStargate(target, false)) {
                        target.setLastUsedBy(player);
                        player.sendMessage(ConfigManager.MessageStrings.gateConnected.toString());
                    } else {
                        player.sendMessage(String.format(ConfigManager.MessageStrings.targetIsInUseBy.toString(), target.getGateName(), target.getLastUsedBy()));

                        CommandUtilities.closeGate(sourceGate, false);
                        wormholePlayer.removeStargate(sourceGate);
                    }
                } else {
                    player.sendMessage(ConfigManager.MessageStrings.targetIsSelf.toString());
                    CommandUtilities.closeGate(sourceGate, false);
                    wormholePlayer.removeStargate(sourceGate);
                }
            } else {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                wormholePlayer.removeStargate(sourceGate);
            }
        } else {
            player.sendMessage(ConfigManager.MessageStrings.gateNotActive.toString());
        }
        
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String[] arguments = CommandUtilities.commandEscaper(args);
        if ((arguments.length < 3) && (arguments.length > 0)) {
            return !CommandUtilities.playerCheck(sender) || doDial((Player) sender, arguments);
        }
        return false;
    }
}
