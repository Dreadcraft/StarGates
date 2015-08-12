package net.doodcraft.Dooder07.Stargates.Wormhole.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.xml.stream.Location;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable.ActionToTake;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate3DShape;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateNetwork;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateShape;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateShapeLayer;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.DataUtils;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.WorldUtils;

public class StargateHelper {

    private static final ConcurrentHashMap<String, StargateShape> stargateShapes = new ConcurrentHashMap<String, StargateShape>();
    private static final byte StargateSaveVersion = 8;
    private static final byte[] emptyBlock = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static Stargate checkStargate(final Block buttonBlock, final BlockFace facing) {
        Stargate s = null;
        for (final String key : getStargateShapes().keySet()) {
            final StargateShape shape = getStargateShapes().get(key);
            if (shape != null) {
                s = shape instanceof Stargate3DShape
                        ? checkStargate3D(buttonBlock, facing, (Stargate3DShape) shape, false)
                        : checkStargate(buttonBlock, facing, shape, false);
                if (s != null) {
                    SGLogger.prettyLog(Level.FINE, false, "Shape: " + shape.getShapeName() + " was found!");
                    break;
                }
            }
        }
        return s;
    }

    public static Stargate checkStargate(final Block buttonBlock, final BlockFace facing, final StargateShape shape) {
        if (shape instanceof Stargate3DShape) {
            return checkStargate3D(buttonBlock, facing, (Stargate3DShape) shape, true);
        } else {
            return checkStargate(buttonBlock, facing, shape, true);
        }
    }

    @SuppressWarnings({ "incomplete-switch", "deprecation" })
	private static Stargate checkStargate(final Block buttonBlock, final BlockFace facing, final StargateShape shape, final boolean create) {
        final BlockFace opposite = WorldUtils.getInverseDirection(facing);
        final Block holdingBlock = buttonBlock.getRelative(opposite);

        if (isStargateMaterial(holdingBlock, shape)) {
            final Stargate tempGate = new Stargate();
            tempGate.setGateWorld(buttonBlock.getWorld());
            tempGate.setGateName("");
            tempGate.setGateDialLeverBlock(buttonBlock);
            tempGate.setGateFacing(facing);
            tempGate.getGateStructureBlocks().add(buttonBlock.getLocation());
            tempGate.setGateShape(shape);
            if (!isStargateMaterial(holdingBlock.getRelative(BlockFace.DOWN), tempGate.getGateShape())) {
                return null;
            }

            final Block possibleSignHolder = holdingBlock.getRelative(WorldUtils.getPerpendicularRightDirection(opposite));
            if (isStargateMaterial(possibleSignHolder, tempGate.getGateShape())) {
                final Block signBlock = possibleSignHolder.getRelative(tempGate.getGateFacing());
                if (!tryCreateGateSign(signBlock, tempGate) && tempGate.isGateSignPowered()) {
                    return tempGate;
                }
            }

            final int[] facingVector = {0, 0, 0};

            final World w = buttonBlock.getWorld();
            switch (facing) {
                case NORTH:
                    facingVector[0] = 1;
                    break;
                case SOUTH:
                    facingVector[0] = -1;
                    break;
                case EAST:
                    facingVector[2] = 1;
                    break;
                case WEST:
                    facingVector[2] = -1;
                    break;
                case UP:
                    facingVector[1] = -1;
                    break;
                case DOWN:
                    facingVector[1] = 1;
                    break;
            }

            final int[] directionVector = {0, 0, 0};
            final int[] startingPosition = {0, 0, 0};

            directionVector[0] = facingVector[1] * shape.getShapeReferenceVector()[2] - facingVector[2] * shape.getShapeReferenceVector()[1];
            directionVector[1] = facingVector[2] * shape.getShapeReferenceVector()[0] - facingVector[0] * shape.getShapeReferenceVector()[2];
            directionVector[2] = facingVector[0] * shape.getShapeReferenceVector()[1] - facingVector[1] * shape.getShapeReferenceVector()[0];

            startingPosition[0] = buttonBlock.getX() + facingVector[0] * shape.getShapeToGateCorner()[2] + directionVector[0] * shape.getShapeToGateCorner()[0];
            startingPosition[1] = buttonBlock.getY() + shape.getShapeToGateCorner()[1];
            startingPosition[2] = buttonBlock.getZ() + facingVector[2] * shape.getShapeToGateCorner()[2] + directionVector[2] * shape.getShapeToGateCorner()[0];

            for (int i = 0; i < shape.getShapeStructurePositions().length; i++) {
                final int[] bVect = shape.getShapeStructurePositions()[i];

                final int[] blockLocation = {bVect[2] * directionVector[0] * -1, bVect[1],
                    bVect[2] * directionVector[2] * -1};

                final Block maybeBlock = w.getBlockAt(blockLocation[0] + startingPosition[0], blockLocation[1] + startingPosition[1], blockLocation[2] + startingPosition[2]);
                if (create) {
                    maybeBlock.setType(tempGate.getGateShape().getShapeStructureMaterial());
                }

                if (isStargateMaterial(maybeBlock, tempGate.getGateShape())) {
                    tempGate.getGateStructureBlocks().add(maybeBlock.getLocation());
                    for (final int lightPosition : shape.getShapeLightPositions()) {
                        if (lightPosition == i) {
                            while (tempGate.getGateLightBlocks().size() < 2) {
                                tempGate.getGateLightBlocks().add(new ArrayList<Location>());
                            }
                            tempGate.getGateLightBlocks().get(1).add(maybeBlock.getLocation());
                        }
                    }
                } else {
                    if (tempGate.getGateNetwork() != null) {
                        tempGate.getGateNetwork().getNetworkGateList().remove(tempGate);
                        if (tempGate.isGateSignPowered()) {
                            tempGate.getGateNetwork().getNetworkSignGateList().remove(tempGate);
                        }
                    }
                    return null;
                }
            }

            if (shape.getShapeSignPosition().length > 0) {
                final int[] signLocationArray = {shape.getShapeSignPosition()[2] * directionVector[0] * -1,
                    shape.getShapeSignPosition()[1], shape.getShapeSignPosition()[2] * directionVector[2] * -1};
                final Block nameBlock = w.getBlockAt(signLocationArray[0] + startingPosition[0], signLocationArray[1] + startingPosition[1], signLocationArray[2] + startingPosition[2]);
                tempGate.setGateNameBlockHolder(nameBlock);
            }
            final int[] teleportLocArray = {shape.getShapeEnterPosition()[2] * directionVector[0] * -1,
                shape.getShapeEnterPosition()[1], shape.getShapeEnterPosition()[2] * directionVector[2] * -1};
            final Block teleBlock = w.getBlockAt(teleportLocArray[0] + startingPosition[0], teleportLocArray[1] + startingPosition[1], teleportLocArray[2] + startingPosition[2]);
            Block bLoc = teleBlock.getRelative(facing);
            while ((bLoc.getTypeId() != 0) && (bLoc.getTypeId() != 8)) {
                bLoc = bLoc.getRelative(BlockFace.UP);
            }
            final Location teleLoc = bLoc.getLocation();
            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(facing));
            teleLoc.setPitch(0);
            teleLoc.setX(teleLoc.getX() + 0.5);
            teleLoc.setY(teleLoc.getY() + 0.66);
            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGatePlayerTeleportLocation(teleLoc);

            for (final int[] bVect : shape.getShapePortalPositions()) {
                final int[] blockLocation = {bVect[2] * directionVector[0] * -1, bVect[1],
                    bVect[2] * directionVector[2] * -1};

                final Block maybeBlock = w.getBlockAt(blockLocation[0] + startingPosition[0], blockLocation[1] + startingPosition[1], blockLocation[2] + startingPosition[2]);
                if (maybeBlock.getTypeId() == 0) {
                    tempGate.getGatePortalBlocks().add(maybeBlock.getLocation());
                } else {
                    if (tempGate.getGateNetwork() != null) {
                        tempGate.getGateNetwork().getNetworkGateList().remove(tempGate);
                    }

                    return null;
                }
            }

            setupSignGateNetwork(tempGate);
            return tempGate;
        }

        return null;
    }
	
    @SuppressWarnings("incomplete-switch")
	private static Stargate checkStargate3D(final Block buttonBlock, final BlockFace facing, final Stargate3DShape shape, final boolean create) {
        final Stargate s = new Stargate();
        s.setGateWorld(buttonBlock.getWorld());
        s.setGateDialLeverBlock(buttonBlock);
        s.getGateStructureBlocks().add(s.getGateDialLeverBlock().getLocation());
        s.setGateShape(shape);
        s.setGateFacing(facing);

        final BlockFace opposite = WorldUtils.getInverseDirection(facing);
        final Block activationBlock = buttonBlock.getRelative(opposite);
        final StargateShapeLayer act_layer = shape.getShapeLayers().get(shape.getShapeActivationLayer());

        final int[] facingVector = {0, 0, 0};
		
        switch (facing) {
            case NORTH:
                facingVector[2] = -1;
                break;
            case SOUTH:
                facingVector[2] = 1;
                break;
            case EAST:
                facingVector[0] = 1;
                break;
            case WEST:
                facingVector[0] = -1;
                break;
            case UP:
                facingVector[1] = 1;
                break;
            case DOWN:
                facingVector[1] = -1;
                break;
        }

        final int[] directionVector = {0, 0, 0};
        final int[] startingPosition = {0, 0, 0};

        directionVector[0] = facingVector[1] * shape.getShapeReferenceVector()[2] - facingVector[2] * shape.getShapeReferenceVector()[1];
        directionVector[1] = facingVector[2] * shape.getShapeReferenceVector()[0] - facingVector[0] * shape.getShapeReferenceVector()[2];
        directionVector[2] = facingVector[0] * shape.getShapeReferenceVector()[1] - facingVector[1] * shape.getShapeReferenceVector()[0];

        startingPosition[0] = activationBlock.getX() - directionVector[0] * act_layer.getLayerActivationPosition()[2];
        startingPosition[1] = activationBlock.getY() - act_layer.getLayerActivationPosition()[1];
        startingPosition[2] = activationBlock.getZ() - directionVector[2] * act_layer.getLayerActivationPosition()[2];

        for (int i = 0; i < shape.getShapeLayers().size(); i++) {
            if ((shape.getShapeLayers().size() > i) && (shape.getShapeLayers().get(i) != null)) {
                final int layerOffset = shape.getShapeActivationLayer() - i;
                final int[] layerStarter = {startingPosition[0] - facingVector[0] * layerOffset, startingPosition[1],
                    startingPosition[2] - facingVector[2] * layerOffset};
                if (!checkStargateLayer(shape.getShapeLayers().get(i), layerStarter, directionVector, s, create)) {
                    if (s.getGateNetwork() != null) {
                        s.getGateNetwork().getNetworkGateList().remove(s);
                        if (s.isGateSignPowered()) {
                            s.getGateNetwork().getNetworkSignGateList().remove(s);
                        }
                    }
                    return null;
                }
            }
        }
        if (shape.getShapeSignPosition().length > 0) {
            final int[] signLocationArray = {shape.getShapeSignPosition()[2] * directionVector[0] * -1,
                shape.getShapeSignPosition()[1], shape.getShapeSignPosition()[2] * directionVector[2] * -1};
            final Block nameBlock = s.getGateWorld().getBlockAt(signLocationArray[0] + startingPosition[0], signLocationArray[1] + startingPosition[1], signLocationArray[2] + startingPosition[2]);
            s.setGateNameBlockHolder(nameBlock);
        }
        if (shape.isShapeRedstoneActivated()) {
            s.setGateRedstonePowered(true);
        }
        setupSignGateNetwork(s);
        return s;
    }

    @SuppressWarnings("deprecation")
	private static boolean checkStargateLayer(final StargateShapeLayer layer, final int[] lowerCorner, final int[] directionVector, final Stargate tempGate, final boolean create) {
        final World w = tempGate.getGateWorld();
        for (int i = 0; i < layer.getLayerBlockPositions().size(); i++) {
            final Block maybeBlock = getBlockFromVector(layer.getLayerBlockPositions().get(i), directionVector, lowerCorner, w);

            if (create) {
                maybeBlock.setType(tempGate.getGateShape().getShapeStructureMaterial());
            }

            if (isStargateMaterial(maybeBlock, tempGate.getGateShape())) {
                tempGate.getGateStructureBlocks().add(maybeBlock.getLocation());
            } else {
                return false;
            }
        }
		
        for (int i = 0; i < layer.getLayerPortalPositions().size(); i++) {
            final Block maybeBlock = getBlockFromVector(layer.getLayerPortalPositions().get(i), directionVector, lowerCorner, w);

            if (create) {
                maybeBlock.setType(Material.AIR);
            }

            if (maybeBlock.getTypeId() == 0) {
                tempGate.getGatePortalBlocks().add(maybeBlock.getLocation());
            } else {
                return false;
            }
        }

        if (layer.getLayerPlayerExitPosition().length > 0) {
            Block teleBlock = StargateHelper.getBlockFromVector(layer.getLayerPlayerExitPosition(), directionVector, lowerCorner, w);

            while ((teleBlock.getTypeId() != 0) && (teleBlock.getTypeId() != 8)) {
                teleBlock = teleBlock.getRelative(BlockFace.UP);
            }
            final Location teleLoc = teleBlock.getLocation();

            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(tempGate.getGateFacing()));
            teleLoc.setPitch(0);

            teleLoc.setX(teleLoc.getX() + 0.5);

            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGatePlayerTeleportLocation(teleLoc);
        }


        if (layer.getLayerMinecartExitPosition().length > 0) {
            Block teleBlock = StargateHelper.getBlockFromVector(layer.getLayerMinecartExitPosition(), directionVector, lowerCorner, w);
            
            while ((!(teleBlock.getType().equals(Material.AIR))) && (!(teleBlock.getType().equals(Material.WATER)))) {
                teleBlock = teleBlock.getRelative(BlockFace.UP);
            }
            
            final Location teleLoc = teleBlock.getLocation();

            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(tempGate.getGateFacing()));
            teleLoc.setPitch(0);
            teleLoc.setX(teleLoc.getX() + 0.5);
            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGateMinecartTeleportLocation(teleLoc);
        }

        for (int i = 0; i < layer.getLayerWooshPositions().size(); i++) {
            if (tempGate.getGateWooshBlocks().size() < i + 1) {
                tempGate.getGateWooshBlocks().add(new ArrayList<Location>());
            }
            if (layer.getLayerWooshPositions().get(i) != null) {
                for (final Integer[] position : layer.getLayerWooshPositions().get(i)) {
                    final Block wooshBlock = StargateHelper.getBlockFromVector(position, directionVector, lowerCorner, w);
                    tempGate.getGateWooshBlocks().get(i).add(wooshBlock.getLocation());
                }
            }
        }

        for (int i = 0; i < layer.getLayerLightPositions().size(); i++) {
            if (tempGate.getGateLightBlocks().size() < i + 1) {
                tempGate.getGateLightBlocks().add(new ArrayList<Location>());
            }
            if (layer.getLayerLightPositions().get(i) != null) {
                for (final Integer[] position : layer.getLayerLightPositions().get(i)) {
                    final Block lightBlock = StargateHelper.getBlockFromVector(position, directionVector, lowerCorner, w);
                    tempGate.getGateLightBlocks().get(i).add(lightBlock.getLocation());
                }
            }
        }

        if (layer.getLayerDialSignPosition().length > 0) {
            final Block signBlockHolder = StargateHelper.getBlockFromVector(layer.getLayerDialSignPosition(), directionVector, lowerCorner, w);
            final Block signBlock = signBlockHolder.getRelative(tempGate.getGateFacing());

            if (!tryCreateGateSign(signBlock, tempGate) && tempGate.isGateSignPowered()) {
                return false;
            } else if (tempGate.isGateSignPowered()) {

                tempGate.getGateStructureBlocks().add(signBlock.getLocation());
            }

        }
        if (layer.getLayerNameSignPosition().length > 0) {
            tempGate.setGateNameBlockHolder(StargateHelper.getBlockFromVector(layer.getLayerNameSignPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneDialActivationPosition().length > 0) {
            tempGate.setGateRedstoneDialActivationBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneDialActivationPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneSignActivationPosition().length > 0) {
            tempGate.setGateRedstoneSignActivationBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneSignActivationPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneGateActivatedPosition().length > 0) {
            tempGate.setGateRedstoneGateActivatedBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneGateActivatedPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerIrisActivationPosition().length > 0) {
            tempGate.setGateIrisLeverBlock(StargateHelper.getBlockFromVector(layer.getLayerIrisActivationPosition(), directionVector, lowerCorner, w).getRelative(tempGate.getGateFacing()));
            tempGate.getGateStructureBlocks().add(tempGate.getGateIrisLeverBlock().getLocation());
        }

        return true;
    }

    private static Block getBlockFromVector(final int[] bVect, final int[] directionVector, final int[] lowerCorner, final World w) {

        final int[] blockLocation = {bVect[2] * directionVector[0], bVect[1], bVect[2] * directionVector[2]};

        return w.getBlockAt(blockLocation[0] + lowerCorner[0], blockLocation[1] + lowerCorner[1], blockLocation[2] + lowerCorner[2]);
    }

    private static Block getBlockFromVector(final Integer[] bVect, final int[] directionVector, final int[] lowerCorner, final World w) {

        final int[] blockLocation = {bVect[2] * directionVector[0], bVect[1], bVect[2] * directionVector[2]};

        return w.getBlockAt(blockLocation[0] + lowerCorner[0], blockLocation[1] + lowerCorner[1], blockLocation[2] + lowerCorner[2]);
    }

    public static List<String> getShapeNames() {
        List<String> shapeNames = new ArrayList<String>();
        for (String shapeName: getStargateShapes().keySet()) {
            shapeNames.add(getStargateShapeName(shapeName));
        }
        return shapeNames;
    }

    public static StargateShape getStargateShape(String shapeName) {
        shapeName = shapeName.toLowerCase();
        if (!getStargateShapes().containsKey(shapeName)) {
            return null;
        }

        return getStargateShapes().get(shapeName);
    }

    public static String getStargateShapeName(String shapeName) {
        shapeName = shapeName.toLowerCase();
        if (!getStargateShapes().containsKey(shapeName)) {
            return null;
        }

        return getStargateShapes().get(shapeName).getShapeName();
    }

    private static ConcurrentHashMap<String, StargateShape> getStargateShapes() {
        return stargateShapes;
    }

    @SuppressWarnings("deprecation")
	private static boolean isStargateMaterial(final Block b, final StargateShape s) {
        return b.getTypeId() == s.getShapeStructureMaterial().getId();
    }

    public static boolean isStargateShape(String name) {
        return getStargateShapes().containsKey(name.toLowerCase());
    }
    
    public static void loadShapes() {
        final File directory = new File("plugins" + File.separator + "StarGates" + File.separator + "GateShapes" + File.separator);

        if (!directory.exists()) {
            try {
                directory.mkdir();
            } catch (final Exception e) {
                SGLogger.prettyLog(Level.SEVERE, false, "Unable to make directory: " + e.getMessage());
            }
        }

        final FilenameFilter filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return !name.startsWith(".") && name.endsWith(".shape");
            }
        };

        if (directory.exists() && (directory.listFiles(filenameFilter).length == 0)) {
            BufferedReader br = null;
            BufferedWriter bw = null;
            final String[] defaultShapeNames = {"Standard.shape", "StandardSignDial.shape", "Minimal.shape",
                "MinimalSignDial.shape", "Horizontal.shape", "HorizontalSignDial.shape"};
            try {
                for (String shape : defaultShapeNames) {
                    final File defaultShapeFile = new File("plugins" + File.separator + "StarGates" + File.separator + "GateShapes" + File.separator + shape);
                    final InputStream is = StarGates.class.getResourceAsStream("/GateShapes/3d/" + shape);
                    br = new BufferedReader(new InputStreamReader(is));
                    bw = new BufferedWriter(new FileWriter(defaultShapeFile));

                    for (String s = ""; (s = br.readLine()) != null;) {
                        bw.write(s);
                        bw.write("\n");
                    }

                    br.close();
                    bw.close();
                    is.close();
                }
            } catch (final IOException e) {
                SGLogger.prettyLog(Level.SEVERE, false, "Unable to create files: " + e.getMessage());
            } catch (final NullPointerException e) {
                SGLogger.prettyLog(Level.SEVERE, false, "Unable to create files: " + e.getMessage());
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (final IOException e) {
                    SGLogger.prettyLog(Level.FINE, false, e.getMessage());
                }
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (final IOException e) {
                    SGLogger.prettyLog(Level.FINE, false, e.getMessage());
                }
            }
        }

        final File[] shapeFiles = directory.listFiles(filenameFilter);
        for (final File fi : shapeFiles) {
            if (fi.getName().contains(".shape")) {
                SGLogger.prettyLog(Level.CONFIG, false, "Loading shape file: \"" + fi.getName() + "\"");
                BufferedReader bufferedReader = null;
                try {
                    final ArrayList<String> fileLines = new ArrayList<String>();
                    bufferedReader = new BufferedReader(new FileReader(fi));
                    for (String s = ""; (s = bufferedReader.readLine()) != null;) {
                        fileLines.add(s);
                    }
                    bufferedReader.close();

                    final StargateShape shape = StargateShapeFactory.createShapeFromFile(fileLines.toArray(new String[fileLines.size()]));

                    if (getStargateShapes().containsKey(shape.getShapeName())) {
                        SGLogger.prettyLog(Level.WARNING, false, "Shape File: " + fi.getName() + " contains shape name: " + shape.getShapeName() + " which already exists. This shape will be unavailable.");
                    } else {
                        getStargateShapes().put(shape.getShapeNameKey(), shape);
                    }
                } catch (final FileNotFoundException e) {
                    SGLogger.prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
                } catch (final IOException e) {
                    SGLogger.prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
                } finally {
                    try {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                    } catch (final IOException e) {
                        SGLogger.prettyLog(Level.FINE, false, e.getMessage());
                    }
                }
                SGLogger.prettyLog(Level.CONFIG, false, "Completed loading shape file: \"" + fi.getName() + "\"");
            }
        }

        if (getStargateShapes().size() == 0) {
            getStargateShapes().put("Standard", new StargateShape());
        }
    }

    public static Stargate parseVersionedData(final byte[] gate_data, final World w, final String name, final StargateNetwork network) {
        final Stargate s = new Stargate();
        s.setGateName(name);
        s.setGateNetwork(network);
        final ByteBuffer byteBuff = ByteBuffer.wrap(gate_data);

        s.setLoadedVersion(byteBuff.get());
        s.setGateWorld(w);

        switch (s.getLoadedVersion()) {
            case 3:
                SGLogger.prettyLog(Level.FINE, false, "Parsing version data: Using parser Version 3 for '" + name + '"');
                return parseVersionedDataV3(w, s, byteBuff);
            case 4:
                SGLogger.prettyLog(Level.FINE, false, "Parsing version data: Using parser Version 4 for '" + name + '"');
                return parseVersionedDataV4(w, s, byteBuff);
            case 5:
                SGLogger.prettyLog(Level.FINE, false, "Parsing version data: Using parser Version 5 for '" + name + '"');
                return parseVersionedDataV5(w, s, byteBuff);
            case 6:
                SGLogger.prettyLog(Level.FINE, false, "Parsing version data: Using parser Version 6 for '" + name + '"');
                return parseVersionedDataV6(w, s, byteBuff);
            case 7:
                SGLogger.prettyLog(Level.FINE, false, "Parsing version data: Using parser Version 7 for '" + name + '"');
                return parseVersionedDataV7(w, s, byteBuff);
            case 8:
                SGLogger.prettyLog(Level.FINE, false, "Parsing version data: Using parser Version 8 for '" + name + '"');
                return parseVersionedDataV8(w, s, byteBuff);
        }
        
        return null;
    }

    private static Stargate parseVersionedDataV3(World w, Stargate s, ByteBuffer byteBuff) {
        final byte[] locArray = new byte[32];
        final byte[] blocArray = new byte[12];

        byteBuff.get(blocArray);
        s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(locArray);
        s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

        byteBuff.get(blocArray);
        s.setGateDialSignIndex(byteBuff.getInt());
        s.setGateTempSignTarget(byteBuff.getInt());
        if (s.isGateSignPowered()) {
            s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

            if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk())) {
                try {
                    s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.WARNING, false, "[V3] Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    SGLogger.prettyLog(Level.FINE, false, "[V3] Stacktrace: " + e.getMessage());
                }
            }
        }

        s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateTempTargetId(byteBuff.getInt());

        final int facingSize = byteBuff.getInt();
        final byte[] strBytes = new byte[facingSize];
        byteBuff.get(strBytes);
        final String faceStr = new String(strBytes);
        s.setGateFacing(BlockFace.valueOf(faceStr));

        s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGatePlayerTeleportLocation().setPitch(0);

        final int idcLen = byteBuff.getInt();
        final byte[] idcBytes = new byte[idcLen];
        byteBuff.get(idcBytes);
        s.setGateIrisDeactivationCode(new String(idcBytes));

        s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));

        int numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateStructureBlocks().add(bl.getLocation());
        }

        numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGatePortalBlocks().add(bl.getLocation());
        }

        return s;
    }
    
    private static Stargate parseVersionedDataV4(World w, Stargate s, ByteBuffer byteBuff) {
        final byte[] locArray = new byte[32];
        final byte[] blocArray = new byte[12];

        byteBuff.get(blocArray);
        s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(locArray);
        s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

        byteBuff.get(blocArray);
        s.setGateDialSignIndex(byteBuff.getInt());
        s.setGateTempSignTarget(byteBuff.getLong());
        if (s.isGateSignPowered()) {
            s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

            if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk())) {
                try {
                    s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.WARNING, false, "[V4] Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    SGLogger.prettyLog(Level.FINE, false, "[V4] Stacktrace: " + e.getMessage());
                }
            }
        }

        s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateTempTargetId(byteBuff.getLong());

        final int facingSize = byteBuff.getInt();
        final byte[] strBytes = new byte[facingSize];
        byteBuff.get(strBytes);
        final String faceStr = new String(strBytes);
        s.setGateFacing(BlockFace.valueOf(faceStr));

        s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGatePlayerTeleportLocation().setPitch(0);

        final int idcLen = byteBuff.getInt();
        final byte[] idcBytes = new byte[idcLen];
        byteBuff.get(idcBytes);
        s.setGateIrisDeactivationCode(new String(idcBytes));

        s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateIrisDefaultActive(s.isGateIrisActive());
        int numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateStructureBlocks().add(bl.getLocation());
        }

        numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGatePortalBlocks().add(bl.getLocation());
        }

        return s;
    }
    
    private static Stargate parseVersionedDataV5(World w, Stargate s, ByteBuffer byteBuff) {
        final byte[] locArray = new byte[32];
        final byte[] blocArray = new byte[12];

        byteBuff.get(blocArray);
        s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(locArray);
        s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

        byteBuff.get(blocArray);
        s.setGateDialSignIndex(byteBuff.getInt());
        s.setGateTempSignTarget(byteBuff.getLong());
        if (s.isGateSignPowered()) {
            s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

            if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk())) {
                try {
                    s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.WARNING, false, "[V5] Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    SGLogger.prettyLog(Level.FINE, false, "[V5] Stacktrace: " + e.getMessage());
                }
            }
        }

        s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateTempTargetId(byteBuff.getLong());

        final int facingSize = byteBuff.getInt();
        final byte[] strBytes = new byte[facingSize];
        byteBuff.get(strBytes);
        final String faceStr = new String(strBytes);
        s.setGateFacing(BlockFace.valueOf(faceStr));

        s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGatePlayerTeleportLocation().setPitch(0);

        final int idcLen = byteBuff.getInt();
        final byte[] idcBytes = new byte[idcLen];
        byteBuff.get(idcBytes);
        s.setGateIrisDeactivationCode(new String(idcBytes));

        s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateIrisDefaultActive(s.isGateIrisActive());
        s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

        int numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateStructureBlocks().add(bl.getLocation());
        }

        numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGatePortalBlocks().add(bl.getLocation());
        }

        while (s.getGateLightBlocks().size() < 2) {
            s.getGateLightBlocks().add(null);
        }

        s.getGateLightBlocks().set(1, new ArrayList<Location>());

        numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateLightBlocks().get(1).add(bl.getLocation());
        }

        return s;        
    }
    
    private static Stargate parseVersionedDataV6(World w, Stargate s, ByteBuffer byteBuff) {
        final byte[] locArray = new byte[32];
        final byte[] blocArray = new byte[12];
        
        byteBuff.get(blocArray);
        s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(locArray);
        s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

        byteBuff.get(blocArray);
        s.setGateDialSignIndex(byteBuff.getInt());
        s.setGateTempSignTarget(byteBuff.getLong());
        if (s.isGateSignPowered()) {
            s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

            if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk())) {
                try {
                    s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.WARNING, false, "[V6] Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    SGLogger.prettyLog(Level.FINE, false, "[V6] Stacktrace: " + e.getMessage());
                }
            }
        }

        s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateTempTargetId(byteBuff.getLong());

        final int facingSize = byteBuff.getInt();
        final byte[] strBytes = new byte[facingSize];
        byteBuff.get(strBytes);
        final String faceStr = new String(strBytes);
        s.setGateFacing(BlockFace.valueOf(faceStr));

        s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGatePlayerTeleportLocation().setPitch(0);

        final int idcLen = byteBuff.getInt();
        final byte[] idcBytes = new byte[idcLen];
        byteBuff.get(idcBytes);
        s.setGateIrisDeactivationCode(new String(idcBytes));

        s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateIrisDefaultActive(s.isGateIrisActive());
        s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

        boolean isRedstone = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstone) {
            s.setGateRedstoneDialActivationBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        isRedstone = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstone) {
            s.setGateRedstoneSignActivationBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        int numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateStructureBlocks().add(bl.getLocation());
        }

        numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGatePortalBlocks().add(bl.getLocation());
        }

        int numLayers = byteBuff.getInt();

        while (s.getGateLightBlocks().size() < numLayers) {
            s.getGateLightBlocks().add(new ArrayList<Location>());
        }
        for (int i = 0; i < numLayers; i++) {
            numBlocks = byteBuff.getInt();
            for (int j = 0; j < numBlocks; j++) {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateLightBlocks().get(i).add(bl.getLocation());
            }
        }

        numLayers = byteBuff.getInt();

        while (s.getGateWooshBlocks().size() < numLayers) {
            s.getGateWooshBlocks().add(new ArrayList<Location>());
        }
        for (int i = 0; i < numLayers; i++) {
            numBlocks = byteBuff.getInt();
            for (int j = 0; j < numBlocks; j++) {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateWooshBlocks().get(i).add(bl.getLocation());
            }
        }

        if (byteBuff.remaining() > 0) {
            SGLogger.prettyLog(Level.WARNING, false, "While loading gate, not all byte data was read. This could be bad: " + byteBuff.remaining());
        }

        return s;        
    }
    
    private static Stargate parseVersionedDataV7(World w, Stargate s, ByteBuffer byteBuff) {
        final byte[] locArray = new byte[32];
        final byte[] blocArray = new byte[12];

        byteBuff.get(blocArray);
        s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(locArray);
        s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        byteBuff.get(locArray);
        s.setGateMinecartTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

        byteBuff.get(blocArray);
        s.setGateDialSignIndex(byteBuff.getInt());
        s.setGateTempSignTarget(byteBuff.getLong());
        if (s.isGateSignPowered()) {
            s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

            if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk())) {
                try {
                    s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.WARNING, false, "[V7] Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    SGLogger.prettyLog(Level.FINE, false, "[V7] Stacktrace: " + e.getMessage());
                }
            }
        }

        s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateTempTargetId(byteBuff.getLong());

        final int facingSize = byteBuff.getInt();
        final byte[] strBytes = new byte[facingSize];
        byteBuff.get(strBytes);
        final String faceStr = new String(strBytes);
        s.setGateFacing(BlockFace.valueOf(faceStr));
        s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGatePlayerTeleportLocation().setPitch(0);

        final int idcLen = byteBuff.getInt();
        final byte[] idcBytes = new byte[idcLen];
        byteBuff.get(idcBytes);
        s.setGateIrisDeactivationCode(new String(idcBytes));

        s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateIrisDefaultActive(s.isGateIrisActive());
        s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

        boolean isRedstone = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstone) {
            s.setGateRedstoneDialActivationBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        isRedstone = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstone) {
            s.setGateRedstoneSignActivationBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        int numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateStructureBlocks().add(bl.getLocation());
        }

        numBlocks = byteBuff.getInt();
        for (int i = 0; i < numBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGatePortalBlocks().add(bl.getLocation());
        }

        int numLayers = byteBuff.getInt();

        while (s.getGateLightBlocks().size() < numLayers) {
            s.getGateLightBlocks().add(new ArrayList<Location>());
        }
        for (int i = 0; i < numLayers; i++) {
            numBlocks = byteBuff.getInt();
            for (int j = 0; j < numBlocks; j++) {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateLightBlocks().get(i).add(bl.getLocation());
            }
        }

        numLayers = byteBuff.getInt();

        while (s.getGateWooshBlocks().size() < numLayers) {
            s.getGateWooshBlocks().add(new ArrayList<Location>());
        }
        for (int i = 0; i < numLayers; i++) {
            numBlocks = byteBuff.getInt();
            for (int j = 0; j < numBlocks; j++) {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateWooshBlocks().get(i).add(bl.getLocation());
            }
        }

        if (byteBuff.remaining() > 0) {
            SGLogger.prettyLog(Level.WARNING, false, "While loading gate, not all byte data was read. This could be bad: " + byteBuff.remaining());
        }

        return s;    
    }
    
    @SuppressWarnings("deprecation")
	private static Stargate parseVersionedDataV8(World w, Stargate s, ByteBuffer byteBuff) {
        final byte[] locArray = new byte[32];
        final byte[] blocArray = new byte[12];

        byteBuff.get(blocArray);
        s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(blocArray);
        s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

        byteBuff.get(locArray);
        s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        byteBuff.get(locArray);
        s.setGateMinecartTeleportLocation(DataUtils.locationFromBytes(locArray, w));

        s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

        byteBuff.get(blocArray);
        s.setGateDialSignIndex(byteBuff.getInt());
        s.setGateTempSignTarget(byteBuff.getLong());
        if (s.isGateSignPowered()) {
            s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

            if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk())) {
                try {
                    s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.WARNING, false, "[V8] Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    SGLogger.prettyLog(Level.FINE, false, "[V8] Stacktrace: " + e.getMessage());
                }
            }
        }

        s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateTempTargetId(byteBuff.getLong());

        final int facingSize = byteBuff.getInt();
        final byte[] strBytes = new byte[facingSize];
        byteBuff.get(strBytes);
        
        final String faceStr = new String(strBytes);
        
        s.setGateFacing(BlockFace.valueOf(faceStr));
        
        s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGatePlayerTeleportLocation().setPitch(0);
        
        s.getGateMinecartTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
        s.getGateMinecartTeleportLocation().setPitch(0);
        
        final int idcLen = byteBuff.getInt();
        final byte[] idcBytes = new byte[idcLen];
        byteBuff.get(idcBytes);
        s.setGateIrisDeactivationCode(new String(idcBytes));

        s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
        s.setGateIrisDefaultActive(s.isGateIrisActive());
        s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

        final boolean isRedstoneDA = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstoneDA) {
            s.setGateRedstoneDialActivationBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        final boolean isRedstoneSA = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstoneSA) {
            s.setGateRedstoneSignActivationBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        final boolean isRedstoneGA = DataUtils.byteToBoolean(byteBuff.get());
        byteBuff.get(blocArray);
        if (isRedstoneGA) {
            s.setGateRedstoneGateActivatedBlock(DataUtils.blockFromBytes(blocArray, w));
        }

        s.setGateRedstonePowered(DataUtils.byteToBoolean(byteBuff.get()));

        s.setGateCustom(DataUtils.byteToBoolean(byteBuff.get()));
        final int gateCustomStructureMaterial = byteBuff.getInt();
        s.setGateCustomStructureMaterial(gateCustomStructureMaterial != -1
                ? Material.getMaterial(gateCustomStructureMaterial)
                : null);
        final int gateCustomPortalMaterial = byteBuff.getInt();
        s.setGateCustomPortalMaterial(gateCustomPortalMaterial != -1
                ? Material.getMaterial(gateCustomPortalMaterial)
                : null);
        final int gateCustomLightMaterial = byteBuff.getInt();
        s.setGateCustomLightMaterial(gateCustomLightMaterial != -1
                ? Material.getMaterial(gateCustomLightMaterial)
                : null);
        final int gateCustomIrisMaterial = byteBuff.getInt();
        s.setGateCustomIrisMaterial(gateCustomIrisMaterial != -1
                ? Material.getMaterial(gateCustomIrisMaterial)
                : null);
        s.setGateCustomWooshTicks(byteBuff.getInt());
        s.setGateCustomLightTicks(byteBuff.getInt());
        s.setGateCustomWooshDepth(byteBuff.getInt());
        s.setGateCustomWooshDepthSquared(s.getGateCustomWooshDepth() >= 0
                ? s.getGateCustomWooshDepth() * s.getGateCustomWooshDepth()
                : -1);

        final int numStructureBlocks = byteBuff.getInt();
        for (int i = 0; i < numStructureBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGateStructureBlocks().add(bl.getLocation());
        }

        final int numPortalBlocks = byteBuff.getInt();
        for (int i = 0; i < numPortalBlocks; i++) {
            byteBuff.get(blocArray);
            final Block bl = DataUtils.blockFromBytes(blocArray, w);
            s.getGatePortalBlocks().add(bl.getLocation());
        }

        final int numLightLayers = byteBuff.getInt();

        while (s.getGateLightBlocks().size() < numLightLayers) {
            s.getGateLightBlocks().add(new ArrayList<Location>());
        }

        for (int i = 0; i < numLightLayers; i++) {
            final int numLightBlocks = byteBuff.getInt();
            for (int j = 0; j < numLightBlocks; j++) {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateLightBlocks().get(i).add(bl.getLocation());
            }
        }

        final int numWooshLayers = byteBuff.getInt();

        while (s.getGateWooshBlocks().size() < numWooshLayers) {
            s.getGateWooshBlocks().add(new ArrayList<Location>());
        }
        for (int i = 0; i < numWooshLayers; i++) {
            final int numWooshBlocks = byteBuff.getInt();
            for (int j = 0; j < numWooshBlocks; j++) {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateWooshBlocks().get(i).add(bl.getLocation());
            }
        }

        if (byteBuff.remaining() > 0) {
            SGLogger.prettyLog(Level.WARNING, false, "While loading gate, not all byte data was read. This could be bad: " + byteBuff.remaining());
        }

        return s;        
    }

    public static void reloadShapes() {
        stargateShapes.clear();
        loadShapes();
    }
    
    private static void setupSignGateNetwork(final Stargate stargate) {

        if ((stargate.getGateName() != null) && (stargate.getGateName().length() > 0)) {
            String networkName = "Public";

            if ((stargate.getGateDialSign() != null) && !stargate.getGateDialSign().getLine(1).equals("")) {

                networkName = stargate.getGateDialSign().getLine(1);
            }
            StargateNetwork net = StargateManager.getStargateNetwork(networkName);
            if (net == null) {
                net = StargateManager.addStargateNetwork(networkName);
            }
            StargateManager.addGateToNetwork(stargate, networkName);

            stargate.setGateNetwork(net);
            stargate.setGateDialSignIndex(-1);
            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(stargate, ActionToTake.DIAL_SIGN_CLICK));
        }
    }

    @SuppressWarnings("deprecation")
	public static byte[] stargatetoBinary(final Stargate s) {
        byte[] utfFaceBytes;
        byte[] utfIdcBytes;
        try {
            utfFaceBytes = s.getGateFacing().toString().getBytes("UTF8");
            utfIdcBytes = s.getGateIrisDeactivationCode().getBytes("UTF8");
        } catch (final Exception e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Unable to store gate in DB, byte encoding failed: " + e.getMessage());
            e.printStackTrace();
            final byte[] b = null;
            return b;
        }

        final int numBlocks = 7;
        final int numLocations = 2;
        final int locationSize = 32;
        final int blockSize = 12;
        final int numBytesWithVersion = 10;
        final int numInts = 12;
        final int numLongs = 2;

        int size = numBytesWithVersion + (numInts * 4) + (numLongs * 8) + (numBlocks * blockSize) + (numLocations * locationSize);
        
        size += (s.getGateStructureBlocks().size() * blockSize) + (s.getGatePortalBlocks().size() * blockSize);

        int numIntsOther = 2;

        for (int i = 0; i < s.getGateLightBlocks().size(); i++) {
            if (s.getGateLightBlocks().get(i) != null) {
                size += s.getGateLightBlocks().get(i).size() * blockSize;
            }

            numIntsOther++;
        }

        for (int i = 0; i < s.getGateWooshBlocks().size(); i++) {
            if (s.getGateWooshBlocks().get(i) != null) {
                size += s.getGateWooshBlocks().get(i).size() * blockSize;
            }

            numIntsOther++;
        }

        size += utfFaceBytes.length + utfIdcBytes.length;
        size += numIntsOther * 4;

        final ByteBuffer dataArr = ByteBuffer.allocate(size);


        dataArr.put(StargateSaveVersion);
        dataArr.put(DataUtils.blockToBytes(s.getGateDialLeverBlock()));
        dataArr.put(s.getGateIrisLeverBlock() != null
                ? DataUtils.blockToBytes(s.getGateIrisLeverBlock())
                : emptyBlock);
        dataArr.put(s.getGateNameBlockHolder() != null
                ? DataUtils.blockToBytes(s.getGateNameBlockHolder())
                : emptyBlock);
        dataArr.put(DataUtils.locationToBytes(s.getGatePlayerTeleportLocation()));
        dataArr.put(s.getGateMinecartTeleportLocation() != null
                ? DataUtils.locationToBytes(s.getGateMinecartTeleportLocation())
                : DataUtils.locationToBytes(s.getGatePlayerTeleportLocation()));

        if (s.isGateSignPowered()) {

            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateDialSignBlock()));
            dataArr.putInt(s.getGateDialSignIndex());
            dataArr.putLong(s.getGateDialSignTarget() != null
                    ? s.getGateDialSignTarget().getGateId()
                    : -1);
        } else {
        	
            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
            dataArr.putInt(-1);
            dataArr.putLong(-1);
        }

        if (s.isGateActive() && (s.getGateTarget() != null)) {

            dataArr.put((byte) 1);
            dataArr.putLong(s.getGateTarget().getGateId());
            
        } else {

            dataArr.put((byte) 0);
            dataArr.putLong(-1);
        }

        dataArr.putInt(utfFaceBytes.length);
        dataArr.put(utfFaceBytes);
        dataArr.putInt(utfIdcBytes.length);
        dataArr.put(utfIdcBytes);
        dataArr.put(s.isGateIrisActive()
                ? (byte) 1
                : (byte) 0);
        dataArr.put(s.isGateLightsActive()
                ? (byte) 1
                : (byte) 0);

        if (s.getGateRedstoneDialActivationBlock() != null) {
        	
            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneDialActivationBlock()));
            
        } else {
        	
            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
        }

        if (s.getGateRedstoneSignActivationBlock() != null) {

            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneSignActivationBlock()));
            
        } else {

            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
        }

        if (s.getGateRedstoneGateActivatedBlock() != null) {

            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneGateActivatedBlock()));
            
        } else {

            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
        }

        dataArr.put(s.isGateRedstonePowered()
                ? (byte) 1
                : (byte) 0);

        dataArr.put(s.isGateCustom()
                ? (byte) 1
                : (byte) 0);

        dataArr.putInt(s.getGateCustomStructureMaterial() != null
                ? s.getGateCustomStructureMaterial().getId()
                : -1);

        dataArr.putInt(s.getGateCustomPortalMaterial() != null
                ? s.getGateCustomPortalMaterial().getId()
                : -1);

        dataArr.putInt(s.getGateCustomLightMaterial() != null
                ? s.getGateCustomLightMaterial().getId()
                : -1);

        dataArr.putInt(s.getGateCustomIrisMaterial() != null
                ? s.getGateCustomIrisMaterial().getId()
                : -1);

        dataArr.putInt(s.getGateCustomWooshTicks());

        dataArr.putInt(s.getGateCustomLightTicks());

        dataArr.putInt(s.getGateCustomWooshDepth());

        dataArr.putInt(s.getGateStructureBlocks().size());
        for (int i = 0; i < s.getGateStructureBlocks().size(); i++) {
            dataArr.put(DataUtils.blockLocationToBytes(s.getGateStructureBlocks().get(i)));
        }

        dataArr.putInt(s.getGatePortalBlocks().size());
        for (int i = 0; i < s.getGatePortalBlocks().size(); i++) {
            dataArr.put(DataUtils.blockLocationToBytes(s.getGatePortalBlocks().get(i)));
        }

        dataArr.putInt(s.getGateLightBlocks().size());
        for (int i = 0; i < s.getGateLightBlocks().size(); i++) {
            if (s.getGateLightBlocks().get(i) != null) {
                dataArr.putInt(s.getGateLightBlocks().get(i).size());
                for (int j = 0; j < s.getGateLightBlocks().get(i).size(); j++) {
                    dataArr.put(DataUtils.blockLocationToBytes(s.getGateLightBlocks().get(i).get(j)));
                }
            } else {
                dataArr.putInt(0);
            }
        }

        dataArr.putInt(s.getGateWooshBlocks().size());
        for (int i = 0; i < s.getGateWooshBlocks().size(); i++) {
            if (s.getGateWooshBlocks().get(i) != null) {
                dataArr.putInt(s.getGateWooshBlocks().get(i).size());
                for (int j = 0; j < s.getGateWooshBlocks().get(i).size(); j++) {
                    dataArr.put(DataUtils.blockLocationToBytes(s.getGateWooshBlocks().get(i).get(j)));
                }
            } else {
                dataArr.putInt(0);
            }
        }

        if (dataArr.remaining() > 0) {
            SGLogger.prettyLog(Level.WARNING, false, "Gate data not filling whole byte array. This could be bad:" + dataArr.remaining());
        }

        return dataArr.array();
    }

    private static boolean tryCreateGateSign(final Block signBlock, final Stargate tempGate) {
        SGLogger.prettyLog(Level.FINE, false, "Trying to create GateSign for gate '" + tempGate.getGateName() + "' in '" + tempGate.getGateWorld().getName() + "'");
        if (signBlock.getType().equals(Material.WALL_SIGN)) {
            tempGate.setGateSignPowered(true);
            tempGate.setGateDialSignBlock(signBlock);
            tempGate.setGateDialSign((Sign) signBlock.getState());
            tempGate.getGateStructureBlocks().add(signBlock.getLocation());

            final String name = tempGate.getGateDialSign().getLine(0);
            if (StargateManager.getStargate(name) != null) {
                tempGate.setGateName("");
                return false;
            }

            String filteredName = name;
            if (name.startsWith("-") && name.endsWith("-")) {            
                for (int i = 0; i < name.length();i++) {
                    if (name.startsWith("-") && name.endsWith("-")) {
                        filteredName = name.substring(1, name.length() - 1);
                    }
                }
            }

            if (filteredName.length() > 2) {
                tempGate.setGateName(filteredName);
            }

            return true;
        }

        return false;
    }
}
