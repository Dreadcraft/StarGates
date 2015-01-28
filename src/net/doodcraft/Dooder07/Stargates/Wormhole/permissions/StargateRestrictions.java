package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable.ActionToTake;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.SGPermissions.PermissionType;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;

import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class StargateRestrictions {

    private static enum RestrictionGroup {

        CD_GROUP_ONE(ConfigManager.getUseCooldownGroupOne()),
        CD_GROUP_TWO(ConfigManager.getUseCooldownGroupTwo()),
        CD_GROUP_THREE(ConfigManager.getUseCooldownGroupThree()),
        BR_GROUP_ONE(ConfigManager.getBuildRestrictionGroupOne()),
        BR_GROUP_TWO(ConfigManager.getBuildRestrictionGroupTwo()),
        BR_GROUP_THREE(ConfigManager.getBuildRestrictionGroupThree());

        private final long restrictionGroupNode;

        private RestrictionGroup(final long restrictionGroupNode) {
            this.restrictionGroupNode = restrictionGroupNode;
        }

        public long getGroupValue() {
            return restrictionGroupNode;
        }
    }

    private static final ConcurrentHashMap<Player, Long> playerUseCooldownStart = new ConcurrentHashMap<Player, Long>();
    private static final ConcurrentHashMap<Player, RestrictionGroup> playerUseCooldownGroup = new ConcurrentHashMap<Player, RestrictionGroup>();

    public static void addPlayerUseCooldown(final Player player) {
        RestrictionGroup cooldownGroup = null;
        if (SGPermissions.checkPermission(player, PermissionType.USE_COOLDOWN_GROUP_ONE)) {
            cooldownGroup = RestrictionGroup.CD_GROUP_ONE;
        } else if (SGPermissions.checkPermission(player, PermissionType.USE_COOLDOWN_GROUP_TWO)) {
            cooldownGroup = RestrictionGroup.CD_GROUP_TWO;
        } else if (SGPermissions.checkPermission(player, PermissionType.USE_COOLDOWN_GROUP_THREE)) {
            cooldownGroup = RestrictionGroup.CD_GROUP_THREE;
        }
        if (cooldownGroup != null) {
            getPlayerUseCooldownStart().put(player, System.nanoTime());
            getPlayerUseCooldownGroup().put(player, cooldownGroup);
            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(WormholePlayerManager.getRegisteredWormholePlayer(player).getStargate(), ActionToTake.COOLDOWN_REMOVE), cooldownGroup.getGroupValue() * 20);
        }
    }

    public static long checkPlayerUseCooldownRemaining(final Player player) {
        if (getPlayerUseCooldownStart().containsKey(player) && getPlayerUseCooldownGroup().containsKey(player)) {
            final long startTime = getPlayerUseCooldownStart().get(player);
            final long currentTime = System.nanoTime();
            final long elapsedTime = (currentTime - startTime) / 1000000000;
            return (getPlayerUseCooldownGroup().get(player).getGroupValue() >= elapsedTime)
                    ? getPlayerUseCooldownGroup().get(player).getGroupValue() - elapsedTime
                    : removePlayerUseCooldown(player);
        }
        return -1;
    }

    private static ConcurrentHashMap<Player, RestrictionGroup> getPlayerUseCooldownGroup() {
        return playerUseCooldownGroup;
    }

    private static ConcurrentHashMap<Player, Long> getPlayerUseCooldownStart() {
        return playerUseCooldownStart;
    }

    public static boolean isPlayerBuildRestricted(final Player player) {
        if (ConfigManager.isBuildRestrictionEnabled()) {
            RestrictionGroup restrictionGroup = null;
            if (SGPermissions.checkPermission(player, PermissionType.BUILD_RESTRICTION_GROUP_ONE)) {
                restrictionGroup = RestrictionGroup.BR_GROUP_ONE;
            } else if (SGPermissions.checkPermission(player, PermissionType.BUILD_RESTRICTION_GROUP_TWO)) {
                restrictionGroup = RestrictionGroup.BR_GROUP_TWO;
            } else if (SGPermissions.checkPermission(player, PermissionType.BUILD_RESTRICTION_GROUP_THREE)) {
                restrictionGroup = RestrictionGroup.BR_GROUP_THREE;
            }
            int gateCount = 0;
            for (final Stargate stargate : StargateManager.getAllGates()) {
                if ((stargate.getGateOwner() != null) && stargate.getGateOwner().equalsIgnoreCase(player.getName())) {
                    gateCount++;
                }
            }
            return (restrictionGroup != null) && (gateCount != 0) && (gateCount >= restrictionGroup.getGroupValue());
        }
        return false;
    }

    public static boolean isPlayerUseCooldown(final Player player) {
        return (getPlayerUseCooldownStart().containsKey(player) && getPlayerUseCooldownGroup().containsKey(player));
    }

    public static int removePlayerUseCooldown(final Player player) {
        if (getPlayerUseCooldownStart().containsKey(player)) {
            getPlayerUseCooldownStart().remove(player);
        }
        if (getPlayerUseCooldownGroup().containsKey(player)) {
            getPlayerUseCooldownGroup().remove(player);
        }
        return 0;
    }
}
