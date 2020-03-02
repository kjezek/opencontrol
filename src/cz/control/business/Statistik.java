/*
 * Statistik.java
 *
 * Vytvoøeno 17. prosinec 2005, 12:21
 *
 
 */

package cz.control.business;

import cz.control.data.ChartItem;
import cz.control.data.Customer;
import cz.control.data.Goods;
import cz.control.data.Suplier;
import cz.control.database.DatabaseAccess;
import java.math.*;
import java.util.*;
import java.sql.*;       

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladový systém
 *
 * Tøída pro výpoèet statistických údajù. Naèítá z databáze údaje o celkových cenách,
 * o maximálních, minimálních a prùmìrných cenách.
 * Dále pak o množství zboží na skladì.
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public class Statistik {
    private static final String GOODS_NAME = DatabaseAccess.GOODS_TABLE_NAME; 

    private static final String BUY_NAME = DatabaseAccess.BUY_TABLE_NAME; 
    private static final String BUY_LISTING_NAME = DatabaseAccess.BUY_LISITNG_TABLE_NAME; 
    private static final String SUPLIER_NAME = DatabaseAccess.SUPLIER_TABLE_NAME; 
    
    private static final String SALE_NAME = DatabaseAccess.SALE_TABLE_NAME; // ulož název databáze
    private static final String SALE_LISTING = DatabaseAccess.SALE_LISTING_TABLE_NAME; // ulož název databáze
    private static final String CUSTOMER_NAME = DatabaseAccess.CUSTOMER_TABLE_NAME; 
    
    
    /** Vytvoøí nový objekt Statistik */
    Statistik()  {

    }
    
    /**
     *  Vrací poèet skladových karet. Nebo-li celkový sortiment
     * @return Poèet skladových karet
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     */
    public int getGoodsCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "";
        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrací poèet kladných skladových karet. Nebo-li celkový sortiment,
     *  který je v souèasné dobì k dispozici
     * @return Poèet nenulových skladových karet
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     */
    public int getPositiveCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "WHERE quantity > 0";
        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrací poèet záporných skladových karet. N
     * @return Poèet nenulových skladových karet
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     */
    public int getNegativeCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "WHERE quantity < 0";
        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrací poèet nulových skladových karet. Nebo-li celkový sortiment,
     *  který byl døíve k dispozici. V souèasné dobì však není
     * @return Poèet nulových skladových karet
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     */
    public int getZeroGoodsCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "WHERE quantity = 0";
        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     * Vrací poèet dodavatelù
     * @return Poèet dodavatelù
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     */
    public int getSuplierCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + SUPLIER_NAME + " ";

        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     * Vrací poèet odbìratelù
     * @return Poèet dodavatelù
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     */
    public int getCustomerCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + CUSTOMER_NAME + " ";

        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrací celkovou hodnotu skladu.
     *  Hodnota je vyjádøena jako souèet NC * quantity pro každé zboží
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     * @return Celkovou cenu skladu. Long èíslo, jehož poslední dvì cifry jsou haléøe
     */
    public long getStorePrice() throws SQLException {
        long result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT SUM(quantity * nc) FROM " + GOODS_NAME + " ";
                
        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        if (rs.next())
            result = (new BigDecimal(rs.getDouble(1)).multiply(Store.CENT)).longValue();
        
        rs.close();
        stm.close();
        return result;
        
    }
    
    /**
     *  Vrací seznam zboží seøazený podle toho, kolikrát se nakoupilo. 
     *  Nejnakupovanìjší zboží se urèí seètením množství na všech pøíjemkách,
     *  které jsou v databázi
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     * @return Seznam zboží seøazený podle nejnakupovanìjšího zboží
     */
    public ArrayList<ChartItem> getTopBuyGoods() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT goods.*, SUM(buy.quantity) count FROM " +
                GOODS_NAME +" goods LEFT JOIN " + BUY_NAME + " buy " +
                "ON goods.goods_id = buy.goods_id GROUP BY goods.goods_id ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        ArrayList<ChartItem> result = getGoodsList(rs);
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrací seznam zboží seøazený podle toho, kolikrát se prodalo. 
     *  Nejprodávanìjšíí zboží se urèí seètením množství na všech výdejkách,
     *  které jsou v databázi
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     * @return Seznam zboží seøazený podle nejprodávanìjšího
     */
    public ArrayList<ChartItem> getTopSaleGoods() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT goods.*, SUM(sale.quantity) count FROM " +
                GOODS_NAME +" goods LEFT JOIN " + SALE_NAME + " sale " +
                "ON goods.goods_id = sale.goods_id GROUP BY goods.goods_id ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze
        
        ArrayList<ChartItem> result = getGoodsList(rs);
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrací žebøíèek zboží vybraný z result setu
     */
    private ArrayList<ChartItem> getGoodsList(ResultSet rs) throws SQLException {
        
        ArrayList<ChartItem> results = new ArrayList<ChartItem>();
        while (rs.next()) { // Doplò do seznamu
            results.add( new ChartItem(
                    new Goods(
                        rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), 
                        rs.getString(5), rs.getString(6), 
                        (new BigDecimal(rs.getString(7))).multiply(Store.CENT).intValue(), 
                        (new BigDecimal(rs.getString(8))).multiply(Store.CENT).intValue(), 
                        (new BigDecimal(rs.getString(9))).multiply(Store.CENT).intValue(), 
                        (new BigDecimal(rs.getString(10))).multiply(Store.CENT).intValue(), 
                        (new BigDecimal(rs.getString(11))).multiply(Store.CENT).intValue(), 
                        rs.getInt(12)),
                        
                    rs.getLong(13)) // poèat "hlasù"
                    );
        }
        
        return results;
    }
    
    /**
     * Vrací seznam dodavatelù, seøazený podle toho, kdo dodal nejvícekrát dodával zboží.
     * Nebo-li který dodavatel je nejèetnìjší
     *  
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     * @return Seznam dodavatelù, seøazený podle toho, kdo nejvcekrát dodal zboží
     */
    public ArrayList<ChartItem> getTopSuplier() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT sup.*, SUM(buy.sup_id IS NOT NULL) count FROM " +
                SUPLIER_NAME+" sup LEFT JOIN "+BUY_LISTING_NAME+" buy " +
                "ON sup.sup_id = buy.sup_id " +
                "GROUP BY sup.sup_id " +
                "ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze

        ArrayList<ChartItem> result = new ArrayList<ChartItem>();
        while (rs.next()) { //vytvoø seznam dodavaltelù
            result.add( new ChartItem(
                    new Suplier(
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), 
                        rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), 
                        rs.getString(11), rs.getString(12), rs.getBoolean(13), rs.getString(14),
                        rs.getString(15)),
                   
                    rs.getLong(16) )
                    );
        }
        
        rs.close();
        stm.close();
        return result;
    }    

    
    /**
     * Vrací seznam odbìratelù, seøazený podle toho, kdo odebral nejvícekrát dodával zboží.
     *  
     * @throws java.sql.SQLException Vyvolá, jestliže dojde pøi práci s databází k chybì
     * @return Seznam odbìratelù seøazený podle toho, kdo nejvícekrát odebral zboží
     */
    public ArrayList<ChartItem> getTopCustomer() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT cust.*, SUM(sale.cust_id IS NOT NULL) count FROM " +
                CUSTOMER_NAME+" cust LEFT JOIN "+SALE_LISTING+" sale " +
                "ON cust.cust_id = sale.cust_id " +
                "GROUP BY sale.cust_id " +
                "ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // naèti zboží z databáze

        ArrayList<ChartItem> result = new ArrayList<ChartItem>();
        while (rs.next()) { //vytvoø seznam dodavaltelù
            result.add( new ChartItem(
                    new Customer(
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), 
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), 
                        rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14), 
                        rs.getString(15), rs.getBoolean(16), rs.getString(17),
                        rs.getString(18)),
                   
                    rs.getLong(19) )
                    );
        }
        
        rs.close();
        stm.close();
        return result;
    }    

    

}
