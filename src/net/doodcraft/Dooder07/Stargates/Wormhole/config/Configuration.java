package net.doodcraft.Dooder07.Stargates.Wormhole.config;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager.ConfigKeys;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager.PermissionLevel;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public class Configuration {

    private static File options = null;

    private static boolean invalidFile(final File file, final PluginDescriptionFile desc) {
        BufferedReader bufferedreader = null;
        try {
            bufferedreader = new BufferedReader(new FileReader(file));
            for (String s = ""; (s = bufferedreader.readLine()) != null;) {
                if (s.indexOf(desc.getVersion()) > -1) {
                    return false;
                }
            }
        } catch (final IOException exception) {
            return true;
        } finally {
            try {
                if (bufferedreader != null) {
                    bufferedreader.close();
                }
            } catch (final IOException e) {
                SGLogger.prettyLog(Level.WARNING, false, "Failure to close stream: " + e.getMessage());
            }
        }
        return true;
    }

    protected static void loadConfiguration(final PluginDescriptionFile desc) {
        readFile(desc);
    }

    private static void readFile(final File file, final PluginDescriptionFile desc) throws IOException {

        for (final Setting element : DefaultSettings.config) {

            final String value = ConfigurationFlatFile.getValueFromSetting(file, element.getName(), element.getValue().toString());

            if (value.toLowerCase().contains("true") || value.toLowerCase().contains("false")) {
                final Setting s = new Setting(element.getName(), Boolean.parseBoolean(value), element.getDescription(), "StarGates");
                ConfigManager.getConfigurations().put(s.getName(), s);
            } else {
			
                try {
                    final Setting s = new Setting(element.getName(), Integer.parseInt(value), element.getDescription(), "StarGates");
                    ConfigManager.getConfigurations().put(s.getName(), s);
                } catch (final NumberFormatException e) {
                    Setting s = null;
                    try {
                        s = new Setting(element.getName(), Double.parseDouble(value), element.getDescription(), "StarGates");
                    } catch (final NumberFormatException nfe) {

                        if (element.getName() == ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL) {
                            s = new Setting(element.getName(), PermissionLevel.valueOf(value), element.getDescription(), "StarGates");
                        } else {

                            s = new Setting(element.getName(), value, element.getDescription(), "StarGates");
                        }
                    }

                    ConfigManager.getConfigurations().put(s.getName(), s);
                }
            }
        }
    }

    private static void readFile(final PluginDescriptionFile desc) {
        final File directory = new File("plugins" + File.separator + desc.getName() + File.separator);
        if (!directory.exists()) {
            try {
                directory.mkdir();
            } catch (final Exception e) {
                SGLogger.prettyLog(Level.SEVERE, false, "Unable to make directory: " + e.getMessage());
            }
        }
        final String input = directory.getPath() + File.separator + "Settings.txt";
        options = new File(input);
        if (!options.exists()) {
            writeFile(options, desc, DefaultSettings.config);
        }
        try {
            readFile(options, desc);
        } catch (final IOException e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Failed to read file: " + e.getMessage());
        }
        if (invalidFile(options, desc)) {
            writeFile(desc);
        }
    }

    private static void writeFile(final File file, final PluginDescriptionFile desc, Setting[] config) {
        try {
            try {
                file.createNewFile();
            } catch (final Exception e) {
                SGLogger.prettyLog(Level.SEVERE, false, "Unable to create file: " + e.getMessage());
            }
            final BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(file));

            ConfigurationFlatFile.createNewHeader(bufferedwriter, desc.getName() + " " + desc.getVersion(), desc.getName() + " Config Settings", true);

            for (final Setting element : config) {
                ConfigurationFlatFile.createNewSetting(bufferedwriter, element.getName(), element.getValue().toString(), element.getDescription());
            }
            bufferedwriter.close();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void writeFile(final PluginDescriptionFile desc) {
        try {
            try {
                options.createNewFile();
            } catch (final Exception e) {
                SGLogger.prettyLog(Level.SEVERE, false, "Unable to create new file: " + e.getMessage());
            }
            final BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(options));

            ConfigurationFlatFile.createNewHeader(bufferedwriter, desc.getName() + " " + desc.getVersion(), desc.getName() + " Config Settings", true);

            final Set<ConfigKeys> keys = ConfigManager.getConfigurations().keySet();
            final ArrayList<ConfigKeys> list = new ArrayList<ConfigKeys>(keys);
            Collections.sort(list);
            for (final ConfigKeys key : list) {
                final Setting s = ConfigManager.getConfigurations().get(key);
                if (s != null) {
                    ConfigurationFlatFile.createNewSetting(bufferedwriter, s.getName(), s.getValue().toString(), s.getDescription());
                }

            }
            bufferedwriter.close();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }
}
