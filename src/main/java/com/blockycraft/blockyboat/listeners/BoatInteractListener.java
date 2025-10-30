package com.blockycraft.blockyboat.listeners;

import com.blockycraft.blockyboat.BlockyBoat;
import com.blockycraft.blockyboat.storage.StorageManager;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Method;

public class BoatInteractListener extends PlayerListener {
    private final StorageManager storageManager;
    private Method openInventoryMethod = null;

    public BoatInteractListener(BlockyBoat plugin, StorageManager storageManager) {
        this.storageManager = storageManager;
        // Busca o método correto para abrir inventário usando reflection
        try {
            Class<?> entityPlayerClass = net.minecraft.server.EntityPlayer.class;
            try {
                openInventoryMethod = entityPlayerClass.getMethod("openInventory", net.minecraft.server.IInventory.class);
            } catch (NoSuchMethodException e1) {
                try {
                    openInventoryMethod = entityPlayerClass.getMethod("a", net.minecraft.server.IInventory.class);
                } catch (NoSuchMethodException e2) {
                    for (Method method : entityPlayerClass.getMethods()) {
                        Class<?>[] params = method.getParameterTypes();
                        if (params.length == 1 && params[0].equals(net.minecraft.server.IInventory.class)) {
                            openInventoryMethod = method;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getPluginLogger().severe("[BlockyBoat] Falha ao localizar método para abrir inventário: " + e.getMessage());
        }
    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Verifica se a entidade clicada é um barco
        if (!(event.getRightClicked() instanceof Boat)) {
            return;
        }
        Player player = event.getPlayer();
        Boat boat = (Boat) event.getRightClicked();

        // Se o jogador estiver agachado (Shift), abre o inventário do barco
        if (player.isSneaking()) {
            event.setCancelled(true);

            Inventory inventory = storageManager.getInventory(boat);

            try {
                CraftPlayer craftPlayer = (CraftPlayer) player;
                net.minecraft.server.EntityPlayer entityPlayer = craftPlayer.getHandle();
                org.bukkit.craftbukkit.inventory.CraftInventory craftInventory = (org.bukkit.craftbukkit.inventory.CraftInventory) inventory;
                net.minecraft.server.IInventory iInventory = craftInventory.getInventory();
                if (openInventoryMethod != null) {
                    openInventoryMethod.invoke(entityPlayer, iInventory);
                }
            } catch (Exception e) {
                // Falha silenciosa: não abre inventário se der erro
            }
        }
        // Se não estiver agachado, permite comportamento normal: embarcar no barco
    }
}
