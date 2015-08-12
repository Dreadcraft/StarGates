package net.doodcraft.Dooder07.Stargates.Wormhole.events;

import java.awt.Event;

public class StargateMinecartTeleportEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
    private Minecart oldMinecart;

    private Minecart newMinecart;

    public StargateMinecartTeleportEvent(Minecart oldMinecart, Minecart newMinecart) {
        this.oldMinecart = oldMinecart;
        this.newMinecart = newMinecart;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Minecart getNewMinecart() {
        return newMinecart;
    }

    public Minecart getOldMinecart() {
        return oldMinecart;
    }
}
