package com.blockycraft.blockyboat;

import com.blockycraft.blockyboat.listeners.BoatBreakListener;
import com.blockycraft.blockyboat.listeners.BoatInteractListener;
import com.blockycraft.blockyboat.storage.BlockyBoatDatabase;
import com.blockycraft.blockyboat.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Logger;

public class BlockyBoat extends JavaPlugin {
    private StorageManager storageManager;
    private BlockyBoatDatabase blockyBoatDatabase;
    private int autoSaveTaskId = -1;
    private Logger logger;
    private Configuration config;
    private int inventorySize;
    private String inventoryTitle;
    private int autoSaveInterval;

    @Override
    public void onEnable() {
        logger = Logger.getLogger("Minecraft");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        loadConfiguration();

        blockyBoatDatabase = new BlockyBoatDatabase(this);
        try {
            blockyBoatDatabase.connect();
        } catch (SQLException e) {
            logger.severe("[BlockyBoat] Falha ao conectar ao banco SQLite: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storageManager = new StorageManager(blockyBoatDatabase, inventorySize, inventoryTitle);

        getServer().getPluginManager().registerEvent(
                org.bukkit.event.Event.Type.PLAYER_INTERACT_ENTITY,
                new BoatInteractListener(this, storageManager),
                org.bukkit.event.Event.Priority.High,
                this
        );
        getServer().getPluginManager().registerEvent(
                org.bukkit.event.Event.Type.VEHICLE_DESTROY,
                new BoatBreakListener(this, storageManager),
                org.bukkit.event.Event.Priority.Monitor,
                this
        );
        startAutoSaveTask();

        logger.info("[BlockyBoat] Plugin habilitado com armazenamento via SQLite!");
    }

    @Override
    public void onDisable() {
        if (autoSaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autoSaveTaskId);
        }
        // Salva todos os inventários dos barcos ativos
        for (String id : storageManager.getAllInventories().keySet()) {
            storageManager.saveInventoryById(id);
        }
        if (blockyBoatDatabase != null) {
            blockyBoatDatabase.close();
        }
        logger.info("[BlockyBoat] Plugin desabilitado!");
    }

    private void loadConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = new Configuration(configFile);
                config.setProperty("auto-save-interval", 5);
                config.setProperty("inventory-size", 27);
                config.setProperty("inventory-title", "Boat Storage");
                config.save();
            } catch (Exception e) {
                logger.severe("[BlockyBoat] Não foi possível criar config.yml: " + e.getMessage());
            }
        } else {
            config = new Configuration(configFile);
            config.load();
        }
        autoSaveInterval = config.getInt("auto-save-interval", 5);
        inventorySize = config.getInt("inventory-size", 27);
        inventoryTitle = config.getString("inventory-title", "Boat Storage");
    }

    private void startAutoSaveTask() {
        long ticks = autoSaveInterval * 60 * 20L;
        autoSaveTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (String id : storageManager.getAllInventories().keySet()) {
                    storageManager.saveInventoryById(id);
                }
            }
        }, ticks, ticks);
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public BlockyBoatDatabase getBlockyBoatDatabase() {
        return blockyBoatDatabase;
    }

    public Configuration getPluginConfig() {
        return config;
    }

    public Logger getPluginLogger() {
        return logger;
    }
}
