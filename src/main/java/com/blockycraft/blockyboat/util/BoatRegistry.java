package com.blockycraft.blockyboat.util;

import org.bukkit.entity.Boat;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.*;
import java.io.*;

public class BoatRegistry {
    private static final Map<Integer, String> entityIdToBoatId = new HashMap<>();
    private static final Map<String, BoatInfo> boatPersistMap = new HashMap<>();

    public static class BoatInfo {
        public final String boatId;
        public final String worldName;
        public final int x, y, z;

        public BoatInfo(String boatId, String worldName, int x, int y, int z) {
            this.boatId = boatId;
            this.worldName = worldName;
            this.x = x; this.y = y; this.z = z;
        }
    }

    public static String generateBoatId() {
        return "boat_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 100000));
    }

    public static void registerBoat(Boat boat) {
        int entityId = boat.getEntityId();
        if (!entityIdToBoatId.containsKey(entityId)) {
            String boatId = generateBoatId();
            entityIdToBoatId.put(entityId, boatId);
            Location loc = boat.getLocation();
            boatPersistMap.put(boatId, new BoatInfo(boatId, boat.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
    }

    public static String getBoatId(Boat boat) {
        return entityIdToBoatId.get(boat.getEntityId());
    }
    public static BoatInfo getBoatInfo(String boatId) {
        return boatPersistMap.get(boatId);
    }
    public static void unregisterBoat(Boat boat) {
        int entityId = boat.getEntityId();
        String boatId = entityIdToBoatId.remove(entityId);
        if (boatId != null) boatPersistMap.remove(boatId);
    }

    public static String findBoatIdForSpawn(World world, Location loc) {
        for (BoatInfo info : boatPersistMap.values()) {
            if (info.worldName.equals(world.getName())
                && Math.abs(info.x - loc.getBlockX()) <= 1
                && Math.abs(info.y - loc.getBlockY()) <= 1
                && Math.abs(info.z - loc.getBlockZ()) <= 1)
                return info.boatId;
        }
        return null;
    }

    public static void mapEntityToBoatId(int entityId, String boatId) {
        entityIdToBoatId.put(entityId, boatId);
    }

    // Atualiza a posição mais recente do barco ao salvar!
    public static void updateBoatPosition(Boat boat) {
        String boatId = getBoatId(boat);
        if (boatId != null) {
            Location loc = boat.getLocation();
            boatPersistMap.put(boatId, new BoatInfo(boatId, boat.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
    }

    public static void saveRegistry(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (BoatInfo info : boatPersistMap.values()) {
                writer.write(info.boatId + ";" + info.worldName + ";" + info.x + ";" + info.y + ";" + info.z);
                writer.newLine();
            }
        } catch (Exception e) {}
    }
    public static void loadRegistry(File file) {
        boatPersistMap.clear();
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";");
                if (tokens.length == 5) {
                    String boatId = tokens[0];
                    String worldName = tokens[1];
                    int x = Integer.parseInt(tokens[2]);
                    int y = Integer.parseInt(tokens[3]);
                    int z = Integer.parseInt(tokens[4]);
                    boatPersistMap.put(boatId, new BoatInfo(boatId, worldName, x, y, z));
                }
            }
        } catch (Exception e) {}
    }

    public static void clearAll() {
        entityIdToBoatId.clear();
        boatPersistMap.clear();
    }
}
