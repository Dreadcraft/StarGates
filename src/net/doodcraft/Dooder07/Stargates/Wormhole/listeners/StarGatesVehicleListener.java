package net.doodcraft.Dooder07.Stargates.Wormhole.listeners;

import java.util.Vector;
import java.util.logging.Level;

import javax.xml.stream.Location;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.events.StargateMinecartTeleportEvent;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.StargateRestrictions;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class StarGatesVehicleListener implements Listener {

    private final static Vector nospeed = new Vector();

    @SuppressWarnings({ "incomplete-switch", "deprecation" })
	private static boolean handleStargateMinecartTeleportEvent(final VehicleMoveEvent event) {
        final Location l = event.getTo();
        final Block ch = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        final Stargate st = StargateManager.getGateFromBlock(ch);
        if ((st != null) && st.isGateActive() && (st.getGateTarget() != null) && (ch.getTypeId() == (st.isGateCustom()
                ? st.getGateCustomPortalMaterial().getId()
                : st.getGateShape() != null
                ? st.getGateShape().getShapePortalMaterial().getId()
                : 9))) {
            String gatenetwork;
            if (st.getGateNetwork() != null) {
                gatenetwork = st.getGateNetwork().getNetworkName();
            } else {
                gatenetwork = "Public";
            }
            
            Location target = st.getGateTarget().getGateMinecartTeleportLocation() != null
                    ? st.getGateTarget().getGateMinecartTeleportLocation()
                    : st.getGateTarget().getGatePlayerTeleportLocation();
            final Minecart veh = (Minecart) event.getVehicle();
            final Vector v = veh.getVelocity();
            veh.setVelocity(nospeed);
            final Entity e = veh.getPassenger();
            if ((e != null) && (e instanceof Player)) {
                final Player p = (Player) e;
                SGLogger.prettyLog(Level.FINE, false, "Minecart Player in gate:" + st.getGateName() + " gate Active: " + st.isGateActive() + " Target Gate: " + st.getGateTarget().getGateName() + " Network: " + gatenetwork);
                if (ConfigManager.getWormholeUseIsTeleport() && ((st.isGateSignPowered() && !SGPermissions.checkPermission(p, st, PermissionType.SIGN)) || (!st.isGateSignPowered() && !SGPermissions.checkPermission(p, st, PermissionType.DIALER)))) {
                    p.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return false;
                }
                if (st.getGateTarget().isGateIrisActive()) {
                    p.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Remote Iris is locked!");
                    veh.teleport(st.getGateMinecartTeleportLocation() != null
                            ? st.getGateMinecartTeleportLocation()
                            : st.getGatePlayerTeleportLocation());
                    if (ConfigManager.getTimeoutShutdown() == 0) {
                        st.shutdownStargate(true);
                    }
                    return false;
                }
                if (ConfigManager.isUseCooldownEnabled()) {
                    if (StargateRestrictions.isPlayerUseCooldown(p)) {
                        p.sendMessage(ConfigManager.MessageStrings.playerUseCooldownRestricted.toString());
                        p.sendMessage(ConfigManager.MessageStrings.playerUseCooldownWaitTime.toString() + StargateRestrictions.checkPlayerUseCooldownRemaining(p));
                        return false;
                    } else {
                        StargateRestrictions.addPlayerUseCooldown(p);
                    }
                }
            } else {
                if (st.getGateTarget().isGateIrisActive()) {
                    SGLogger.prettyLog(Level.FINE, false, "Minecart in gate:" + st.getGateName() + " gate Active: " + st.isGateActive() + " Target Gate: " + st.getGateTarget().getGateName() + " Network: " + gatenetwork);
                    veh.teleport(st.getGateMinecartTeleportLocation() != null
                            ? st.getGateMinecartTeleportLocation()
                            : st.getGatePlayerTeleportLocation());
                    if (ConfigManager.getTimeoutShutdown() == 0) {
                        st.shutdownStargate(true);
                    }
                    return false;
                }

            }

            final double speed = v.length();
            final Vector new_speed = new Vector();
            switch (st.getGateTarget().getGateFacing()) {
                case NORTH:
                    new_speed.setX(-1);
                    break;
                case SOUTH:
                    new_speed.setX(1);
                    break;
                case EAST:
                    new_speed.setZ(-1);
                    break;
                case WEST:
                    new_speed.setZ(1);
                    break;
            }
            
            // As we all know stargates accelerate matter.
            new_speed.multiply(speed * 5);
            if (st.getGateTarget().isGateIrisActive()) {
                target = st.getGateMinecartTeleportLocation() != null
                        ? st.getGateMinecartTeleportLocation()
                        : st.getGatePlayerTeleportLocation();
                veh.teleport(target);
                veh.setVelocity(new_speed);
            } else {
                if (e != null) {
                    SGLogger.prettyLog(Level.FINE, false, "Removing player from cart and doing some teleport hackery");
                    veh.eject();
                    veh.remove();
                    final Minecart newveh = target.getWorld().spawn(target, Minecart.class);
                    final Event teleportevent = new StargateMinecartTeleportEvent(veh, newveh);
                    StarGates.getThisPlugin().getServer().getPluginManager().callEvent(teleportevent);
                    e.teleport(target);
                    final Vector newnew_speed = new_speed;
                    StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new Runnable() {

                        @Override
                        public void run() {
                            newveh.setPassenger(e);
                            newveh.setVelocity(newnew_speed);
                            newveh.setFireTicks(0);
                        }
                    }, 5);
                } else {
                    veh.teleport(target);
                    veh.setVelocity(new_speed);
                }
            }

            if (ConfigManager.getTimeoutShutdown() == 0) {
                st.shutdownStargate(true);
            }
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            handleStargateMinecartTeleportEvent(event);
        }
    }
}
