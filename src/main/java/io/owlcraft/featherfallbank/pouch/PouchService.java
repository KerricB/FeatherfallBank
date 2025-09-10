package io.owlcraft.featherfallbank.pouch;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PouchService {
    private final File file;
    private final FileConfiguration data;

    public PouchService(File dataFolder) {
        this.file = new File(dataFolder, "pouch.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public long get(UUID uuid) {
        return data.getLong("players." + uuid + ".pouch", 0L);
    }

    public long get(OfflinePlayer p) { return get(p.getUniqueId()); }

    public void set(UUID uuid, long amount) {
        if (amount < 0) amount = 0;
        data.set("players." + uuid + ".pouch", amount);
        save();
    }

    public void add(UUID uuid, long delta) { set(uuid, get(uuid) + delta); }

    public boolean take(UUID uuid, long delta) {
        long cur = get(uuid);
        if (delta < 0) delta = 0;
        if (cur < delta) return false;
        set(uuid, cur - delta);
        return true;
    }

    private void save() {
        try { data.save(file); } catch (IOException ignored) {}
    }
}
