package net.doodcraft.Dooder07.Stargates.Wormhole.model;

import java.util.ArrayList;
import java.util.HashMap;

import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager;

public class StargateNetwork {

    private String networkName;
    private final ArrayList<Stargate> networkGateList = new ArrayList<Stargate>();
    private final ArrayList<Stargate> networkSignGateList = new ArrayList<Stargate>();
    private Object networkGateLock = new Object();
    private final HashMap<String, PermissionsManager.PermissionLevel> networkIndividualPermissions = new HashMap<String, PermissionsManager.PermissionLevel>();

    public ArrayList<Stargate> getNetworkGateList() {
        return networkGateList;
    }

    public Object getNetworkGateLock() {
        return networkGateLock;
    }

    public HashMap<String, PermissionsManager.PermissionLevel> getNetworkIndividualPermissions() {
        return networkIndividualPermissions;
    }

    public String getNetworkName() {
        return networkName;
    }

    public ArrayList<Stargate> getNetworkSignGateList() {
        return networkSignGateList;
    }

    public void setNetworkGateLock(final Object networkGateLock) {
        this.networkGateLock = networkGateLock;
    }

    public void setNetworkName(final String networkName) {
        this.networkName = networkName;
    }
}