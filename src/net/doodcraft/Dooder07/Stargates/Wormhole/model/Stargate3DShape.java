package net.doodcraft.Dooder07.Stargates.Wormhole.model;


import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stargate3DShape extends StargateShape {

    private final ArrayList<StargateShapeLayer> shapeLayers = new ArrayList<StargateShapeLayer>();
    private int shapeActivationLayer = -1;
    private int shapeSignLayer = -1;
    private boolean shapeRedstoneActivated = false;

    public Stargate3DShape(final String[] fileLines) {
        setShapeSignPosition(new int[]{});
        setShapeEnterPosition(new int[]{});

        int height = 0;
        int width = 0;
        int wooshDepth = 0;
        for (int i = 0; i < fileLines.length; i++) {
            final String line = fileLines[i];

            if (line.startsWith("#")) {
                continue;
            }

            if (line.contains("Name=")) {
                setShapeName(line.split("=")[1]);
                SGLogger.prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + getShapeName() + "\"");
            } else if (line.equals("GateShape=")) {
                int index = i;

                while (!fileLines[index].startsWith("[")) {
                    index++;
                }

                while (fileLines[index].startsWith("[")) {
                    if (width <= 0) {
                        final Pattern p = Pattern.compile("(\\[.*?\\])");
                        final Matcher m = p.matcher(fileLines[index]);
                        while (m.find()) {
                            width++;
                        }
                    }

                    height++;
                    index++;
                }

                if ((height <= 0) || (width <= 0)) {
                    SGLogger.prettyLog(Level.SEVERE, false, "Unable to parse custom gate due to incorrect height or width: \"" + getShapeName() + "\"");
                    throw new IllegalArgumentException("Unable to parse custom gate due to incorrect height or width: \"" + getShapeName() + "\"");
                } else {
                    SGLogger.prettyLog(Level.CONFIG, false, "Shape: \"" + getShapeName() + "\"" + " Height: \"" + Integer.toString(height) + "\"" + " Width: \"" + Integer.toString(width) + "\"");
                }
            } else if (line.startsWith("Layer")) {
                // TODO : Add some debug output for each layer!
                // 1. get layer #
                final int layer = Integer.valueOf(line.trim().split("[#=]")[1]);

                // 2. add each line that starts with [ to a new string[]
                i++;
                final String[] layerLines = new String[height];
                int line_index = 0;
                while (fileLines[i].startsWith("[") || fileLines[i].startsWith("#")) {
                    SGLogger.prettyLog(Level.CONFIG, false, "Layer=" + layer + " i=" + i + " line_index=" + line_index + " Line=" + fileLines[i]);
                    layerLines[line_index] = fileLines[i];
                    i++;

                    if (fileLines[i].startsWith("#")) {
                        continue;
                    }

                    line_index++;
                }

                // 3. call constructor
                final StargateShapeLayer ssl = new StargateShapeLayer(layerLines, height, width);
                // bad hack to make sure list is big enough :(
                while (getShapeLayers().size() <= layer) {
                    getShapeLayers().add(null);
                }
                getShapeLayers().set(layer, ssl);

                if (ssl.getLayerActivationPosition().length > 0) {
                    setShapeActivationLayer(layer);
                }
                if (ssl.getLayerDialSignPosition().length > 0) {
                    setShapeSignLayer(layer);
                }
                if ((ssl.getLayerPlayerExitPosition() != null) && (ssl.getLayerPlayerExitPosition().length == 3)) {
                    // This is only so we know it has been set or not and can warn players
                    setShapeEnterPosition(ssl.getLayerPlayerExitPosition());
                }
                if (ssl.getLayerWooshPositions().size() > 0) {
                    wooshDepth++;
                }
            } else if (line.contains("PORTAL_MATERIAL=") && (line.split("=").length > 1)) {
                setShapePortalMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("IRIS_MATERIAL=") && (line.split("=").length > 1)) {
                setShapeIrisMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("STARGATE_MATERIAL=") && (line.split("=").length > 1)) {
                setShapeStructureMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("ACTIVE_MATERIAL=") && (line.split("=").length > 1)) {
                setShapeLightMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("LIGHT_TICKS=") && (line.split("=").length > 1)) {
                setShapeLightTicks(Integer.valueOf(line.split("=")[1]));
            } else if (line.contains("WOOSH_TICKS=") && (line.split("=").length > 1)) {
                setShapeWooshTicks(Integer.valueOf(line.split("=")[1]));
            } else if (line.startsWith("REDSTONE_ACTIVATED=") && (line.split("=").length > 1)) {
                setShapeRedstoneActivated(Boolean.valueOf(line.split("=")[1]));
            }
        }

        setShapeWooshDepth(wooshDepth > 0
                ? wooshDepth
                : 0);
        setShapeWooshDepthSquared(getShapeWooshDepth() * getShapeWooshDepth());

        if (getShapeEnterPosition().length != 3) {
            SGLogger.prettyLog(Level.SEVERE, false, "Shape: \"" + getShapeName() + "\" does not have an entrance/exit point for players to teleport in. This will cause errors.");
            throw new IllegalArgumentException("Shape: \"" + getShapeName() + "\" does not have an enterance point for players to teleport in. This will cause errors.");
        }

        SGLogger.prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + getShapeName() + "\"");
    }

    public int getShapeActivationLayer() {
        return shapeActivationLayer;
    }

    public ArrayList<StargateShapeLayer> getShapeLayers() {
        return shapeLayers;
    }

    public int getShapeSignLayer() {
        return shapeSignLayer;
    }

    public boolean isShapeRedstoneActivated() {
        return shapeRedstoneActivated;
    }

    private void setShapeActivationLayer(final int shapeActivationLayer) {
        this.shapeActivationLayer = shapeActivationLayer;
    }

    private void setShapeRedstoneActivated(final boolean shapeRedstoneActivated) {
        this.shapeRedstoneActivated = shapeRedstoneActivated;
    }

    private void setShapeSignLayer(final int shapeSignLayer) {
        this.shapeSignLayer = shapeSignLayer;
    }
}
