

package cz.control.business;

import cz.control.database.DatabaseAccess;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Zji��uje informace o datab�zov�m sch�matu
 * @author kamilos
 */
public class Schemas {

    /**
     * Metoda zjisti, zda existuje datab�zov� sch�ma,
     * ve kter�m jsou ulo�eny ve�ker� tabulky
     * @return
     */
    public boolean schemaExists() throws SQLException {

        String dbName = Settings.getDatabaseName();
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        ResultSet rs = stm.executeQuery("" +
                "SHOW DATABASES LIKE '" + dbName + "'");

        boolean result = false;

        //�najdi zda sch�ma existuje
        while (rs.next()) {
            if (rs.getString(1).equals(dbName)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
