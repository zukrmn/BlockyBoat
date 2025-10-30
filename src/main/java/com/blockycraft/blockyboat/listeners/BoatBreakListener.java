package com.blockycraft.blockyboat.listeners;

import com.blockycraft.blockyboat.BlockyBoat;
import com.blockycraft.blockyboat.storage.StorageManager;
import com.blockycraft.blockyboat.util.BoatIdentifier;
import org.bukkit.Material;
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

        // Sempre tenta buscar o inventário do barco, independentemente do motivo da destruição
        Inventory inventory = storageManager.getInventory(boat);

        // Se não encontrar, tenta usar o identificador diretamente (em caso de mudança de instância)
        if (inventory == null) {
            inventory = storageManager.getAllInventories().get(identifier);
        }

        // Dropa itens do inventário (se existir), inclusive em colisão
        if (inventory != null) {
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    boat.getWorld().dropItemNaturally(boat.getLocation(), item);
                }
            }
        } else {
            plugin.getPluginLogger().warning("[BlockyBoat] Inventário não encontrado na destruição do barco: " + identifier);
        }

        // Remove inventário da memória e persistência
        storageManager.removeInventory(boat);

        // Nota: no Beta 1.7.3, o próprio evento já dropa tábuas/gravetos da colisão. O barco "item" só é dropado se foi quebrado manualmente.
    }
}
