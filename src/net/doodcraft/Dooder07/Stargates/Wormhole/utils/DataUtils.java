package net.doodcraft.Dooder07.Stargates.Wormhole.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.nio.ByteBuffer;

public class DataUtils {

    public static Block blockFromBytes(final byte[] bytes, final World w) {
        final ByteBuffer b = ByteBuffer.wrap(bytes);
        return w.getBlockAt(b.getInt(), b.getInt(), b.getInt());
    }

    public static byte[] blockLocationToBytes(final Location l) {
        final ByteBuffer bb = ByteBuffer.allocate(12);

        bb.putInt(l.getBlockX());
        bb.putInt(l.getBlockY());
        bb.putInt(l.getBlockZ());

        return bb.array();
    }

    public static byte[] blockToBytes(final Block b) {
        final ByteBuffer bb = ByteBuffer.allocate(12);

        bb.putInt(b.getX());
        bb.putInt(b.getY());
        bb.putInt(b.getZ());

        return bb.array();
    }

     public static int byteArrayToInt(final byte[] b, final int index) {
         return (b[index] << 24) + ((b[index + 1] & 0xFF) << 16) + ((b[index + 2] & 0xFF) << 8) + (b[index + 3] & 0xFF);
     }
     
    public static boolean byteToBoolean(final byte b) {
        return b >= 1;
    }

     public static byte[] intToByteArray(final int value) {
         return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
     }
     
    public static Location locationFromBytes(final byte[] bytes, final World w) {
        final ByteBuffer b = ByteBuffer.wrap(bytes);
        return new Location(w, b.getDouble(), b.getDouble(), b.getDouble(), b.getFloat(), b.getFloat());
    }

    public static byte[] locationToBytes(final Location l) {
        final ByteBuffer b = ByteBuffer.allocate(32);
        b.putDouble(l.getX());
        b.putDouble(l.getY());
        b.putDouble(l.getZ());
        b.putFloat(l.getPitch());
        b.putFloat(l.getYaw());

        return b.array();
    }
}