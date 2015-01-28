package net.doodcraft.Dooder07.Stargates.Wormhole.events;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StargateMinecartTeleportEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Minecart oldMinecart;
    private Minecart newMinecart;

    public StargateMinecartTeleportEvent(Minecart oldMinecart, Minecart newMinecart) {
        this.oldMinecart = oldMinecart;
        this.newMinecart = newMinecart;
    }

    public Minecart getNewMinecart() {
        return newMinecart;
    }

    public Minecart getOldMinecart() {
        return oldMinecart;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
