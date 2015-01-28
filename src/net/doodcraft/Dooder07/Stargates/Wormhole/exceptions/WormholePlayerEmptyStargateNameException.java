package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public class WormholePlayerEmptyStargateNameException extends StarGatesException {
    private String message;
    
    public WormholePlayerEmptyStargateNameException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
