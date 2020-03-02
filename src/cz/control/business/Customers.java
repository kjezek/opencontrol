/*
 * Customer.java
 *
 * Created on 23. záøí 2005, 18:02
 */

package cz.control.business;

import cz.control.data.Customer;
import cz.control.database.DatabaseAccess;
import java.util.*;
import java.sql.*;       

import static cz.control.database.DatabaseAccess.*;

/**
 * Program Control - Skladovı systém
 *
 * Tøída slouí pro práci s kartami zákazníkù. Pracuje s databází - ukládá a 
 * naèítá karty zákazníkù.
 * Obsahuje veøejné metody ke kterım mají pøístup všichni uivatele. Dále obsahuje
 * pøátelské metody, ke kterım mají pøístup jenom urèití uivatele pomocí tøídy User
 *
 * @author Kamil Jeek
 * 
 * (C) 2005, ver. 1.0
 */
public final class Customers {
    private static final String CUS_NAME = DatabaseAccess.CUSTOMER_TABLE_NAME; // ulo název databáze
    
    /* Zaka vytváøet instance zvenku */
    Customers() {

    }
    
    /* 
     * Vymae zákazníka v databázi
     */
    void deleteCustomer(Customer customer) throws SQLException  {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + CUS_NAME + " WHERE cust_id = '" + customer.getId() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /*
     * Zmìní zákazníka v databázi
     */
    void editCustomer(Customer oldCustomer, Customer newCustomer) throws SQLException  {
        String command = 
                "UPDATE " + CUS_NAME + " SET name = ?, person = ?, send_street = ?, " +
                "send_city = ?, send_PSC = ?, pay_street = ?, pay_city = ?, pay_PSC = ?, " +
                "tel = ?, fax = ?, mail = ?, web = ?, ICO = ?, DIC = ?, is_DPH = ?, account = ?, " +
                "note = ?" + 
                " WHERE cust_id = ?";
        
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        pstm.setString(1, newCustomer.getName());
        pstm.setString(2, newCustomer.getPerson());
        pstm.setString(3, newCustomer.getSendStreet());
        pstm.setString(4, newCustomer.getSendCity());
        pstm.setString(5, newCustomer.getSendPsc());
        pstm.setString(6, newCustomer.getPayStreet());
        pstm.setString(7, newCustomer.getPayCity());
        pstm.setString(8, newCustomer.getPayPsc());
        pstm.setString(9, newCustomer.getTel());
        pstm.setString(10, newCustomer.getFax());
        pstm.setString(11, newCustomer.getMail());
        pstm.setString(12, newCustomer.getWeb());
        pstm.setString(13, newCustomer.getIco());
        pstm.setString(14, newCustomer.getDic());
        pstm.setBoolean(15, newCustomer.isDph());
        pstm.setString(16, newCustomer.getAccount());
        pstm.setString(17, newCustomer.getNote());
        pstm.setInt(18, oldCustomer.getId());
        pstm.executeUpdate();
        pstm.close();
    }
    
    /*
     * Uloí nového zákazníka do databáze 
     */
    void createCustomer(Customer customer) throws SQLException  {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
            "INSERT INTO " + CUS_NAME + " " +
            "(name, person, send_street, send_city, send_PSC, pay_street, pay_city, pay_PSC, tel, fax, mail, web, ICO, DIC, is_DPH, account, note) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        pstm.setString(1, customer.getName());
        pstm.setString(2, customer.getPerson());
        pstm.setString(3, customer.getSendStreet());
        pstm.setString(4, customer.getSendCity());
        pstm.setString(5, customer.getSendPsc());
        pstm.setString(6, customer.getPayStreet());
        pstm.setString(7, customer.getPayCity());
        pstm.setString(8, customer.getPayPsc());
        pstm.setString(9, customer.getTel());
        pstm.setString(10, customer.getFax());
        pstm.setString(11, customer.getMail());
        pstm.setString(12, customer.getWeb());
        pstm.setString(13, customer.getIco());
        pstm.setString(14, customer.getDic());
        pstm.setBoolean(15, customer.isDph());
        pstm.setString(16, customer.getAccount());
        pstm.setString(17, customer.getNote());
        pstm.executeUpdate(); // Proveï operaci
        pstm.close();
    }
    
    /**
     * Vrátí seznam všech odbìratelù v databázi
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     * @return seznam odbìratelù v databázi
     */
    public ArrayList<Customer> getAllCustomers() throws SQLException  {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + CUS_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti dodavatele  z databáze
        
        ArrayList<Customer> result = new ArrayList<Customer>();
        while (rs.next()) { //vytvoø seznam dodavaltelù
            result.add( new Customer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), 
                 rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), 
                 rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14), 
                 rs.getString(15), rs.getBoolean(16), rs.getString(17),
                 rs.getString(18)) );
        }
        
        rs.close();
        stm.close();
        return result;
    }
    
    /**
     * Vyhledá odbìratele podle identifikaèního èísla. Jestlie odbìratele
     * nenalezl, vrátí prázdnı objekt
     * @param id identifikaèní èíslo
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     * @return nalezenı odbìratel, nebo prázdnı objekt. Prázdnı objekt je vhodné
     * rozeznat podle záporné hodnoty parametru <code>sup_id</code>
     */
    public Customer getCustomerByID(int id) throws SQLException  {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + CUS_NAME + " WHERE cust_id = '" + id + "'";
        ResultSet rs = stm.executeQuery(command); // naèti dodavateùe z databáze
        
        if (rs.next() == false) {
            rs.close();
            return new Customer(-1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", false, "", ""); // jestlie seznam prázdnı, vra "nulového" dodavatele 
        }

        Customer customer = new Customer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), 
                rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), 
                rs.getString(12), rs.getString(13), rs.getString(14), rs.getString(15), rs.getBoolean(16), 
                rs.getString(17), rs.getString(18));
        rs.close();
        stm.close();
        return customer;
    }
    
    /**
     * Vyhledá odbìratele podle klíèového slova. 
     * @return seznam nalezenıch odbìratelù
     * @param keyword klíèové slovo, které se bude vyhledávat
     * @throws java.sql.SQLException vyvolá, jestlie došlo k chybì s databází
     */
    public ArrayList<Customer> getCustomerByKeyword(String keyword) throws SQLException  {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + CUS_NAME + " " +
                "WHERE " +
                "name LIKE '%" + keyword + "%' || " +
                "person LIKE '%" + keyword + "%' || " +
                "send_street LIKE '%" + keyword + "%' || " +
                "send_city LIKE '%" + keyword + "%' || " +
                "send_PSC LIKE '%" + keyword + "%' || " +
                "pay_street LIKE '%" + keyword + "%' || " +
                "pay_city LIKE '%" + keyword + "%' || " +
                "pay_PSC LIKE '%" + keyword + "%' || " +
                "tel LIKE '%" + keyword + "%' || " +
                "fax LIKE '%" + keyword + "%' || " +
                "mail LIKE '%" + keyword + "%' || " +
                "web LIKE '%" + keyword + "%' || " +
                "ICO LIKE '%" + keyword + "%' || " +
                "DIC LIKE '%" + keyword + "%' || " +
                "account LIKE '%" + keyword + "%' " +
                "ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // naèti dodavatele  z databáze
        
        ArrayList<Customer> result = new ArrayList<Customer>();
        while (rs.next()) { //vytvoø seznam dodavaltelù
            result.add( new Customer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), 
                 rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), 
                 rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14), 
                 rs.getString(15), rs.getBoolean(16), rs.getString(17), rs.getString(18)) );
        }
        
        rs.close();
        stm.close();
        return result;
    }
    
}
