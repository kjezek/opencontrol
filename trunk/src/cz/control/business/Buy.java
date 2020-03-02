/*
 * Buy.java
 *
 * Vytvoøeno 1. listopad 2005, 9:53
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
 * Program Control - Skladovı systém
 *
 * Tøída naèítá a umoòuje editvoat pøíjemky v databázi
 *
 * @author Kamil Jeek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil Jeek
 */
public class Buy {
    private static final String BUY_NAME = DatabaseAccess.BUY_TABLE_NAME; // ulo název databáze
    private static final String BUY_LISTING_NAME = DatabaseAccess.BUY_LISITNG_TABLE_NAME; // ulo název databáze
        
    
    /** Vytvoøí novı objekt Buy */
    Buy() {
    }
    
    /**
     *  Vymae z databáze pøíjemku 
     */
    void deleteBuy(TradeItemPreview tradeItem) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + BUY_LISTING_NAME + " WHERE id_buy_listing = '" + tradeItem.getTradeIdListing() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /**
     * Vrací pøehledy všech pøíjemek v databázi
     * @return Všechny pøehledy pøíjemek v databázi
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<TradeItemPreview> getAllBuy() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + BUY_LISTING_NAME + " ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            long dateMillis =  rs.getTimestamp("date").getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);

            // Hodnotu ceny naèti jako String
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
     * Vrací pøehledy všech pøíjemek v databázi
     * 
     * @return Všechny pøehledy pøíjemek v databázi
     * @param start poèáteèní datum
     * @param end koncové datum
     * @param limit max poloek
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
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

        ResultSet rs = stm.executeQuery(); // naèti uivatele z databáze
        
        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            long dateMillis =  rs.getTimestamp(3).getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);

            // Hodnotu ceny naèti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }    

    /**
     * Vrací pøehledy všech pøíjemek v databázi odpovídající danému datu.
     * Kontroluje rok, mìsíc a den uvedené u pøíjemky 
     * 
     * @return Všechny pøehledy pøíjemek v databázi
     * @param findCalendar datum, pro které se má vyhledat pøíjemka 
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<TradeItemPreview> getAllBuy(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber poloky odpovídající datu */
        String command = "SELECT * FROM " + BUY_LISTING_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "ORDER BY date DESC ";
        
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            
            /* Nahrazeno SQL dotazem */
            /* Zkontroluj, zda odpovídá datum */
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
     * Vrací nejvìtší poøadové èíslo pøíjemky vybrané z pøíjemek daného data 
     * 
     * @return Všechny pøehledy pøíjemek v databázi
     * @param findCalendar datum, pro které se má vyhledat pøíjemka 
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public int getMaxBuyNumber(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber poloky odpovídající datu - nalezne max èíslo pro jeden den*/
        /*String command = "SELECT MAX(number) FROM " + BUY_LISTING_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "GROUP BY DATE(date) " +
                "ORDER BY date DESC ";*/
        
        /* vyber poloky odpovídající datu - nalezne max èíslo za celı rok */
        String command = "SELECT MAX(number) FROM " + BUY_LISTING_NAME + " " +
                "WHERE " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "GROUP BY YEAR(date) " +
                "ORDER BY date DESC ";
        
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        int result = 0;
        
        if (rs.next()) {
            result = rs.getInt(1);
        }
        
        rs.close();
        stm.close();
        return result;
    }    
    
    /**
     * Vrací pøehled pøíjemky podle identifikaèního èísla
     * Vrací jednu pøíjemku, nebo prázdnı objekt, jestlie ID není v tabulce nalezeno
     * @return Pøehled pøíjemky, která odpovídá zadanému datu a èíslu
     * @param id identifikaèní èíslo, urèující pøíjemku
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public TradeItemPreview getBuy(int id) throws SQLException {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + BUY_LISTING_NAME + " WHERE id_buy_listing = " + id + " ";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        if  (rs.next() == false) { 
            rs.close();
            return new TradeItemPreview();
        }

        /* Je zaruèeno, e bude vdy jeden, nebo ádnı vısledek, nebo ID je jednoznaèné */
        TradeItemPreview result = translateRsToTrItemListing(rs);

        rs.close();
        stm.close();
        return result;        
    }
    
    /**
     * Vrací jednotlivé poloky jedné pøíjemky
     * @return Jednotlivé poloky pøíjemky
     * @param buy Pøíjemka, její obsah se má zjistit
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<TradeItem> getAllBuyItem(TradeItemPreview buy) throws SQLException {
       
        if (buy == null) {
            return new ArrayList<TradeItem>(); // vra prázdné pole
        }
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + BUY_NAME + " " +
                "WHERE id_buy_listing = " + buy.getTradeIdListing() + " " +
                "ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<TradeItem> buyItems = new ArrayList<TradeItem>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            // Hodnotu ceny naèti jako String
            buyItems.add( translateRsToTrItem(rs) );
        }

        rs.close();
        stm.close();
        return buyItems;        
    }
    
    /**
     * P5evede vstupní result set na bean
     * @param rs rs s obsahem tabulky buy_listing
     * @return java bean s hodnotami z øádku tabulky
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
     * Pøeve vstupní result set na bean s daty 
     * @param rs result set odpovádající tabulce buy
     * @return java bean s daty z tabulky buy
     * @throws java.sql.SQLException
     */
    private TradeItem translateRsToTrItem(ResultSet rs) throws SQLException {
        return new TradeItem(rs.getInt("id_buy"), rs.getInt("id_buy_listing"), rs.getString("goods_id"), rs.getString("name"), 
                    rs.getInt("dph"),  (new BigDecimal(rs.getString("nc")).multiply(Store.CENT)).intValue(), 
                    rs.getDouble("quantity"), rs.getString("unit"), 0);   
    }
    
}
