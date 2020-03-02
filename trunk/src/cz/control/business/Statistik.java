/*
 * Statistik.java
 *
 * Vytvo�eno 17. prosinec 2005, 12:21
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
 * Program Control - Skladov� syst�m
 *
 * T��da pro v�po�et statistick�ch �daj�. Na��t� z datab�ze �daje o celkov�ch cen�ch,
 * o maxim�ln�ch, minim�ln�ch a pr�m�rn�ch cen�ch.
 * D�le pak o mno�stv� zbo�� na sklad�.
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public class Statistik {
    private static final String GOODS_NAME = DatabaseAccess.GOODS_TABLE_NAME; 

    private static final String BUY_NAME = DatabaseAccess.BUY_TABLE_NAME; 
    private static final String BUY_LISTING_NAME = DatabaseAccess.BUY_LISITNG_TABLE_NAME; 
    private static final String SUPLIER_NAME = DatabaseAccess.SUPLIER_TABLE_NAME; 
    
    private static final String SALE_NAME = DatabaseAccess.SALE_TABLE_NAME; // ulo� n�zev datab�ze
    private static final String SALE_LISTING = DatabaseAccess.SALE_LISTING_TABLE_NAME; // ulo� n�zev datab�ze
    private static final String CUSTOMER_NAME = DatabaseAccess.CUSTOMER_TABLE_NAME; 
    
    
    /** Vytvo�� nov� objekt Statistik */
    Statistik()  {

    }
    
    /**
     *  Vrac� po�et skladov�ch karet. Nebo-li celkov� sortiment
     * @return Po�et skladov�ch karet
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getGoodsCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrac� po�et kladn�ch skladov�ch karet. Nebo-li celkov� sortiment,
     *  kter� je v sou�asn� dob� k dispozici
     * @return Po�et nenulov�ch skladov�ch karet
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getPositiveCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "WHERE quantity > 0";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrac� po�et z�porn�ch skladov�ch karet. N
     * @return Po�et nenulov�ch skladov�ch karet
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getNegativeCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "WHERE quantity < 0";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrac� po�et nulov�ch skladov�ch karet. Nebo-li celkov� sortiment,
     *  kter� byl d��ve k dispozici. V sou�asn� dob� v�ak nen�
     * @return Po�et nulov�ch skladov�ch karet
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getZeroGoodsCardCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + GOODS_NAME + " " +
                "WHERE quantity = 0";
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     * Vrac� po�et dodavatel�
     * @return Po�et dodavatel�
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getSuplierCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + SUPLIER_NAME + " ";

        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     * Vrac� po�et odb�ratel�
     * @return Po�et dodavatel�
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    public int getCustomerCount() throws SQLException {
        int result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT COUNT(*) FROM " + CUSTOMER_NAME + " ";

        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = rs.getInt(1);
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrac� celkovou hodnotu skladu.
     *  Hodnota je vyj�d�ena jako sou�et NC * quantity pro ka�d� zbo��
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     * @return Celkovou cenu skladu. Long ��slo, jeho� posledn� dv� cifry jsou hal��e
     */
    public long getStorePrice() throws SQLException {
        long result = 0;
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT SUM(quantity * nc) FROM " + GOODS_NAME + " ";
                
        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        if (rs.next())
            result = (new BigDecimal(rs.getDouble(1)).multiply(Store.CENT)).longValue();
        
        rs.close();
        stm.close();
        return result;
        
    }
    
    /**
     *  Vrac� seznam zbo�� se�azen� podle toho, kolikr�t se nakoupilo. 
     *  Nejnakupovan�j�� zbo�� se ur�� se�ten�m mno�stv� na v�ech p��jemk�ch,
     *  kter� jsou v datab�zi
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     * @return Seznam zbo�� se�azen� podle nejnakupovan�j��ho zbo��
     */
    public ArrayList<ChartItem> getTopBuyGoods() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT goods.*, SUM(buy.quantity) count FROM " +
                GOODS_NAME +" goods LEFT JOIN " + BUY_NAME + " buy " +
                "ON goods.goods_id = buy.goods_id GROUP BY goods.goods_id ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        ArrayList<ChartItem> result = getGoodsList(rs);
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrac� seznam zbo�� se�azen� podle toho, kolikr�t se prodalo. 
     *  Nejprod�van�j��� zbo�� se ur�� se�ten�m mno�stv� na v�ech v�dejk�ch,
     *  kter� jsou v datab�zi
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     * @return Seznam zbo�� se�azen� podle nejprod�van�j��ho
     */
    public ArrayList<ChartItem> getTopSaleGoods() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT goods.*, SUM(sale.quantity) count FROM " +
                GOODS_NAME +" goods LEFT JOIN " + SALE_NAME + " sale " +
                "ON goods.goods_id = sale.goods_id GROUP BY goods.goods_id ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze
        
        ArrayList<ChartItem> result = getGoodsList(rs);
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     *  Vrac� �eb���ek zbo�� vybran� z result setu
     */
    private ArrayList<ChartItem> getGoodsList(ResultSet rs) throws SQLException {
        
        ArrayList<ChartItem> results = new ArrayList<ChartItem>();
        while (rs.next()) { // Dopl� do seznamu
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
                        
                    rs.getLong(13)) // po�at "hlas�"
                    );
        }
        
        return results;
    }
    
    /**
     * Vrac� seznam dodavatel�, se�azen� podle toho, kdo dodal nejv�cekr�t dod�val zbo��.
     * Nebo-li kter� dodavatel je nej�etn�j��
     *  
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     * @return Seznam dodavatel�, se�azen� podle toho, kdo nejvcekr�t dodal zbo��
     */
    public ArrayList<ChartItem> getTopSuplier() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT sup.*, SUM(buy.sup_id IS NOT NULL) count FROM " +
                SUPLIER_NAME+" sup LEFT JOIN "+BUY_LISTING_NAME+" buy " +
                "ON sup.sup_id = buy.sup_id " +
                "GROUP BY sup.sup_id " +
                "ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        ArrayList<ChartItem> result = new ArrayList<ChartItem>();
        while (rs.next()) { //vytvo� seznam dodavaltel�
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
     * Vrac� seznam odb�ratel�, se�azen� podle toho, kdo odebral nejv�cekr�t dod�val zbo��.
     *  
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde p�i pr�ci s datab�z� k chyb�
     * @return Seznam odb�ratel� se�azen� podle toho, kdo nejv�cekr�t odebral zbo��
     */
    public ArrayList<ChartItem> getTopCustomer() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT cust.*, SUM(sale.cust_id IS NOT NULL) count FROM " +
                CUSTOMER_NAME+" cust LEFT JOIN "+SALE_LISTING+" sale " +
                "ON cust.cust_id = sale.cust_id " +
                "GROUP BY sale.cust_id " +
                "ORDER BY count DESC";

        ResultSet rs = stm.executeQuery(command); // na�ti zbo�� z datab�ze

        ArrayList<ChartItem> result = new ArrayList<ChartItem>();
        while (rs.next()) { //vytvo� seznam dodavaltel�
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
