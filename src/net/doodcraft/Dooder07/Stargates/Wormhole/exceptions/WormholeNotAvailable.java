package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public class WormholeNotAvailable extends StarGatesException {
    private String message;

    public WormholeNotAvailable(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
