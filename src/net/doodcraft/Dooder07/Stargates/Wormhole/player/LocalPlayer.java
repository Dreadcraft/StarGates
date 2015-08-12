package net.doodcraft.Dooder07.Stargates.Wormhole.player;


public abstract class LocalPlayer {
    protected Player player = null;
    
    protected LocalPlayer(Player player) {
        this.player = player;
    }
    
    public PlayerOrientation getCardinalDirection() {
        double rotation = (this.player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        
        return this.getDirection(rotation);
    }
    
    private PlayerOrientation getDirection(double rotation) {
        if (0 <= rotation && rotation < 22.5) {
            return PlayerOrientation.NORTH;
        } else if (22.5 <= rotation && rotation < 67.5) {
            return PlayerOrientation.NORTH_EAST;
        } else if (67.5 <= rotation && rotation < 112.5) {
            return PlayerOrientation.EAST;
        } else if (112.5 <= rotation && rotation < 157.5) {
            return PlayerOrientation.SOUTH_EAST;
        } else if (157.5 <= rotation && rotation < 202.5) {
            return PlayerOrientation.SOUTH;
        } else if (202.5 <= rotation && rotation < 247.5) {
            return PlayerOrientation.SOUTH_WEST;
        } else if (247.5 <= rotation && rotation < 292.5) {
            return PlayerOrientation.WEST;
        } else if (292.5 <= rotation && rotation < 337.5) {
            return PlayerOrientation.NORTH_WEST;
        } else if (337.5 <= rotation && rotation < 360.0) {
            return PlayerOrientation.NORTH;
        } else {
            return null;
        }
    }
    
    public abstract String getDisplayName();
    
    public abstract String getName();
    
    public boolean isOnline() {
        return this.player.isOnline();

    }    
}
