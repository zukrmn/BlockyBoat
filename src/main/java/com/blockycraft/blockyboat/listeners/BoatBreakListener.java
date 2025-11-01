package com.blockycraft.blockyboat.listeners;

import com.blockycraft.blockyboat.storage.StorageManager;
import org.bukkit.entity.Boat;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        Inventory inventory = storageManager.getInventory(boat);
        if (inventory != null) {
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getTypeId() != 0 && item.getAmount() > 0) {
                    boat.getWorld().dropItemNaturally(boat.getLocation(), item);
                }
            }
        }
        storageManager.removeInventory(boat);
    }
}
