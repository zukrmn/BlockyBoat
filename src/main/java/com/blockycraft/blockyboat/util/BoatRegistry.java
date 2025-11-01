package com.blockycraft.blockyboat.util;

import org.bukkit.entity.Boat;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.io.*;

public class BoatRegistry {
    // entityId -> boatId (sessão do servidor)
    private static final Map<Integer, String> entityIdToBoatId = new HashMap<>();

    // Persistência: boatId -> dados do barco (posição inicial)
    private static final Map<String, BoatInfo> boatPersistMap = new HashMap<>();

    // Informações do barco para matching após reboot
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

    // Gera boatId único
    public static String generateBoatId() {
        return "boat_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 100000));
    }

    // Registra boatId para um entityId e salva boat info (usado na criação inicial)
    public static void registerBoat(Boat boat) {
        int entityId = boat.getEntityId();
        String boatId = entityIdToBoatId.get(entityId);
        if (boatId == null) {
            boatId = generateBoatId();
            entityIdToBoatId.put(entityId, boatId);
            Location loc = boat.getLocation();
            boatPersistMap.put(boatId, new BoatInfo(boatId, boat.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
    }

    // Usa entityId atual (durante a sessão)
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

    // Matching tolerante após reiniciar
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

    // Permite associar entityId a um boatId explicitamente (usado após reboot e matching)
    public static void mapEntityToBoatId(int entityId, String boatId) {
        entityIdToBoatId.put(entityId, boatId);
    }

    // SERIALIZAÇÃO: save/load boatPersistMap para disco
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

    // Chamada no onDisable
    public static void clearAll() {
        entityIdToBoatId.clear();
        boatPersistMap.clear();
    }
}
