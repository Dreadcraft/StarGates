package net.doodcraft.Dooder07.Stargates.Wormhole.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.stream.Location;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable.ActionToTake;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class StargateManager {

    private static Map<Location, Stargate> allGateBlocks = new HashMap<Location, Stargate>();
    private static Map<String, Stargate> stargateList = new HashMap<String, Stargate>();
    private static Map<String, Stargate> incompleteStargates = new HashMap<String, Stargate>();
    private static Map<String, Stargate> activatedStargates = new HashMap<String, Stargate>();
    private static Map<String, StargateNetwork> stargateNetworks = new HashMap<String, StargateNetwork>();
    private static Map<String, StargateShape> playerBuilders = new HashMap<String, StargateShape>();
    private static Map<Location, Block> openingAnimationBlocks = new HashMap<Location, Block>();

    public static void addActivatedStargate(Stargate s) {
        addActivatedStargate(s.getGateName(), s);
    }
    
    public static void addActivatedStargate(String gateName, Stargate s) {
        if (!hasActivatedStargate(gateName))
            getActivatedStargates().put(gateName, s);
    }

    public static void addBlockIndex(Block b, Stargate s) {
        if ((b != null) && (s != null)) {
            getAllGateBlocks().put(b.getLocation(), s);
        }
    }
    
    public static void addGateToNetwork(Stargate gate, String network) {
        if (!getStargateNetworks().containsKey(network)) {
            addStargateNetwork(network);
        }

        StargateNetwork net;
        if ((net = getStargateNetworks().get(network)) != null) {
            synchronized (net.getNetworkGateLock()) {
                net.getNetworkGateList().add(gate);
                if (gate.isGateSignPowered()) {
                    net.getNetworkSignGateList().add(gate);
                }
            }
        }
    }
    
    public static void addIncompleteStargate(String playerName, Stargate stargate) {
        getIncompleteStargates().put(playerName, stargate);
    }

    public static void addPlayerBuilderShape(String playerName, StargateShape shape) {
        getPlayerBuilders().put(playerName, shape);
    }

    protected static boolean addStargate(Stargate s) {
    	
    	for(Location b : s.getGateStructureBlocks()) {
    		if(getAllGateBlocks().get(b) != null && getAllGateBlocks().get(b) != s) {
    			return false;
    		}
    	}
    	
    	for(Location b : s.getGatePortalBlocks()) {
    		if(getAllGateBlocks().get(b) != null && getAllGateBlocks().get(b) != s) {
    			return false;
    		}
    	}
    	
    			
        getStargateList().put(s.getGateName(), s);
        
        for (Location b : s.getGateStructureBlocks()) {
            getAllGateBlocks().put(b, s);
        }
        
        for (Location b : s.getGatePortalBlocks()) {
            getAllGateBlocks().put(b, s);
        }
        
        return true;
        
    }

    public static StargateNetwork addStargateNetwork(String networkName) {
        if (getStargateNetworks().containsKey(networkName))
            return getStargateNetworks().get(networkName);
        
        StargateNetwork sn = new StargateNetwork();
        sn.setNetworkName(networkName);
        getStargateNetworks().put(networkName, sn);
        
        return sn;
    }

    public static boolean completeStargate(Player player, Stargate stargate) {
        return completeStargate(player.getName(), stargate);
    }

    public static boolean completeStargate(Player player, String gateName, String idc, String network) {
        return completeStargate(player.getName(), gateName, idc, network);
    }

    public static boolean completeStargate(String playerName, Stargate stargate) {
        Stargate posDupe = StargateManager.getStargate(stargate.getGateName());
        if (posDupe != null)
            return false;
        
        stargate.setGateName(stargate.getGateName());
        
        
        if(addStargate(stargate)) {
        	stargate.setGateOwner(playerName);
        	stargate.completeGate(stargate.getGateName(), "");
        	
        	SGLogger.prettyLog(Level.INFO, false, "Player: " + playerName + " completed a wormhole: " + stargate.getGateName());
        	
        	StargateDBManager.stargateToSQL(stargate);
        	
        	return true;
        }
        else {
        	Player player = Bukkit.getPlayer(playerName);
        	if(player != null) {
        		player.sendMessage(ChatColor.RED + "Stargate shares blocks with another gate.");
        	}
        	
        	return false;
        }
    }
    
    public static boolean completeStargate(String playerName, String gateName, String idc, String network) {
        final Stargate complete = getIncompleteStargates().remove(playerName);
        
        if (complete != null) {
        	
        	complete.setGateName(gateName);
        	
        	if(addStargate(complete)) {
                complete.setGateOwner(playerName);
                complete.completeGate(gateName, idc);
                
	            if (!network.equals("")) {
	                StargateNetwork net = StargateManager.getStargateNetwork(network);
	                if (net == null) {
	                    net = StargateManager.addStargateNetwork(network);
	                }
	                StargateManager.addGateToNetwork(complete, network);
	                complete.setGateNetwork(net);
	            }
	            
	            SGLogger.prettyLog(Level.INFO, false, "Player: " + playerName + " completed a wormhole: " + complete.getGateName());
	            
	            StargateDBManager.stargateToSQL(complete);
	            
	            return true;
        	}
        	else {
        		Player player = Bukkit.getPlayer(playerName);
        		if(player != null) {
        			player.sendMessage(ChatColor.RED + "Stargate shares blocks with another stargate.");
        		}
        		
        		return false;
        	}
        }

        return false;        
    }

    public static double distanceSquaredToClosestGateBlock(Location self, Stargate stargate) {
        double distance = Double.MAX_VALUE;
        
        if ((stargate != null) && (self != null)) {
            ArrayList<Location> gateblocks = stargate.getGateStructureBlocks();
            for (Location l : gateblocks) {
                double blockdistance = getSquaredDistance(self, l);
                if (blockdistance < distance) {
                    distance = blockdistance;
                }
            }
        }
        
        return distance;
    }
    
    public static Stargate findClosestStargate(Location self) {
        Stargate stargate = null;
        
        if (self != null) {
            ArrayList<Stargate> gates = StargateManager.getAllGates();
            double man = Double.MAX_VALUE;
            
            for (Stargate s : gates) {
                Location t = s.getGatePlayerTeleportLocation();
                double distance = getSquaredDistance(self, t);
                if (distance < man) {
                    man = distance;
                    stargate = s;
                }
            }
        }
        
        return stargate;
    }

    private static HashMap<String, Stargate> getActivatedStargates() {
        return (HashMap<String, Stargate>) activatedStargates;
    }

    private static HashMap<Location, Stargate> getAllGateBlocks() {
        return (HashMap<Location, Stargate>) allGateBlocks;
    }

    public static ArrayList<Stargate> getAllGates() {
        ArrayList<Stargate> gates = new ArrayList<Stargate>();

        for (Stargate s : getStargateList().values()) {
            gates.add(s);
        }

        return gates;
    }

    public static Stargate getGateFromBlock(Block block) {
        if (getAllGateBlocks().containsKey(block.getLocation())) {
            return getAllGateBlocks().get(block.getLocation());
        }

        return null;
    }

    private static HashMap<String, Stargate> getIncompleteStargates() {
        return (HashMap<String, Stargate>) incompleteStargates;
    }

    protected static HashMap<Location, Block> getOpeningAnimationBlocks() {
        return (HashMap<Location, Block>) openingAnimationBlocks;
    }

    private static HashMap<String, StargateShape> getPlayerBuilders() {
        return (HashMap<String, StargateShape>) playerBuilders;
    }

    public static StargateShape getPlayerBuilderShape(Player player) {
        return getPlayerBuilderShape(player.getName());
    }

    public static StargateShape getPlayerBuilderShape(String playerName) {
        if (getPlayerBuilders().containsKey(playerName)) {
            return getPlayerBuilders().remove(playerName);
        }
        
        return null;
    }

    private static double getSquaredDistance(Location self, Location target) {
        double distance = Double.MAX_VALUE;
        if ((self != null) && (target != null)) {
            distance = Math.pow(self.getX() - target.getX(), 2) + Math.pow(self.getY() - target.getY(), 2) + Math.pow(self.getZ() - target.getZ(), 2);
        }
        return distance;
    }
    
    public static Stargate getStargate(String gateName) {
        if (getStargateList().containsKey(gateName)) {
            return getStargateList().get(gateName);
        }
        
        return null;
    }    
    
    public static Stargate getStargateByPlayer(Player player) {
        return getStargateByPlayer(player.getName());
    }

    public static Stargate getStargateByPlayer(String playerName) {
        return getActivatedStargates().get(playerName);
    }

    private static HashMap<String, Stargate> getStargateList() {
        return (HashMap<String, Stargate>) stargateList;
    }

    public static StargateNetwork getStargateNetwork(String name) {
        if (getStargateNetworks().containsKey(name)) {
            return getStargateNetworks().get(name);
        }
        
        return null;
    }

    private static HashMap<String, StargateNetwork> getStargateNetworks() {
        return (HashMap<String, StargateNetwork>) stargateNetworks;
    }

    public static boolean hasActivatedStargate(Stargate s) {
        return hasActivatedStargate(s.getGateName());
    }

    public static boolean hasActivatedStargate(String gateName) {
        return getActivatedStargates().containsKey(gateName);

    }

    public static boolean isBlockInGate(Block block) {
        return isLocationInGate(block.getLocation());
    }

    public static boolean isLocationInGate(Location loc) {
        return getAllGateBlocks().containsKey(loc) || getOpeningAnimationBlocks().containsKey(loc);
    }

    public static boolean isStargate(String gateName) {
        return getStargateList().containsKey(gateName);
    }

    public static Stargate removeActivatedStargate(String gateName) {
        return getActivatedStargates().remove(gateName);
    }
    
    public static void removeBlockIndex(Block block) {
        if (block != null) {
            getAllGateBlocks().remove(block.getLocation());
        }
    }

    public static void removeIncompleteStargate(Player player) {
        removeIncompleteStargate(player.getName());
    }
    
    public static void removeIncompleteStargate(String playerName) {
        getIncompleteStargates().remove(playerName);
    }
    
    public static void removeStargate(Stargate s) {
        getStargateList().remove(s.getGateName());

        if (WormholePlayerManager.findPlayerByGateName(s.getGateName()) != null)
            WormholePlayerManager.findPlayerByGateName(s.getGateName()).removeStargate(s);

        StargateDBManager.removeStargateFromSQL(s);
        if (s.getGateNetwork() != null) {
            synchronized (s.getGateNetwork().getNetworkGateLock()) {
                s.getGateNetwork().getNetworkGateList().remove(s);
                if (s.isGateSignPowered()) {
                    s.getGateNetwork().getNetworkSignGateList().remove(s);
                }

                for (Stargate s2 : s.getGateNetwork().getNetworkSignGateList()) {
                    if ((s2.getGateDialSignTarget() != null) && (s2.getGateDialSignTarget().getGateId() == s.getGateId()) && s2.isGateSignPowered()) {
                        s2.setGateDialSignTarget(null);
                        if (s.getGateNetwork().getNetworkSignGateList().size() > 1) {
                            s2.setGateDialSignIndex(0);
                            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(s2, ActionToTake.DIAL_SIGN_CLICK));
                        }
                    }
                }
            }
        }

        for (Location b : s.getGateStructureBlocks()) {
            getAllGateBlocks().remove(b);
        }

        for (Location b : s.getGatePortalBlocks()) {
            getAllGateBlocks().remove(b);
        }        
    }
}