/*
 * Account.java
 *
 * Created on 13. záøí 2005, 20:10
 */

package cz.control.business;

import cz.control.database.DatabaseAccess;
import cz.control.data.*;
import cz.control.business.*;
import java.sql.*;
import java.util.*;

/**
 * Program Control - Skladovı systém
 *
 * Tøída pracující s uivatelskımi úèty uivatelù. Pracuje s databází ze které naèítá a ukládá
 * úèty uivatelù.
 *
 * @author Kamil Jeek
 *
 * (C) 2005, ver. 1.0
 */
public final class Account {
    private static final String ACC_NAME = DatabaseAccess.ACCOUNT_TABLE_NAME; // ulo název databáze
    
    private ArrayList<Client> clients = new ArrayList<Client>();
    
    /**
     * 
     *  Vytvoøí instanci pracující s uivatelskımi úèty
     * @throws java.sql.SQLException Jestlie dojde k chybì pøi práci s databází
     */
    Account()  { // konstruktor lokální v rámci balíku

    }

    /**
     * Vytvoøí nového uivatele systému
     *
     * @param client novı uivatel, kterı bude uloen do databáze
     * @throws java.sql.SQLException jestlie se nepodaøilo zapsat uivatele do databáze
     */
    public void createUser(Client client) throws SQLException{
        /* Ulo uivatele do databáze */
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + ACC_NAME + " (type, name, login_name, password) VALUES (?, ?, ?, ?)");
        pstm.setInt(1, client.getType());
        pstm.setString(2, client.getName());
        pstm.setString(3, client.getLoginName());
        pstm.setString(4, client.getPassword());
        pstm.executeUpdate(); // Proveï operaci
        
        pstm.close();
    }
    

    /**
     * Vrací seznam všech uivatelù, kteøí jsou uloeny v systému. Uivatele naèítá z databáze
     * @throws java.sql.SQLException vyvolá, jestlie se nepodaøilo uivatel naèíst z databáze
     * @return ArrayList<Client> - senzma uivatelù systému. Jestlie nejsou v databázi ádní 
     * uivatelé, vrací prázdné pole
     */
    public ArrayList<Client> getAllUser() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + ACC_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze
        
        clients.clear(); // vyma poloky
        while (rs.next()) { //vytvoø seznam klientù
            clients.add(new Client(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5)));
        }
        
        rs.close();
        stm.close();
        return clients;
    }
    
    /**
     * Vrací uivatele z databáze, kterı dpovídá zadanému pøihlašovacímu jménu
     * @param name Pøihlašovací jméno uivatele
     * @throws java.sql.SQLException Vyvolá, jestlie se nepodaøilo uivatele naèíst z databáze
     * @return intanci klienta, nebo prázdného klienta, jestlie v databázi nebyl nalezen odpovídající
     * záznam.
     * Prázdného klienta je vhodné rozeznávat tak, e má nastaven parametr <CODE>type</CODE> na 0
     */
    public Client getUserByLoginName(String name) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + ACC_NAME + " WHERE login_name LIKE '" + name + "'";
        ResultSet rs = stm.executeQuery(command); // naèti uivatele z databáze
        
         // jestlie seznam prázdnı, vra "nulového" uivatele 
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
     * Vymae uivatele z databáze
     * @param client uivatel, kterı má bıt vymazán
     * @throws java.sql.SQLException vyvolá, jestlie dojde k chybì pøi práci s databází
     */
    public void deleteUser(Client client) throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + ACC_NAME + " WHERE login_name LIKE '" + client.getLoginName() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /**
     * Provede zmìnu uivatele v databázi
     * @param oldClient Starı klient, kterı bude zmìnìn
     * @param newClient Novı klient, na kterého se má zmìnit
     * @throws java.sql.SQLException Vyvolá, jestlie dojde bìhem operace k chybì s databází
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
     * Provede zmìnu uivatele
     * @param loginName staré pøihlašovací jméno
     * @param newName nové jméno uivatele
     * @param newLoginName nové pøihlašovací jméno
     * @param newPassword nové heslo
     */
    public void editUser(String loginName, String newName, String newLoginName, String newPassword) throws SQLException {
        Client oldClient = getUserByLoginName(loginName);
        Client newClient = new Client(0, oldClient.getType(), newName, newLoginName, newPassword);
        editUser(oldClient, newClient);
    }
    
    /**
     * Vrací poèet uivatelù v databázi
     * 
     * @return poèet uivatelù. Pokud dotaz do databáze nevrátí ádnı zıznam, metoda vrací 0
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
