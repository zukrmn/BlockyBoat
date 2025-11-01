package com.blockycraft.blockyboat.storage;

import com.blockycraft.blockyboat.util.BoatRegistry;
import org.bukkit.entity.Boat;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Armazena inventário persistente dos barcos, com backup agressivo para garantir drop ao destruir por qualquer evento.
 */
public class StorageManager {
    private final BlockyBoatDatabase database;
    private final int inventorySize;
    private final String inventoryTitle;
    private final Map<String, Inventory> inventories = new HashMap<>();
    private final Map<String, ItemStack[]> backupInventories = new HashMap<>(); // NOVO: backup agressivo

    public StorageManager(BlockyBoatDatabase database, int inventorySize, String inventoryTitle) {
        this.database = database;
        this.inventorySize = inventorySize;
        this.inventoryTitle = inventoryTitle;
    }

    public Inventory getInventory(Boat boat) {
        BoatRegistry.registerBoat(boat); // Garante que boatId existe
        String boatId = BoatRegistry.getBoatId(boat);
        if (boatId == null) return null;
        if (inventories.containsKey(boatId)) {
            backupInventories.put(boatId, inventories.get(boatId).getContents()); // backup sempre atualizado
            return inventories.get(boatId);
        }
        Inventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventory(
            new BoatInventory(inventoryTitle, inventorySize)
        );
        try {
            ItemStack[] loaded = database.loadBoatInventory(boatId, inventorySize);
            inventory.setContents(loaded);
            backupInventories.put(boatId, loaded); // backup inicial
        } catch (SQLException e) {}
        inventories.put(boatId, inventory);
        return inventory;
    }

    public void saveInventory(Boat boat) {
        String boatId = BoatRegistry.getBoatId(boat);
        Inventory inventory = inventories.get(boatId);
        if (inventory != null) {
            try {
                database.saveBoatInventory(boatId, inventory.getContents());
                backupInventories.put(boatId, inventory.getContents());
            } catch (SQLException e) {}
        }
    }

    public void saveInventoryById(String boatId) {
        Inventory inventory = inventories.get(boatId);
        if (inventory != null) {
            try {
                database.saveBoatInventory(boatId, inventory.getContents());
                backupInventories.put(boatId, inventory.getContents());
            } catch (SQLException e) {}
        }
    }

    public void removeInventory(Boat boat) {
        String boatId = BoatRegistry.getBoatId(boat);
        inventories.remove(boatId);
        backupInventories.remove(boatId); // Limpa backup também
        try {
            database.deleteBoat(boatId);
        } catch (SQLException e) {}
        BoatRegistry.unregisterBoat(boat);
    }

    public Map<String, Inventory> getAllInventories() { return inventories; }

    // NOVO: backup agressivo
    public ItemStack[] getBackupInventory(String boatId) {
        ItemStack[] backup = backupInventories.get(boatId);
        if (backup == null) {
            backup = new ItemStack[inventorySize];
        }
        return backup;
    }

    private static class BoatInventory implements net.minecraft.server.IInventory {
        private final net.minecraft.server.ItemStack[] items;
        private final String name;

        public BoatInventory(String name, int size) {
            this.name = name;
            this.items = new net.minecraft.server.ItemStack[size];
        }

        @Override public int getSize() { return items.length; }
        @Override public net.minecraft.server.ItemStack getItem(int i) { return items[i]; }
        @Override public net.minecraft.server.ItemStack splitStack(int i, int j) {
            if (items[i] != null) {
                net.minecraft.server.ItemStack itemstack;
                if (items[i].count <= j) {
                    itemstack = items[i];
                    items[i] = null;
                    return itemstack;
                } else {
                    itemstack = items[i].a(j);
                    if (items[i].count == 0) { items[i] = null; }
                    return itemstack;
                }
            }
            return null;
        }
        @Override public void setItem(int i, net.minecraft.server.ItemStack itemstack) { items[i] = itemstack; }
        @Override public String getName() { return name; }
        @Override public int getMaxStackSize() { return 64; }
        @Override public void update() {}
        @Override public boolean a_(net.minecraft.server.EntityHuman entityhuman) { return true; }
        @Override public net.minecraft.server.ItemStack[] getContents() { return items; }
    }
}
