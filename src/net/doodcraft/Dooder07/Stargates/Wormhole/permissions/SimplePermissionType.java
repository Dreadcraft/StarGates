package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;

import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

enum SimplePermissionType {

    USE("wormhole.simple.use"),
    BUILD("wormhole.simple.build"),
    REMOVE("wormhole.simple.remove"),
    CONFIG("wormhole.simple.config");

    private final String simplePermissionNode;
    private static final Map<String, SimplePermissionType> simplePermissionMap = new HashMap<String, SimplePermissionType>();
    static {
        for (final SimplePermissionType simplePermissionType : EnumSet.allOf(SimplePermissionType.class)) {
            simplePermissionMap.put(simplePermissionType.simplePermissionNode, simplePermissionType);
        }
    }

    public static SimplePermissionType fromSimplePermissionNode(final String simplePermissionNode) // NO_UCD
    {
        return simplePermissionMap.get(simplePermissionNode);
    }

    private SimplePermissionType(final String simplePermissionNode) {
        this.simplePermissionNode = simplePermissionNode;
    }

    public String getString() {
        return simplePermissionNode;
    }

    public boolean checkPermission(Player player) {
        return StarGates.getPermissionManager().has(player, getString());
    }
}
