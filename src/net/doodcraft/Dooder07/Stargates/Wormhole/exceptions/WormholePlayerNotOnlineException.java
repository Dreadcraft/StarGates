package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public class WormholePlayerNotOnlineException extends StarGatesException {
    private String message;
    
    public WormholePlayerNotOnlineException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
