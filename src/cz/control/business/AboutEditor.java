/*
 * AboutEditor.java
 *
 * Vytvoøeno 17. bøezen 2006, 19:56
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;


import cz.control.data.About;
import cz.control.database.DatabaseAccess;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.sql.*;       

import static cz.control.database.DatabaseAccess.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pracující s informacemi o spoleènosti, které jsou uloženy v databázi
 *
 * @author Kamil Ježek
 *
 * (C) 2006, ver. 1.0
 */
public class AboutEditor {
    
    /**
     * Vytvoøí novou instanci AboutEditor
     */
    AboutEditor() {
    }
    
    /**
     * Vymaže zákazníka v databázi
     */
    void deleteAbout(About about) throws SQLException  {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + ABOUT_TABLE_NAME + " WHERE id = '" + about.getCustomer().getId() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /*
     * Zmìní zákazníka v databázi
     */
    void editAbout(About oldAbout, About newAbout) throws SQLException  {
        String command = 
                "UPDATE " + ABOUT_TABLE_NAME + " SET name = ?, person = ?, send_street = ?, " +
                "send_city = ?, send_PSC = ?, pay_street = ?, pay_city = ?, pay_PSC = ?, " +
                "tel = ?, fax = ?, mail = ?, web = ?, ICO = ?, DIC = ?, is_DPH = ?, account = ?, " +
                "logopath = ?, note = ?" + 
                " WHERE id = ?";
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        pstm.setString(1, newAbout.getCustomer().getName());
        pstm.setString(2, newAbout.getCustomer().getPerson());
        pstm.setString(3, newAbout.getCustomer().getSendStreet());
        pstm.setString(4, newAbout.getCustomer().getSendCity());
        pstm.setString(5, newAbout.getCustomer().getSendPsc());
        pstm.setString(6, newAbout.getCustomer().getPayStreet());
        pstm.setString(7, newAbout.getCustomer().getPayCity());
        pstm.setString(8, newAbout.getCustomer().getPayPsc());
        pstm.setString(9, newAbout.getCustomer().getTel());
        pstm.setString(10, newAbout.getCustomer().getFax());
        pstm.setString(11, newAbout.getCustomer().getMail());
        pstm.setString(12, newAbout.getCustomer().getWeb());
        pstm.setString(13, newAbout.getCustomer().getIco());
        pstm.setString(14, newAbout.getCustomer().getDic());
        pstm.setBoolean(15, newAbout.getCustomer().isDph());
        pstm.setString(16, newAbout.getCustomer().getAccount());
        pstm.setBinaryStream(17, new ByteArrayInputStream(newAbout.getLogoPath()), newAbout.getLogoPath().length );
        pstm.setString(18, newAbout.getCustomer().getNote());
        pstm.setInt(19, oldAbout.getCustomer().getId());
        pstm.executeUpdate();
        pstm.close();
    }
    
    /**
     * Uloží nového zákazníka do databáze 
     */
    void createAbout(About about) throws SQLException  {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + ABOUT_TABLE_NAME + " " +
            "(name, person, send_street, send_city, send_PSC, pay_street, pay_city, pay_PSC, tel, fax, mail, web, ICO, DIC, is_DPH, account, logopath, note) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        pstm.setString(1, about.getCustomer().getName());
        pstm.setString(2, about.getCustomer().getPerson());
        pstm.setString(3, about.getCustomer().getSendStreet());
        pstm.setString(4, about.getCustomer().getSendCity());
        pstm.setString(5, about.getCustomer().getSendPsc());
        pstm.setString(6, about.getCustomer().getPayStreet());
        pstm.setString(7, about.getCustomer().getPayCity());
        pstm.setString(8, about.getCustomer().getPayPsc());
        pstm.setString(9, about.getCustomer().getTel());
        pstm.setString(10, about.getCustomer().getFax());
        pstm.setString(11, about.getCustomer().getMail());
        pstm.setString(12, about.getCustomer().getWeb());
        pstm.setString(13, about.getCustomer().getIco());
        pstm.setString(14, about.getCustomer().getDic());
        pstm.setBoolean(15, about.getCustomer().isDph());
        pstm.setString(16, about.getCustomer().getAccount());
        pstm.setBinaryStream(17, new ByteArrayInputStream(about.getLogoPath()), about.getLogoPath().length );
        pstm.setString(18, about.getCustomer().getNote());
        pstm.executeUpdate(); // Proveï operaci
        pstm.close();
    }
    
    /**
     * Vrátí objekt s informacemi o spoleènosti
     * @throws java.sql.SQLException vyvolá, jestliže došlo k chybì s databází
     * @return objekt s informacemi o spoleènosti. Jestliže neni v databázi, vrací prázdný objekt
     */
    public About getMainAbout() throws SQLException  {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + ABOUT_TABLE_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti dodavatele  z databáze
        
        About result = new About();
        if (rs.next()) { 
            result = new About(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), 
                 rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), 
                 rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14), 
                 rs.getString(15), rs.getBoolean(16), rs.getString(17), rs.getBytes(18), rs.getString(19) );
        }
        
        rs.close();
        stm.close();
        return result;
    }    
    
}
