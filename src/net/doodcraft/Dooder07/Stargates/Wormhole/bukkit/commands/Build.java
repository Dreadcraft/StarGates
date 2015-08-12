package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateHelper;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGStringUtils;

public class Build implements CommandExecutor {

    private static boolean doBuild(final Player player, final String[] args) {
        if (args.length == 1) {
            if ((!SGPermissions.checkPermission(player, PermissionType.CONFIG)) || (!SGPermissions.checkPermission(player, PermissionType.BUILD))) {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                return true;
            }

            String shapeName = args[0];

            if (SGStringUtils.isIntNumber(shapeName)) {
                int sCount = 1;
                int sCEnd = Integer.parseInt(shapeName);
                for (String sName: StargateHelper.getShapeNames()) {
                    if (sCount >= sCEnd) {
                        shapeName = sName;
                        break;
                    }
                    sCount++;
                }
            }

            if (StargateHelper.isStargateShape(shapeName)) {
                StargateManager.addPlayerBuilderShape(player.getName(), StargateHelper.getStargateShape(shapeName));
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Press activation button on a new DHD to autobuild Stargate in the shape of: " + StargateHelper.getStargateShapeName(shapeName));
            } else {
                player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid shape: " + shapeName);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (CommandUtilities.playerCheck(sender)) {
            final String[] arguments = CommandUtilities.commandEscaper(args);
            if ((arguments.length < 3) && (arguments.length > 0)) {
                final Player player = (Player) sender;
                return doBuild(player, arguments);
            }
            return false;
        }
        return true;
    }
}
