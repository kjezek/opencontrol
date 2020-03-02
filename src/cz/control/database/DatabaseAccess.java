/*
 * DatabaseAccess.java
 *
 * Created on 13. z��� 2005, 21:15
 */

package cz.control.database;

import cz.control.business.Settings;
import snaq.db.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da zaji��uj�c� p�ipojen� k datab�zi. Vytv��� spojen� a poskytuje spojen� ostatn�m t��d�m v 
 * aplika�n� vrstv�.
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
final public class DatabaseAccess {
    private static final String CLASS_NAME = "com.mysql.jdbc.Driver"; 
    private static final String DATABASE_URL_PREFIX = "jdbc:mysql:mxj://";
    private static Connection actualConnection = null;
    private static ConnectionPool connectionPool = null;
    
    //private static final int CON_TIMENOUT = 5*1000; // sec
    
    /**
     *  ��st SQL pro zam�en� ��dku
     */
    public static final String LOCK_TEXT = " LOCK IN SHARE MODE ";
    
    private static String databaseURL;
    private static String databaseUserName;
    private static String databaseUserPassword;
    private static String databaseName;

    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky u�ivatel�
     */
    public static final String ACCOUNT_TABLE_NAME = "account";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky zbo��
     */
    public static final String GOODS_TABLE_NAME = "goods";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky dodavatel�
     */
    public static final String SUPLIER_TABLE_NAME = "suplier";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky odb�ratel�
     */
    public static final String CUSTOMER_TABLE_NAME = "customer";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky P�ehledu p��jemek
     */
    public static final String BUY_LISITNG_TABLE_NAME = "buy_listing";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky P��jemek
     */
    public static final String BUY_TABLE_NAME = "buy";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky P�ehledu v�dejek
     */
    public static final String SALE_LISTING_TABLE_NAME = "sale_listing";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky V�dejek
     */
    public static final String SALE_TABLE_NAME = "sale";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky Inventurn�ch polo�ekk
     */
    public static final String STOCKING_TABLE_NAME = "stocking";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky P�ehled� inventur
     */
    public static final String STOCKING_LISTING_TABLE_NAME = "stocking_listing";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky rekapitulac�
     */
    public static final String RECAP_MONTH_TABLE_NAME = "recap_month";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky cen�k�
     */
    public static final String PRICELIST_TABLE_NAME = "pricelist";
    /**
     * Obsahuje �et�zec p�edstavuj�c� jm�no tabulky O n�s
     */
    public static final String ABOUT_TABLE_NAME = "about";
    
    public static final String PARAMETER_TABLE_NAME = "parameters";
    
    /* Zaka� vytvo�en� instance */
    private DatabaseAccess() {}
    
    /**
     * Vrac� aktu�ln� spojen�.
     * V p��pad�, �e neb�� ��dn� transakce, vybere nov� spojen� z Connection Poolu.
     * Jestli�e b�� transakce, vrac� naposledy otev�en� spojen�.
     * V p��pad�, �e se vrac� nov� spojen�. Star� uzav�e. Na to je t�eba d�t pozor!
     * 
     * @return Ukazatel na p�ipojen� k datab�zi, nebo null, jestli�e nen� ��dn� spojen� vytvo�eno
     * @throws java.sql.SQLException 
     */
    public static final Connection getCurrentConnection() throws SQLException  {
        
        // Nov� p�ipojen� vytv��ej pouze pokud:
        // - ��dn� spojen� neexistuje
        // - neb�� transakce (pokud b��, pou��v� se st�vaj�c� spojen�)
        if (actualConnection == null || actualConnection.getAutoCommit()) {
            
            // P��padn� p�edchoz� spojen� uzav�i
            if (actualConnection != null) {
                actualConnection.close();
            }
            
            //actualConnection = establishConnection();
            actualConnection = connectionPool.getConnection();
        }

        if (actualConnection.isClosed()) {
            reconnect();
        }

        return actualConnection;
    }

    /**
     *  Nastav�, nebo vypne automatick� prov�d�n� SQL p��kaz�.
     *  Funkce si pamatuje kolikr�t byla vol�na. P�i n�kolin�sobn�m vol�n�
     *  s hodnotou false je pak pot�eba volat tolikr�t s hodnotou true,
     *  aby byla vlastnost nastavena.
     *  To je pot�en�. jestli�e n�kolik komponent programu je vol�na navz�jem vno�en� a 
     *  vn�j�� modul pot�ebuje pracovat s transakc�, nesm� vno�en� modul transakci zru�it
     * 
     * @param status ur�ije zda m� b�t automatick� prov�d�n� zapnuto
     *  hodnoty true - automatick� prov�d�n� je zapnutp
     *          false - automatick� prov�d�n� je vypnuto
     * @throws java.sql.SQLException 
     */
    public static void setAutoCommit(boolean status) throws SQLException {

        if (status) {
            
            getCurrentConnection().setAutoCommit(status); // zaptni auto commit
        } else {
            getCurrentConnection().setAutoCommit(status); // zaptni auto commit
        }
            
    }
    
    /**
     * Provede proveden� SQL p��kazu. Tuto metodu je nutno volat,
     * jestli�e je vypnut� automatick� prov�d�n� SQL p��kaz�.
     *  Vol�n� se vztahuje k p�ipojen� na vrcholu z�sobn�ku
     * @throws java.sql.SQLException 
     */
    public static void commit() throws SQLException  {
        getCurrentConnection().commit();
    }
    
    /**
     * Vr�t� naposledy proveden� kroky, jestli�e do�lo p�i prov�d�n� transakce k chyb�
     *  Vol�n� se vztahuje k p�ipojen� na vrcholu z�sobn�ku 
     * @throws java.sql.SQLException Vyvol� jestli�e do�lo k chyb�
     */
    public static void rollBack() throws SQLException {
        getCurrentConnection().rollback();
    }
    
    /**
     * Na�te ovlada� pro pr�ci s MySQL datab�z� 
     * @throws Exception jestli�e se nepoda�ilo na��st ovlada� pro pr�ci s datab�z�
     */
    public static final void loadJDBCDriver() throws Exception {
        Class.forName(CLASS_NAME).newInstance();
    }
    
    /**
     * Vytvo�� spojen� s datab�z�. 
     * 
     * Pou�ije p�ihla�ovac� �daje zadan� p�i vol�n� 
     * <code>establishConnection(String databaseURL, String databaseUserName, String databaseUserPassword)</code>
     * Nen� tedy mo�n� volat tuto metodu jako prvn�
     * 
     * 
     * @throws java.sql.SQLException jestli�e se nepoda�ilo nav�zat spojen�
     * @return 
     */
    public static final Connection establishConnection(String databaseName) throws SQLException {
        return establishConnection(databaseURL, databaseUserName, databaseUserPassword, databaseName);
    }
    
    
    /**
     * Ukon�� aktu�ln� spojen� s datab�z�, kter� je na vrcholu z�sobn�ku
     * a odstran� toto spojen� ze z�sobn�ku
     * @throws java.sql.SQLException Vyvol�, jestli�e do�lo p�i k chyb� p�i zav�r�� spojen�
     */
    public static final void closeConnection() throws SQLException {
        actualConnection.close();
        actualConnection = null;
    }

    
    /**
     * Vytvo�� spojen� s datab�z� a nastav� instanci con na aktu�ln� spojen�
     *
     * @param databaseURL Adresa po��ta�e v s�ti na kter�m je nainstalov�na datab�ze
     * @param databaseUserName U�ivatelsk� jm�no pro p��stup do datab�ze
     * @param databaseUserPassword U�ivatelsk� heslo pro p��stup do datab�ze
     * @param databaseName jm�no datab�ze
     * @return Vrac� aktu�ln� nav�zan� spojen�
     * @throws java.sql.SQLException jestli�e se nepoda�ilo nav�zat spojen�
     */
    public static final Connection establishConnection(String databaseURL, String databaseUserName, String databaseUserPassword, String databaseName) throws SQLException {
        //p��m� vytvo�en� spojen�
        //Connection con = DriverManager.getConnection(DATABASE_URL_PREFIX + databaseURL + "/" + DATABASE_NAME, databaseUserName, databaseUserPassword);
        //actualConnection = con;

        String connectionString = createEmbeddedDBUrl(databaseURL, databaseName);
        if (Settings.getDatabaseLocation().equalsIgnoreCase("remote")) {
            connectionString = createStandAloneDBUrl(databaseURL, databaseName);
        }

        // vyu��v� connection pool
        connectionPool = new ConnectionPool("local", 0, 0, 0, connectionString, databaseUserName, databaseUserPassword);
        actualConnection = connectionPool.getConnection();
        
        // ulo� p�ihla�ovac� �daje 
        DatabaseAccess.databaseURL = databaseURL;
        DatabaseAccess.databaseUserName = databaseUserName;
        DatabaseAccess.databaseUserPassword = databaseUserPassword;
        DatabaseAccess.databaseName = databaseName;
        
        return actualConnection;
    }
    
    /**
     * Vytvo�� p�ipojovc� �et�zec pro stand-alone instanci
     * datab�ze
     */
    private static String createStandAloneDBUrl(String databaseURL, String databaseName) {
        return "jdbc:mysql://" + 
            databaseURL + "/" + databaseName;
    }
    
    /**
     * Vytvo�� p�ipojovac�� �et�zec pro embedded datab�zi
     */
    private static String createEmbeddedDBUrl(String databaseURL, String databaseName) {
        return "jdbc:mysql:mxj://" + 
            databaseURL + "/" + databaseName
            + "?createDatabaseIfNotExist=true"
            + "&server.initialize-user=true"
            + "&server.datadir=./database/"
            + "&server.character-set-server=utf8"
            + "&server.collation-server=utf8_czech_ci";
    }
    
    /**
     * Provede znovup�ipojen� k datab�zi s pou�it�m p�edchoz�ch p�ihla�ovac�ch 
     * hodnot
     */
    public static final void reconnect() throws SQLException {
        actualConnection = establishConnection(databaseName);
        //actualConnection = connectionPool.getConnection();
    }
    

    
    
}
