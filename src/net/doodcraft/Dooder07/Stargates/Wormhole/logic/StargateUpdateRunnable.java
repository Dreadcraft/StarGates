package net.doodcraft.Dooder07.Stargates.Wormhole.logic;

import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.StargateRestrictions;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class StargateUpdateRunnable implements Runnable {

    public enum ActionToTake {

        SHUTDOWN,
        ANIMATE_WOOSH,
        DEACTIVATE,
        AFTERSHUTDOWN,
        DIAL_SIGN_CLICK,
        LIGHTUP,
        COOLDOWN_REMOVE,
        DIAL_SIGN_RESET,
        ESTABLISH_WORMHOLE
        
    }
    
    private final Stargate stargate;
    private final ActionToTake action;
    
    private Action eventBlockAction;
    
    public StargateUpdateRunnable(final Stargate stargate, final ActionToTake action) {
        this(stargate, action, null);
    }
    
    public StargateUpdateRunnable(Stargate stargate, ActionToTake action, Action eventBlockAction) {
        this.stargate = stargate;
        this.action = action;
        this.eventBlockAction = eventBlockAction;
    }

    @Override
    public void run() {
        runLogger(action);
        
        Player player = null;
        if (WormholePlayerManager.getRegisteredWormholePlayer(stargate.getLastUsedBy()) != null) {
            player = WormholePlayerManager.getRegisteredWormholePlayer(stargate.getLastUsedBy()).getPlayer();
        }

        switch (action) {
            case ESTABLISH_WORMHOLE:
                stargate.establishWormhole();
                break;
            case SHUTDOWN:
                stargate.shutdownStargate(true);
                break;
            case ANIMATE_WOOSH:
                stargate.animateOpening();
                break;
            case DEACTIVATE:
                stargate.timeoutStargate();
                break;
            case AFTERSHUTDOWN:
                stargate.stopAfterShutdownTimer();
                break;
            case DIAL_SIGN_CLICK:
                stargate.dialSignClicked(this.eventBlockAction);
                if ((player != null) && (stargate.getGateDialSignTarget() == null)) {
                    player.sendMessage("No available target to set dialer to.");
                }
                break;
            case DIAL_SIGN_RESET:
                stargate.resetSign(true);
                break;
            case LIGHTUP:
                stargate.lightStargate(true);
                break;
            case COOLDOWN_REMOVE:
                StargateRestrictions.removePlayerUseCooldown(player);
                break;
            default:
                break;
        }
    }
    
    @SuppressWarnings("incomplete-switch")
	private void runLogger(ActionToTake action) {

        switch (action) {
            case ESTABLISH_WORMHOLE:
            case ANIMATE_WOOSH:
            case LIGHTUP:
                SGLogger.prettyLog(Level.FINER, false, "Run Action \"" + action.toString() + (stargate != null
                        ? "\" Stargate \"" + stargate.getGateName()
                        : "") + "\"");
                return;
        }
        
        SGLogger.prettyLog(Level.FINE, false, "Run Action \"" + action.toString() + ", ActionType: " + ((this.eventBlockAction != null) ? this.eventBlockAction.toString() : "NULL") + (stargate != null
                ? "\" Stargate \"" + stargate.getGateName()
                : "") + "\"");        
    }
}
