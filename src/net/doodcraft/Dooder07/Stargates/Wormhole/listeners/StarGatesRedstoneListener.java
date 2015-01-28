package net.doodcraft.Dooder07.Stargates.Wormhole.listeners;

import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.logging.Level;

public class StarGatesRedstoneListener implements Listener {

    private static boolean isCurrentNew(final int oldCurrent, final int newCurrent) {
        return ((oldCurrent == 0) && (newCurrent > 0)) || ((oldCurrent > 0) && (newCurrent == 0));
    }

    private static boolean isCurrentOn(final int oldCurrent, final int newCurrent) {
        return (newCurrent > 0) && (oldCurrent == 0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
        final Block block = event.getBlock();
        
        if (StargateManager.isBlockInGate(block)) {
            SGLogger.prettyLog(Level.FINEST, false, "Caught redstone event on block: " + block.toString() + " oldCurrent: " + event.getOldCurrent() + " newCurrent: " + event.getNewCurrent());
            
            final Stargate stargate = StargateManager.getGateFromBlock(event.getBlock());
            if (
                    (stargate.isGateSignPowered()) && (stargate.isGateRedstonePowered()) && 
                    (block.getType().equals(Material.REDSTONE_WIRE)) && (isCurrentNew(event.getOldCurrent(), event.getNewCurrent())) && 
                    (!stargate.isGateActive())
                ) {
                
                if ((stargate.getGateRedstoneSignActivationBlock() != null) && block.equals(stargate.getGateRedstoneSignActivationBlock()) && isCurrentOn(event.getOldCurrent(), event.getNewCurrent())) {
                    stargate.tryClickTeleportSign(stargate.getGateDialSignBlock(), Action.PHYSICAL);
                    SGLogger.prettyLog(Level.FINE, false, "Caught redstone sign event on gate: " + stargate.getGateName() + " block: " + block.toString());
                } else if ((stargate.getGateRedstoneDialActivationBlock() != null) && block.equals(stargate.getGateRedstoneDialActivationBlock()) && isCurrentOn(event.getOldCurrent(), event.getNewCurrent())) {
                    if (stargate.isGateActive() && (stargate.getGateTarget() != null)) {
                        stargate.shutdownStargate(true);
                        SGLogger.prettyLog(Level.FINE, false, "Caught redstone shutdown event on gate: " + stargate.getGateName() + " block: " + block.toString());
                    }
                    
                    if (!stargate.isGateActive() && (stargate.getGateDialSignTarget() != null) && !stargate.isGateRecentlyActive()) {
                        stargate.dialStargate(stargate.getGateDialSignTarget(), false);
                        SGLogger.prettyLog(Level.FINE, false, "Caught redstone dial event on gate: " + stargate.getGateName() + " block: " + block.toString());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFromToEvent(BlockFromToEvent event) {
        SGLogger.prettyLog(Level.FINE, false, "We got a BlockFromToEvent here: " + event.getToBlock());
    }
}
