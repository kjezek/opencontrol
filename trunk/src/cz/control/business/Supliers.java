/*
 * Supliers.java
 *
 * Created on 23. z��� 2005, 20:36
 */

package cz.control.business;

import cz.control.data.Suplier;
import cz.control.database.DatabaseAccess;
import java.util.*;
import java.sql.*;

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladov� syst�m
 *
 * Pracuje s kartami dodavatel�
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public final class Supliers {
    private static final String SUP_NAME = DatabaseAccess.SUPLIER_TABLE_NAME; // ulo� n�zev datab�ze
    
    /* Zaka� vytvo�it instanci zvenku  */
    Supliers()   {

    }
    
    /*
     *  Vytvo�� u�ivatele a ulo�� ho do datab�ze
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
        pstm.executeUpdate(); // Prove� operaci
        pstm.close();
    }
    
    /*
     *  Vyma�e u�ivatele z datab�ze
     */
    void deleteSuplier(Suplier suplier)  throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + SUP_NAME + " WHERE sup_id = '" + suplier.getId() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /*
     *  Zm�n� u�ivatele
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
     * Vyhled� dodavatele podle kl��ov�ho slova. 
     * @return seznam nalezen�ch dodavatel�
     * @param keyword kl��ov� slovo, kter� se bude vyhled�vat
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
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
        ResultSet rs = stm.executeQuery(command); // na�ti dodavatele  z datab�ze
        
        ArrayList<Suplier> result = new ArrayList<Suplier>();
        while (rs.next()) { //vytvo� seznam dodavaltel�
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
     * Vyhled� dodavatele podle identifika�n�ho ��sla. Jestli�e dodavatele
     * nenalezl, vr�t� pr�zdn� objekt
     * @return nalezen� dodavatel, nebo pr�zdn� objekt. Pr�zdn� objekt je vhodn�
     * rozeznat podle z�porn� hodnoty parametru <code>sup_id</code>
     * @param isLock true, jestli�e se m� ��dek v datab�zi zamknout
     * @param id identifika�n� ��slo
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
     */
    public Suplier getSuplierByID(int id, boolean isLock)  throws SQLException {
        String lock = (isLock) ? LOCK_TEXT : " ";
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SUP_NAME + " WHERE sup_id = '" + id + "'" + lock;
        ResultSet rs = stm.executeQuery(command); // na�ti dodavate�e z datab�ze
        
        if (rs.next() == false) {
            rs.close();
            return new Suplier(0, "", "", "", "", "", "", "", "", "", "", "", false, "", ""); // jestli�e seznam pr�zdn�, vra� "nulov�ho" dodavatele 
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
     * Vyhled� dodavatele podle identifika�n�ho ��sla. Jestli�e dodavatele
     * nenalezl, vr�t� pr�zdn� objekt
     * @param id identifika�n� ��slo
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
     * @return nalezen� dodavatel, nebo pr�zdn� objekt. Pr�zdn� objekt je vhodn�
     * rozeznat podle z�porn� hodnoty parametru <code>sup_id</code>
     */
    public Suplier getSuplierByID(int id)  throws SQLException {
        return getSuplierByID(id, false);
    }
    
    /**
     * Vr�t� seznam v�ech dodavatel� v datab�zi
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
     * @return seznam dodavatel� v datab�zi
     */
    public ArrayList<Suplier> getAllSupliers()  throws SQLException {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + SUP_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti dodavatele  z datab�ze
        
        ArrayList<Suplier> result = new ArrayList<Suplier>();
        while (rs.next()) { //vytvo� seznam dodavaltel�
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
