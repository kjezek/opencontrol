/*
 * Account.java
 *
 * Created on 13. z��� 2005, 20:10
 */

package cz.control.business;

import cz.control.database.DatabaseAccess;
import cz.control.data.*;
import cz.control.business.*;
import java.sql.*;
import java.util.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pracuj�c� s u�ivatelsk�mi ��ty u�ivatel�. Pracuje s datab�z� ze kter� na��t� a ukl�d�
 * ��ty u�ivatel�.
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public final class Account {
    private static final String ACC_NAME = DatabaseAccess.ACCOUNT_TABLE_NAME; // ulo� n�zev datab�ze
    
    private ArrayList<Client> clients = new ArrayList<Client>();
    
    /**
     * 
     *  Vytvo�� instanci pracuj�c� s u�ivatelsk�mi ��ty
     * @throws java.sql.SQLException Jestli�e dojde k chyb� p�i pr�ci s datab�z�
     */
    Account()  { // konstruktor lok�ln� v r�mci bal�ku

    }

    /**
     * Vytvo�� nov�ho u�ivatele syst�mu
     *
     * @param client nov� u�ivatel, kter� bude ulo�en do datab�ze
     * @throws java.sql.SQLException jestli�e se nepoda�ilo zapsat u�ivatele do datab�ze
     */
    public void createUser(Client client) throws SQLException{
        /* Ulo� u�ivatele do datab�ze */
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + ACC_NAME + " (type, name, login_name, password) VALUES (?, ?, ?, ?)");
        pstm.setInt(1, client.getType());
        pstm.setString(2, client.getName());
        pstm.setString(3, client.getLoginName());
        pstm.setString(4, client.getPassword());
        pstm.executeUpdate(); // Prove� operaci
        
        pstm.close();
    }
    

    /**
     * Vrac� seznam v�ech u�ivatel�, kte�� jsou ulo�eny v syst�mu. U�ivatele na��t� z datab�ze
     * @throws java.sql.SQLException vyvol�, jestli�e se nepoda�ilo u�ivatel na��st z datab�ze
     * @return ArrayList<Client> - senzma u�ivatel� syst�mu. Jestli�e nejsou v datab�zi ��dn� 
     * u�ivatel�, vrac� pr�zdn� pole
     */
    public ArrayList<Client> getAllUser() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + ACC_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze
        
        clients.clear(); // vyma� polo�ky
        while (rs.next()) { //vytvo� seznam klient�
            clients.add(new Client(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5)));
        }
        
        rs.close();
        stm.close();
        return clients;
    }
    
    /**
     * Vrac� u�ivatele z datab�ze, kter� dpov�d� zadan�mu p�ihla�ovac�mu jm�nu
     * @param name P�ihla�ovac� jm�no u�ivatele
     * @throws java.sql.SQLException Vyvol�, jestli�e se nepoda�ilo u�ivatele na��st z datab�ze
     * @return intanci klienta, nebo pr�zdn�ho klienta, jestli�e v datab�zi nebyl nalezen odpov�daj�c�
     * z�znam.
     * Pr�zdn�ho klienta je vhodn� rozezn�vat tak, �e m� nastaven parametr <CODE>type</CODE> na 0
     */
    public Client getUserByLoginName(String name) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + ACC_NAME + " WHERE login_name LIKE '" + name + "'";
        ResultSet rs = stm.executeQuery(command); // na�ti u�ivatele z datab�ze
        
         // jestli�e seznam pr�zdn�, vra� "nulov�ho" u�ivatele 
        if (rs.next() == false) {
            rs.close();
            return new Client();
        }

        Client client = new Client(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5));
        rs.close();
        stm.close();
        return client;
    }
    
    /**
     * Vyma�e u�ivatele z datab�ze
     * @param client u�ivatel, kter� m� b�t vymaz�n
     * @throws java.sql.SQLException vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z�
     */
    public void deleteUser(Client client) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + ACC_NAME + " WHERE login_name LIKE '" + client.getLoginName() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /**
     * Provede zm�nu u�ivatele v datab�zi
     * @param oldClient Star� klient, kter� bude zm�n�n
     * @param newClient Nov� klient, na kter�ho se m� zm�nit
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde b�hem operace k chyb� s datab�z�
     */
    public void editUser(Client oldClient, Client newClient) throws SQLException {
        String command = 
                "UPDATE " + ACC_NAME + " SET type = ?, name = ?, login_name = ?, password = ? " + 
                " WHERE login_name LIKE ?";
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        pstm.setInt(1, newClient.getType());
        pstm.setString(2, newClient.getName());
        pstm.setString(3, newClient.getLoginName());
        pstm.setString(4, newClient.getPassword());
        pstm.setString(5, oldClient.getLoginName());
        pstm.executeUpdate();
        pstm.close();
    }
    
    /**
     * Provede zm�nu u�ivatele
     * @param loginName star� p�ihla�ovac� jm�no
     * @param newName nov� jm�no u�ivatele
     * @param newLoginName nov� p�ihla�ovac� jm�no
     * @param newPassword nov� heslo
     */
    public void editUser(String loginName, String newName, String newLoginName, String newPassword) throws SQLException {
        Client oldClient = getUserByLoginName(loginName);
        Client newClient = new Client(0, oldClient.getType(), newName, newLoginName, newPassword);
        editUser(oldClient, newClient);
    }
    
    /**
     * Vrac� po�et u�ivatel� v datab�zi
     * 
     * @return po�et u�ivatel�. Pokud dotaz do datab�ze nevr�t� ��dn� z�znam, metoda vrac� 0
     * @throws java.sql.SQLException SQL chyba
     */
    public static int getUsersCount() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        ResultSet rs = stm.executeQuery("SELECT count(*) FROM " + ACC_NAME);
        if (rs.next() == false) {
            return 0;
        }
        return rs.getInt(1);
    }
    
    
    
}
