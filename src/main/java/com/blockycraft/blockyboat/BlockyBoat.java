package com.blockycraft.blockyboat;

import com.blockycraft.blockyboat.listeners.BoatBreakListener;
import com.blockycraft.blockyboat.listeners.BoatInteractListener;
import com.blockycraft.blockyboat.storage.BlockyBoatDatabase;
import com.blockycraft.blockyboat.storage.StorageManager;
import com.blockycraft.blockyboat.util.BoatRegistry;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Logger;

public class BlockyBoat extends JavaPlugin {
    private StorageManager storageManager;
    private BlockyBoatDatabase blockyBoatDatabase;
    private int autoSaveTaskId = -1;
    private int fastSaveTaskId = -1;
    private int forceBackupTaskId = -1;
    private Logger logger;
    private Configuration config;
    private int inventorySize;
    private String inventoryTitle;
    private int autoSaveInterval;

    @Override
    public void onEnable() {
        logger = Logger.getLogger("Minecraft");
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        loadConfiguration();
        BoatRegistry.loadRegistry(new File(getDataFolder(), "boats.db"));

        blockyBoatDatabase = new BlockyBoatDatabase(this);
        try {
            blockyBoatDatabase.connect();
        } catch (SQLException e) {
            logger.severe("[BlockyBoat] Falha ao conectar ao banco SQLite: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        storageManager = new StorageManager(blockyBoatDatabase, inventorySize, inventoryTitle);
        matchAllBoats();

        getServer().getPluginManager().registerEvent(
            org.bukkit.event.Event.Type.PLAYER_INTERACT_ENTITY,
            new BoatInteractListener(this, storageManager),
            org.bukkit.event.Event.Priority.High,
            this
        );
        getServer().getPluginManager().registerEvent(
            org.bukkit.event.Event.Type.VEHICLE_DESTROY,
            new BoatBreakListener(storageManager),
            org.bukkit.event.Event.Priority.Monitor,
            this
        );

        startAutoSaveTask();
        startFastSaveTask();
        startForceInventoryBackupTask();
        logger.info("[BlockyBoat] Plugin habilitado com identificador único por barco!");
    }

    @Override
    public void onDisable() {
        if (autoSaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autoSaveTaskId);
        }
        if (fastSaveTaskId != -1) {
            getServer().getScheduler().cancelTask(fastSaveTaskId);
        }
        if (forceBackupTaskId != -1) {
            getServer().getScheduler().cancelTask(forceBackupTaskId);
        }
        for (String id : storageManager.getAllInventories().keySet()) {
            storageManager.saveInventoryById(id);
        }
        if (blockyBoatDatabase != null) blockyBoatDatabase.close();
        BoatRegistry.saveRegistry(new File(getDataFolder(), "boats.db"));
        logger.info("[BlockyBoat] Plugin desabilitado!");
        BoatRegistry.clearAll();
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
                BoatRegistry.saveRegistry(new File(getDataFolder(), "boats.db"));
            }
        }, ticks, ticks);
    }

    private void startFastSaveTask() {
        long ticks = 2 * 20L;
        fastSaveTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (World world : getServer().getWorlds()) {
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (entity instanceof Boat) {
                            BoatRegistry.updateBoatPosition((Boat) entity);
                        }
                    }
                }
                for (String id : storageManager.getAllInventories().keySet()) {
                    storageManager.saveInventoryById(id);
                }
                BoatRegistry.saveRegistry(new File(getDataFolder(), "boats.db"));
            }
        }, ticks, ticks);
    }

    // --- NOVO: Force backup agressivo dos inventários a cada segundo (para evitar race condition de destruição da entidade) ---
    private void startForceInventoryBackupTask() {
        long ticks = 20L; // 1 segundo
        forceBackupTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (World world : getServer().getWorlds()) {
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (entity instanceof Boat) {
                            Boat boat = (Boat) entity;
                            storageManager.saveInventory(boat); // backup e persistência
                            BoatRegistry.updateBoatPosition(boat);
                        }
                    }
                }
                BoatRegistry.saveRegistry(new File(getDataFolder(), "boats.db"));
            }
        }, ticks, ticks);
    }

    private void matchAllBoats() {
        Server server = getServer();
        for (World world : server.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof Boat) {
                    Boat boat = (Boat) entity;
                    String boatId = BoatRegistry.findBoatIdForSpawn(world, boat.getLocation());
                    if (boatId != null) {
                        BoatRegistry.mapEntityToBoatId(boat.getEntityId(), boatId);
                    } else {
                        BoatRegistry.registerBoat(boat);
                    }
                }
            }
        }
    }

    public StorageManager getStorageManager() { return storageManager; }
    public BlockyBoatDatabase getBlockyBoatDatabase() { return blockyBoatDatabase; }
    public Configuration getPluginConfig() { return config; }
    public Logger getPluginLogger() { return logger; }
}
