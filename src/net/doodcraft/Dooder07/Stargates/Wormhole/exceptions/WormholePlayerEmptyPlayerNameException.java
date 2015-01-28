package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public class WormholePlayerEmptyPlayerNameException extends StarGatesException {
    private String message;
    
    public WormholePlayerEmptyPlayerNameException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
