package net.doodcraft.Dooder07.Stargates.Wormhole.permissions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.exceptions.WormholePermissionBackendException;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public abstract class PermissionBackend {

    protected final static String defaultBackend = "bukkit";
    protected static Map<String, Class<? extends PermissionBackend>> registeredBackendAliases = new HashMap<String, Class<? extends PermissionBackend>>();
    public static PermissionBackend getBackend(String backendName, ConfigManager configManager) {
        return getBackend(backendName, StarGates.getPermissionManager(), configManager, defaultBackend);
    }
    public static PermissionBackend getBackend(String backendName, PermissionManager manager, ConfigManager configManager) {
        return getBackend(backendName, manager, configManager, defaultBackend);
    }
    public static PermissionBackend getBackend(String backendName, PermissionManager manager, ConfigManager configManager, String fallBackBackend) {
        if (backendName == null || backendName.isEmpty()) {
            backendName = defaultBackend;
        }

        String className = getBackendClassName(backendName);

        try {
            Class<? extends PermissionBackend> backendClass = getBackendClass(backendName);

            SGLogger.prettyLog(Level.INFO, false, "Initializing " + backendName + " backend");

            Constructor<? extends PermissionBackend> constructor = backendClass.getConstructor(PermissionManager.class, ConfigManager.class, String.class);
            return constructor.newInstance(manager, configManager, getBackendPluginName(backendName));
        } catch (ClassNotFoundException e) {

            SGLogger.prettyLog(Level.WARNING, false, "Backend \"" + backendName + "\" not found");

            if (fallBackBackend == null) {
                throw new WormholePermissionBackendException("Backend \"" + backendName + "\" not found: " + e.getMessage());
            }

            if (!className.equals(getBackendClassName(fallBackBackend))) {
                return getBackend(fallBackBackend, manager, configManager, null);
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getBackendAlias(Class<? extends PermissionBackend> backendClass) {
        if (registeredBackendAliases.containsValue(backendClass)) {
            for (String alias : registeredBackendAliases.keySet()) {
                if (registeredBackendAliases.get(alias).equals(backendClass)) {
                    return alias;
                }
            }
        }

        return backendClass.getName();
    }

    @SuppressWarnings("unchecked")
	public static Class<? extends PermissionBackend> getBackendClass(String alias) throws ClassNotFoundException {
        if (!registeredBackendAliases.containsKey(alias)) {
            return (Class<? extends PermissionBackend>) Class.forName(alias);
        }

        return registeredBackendAliases.get(alias);
    }

    public static String getBackendClassName(String alias) {
        if (registeredBackendAliases.containsKey(alias)) {
            return registeredBackendAliases.get(alias).getName();
        }

        return alias;
    }

    public static String getBackendPluginName(String alias) {
        String pluginName = getBackendClassName(alias);
        if (pluginName.lastIndexOf('.') > 0) {
            pluginName = pluginName.substring(pluginName.lastIndexOf('.'));
        }
        return pluginName.substring(1, pluginName.length() - "Support".length());
    }

    public static PermissionBackend getDefaultBackend() {
        return getBackend(null, StarGates.getPermissionManager(), null, defaultBackend);
    }

    public static List<String> getRegisteredAliases() {
        return new ArrayList<String>(registeredBackendAliases.keySet());
    }

    public static List<Class<? extends PermissionBackend>> getRegisteredClasses() {
        return new ArrayList<Class<? extends PermissionBackend>>(registeredBackendAliases.values());
    }

    public static void registerBackendAlias(String alias, Class<? extends PermissionBackend> backendClass) {
        if (!PermissionBackend.class.isAssignableFrom(backendClass)) {
            throw new WormholePermissionBackendException("Provided class should be subclass of PermissionBackend.class");
        }

        registeredBackendAliases.put(alias, backendClass);

        SGLogger.prettyLog(Level.INFO, false, "PermissionAlias backend: '" + alias + "' registered!");
    }

    protected PermissionManager manager;

    protected ConfigManager configManager;

    protected String providerName;

    protected PermissionBackend(PermissionManager manager, ConfigManager configManager, String providerName) {
        this.manager = manager;
        this.configManager = configManager;
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public boolean has(Player player, String permissionString) {
        return hasPermission(player, permissionString);
    }

    public abstract boolean hasPermission(Player player, String permissionString);

    public abstract void initialize();

    public abstract void reload();

}
