package com.blockycraft.blockyboat.listeners;

import com.blockycraft.blockyboat.storage.StorageManager;
import com.blockycraft.blockyboat.util.BoatRegistry;
import org.bukkit.entity.Boat;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Garante que o inventário do barco é dropado mesmo em destruição por colisão, usando backup agressivo.
 */
public class BoatBreakListener extends VehicleListener {
    private final StorageManager storageManager;

    public BoatBreakListener(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @Override
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getVehicle() instanceof Boat)) return;
        if (event.isCancelled()) return;
        Boat boat = (Boat) event.getVehicle();
        String boatId = BoatRegistry.getBoatId(boat);

        Inventory inventory = storageManager.getInventory(boat);
        ItemStack[] dropItems = null;

        // Prioridade - usa inventory se disponível, senão, backup!
        if (inventory != null) {
            dropItems = inventory.getContents();
        } else if (boatId != null) {
            dropItems = storageManager.getBackupInventory(boatId);
        }

        if (dropItems != null) {
            for (ItemStack item : dropItems) {
                if (item != null && item.getTypeId() != 0 && item.getAmount() > 0) {
                    boat.getWorld().dropItemNaturally(boat.getLocation(), item);
                }
            }
        }
        storageManager.removeInventory(boat);
    }
}
