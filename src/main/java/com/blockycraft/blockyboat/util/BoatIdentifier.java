package com.blockycraft.blockyboat.util;

import org.bukkit.entity.Boat;

public class BoatIdentifier {
    /**
     * Gera um identificador único para um barco.
     * Usa o EntityId, que em Beta 1.7.3 persiste apenas na sessão.
     * Para persistência cross-restart seria necessário outro método (mais complexo).
     */
    public static String getIdentifier(Boat boat) {
        return "boat_" + boat.getEntityId();
    }
}
