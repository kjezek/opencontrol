/*
 * PriceListEditor.java
 *
 * Vytvoøeno 11. bøezen 2006, 17:19
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;
import cz.control.data.PriceList;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;
import java.util.*;
import java.sql.*;       

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladový systém
 *
 * Tøída pracuje s ceníky. Naèítá a zapisuje ceníky do databáze.
 * Pomocí ceníkù se poèítají prodejní ceny z nákupní pøi 
 * nabírání zboží, nebo tvorbì skladových karet
 *
 * @author Kamil Ježek
 * 
 * (C) 2006, ver. 1.0
 */
public final class PriceListEditor {
    
    /**
     * Vytvoøí novou instanci PriceListEditor
     */
    PriceListEditor() {
    }
    
    /**
     *  Vymaže pøíslušný ceník z databáze
     */
    void deletePriceList(PriceList priceList) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + PRICELIST_TABLE_NAME + " " +
                "WHERE id = '" + priceList.getId() + "'";
        stm.executeUpdate(command);
        stm.close();        
    }
    
    /**
     *  Zapíše do databáze ceník podle vzstupního parametru.
     *  Rozpozná podle èísla dodavatele, zda se jedná o výchozí, nebo dodavatelský ceník.
     *  Jestliže je ID dodavatel == 0, jedná se o výchozí ceník
     */
    void createPriceList(PriceList priceList) throws SQLException {
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + PRICELIST_TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)");

        pstm.setInt(1, priceList.getId());
        
        // Jestliže se jedná o výchozí ceník
        if (priceList.getSupID() == 0) {
            pstm.setObject(2, null);
        } else {
            pstm.setInt(2, priceList.getSupID());
        }
        
        pstm.setBigDecimal(3, (new BigDecimal(priceList.getPcA())).divide(Store.CENT) );
        pstm.setBigDecimal(4, (new BigDecimal(priceList.getPcB())).divide(Store.CENT) );
        pstm.setBigDecimal(5, (new BigDecimal(priceList.getPcC())).divide(Store.CENT) );
        pstm.setBigDecimal(6, (new BigDecimal(priceList.getPcD())).divide(Store.CENT) );
        pstm.executeUpdate(); // Proveï operaci
        pstm.close();        
    }
    
    /**
     *  Nahradí starý ceník novým. Nahrazuje ceník který má shodné ID
     */
    void editPriceList(PriceList oldPriceList, PriceList newPriceList ) throws SQLException {
        String command = 
                "UPDATE " + PRICELIST_TABLE_NAME + " SET sup_id = ?, " + 
                "PC_A = ?, PC_B = ?, PC_C = ?, PC_D = ? " +
                " WHERE id = ?";
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);

        pstm.setInt(6, oldPriceList.getId());
        
        if (newPriceList.getSupID() == 0) {
            pstm.setObject(1,  null); // Výchozí ceník
        } else {
            pstm.setInt(1,  newPriceList.getSupID());
        }
        
        pstm.setBigDecimal(2, ( new BigDecimal(newPriceList.getPcA()).divide(Store.CENT)));
        pstm.setBigDecimal(3, ( new BigDecimal(newPriceList.getPcB()).divide(Store.CENT)));
        pstm.setBigDecimal(4, ( new BigDecimal(newPriceList.getPcC()).divide(Store.CENT)));
        pstm.setBigDecimal(5, ( new BigDecimal(newPriceList.getPcD()).divide(Store.CENT)));

        pstm.executeUpdate();
        pstm.close();        
    }
    
    
    /**
     * Vrátí seznam všech ceníkù v databázi
     * @throws java.sql.SQLException vyvolá, jestliže došlo k chybì s databází
     * @return seznam ceníkù v databázi
     */
    public ArrayList<PriceList> getAllPriceList()  throws SQLException {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + PRICELIST_TABLE_NAME + " ";
        ResultSet rs = stm.executeQuery(command); // naèti z databáze
        
        ArrayList<PriceList> result = new ArrayList<PriceList>();
        while (rs.next()) { //vytvoø seznam 
            result.add( 
                    new PriceList(rs.getInt(1), rs.getInt(2),
                    rs.getBigDecimal(3).multiply(Store.CENT).intValue(),
                    rs.getBigDecimal(4).multiply(Store.CENT).intValue(),
                    rs.getBigDecimal(5).multiply(Store.CENT).intValue(),
                    rs.getBigDecimal(6).multiply(Store.CENT).intValue())
                    );
        }
        
        rs.close();
        stm.close();
        return result;
    }   
    
    /**
     * Naète z databáze jeden ceník, podle èísla dodavatele
     * @param supID èíslo dodavatele
     * @throws java.sql.SQLException databázová chyba
     * @return 
     */
    public PriceList getPriceList(int supID) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + PRICELIST_TABLE_NAME + " WHERE sup_id = " + supID;
        ResultSet rs = stm.executeQuery(command); // naèti z databáze

        PriceList result = new PriceList();
        
        if (rs.next()) {
            result = new PriceList(rs.getInt(1), rs.getInt(2),
                rs.getBigDecimal(3).multiply(Store.CENT).intValue(),
                rs.getBigDecimal(4).multiply(Store.CENT).intValue(),
                rs.getBigDecimal(5).multiply(Store.CENT).intValue(),
                rs.getBigDecimal(6).multiply(Store.CENT).intValue());
        }
        
        rs.close();
        stm.close();
        return result;
    }
    
    
    /**
     * Naète z databáze výchozí ceník
     * @param supID èíslo dodavatele
     * @throws java.sql.SQLException databázová chyba
     * @return 
     */
    public PriceList getDefaultPriceList() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + PRICELIST_TABLE_NAME + " WHERE sup_id IS NULL ";
        ResultSet rs = stm.executeQuery(command); // naèti z databáze

        PriceList result = new PriceList();
        
        if (rs.next()) {
            result = new PriceList(rs.getInt(1), rs.getInt(2),
                rs.getBigDecimal(3).multiply(Store.CENT).intValue(),
                rs.getBigDecimal(4).multiply(Store.CENT).intValue(),
                rs.getBigDecimal(5).multiply(Store.CENT).intValue(),
                rs.getBigDecimal(6).multiply(Store.CENT).intValue());
        }
        
        rs.close();
        stm.close();
        return result;
    }    
    
}
