package net.doodcraft.Dooder07.Stargates.Wormhole.logic;

import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate3DShape;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateShape;

public class StargateShapeFactory {

    private static StargateShape create2DShape(final String[] fileLines) {
        return new StargateShape(fileLines);
    }

    private static Stargate3DShape create3DShape(final String[] fileLines) {
        return new Stargate3DShape(fileLines);
    }

    protected static StargateShape createShapeFromFile(final String[] fileLines) {
        for (final String line : fileLines) {
            if (line.startsWith("Version=2")) {
                return create3DShape(fileLines);
            }
        }

        return create2DShape(fileLines);
    }
}
