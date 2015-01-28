package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public class WormholePlayerNotFoundException extends StarGatesException {
    private String message;
    
    public WormholePlayerNotFoundException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
