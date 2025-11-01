package com.blockycraft.blockyboat.util;

import org.bukkit.entity.Boat;
import org.bukkit.Location;

/**
 * Gera um identificador único semi-persistente para um barco.
 * Usa posição inicial e mundo — persistente entre reinícios.
 * OBS: Se o barco for deslocado, o inventário ficará preso à última posição salva.
 * (Para versões antigas sem UUID, posicionamento é a opção mais robusta.)
 */
public class BoatIdentifier {

    /**
     * Cria um identificador por posição e mundo, com precisão de bloco.
     * Exemplo: "world_-123_65_456"
     */
    public static String getIdentifier(Boat boat) {
        Location loc = boat.getLocation();
        return boat.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }
}
