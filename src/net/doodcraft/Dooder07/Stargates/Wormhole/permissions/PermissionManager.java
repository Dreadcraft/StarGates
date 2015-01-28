package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.events.WormholeSystemEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PermissionManager {

    protected PermissionBackend backend = null;
    protected ConfigManager configManager;

    public PermissionManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.initBackend();
    }

    private void initBackend() {
        // @TODO use configManager instead of static call
        String backendName = ConfigManager.getConfigurations().get(ConfigManager.ConfigKeys.PERMISSIONS_BACKEND).getStringValue();

        if (backendName == null || backendName.isEmpty()) {
            backendName = PermissionBackend.defaultBackend;
            ConfigManager.setPermissionBackend(backendName);
        }

        this.setBackend(backendName);
    }

    public PermissionBackend getBackend() {
        return this.backend;
    }

    public void setBackend(String backendName) {
        synchronized (this) {
            this.backend = PermissionBackend.getBackend(backendName, this, configManager);
            this.backend.initialize();
        }

        this.callEvent(WormholeSystemEvent.Action.PERMISSION_BACKEND_CHANGED);
    }

    protected void callEvent(WormholeSystemEvent event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    protected void callEvent(WormholeSystemEvent.Action action) {
        this.callEvent(new WormholeSystemEvent(action));
    }

    public void reset() {
        if (this.backend != null) {
            this.backend.reload();
        }

        this.callEvent(WormholeSystemEvent.Action.RELOADED);
    }

    public void end() {
        reset();
    }

    public boolean has(Player player, String permissionString) {
        return backend.has(player, permissionString);
    }

    public boolean hasPermission(Player player, String permissionString) {
        return backend.hasPermission(player, permissionString);
    }
}
