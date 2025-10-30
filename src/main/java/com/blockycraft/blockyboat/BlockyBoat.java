package com.blockycraft.blockyboat;

import com.blockycraft.blockyboat.listeners.BoatBreakListener;
import com.blockycraft.blockyboat.listeners.BoatInteractListener;
import com.blockycraft.blockyboat.storage.DataHandler;
import com.blockycraft.blockyboat.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import java.io.File;
import java.util.logging.Logger;

public class BlockyBoat extends JavaPlugin {
    private StorageManager storageManager;
    private DataHandler dataHandler;
    private int autoSaveTaskId = -1;
    private Logger logger;
    private Configuration config;

    @Override
    public void onEnable() {
        logger = Logger.getLogger("Minecraft");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        loadConfiguration();

        dataHandler = new DataHandler(this);
        storageManager = new StorageManager(this, dataHandler);

        try {
            dataHandler.loadData();
        } catch (Exception e) {
            logger.severe("[BlockyBoat] Falha ao carregar dados dos barcos: " + e.getMessage());
        }

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

        logger.info("[BlockyBoat] Plugin habilitado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (autoSaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autoSaveTaskId);
        }
        try {
            dataHandler.saveData();
            logger.info("[BlockyBoat] Todos os dados dos barcos salvos com sucesso!");
        } catch (Exception e) {
            logger.severe("[BlockyBoat] Falha ao salvar dados dos barcos: " + e.getMessage());
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
    }

    private void startAutoSaveTask() {
        int interval = config.getInt("auto-save-interval", 5);
        long ticks = interval * 60 * 20L;
        autoSaveTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    dataHandler.saveData();
                } catch (Exception e) {
                    logger.severe("[BlockyBoat] Auto-save falhou: " + e.getMessage());
                }
            }
        }, ticks, ticks);
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public Configuration getPluginConfig() {
        return config;
    }

    public Logger getPluginLogger() {
        return logger;
    }
}
