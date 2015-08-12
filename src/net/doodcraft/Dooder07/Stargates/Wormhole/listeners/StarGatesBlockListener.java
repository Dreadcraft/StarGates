package net.doodcraft.Dooder07.Stargates.Wormhole.listeners;

import java.util.logging.Level;

import javax.xml.stream.Location;

import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.WorldUtils;

public class StarGatesBlockListener implements Listener {

    @SuppressWarnings("deprecation")
	private static boolean handleBlockBreak(final Player player, final Stargate stargate, final Block block) {
        final boolean allowed = SGPermissions.checkPermission(player, stargate, PermissionType.DAMAGE);
        if (allowed) {
            if (!WorldUtils.isSameBlock(stargate.getGateDialLeverBlock(), block)) {
                if ((stargate.getGateDialSignBlock() != null) && WorldUtils.isSameBlock(stargate.getGateDialSignBlock(), block)) {
                    player.sendMessage("Destroyed DHD Sign. You will be unable to change dialing target from this gate.");
                    player.sendMessage("You can rebuild it later.");
                    stargate.setGateDialSign(null);
                } else if (block.getTypeId() == (stargate.isGateCustom()
                        ? stargate.getGateCustomIrisMaterial().getId()
                        : stargate.getGateShape() != null
                        ? stargate.getGateShape().getShapeIrisMaterial().getId()
                        : 1)) {
                    return true;
                } else {
                    if (stargate.isGateActive()) {
                        stargate.setGateActive(false);
                        stargate.fillGateInterior(Material.AIR);
                    }
                    if (stargate.isGateLightsActive()) {
                        stargate.lightStargate(false);
                        stargate.stopActivationTimer();
                        
                        StargateManager.removeActivatedStargate(stargate.getGateName());
                    }
                    stargate.resetTeleportSign();
                    stargate.setupGateSign(false);
                    if (!stargate.getGateIrisDeactivationCode().equals("")) {
                        stargate.setupIrisLever(false);
                    }
                    if (stargate.isGateRedstonePowered()) {
                        stargate.setupRedstone(false);
                    }
                    StargateManager.removeStargate(stargate);
                    player.sendMessage("Stargate Destroyed: " + stargate.getGateName());
                }
            } else {
                player.sendMessage("Destroyed DHD. You will be unable to dial out from this gate.");
                player.sendMessage("You can rebuild it later.");
            }
            return false;
        } else {
            if (player != null) {
                SGLogger.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied block destroy on: " + stargate.getGateName());
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            final Block block = event.getBlock();
            final Stargate stargate = StargateManager.getGateFromBlock(block);
            final Player player = event.getPlayer();
            if ((stargate != null) && handleBlockBreak(player, stargate, block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled()) {
            final Location current = event.getBlock().getLocation();
            final Stargate closest = StargateManager.findClosestStargate(current);
            //TODO This is bad, very bad for performance!
            if ((closest != null) && (closest.isGateActive() || closest.isGateRecentlyActive()) && ((closest.isGateCustom()
                    ? closest.getGateCustomPortalMaterial()
                    : closest.getGateShape() != null
                    ? closest.getGateShape().getShapePortalMaterial()
                    : Material.STATIONARY_WATER) == Material.STATIONARY_LAVA)) {
                final double blockDistanceSquared = StargateManager.distanceSquaredToClosestGateBlock(current, closest);
                if (((blockDistanceSquared <= (closest.isGateCustom()
                        ? closest.getGateCustomWooshDepthSquared()
                        : closest.getGateShape() != null
                        ? closest.getGateShape().getShapeWooshDepthSquared()
                        : 0)) && ((closest.isGateCustom()
                        ? closest.getGateCustomWooshDepth()
                        : closest.getGateShape() != null
                        ? closest.getGateShape().getShapeWooshDepth()
                        : 0) != 0)) || (blockDistanceSquared <= 25)) {
                    SGLogger.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.getGateName() + "\" Proximity Block Burn Distance Squared: \"" + blockDistanceSquared + "\"");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!event.isCancelled()) {
            final Stargate stargate = StargateManager.getGateFromBlock(event.getBlock());
            final Player player = event.getPlayer();
            if ((stargate != null) && (player != null) && !SGPermissions.checkPermission(player, stargate, PermissionType.DAMAGE)) {
                event.setCancelled(true);
                SGLogger.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied damage on: " + stargate.getGateName());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!event.isCancelled()) {
            if (StargateManager.isBlockInGate(event.getToBlock()) || StargateManager.isBlockInGate(event.getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!event.isCancelled()) {
            final Location current = event.getBlock().getLocation();
            final Stargate closest = StargateManager.findClosestStargate(current);
            if ((closest != null) && (closest.isGateActive() || closest.isGateRecentlyActive()) && ((closest.isGateCustom()
                    ? closest.getGateCustomPortalMaterial()
                    : closest.getGateShape() != null
                    ? closest.getGateShape().getShapePortalMaterial()
                    : Material.STATIONARY_WATER) == Material.STATIONARY_LAVA)) {
                final double blockDistanceSquared = StargateManager.distanceSquaredToClosestGateBlock(current, closest);
                if (((blockDistanceSquared <= (closest.isGateCustom()
                        ? closest.getGateCustomWooshDepthSquared()
                        : closest.getGateShape().getShapeWooshDepthSquared())) && ((closest.isGateCustom()
                        ? closest.getGateCustomWooshDepth()
                        : closest.getGateShape() != null
                        ? closest.getGateShape().getShapeWooshDepth()
                        : 0) != 0)) || (blockDistanceSquared <= 25)) {
                    SGLogger.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.getGateName() + "\" Block Type: \"" + event.getBlock().getType().toString() + "\" Proximity Block Ignite: \"" + event.getCause().toString() + "\" Distance Squared: \"" + blockDistanceSquared + "\"");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!event.isCancelled()) {
            final Block block = event.getBlock();
            if (StargateManager.isBlockInGate(block) && (!block.getType().equals(Material.REDSTONE_WIRE))) {
                event.setCancelled(true);
            }
        }
    }
}
