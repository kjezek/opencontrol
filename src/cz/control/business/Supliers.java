/*
 * Supliers.java
 *
 * Created on 23. záøí 2005, 20:36
 */

package cz.control.business;

import cz.control.data.Suplier;
import cz.control.database.DatabaseAccess;
import java.util.*;
import java.sql.*;

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladovı systém
 *
 * Pracuje s kartami dodavatelù
 *
 * @author Kamil Jeek
 * 
 * (C) 2005, ver. 1.0
 */
public final class Supliers {
    private static final String SUP_NAME = DatabaseAccess.SUPLIER_TABLE_NAME; // ulo název databáze
    
    /* Zaka vytvoøit instanci zvenku  */
    Supliers()   {

    }
    
    /*
     *  Vytvoøí uivatele a uloí ho do databáze
     */
    void createSuplier(Suplier suplier) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + SUP_NAME + " " +
            "(name, person, send_street, send_city, send_PSC, tel, fax, mail, web, ICO, DIC, is_DPH, account, note) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        pstm.setString(1 ,suplier.getName());
        pstm.setString(2 ,suplier.getPerson());
        pstm.setString(3 ,suplier.getSendStreet());
        pstm.setString(4 ,suplier.getSendCity());
        pstm.setString(5 ,suplier.getSendPsc());
        pstm.setString(6 ,suplier.getTel());
        pstm.setString(7 ,suplier.getFax());
        pstm.setString(8 ,suplier.getMail());
        pstm.setString(9 ,suplier.getWeb());
        pstm.setString(10 ,suplier.getIco());
        pstm.setString(11 ,suplier.getDic());
        pstm.setBoolean(12 ,suplier.isDph());
        pstm.setString(13 ,suplier.getAccount());
        pstm.setString(14 ,suplier.getNote());
        pstm.executeUpdate(); // Proveï operaci
        pstm.close();
    }
    
    /*
     *  Vymae uivatele z databáze
     */
    void deleteSuplier(Suplier suplier)  throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + SUP_NAME + " WHERE sup_id = '" + suplier.getId() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /*
     *  Zmìní uivatele
     */
    void editSuplier(Suplier oldSuplier, Suplier newSuplier)  throws SQLException {
        String command = 
                "UPDATE " + SUP_NAME + " SET name = ?, person = ?, send_street = ?, " +
                "send_city = ?, send_PSC = ?, tel = ?, fax = ?, mail = ?, web = ?, ICO = ?, " +
                "DIC = ?, is_DPH = ?, account = ?, note = ?" + 
                " WHERE sup_id = ?";
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        pstm.setString(1, newSuplier.getName());
        pstm.setString(2, newSuplier.getPerson());
        pstm.setString(3, newSuplier.getSendStreet());
        pstm.setString(4, newSuplier.getSendCity());
        pstm.setString(5, newSuplier.getSendPsc());
        pstm.setString(6, newSuplier.getTel());
        pstm.setString(7, newSuplier.getFax());
        pstm.setString(8, newSuplier.getMail());
        pstm.setString(9, newSuplier.getWeb());
        pstm.setString(10, newSuplier.getIco());
        pstm.setString(11, newSuplier.getDic());
        pstm.setBoolean(12, newSuplier.isDph());
        pstm.setString(13, newSuplier.getAccount());
        pstm.setString(14, newSuplier.getNote());
        pstm.setInt(15, oldSuplier.getId());
        pstm.executeUpdate();
        pstm.close();
    }
    
    /**
     * Vyhledá dodavatele podle klíèového slova. 
     * @return seznam nalezenıch dodavatelù
     * @param keyword klíèové slovo, které se bude vyhledávat
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     */
    public ArrayList<Suplier> getSuplierByKeyword(String keyword)  throws SQLException {
       
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SUP_NAME + " " +
                "WHERE " +
                "name LIKE '%" + keyword + "%' || " +
                "person LIKE '%" + keyword + "%' || " +
                "send_street LIKE '%" + keyword + "%' || " +
                "send_city LIKE '%" + keyword + "%' || " +
                "send_PSC LIKE '%" + keyword + "%' || " +
                "tel LIKE '%" + keyword + "%' || " +
                "fax LIKE '%" + keyword + "%' || " +
                "mail LIKE '%" + keyword + "%' || " +
                "web LIKE '%" + keyword + "%' || " +
                "ICO LIKE '%" + keyword + "%' || " +
                "DIC LIKE '%" + keyword + "%' || " +
                "account LIKE '%" + keyword + "%' " +
                "ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti dodavatele  z databáze
        
        ArrayList<Suplier> result = new ArrayList<Suplier>();
        while (rs.next()) { //vytvoø seznam dodavaltelù
            result.add( new Suplier(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), 
                 rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), 
                 rs.getString(11), rs.getString(12), rs.getBoolean(13), rs.getString(14),
                    rs.getString(15)) );
        }
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     * Vyhledá dodavatele podle identifikaèního èísla. Jestlie dodavatele
     * nenalezl, vrátí prázdnı objekt
     * @return nalezenı dodavatel, nebo prázdnı objekt. Prázdnı objekt je vhodné
     * rozeznat podle záporné hodnoty parametru <code>sup_id</code>
     * @param isLock true, jestlie se má øádek v databázi zamknout
     * @param id identifikaèní èíslo
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     */
    public Suplier getSuplierByID(int id, boolean isLock)  throws SQLException {
        String lock = (isLock) ? LOCK_TEXT : " ";
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SUP_NAME + " WHERE sup_id = '" + id + "'" + lock;
        ResultSet rs = stm.executeQuery(command); // naèti dodavateùe z databáze
        
        if (rs.next() == false) {
            rs.close();
            return new Suplier(0, "", "", "", "", "", "", "", "", "", "", "", false, "", ""); // jestlie seznam prázdnı, vra "nulového" dodavatele 
        }
        
        Suplier suplier =  new Suplier(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), 
                 rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), 
                 rs.getString(11), rs.getString(12), rs.getBoolean(13), rs.getString(14),
                rs.getString(15));

        rs.close();
        stm.close();
        return suplier;
    }
    
    /**
     * Vyhledá dodavatele podle identifikaèního èísla. Jestlie dodavatele
     * nenalezl, vrátí prázdnı objekt
     * @param id identifikaèní èíslo
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     * @return nalezenı dodavatel, nebo prázdnı objekt. Prázdnı objekt je vhodné
     * rozeznat podle záporné hodnoty parametru <code>sup_id</code>
     */
    public Suplier getSuplierByID(int id)  throws SQLException {
        return getSuplierByID(id, false);
    }
    
    /**
     * Vrátí seznam všech dodavatelù v databázi
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     * @return seznam dodavatelù v databázi
     */
    public ArrayList<Suplier> getAllSupliers()  throws SQLException {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SUP_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti dodavatele  z databáze
        
        ArrayList<Suplier> result = new ArrayList<Suplier>();
        while (rs.next()) { //vytvoø seznam dodavaltelù
            result.add( new Suplier(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), 
                 rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), 
                 rs.getString(11), rs.getString(12), rs.getBoolean(13), rs.getString(14),
                    rs.getString(15)) );
        }
        
        rs.close();
        stm.close();
        return result;
    }
    
}
