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
        // Verifica se o veículo é um barco
        if (!(event.getVehicle() instanceof Boat)) {
            return;
        }
        // Ignora o evento caso ele seja cancelado
        if (event.isCancelled()) {
            return;
        }
        Boat boat = (Boat) event.getVehicle();
        String identifier = BoatIdentifier.getIdentifier(boat);

        // Busca inventário persistente do barco
        Inventory inventory = storageManager.getInventory(boat);

        // Dropa itens armazenados no inventário
        if (inventory != null) {
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getTypeId() != 0 && item.getAmount() > 0) {
                    boat.getWorld().dropItemNaturally(boat.getLocation(), item);
                }
            }
        } else {
            plugin.getPluginLogger().warning("[BlockyBoat] Inventário não encontrado na destruição do barco: " + identifier);
        }

        // Remove inventário do barco (deleta dos registros)
        storageManager.removeInventory(boat);
        // O próprio evento já dropa os itens padrões (gravetos/tábuas)
    }
}
