package net.doodcraft.Dooder07.Stargates.Wormhole.player;

import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholePlayerEmptyPlayerNameException;
import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholePlayerNotFoundException;
import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholePlayerNotOnlineException;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WormholePlayerManager {
    
    private static Map<String, WormholePlayer> wormholePlayers = new HashMap<String, WormholePlayer>();
    
    public static void registerPlayer(String playerName) {
        try {
            Player player = Bukkit.getServer().getPlayer(playerName);
            if (player == null)
                throw new WormholePlayerNotFoundException("Player '" + playerName + "' not found");
            
            if ((player != null) && (!player.isOnline()))
                throw new WormholePlayerNotOnlineException("Player '" + playerName + "' is not online");
            
            WormholePlayerManager.registerPlayer(player);
        } catch (WormholePlayerNotFoundException e) {
            SGLogger.prettyLog(Level.SEVERE, false, e.getMessage());
        } catch (WormholePlayerNotOnlineException e) {
            SGLogger.prettyLog(Level.WARNING, false, e.getMessage());
        }
    }
    
    public static void registerPlayer(Player player) {
        if (!isRegistered(player.getName())) {
            SGLogger.prettyLog(Level.FINE, false, "Registering player '" + player.getName() +"' as WormholePlayer");
            wormholePlayers.put(player.getName(), new WormholePlayer(player));
        }
    }
    
    public static boolean isRegistered(Player player) {
        return isRegistered(player.getName());
    }
    
    public static boolean isRegistered(String playerName) {
        try {
            if ("".equals(playerName))
                throw new WormholePlayerEmptyPlayerNameException("playerName can't be empty.");
            
            if (!wormholePlayers.containsKey(playerName)) {
                SGLogger.prettyLog(Level.FINE, false, "'" + playerName +"' was not registered");
                return false;
            }
        } catch (WormholePlayerEmptyPlayerNameException e) {
            SGLogger.prettyLog(Level.SEVERE, true, e.getMessage());
        }
        
        return true;
    }
    
    public static void unregisterPlayer(Player player) {
        unregisterPlayer(player.getName());
    }

    public static void unregisterPlayer(String playerName) {
        if (!isRegistered(playerName))
            return;
        
        SGLogger.prettyLog(Level.FINE, false, "Unregistering WormholePlayer '" + playerName +"'");

        wormholePlayers.get(playerName).resetPlayer();
        wormholePlayers.remove(playerName);
    }
    
    public static void unregisterAllPlayers() {
        SGLogger.prettyLog(Level.FINE, false, "Unregistering all WormholePlayers.");
        wormholePlayers.clear();
    }
    
	public static void registerAllOnlinePlayers() {
        SGLogger.prettyLog(Level.FINE, false, "Registering all online players as WormholePlayers.");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            registerPlayer(player);
        }
    }
    
    public static HashMap<String, WormholePlayer> getAllRegisteredPlayers() {
        return (HashMap<String, WormholePlayer>) wormholePlayers;
    }
    
    public static WormholePlayer getRegisteredWormholePlayer(Player player) {
        return getRegisteredWormholePlayer(player.getName());
    }
    
    public static WormholePlayer getRegisteredWormholePlayer(String playerName) {
        if (isRegistered(playerName))
            return wormholePlayers.get(playerName);
        
        return null;        
    }
    
    public static WormholePlayer findPlayerByGateName(String gateName) {
        for (String pl : wormholePlayers.keySet()) {
            for (Stargate s : wormholePlayers.get(pl).getStargates()) {
                if (s.getGateName().equalsIgnoreCase(gateName))
                    return wormholePlayers.get(pl);
            }
        }
        
        return null;
    }
}
