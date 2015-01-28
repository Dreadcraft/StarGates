package net.doodcraft.Dooder07.Stargates.Wormhole.model;

import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StargateShape {

    private String shapeName = "Standard";
    private int[][] shapeStructurePositions = {{0, 2, 0}, {0, 3, 0}, {0, 4, 0}, {0, 1, 1}, {0, 5, 1}, {0, 0, 2},
        {0, 6, 2}, {0, 6, 3}, {0, 0, 3}, {0, 0, 4}, {0, 6, 4}, {0, 5, 5}, {0, 1, 5}, {0, 2, 6}, {0, 3, 6}, {0, 4, 6}};
    private int[] shapeSignPosition = {0, 3, 6};
    private int[] shapeEnterPosition = {0, 0, 3};
    private int[] shapeLightPositions = {3, 4, 11, 12};
    private int[][] shapePortalPositions = {{0, 2, 1}, {0, 3, 1}, {0, 4, 1}, {0, 1, 2}, {0, 2, 2}, {0, 3, 2},
        {0, 4, 2}, {0, 5, 2}, {0, 1, 3}, {0, 2, 3}, {0, 3, 3}, {0, 4, 3}, {0, 5, 3}, {0, 1, 4}, {0, 2, 4}, {0, 3, 4},
        {0, 4, 4}, {0, 5, 4}, {0, 2, 5}, {0, 3, 5}, {0, 4, 5}};
    private int[] shapeReferenceVector = {0, 1, 0};
    private int[] shapeToGateCorner = {1, -1, 4};
    private int shapeWooshDepth = 0;
    private int shapeWooshDepthSquared = 0;
    private Material shapePortalMaterial = Material.STATIONARY_WATER;
    private Material shapeIrisMaterial = Material.STONE;
    private Material shapeStructureMaterial = Material.OBSIDIAN;
    private Material shapeLightMaterial = Material.GLOWSTONE;
    private int shapeWooshTicks = 3;
    private int shapeLightTicks = 3;


    public StargateShape() {
    }

    public StargateShape(final String[] file_data) {
        this.setShapeSignPosition(new int[]{});
        this.setShapeEnterPosition(new int[]{});

        final ArrayList<Integer[]> blockPositions = new ArrayList<Integer[]>();
        final ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>();
        final ArrayList<Integer> lightPositions = new ArrayList<Integer>();

        int numBlocks = 0;
        int curWooshDepth = 0;

        int height = 0;
        int width = 0;
        for (int i = 0; i < file_data.length; i++) {
            final String line = file_data[i];

            if (line.contains("Name=")) {
                shapeName = line.split("=")[1];
                SGLogger.prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + shapeName + "\"");
            } else if (line.equals("GateShape=")) {
                int index = i + 1;
                while (file_data[index].startsWith("[")) {
                    if (width <= 0) {
                        final Pattern p = Pattern.compile("(\\[.*?\\])");
                        final Matcher m = p.matcher(file_data[index]);
                        while (m.find()) {
                            width++;
                        }
                    }

                    height++;
                    index++;
                }

                if ((height <= 0) || (width <= 0)) {
                    SGLogger.prettyLog(Level.SEVERE, false, "Unable to parse custom gate due to incorrect height or width: \"" + shapeName + "\"");
                    throw new IllegalArgumentException("Unable to parse custom gate due to incorrect height or width: \"" + shapeName + "\"");
                } else {
                    SGLogger.prettyLog(Level.CONFIG, false, "Shape: \"" + shapeName + "\"" + " Height: \"" + Integer.toString(height) + "\"" + " Width: \"" + Integer.toString(width) + "\"");
                }

                index = i + 1;
                while (file_data[index].startsWith("[")) {

                    final Pattern p = Pattern.compile("(\\[.*?\\])");
                    final Matcher m = p.matcher(file_data[index]);
                    int j = 0;
                    while (m.find()) {
                        final String block = m.group(0);
                        final Integer[] point = {0, (height - 1 - (index - i - 1)), (width - 1 - j)};
                        if (block.contains("O")) {
                            numBlocks++;
                            blockPositions.add(point);
                        } else if (block.contains("P")) {
                            portalPositions.add(point);
                        }

                        if (block.contains("S") || block.contains("E")) {
                            final int[] pointI = new int[3];
                            for (int k = 0; k < 3; k++) {
                                pointI[k] = point[k];
                            }

                            if (block.contains("S")) {
                                this.setShapeSignPosition(pointI);
                            }
                            if (block.contains("E")) {
                                this.setShapeEnterPosition(pointI);
                            }
                        }

                        if (block.contains("L") && block.contains("O")) {
                            lightPositions.add(numBlocks - 1);
                        }

                        j++;
                    }
                    index++;
                }
            } else if (line.contains("BUTTON_UP")) {
                this.getShapeToGateCorner()[1] = Integer.parseInt(line.split("=")[1]);
            } else if (line.contains("BUTTON_RIGHT")) {
                this.getShapeToGateCorner()[0] = Integer.parseInt(line.split("=")[1]);
            } else if (line.contains("BUTTON_AWAY")) {
                this.getShapeToGateCorner()[2] = Integer.parseInt(line.split("=")[1]);
            } else if (line.contains("WOOSH_DEPTH")) {
                curWooshDepth = Integer.parseInt(line.split("=")[1]);
            } else if (line.contains("PORTAL_MATERIAL")) {
                this.setShapePortalMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("IRIS_MATERIAL")) {
                this.setShapeIrisMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("STARGATE_MATERIAL")) {
                this.setShapeStructureMaterial(Material.valueOf(line.split("=")[1]));
            } else if (line.contains("ACTIVE_MATERIAL")) {
                this.setShapeLightMaterial(Material.valueOf(line.split("=")[1]));
            }
        }
        //TODO: debug printout for the materials the gate uses.
        //TODO: debug printout for the redstone_activated
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(this.getShapeSignPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(this.getShapeEnterPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Button Position [Left/Right,Up/Down,Forward/Back]: \"" + Arrays.toString(getShapeToGateCorner()) + "\"");

        final int[][] tempPortalPositions = new int[portalPositions.size()][3];
        for (int i = 0; i < portalPositions.size(); i++) {
            final int[] point = new int[3];
            for (int j = 0; j < 3; j++) {
                point[j] = portalPositions.get(i)[j];
            }
            tempPortalPositions[i] = point;
        }
        
        this.setShapePortalPositions(tempPortalPositions);
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString(this.getShapePortalPositions()) + "\"");

        final int[] tempLightPositions = new int[lightPositions.size()];
        for (int i = 0; i < lightPositions.size(); i++) {
            tempLightPositions[i] = lightPositions.get(i);
        }
        
        this.setShapeLightPositions(tempLightPositions);
        SGLogger.prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + Arrays.toString(this.getShapeLightPositions()) + "\"");

        final int[][] tempStructurePositions = new int[blockPositions.size()][3];
        for (int i = 0; i < blockPositions.size(); i++) {
            final int[] point = new int[3];
            for (int j = 0; j < 3; j++) {
                point[j] = blockPositions.get(i)[j];
            }
            tempStructurePositions[i] = point;
        }
        
        this.setShapeStructurePositions(tempStructurePositions);
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString(this.getShapeStructurePositions()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + shapeName + "\"");

        this.setShapeWooshDepth(curWooshDepth);
        this.setShapeWooshDepthSquared(curWooshDepth * curWooshDepth);
    }

    public final int[] getShapeEnterPosition() {
        return shapeEnterPosition.clone();
    }

    public Material getShapeIrisMaterial() {
        return shapeIrisMaterial;
    }

    public Material getShapeLightMaterial() {
        return shapeLightMaterial;
    }

    public final int[] getShapeLightPositions() {
        return shapeLightPositions.clone();
    }

    public int getShapeLightTicks() {
        return shapeLightTicks;
    }

    public String getShapeName() {
        return shapeName;
    }

    public String getShapeNameKey() {
        return shapeName.toLowerCase();
    }

    public Material getShapePortalMaterial() {
        return shapePortalMaterial;
    }

    public final int[][] getShapePortalPositions() {
        return shapePortalPositions.clone();
    }

    public int[] getShapeReferenceVector() {
        return shapeReferenceVector.clone();
    }

    public final int[] getShapeSignPosition() {
        return shapeSignPosition.clone();
    }

    public Material getShapeStructureMaterial() {
        return shapeStructureMaterial;
    }

    public int[][] getShapeStructurePositions() {
        return shapeStructurePositions.clone();
    }

    public final int[] getShapeToGateCorner() {
        return shapeToGateCorner.clone();
    }

    public int getShapeWooshDepth() {
        return shapeWooshDepth;
    }

    public int getShapeWooshDepthSquared() {
        return shapeWooshDepthSquared;
    }

    public int getShapeWooshTicks() {
        return shapeWooshTicks;
    }

    public final void setShapeEnterPosition(final int[] shapeEnterPosition) {
        this.shapeEnterPosition = shapeEnterPosition.clone();
    }

    public final void setShapeIrisMaterial(final Material shapeIrisMaterial) {
        this.shapeIrisMaterial = shapeIrisMaterial;
    }

    public final void setShapeLightMaterial(final Material shapeLightMaterial) {
        this.shapeLightMaterial = shapeLightMaterial;
    }

    public final void setShapeLightPositions(final int[] shapeLightPositions) {
        this.shapeLightPositions = shapeLightPositions.clone();
    }

    public void setShapeLightTicks(final int shapeLightTicks) {
        this.shapeLightTicks = shapeLightTicks;
    }

    public void setShapeName(final String shapeName) {
        this.shapeName = shapeName;
    }

    public final void setShapePortalMaterial(final Material shapePortalMaterial) {
        this.shapePortalMaterial = shapePortalMaterial;
    }

    public final void setShapePortalPositions(final int[][] shapePortalPositions) {
        this.shapePortalPositions = shapePortalPositions.clone();
    }

    public void setShapeReferenceVector(final int[] shapeReferenceVector) {
        this.shapeReferenceVector = shapeReferenceVector.clone();
    }

    public final void setShapeSignPosition(final int[] shapeSignPosition) {
        this.shapeSignPosition = shapeSignPosition.clone();
    }

    public final void setShapeStructureMaterial(final Material shapeStructureMaterial) {
        this.shapeStructureMaterial = shapeStructureMaterial;
    }

    public final void setShapeStructurePositions(final int[][] shapeStructurePositions) {
        this.shapeStructurePositions = shapeStructurePositions.clone();
    }

    public void setShapeToGateCorner(final int[] shapeToGateCorner) {
        this.shapeToGateCorner = shapeToGateCorner.clone();
    }

    public final void setShapeWooshDepth(final int shapeWooshDepth) {
        this.shapeWooshDepth = shapeWooshDepth;
    }

    public final void setShapeWooshDepthSquared(final int shapeWooshDepthSquared) {
        this.shapeWooshDepthSquared = shapeWooshDepthSquared;
    }

    public void setShapeWooshTicks(final int shapeWooshTicks) {
        this.shapeWooshTicks = shapeWooshTicks;
    }
}
