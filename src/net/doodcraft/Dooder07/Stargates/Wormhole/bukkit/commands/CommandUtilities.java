package net.doodcraft.Dooder07.Stargates.Wormhole.bukkit.commands;

import net.doodcraft.Dooder07.Stargates.Wormhole.model.Stargate;
import net.doodcraft.Dooder07.Stargates.Wormhole.model.StargateManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandUtilities {

    public static void closeGate(final Stargate stargate, final boolean iris) {
        if (stargate != null) {
            if (stargate.isGateActive()) {
                stargate.shutdownStargate(true);
                if (stargate.isGateActive()) {
                    stargate.setGateActive(false);
                }
            }
            if (stargate.isGateLightsActive()) {
                stargate.lightStargate(false);
                stargate.stopActivationTimer();
            }
            if (iris && stargate.isGateIrisActive()) {
                stargate.toggleIrisActive(false);
            }
        }
    }

    public static String[] commandEscaper(final String[] args) {
        StringBuilder tempString = new StringBuilder();
        boolean startQuoteFound = false;
        boolean endQuoteFound = false;

        final ArrayList<String> argsPartsList = new ArrayList<String>();

        for (final String part : args) {

            if (part.contains("\"") && !startQuoteFound) {

                if (!part.replaceFirst("\"", "").contains("\"")) {
                    startQuoteFound = true;
                }
            } else if (part.contains("\"") && startQuoteFound) {
                endQuoteFound = true;
            }

            if (!startQuoteFound) {
                argsPartsList.add(part);
            }

            if (startQuoteFound) {
                tempString.append(part.replace("\"", ""));
                if (endQuoteFound) {
                    argsPartsList.add(tempString.toString());
                    startQuoteFound = false;
                    endQuoteFound = false;
                    tempString = new StringBuilder();
                } else {
                    tempString.append(" ");
                }
            }
        }
        
        return argsPartsList.toArray(new String[argsPartsList.size()]);
    }

    public static void gateRemove(final Stargate stargate, final boolean destroy) {
        stargate.setupGateSign(false);

        if (!destroy) {
            stargate.resetTeleportSign();
        }

        if (!stargate.getGateIrisDeactivationCode().equals("")) {
            if (stargate.isGateIrisActive()) {
                stargate.toggleIrisActive(false);
            }
            stargate.setupIrisLever(false);
        }
        if (stargate.isGateRedstonePowered()) {
            stargate.setupRedstone(false);
        }
        
        if (destroy) {
            stargate.deleteGateBlocks();
            stargate.deletePortalBlocks();
            stargate.deleteTeleportSign();
        }
        StargateManager.removeStargate(stargate);
    }

    public static String getGateNetwork(final Stargate stargate) {
        if (stargate != null) {
            if (stargate.getGateNetwork() != null) {
                return stargate.getGateNetwork().getNetworkName();
            }
        }
        return "Public";
    }

    public static boolean isBoolean(final String booleanString) {
        return booleanString.equalsIgnoreCase("true") || booleanString.equalsIgnoreCase("false");
    }

    public static boolean playerCheck(final CommandSender sender) {
        return sender instanceof Player;

    }
}
