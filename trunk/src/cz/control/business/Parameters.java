/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.business;

import cz.control.data.Parameter;
import cz.control.database.DatabaseAccess;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static cz.control.database.DatabaseAccess.*;

/**
 * Tøída pracuje s tabulkou parametrù
 * @author kamilos
 */
public class Parameters {
    
    /**
     * Zjistí zda existuje parametrická tabulka
     * 
     * @return
     * @throws java.sql.SQLException
     */
    public boolean tableExists() throws SQLException {

        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        ResultSet rs = stm.executeQuery("" +
                "SHOW TABLES");
        
        while (rs.next()) {
            if (rs.getString(1).equals(DatabaseAccess.PARAMETER_TABLE_NAME)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Uloží parametr do databáze
     * @param parameter
     */
    public void updateParameter(Parameter parameter) throws SQLException {
        
        // nejprve zjistíme zda parametr existuje
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        ResultSet rs = stm.executeQuery("" +
                "SELECT count(*) FROM " + PARAMETER_TABLE_NAME);
        
        PreparedStatement pstm;
        if (rs.next() && rs.getInt(1) > 0) { // parametr již existuje
            // update
            pstm  = DatabaseAccess.getCurrentConnection().prepareStatement("" +
                    "UPDATE " + PARAMETER_TABLE_NAME + " SET " +
                    "db_version = ?");
        } else { // parametr neexistuje
            // insert
            pstm  = DatabaseAccess.getCurrentConnection().prepareStatement("" +
                    "INSERT INTO " + PARAMETER_TABLE_NAME + " VALUES (" +
                    "?" +
                    ")");
            
        }
        
        pstm.setInt(1, parameter.getDbVersion());
        
        pstm.executeUpdate();
    }
    
    public Parameter getParameter() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        ResultSet rs = stm.executeQuery("" +
                "SELECT * FROM " + PARAMETER_TABLE_NAME);

        Parameter param = new Parameter();
        
        if (rs.next()) {
            param.setDbVersion(rs.getInt("db_version"));
        } 
        
        return param;        
    }
}
