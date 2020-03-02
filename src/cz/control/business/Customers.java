/*
 * Customer.java
 *
 * Created on 23. z��� 2005, 18:02
 */

package cz.control.business;

import cz.control.data.Customer;
import cz.control.database.DatabaseAccess;
import java.util.*;
import java.sql.*;       

import static cz.control.database.DatabaseAccess.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da slou�� pro pr�ci s kartami z�kazn�k�. Pracuje s datab�z� - ukl�d� a 
 * na��t� karty z�kazn�k�.
 * Obsahuje ve�ejn� metody ke kter�m maj� p��stup v�ichni u�ivatele. D�le obsahuje
 * p��telsk� metody, ke kter�m maj� p��stup jenom ur�it� u�ivatele pomoc� t��dy User
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public final class Customers {
    private static final String CUS_NAME = DatabaseAccess.CUSTOMER_TABLE_NAME; // ulo� n�zev datab�ze
    
    /* Zaka� vytv��et instance zvenku */
    Customers() {

    }
    
    /* 
     * Vyma�e z�kazn�ka v datab�zi
     */
    void deleteCustomer(Customer customer) throws SQLException  {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + CUS_NAME + " WHERE cust_id = '" + customer.getId() + "'";
        stm.executeUpdate(command);
        stm.close();
    }
    
    /*
     * Zm�n� z�kazn�ka v datab�zi
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
     * Ulo�� nov�ho z�kazn�ka do datab�ze 
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
        pstm.executeUpdate(); // Prove� operaci
        pstm.close();
    }
    
    /**
     * Vr�t� seznam v�ech odb�ratel� v datab�zi
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
     * @return seznam odb�ratel� v datab�zi
     */
    public ArrayList<Customer> getAllCustomers() throws SQLException  {
     
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + CUS_NAME + " ORDER BY name";
        ResultSet rs = stm.executeQuery(command); // na�ti dodavatele  z datab�ze
        
        ArrayList<Customer> result = new ArrayList<Customer>();
        while (rs.next()) { //vytvo� seznam dodavaltel�
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
     * Vyhled� odb�ratele podle identifika�n�ho ��sla. Jestli�e odb�ratele
     * nenalezl, vr�t� pr�zdn� objekt
     * @param id identifika�n� ��slo
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
     * @return nalezen� odb�ratel, nebo pr�zdn� objekt. Pr�zdn� objekt je vhodn�
     * rozeznat podle z�porn� hodnoty parametru <code>sup_id</code>
     */
    public Customer getCustomerByID(int id) throws SQLException  {
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + CUS_NAME + " WHERE cust_id = '" + id + "'";
        ResultSet rs = stm.executeQuery(command); // na�ti dodavate�e z datab�ze
        
        if (rs.next() == false) {
            rs.close();
            return new Customer(-1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", false, "", ""); // jestli�e seznam pr�zdn�, vra� "nulov�ho" dodavatele 
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
     * Vyhled� odb�ratele podle kl��ov�ho slova. 
     * @return seznam nalezen�ch odb�ratel�
     * @param keyword kl��ov� slovo, kter� se bude vyhled�vat
     * @throws java.sql.SQLException vyvol�, jestli�e do�lo k chyb� s datab�z�
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
        ResultSet rs = stm.executeQuery(command); // na�ti dodavatele  z datab�ze
        
        ArrayList<Customer> result = new ArrayList<Customer>();
        while (rs.next()) { //vytvo� seznam dodavaltel�
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
