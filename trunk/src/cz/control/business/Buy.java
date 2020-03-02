/*
 * Buy.java
 *
 * Vytvo�eno 1. listopad 2005, 9:53
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

/**
 * Program Control - Skladov� syst�m
 *
 * T��da na��t� a umo��uje editvoat p��jemky v datab�zi
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil Je�ek
 */
public class Buy {
    private static final String BUY_NAME = DatabaseAccess.BUY_TABLE_NAME; // ulo� n�zev datab�ze
    private static final String BUY_LISTING_NAME = DatabaseAccess.BUY_LISITNG_TABLE_NAME; // ulo� n�zev datab�ze
        
    
    /** Vytvo�� nov� objekt Buy */
    Buy() {
    }
    
    /**
     *  Vyma�e z datab�ze p��jemku 
     */
    void deleteBuy(TradeItemPreview tradeItem) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + BUY_LISTING_NAME + " WHERE id_buy_listing = '" + tradeItem.getTradeIdListing() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /**
     * Vrac� p�ehledy v�ech p��jemek v datab�zi
     * @return V�echny p�ehledy p��jemek v datab�zi
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<TradeItemPreview> getAllBuy() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + BUY_LISTING_NAME + " ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            long dateMillis =  rs.getTimestamp("date").getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);

            // Hodnotu ceny na�ti jako String
            buy.add( new TradeItemPreview(
                    rs.getInt("id_buy_listing"), 
                    rs.getInt("number"), 
                    calendar, 
                    rs.getInt("sup_id"), 
                    (new BigDecimal(rs.getString("total_nc_dph")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("total_dph")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("total_nc")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("reduction")).multiply(Store.CENT)).longValue(), 
                    rs.getString("author"), 
                    rs.getInt("user_id"),
                    rs.getString("bill_number"),
                    rs.getBoolean("is_cash")) );
        }

        rs.close();
        stm.close();
        return buy;        
    }
    
    /**
     * Vrac� p�ehledy v�ech p��jemek v datab�zi
     * 
     * @return V�echny p�ehledy p��jemek v datab�zi
     * @param start po��te�n� datum
     * @param end koncov� datum
     * @param limit max polo�ek
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<TradeItemPreview> getAllBuy(java.util.Date start, java.util.Date end, int limit) throws SQLException {
        String command = 
                "SELECT * FROM " + BUY_LISTING_NAME + " " +
                "WHERE DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) " +
                "ORDER BY DATE(date) DESC, number DESC " +
                "LIMIT ? ";

        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        
        stm.setTimestamp(1, new Timestamp(start.getTime()) );
        stm.setTimestamp(2, new Timestamp(end.getTime()));
        stm.setInt(3, limit);

        ResultSet rs = stm.executeQuery(); // na�ti u�ivatele z datab�ze
        
        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            long dateMillis =  rs.getTimestamp(3).getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);

            // Hodnotu ceny na�ti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }    

    /**
     * Vrac� p�ehledy v�ech p��jemek v datab�zi odpov�daj�c� dan�mu datu.
     * Kontroluje rok, m�s�c a den uveden� u p��jemky 
     * 
     * @return V�echny p�ehledy p��jemek v datab�zi
     * @param findCalendar datum, pro kter� se m� vyhledat p��jemka 
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<TradeItemPreview> getAllBuy(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber polo�ky odpov�daj�c� datu */
        String command = "SELECT * FROM " + BUY_LISTING_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "ORDER BY date DESC ";
        
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            
            /* Nahrazeno SQL dotazem */
            /* Zkontroluj, zda odpov�d� datum */
//            if (findCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
//                findCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
//                findCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                
            buy.add( translateRsToTrItemListing(rs) );
//            }
        }

        rs.close();
        stm.close();
        return buy;        
    }
    
    /**
     * Vrac� nejv�t�� po�adov� ��slo p��jemky vybran� z p��jemek dan�ho data 
     * 
     * @return V�echny p�ehledy p��jemek v datab�zi
     * @param findCalendar datum, pro kter� se m� vyhledat p��jemka 
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getMaxBuyNumber(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber polo�ky odpov�daj�c� datu - nalezne max ��slo pro jeden den*/
        /*String command = "SELECT MAX(number) FROM " + BUY_LISTING_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "GROUP BY DATE(date) " +
                "ORDER BY date DESC ";*/
        
        /* vyber polo�ky odpov�daj�c� datu - nalezne max ��slo za cel� rok */
        String command = "SELECT MAX(number) FROM " + BUY_LISTING_NAME + " " +
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
     * Vrac� p�ehled p��jemky podle identifika�n�ho ��sla
     * Vrac� jednu p��jemku, nebo pr�zdn� objekt, jestli�e ID nen� v tabulce nalezeno
     * @return P�ehled p��jemky, kter� odpov�d� zadan�mu datu a ��slu
     * @param id identifika�n� ��slo, ur�uj�c� p��jemku
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public TradeItemPreview getBuy(int id) throws SQLException {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + BUY_LISTING_NAME + " WHERE id_buy_listing = " + id + " ";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        if  (rs.next() == false) { 
            rs.close();
            return new TradeItemPreview();
        }

        /* Je zaru�eno, �e bude v��dy jeden, nebo ��dn� v�sledek, nebo� ID je jednozna�n� */
        TradeItemPreview result = translateRsToTrItemListing(rs);

        rs.close();
        stm.close();
        return result;        
    }
    
    /**
     * Vrac� jednotliv� polo�ky jedn� p��jemky
     * @return Jednotliv� polo�ky p��jemky
     * @param buy P��jemka, jej� obsah se m� zjistit
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<TradeItem> getAllBuyItem(TradeItemPreview buy) throws SQLException {
       
        if (buy == null) {
            return new ArrayList<TradeItem>(); // vra� pr�zdn� pole
        }
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + BUY_NAME + " " +
                "WHERE id_buy_listing = " + buy.getTradeIdListing() + " " +
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
     * P5evede vstupn� result set na bean
     * @param rs rs s obsahem tabulky buy_listing
     * @return java bean s hodnotami z ��dku tabulky
     * @throws java.sql.SQLException
     */
    private TradeItemPreview translateRsToTrItemListing(ResultSet rs) throws SQLException {
            long dateMillis =  rs.getTimestamp("date").getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);
            
            return new TradeItemPreview(
                    rs.getInt("id_buy_listing"), 
                    rs.getInt("number"), 
                    calendar, 
                    rs.getInt("sup_id"), 
                    (new BigDecimal(rs.getString("total_nc_dph")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("total_dph")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("total_nc")).multiply(Store.CENT)).longValue(), 
                    (new BigDecimal(rs.getString("reduction")).multiply(Store.CENT)).longValue(), 
                    rs.getString("author"), 
                    rs.getInt("user_id"),
                    rs.getString("bill_number"),
                    rs.getBoolean("is_cash"));
        
    }
    
    /**
     * P�eve vstupn� result set na bean s daty 
     * @param rs result set odpov�daj�c� tabulce buy
     * @return java bean s daty z tabulky buy
     * @throws java.sql.SQLException
     */
    private TradeItem translateRsToTrItem(ResultSet rs) throws SQLException {
        return new TradeItem(rs.getInt("id_buy"), rs.getInt("id_buy_listing"), rs.getString("goods_id"), rs.getString("name"), 
                    rs.getInt("dph"),  (new BigDecimal(rs.getString("nc")).multiply(Store.CENT)).intValue(), 
                    rs.getDouble("quantity"), rs.getString("unit"), 0);   
    }
    
}
