package net.doodcraft.Dooder07.Stargates.Wormhole.listeners;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.plugin.WormholeWorldsSupport;

public class StarGatesServerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(final PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("PermissionsEx") && !ConfigManager.getPermissionsSupportDisable()) {
            StarGates.getPermissionManager().end();
        } else if (event.getPlugin().getDescription().getName().equals("WormholeXTremeWorlds") && ConfigManager.isWormholeWorldsSupportEnabled()) {
            WormholeWorldsSupport.disableWormholeWorlds();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(final PluginEnableEvent event) {
    }
}
