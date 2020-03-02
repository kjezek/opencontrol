package cz.control.database;

import cz.control.data.*;
import cz.control.business.*;
import cz.control.data.Parameter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static cz.control.database.DatabaseAccess.*;

/**
 * T��da aktualizuje verze datab�ze
 *
 * @author Kamil Je�ek
 */
public class DatabaseVersions {
    private Parameters parameter = new Parameters();
    private DbUpgrade upgrade = new DbUpgrade();
    private Schemas schemas = new Schemas();
    
    private static String METHOD_PREFIX = "upgrade_";
    
   
    
    
    public void runUpdates() throws Exception {
        if (getCurrentConnection() == null) { // Je pot�eba m�t vytvo�eno spojen� s datab�z�
            throw new SQLException("Nen� vytvo�eno spojen� s datab�z�");
        }
        
        try {

            setAutoCommit(false);
            
            // Pokud tabulka parametr� nebo cel� datab�ze neexistuje
            // spus� prvn� upgrade, kter� vytv��� tabulkky
            if (!schemas.schemaExists() || !parameter.tableExists()) {
                upgrade.upgrade_0(); 
            }

            // Na�ti sou�asnou verzi z datab�ze
            Parameter param = parameter.getParameter();
            int currentVer = param.getDbVersion();


            List<String> upgradeMethods = new ArrayList<String>();
            // Nejprve nalezni v�echny "upgrade" metody
            for (Method method: upgrade.getClass().getMethods()) {

                String methodName = method.getName();
            
                if (methodName.startsWith(METHOD_PREFIX)) {
                    // Na�ti ��slo verze
                    int versionNumber = versionFromMethod(methodName);
                    
                    if (versionNumber > currentVer) {
                        upgradeMethods.add(methodName);
                    }
                }
            }
            
            // se�a� nalezen� metody 
            Collections.sort(upgradeMethods, new Comparator<String>() {

                public int compare(String o1, String o2) {
                    int version1 = versionFromMethod(o1);
                    int version2 = versionFromMethod(o2);
                    
                    return version1 - version2;
                }
            });
            
            // zavolej upgrade metody
            for (String method: upgradeMethods) {
                Method updateMethod = upgrade.getClass().getMethod(method);
                
                // Volej metodu pro upgrade
                updateMethod.invoke(upgrade);
            }

            // ulo� novou verzi DB
            if (!upgradeMethods.isEmpty()) {
                String lastMethod = upgradeMethods.get(upgradeMethods.size()-1);
                param.setDbVersion(versionFromMethod(lastMethod));
                parameter.updateParameter(param);
            }

            commit();
        
        } catch (Exception e) {
            rollBack();
            throw e;
        } finally {
            setAutoCommit(true);
        }
    }
    
    private int versionFromMethod(String methodName) {
        return Integer.parseInt(methodName.split("\\_")[1]);
    }

    
}
