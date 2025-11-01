// BlockyBoatDatabase.java
// Corrigido para usar logger padrão "Minecraft"

package com.blockycraft.blockyboat.storage;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import java.util.logging.Logger;

public class BlockyBoatDatabase {
    private final Plugin plugin;
    private Connection connection;
    private Logger logger;

    public BlockyBoatDatabase(Plugin plugin) {
        this.plugin = plugin;
        this.logger = Logger.getLogger("Minecraft");
    }

    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(plugin.getDataFolder(), "blockyboat.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            initTables();
        } catch (ClassNotFoundException cnfe) {
            logger.severe("[BlockyBoat] Driver JDBC para SQLite não encontrado! Adicione sqlite-jdbc ao classpath ou verifique o empacotamento do plugin.");
            throw new SQLException("Driver JDBC SQLite não encontrado.", cnfe);
        } catch (SQLException e) {
            logger.severe("[BlockyBoat] Falha ao conectar ou criar o banco de dados SQLite: " + e.getMessage());
            if (e.getCause() != null) {
                logger.severe("[BlockyBoat] Causa: " + e.getCause());
            }
            throw new SQLException("Erro ao conectar ou criar o banco de dados SQLite", e);
        }
    }

    private void initTables() throws SQLException {
        Statement s = connection.createStatement();
        s.executeUpdate(
            "CREATE TABLE IF NOT EXISTS boats (" +
            "id TEXT PRIMARY KEY, " +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "last_update DATETIME)"
        );
        s.executeUpdate(
            "CREATE TABLE IF NOT EXISTS boat_inventory (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "boat_id TEXT, " +
            "slot INTEGER, " +
            "item_id INTEGER, " +
            "amount INTEGER, " +
            "data INTEGER, " +
            "FOREIGN KEY(boat_id) REFERENCES boats(id) ON DELETE CASCADE)"
        );
        s.close();
    }

    public void saveBoatInventory(String boatId, ItemStack[] items) throws SQLException {
        if (connection == null) throw new SQLException("Conexão SQLite não foi inicializada!");
        PreparedStatement psBoat = connection.prepareStatement(
            "INSERT OR IGNORE INTO boats (id, last_update) VALUES (?, CURRENT_TIMESTAMP)"
        );
        psBoat.setString(1, boatId);
        psBoat.executeUpdate();
        psBoat.close();

        PreparedStatement psDel = connection.prepareStatement(
            "DELETE FROM boat_inventory WHERE boat_id = ?"
        );
        psDel.setString(1, boatId);
        psDel.executeUpdate();
        psDel.close();

        PreparedStatement psIns = connection.prepareStatement(
            "INSERT INTO boat_inventory (boat_id, slot, item_id, amount, data) VALUES (?, ?, ?, ?, ?)"
        );
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getTypeId() == 0 || item.getAmount() == 0) continue;
            psIns.setString(1, boatId);
            psIns.setInt(2, i);
            psIns.setInt(3, item.getTypeId());
            psIns.setInt(4, item.getAmount());
            psIns.setInt(5, item.getDurability());
            psIns.addBatch();
        }
        psIns.executeBatch();
        psIns.close();

        PreparedStatement psUpd = connection.prepareStatement(
            "UPDATE boats SET last_update = CURRENT_TIMESTAMP WHERE id = ?"
        );
        psUpd.setString(1, boatId);
        psUpd.executeUpdate();
        psUpd.close();
    }

    public ItemStack[] loadBoatInventory(String boatId, int inventorySize) throws SQLException {
        if (connection == null) throw new SQLException("Conexão SQLite não foi inicializada!");
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 0; i < inventorySize; ++i) items.add(null);

        PreparedStatement ps = connection.prepareStatement(
            "SELECT slot, item_id, amount, data FROM boat_inventory WHERE boat_id = ?"
        );
        ps.setString(1, boatId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int slot = rs.getInt("slot");
            int typeId = rs.getInt("item_id");
            int amount = rs.getInt("amount");
            short data = (short) rs.getInt("data");
            if (slot >= 0 && slot < inventorySize && typeId > 0 && amount > 0) {
                ItemStack stack = new ItemStack(typeId, amount, data);
                items.set(slot, stack);
            }
        }
        rs.close();
        ps.close();
        return items.toArray(new ItemStack[items.size()]);
    }

    public void deleteBoat(String boatId) throws SQLException {
        if (connection == null) throw new SQLException("Conexão SQLite não foi inicializada!");
        PreparedStatement ps = connection.prepareStatement(
            "DELETE FROM boats WHERE id = ?"
        );
        ps.setString(1, boatId);
        ps.executeUpdate();
        ps.close();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
