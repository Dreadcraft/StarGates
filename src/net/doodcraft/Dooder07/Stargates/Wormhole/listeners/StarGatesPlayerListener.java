package net.doodcraft.Dooder07.Stargates.Wormhole.listeners;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateHelper;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateShape;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.StargateRestrictions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.PlayerOrientation;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayer;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.WorldUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;

import java.util.logging.Level;

public class StarGatesPlayerListener implements Listener {

    @SuppressWarnings("deprecation")
	protected static boolean buttonLeverHit(Player player, Block clickedBlock, BlockFace direction) {
        Stargate stargate = StargateManager.getGateFromBlock(clickedBlock);

        if (stargate != null) {
        	
            if (WorldUtils.isSameBlock(stargate.getGateDialLeverBlock(), clickedBlock) && ((stargate.isGateSignPowered() && SGPermissions.checkPermission(player, stargate, PermissionType.SIGN)) || (!stargate.isGateSignPowered() && SGPermissions.checkPermission(player, stargate, PermissionType.DIALER)))) {

                WormholePlayerManager.getRegisteredWormholePlayer(player).addStargate(stargate);
                WormholePlayer wormholePlayer = handleGateActivationSwitch(player);

                if (wormholePlayer == null)
                    return true;

                if ((!wormholePlayer.getProperties().hasReceivedRemoteActiveMessage()) && (wormholePlayer.getProperties().hasActivatedStargate())) {
                    if (wormholePlayer.getStargate().getGateName() == null ? stargate.getGateName() != null : !wormholePlayer.getStargate().getGateName().equals(stargate.getGateName())) {

                        if (!wormholePlayer.getStargate().isGateActive()) {
                            SGLogger.prettyLog(Level.FINE, false, "Gate '" + wormholePlayer.getStargate().getGateName() + "' didnt light up.");
                        }

                        SGLogger.prettyLog(Level.FINE, false, "New gate for player '" + player.getName() + "' was set to stargate '" + wormholePlayer.getStargate().getGateName() + "'");
                    }
                } else {
                    SGLogger.prettyLog(Level.FINE, false, "Gate '" + stargate.getGateName() + "' was remote active for player '" + player.getName() + "': no permission, invalid gate target");
                    wormholePlayer.getProperties().setHasReceivedRemoteActiveMessage(true);
                }

                if ((wormholePlayer.getProperties().hasShutdownGate()) || (wormholePlayer.getProperties().hasReceivedIrisLockMessage()) ||
                    (!wormholePlayer.getProperties().hasActivatedStargate()) || (wormholePlayer.getProperties().hasReceivedRemoteActiveMessage())) {
                    wormholePlayer.removeStargate(stargate);
                }
            } else if (WorldUtils.isSameBlock(stargate.getGateIrisLeverBlock(), clickedBlock) && (!stargate.isGateSignPowered() && SGPermissions.checkPermission(player, stargate, PermissionType.DIALER))) {
                Lever lever = new Lever(clickedBlock.getType(), clickedBlock.getData());
                SGLogger.prettyLog(Level.FINE, false, "Player '" + player.getName() + "' has triggered the iris lever of gate '" + stargate.getGateName() + "' status is now " + (!lever.isPowered()));
                stargate.toggleIrisActive(true);
            } else if (WorldUtils.isSameBlock(stargate.getGateIrisLeverBlock(), clickedBlock) || WorldUtils.isSameBlock(stargate.getGateDialLeverBlock(), clickedBlock)) {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());


                WormholePlayerManager.getRegisteredWormholePlayer(player).removeStargate(stargate);
            }

            return true;
        } else {
            if (direction == null) {
                Button directionButton = new Button(Material.STONE_BUTTON);
                directionButton.setData(clickedBlock.getData());

                direction = directionButton.getFacing();

                if (direction == null) {
                    return false;
                }
            }

            StargateShape shape = StargateManager.getPlayerBuilderShape(player);

            Stargate newGate;
            if (shape != null) {
                newGate = StargateHelper.checkStargate(clickedBlock, direction, shape);
            } else {
                SGLogger.prettyLog(Level.FINEST, false, "Attempting to find any gate shapes!");
                newGate = StargateHelper.checkStargate(clickedBlock, direction);
            }

            if (newGate != null) {
                if (SGPermissions.checkPermission(player, newGate, PermissionType.BUILD) && !StargateRestrictions.isPlayerBuildRestricted(player)) {
                    if (newGate.isGateSignPowered()) {
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargate Design Valid with Sign Nav.");
                        if (newGate.getGateName().equals("")) {
                            player.sendMessage(ConfigManager.MessageStrings.constructNameInvalid.toString() + "\"\"");
                        } else {
                            boolean success = StargateManager.completeStargate(player, newGate);
                            if (success) {
                                player.sendMessage(ConfigManager.MessageStrings.constructSuccess.toString());
                                newGate.getGateDialSign().setLine(0, "-" + newGate.getGateName() + "-");
                                newGate.getGateDialSign().setData(newGate.getGateDialSign().getData());
                                newGate.getGateDialSign().update();
                            } else {
                                player.sendMessage("Stargate constrution failed!?");
                            }
                        }

                    } else {

                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid Stargate Design! \u00A73:: \u00A7B<required> \u00A76[optional]");
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/sgcomplete \u00A7B<name> \u00A76[idc=IDC] [net=NET]\u00A77\' to complete.");

                        StargateManager.addIncompleteStargate(player.getName(), newGate);
                    }
                    return true;
                } else {
                    if (newGate.isGateSignPowered()) {
                        newGate.resetTeleportSign();
                    }
                    StargateManager.removeIncompleteStargate(player);
                    if (StargateRestrictions.isPlayerBuildRestricted(player)) {
                        player.sendMessage(ConfigManager.MessageStrings.playerBuildCountRestricted.toString());
                    }
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return true;
                }
            } else {
                SGLogger.prettyLog(Level.FINE, false, player.getName() + " pressed a button or lever but we did not find a valid gate shape");
                //player.sendMessage(ConfigManager.MessageStrings.gateWithInvalidShape.toString() + ConfigManager.MessageStrings.gateWithInvalidShapeAssistance);
            }
        }
        return false;
    }

    protected static WormholePlayer handleGateActivationSwitch(Player player) {
        WormholePlayer wormholePlayer = WormholePlayerManager.getRegisteredWormholePlayer(player);

        if ((wormholePlayer == null) || (wormholePlayer.getStargate() == null))
            return null;

        Stargate currentGate = wormholePlayer.getStargate();

        if (currentGate.isGateActive() || currentGate.isGateLightsActive()) {

            if (currentGate.getGateTarget() != null) {

                currentGate.shutdownStargate(true);
                player.sendMessage(String.format(ConfigManager.MessageStrings.gateShutdown.toString(), currentGate.getGateName() + " "));

                wormholePlayer.getProperties().setHasShutdownGate(true);

                return wormholePlayer;
            } else {

                if ((currentGate.getSourceGateName() == null) && (currentGate.isGateActive())) {
                    currentGate.stopActivationTimer();
                    currentGate.setGateActive(false);
                    currentGate.toggleDialLeverState(false);
                    currentGate.lightStargate(false);

                    wormholePlayer.getProperties().setHasActivatedStargate(false);
                    currentGate.setLastUsedBy(wormholePlayer.getPlayer());
                    player.sendMessage(String.format(ConfigManager.MessageStrings.gateDeactivated.toString(), currentGate.getGateName() + " "));

                    return wormholePlayer;
                } else {
                    if (currentGate.isGateLightsActive() && (!currentGate.isGateActive()) && (currentGate.getLastUsedBy() != wormholePlayer.getName())) {
                        player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate has been activated by '" + currentGate.getLastUsedBy() + "' already.");
                        wormholePlayer.getProperties().setHasReceivedWasActivatedOther(true);
                    } else if (currentGate.isGateLightsActive() && (!currentGate.isGateActive()) && (currentGate.getLastUsedBy().equals(wormholePlayer.getName()))) {
                        currentGate.stopActivationTimer();
                        currentGate.setGateActive(false);
                        currentGate.toggleDialLeverState(false);
                        currentGate.lightStargate(false);

                        wormholePlayer.getProperties().setHasActivatedStargate(false);
                        currentGate.setLastUsedBy(wormholePlayer.getPlayer());
                        player.sendMessage(String.format(ConfigManager.MessageStrings.gateDeactivated.toString(), currentGate.getGateName() + " "));
                    } else {
                        wormholePlayer.getProperties().setHasReceivedRemoteActiveMessage(true);
                        Stargate sourceGate = StargateManager.getStargate(currentGate.getSourceGateName());
                        if (sourceGate != null) {
                            player.sendMessage(String.format(ConfigManager.MessageStrings.gateRemoteActive.toString(), currentGate.getGateName()+" ", " by " + sourceGate.getLastUsedBy()));
                            player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Detected Wormhole source " + currentGate.getSourceGateName());
                        }
                    }

                    return wormholePlayer;
                }
            }
        } else {
            if (currentGate.isGateSignPowered()) {
                if (SGPermissions.checkPermission(player, currentGate, PermissionType.SIGN)) {
                    if ((currentGate.getGateDialSign() == null) && (currentGate.getGateDialSignBlock() != null)) {
                        currentGate.tryClickTeleportSign(currentGate.getGateDialSignBlock());
                    }

                    if (currentGate.getGateDialSignTarget() != null) {
                        if (currentGate.dialStargate(currentGate.getGateDialSignTarget(), false)) {
                            player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargates connected!");

                            wormholePlayer.getProperties().setHasActivatedStargate(true);
                            currentGate.setLastUsedBy(player.getName());
                            SGLogger.prettyLog(Level.FINE, false, "Player '" + currentGate.getLastUsedBy() + "' has activated gate '" + wormholePlayer.getStargate().getGateName() + "'");

                            return wormholePlayer;
                        } else {
                            Stargate targetGate = StargateManager.getStargate(currentGate.getGateDialSignTarget().getGateName());
                            player.sendMessage(String.format(ConfigManager.MessageStrings.gateRemoteActive.toString(), targetGate.getGateName()+" ", " by " + StargateManager.getStargate(targetGate.getSourceGateName()).getLastUsedBy()));
                            player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Detected Wormhole source " + targetGate.getSourceGateName());

                            wormholePlayer.getProperties().setHasReceivedRemoteActiveMessage(true);

                            return wormholePlayer;
                        }
                    } else {
                        // TODO: check if this is still needed to catch cause all sign powered gates have now a target after load.
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());

                        wormholePlayer.getProperties().setHasReceivedInvalidTargetMessage(true);

                        return wormholePlayer;
                    }
                } else {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());

                    wormholePlayer.getProperties().setHasReceivedNoPermissionMessage(true);

                    return wormholePlayer;
                }
            } else {

                player.sendMessage(String.format(ConfigManager.MessageStrings.gateActivated.toString(), currentGate.getGateName() + " "));
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Chevrons locked! \u00A73:: \u00A7B<required> \u00A76[optional]");
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/dial \u00A7B<GateName> \u00A76[idc]\u00A77\'");

                StargateManager.addActivatedStargate(currentGate);
                currentGate.startActivationTimer(player);
                currentGate.lightStargate(true);

                wormholePlayer.getProperties().setHasActivatedStargate(true);

                currentGate.setLastUsedBy(player.getName());

                return wormholePlayer;
            }
        }
    }

    protected static boolean handlePlayerInteractEvent(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        Action clickedBlockAction = event.getAction();
        Player player = event.getPlayer();

        if ((clickedBlock != null) && ((clickedBlock.getType().equals(Material.STONE_BUTTON)) || (clickedBlock.getType().equals(Material.LEVER)))) {

            if (((clickedBlock.getType().equals(Material.LEVER)) && (!SGPermissions.checkPermission(player, PermissionType.USE))) ||
                ((clickedBlock.getType().equals(Material.STONE_BUTTON)) && (!SGPermissions.checkPermission(player, PermissionType.BUILD)))) {
                return false;
            }

            if (buttonLeverHit(player, clickedBlock, null)) {
                return true;
            }
        } else if ((clickedBlock != null) && (clickedBlock.getType().equals(Material.WALL_SIGN))) {
            //@TODO refactor permission levels to be able to check for permissions first
            Stargate stargate = StargateManager.getGateFromBlock(clickedBlock);
            if (stargate != null) {
                if (!SGPermissions.checkPermission(player, stargate, PermissionType.SIGN)) {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return true;
                }

                stargate.setLastUsedBy(player.getName());
                if (stargate.tryClickTeleportSign(clickedBlock, clickedBlockAction)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasChangedBlockCoordinates(final Location fromLoc, final Location toLoc) {
        return !(fromLoc.getWorld().equals(toLoc.getWorld())
                && fromLoc.getBlockX() == toLoc.getBlockX()
                && fromLoc.getBlockY() == toLoc.getBlockY()
                && fromLoc.getBlockZ() == toLoc.getBlockZ());
    }

    @SuppressWarnings({ "deprecation", "incomplete-switch" })
	protected WormholePlayer handlePlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location toLocFinal = event.getTo();
        Block gateBlockFinal = toLocFinal.getWorld().getBlockAt(toLocFinal.getBlockX(), toLocFinal.getBlockY(), toLocFinal.getBlockZ());

        Stargate stargate = StargateManager.getGateFromBlock(gateBlockFinal);

        if ((stargate != null) && (stargate.isGateActive()) && (stargate.getGateTarget() != null) && (gateBlockFinal.getTypeId() == (stargate.isGateCustom()
                ? stargate.getGateCustomPortalMaterial().getId()
                : stargate.getGateShape() != null
                ? stargate.getGateShape().getShapePortalMaterial().getId()
                : Material.STATIONARY_WATER.getId())) 
                && (!WormholePlayerManager.getRegisteredWormholePlayer(player).getProperties(stargate).hasUsedStargate())) {

            WormholePlayer wormholePlayer = WormholePlayerManager.getRegisteredWormholePlayer(player);

            wormholePlayer.addStargate(stargate);
            wormholePlayer.setCurrentGateName(stargate.getGateName());

            String gateNetwork;
            if (stargate.getGateNetwork() != null) {
                gateNetwork = stargate.getGateNetwork().getNetworkName();
            } else {
                gateNetwork = "Public";
            }

            SGLogger.prettyLog(Level.FINE, false, "Player in gate: " + stargate.getGateName() + " gate Active: " + stargate.isGateActive() + " Target Gate: " + stargate.getGateTarget().getGateName() + " Network: " + gateNetwork);

            if (!wormholePlayer.getProperties().hasReceivedIrisLockMessage()) {
                if (ConfigManager.getWormholeUseIsTeleport() && ((stargate.isGateSignPowered() && !SGPermissions.checkPermission(player, stargate, PermissionType.SIGN)) || (!stargate.isGateSignPowered() && !SGPermissions.checkPermission(player, stargate, PermissionType.DIALER)))) {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());

                    wormholePlayer.getProperties().setHasPermission(false);
                    return wormholePlayer;
                }

                if (ConfigManager.isUseCooldownEnabled()) {
                    if (StargateRestrictions.isPlayerUseCooldown(player)) {
                        player.sendMessage(ConfigManager.MessageStrings.playerUseCooldownRestricted.toString());
                        player.sendMessage(ConfigManager.MessageStrings.playerUseCooldownWaitTime.toString() + StargateRestrictions.checkPlayerUseCooldownRemaining(player));

                        wormholePlayer.getProperties().setHasUseCooldown(true);

                        return wormholePlayer;
                    } else {
                        StargateRestrictions.addPlayerUseCooldown(player);
                    }
                }

                if ((stargate.getGateTarget().isGateIrisActive()) && (!wormholePlayer.getProperties().hasReceivedIrisLockMessage())) {
                    player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Remote Iris is locked!");
                    wormholePlayer.getProperties().setHasReceivedIrisLockMessage(true);
                    int wkbCount = ConfigManager.getWormholeKickbackBlockCount();

                    if (wkbCount > 0) {

                        player.setNoDamageTicks(5);

                        PlayerOrientation direction = wormholePlayer.getKickBackDirection(wormholePlayer.getStargate().getGateFacing().getOppositeFace());

                        double pLocX = wormholePlayer.getPlayer().getLocation().getX();
                        double pLocY = wormholePlayer.getPlayer().getLocation().getY();
                        double pLocZ = wormholePlayer.getPlayer().getLocation().getZ();

                        SGLogger.prettyLog(Level.FINE, false, "PlayerOrientation: " + direction.getName());
                        SGLogger.prettyLog(Level.FINE, false, "old X:"+pLocX+", Y:"+pLocY+", Z:"+pLocZ);

                        switch (direction) {
                            case NORTH:
                                SGLogger.prettyLog(Level.FINE, false, "NORTH: " + pLocZ + " - 2 = " + (pLocX-2d));
                                pLocZ -= (double) wkbCount;
                                break;
                            case SOUTH:
                                SGLogger.prettyLog(Level.FINE, false, "SOUTH: " + pLocZ + " + 2 = " + (pLocX+2d));
                                pLocZ += (double) wkbCount;
                                break;                        
                            case EAST:
                                SGLogger.prettyLog(Level.FINE, false, "EAST: " + pLocX + " + 2 = " + (pLocZ-2d));
                                pLocX += (double) wkbCount;
                                break;
                            case WEST:
                                SGLogger.prettyLog(Level.FINE, false, "WEST: " + pLocX + " - 2 = " + (pLocZ+2d));
                                pLocX -= (double) wkbCount;
                                break;
                        }

                        Location newLoc = new Location(player.getWorld(), pLocX, pLocY, pLocZ, player.getLocation().getYaw(), player.getLocation().getPitch());
                        pLocY = player.getWorld().getHighestBlockYAt(newLoc);

                        if (ConfigManager.getGateTransportMethod()) {
                            event.setTo(newLoc);
                            SGLogger.prettyLog(Level.FINE, false, "Player was kicked back via event");
                        } else {
                            player.teleport(newLoc);
                            SGLogger.prettyLog(Level.FINE, false, "Player was kicked back via teleport");

                        }

                        SGLogger.prettyLog(Level.FINE, false, "new X:"+pLocX+", Y:"+pLocY+", Z:"+pLocZ);
                    }

                    return wormholePlayer;
                }

                Location target = stargate.getGateTarget().getGatePlayerTeleportLocation();
                player.setNoDamageTicks(5);
                if (ConfigManager.getGateTransportMethod()) {
                    event.setTo(target);
                    SGLogger.prettyLog(Level.FINE, false, "Player was transported via event");
                } else {
                    player.teleport(target);
                    SGLogger.prettyLog(Level.FINE, false, "Player was transported via teleport");
                }

                if ((target != stargate.getGatePlayerTeleportLocation()) && (!wormholePlayer.getProperties().hasUsedStargate())) {
                    SGLogger.prettyLog(Level.INFO, false, player.getName() + " used wormhole: " + stargate.getGateName() + " to go to: " + stargate.getGateTarget().getGateName());
                    wormholePlayer.getProperties().setHasUsedStargate(true);
                }

                if (ConfigManager.getTimeoutShutdown() == 0) {
                    stargate.shutdownStargate(true);
                }
            } else {
                SGLogger.prettyLog(Level.FINE, false, "Player '" + player.getName() + "' has received IRISLOCK_MESSASGE unlocking player.");
                wormholePlayer.removeStargate(stargate);
            }

            return wormholePlayer;
        } else if ((stargate != null) && (WormholePlayerManager.getRegisteredWormholePlayer(player).getProperties(stargate).hasReachedDestination())) {
            // @TODO: can be used for later stargate monitoring
            SGLogger.prettyLog(Level.FINE, false, "Player '" + player.getName() + "' has safely reached destination.");
            WormholePlayerManager.getRegisteredWormholePlayer(player).removeStargate(stargate);
        }

        return null;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!event.isCancelled()) {
            Stargate stargate = StargateManager.getGateFromBlock(event.getBlockClicked());
            if ((stargate != null) || StargateManager.isBlockInGate(event.getBlockClicked())) {
                event.setCancelled(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.player.PlayerListener#onPlayerBucketFill(org.bukkit.event.player.PlayerBucketFillEvent)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!event.isCancelled()) {
            Stargate stargate = StargateManager.getGateFromBlock(event.getBlockClicked());
            if ((stargate != null) || StargateManager.isBlockInGate(event.getBlockClicked())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            SGLogger.prettyLog(Level.FINE, false, "InteractEvent was canceled");
            return;
        }
        
        if (event.getClickedBlock() != null) {
            SGLogger.prettyLog(Level.FINE, false, "Caught Player: \"" + event.getPlayer().getName() + "\" Event Name: \"" + event.getEventName() + "\" Action Type: \"" + event.getAction().toString() + "\" Event Block Type: \"" + event.getClickedBlock().getType().toString() + "\" Event World: \"" + event.getClickedBlock().getWorld().toString() + "\" Event Block: " + event.getClickedBlock().toString() + "\"");

            if (handlePlayerInteractEvent(event)) {
                event.setCancelled(true);
                SGLogger.prettyLog(Level.FINE, false, "Cancelled Player: \"" + event.getPlayer().getName() + "\" Event Name: \"" + event.getEventName() + "\" Action Type: \"" + event.getAction().toString() + "\" Event Block Type: \"" + event.getClickedBlock().getType().toString() + "\" Event World: \"" + event.getClickedBlock().getWorld().toString() + "\" Event Block: " + event.getClickedBlock().toString() + "\"");
            }
        } else {
            SGLogger.prettyLog(Level.FINE, false, "Caught and ignored Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getEventName() + "\"");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!this.hasChangedBlockCoordinates(event.getFrom(), event.getTo())) {
            return;
        }

        WormholePlayer wormholePlayer = handlePlayerMoveEvent(event);
        Player player = event.getPlayer();

        if (!WormholePlayerManager.isRegistered(player))
            return;

        if ((wormholePlayer != null) && (wormholePlayer.getProperties() != null) && (wormholePlayer.getProperties().hasUsedStargate())) {
            if (ConfigManager.isGateArrivalWelcomeMessageEnabled()) {
                player.sendMessage(
                        String.format(ConfigManager.MessageStrings.playerUsedStargate.toString(),
                        "Gate " + wormholePlayer.getStargate().getGateTarget().getGateName(),
                        " - created by " + wormholePlayer.getStargate().getGateTarget().getGateOwner()));
                SGLogger.prettyLog(Level.FINE, false, "has received SHOW_GATE_WELCOME_MESSAGE");
            } else{
                SGLogger.prettyLog(Level.FINE, false, "has disabled SHOW_GATE_WELCOME_MESSAGE");
            }

            wormholePlayer.getProperties().setHasReachedDestination(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SGLogger.prettyLog(Level.FINE, false, "Player '" + player.getName() + "' joined the server. Adding player to keyring.");
        WormholePlayerManager.registerPlayer(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        SGLogger.prettyLog(Level.FINE, false, "Player '" + player.getName() + "' quit. Removing player from keyring.");
        WormholePlayerManager.unregisterPlayer(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {

        Player player = event.getPlayer();

        SGLogger.prettyLog(Level.FINE, false, "Player '" + player.getName() + "' was kicked. Removing player from keyring.");
        WormholePlayerManager.unregisterPlayer(player);
    }
}
