package net.doodcraft.Dooder07.Stargates.Wormhole.config;

import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager.ConfigKeys;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager.PermissionLevel;

public class Setting {

    private final ConfigKeys name;
    private final String desc;
    private Object value;
    private final String plugin;

    protected Setting(final ConfigKeys name, Object value, String desc, String plugin) {
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.plugin = plugin;
    }

    public boolean getBooleanValue() {
        return ((Boolean) value).booleanValue();
    }

    public String getDescription() {
        return desc;
    }

    public double getDoubleValue() {
        return ((Double) value).doubleValue();
    }

    public int getIntValue() {
        return ((Integer) value).intValue();
    }

    public Level getLevel() {
        return Level.parse((String) value);
    }

    public Material getMaterialValue() {
        return (Material) value;
    }

    public ConfigKeys getName() {
        return name;
    }

    public PermissionLevel getPermissionLevel() {
        return (PermissionLevel) value;
    }

    public String getPluginName() {
        return plugin;
    }

    public String getStringValue() {
        return (String) value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }
}
