package net.doodcraft.Dooder07.Stargates.Wormhole.listeners;

import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.logging.Level;

public class StarGatesEntityListener implements Listener {

    private static boolean handleEntityExplodeEvent(final List<Block> explodeBlocks) {
        for (Block explodeBlock : explodeBlocks) {
            if (StargateManager.isBlockInGate(explodeBlock)) {
                final Stargate s = StargateManager.getGateFromBlock(explodeBlock);
                SGLogger.prettyLog(Level.FINE, false, "Blocked Creeper Explosion on Stargate: \"" + s.getGateName() + "\"");
                return true;
            }
        }
        return false;
    }

    private static boolean handlePlayerDamageEvent(final EntityDamageEvent event) {
        final Player p = (Player) event.getEntity();
        final Location current = p.getLocation();
        final Stargate closest = StargateManager.findClosestStargate(current);
        if ((closest != null) && (((closest.isGateCustom()
                ? closest.getGateCustomPortalMaterial()
                : closest.getGateShape() != null
                ? closest.getGateShape().getShapePortalMaterial()
                : Material.STATIONARY_WATER) == Material.STATIONARY_LAVA) || ((closest.getGateTarget() != null) && ((closest.getGateTarget().isGateCustom()
                ? closest.getGateTarget().getGateCustomPortalMaterial()
                : closest.getGateTarget().getGateShape() != null
                ? closest.getGateTarget().getGateShape().getShapePortalMaterial()
                : Material.STATIONARY_WATER) == Material.STATIONARY_LAVA)))) {
            final double blockDistanceSquared = StargateManager.distanceSquaredToClosestGateBlock(current, closest);
            if ((closest.isGateActive() || closest.isGateRecentlyActive()) && (((blockDistanceSquared <= (closest.isGateCustom()
                    ? closest.getGateCustomWooshDepthSquared()
                    : closest.getGateShape() != null
                    ? closest.getGateShape().getShapeWooshDepthSquared()
                    : 0)) && ((closest.isGateCustom()
                    ? closest.getGateCustomWooshDepth()
                    : closest.getGateShape() != null
                    ? closest.getGateShape().getShapeWooshDepth()
                    : 0) != 0)) || (blockDistanceSquared <= 16))) {
                SGLogger.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.getGateName() + "\" Proximity Event: \"" + event.getCause().toString() + "\" On: \"" + p.getName() + "\" Distance Squared: \"" + blockDistanceSquared + "\"");
                p.setFireTicks(0);
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!event.isCancelled() && (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA))) {
            if (event.getEntity() instanceof Player) {
                if (handlePlayerDamageEvent(event)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (!event.isCancelled()) {
            final List<Block> explodeBlocks = event.blockList();
            if (handleEntityExplodeEvent(explodeBlocks)) {
                event.setCancelled(true);
            }
        }
    }
}