/*
 * Store.java
 *
 * Created on 21. z��� 2005, 13:50
 */
package cz.control.business;

import cz.control.data.Goods;
import cz.control.data.GoodsTradesHistory;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static cz.control.database.DatabaseAccess.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro editaci skladu. Pracuje s datab�z� ve kter� jsou ulo�eny skladov�
 * karty.
 * Obsahuje ve�ejn� metody ke kter�m maj� p��stup v�ichni u�ivatele. D�le obsahuje
 * p��telsk� metody, ke kter�m maj� p��stup jenom ur�it� u�ivatele pomoc� t��dy User
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public final class Store {

    private static final String GOODS_NAME = DatabaseAccess.GOODS_TABLE_NAME; // ulo� n�zev datab�ze
    public static final BigDecimal CENT = new BigDecimal(100);

    /* Zaka� vytv��et instance z venku */
    Store() {
    }

    /*
     *  Vyma�e zbo�� z datab�ze
     */
    void deleteGoods(Goods goods) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + GOODS_NAME + " WHERE goods_id = '" + goods.getGoodsID() + "'";
        stm.executeUpdate(command);
        stm.close();
    }

    /**
     * Vyma�e v�ce zbo�� z datab�ze
     * @param goods 
     */
    void deleteGoods(List<Goods> goods) throws SQLException {

        if (goods.isEmpty()) {
            return;
        }
        
        DatabaseAccess.setAutoCommit(false);

        try {

            // disconnect dependencies
            String ids = "";

            for (Goods good : goods) {
                ids += "'" + good.getGoodsID() + "', ";
            }
            ids = ids.substring(0, ids.length() - 2) + ")";
            
            Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
            stm.executeUpdate("UPDATE " + BUY_TABLE_NAME + " SET goods_id = null WHERE goods_id IN (" + ids);
            stm.close();
            
            stm = DatabaseAccess.getCurrentConnection().createStatement();
            stm.executeUpdate("UPDATE " + SALE_TABLE_NAME + " SET goods_id = null WHERE goods_id IN (" + ids);
            stm.close();
            
            stm = DatabaseAccess.getCurrentConnection().createStatement();
            stm.executeUpdate("UPDATE " + STOCKING_TABLE_NAME + " SET goods_id = null WHERE goods_id IN (" + ids);
            stm.close();
            
            stm = DatabaseAccess.getCurrentConnection().createStatement();
            stm.executeUpdate("DELETE FROM " + GOODS_NAME + " WHERE goods_id IN (" + ids);
            stm.close();
            

            DatabaseAccess.commit();
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        } finally {
            DatabaseAccess.setAutoCommit(true);
        }
    }

    /*
     * Zm�n� zbo�� v datab�zi
     */
    void editGoods(Goods oldGoods, Goods newGoods) throws SQLException {
        String command =
                "UPDATE " + GOODS_NAME + " SET goods_id = ?, name = ?, type = ?, DPH = ?, "
                + "unit = ?, EAN = ?, NC = ?, PC_A = ?, PC_B = ?, PC_C = ?, PC_D = ?, quantity = ? "
                + " WHERE goods_id = ?";

        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        pstm.setString(1, newGoods.getGoodsID());
        pstm.setString(2, newGoods.getName());
        pstm.setInt(3, newGoods.getType());
        pstm.setInt(4, newGoods.getDph());
        pstm.setString(5, newGoods.getUnit());
        pstm.setString(6, newGoods.getEan());
        pstm.setDouble(7, (new BigDecimal(newGoods.getNc()).divide(CENT)).doubleValue());
        pstm.setDouble(8, (new BigDecimal(newGoods.getPcA()).divide(CENT)).doubleValue());
        pstm.setDouble(9, (new BigDecimal(newGoods.getPcB()).divide(CENT)).doubleValue());
        pstm.setDouble(10, (new BigDecimal(newGoods.getPcC()).divide(CENT)).doubleValue());
        pstm.setDouble(11, (new BigDecimal(newGoods.getPcD()).divide(CENT)).doubleValue());
        pstm.setDouble(12, newGoods.getQuantity());
        pstm.setString(13, oldGoods.getGoodsID());
        pstm.executeUpdate();
        pstm.close();

    }

    /*
     * Vytvo�� zbo�� v datab�zi
     */
    void createGoods(Goods goods) throws SQLException {
        /* Ulo� zbo�� do datab�ze */
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                "INSERT INTO " + GOODS_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        pstm.setString(1, goods.getGoodsID());
        pstm.setString(2, goods.getName());
        pstm.setInt(3, goods.getType());
        pstm.setInt(4, goods.getDph());
        pstm.setString(5, goods.getUnit());
        pstm.setString(6, goods.getEan());
        pstm.setDouble(7, (new BigDecimal(goods.getNc())).divide(CENT).doubleValue());
        pstm.setDouble(8, (new BigDecimal(goods.getPcA())).divide(CENT).doubleValue());
        pstm.setDouble(9, (new BigDecimal(goods.getPcB())).divide(CENT).doubleValue());
        pstm.setDouble(10, (new BigDecimal(goods.getPcC())).divide(CENT).doubleValue());
        pstm.setDouble(11, (new BigDecimal(goods.getPcD())).divide(CENT).doubleValue());
        pstm.setDouble(12, goods.getQuantity());
        pstm.executeUpdate(); // Prove� operaci
        pstm.close();
    }

    /*
     * Zm�n� cenu zbo��. Provede tak, �e vyhled� v datab�zi zbo�� a aktualizuje ceny
     */
    void editPrice(String goodsId, int nc, int pcA, int pcB, int pcC, int pcD) throws SQLException {
        String command =
                "UPDATE " + GOODS_NAME + " SET NC = ?, PC_A = ?, PC_B = ?, PC_C = ?, PC_D = ?"
                + " WHERE goods_id = ?";

        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        pstm.setDouble(1, (new BigDecimal(nc)).divide(CENT).doubleValue());
        pstm.setDouble(2, (new BigDecimal(pcA)).divide(CENT).doubleValue());
        pstm.setDouble(3, (new BigDecimal(pcB)).divide(CENT).doubleValue());
        pstm.setDouble(4, (new BigDecimal(pcC)).divide(CENT).doubleValue());
        pstm.setDouble(5, (new BigDecimal(pcD)).divide(CENT).doubleValue());
        pstm.setString(6, goodsId);
        pstm.executeUpdate();
        pstm.close();
    }

    void editPriceAndQuantity(String goodsId, int nc, int pcA, int pcB, int pcC, int pcD, double quantity) throws SQLException {
        Goods goods = this.getGoodsByID(goodsId);
        Goods newGoods = new Goods(goods.getGoodsID(), goods.getName(), goods.getType(),
                goods.getDph(), goods.getUnit(), goods.getEan(), nc,
                pcA, pcB, pcC, pcD,
                quantity); // Nastav nov� mno�stv�

        this.editGoods(goods, newGoods); // Zm�� zbo�� 
    }

    /**
     * Zm�n� mno�stv� zbo�� na sklad�
     */
    void editQuantity(String goodsId, double quantity) throws SQLException {
        Goods goods = this.getGoodsByID(goodsId);
        Goods newGoods = new Goods(goods.getGoodsID(), goods.getName(), goods.getType(),
                goods.getDph(), goods.getUnit(), goods.getEan(), goods.getNc(),
                goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(),
                quantity); // Nastav nov� mno�stv�

        this.editGoods(goods, newGoods); // Zm�� zbo��
    }

    /**
     * Nalezne zbo�� v datab�zi podle skladov�ho ��sla
     * Jestli�e zbo�� nenalezne, vrac� pr�zdn� objekt.
     * null, ��sla na z�porn� hodnoty.
     * @return nalezen� zbo�� ve sklad�, nebo pr�zdn� objekt, jestli�e zbo�� nebylo nalezeno.
     * Pr�zdn� objetk je vhodn� rozeznat podle z�porn�ch hodnot parametr�
     * <code>type</code> a <code>EAN</code>
     * @param isLock true, jestli�e m� b�t na�ten� polo�ka uzam�ena ostatn�m u�ivatel�m
     * @param id skladov� ��slo zbo��
     * @throws java.sql.SQLException 
     */
    public Goods getGoodsByID(String id, boolean isLock) throws SQLException {
        String lock = (isLock) ? LOCK_TEXT : " ";
//        String lock = " LOCK IN SHARE MODE ";
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " WHERE goods_id = '" + id + "'" + lock;
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        // je zaru�eno, �e bude v�y jeden, nebo ��dn� v�sledek,
        // nebo� identifika�n� ��slo je jednozna�n�
        if (rs.next() == false) {
            rs.close();
            return new Goods(null, "", -1, -1, "", "", -1, -1, -1, -1, -1, -1); // Vra� pr�zdn� objekt
        }

        Goods goods = new Goods(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getString(5),
                rs.getString(6),
                // ceny na�ti jako string, nebo� double je nep�esn�    
                (new BigDecimal(rs.getString(7))).multiply(CENT).intValue(),
                (new BigDecimal(rs.getString(8))).multiply(CENT).intValue(),
                (new BigDecimal(rs.getString(9))).multiply(CENT).intValue(),
                (new BigDecimal(rs.getString(10))).multiply(CENT).intValue(),
                (new BigDecimal(rs.getString(11))).multiply(CENT).intValue(),
                rs.getDouble(12));

        rs.close();
        stm.close();
        return goods;
    }

    /**
     * Nalezne zbo�� v datab�zi podle skladov�ho ��sla
     * Jestli�e zbo�� nenalezne, vrac� pr�zdn� objekt.
     * null, ��sla na z�porn� hodnoty.
     * 
     * @return nalezen� zbo�� ve sklad�, nebo pr�zdn� objekt, jestli�e zbo�� nebylo nalezeno.
     * Pr�zdn� objetk je vhodn� rozeznat podle z�porn�ch hodnot parametr�
     * <code>type</code> a <code>EAN</code> nebo podle hodnoty null v parametry <code>goodsId</code>
     * @param id skladov� ��slo zbo��
     * @throws java.sql.SQLException 
     */
    public Goods getGoodsByID(String id) throws SQLException {
        return getGoodsByID(id, false);
    }

    /**
     * Vr�t� seznam zbo�� podle zadan�ho jm�na
     * Jestli�e ��dn� zbo�� nenalezne, vrac� pole nulov� d�lky
     * null, ��sla na z�porn� hodnoty.
     * 
     * @return nalezen� zbo��
     * @param name jm�no zbo��
     * @throws java.sql.SQLException 
     */
    public List<Goods> getGoodsByName(String name) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " WHERE name = '" + name + "'";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Vr�t� seznam zbo�� podle zadan�ho jm�na
     * Jestli�e ��dn� zbo�� nenalezne, vrac� pole nulov� d�lky
     * null, ��sla na z�porn� hodnoty.
     * @return nalezen� zbo��
     * @param limit limit plo�ek
     * @param name jm�no zbo��
     * @throws java.sql.SQLException 
     */
    public List<Goods> getGoodsByName(String name, int limit) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " WHERE name = '" + name + "' "
                + "LIMIT " + limit + "";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Na�te ve�ker� zbo�� ze skladu
     * 
     * @return pole s obsahem zbo�� na sklad�.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznam
     * @throws java.sql.SQLException 
     */
    public List<Goods> getAllGoods() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Na�te ve�ker� zbo�� ze skladu
     * @return pole s obsahem zbo�� na sklad�.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznam
     * @param limit limit plo�ek
     * @throws java.sql.SQLException 
     */
    public List<Goods> getAllGoods(int limit) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " ORDER BY name "
                + "LIMIT " + limit + "";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Vyhled� zbo�� ze skladu podle zadan�ho kl��ov�ho slova
     * 
     * @return pole s obsahem zbo�� na sklad�, kter� odpov�d� kl��ov�mu slovu.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznnam
     * @param keyword 
     * @throws java.sql.SQLException 
     */
    public List<Goods> getGoodsByKeyword(String keyword) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + "  "
                + "WHERE goods_id LIKE '%" + keyword + "%' || "
                + " name LIKE '%" + keyword + "%' || "
                + " ean LIKE '%" + keyword + "%' ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Vyhled� zbo�� ze skladu podle zadan�ho kl��ov�ho slova
     * @return pole s obsahem zbo�� na sklad�, kter� odpov�d� kl��ov�mu slovu.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznnam
     * @param keyword kl��ov� slovo
     * @param limit limit plo�ek
     * @throws java.sql.SQLException 
     */
    public List<Goods> getGoodsByKeyword(String keyword, int limit) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + "  "
                + "WHERE goods_id LIKE '%" + keyword + "%' || "
                + " name LIKE '%" + keyword + "%' || "
                + " ean LIKE '%" + keyword + "%' ORDER BY name "
                + "LIMIT " + limit + "";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Na�te ve�ker� zbo�� ze skladu, kter� nem� nulov� stav 
     * 
     * @return pole s obsahem zbo�� na sklad�.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznam
     * @throws java.sql.SQLException 
     */
    public List<Goods> getAllNotZeroGoods() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " WHERE quantity != 0 ORDER BY name "
                + "";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Na�te ve�ker� zbo�� ze skladu, kter� nem� nulov� stav
     * @return pole s obsahem zbo�� na sklad�.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznam
     * @param limit limit plo�ek
     * @throws java.sql.SQLException 
     */
    public List<Goods> getAllNotZeroGoods(int limit) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + GOODS_NAME + " WHERE quantity != 0 ORDER BY name "
                + "LIMIT " + limit;
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Na�te zbo�� ze skladu, kter� nem� nulov� stav a odpov�d� kl��ov�mu slovu
     * @return pole s obsahem zbo�� na sklad�.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznam
     * @param keyword kl��ov� slovo
     * @throws java.sql.SQLException 
     */
    public List<Goods> getAllNotZeroGoodsByKeyword(String keyword) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM ( SELECT * FROM " + GOODS_NAME + " "
                + "WHERE quantity != 0 ) AS sub_goods_tab "
                + "WHERE goods_id LIKE '%" + keyword + "%' || "
                + " name LIKE '%" + keyword + "%' || "
                + " ean LIKE '%" + keyword + "%' "
                + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Na�te zbo�� ze skladu, kter� nem� nulov� stav a odpov�d� kl��ov�mu slovu
     * @return pole s obsahem zbo�� na sklad�.
     * Jestli�e ��dn� zbo�� nenalezl, vrac� pr�zdn� seznam
     * @param limit limit plo�ek
     * @param keyword kl��ov� slovo
     * @throws java.sql.SQLException 
     */
    public List<Goods> getAllNotZeroGoodsByKeyword(String keyword, int limit) throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM ( SELECT * FROM " + GOODS_NAME + " "
                + "WHERE quantity != 0 ) AS sub_goods_tab "
                + "WHERE goods_id LIKE '%" + keyword + "%' || "
                + " name LIKE '%" + keyword + "%' || "
                + " ean LIKE '%" + keyword + "%' "
                + " ORDER BY name "
                + "LIMIT " + limit + "";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        List<Goods> result = getList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Vr�t� seznam zbo�� 
     */
    private List<Goods> getList(ResultSet rs) throws SQLException {

        List<Goods> results = new ArrayList<Goods>();
        while (rs.next()) { // Dopl� do seznamu
            results.add(new Goods(rs.getString("goods_id"), rs.getString("name"), rs.getInt("type"), rs.getInt("dph"),
                    rs.getString("unit"), rs.getString("ean"),
                    // ceny na�ti jako string, nebo� double je nep�esn�    
                    (new BigDecimal(rs.getString("nc"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_a"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_b"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_c"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_d"))).multiply(CENT).intValue(),
                    rs.getDouble("quantity")));
        }

        return results;
    }

    /**
     * Vrac� historii zvolen�ho zbo��
     * @param goodsId 
     * @throws java.sql.SQLException 
     * @return 
     */
    public List<Goods> getHistorizedItems(String goodsId) throws SQLException {
        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(
                "SELECT * FROM goods_history WHERE goods_id = ? ORDER BY name ");
        stm.setString(1, goodsId);
        ResultSet rs = stm.executeQuery();

        List<Goods> result = getHistorizedItemsList(rs);
        rs.close();
        stm.close();
        return result;
    }

    /**
     * Vrac� historii smazan�ho zbo��. Neum� rozeznat, souvislost zbo�� navz�jem
     * @throws java.sql.SQLException 
     * @return 
     */
    public List<Goods> getDeletedHistorizedItems() throws SQLException {
        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(
                "SELECT * FROM goods_history WHERE goods_id IS NULL ORDER BY goods_old_id, name");
        ResultSet rs = stm.executeQuery();

        List<Goods> result = getHistorizedItemsList(rs);
        rs.close();
        stm.close();
        return result;
    }

    private List<Goods> getHistorizedItemsList(ResultSet rs) throws SQLException {
        List<Goods> results = new ArrayList<Goods>();
        while (rs.next()) { // Dopl� do seznamu
            results.add(new Goods(rs.getString("goods_old_id"), rs.getString("name"), rs.getInt("type"), rs.getInt("dph"),
                    rs.getString("unit"), rs.getString("ean"),
                    // ceny na�ti jako string, nebo� double je nep�esn�    
                    (new BigDecimal(rs.getString("nc"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_a"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_b"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_c"))).multiply(CENT).intValue(),
                    (new BigDecimal(rs.getString("pc_d"))).multiply(CENT).intValue(),
                    0));
        }

        return results;
    }

    /**
     * Vrac� historii zbo��
     * @param goodsId
     * @return
     */
    public List<GoodsTradesHistory> getTradesHistory(String goodsId, Date startDate, Date endDate, int limit) throws SQLException {

        // typ v�dejky a p��jemky je rozli�en dodate�nou hodnotou
        // 1 nebo 2 v selectu

        // selektuj z prodejek a p��jemek p�es union
        // INNER JOIN na master tabulku p��jemek/v�dejek, nebo� v�dy mus� b�t n�vaznost
        // LEFT JOIN na odb�ratele, nebo� jako NULL je ozna�en maloobchod
        String sql = "SELECT a.id_sale_listing as id, number, date, quantity, unit, pc as price, dph, c.name, 1 as item_type "
                + "FROM " + SALE_LISTING_TABLE_NAME + " AS a  "
                + "INNER JOIN " + SALE_TABLE_NAME + " AS b ON a.id_sale_listing = b.id_sale_listing  "
                + "LEFT JOIN " + CUSTOMER_TABLE_NAME + " AS c ON a.cust_id = c.cust_id  "
                + "WHERE b.goods_id = ? AND DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) "
                + "union  "
                + "SELECT a.id_buy_listing as id, number, date, quantity, unit, nc as price, dph, c.name, 2 as item_type  "
                + "FROM " + BUY_LISITNG_TABLE_NAME + " AS a  "
                + "INNER JOIN " + BUY_TABLE_NAME + " AS b ON a.id_buy_listing = b.id_buy_listing  "
                + "LEFT JOIN " + SUPLIER_TABLE_NAME + " AS c ON a.sup_id = c.sup_id  "
                + "WHERE b.goods_id = ? AND DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) "
                + "order by date desc, number desc, name "
                + "LIMIT ?";


        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(sql);
        pstm.setString(1, goodsId);
        pstm.setTimestamp(2, new Timestamp(startDate.getTime()));
        pstm.setTimestamp(3, new Timestamp(endDate.getTime()));
        pstm.setString(4, goodsId);
        pstm.setTimestamp(5, new Timestamp(startDate.getTime()));
        pstm.setTimestamp(6, new Timestamp(endDate.getTime()));
        pstm.setInt(7, limit);

        ResultSet rs = pstm.executeQuery();

        List<GoodsTradesHistory> result = resolveTradesHistoryFromRs(rs);

        return result;
    }

    private List<GoodsTradesHistory> resolveTradesHistoryFromRs(ResultSet rs) throws SQLException {
        List<GoodsTradesHistory> result = new ArrayList<GoodsTradesHistory>();

        // napl� z�znamy
        while (rs.next()) {
            GoodsTradesHistory item = new GoodsTradesHistory(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("number"),
                    rs.getDate("date"),
                    rs.getDouble("quantity"),
                    rs.getString("unit"),
                    rs.getBigDecimal("price"),
                    rs.getBigDecimal("dph"));
            item.setItemType(rs.getInt("item_type"));

            result.add(item);
        }

        return result;
    }

    /**
     * Metoda provede �pravu Dph
     * @param oldDph p�vodn� hodnota Dph
     * @param newDph nov� hodnota Dph
     */
    public void updateDphLayer(BigDecimal oldDph, BigDecimal newDph) throws SQLException {

        Connection con = DatabaseAccess.getCurrentConnection();

        try {

            PreparedStatement pstm = con.prepareStatement(""
                    + "update " + DatabaseAccess.GOODS_TABLE_NAME + " "
                    + "set dph = ? where dph = ?");

            pstm.setBigDecimal(1, newDph);
            pstm.setBigDecimal(2, oldDph);

            pstm.executeUpdate();

            pstm.close();

        } finally {
            DatabaseAccess.closeConnection();
        }
    }

    /**
     * This method returns all goods that have not been used 
     * between given dates. It means the goods have not been
     * bought or sold. 
     * @param from from date
     * @param to to date
     * @return goods
     */
    public List<Goods> getUnusedGoods(Date from, Date to) throws SQLException {

        String buySql = "select goods_id from buy "
                + "where id_buy_listing in (select id_buy_listing from buy_listing "
                + "where date > ? and date < ?)";

        String saleSql = "select goods_id from sale "
                + "where id_sale_listing in (select id_sale_listing from sale_listing "
                + "where date > ? and date < ?)";

        String sql = "select * from goods "
                + "where quantity <= 0 "
                + "and goods_id not in (" + buySql + ") "
                + "and goods_id not in (" + saleSql + ")";

        try {
            Connection con = DatabaseAccess.getCurrentConnection();

            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setTimestamp(1, new Timestamp(from.getTime()));
            pstm.setTimestamp(2, new Timestamp(to.getTime()));
            pstm.setTimestamp(3, new Timestamp(from.getTime()));
            pstm.setTimestamp(4, new Timestamp(to.getTime()));

            ResultSet rs = pstm.executeQuery();
            try {
                return getList(rs);
            } finally {
                pstm.close();
            }
        } finally {
            DatabaseAccess.closeConnection();
        }

    }
}
