package com.blockycraft.blockyboat.util;

import org.bukkit.entity.Boat;

public class BoatIdentifier {
    /**
     * Gera um identificador único para um barco.
     * Usa o EntityId, que persiste somente durante a sessão.
     */
    public static String getIdentifier(Boat boat) {
        return "boat_" + boat.getEntityId();
    }
}
