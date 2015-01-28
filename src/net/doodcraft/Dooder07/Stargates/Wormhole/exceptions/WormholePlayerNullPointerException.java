package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public class WormholePlayerNullPointerException extends StarGatesException {
    private String message;
    
    public WormholePlayerNullPointerException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
