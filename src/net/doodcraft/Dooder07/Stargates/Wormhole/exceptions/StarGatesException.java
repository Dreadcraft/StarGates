package net.doodcraft.Dooder07.Stargates.Wormhole.exceptions;

@SuppressWarnings("serial")
public abstract class StarGatesException extends RuntimeException {
    protected StarGatesException() {
    }

    protected StarGatesException(String msg) {
        super(msg);
    }
}