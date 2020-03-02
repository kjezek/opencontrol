/*
 * Stockings.java
 *
 * Vytvo�eno 1. b�ezen 2006, 0:19
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da pro editaci tabulek inventur. Umo��uje na��tat jednotliv� p�ehledy
 * inventur, nebo nar�z na��st v�echny p�ehledy. D�le umo��uje k jednotliv�m 
 * p�ehled�m na��st p��slu�n� inventurn� polo�ky 
 *
 * @author Kamil Je�ek
 * 
 * (C) 2006, ver. 1.0
 */
public class Stockings {
    
    /**
     * Vytvo�� novou instanci Stockings
     */
    Stockings()  {

    }
    
    
    /**
     *  Vyma�e z datab�ze inventuru
     */
    void deleteStocking(StockingPreview stocking) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + STOCKING_LISTING_TABLE_NAME + " WHERE id_stocking_listing = '" + stocking.getStockingIdListing() + "'";
        stm.executeUpdate(command);
        stm.close();
    }   
    
    /**
     *  Nastav� z�mek na inventuru
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
     * Zji�tuje, zda je obdob� uzam�eno. V zam�en�m obdob� je zak�z�no prov�d�t
     * ve�ker� obchody
     * 
     * @param date datum, pro kter� se m� zjistit z�mek
     * @return true - jestli�e je obdob� uzam�eno
     *  false - jestli�e je odem�eno
     */
    public boolean isStockingLock(Calendar date) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT DATE(MAX(date)) AS maximum FROM " + STOCKING_LISTING_TABLE_NAME + " " +
                "WHERE " +
                "is_lock = true HAVING maximum IS NOT NULL " +
                "ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // na�ti maxim�ln� zam�en� datum
        
        //zakokrouhl� vstupn� datum na hodnotu bez �asu 
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH);
        int year = date.get(Calendar.YEAR);
        Calendar partOfDate = new GregorianCalendar(year, month, day);
        
        // Jestli�e nenalezl ��dnou inventuru, je odem�eno
        if (!rs.next()) {
            return false;
        }
        
        java.sql.Timestamp tmp = rs.getTimestamp(1);
        
        // pokud nebylo mo�no na��st datum, znamen� to,
        // �e SQL dotaz nalez v�sledek
        if (tmp == null) {
            return false;
        }
                
        long time = tmp.getTime();
        
        // porovnej zda je datum zam�en� pozd�j�� ne� zkouman� datum
        return time >= partOfDate.getTimeInMillis();
    }
    
    /**
     * Vrac� p�ehledy v�ech inventur v datab�zi
     * @return V�echny p�ehledy inventur v datab�zi
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<StockingPreview> getAllStocking() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " ORDER BY DATE(date) DESC, number DESC";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<StockingPreview> stocking = new ArrayList<StockingPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
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
     * Vrac� p�ehledy v�ech inventur v datab�zi
     * 
     * @return V�echny p�ehledy inventur v datab�zi
     * @param start po��te�n� datum
     * @param end kone�n� datum
     * @param limit limit polo�ek 
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
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

        ResultSet rs = stm.executeQuery(); // na�ti u�ivatele z datab�ze
        
        ArrayList<StockingPreview> stocking = new ArrayList<StockingPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
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
     * Vrac� p�ehledy v�ech inventur v datab�zi odpov�daj�c� dan�mu datu.
     * Kontroluje rok, m�s�c a den uveden� u inventury 
     * 
     * @return V�echny p�ehledy inventur v datab�zi
     * @param findCalendar datum, pro kter� se m� vyhledat p��jemka 
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<StockingPreview> getAllStocking(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber polo�ky odpov�daj�c� datu */
        String command = "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " " +
                "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
                "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
                "ORDER BY date DESC ";
        
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<StockingPreview> stocking = new ArrayList<StockingPreview>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
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
     * Vrac� nejv�t�� po�adov� ��slo inventury vybran� z inventur dan�ho data 
     * 
     * @return V�echny p�ehledy inventur v datab�zi
     * @param findCalendar datum, pro kter� se m� vyhledat inventura
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getMaxStockingNumber(Calendar findCalendar) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        
        /* vyber polo�ky odpov�daj�c� datu */
        //String command = "SELECT MAX(number) FROM " + STOCKING_LISTING_TABLE_NAME + " " +
        //        "WHERE DAY(date) = " + findCalendar.get(Calendar.DAY_OF_MONTH) + " AND " +
        //        "MONTH(date) =  " + (findCalendar.get(Calendar.MONTH)+1) + " AND " +
        //        "YEAR(date) = " + findCalendar.get(Calendar.YEAR) + " " +
        //        "GROUP BY DATE(date) " +
        //        "ORDER BY date DESC ";
        
        /* vyber polo�ky odpov�daj�c� datu - nalezne max ��slo za cel� rok */
        String command = "SELECT MAX(number) FROM " + STOCKING_LISTING_TABLE_NAME + " " +
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
     * Vrac� p�ehled inventur podle identifika�n�ho ��sla
     * Vrac� jednu inventuru, nebo pr�zdn� objekt, jestli�e ID nen� v tabulce nalezeno
     * @return P�ehled inventury, kter� odpov�d� zadan�mu datu a ��slu
     * @param id identifika�n� ��slo, ur�uj�c� inventuru
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public StockingPreview getStocking(int id) throws SQLException {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + STOCKING_LISTING_TABLE_NAME + " WHERE id_stocking_listing = " + id + " ";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        if  (!rs.next()) { 
            rs.close();
            return new StockingPreview();
        }

        /* Je zaru�eno, �e bude v��dy jeden, nebo ��dn� v�sledek, nebo� ID je jednozna�n� */
        java.util.Date date =  rs.getDate(3);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTime());
        // Hodnotu ceny na�ti jako String
        StockingPreview result = new StockingPreview(rs.getInt(1), rs.getInt(2), calendar, 
                    (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue(), 
                    rs.getString(5), rs.getInt(6), rs.getString(7), rs.getBoolean(8),
                    rs.getInt(9));


        rs.close();
        stm.close();
        return result;        
    }    
    
    /**
     * Vrac� jednotliv� polo�ky jedn� inventury
     * @return Jednotliv� polo�ky inventury
     * @param buy Inventura, jej� obsah se m� zjistit
     * @throws java.sql.SQLException Vyvol�m. jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public ArrayList<Stocking> getAllStockingItems(StockingPreview stocking) throws SQLException {
       
        if (stocking == null) {
            return new ArrayList<Stocking>(); // vra� pr�zdn� pole
        }
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + STOCKING_TABLE_NAME + " " +
                "WHERE id_stocking_listing = " + stocking.getStockingIdListing() + " " +
                "ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze

        ArrayList<Stocking> result = new ArrayList<Stocking>();
        
        while (rs.next()) { //vytvo� seznam p��jemek
            // Hodnotu ceny na�ti jako String
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
