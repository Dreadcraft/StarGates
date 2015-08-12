package net.doodcraft.Dooder07.Stargates.Wormhole.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholePlayerEmptyStargateNameException;
import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholePlayerNullPointerException;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class WormholePlayer extends LocalPlayer {
    
    private Map<String, WormholePlayerUsageProperties> usageProperties = new HashMap<String, WormholePlayerUsageProperties>();
    private Map<String, Stargate> stargateMap = new HashMap<String, Stargate>();
    
    private String currentGateName = "";
    
    protected WormholePlayer(Player player) {
        super(player);
    }

    private void addProperties(String stargateName) {
        SGLogger.prettyLog(Level.FINE, false, "Adding properties for gate '" + stargateName + "' to player '" + getName() + "'");
        usageProperties.put(stargateName, new WormholePlayerUsageProperties());
    }
    
    public void addStargate(Stargate stargate) {
        if (this.hasStargate(stargate)) {
            SGLogger.prettyLog(Level.FINE, false, "Stargate '" + stargate.getGateName() + "' was already added for player '" + getName() + "'");
            this.setCurrentGateName(stargate.getGateName());
            return;
        }
        
        SGLogger.prettyLog(Level.FINE, false, "Adding Stargate '" + stargate.getGateName() + "' to player '" + getName() + "'");
        stargateMap.put(stargate.getGateName(), stargate);
        this.addProperties(stargate.getGateName());
        this.setCurrentGateName(stargate.getGateName());
    }

    public String getCurrentGateName() {
        return this.currentGateName;
    }
    
    @Override
    public String getDisplayName() {
        return this.player.getDisplayName();
    }
    
    public int getGateCount() {
        return stargateMap.keySet().size();
    }
    
    public PlayerOrientation getKickBackDirection(BlockFace facing) {
        return this.getKickBackDirection(null, facing);
    }
    
    public PlayerOrientation getKickBackDirection(PlayerOrientation direction) {
        return this.getKickBackDirection(direction, null);
    }

    private PlayerOrientation getKickBackDirection(PlayerOrientation direction, BlockFace facing) {
        if ((this.isOnline()) && ((direction != null) || (facing != null))) {
            SGLogger.prettyLog(Level.FINE, false, "PlayerDirection: " + this.getCardinalDirection() + ", BlockFacing: " + facing);

            PlayerOrientation kickBack = null;
            if (direction != null) {
                kickBack = PlayerOrientation.byCaseInsensitiveName(direction.name());
            }
            
            if (facing != null) {
                kickBack = PlayerOrientation.byCaseInsensitiveName(facing.name());
            }
            
            switch (kickBack) {
                case NORTH:
                case NORTH_EAST:
                case NORTH_WEST:
                    SGLogger.prettyLog(Level.FINE, false, "NORTH: kickback direction SOUTH");
                    return PlayerOrientation.SOUTH;
                case SOUTH:
                case SOUTH_EAST:
                case SOUTH_WEST:
                    SGLogger.prettyLog(Level.FINE, false, "SOUTH: kickback direction NORTH");
                    return PlayerOrientation.NORTH;
                case EAST:
                    SGLogger.prettyLog(Level.FINE, false, "EAST: kickback direction WEST");
                    return PlayerOrientation.WEST;
                case WEST:
                    SGLogger.prettyLog(Level.FINE, false, "WEST: kickback direction EAST");
                    return PlayerOrientation.EAST;
                default:
                    SGLogger.prettyLog(Level.FINE, false, "No kickback direction found");
                    break;
            }
        }
        
        return null;
    }
    
    @Override
    public String getName() {
        return this.player.getName();
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public WormholePlayerUsageProperties getProperties() {
        if (!"".equals(this.getCurrentGateName()))
            return this.getProperties(this.getCurrentGateName());
        
        return null;
    }
    
    public WormholePlayerUsageProperties getProperties(Stargate stargate) {
        return this.getProperties(stargate.getGateName());
    }
    
    public WormholePlayerUsageProperties getProperties(String gateName) {
        if (this.hasStargate(gateName))
            return usageProperties.get(gateName);
        
        return new WormholePlayerUsageProperties();
    }
    
    public Stargate getStargate() {
        if (!"".equalsIgnoreCase(this.getCurrentGateName()))
            return this.getStargate(this.getCurrentGateName());
        
        return null;
    }
    
    public Stargate getStargate(String gateName) {
        if (this.hasStargate(gateName)) {
            SGLogger.prettyLog(Level.FINE, false, "Get stargate '" + gateName + "'");
            return stargateMap.get(gateName);
        }
        
        SGLogger.prettyLog(Level.WARNING, false, "Could not get stargate '" + gateName + "' for player '" + getName() + "'");
        return new Stargate();
    }

    public List<Stargate> getStargates() {
        List<Stargate> stargates = new ArrayList<Stargate>();
        for (Stargate s : stargateMap.values())
            stargates.add(s);
        
        return stargates;
    }
    
    public boolean hasStargate(Stargate stargate) {
        return this.hasStargate(stargate.getGateName());
    }
    
    public boolean hasStargate(String gateName) {
        try {
            if (gateName != null)
                return (stargateMap.containsKey(gateName));
            
            throw new WormholePlayerNullPointerException("hasStargate checked for null. Can't check for null gateNames!");
        } catch (WormholePlayerNullPointerException e) {
            SGLogger.prettyLog(Level.SEVERE, true, e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private void removeProperty(String gateName) {
        SGLogger.prettyLog(Level.FINE, false, "Removing property for Stargate '" + gateName + "' from player '" + getName() + "'");
        usageProperties.remove(gateName);
    }

    public void removeStargate(Stargate stargate) {
        try {
            if (stargate != null) {
                this.removeStargate(stargate.getGateName());
                return;
            }
            
            throw new WormholePlayerNullPointerException("Remove Stargate failed. Stargate name was null.");
        } catch (WormholePlayerNullPointerException e) {
            SGLogger.prettyLog(Level.SEVERE, true, e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeStargate(String stargateName) {
        try {
            SGLogger.prettyLog(Level.FINE, false, "Removing Stargate '" + stargateName + "' from player '" + getName() + "'");
            if (!"".equals(stargateName)) {
                if (stargateMap.remove(stargateName) == null) {
                    SGLogger.prettyLog(Level.FINE, false, "Stargate '" + stargateName + "' wasn't attached to player '" + getName() + "'");
                } else {
                    SGLogger.prettyLog(Level.FINE, false, "StargateMaps count is now: '" + this.getGateCount() + "'");
                }
                
                this.removeProperty(stargateName);
                
                return;
            }
            
            throw new WormholePlayerEmptyStargateNameException("Stargate name can't be empty. Probably a malfunction during execution.");
        } catch (WormholePlayerEmptyStargateNameException e) {
            SGLogger.prettyLog(Level.SEVERE, true, e.getMessage());
        }
    }
    
    public void resetPlayer() {
        SGLogger.prettyLog(Level.FINE, false, "Resetting player '" + this.getName() + "'");
        for (Stargate s : this.getStargates()) {
            this.removeStargate(s.getGateName());
            this.removeProperty(s.getGateName());
        }
    }
    
    public void sendMessage(String message) {
        if (this.player.isOnline())
            this.getPlayer().sendMessage(message);
    }
    
    public void setCurrentGateName(String gateName) {
        if (gateName == null)
            gateName = "";
        
        SGLogger.prettyLog(Level.FINE, false, "Setting current used gateName to '" + gateName + "' for player '" + getName() + "'");
        this.currentGateName = gateName;
    }
}
