package net.doodcraft.Dooder07.Stargates.Wormhole.permissions.backends;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionBackend;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.logging.Level;


public class PermissionsExSupport extends PermissionBackend {

    protected ru.tehkode.permissions.PermissionManager provider = null;

    public PermissionsExSupport(net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionManager manager, ConfigManager config, String providerName) {
        super(manager, config, providerName);
    }

    @Override
    public void initialize() {
        if (!(StarGates.getPermissionManager() == null)) {
            return;
        }

        Plugin testPlugin = Bukkit.getServer().getPluginManager().getPlugin(getProviderName());
        if ((testPlugin != null) && (Bukkit.getServer().getPluginManager().isPluginEnabled(getProviderName()))) {
            final String version = testPlugin.getDescription().getVersion();

            try {
                provider = PermissionsEx.getPermissionManager();
                SGLogger.prettyLog(Level.INFO, false, "Attached to " + providerName + " version " + version);
            } catch (final ClassCastException e) {
                SGLogger.prettyLog(Level.WARNING, false, "Failed to get Permissions Handler. Defaulting to built-in permissions.");
            }
        } else {
            SGLogger.prettyLog(Level.INFO, false, "Permission Plugin not yet available. Defaulting to built-in permissions until Permissions is loaded.");
        }
    }

    @Override
    public void reload() {
        provider = null;
        SGLogger.prettyLog(Level.INFO, false, "Detached from Permissions plugin '" + getProviderName() + "'.");
    }

    @Override
    public boolean hasPermission(Player player, String permissionString) {
        return provider.has(player, permissionString);
    }
}