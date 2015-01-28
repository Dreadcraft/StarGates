package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager.PermissionLevel;

import org.bukkit.entity.Player;

public class SGPermissions {

    public static enum PermissionType {

        DAMAGE,
        SIGN,
        DIALER,
        BUILD,
        REMOVE,
        USE,
        LIST,
        CONFIG,
        GO,
        COMPASS,
        USE_COOLDOWN_GROUP_ONE,
        USE_COOLDOWN_GROUP_TWO,
        USE_COOLDOWN_GROUP_THREE,
        BUILD_RESTRICTION_GROUP_ONE,
        BUILD_RESTRICTION_GROUP_TWO,
        BUILD_RESTRICTION_GROUP_THREE
    }

    public static boolean checkPermission(final Player player, final PermissionType permissiontype) {
        return checkPermission(player, null, null, permissiontype);
    }

    public static boolean checkPermission(final Player player, final Stargate stargate, final PermissionType permissionType) {
        return checkPermission(player, stargate, null, permissionType);
    }

    private static boolean checkPermission(final Player player, final Stargate stargate, final String network, final PermissionType permissionType) {
        if (player == null) {
            return false;
        }
        if (player.isOp()) {
            switch (permissionType) {
                case DAMAGE:
                case REMOVE:
                case CONFIG:
                case GO:
                case SIGN:
                case DIALER:
                case USE:
                case LIST:
                case COMPASS:
                case BUILD:
                    return true;
                default:
                    return false;
            }
        } else if (!ConfigManager.getPermissionsSupportDisable() && (StarGates.getPermissionManager() != null)) {

            if (ConfigManager.getSimplePermissions()) {
                switch (permissionType) {
                    case LIST:
                        return (SimplePermissionType.CONFIG.checkPermission(player) || SimplePermissionType.USE.checkPermission(player));
                    case GO:
                    case CONFIG:
                        return SimplePermissionType.CONFIG.checkPermission(player);
                    case DAMAGE:
                    case REMOVE:
                        return (SimplePermissionType.REMOVE.checkPermission(player) || SimplePermissionType.CONFIG.checkPermission(player));
                    case COMPASS:
                    case SIGN:
                    case DIALER:
                    case USE:
                        return SimplePermissionType.USE.checkPermission(player);
                    case BUILD:
                        return SimplePermissionType.BUILD.checkPermission(player);
                    default:
                        return false;
                }
            } else {
                String networkName = "Public";
                switch (permissionType) {
                    case LIST:
                        return (ComplexPermissionType.LIST.checkPermission(player) || ComplexPermissionType.CONFIG.checkPermission(player));
                    case CONFIG:
                        return ComplexPermissionType.CONFIG.checkPermission(player);
                    case GO:
                        return ComplexPermissionType.GO.checkPermission(player);
                    case COMPASS:
                        return ComplexPermissionType.USE_COMPASS.checkPermission(player);
                    case DAMAGE:
                    case REMOVE:
                        return (ComplexPermissionType.CONFIG.checkPermission(player) || ComplexPermissionType.REMOVE_ALL.checkPermission(player) || ComplexPermissionType.REMOVE_OWN.checkPermission(player, stargate));
                    case SIGN:
                        if ((stargate != null) && (stargate.getGateNetwork() != null)) {
                            networkName = stargate.getGateNetwork().getNetworkName();
                        }
                        return ((ComplexPermissionType.USE_SIGN.checkPermission(player) && (networkName.equals("Public") || (!networkName.equals("Public") && ComplexPermissionType.NETWORK_USE.checkPermission(player, networkName)))));
                    case DIALER:
                        if ((stargate != null) && (stargate.getGateNetwork() != null)) {
                            networkName = stargate.getGateNetwork().getNetworkName();
                        }
                        return ((ComplexPermissionType.USE_DIALER.checkPermission(player) && (networkName.equals("Public") || (!networkName.equals("Public") && ComplexPermissionType.NETWORK_USE.checkPermission(player, networkName)))));
                    case USE:
                        if ((stargate != null) && (stargate.getGateNetwork() != null)) {
                            networkName = stargate.getGateNetwork().getNetworkName();
                        }
                        return (((ComplexPermissionType.USE_SIGN.checkPermission(player) && (networkName.equals("Public") || (!networkName.equals("Public") && ComplexPermissionType.NETWORK_USE.checkPermission(player, networkName)))) || (ComplexPermissionType.USE_DIALER.checkPermission(player) && (networkName.equals("Public") || (!networkName.equals("Public") && ComplexPermissionType.NETWORK_USE.checkPermission(player, networkName))))));
                    case BUILD:
                        if (stargate != null) {
                            if (stargate.getGateNetwork() != null) {
                                networkName = stargate.getGateNetwork().getNetworkName();
                            }
                        } else {
                            if (network != null) {
                                networkName = network;
                            }
                        }
                        return ((ComplexPermissionType.BUILD.checkPermission(player) && (networkName.equals("Public") || (!networkName.equals("Public") && ComplexPermissionType.NETWORK_BUILD.checkPermission(player, networkName)))));
                    case USE_COOLDOWN_GROUP_ONE:
                        return ComplexPermissionType.USE_COOLDOWN_GROUP_ONE.checkPermission(player);
                    case USE_COOLDOWN_GROUP_TWO:
                        return ComplexPermissionType.USE_COOLDOWN_GROUP_TWO.checkPermission(player);
                    case USE_COOLDOWN_GROUP_THREE:
                        return ComplexPermissionType.USE_COOLDOWN_GROUP_THREE.checkPermission(player);
                    case BUILD_RESTRICTION_GROUP_ONE:
                        return ComplexPermissionType.BUILD_RESTRICTION_GROUP_ONE.checkPermission(player);
                    case BUILD_RESTRICTION_GROUP_TWO:
                        return ComplexPermissionType.BUILD_RESTRICTION_GROUP_TWO.checkPermission(player);
                    case BUILD_RESTRICTION_GROUP_THREE:
                        return ComplexPermissionType.BUILD_RESTRICTION_GROUP_THREE.checkPermission(player);
                    default:
                        return false;
                }
            }
        } else {
            if (stargate != null) {
                PermissionLevel lvl = null;
                switch (permissionType) {
                    case DAMAGE:
                    case REMOVE:
                    case CONFIG:
                    case GO:
                        lvl = PermissionsManager.getPermissionLevel(player, stargate);
                        return (lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION);
                    case SIGN:
                    case DIALER:
                    case USE:
                    case LIST:
                    case COMPASS:
                        lvl = PermissionsManager.getPermissionLevel(player, stargate);
                        return (lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION) || (lvl == PermissionLevel.WORMHOLE_USE_PERMISSION) || (lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION);
                    case BUILD:
                        lvl = PermissionsManager.getPermissionLevel(player, stargate);
                        return (lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION) || (lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION);
                    default:
                        return false;

                }
            }
        }
        return false;
    }

    public static boolean checkPermission(final Player player, final String network, final PermissionType permissiontype) {
        return checkPermission(player, null, network, permissiontype);
    }
}
