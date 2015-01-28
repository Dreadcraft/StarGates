package net.doodcraft.Dooder07.Stargates.Wormhole.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class StargateShapeLayer {

    private ArrayList<Integer[]> layerBlockPositions = new ArrayList<Integer[]>();
    private int[] layerNameSignPosition = null;
    private int[] layerPlayerExitPosition = null;
    private int[] layerMinecartExitPosition = null;
    private int[] layerActivationPosition = null;
    private int[] layerIrisActivationPosition = null;
    private int[] layerDialSignPosition = null;
    private int[] layerRedstoneDialActivationPosition = null;
    private int[] layerRedstoneSignActivationPosition = null;
    private int[] layerRedstoneGateActivatedPosition = null;
    private ArrayList<ArrayList<Integer[]>> layerLightPositions = new ArrayList<ArrayList<Integer[]>>();
    private ArrayList<ArrayList<Integer[]>> layerWooshPositions = new ArrayList<ArrayList<Integer[]>>();
    private ArrayList<Integer[]> layerPortalPositions = new ArrayList<Integer[]>();

    @SuppressWarnings("unused")
	protected StargateShapeLayer(final String[] layerLines, final int height, final int width) {
        int numBlocks = 0;

        for (int i = 0; i < layerLines.length; i++) {
            if (Pattern.compile("\\[(.+?)\\]") == null) {
                SGLogger.prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(getLayerNameSignPosition()) + "\"");
            }
            final Matcher m = Pattern.compile("\\[(.+?)\\]").matcher(layerLines[i]);
            int j = 0;
            while (m.find()) {
                final Integer[] point = {0, (height - 1 - i), (width - 1 - j)};

                for (final String mod : m.group(1).split(":")) {
                    if (mod.equalsIgnoreCase("S")) {
                        numBlocks++;
                        getLayerBlockPositions().add(point);
                    } else if (mod.equalsIgnoreCase("P")) {
                        getLayerPortalPositions().add(point);
                    } else if (mod.equalsIgnoreCase("N") || mod.equalsIgnoreCase("EP") || mod.equalsIgnoreCase("EM") || mod.equalsIgnoreCase("A") || mod.equalsIgnoreCase("D") || mod.equalsIgnoreCase("IA") || mod.equalsIgnoreCase("RA") || mod.equalsIgnoreCase("RD") || mod.equalsIgnoreCase("RS")) {
                        final int[] pointI = new int[3];
                        for (int k = 0; k < 3; k++) {
                            pointI[k] = point[k];
                        }

                        if (mod.equalsIgnoreCase("N")) {
                            setLayerNameSignPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("EP")) {
                            setLayerPlayerExitPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("EM")) {
                            setLayerMinecartExitPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("A")) {
                            setLayerActivationPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("D")) {
                            setLayerDialSignPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("IA")) {
                            setLayerIrisActivationPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("RA")) {
                            setLayerRedstoneGateActivatedPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("RD")) {
                            setLayerRedstoneDialActivationPosition(pointI);
                        }
                        if (mod.equalsIgnoreCase("RS")) {
                            setLayerRedstoneSignActivationPosition(pointI);
                        }
                    } else if (mod.contains("L") || mod.contains("l")) {
                        final int light_iteration = mod.contains("#") ? Integer.parseInt(mod.split("#")[1]) : 1;

                        while (getLayerLightPositions().size() <= light_iteration) {
                            getLayerLightPositions().add(null);
                        }

                        if (getLayerLightPositions().get(light_iteration) == null) {
                            final ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
                            getLayerLightPositions().set(light_iteration, new_it);
                        }

                        getLayerLightPositions().get(light_iteration).add(point);
                        SGLogger.prettyLog(Level.CONFIG, false, "Light Material Position (Order:" + light_iteration + " Position:" + Arrays.toString(point) + ")");
                    } else if (mod.contains("W") || mod.contains("w")) {
                        final int w_iteration = mod.contains("#") ? Integer.parseInt(mod.split("#")[1]) : 1;

                        while (getLayerWooshPositions().size() <= w_iteration) {
                            getLayerWooshPositions().add(null);
                        }

                        if (getLayerWooshPositions().get(w_iteration) == null) {
                            final ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
                            getLayerWooshPositions().set(w_iteration, new_it);
                        }

                        getLayerWooshPositions().get(w_iteration).add(point);
                        SGLogger.prettyLog(Level.CONFIG, false, "Woosh Position (Order:" + w_iteration + " Position:" + Arrays.toString(point) + ")");
                    }
                }
                j++;
            }
        }
        //TODO: debug printout for the materials the gate uses.
        //TODO: debug printout for the redstone_activated
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(getLayerNameSignPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Player Exit Position: \"" + Arrays.toString(getLayerPlayerExitPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Minecart Exit Position: \"" + Arrays.toString(getLayerMinecartExitPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Activation Position: \"" + Arrays.toString(getLayerActivationPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Iris Activation Position: \"" + Arrays.toString(getLayerIrisActivationPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Dial Sign Position: \"" + Arrays.toString(getLayerDialSignPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Redstone Dial Activation Position: \"" + Arrays.toString(getLayerRedstoneDialActivationPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Redstone Sign Activation Position: \"" + Arrays.toString(getLayerRedstoneSignActivationPosition()) + "\"");
        SGLogger.prettyLog(Level.CONFIG, false, "Stargate Redstone Gate Activated Position: \"" + Arrays.toString(getLayerRedstoneGateActivatedPosition()) + "\"");
        //SGLogger.prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString((int[][])this.waterPositions) + "\"");
        //SGLogger.prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + lightPositions.toString() + "\"");
        //SGLogger.prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString((int[][])this.stargatePositions) + "\"");
    }

    public int[] getLayerActivationPosition() {
        return layerActivationPosition != null
                ? layerActivationPosition.clone()
                : new int[]{};
    }

    public ArrayList<Integer[]> getLayerBlockPositions() {
        return layerBlockPositions;
    }

    public int[] getLayerDialSignPosition() {
        return layerDialSignPosition != null
                ? layerDialSignPosition.clone()
                : new int[]{};
    }

    public int[] getLayerIrisActivationPosition() {
        return layerIrisActivationPosition != null
                ? layerIrisActivationPosition.clone()
                : new int[]{};
    }

    public ArrayList<ArrayList<Integer[]>> getLayerLightPositions() {
        return layerLightPositions;
    }

    public int[] getLayerMinecartExitPosition() {
        return layerMinecartExitPosition != null
                ? layerMinecartExitPosition.clone()
                : new int[]{};
    }

    public int[] getLayerNameSignPosition() {
        return layerNameSignPosition != null
                ? layerNameSignPosition.clone()
                : new int[]{};
    }

    public int[] getLayerPlayerExitPosition() {
        return layerPlayerExitPosition != null
                ? layerPlayerExitPosition.clone()
                : new int[]{};
    }

    public ArrayList<Integer[]> getLayerPortalPositions() {
        return layerPortalPositions;
    }

    public int[] getLayerRedstoneDialActivationPosition() {
        return layerRedstoneDialActivationPosition != null
                ? layerRedstoneDialActivationPosition.clone()
                : new int[]{};
    }

    public int[] getLayerRedstoneGateActivatedPosition() {
        return layerRedstoneGateActivatedPosition != null
                ? layerRedstoneGateActivatedPosition.clone()
                : new int[]{};
    }

    public int[] getLayerRedstoneSignActivationPosition() {
        return layerRedstoneSignActivationPosition != null
                ? layerRedstoneSignActivationPosition.clone()
                : new int[]{};
    }

    public ArrayList<ArrayList<Integer[]>> getLayerWooshPositions() {
        return layerWooshPositions;
    }

    public void setLayerActivationPosition(final int[] layerActivationPosition) {
        this.layerActivationPosition = layerActivationPosition.clone();
    }

    public void setLayerBlockPositions(final ArrayList<Integer[]> layerBlockPositions) {
        this.layerBlockPositions = layerBlockPositions;
    }

    public void setLayerDialSignPosition(final int[] layerDialSignPosition) {
        this.layerDialSignPosition = layerDialSignPosition.clone();
    }

    public void setLayerIrisActivationPosition(final int[] layerIrisActivationPosition) {
        this.layerIrisActivationPosition = layerIrisActivationPosition.clone();
    }

    public void setLayerLightPositions(final ArrayList<ArrayList<Integer[]>> layerLightPositions) {
        this.layerLightPositions = layerLightPositions;
    }

    public void setLayerMinecartExitPosition(final int[] layerMinecartExitPosition) {
        this.layerMinecartExitPosition = layerMinecartExitPosition.clone();
    }

    public void setLayerNameSignPosition(final int[] layerNameSignPosition) {
        this.layerNameSignPosition = layerNameSignPosition.clone();
    }

    public void setLayerPlayerExitPosition(final int[] layerPlayerExitPosition) {
        this.layerPlayerExitPosition = layerPlayerExitPosition.clone();
    }

    public void setLayerPortalPositions(final ArrayList<Integer[]> layerPortalPositions) {
        this.layerPortalPositions = layerPortalPositions;
    }

    public void setLayerRedstoneDialActivationPosition(final int[] layerRedstoneDialActivationPosition) {
        this.layerRedstoneDialActivationPosition = layerRedstoneDialActivationPosition.clone();
    }

    public void setLayerRedstoneGateActivatedPosition(final int[] layerRedstoneGateActivatedPosition) {
        this.layerRedstoneGateActivatedPosition = layerRedstoneGateActivatedPosition.clone();
    }

    public void setLayerRedstoneSignActivationPosition(final int[] layerRedstoneSignActivationPosition) {
        this.layerRedstoneSignActivationPosition = layerRedstoneSignActivationPosition.clone();
    }

    public void setLayerWooshPositions(final ArrayList<ArrayList<Integer[]>> layerWooshPositions) {
        this.layerWooshPositions = layerWooshPositions;
    }
}
