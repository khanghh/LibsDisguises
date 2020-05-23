package me.libraryaddict.disguise.utilities.sounds;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 23/05/2020.
 */
public class SoundManager {
    public void load() {
        loadSounds();
        loadCustomSounds();
    }

    private void loadCustomSounds() {
        File f = new File(LibsDisguises.getInstance().getDataFolder(), "sounds.yml");

        if (!f.exists()) {
            LibsDisguises.getInstance().saveResource("sounds.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key) || key.equals("GroupName")) {
                continue;
            }

            if (SoundGroup.getGroups().keySet().stream().anyMatch(k -> k.equalsIgnoreCase(key))) {
                DisguiseUtilities.getLogger().warning("The SoundGroup " + key + " has already been registered!");
                continue;
            }

            SoundGroup group = new SoundGroup(key);
            ConfigurationSection section = config.getConfigurationSection(key);

            for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
                if (type == SoundGroup.SoundType.CANCEL) {
                    continue;
                }

                List<String> list = section
                        .getStringList(type.name().charAt(0) + type.name().substring(1).toLowerCase());

                if (list == null || list.isEmpty()) {
                    continue;
                }

                for (String sound : list) {
                    if (!sound.matches(".+:.+")) {
                        DisguiseUtilities.getLogger()
                                .warning("Invalid sound '" + sound + "'! Must be a minecraft:sound.name");
                        continue;
                    }

                    group.addSound(sound, type);
                }
            }

            DisguiseUtilities.getLogger().info("Loaded sound group '" + key + "'");
        }
    }

    private void loadSounds() {
        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCODED_WITH_SOUNDS")) {
            List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.toList());

            for (String line : lines) {
                String[] groups = line.split("/", -1);

                SoundGroup group = new SoundGroup(groups[0]);

                int i = 0;
                for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
                    String s = groups[++i];

                    if (s.isEmpty()) {
                        continue;
                    }

                    String[] sounds = s.split(",");

                    for (String sound : sounds) {
                        try {
                            Sound actualSound = Sound.valueOf(sound);

                            group.addSound(actualSound, type);
                        }
                        catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        catch (IOException | NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }
}
