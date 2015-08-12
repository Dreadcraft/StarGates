package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

enum ComplexPermissionType {

    USE_SIGN("wormhole.use.sign"),
    USE_DIALER("wormhole.use.dialer"),
    USE_COMPASS("wormhole.use.compass"),
    USE_COOLDOWN_GROUP_ONE("wormhole.cooldown.groupone"),
    USE_COOLDOWN_GROUP_TWO("wormhole.cooldown.grouptwo"),
    USE_COOLDOWN_GROUP_THREE("wormhole.cooldown.groupthree"),
    REMOVE_OWN("wormhole.remove.own"),
    REMOVE_ALL("wormhole.remove.all"),
    BUILD("wormhole.build"),
    BUILD_RESTRICTION_GROUP_ONE("wormhole.build.groupone"),
    BUILD_RESTRICTION_GROUP_TWO("wormhole.build.grouptwo"),
    BUILD_RESTRICTION_GROUP_THREE("wormhole.build.groupthree"),
    CONFIG("wormhole.config"),
    LIST("wormhole.list"),
    NETWORK_USE("wormhole.network.use."),
    NETWORK_BUILD("wormhole.network.build."),
    GO("wormhole.go");

    private static final Map<String, ComplexPermissionType> complexPermissionMap = new HashMap<String, ComplexPermissionType>();
    static {
        for (final ComplexPermissionType type : EnumSet.allOf(ComplexPermissionType.class)) {
            complexPermissionMap.put(type.complexPermissionNode, type);
        }
    }
    public static ComplexPermissionType fromComplexPermissionNode(final String complexPermissionNode) // NO_UCD
    {
        return complexPermissionMap.get(complexPermissionNode);
    }

    private final String complexPermissionNode;

    private ComplexPermissionType(final String complexPermissionNode) {
        this.complexPermissionNode = complexPermissionNode;
    }

    protected boolean checkPermission(final Player player) {
        return checkPermission(player, null, null);
    }

    protected boolean checkPermission(final Player player, final Stargate stargate) {
        return checkPermission(player, stargate, null);
    }

    public boolean checkPermission(final Player player, final Stargate stargate, final String networkName) {
        if ((player != null) && (StarGates.getPermissionManager() != null) && !ConfigManager.getSimplePermissions()) {
            boolean allowed = false;

            switch (this) {
                case NETWORK_USE:
                case NETWORK_BUILD:
                    allowed = networkName != null && StarGates.getPermissionManager().has(player, getString() + networkName);
                    break;
                case REMOVE_OWN:
                    allowed = ((stargate != null) && (stargate.getGateOwner() != null) && stargate.getGateOwner().equals(player.getName()) && StarGates.getPermissionManager().has(player, complexPermissionNode));
                    break;
                default:
                    allowed = StarGates.getPermissionManager().has(player, getString());
                    break;
            }
            
            if (allowed) {
                SGLogger.prettyLog(Level.FINE, false, "Player: '" + player.getName() + "' granted complex \"" + toString() + "\" permission" + (networkName != null
                        ? " on network \"" + networkName + "\""
                        : "") + ".");
                return true;
            }
            
            SGLogger.prettyLog(Level.FINE, false, "Player: '" + player.getName() + "' denied complex \"" + toString() + "\" permission" + (networkName != null
                    ? " on network \"" + networkName + "\""
                    : "") + ".");
        }
        return false;
    }

    protected boolean checkPermission(final Player player, final String networkName) {
        return checkPermission(player, null, networkName);
    }

    public String getString() {
        return complexPermissionNode;
    }
}
