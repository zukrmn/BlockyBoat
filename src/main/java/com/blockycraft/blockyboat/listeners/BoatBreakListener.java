package com.blockycraft.blockyboat.listeners;

import com.blockycraft.blockyboat.BlockyBoat;
import com.blockycraft.blockyboat.storage.StorageManager;
import com.blockycraft.blockyboat.util.BoatIdentifier;
import org.bukkit.entity.Boat;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BoatBreakListener extends VehicleListener {
    private final StorageManager storageManager;
    private final BlockyBoat plugin;

    public BoatBreakListener(BlockyBoat plugin, StorageManager storageManager) {
        this.storageManager = storageManager;
        this.plugin = plugin;
    }

    @Override
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getVehicle() instanceof Boat)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        Boat boat = (Boat) event.getVehicle();
        String identifier = BoatIdentifier.getIdentifier(boat);

        Inventory inventory = storageManager.getInventory(boat);

        if (inventory != null) {
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getTypeId() != 0 && item.getAmount() > 0) {
                    boat.getWorld().dropItemNaturally(boat.getLocation(), item);
                }
            }
        } else {
            plugin.getPluginLogger().warning("[BlockyBoat] Inventário não encontrado na destruição do barco: " + identifier);
        }
        storageManager.removeInventory(boat);
        // O evento já dropa os itens padrão do barco
    }
}
