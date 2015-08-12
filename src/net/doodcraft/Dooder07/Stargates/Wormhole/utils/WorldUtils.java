package net.doodcraft.Dooder07.Stargates.Wormhole.utils;

import java.awt.Button;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;

public class WorldUtils {

    @SuppressWarnings("deprecation")
	public static byte getButtonFacingByteFromBlockFace(final BlockFace blockFace) {
        Button buttonFacing = new Button(Material.STONE_BUTTON);
        buttonFacing.setFacingDirection(blockFace);

        return buttonFacing.getData();
    }

    public static Float getDegreesFromBlockFace(final BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
                return (float) 180;
            case EAST:
                return (float) 270;
            case SOUTH:
                return (float) 0;
            case WEST:
                return (float) 90;
            default:
                return (float) 0;
        }
    }

    public static BlockFace getInverseDirection(final BlockFace blockFace) {
        return blockFace.getOppositeFace();
    }

    public static byte getLeverFacingByteFromBlockFace(final BlockFace blockFace) {
        return getButtonFacingByteFromBlockFace(blockFace);
    }

    public static byte getLeverToggleByte(final byte leverState, final boolean isActive) {
        return (byte) (isActive
                ? (leverState & 0x8) != 0x8
                ? leverState ^ 0x8
                : leverState
                : (leverState & 0x8) == 0x8
                ? leverState ^ 0x8
                : leverState);
    }

    public static BlockFace getPerpendicularLeftDirection(final BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
            case UP:
                return BlockFace.WEST;
            case SOUTH:
            case DOWN:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.NORTH;
            case WEST:
                return BlockFace.SOUTH;
            case NORTH_EAST:
                return BlockFace.NORTH_WEST;
            case SOUTH_WEST:
                return BlockFace.SOUTH_EAST;
            case NORTH_WEST:
                return BlockFace.SOUTH_WEST;
            case SOUTH_EAST:
                return BlockFace.NORTH_EAST;
            default:
                return blockFace;
        }
    }

    public static BlockFace getPerpendicularRightDirection(final BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
            case UP:
                return BlockFace.EAST;
            case SOUTH:
            case DOWN:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.SOUTH;
            case WEST:
                return BlockFace.NORTH;
            case NORTH_EAST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_WEST:
                return BlockFace.NORTH_WEST;
            case NORTH_WEST:
                return BlockFace.NORTH_EAST;
            case SOUTH_EAST:
                return BlockFace.SOUTH_WEST;
            default:
                return blockFace;
        }
    }

    @SuppressWarnings("deprecation")
	public static byte getSignFacingByteFromBlockFace(final BlockFace blockFace) {
        Sign signFacing = new Sign(Material.WALL_SIGN);
        signFacing.setFacingDirection(blockFace);

        return signFacing.getData();
    }

    public static boolean isSameBlock(final Block b1, final Block b2) {
        if ((b1 == null) || (b2 == null)) {
            return false;
        }

        return (b1.getX() == b2.getX()) && (b1.getY() == b2.getY()) && (b1.getZ() == b2.getZ());
    }

    public static void scheduleChunkLoad(final Block b) {
        final World w = b.getWorld();
        final Chunk c = b.getChunk();
        if (StarGates.getWorldHandler() != null) {
            StarGates.getWorldHandler().addStickyChunk(c, "WormholeXTreme");
        } else {
            final int cX = c.getX();
            final int cZ = c.getZ();
            if (!w.isChunkLoaded(cX, cZ)) {
                SGLogger.prettyLog(Level.FINE, false, "Loading chunk: " + c.toString() + " on: " + w.getName());
                w.loadChunk(cX, cZ);
            }
        }
    }

    public static void scheduleChunkUnload(final Block b) {
        final World w = b.getWorld();
        final Chunk c = b.getChunk();
        if (StarGates.getWorldHandler() != null) {
            StarGates.getWorldHandler().removeStickyChunk(c, "WormholeXTreme");
        } else {
            final int cX = c.getX();
            final int cZ = c.getZ();
            if (w.isChunkLoaded(cX, cZ)) {
                SGLogger.prettyLog(Level.FINE, false, "Scheduling chunk unload: " + c.toString() + " on: " + w.getName());
                w.unloadChunkRequest(cX, cZ);
            }
        }
    }
}
