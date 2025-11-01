package com.blockycraft.blockyboat.util;

import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.Location;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoatIdentifier {
    // Armazena posições (world_x_y_z) -> identificador persistente
    private static final Map<String, String> persistentIdMap = new ConcurrentHashMap<>();
    // Associa entityId atual a identificador persistente durante a sessão
    private static final Map<Integer, String> entityIdMap = new ConcurrentHashMap<>();

    // Gera string de chave por posição
    public static String getBoatKey(World world, Location loc) {
        return world.getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    // Matching tolerante: busca identificador persistente no raio de 1 bloco
    public static String findIdentifierForBoat(World world, Location loc) {
        for (String key : persistentIdMap.keySet()) {
            String[] parts = key.split("_");
            if (!parts[0].equals(world.getName())) continue;
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            if (Math.abs(loc.getBlockX() - x) <= 1 &&
                Math.abs(loc.getBlockY() - y) <= 1 &&
                Math.abs(loc.getBlockZ() - z) <= 1) {
                return persistentIdMap.get(key);
            }
        }
        return null;
    }

    // Na inicialização da sessão, mapeia entityId do barco ao identificador persistente
    public static void mapEntityIdToPersistentId(int entityId, String persistentId) {
        entityIdMap.put(entityId, persistentId);
    }

    // Gera identificador persistente novo para barcos nunca salvos antes
    public static String generateNewIdentifier(Boat boat) {
        String id = "boat_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 100000));
        String key = getBoatKey(boat.getWorld(), boat.getLocation());
        persistentIdMap.put(key, id);
        entityIdMap.put(boat.getEntityId(), id);
        return id;
    }

    // Durante a sessão, obtém identificador persistente pelo entityId
    public static String getIdentifier(Boat boat) {
        int entityId = boat.getEntityId();
        if (entityIdMap.containsKey(entityId)) {
            return entityIdMap.get(entityId);
        }
        // matching tolerante se não tiver, tenta associar agora
        String persistentId = findIdentifierForBoat(boat.getWorld(), boat.getLocation());
        if (persistentId != null) {
            mapEntityIdToPersistentId(entityId, persistentId);
            return persistentId;
        }
        // Não achou, gera novo
        return generateNewIdentifier(boat);
    }

    // Usado pelo plugin para persistir no disco
    public static Map<String, String> getPersistentMap() {
        return persistentIdMap;
    }
    public static void restorePersistentMap(Map<String, String> fromDisk) {
        persistentIdMap.clear();
        persistentIdMap.putAll(fromDisk);
    }
    public static void clearEntityMap() {
        entityIdMap.clear();
    }
}
