/*
 * Sale.java
 *
 * Vytvoøeno 29. listopad 2005, 12:09
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
 * Program Control - Skladovı systém
 *
 * Tøída pracuje s vıdejkami v databázi. Umoòuje je naèítat a editovat 
 *
 * @author Kamil Jeek
 *
 * (C) 2005, ver. 1.0
 *
 */

public class Sale {
    private static final String SALE_NAME = DatabaseAccess.SALE_TABLE_NAME; // ulo název databáze
    private static final String SALE_LISTING_NAME = DatabaseAccess.SALE_LISTING_TABLE_NAME; // ulo název databáze
    
    /** Vytvoøí novı objekt Sale */
    Sale() {
    }
    
    /**
     *  Vymae vıdejku z databáze
     */
    void deleteSale(TradeItemPreview tradeItem) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + SALE_LISTING_NAME + " WHERE id_sale_listing = '" + tradeItem.getTradeIdListing() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /**
     * Vrací pøehledy všech vıdejek v databázi
     * @return Všechny pøehledy vıdejek v databázi
     * @throws java.sql.SQLException Vyvolá, jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<TradeItemPreview> getAllSale() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SALE_LISTING_NAME + " ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            // Hodnotu ceny naèti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }
    
    /**
     * Vrací pøehledy všech vıdejek (bez prodejek) v databázi
     * 
     * @return Všechny pøehledy vıdejek v databázi
     * @param start poèáteèní datum
     * @param end koncové datum
     * @param limit max poloek
     * @throws java.sql.SQLException Vyvolá, jestlie dojde pøi práci s databází k chybì
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

        ResultSet rs = stm.executeQuery(); // naèti uivatele z databáze
        
        List<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        while (rs.next()) { //vytvoø seznam pøíjemek
            // Hodnotu ceny naèti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }
    
    /**
     * Vrací pøehledy všech prodejek (vydanıch maloobchodnì) v databázi
     * 
     * @return Všechny pøehledy vıdejek v databázi
     * @param start poèáteèní datum
     * @param end koncové datum
     * @param limit max poloek
     * @throws java.sql.SQLException Vyvolá, jestlie dojde pøi práci s databází k chybì
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

        ResultSet rs = stm.executeQuery(); // naèti uivatele z databáze
        
        List<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        while (rs.next()) { //vytvoø seznam pøíjemek
            // Hodnotu ceny naèti jako String
            buy.add( translateRsToTrItemListing(rs) );
        }

        rs.close();
        stm.close();
        return buy;        
    }    
        
    /**
     * Vrací pøehledy všech vıdejek v databázi odpovídající danému datu.
     * Kontroluje rok, mìsíc a den uvedené u vıdejke (èas se zanedbává) 
     * 
     * @return Všechny vıdejky pøíjemek v databázi
     * @param findCalendar datum, pro které se má vyhledat vıdejku 
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public List<TradeItemPreview> getAllSale(Calendar findCalendar) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "SELECT * FROM " + SALE_LISTING_NAME + " WHERE DATE(date) = ? ORDER BY date DESC");
        
        pstm.setTimestamp(1, new Timestamp(findCalendar.getTimeInMillis()));
        
        ResultSet rs = pstm.executeQuery(SALE_NAME); // naèti uivatele z databáze

        ArrayList<TradeItemPreview> buy = new ArrayList<TradeItemPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            buy.add( translateRsToTrItemListing(rs));
        }

        rs.close();
        pstm.close();
        return buy;        
    }
     
    /**
     * Vrací pøehled vıdejky podle identifikaèního èísla
     * Vrací jednu vıdejku, nebo prázdnı objekt, jestlie ID není v tabulce nalezeno
     * @return Pøehled pøíjemky, která odpovídá zadanému datu a èíslu
     * @param id identifikaèní èíslo, urèující pøíjemku
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public TradeItemPreview getSale(int id) throws SQLException {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SALE_LISTING_NAME + " WHERE id_sale_listing = " + id + " ";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

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
     * Vrací jednotlivé poloky jedné vıdejky
     * @return Jednotlivé poloky vıdejky
     * @param buy Pøíjemka, její obsah se má zjistit
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<TradeItem> getAllSaleItem(TradeItemPreview buy) throws SQLException {
       
        if (buy == null) {
            return new ArrayList<TradeItem>(); // vra prázdné pole
        }
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SALE_NAME + " " +
                "WHERE id_sale_listing = " + buy.getTradeIdListing() + " " +
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
     * Vrací nejvìtší poøadové èíslo vıdejky vybrané z vıdejek daného data 
     * 
     * @return Všechny pøehledy pøíjemek v databázi
     * @param findCalendar datum, pro které se má vyhledat pøíjemka 
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public int getMaxSaleNumber(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber poloky odpovídající datu - nalezne nejvyšší pro jeden den*/
        /*String command = "SELECT MAX(number) FROM " + SALE_LISTING_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "GROUP BY DATE(date) " +
                "ORDER BY date DESC ";*/
        
        /* vyber poloky odpovídající datu - nalezne nejvyšší za celı rok*/
        String command = "SELECT MAX(number) FROM " + SALE_LISTING_NAME + " " +
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
     * P5evede vstupní result set na bean
     * @param rs rs s obsahem tabulky sale_listing
     * @return java bean s hodnotami z øádku tabulky
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
                    "", // není obsaeno v tabulce 
                    false); // není obsaeno v tabulce 
        
    }
    
    /**
     * Pøeve vstupní result set na bean s daty 
     * @param rs result set odpovádající tabulce sale
     * @return java bean s daty z tabulky sale
     * @throws java.sql.SQLException
     */
    private TradeItem translateRsToTrItem(ResultSet rs) throws SQLException {
        return new TradeItem(rs.getInt("id_sale"), rs.getInt("id_sale_listing"), rs.getString("goods_id"), rs.getString("name"), 
                    rs.getInt("dph"),  (new BigDecimal(rs.getString("pc")).multiply(Store.CENT)).intValue(), 
                    rs.getDouble("quantity"), rs.getString("unit"), rs.getInt("use_price"));   
    }    
    
}
