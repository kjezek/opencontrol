/*
 * PriceListEditor.java
 *
 * Vytvo�eno 11. b�ezen 2006, 17:19
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da pracuje s cen�ky. Na��t� a zapisuje cen�ky do datab�ze.
 * Pomoc� cen�k� se po��taj� prodejn� ceny z n�kupn� p�i 
 * nab�r�n� zbo��, nebo tvorb� skladov�ch karet
 *
 * @author Kamil Je�ek
 * 
 * (C) 2006, ver. 1.0
 */
public final class PriceListEditor {
    
    /**
     * Vytvo�� novou instanci PriceListEditor
     */
    PriceListEditor() {
    }
    
    /**
     *  Vyma�e p��slu�n� cen�k z datab�ze
     */
    void deletePriceList(PriceList priceList) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + PRICELIST_TABLE_NAME + " " +
                "WHERE id = '" + priceList.getId() + "'";
        stm.executeUpdate(command);
        stm.close();        
    }
    
    /**
     *  Zap�e do datab�ze cen�k podle vzstupn�ho parametru.
     *  Rozpozn� podle ��sla dodavatele, zda se jedn� o v�choz�, nebo dodavatelsk� cen�k.
     *  Jestli�e je ID dodavatel == 0, jedn� se o v�choz� cen�k
     */
    void createPriceList(PriceList priceList) throws SQLException {
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + PRICELIST_TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)");

        pstm.setInt(1, priceList.getId());
        
        // Jestli�e se jedn� o v�choz� cen�k
        if (priceList.getSupID() == 0) {
            pstm.setObject(2, null);
        } else {
            pstm.setInt(2, priceList.getSupID());
        }
        
        pstm.setBigDecimal(3, (new BigDecimal(priceList.getPcA())).divide(Store.CENT) );
        pstm.setBigDecimal(4, (new BigDecimal(priceList.getPcB())).divide(Store.CENT) );
        pstm.setBigDecimal(5, (new BigDecimal(priceList.getPcC())).divide(Store.CENT) );
        pstm.setBigDecimal(6, (new BigDecimal(priceList.getPcD())).divide(Store.CENT) );
        pstm.executeUpdate(); // Prove� operaci
        pstm.close();        
    }
    
    /**
     *  Nahrad� star� cen�k nov�m. Nahrazuje cen�k kter� m� shodn� ID
     */
    void editPriceList(PriceList oldPriceList, PriceList newPriceList ) throws SQLException {
        String command = 
                "UPDATE " + PRICELIST_TABLE_NAME + " SET sup_id = ?, " + 
                "PC_A = ?, PC_B = ?, PC_C = ?, PC_D = ? " +
                " WHERE id = ?";
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);

        pstm.setInt(6, oldPriceList.getId());
        
        if (newPriceList.getSupID() == 0) {
            pstm.setObject(1,  null); // V�choz� cen�k
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
     * Vr�t� seznam v�ech cen�k� v datab�zi
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
     * @return seznam cen�k� v datab�zi
     */
    public ArrayList<PriceList> getAllPriceList()  throws SQLException {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + PRICELIST_TABLE_NAME + " ";
        ResultSet rs = stm.executeQuery(command); // na�ti z datab�ze
        
        ArrayList<PriceList> result = new ArrayList<PriceList>();
        while (rs.next()) { //vytvo� seznam 
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
     * Na�te z datab�ze jeden cen�k, podle ��sla dodavatele
     * @param supID ��slo dodavatele
     * @throws java.sql.SQLException datab�zov� chyba
     * @return 
     */
    public PriceList getPriceList(int supID) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + PRICELIST_TABLE_NAME + " WHERE sup_id = " + supID;
        ResultSet rs = stm.executeQuery(command); // na�ti z datab�ze

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
     * Na�te z datab�ze v�choz� cen�k
     * @param supID ��slo dodavatele
     * @throws java.sql.SQLException datab�zov� chyba
     * @return 
     */
    public PriceList getDefaultPriceList() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + PRICELIST_TABLE_NAME + " WHERE sup_id IS NULL ";
        ResultSet rs = stm.executeQuery(command); // na�ti z datab�ze

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
