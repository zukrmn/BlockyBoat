package com.blockycraft.blockyboat.storage;

import com.blockycraft.blockyboat.BlockyBoat;
import com.blockycraft.blockyboat.util.BoatIdentifier;
import org.bukkit.entity.Boat;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class StorageManager {
    private final DataHandler dataHandler;
    private final Map<String, Inventory> inventories;
    private boolean dirty = false;

    public StorageManager(BlockyBoat plugin, DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        this.inventories = new HashMap<String, Inventory>();
    }

    public Inventory getInventory(Boat boat) {
        String identifier = BoatIdentifier.getIdentifier(boat);

        // Retorna inventário existente se já criado
        if (inventories.containsKey(identifier)) {
            return inventories.get(identifier);
        }

        // Cria inventário customizado
        BoatInventory boatInventory = new BoatInventory("Boat", 27); // Tamanho padrão, atualizado depois
        // Envolve em CraftInventory para integração com Bukkit/NMS
        Inventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventory(boatInventory);

        // Carrega itens salvos, se houver
        ItemStack[] savedItems = dataHandler.getStoredItems(identifier);
        if (savedItems != null) {
            inventory.setContents(savedItems);
        }

        inventories.put(identifier, inventory);
        return inventory;
    }

    public void removeInventory(Boat boat) {
        String identifier = BoatIdentifier.getIdentifier(boat);
        inventories.remove(identifier);
        dataHandler.removeStoredItems(identifier);
        dirty = true;
    }

    public Map<String, Inventory> getAllInventories() {
        return inventories;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    // Inner class: implementa IInventory para o barco (Beta 1.7.3)
    private static class BoatInventory implements net.minecraft.server.IInventory {
        private final net.minecraft.server.ItemStack[] items;
        private final String name;

        public BoatInventory(String name, int size) {
            this.name = name;
            this.items = new net.minecraft.server.ItemStack[size];
        }

        @Override
        public int getSize() {
            return items.length;
        }

        @Override
        public net.minecraft.server.ItemStack getItem(int i) {
            return items[i];
        }

        @Override
        public net.minecraft.server.ItemStack splitStack(int i, int j) {
            if (items[i] != null) {
                net.minecraft.server.ItemStack itemstack;
                if (items[i].count <= j) {
                    itemstack = items[i];
                    items[i] = null;
                    return itemstack;
                } else {
                    itemstack = items[i].a(j);
                    if (items[i].count == 0) {
                        items[i] = null;
                    }
                    return itemstack;
                }
            }
            return null;
        }

        @Override
        public void setItem(int i, net.minecraft.server.ItemStack itemstack) {
            items[i] = itemstack;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void update() {
            // Não implementado
        }

        @Override
        public boolean a_(net.minecraft.server.EntityHuman entityhuman) {
            return true;
        }

        @Override
        public net.minecraft.server.ItemStack[] getContents() {
            return items;
        }
    }
}
