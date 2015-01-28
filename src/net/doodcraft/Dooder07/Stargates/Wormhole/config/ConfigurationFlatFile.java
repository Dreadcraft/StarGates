package net.doodcraft.Dooder07.Stargates.Wormhole.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager.ConfigKeys;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

public class ConfigurationFlatFile {

    protected static void createNewHeader(final BufferedWriter output, final String title, final String subtitle, final boolean firstHeader) throws IOException {
        final String linebreak = "-------------------------------";
        if (!firstHeader) {
            output.write("---------------");
            output.newLine();
            output.newLine();
            output.write(linebreak);
            output.newLine();
        }
        output.write(title);
        output.newLine();
        output.write(subtitle);
        output.newLine();
        output.write(linebreak);
        output.newLine();
        output.newLine();
    }

    protected static void createNewSetting(final BufferedWriter output, final ConfigKeys name, final String value, final String description) throws IOException {
        final String linebreak = "---------------";
        output.append(linebreak);
        output.newLine();
        output.write("Setting: " + name);
        output.newLine();
        output.write("Value: " + value);
        output.newLine();
        output.write("Description:");

        final ArrayList<String> desc = new ArrayList<String>();
        desc.add(0, "");
        final int maxLength = 80;
        final String[] words = description.split(" ");
        int lineNumber = 0;
        for (final String word : words) {
            if (desc.get(lineNumber).length() + word.length() < maxLength) {
                desc.set(lineNumber, desc.get(lineNumber) + " " + word);
            } else {
                lineNumber++;
                desc.add(lineNumber, "             " + word);
            }
        }
        for (final String s : desc) {
            output.write(s);
            output.newLine();
        }
    }

    protected static String getValueFromSetting(final File input, final ConfigKeys name, final String defaultVal) throws IOException {

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(input));
            for (String s = ""; (s = bufferedReader.readLine()) != null;) {
                try {
                    s = s.trim();
                    if (s.contains("Setting:")) {
                        final String key[] = s.split(":");
                        key[1] = key[1].trim();
                        final ConfigKeys key_value = ConfigKeys.valueOf(key[1]);
                        if (key_value == name) {
                            //Next line
                            if ((s = bufferedReader.readLine()) != null) {
                                final String val[] = s.split(":");
                                bufferedReader.close();
                                return val[1].trim();
                            }
                        }
                    }
                } catch (final Exception e) {
                    SGLogger.prettyLog(Level.FINE, false, "Error parsing setting enum:" + e.toString());
                }
            }
            bufferedReader.close();

        } catch (final FileNotFoundException e) {
            SGLogger.prettyLog(Level.FINE, false, e.getMessage());
        } finally {
            bufferedReader.close();
        }
        return defaultVal.trim();
    }
}
