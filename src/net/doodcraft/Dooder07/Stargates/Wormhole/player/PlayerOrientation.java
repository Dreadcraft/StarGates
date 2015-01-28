package net.doodcraft.Dooder07.Stargates.Wormhole.player;

import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PlayerOrientation {
    NORTH("North", new Vector(0, 0, -1), new Vector(-1, 0, 0), true),
    NORTH_EAST("Northeast", new Vector(1, 0, -1).normalize(), new Vector(-1, 0, -1).normalize(), false),
    EAST("East", new Vector(1, 0, 0), new Vector(0, 0, -1), true),
    SOUTH_EAST("Southeast", new Vector(1, 0, 1).normalize(), new Vector(1, 0, -1).normalize(), false),
    SOUTH("South", new Vector(0, 0, 1), new Vector(1, 0, 0), true),
    SOUTH_WEST("Southwest", new Vector(-1, 0, 1).normalize(), (new Vector(1, 0, 1)).normalize(), false),
    WEST("West", new Vector(-1, 0, 0), new Vector(0, 0, 1), true),
    NORTH_WEST("Northwest", new Vector(-1, 0, -1).normalize(), (new Vector(-1, 0, 1)).normalize(), false),
    UP("Up", new Vector(0, 1, 0), new Vector(0, 0, 1), true),
    DOWN("Down", new Vector(0, -1, 0), new Vector(0, 0, 1), true);
    
    private String name;
    private Vector direction;
    private Vector leftDirection;
    private boolean isOrthogonal;
    
    private static final Map<String, PlayerOrientation> mapping = new HashMap<String, PlayerOrientation>();

    static {
        for (PlayerOrientation pd : EnumSet.allOf(PlayerOrientation.class)) {
            mapping.put(pd.name, pd);
        }
    }    
    
    private PlayerOrientation(String name, Vector direction, Vector leftDirection, boolean isOrthogonal) {
        this.name = name;
        this.direction = direction;
        this.leftDirection = leftDirection;
        this.isOrthogonal = isOrthogonal;
    }    
    
    public String getName() {
        return this.name;
    }
    
    public Vector getVector() {
        return direction;
    }

    public Vector leftVector() {
        return leftDirection;
    }
    
    public boolean isOrthogonal() {
        return isOrthogonal;
    }
    
    public static PlayerOrientation byCaseInsensitiveName(String name) {
        return mapping.get(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
    }

    public static PlayerOrientation byName(String name) {
        return mapping.get(name);
    }
    
    public static PlayerOrientation byDirection(PlayerOrientation facing) {
        return mapping.get(facing.name());
    }
}
