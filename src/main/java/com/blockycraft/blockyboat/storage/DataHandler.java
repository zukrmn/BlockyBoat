package com.blockycraft.blockyboat.storage;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataHandler {
    // O campo 'plugin' foi removido pois não era usado.
    private final Map<String, ItemStack[]> data = new HashMap<String, ItemStack[]>();
    private final File dataFile;
    private Configuration config;

    public DataHandler(org.bukkit.plugin.Plugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        if (!dataFile.exists()) {
            data.clear();
            return;
        }
        config = new Configuration(dataFile);
        config.load();
        data.clear();

        for (String id : config.getKeys()) {
            Object raw = config.getProperty(id);
            if (raw instanceof ItemStack[]) {
                data.put(id, (ItemStack[]) raw);
            } else if (raw instanceof java.util.List) {
                java.util.List<Object> list = (java.util.List<Object>) raw;
                ItemStack[] items = new ItemStack[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    items[i] = (ItemStack) list.get(i);
                }
                data.put(id, items);
            }
        }
    }

    public void saveData() {
        config = new Configuration(dataFile);
        for (Map.Entry<String, ItemStack[]> entry : data.entrySet()) {
            config.setProperty(entry.getKey(), entry.getValue());
        }
        config.save();
    }

    public void removeStoredItems(String identifier) {
        data.remove(identifier);
    }

    public ItemStack[] getStoredItems(String identifier) {
        return data.get(identifier);
    }

    public void putStoredItems(String identifier, ItemStack[] items) {
        data.put(identifier, items);
    }
}
