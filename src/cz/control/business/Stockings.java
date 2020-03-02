/*
 * Stockings.java
 *
 * Vytvoøeno 1. bøezen 2006, 0:19
 *
 * Autor: Kamil Jeek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;
import cz.control.data.Stocking;
import cz.control.data.StockingPreview;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;
import java.util.*;
import java.sql.*;       

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladovı systém
 *
 * Tøída pro editaci tabulek inventur. Umoòuje naèítat jednotlivé pøehledy
 * inventur, nebo naráz naèíst všechny pøehledy. Dále umoòuje k jednotlivım 
 * pøehledùm naèíst pøíslušné inventurní poloky 
 *
 * @author Kamil Jeek
 * 
 * (C) 2006, ver. 1.0
 */
public class Stockings {
    
    /**
     * Vytvoøí novou instanci Stockings
     */
    Stockings()  {

    }
    
    
    /**
     *  Vymae z databáze inventuru
     */
    void deleteStocking(StockingPreview stocking) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + STOCKING_LISTING_TABLE_NAME + " WHERE id_stocking_listing = '" + stocking.getStockingIdListing() + "'";
        stm.executeUpdate(command);
        stm.close();
    }   
    
    /**
     *  Nastaví zámek na inventuru
     */
    void changeStockingLock(StockingPreview stocking, boolean lock) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                "UPDATE " + STOCKING_LISTING_TABLE_NAME + " SET is_lock = ? " +
                "WHERE id_stocking_listing = " + stocking.getStockingIdListing() + " ");
        
        pstm.setBoolean(1, lock);
        pstm.executeUpdate();
        pstm.close();
    }
    
    /**
     * Zjištuje, zda je období uzamèeno. V zamèeném období je zakázáno provádìt
     * veškeré obchody
     * 
     * @param date datum, pro které se má zjistit zámek
     * @return true - jestlie je období uzamèeno
     *  false - jestlie je odemèeno
     */
    public boolean isStockingLock(Calendar date) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT DATE(MAX(date)) AS maximum FROM " + STOCKING_LISTING_TABLE_NAME + " " +
                "WHERE " +
                "is_lock = true HAVING maximum IS NOT NULL " +
                "ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // naèti maximální zamèené datum
        
        //zakokrouhlí vstupní datum na hodnotu bez èasu 
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH);
        int year = date.get(Calendar.YEAR);
        Calendar partOfDate = new GregorianCalendar(year, month, day);
        
        // Jestlie nenalezl ádnou inventuru, je odemèeno
        if (!rs.next()) {
            return false;
        }
        
        java.sql.Timestamp tmp = rs.getTimestamp(1);
        
        // pokud nebylo mono naèíst datum, znamená to,
        // e SQL dotaz nalez vısledek
        if (tmp == null) {
            return false;
        }
                
        long time = tmp.getTime();
        
        // porovnej zda je datum zamèení pozdìjší ne zkoumané datum
        return time >= partOfDate.getTimeInMillis();
    }
    
    /**
     * Vrací pøehledy všech inventur v databázi
     * @return Všechny pøehledy inventur v databázi
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<StockingPreview> getAllStocking() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<StockingPreview> stocking = new ArrayList<StockingPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            long dateMillis =  rs.getTimestamp(3).getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);

            stocking.add(new StockingPreview(rs.getInt(1), rs.getInt(2), calendar, 
                    (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue(), 
                    rs.getString(5), rs.getInt(6), rs.getString(7), rs.getBoolean(8),
                    rs.getInt(9)) );
        }

        rs.close();
        stm.close();
        return stocking;        
    }    
    
    /**
     * Vrací pøehledy všech inventur v databázi
     * 
     * @return Všechny pøehledy inventur v databázi
     * @param start poèáteèní datum
     * @param end koneèné datum
     * @param limit limit poloek 
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<StockingPreview> getAllStocking(java.util.Date start, java.util.Date end, int limit) throws SQLException {
        String command = 
                "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " " +
                "WHERE DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) " +
                "ORDER BY DATE(date) DESC, number DESC " +
                "LIMIT ? ";

        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        
        stm.setTimestamp(1, new Timestamp(start.getTime()) );
        stm.setTimestamp(2, new Timestamp(end.getTime()));
        stm.setInt(3, limit);

        ResultSet rs = stm.executeQuery(); // naèti uivatele z databáze
        
        ArrayList<StockingPreview> stocking = new ArrayList<StockingPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            long dateMillis =  rs.getTimestamp(3).getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);

            stocking.add(new StockingPreview(rs.getInt(1), rs.getInt(2), calendar, 
                    (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue(), 
                    rs.getString(5), rs.getInt(6), rs.getString(7), rs.getBoolean(8),
                    rs.getInt(9)) );
        }

        rs.close();
        stm.close();
        return stocking;        
    }        
    
    /**
     * Vrací pøehledy všech inventur v databázi odpovídající danému datu.
     * Kontroluje rok, mìsíc a den uvedené u inventury 
     * 
     * @return Všechny pøehledy inventur v databázi
     * @param findCalendar datum, pro které se má vyhledat pøíjemka 
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<StockingPreview> getAllStocking(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber poloky odpovídající datu */
        String command = "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "ORDER BY date DESC ";
        
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<StockingPreview> stocking = new ArrayList<StockingPreview>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            long dateMillis =  rs.getTimestamp(3).getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);
            
            stocking.add(new StockingPreview(rs.getInt(1), rs.getInt(2), calendar, 
                    (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue(), 
                    rs.getString(5), rs.getInt(6), rs.getString(7), rs.getBoolean(8),
                    rs.getInt(9)) );
        }

        rs.close();
        stm.close();
        return stocking;        
    }
    
    /**
     * Vrací nejvìtší poøadové èíslo inventury vybrané z inventur daného data 
     * 
     * @return Všechny pøehledy inventur v databázi
     * @param findCalendar datum, pro které se má vyhledat inventura
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public int getMaxStockingNumber(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber poloky odpovídající datu */
        //String command = "SELECT MAX(number) FROM " + STOCKING_LISTING_TABLE_NAME + " " +
        //        "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
        //        "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
        //        "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
        //        "GROUP BY DATE(date) " +
        //        "ORDER BY date DESC ";
        
        /* vyber poloky odpovídající datu - nalezne max èíslo za celı rok */
        String command = "SELECT MAX(number) FROM " + STOCKING_LISTING_TABLE_NAME + " " +
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
     * Vrací pøehled inventur podle identifikaèního èísla
     * Vrací jednu inventuru, nebo prázdnı objekt, jestlie ID není v tabulce nalezeno
     * @return Pøehled inventury, která odpovídá zadanému datu a èíslu
     * @param id identifikaèní èíslo, urèující inventuru
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public StockingPreview getStocking(int id) throws SQLException {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " WHERE id_stocking_listing = " + id + " ";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        if  (!rs.next()) { 
            rs.close();
            return new StockingPreview();
        }

        /* Je zaruèeno, e bude vdy jeden, nebo ádnı vısledek, nebo ID je jednoznaèné */
        java.util.Date date =  rs.getDate(3);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTime());
        // Hodnotu ceny naèti jako String
        StockingPreview result = new StockingPreview(rs.getInt(1), rs.getInt(2), calendar, 
                    (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue(), 
                    rs.getString(5), rs.getInt(6), rs.getString(7), rs.getBoolean(8),
                    rs.getInt(9));


        rs.close();
        stm.close();
        return result;        
    }    
    
    /**
     * Vrací jednotlivé poloky jedné inventury
     * @return Jednotlivé poloky inventury
     * @param buy Inventura, její obsah se má zjistit
     * @throws java.sql.SQLException Vyvolám. jestlie dojde pøi práci s databází k chybì
     */
    public ArrayList<Stocking> getAllStockingItems(StockingPreview stocking) throws SQLException {
       
        if (stocking == null) {
            return new ArrayList<Stocking>(); // vra prázdné pole
        }
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + STOCKING_TABLE_NAME + " " +
                "WHERE id_stocking_listing = " + stocking.getStockingIdListing() + " " +
                "ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze

        ArrayList<Stocking> result = new ArrayList<Stocking>();
        
        while (rs.next()) { //vytvoø seznam pøíjemek
            // Hodnotu ceny naèti jako String
            result.add( new Stocking(rs.getInt(1), rs.getInt(2), rs.getString(3),
                    rs.getString(4), rs.getInt(5), 
                    ( new BigDecimal(rs.getInt(6))).multiply(Store.CENT).intValue(), 
                    rs.getDouble(7), rs.getString(8)) );
        }

        rs.close();
        stm.close();
        return result;        
    }    

}
