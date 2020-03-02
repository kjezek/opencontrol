/*
 * Sale.java
 *
 * Vytvo�eno 29. listopad 2005, 12:09
 *
 
 */

package cz.control.business;
import cz.control.data.*;
import cz.control.business.*;
import cz.control.data.TradeItem;
import cz.control.data.TradeItemPreview;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;

import java.sql.*;
import java.util.*;

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladov� syst�m
 *
 * T��da pracuje s v�dejkami v datab�zi. Umo��uje je na��tat a editovat 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 */

public class Sale {
    private static final String SALE_NAME = DatabaseAccess.SALE_TABLE_NAME; // ulo� n�zev datab�ze
    private static final String SALE_LISTING_NAME = DatabaseAccess.SALE_LISTING_TABLE_NAME; // ulo� n�zev datab�ze
    
    /** Vytvo�� nov� objekt Sale */
    Sale() {
    }
    
    /**
     *  Vyma�e v�dejku z datab�ze
     */
    void deleteSale(TradeItemPreview tradeItem) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + SALE_LISTING_NAME + " WHERE id_sale_listing = '" + tradeItem.getTradeIdListing() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /**
     * Vrac� p�ehledy v�ech v�dejek v datab�zi
     * @return V�echny p�ehledy v�dejek v datab�zi
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<TradeItemPreview> getAllSale() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SALE_LISTING_NAME + " ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            // Hodnotu ceny na�ti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }
    
    /**
     * Vrac� p�ehledy v�ech v�dejek (bez prodejek) v datab�zi
     * 
     * @return V�echny p�ehledy v�dejek v datab�zi
     * @param start po��te�n� datum
     * @param end koncov� datum
     * @param limit max polo�ek
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public List<TradeItemPreview> getAllSaleOnly(java.util.Date start, java.util.Date end, int limit) throws SQLException {
        String command = 
                "SELECT * FROM " + SALE_LISTING_NAME + " " +
                "WHERE DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) " +
                "AND cust_id IS NOT NULL " +
                "ORDER BY DATE(date) DESC, number DESC " +
                "LIMIT ? ";

        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        
        stm.setTimestamp(1, new Timestamp(start.getTime()) );
        stm.setTimestamp(2, new Timestamp(end.getTime()));
        stm.setInt(3, limit);

        ResultSet rs = stm.executeQuery(); // na�ti u�ivatele z datab�ze
        
        List<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        while (rs.next()) { //vytvo� seznam p��jemek
            // Hodnotu ceny na�ti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }
    
    /**
     * Vrac� p�ehledy v�ech prodejek (vydan�ch maloobchodn�) v datab�zi
     * 
     * @return V�echny p�ehledy v�dejek v datab�zi
     * @param start po��te�n� datum
     * @param end koncov� datum
     * @param limit max polo�ek
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public List<TradeItemPreview> getAllDiscountOnly(java.util.Date start, java.util.Date end, int limit) throws SQLException {
        String command = 
                "SELECT * FROM " + SALE_LISTING_NAME + " " +
                "WHERE DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) " +
                "AND cust_id IS NULL " +
                "ORDER BY DATE(date) DESC, number DESC " +
                "LIMIT ? ";

        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        
        stm.setTimestamp(1, new Timestamp(start.getTime()) );
        stm.setTimestamp(2, new Timestamp(end.getTime()));
        stm.setInt(3, limit);

        ResultSet rs = stm.executeQuery(); // na�ti u�ivatele z datab�ze
        
        List<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        while (rs.next()) { //vytvo� seznam p��jemek
            // Hodnotu ceny na�ti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }    
        
    /**
     * Vrac� p�ehledy v�ech v�dejek v datab�zi odpov�daj�c� dan�mu datu.
     * Kontroluje rok, m�s�c a den uveden� u v�dejke (�as se zanedb�v�) 
     * 
     * @return V�echny v�dejky p��jemek v datab�zi
     * @param findCalendar datum, pro kter� se m� vyhledat v�dejku 
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public List<TradeItemPreview> getAllSale(Calendar findCalendar) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "SELECT * FROM " + SALE_LISTING_NAME + " WHERE DATE(date) = ? ORDER BY date DESC");
        
        pstm.setTimestamp(1, new Timestamp(findCalendar.getTimeInMillis()));
        
        ResultSet rs = pstm.executeQuery(SALE_NAME); // na�ti u�ivatele z datab�ze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            buy.add( translateRsToTrItemListing(rs));
        }

        rs.close();
        pstm.close();
        return buy;        
    }
     
    /**
     * Vrac� p�ehled v�dejky podle identifika�n�ho ��sla
     * Vrac� jednu v�dejku, nebo pr�zdn� objekt, jestli�e ID nen� v tabulce nalezeno
     * @return P�ehled p��jemky, kter� odpov�d� zadan�mu datu a ��slu
     * @param id identifika�n� ��slo, ur�uj�c� p��jemku
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public TradeItemPreview getSale(int id) throws SQLException {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SALE_LISTING_NAME + " WHERE id_sale_listing = " + id + " ";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        if  (rs.next() == false) { 
            rs.close();
            return new TradeItemPreview();
        }

        TradeItemPreview result = translateRsToTrItemListing(rs);
        
        rs.close();
        stm.close();
        return result;        
    }
    
    
    /**
     * Vrac� jednotliv� polo�ky jedn� v�dejky
     * @return Jednotliv� polo�ky v�dejky
     * @param buy P��jemka, jej� obsah se m� zjistit
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<TradeItem> getAllSaleItem(TradeItemPreview buy) throws SQLException {
       
        if (buy == null) {
            return new ArrayList<TradeItem>(); // vra� pr�zdn� pole
        }
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SALE_NAME + " " +
                "WHERE id_sale_listing = " + buy.getTradeIdListing() + " " +
                "ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<TradeItem> buyItems = new ArrayList<TradeItem>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            // Hodnotu ceny na�ti jako String
            buyItems.add( translateRsToTrItem(rs) );
        }

        rs.close();
        stm.close();
        return buyItems;        
    }
     
    /**
     * Vrac� nejv�t�� po�adov� ��slo v�dejky vybran� z v�dejek dan�ho data 
     * 
     * @return V�echny p�ehledy p��jemek v datab�zi
     * @param findCalendar datum, pro kter� se m� vyhledat p��jemka 
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getMaxSaleNumber(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber polo�ky odpov�daj�c� datu - nalezne nejvy��� pro jeden den*/
        /*String command = "SELECT MAX(number) FROM " + SALE_LISTING_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "GROUP BY DATE(date) " +
                "ORDER BY date DESC ";*/
        
        /* vyber polo�ky odpov�daj�c� datu - nalezne nejvy��� za cel� rok*/
        String command = "SELECT MAX(number) FROM " + SALE_LISTING_NAME + " " +
                "WHERE " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "GROUP BY YEAR(date) " +
                "ORDER BY date DESC ";
        
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        int result = 0;
        
        if (rs.next()) {
            result = rs.getInt(1);
        }
        
        rs.close();
        stm.close();
        return result;
    }  
    
    /**
     * P5evede vstupn� result set na bean
     * @param rs rs s obsahem tabulky sale_listing
     * @return java bean s hodnotami z ��dku tabulky
     * @throws java.sql.SQLException
     */
    private TradeItemPreview translateRsToTrItemListing(ResultSet rs) throws SQLException {
            long dateMillis =  rs.getTimestamp("date").getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);
            
            return new TradeItemPreview(
                    rs.getInt("id_sale_listing"), 
                    rs.getInt("number"), 
                    calendar, 
                    rs.getInt("cust_id"), 
                    (new BigDecimal(rs.getString("total_pc_dph")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("total_dph")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("total_pc")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("reduction")).multiply(Store.CENT)).longValue(), 
                    rs.getString("author"), 
                    rs.getInt("user_id"),
                    "", // nen� obsa�eno v tabulce 
                    false); // nen� obsa�eno v tabulce 
        
    }
    
    /**
     * P�eve vstupn� result set na bean s daty 
     * @param rs result set odpov�daj�c� tabulce sale
     * @return java bean s daty z tabulky sale
     * @throws java.sql.SQLException
     */
    private TradeItem translateRsToTrItem(ResultSet rs) throws SQLException {
        return new TradeItem(rs.getInt("id_sale"), rs.getInt("id_sale_listing"), rs.getString("goods_id"), rs.getString("name"), 
                    rs.getInt("dph"),  (new BigDecimal(rs.getString("pc")).multiply(Store.CENT)).intValue(), 
                    rs.getDouble("quantity"), rs.getString("unit"), rs.getInt("use_price"));   
    }    
    
}
