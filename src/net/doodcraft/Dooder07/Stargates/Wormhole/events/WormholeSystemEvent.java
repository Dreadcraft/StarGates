package net.doodcraft.Dooder07.Stargates.Wormhole.events;


public class WormholeSystemEvent extends WormholeEvent {
    public enum Action {
        PERMISSION_BACKEND_CHANGED,
        RELOADED
    }
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected Action action;

    public WormholeSystemEvent(Action action) {
        super(action.toString());

        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
