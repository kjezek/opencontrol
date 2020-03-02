

package cz.control.business;

import cz.control.database.DatabaseAccess;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Zjiöùuje informace o datab·zovÈm schÈmatu
 * @author kamilos
 */
public class Schemas {

    /**
     * Metoda zjisti, zda existuje datab·zovÈ schÈma,
     * ve kterÈm jsou uloûeny veökerÈ tabulky
     * @return
     */
    public boolean schemaExists() throws SQLException {

        String dbName = Settings.getDatabaseName();
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        ResultSet rs = stm.executeQuery("" +
                "SHOW DATABASES LIKE '" + dbName + "'");

        boolean result = false;

        //†najdi zda schÈma existuje
        while (rs.next()) {
            if (rs.getString(1).equals(dbName)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
