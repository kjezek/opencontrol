/*
 * DatabaseAccess.java
 *
 * Created on 13. záøí 2005, 21:15
 */

package cz.control.database;

import cz.control.business.Settings;
import snaq.db.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Program Control - Skladovı systém
 *
 * Tøída zajišující pøipojené k databázi. Vytváøí spojení a poskytuje spojení ostatním tøídám v 
 * aplikaèní vrstvì.
 *
 * @author Kamil Jeek
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
     *  èást SQL pro zamèení øádku
     */
    public static final String LOCK_TEXT = " LOCK IN SHARE MODE ";
    
    private static String databaseURL;
    private static String databaseUserName;
    private static String databaseUserPassword;
    private static String databaseName;

    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky uivatelù
     */
    public static final String ACCOUNT_TABLE_NAME = "account";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky zboí
     */
    public static final String GOODS_TABLE_NAME = "goods";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky dodavatelù
     */
    public static final String SUPLIER_TABLE_NAME = "suplier";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky odbìratelù
     */
    public static final String CUSTOMER_TABLE_NAME = "customer";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky Pøehledu pøíjemek
     */
    public static final String BUY_LISITNG_TABLE_NAME = "buy_listing";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky Pøíjemek
     */
    public static final String BUY_TABLE_NAME = "buy";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky Pøehledu vıdejek
     */
    public static final String SALE_LISTING_TABLE_NAME = "sale_listing";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky Vıdejek
     */
    public static final String SALE_TABLE_NAME = "sale";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky Inventurních poloekk
     */
    public static final String STOCKING_TABLE_NAME = "stocking";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky Pøehledù inventur
     */
    public static final String STOCKING_LISTING_TABLE_NAME = "stocking_listing";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky rekapitulací
     */
    public static final String RECAP_MONTH_TABLE_NAME = "recap_month";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky ceníkù
     */
    public static final String PRICELIST_TABLE_NAME = "pricelist";
    /**
     * Obsahuje øetìzec pøedstavující jméno tabulky O nás
     */
    public static final String ABOUT_TABLE_NAME = "about";
    
    public static final String PARAMETER_TABLE_NAME = "parameters";
    
    /* Zaka vytvoøení instance */
    private DatabaseAccess() {}
    
    /**
     * Vrací aktuální spojení.
     * V pøípadì, e nebìí ádná transakce, vybere nové spojení z Connection Poolu.
     * Jestlie bìí transakce, vrací naposledy otevøené spojení.
     * V pøípadì, e se vrací nové spojení. Staré uzavøe. Na to je tøeba dát pozor!
     * 
     * @return Ukazatel na pøipojené k databázi, nebo null, jestlie není ádné spojení vytvoøeno
     * @throws java.sql.SQLException 
     */
    public static final Connection getCurrentConnection() throws SQLException  {
        
        // Nové pøipojení vytváøej pouze pokud:
        // - ádné spojení neexistuje
        // - nebìí transakce (pokud bìí, pouívá se stávající spojení)
        if (actualConnection == null || actualConnection.getAutoCommit()) {
            
            // Pøípadné pøedchozí spojení uzavøi
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
     *  Nastaví, nebo vypne automatické provádìní SQL pøíkazù.
     *  Funkce si pamatuje kolikrát byla volána. Pøi nìkolinásobném volání
     *  s hodnotou false je pak potøeba volat tolikrát s hodnotou true,
     *  aby byla vlastnost nastavena.
     *  To je potøené. jestlie nìkolik komponent programu je volána navzájem vnoøenì a 
     *  vnìjší modul potøebuje pracovat s transakcí, nesmí vnoøenı modul transakci zrušit
     * 
     * @param status urèije zda má bıt automatické provádìní zapnuto
     *  hodnoty true - automatické provádìní je zapnutp
     *          false - automatické provádìní je vypnuto
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
     * Provede provedení SQL pøíkazu. Tuto metodu je nutno volat,
     * jestlie je vypnuté automatické provádìní SQL pøíkazù.
     *  Volání se vztahuje k pøipojení na vrcholu zásobníku
     * @throws java.sql.SQLException 
     */
    public static void commit() throws SQLException  {
        getCurrentConnection().commit();
    }
    
    /**
     * Vrátí naposledy provedené kroky, jestlie došlo pøi provádìní transakce k chybì
     *  Volání se vztahuje k pøipojení na vrcholu zásobníku 
     * @throws java.sql.SQLException Vyvolá jestlie došlo k chybì
     */
    public static void rollBack() throws SQLException {
        getCurrentConnection().rollback();
    }
    
    /**
     * Naète ovladaè pro práci s MySQL databází 
     * @throws Exception jestlie se nepodaøilo naèíst ovladaè pro práci s databází
     */
    public static final void loadJDBCDriver() throws Exception {
        Class.forName(CLASS_NAME).newInstance();
    }
    
    /**
     * Vytvoøí spojení s databází. 
     * 
     * Pouije pøihlašovací údaje zadané pøi volání 
     * <code>establishConnection(String databaseURL, String databaseUserName, String databaseUserPassword)</code>
     * Není tedy moné volat tuto metodu jako první
     * 
     * 
     * @throws java.sql.SQLException jestlie se nepodaøilo navázat spojení
     * @return 
     */
    public static final Connection establishConnection(String databaseName) throws SQLException {
        return establishConnection(databaseURL, databaseUserName, databaseUserPassword, databaseName);
    }
    
    
    /**
     * Ukonèí aktuální spojení s databází, které je na vrcholu zásobníku
     * a odstraní toto spojení ze zásobníku
     * @throws java.sql.SQLException Vyvolá, jestlie došlo pøi k chybì pøi zavíráí spojení
     */
    public static final void closeConnection() throws SQLException {
        actualConnection.close();
        actualConnection = null;
    }

    
    /**
     * Vytvoøí spojení s databází a nastaví instanci con na aktuální spojení
     *
     * @param databaseURL Adresa poèítaèe v síti na kterém je nainstalována databáze
     * @param databaseUserName Uivatelské jméno pro pøístup do databáze
     * @param databaseUserPassword Uivatelské heslo pro pøístup do databáze
     * @param databaseName jméno databáze
     * @return Vrací aktuální navázané spojení
     * @throws java.sql.SQLException jestlie se nepodaøilo navázat spojení
     */
    public static final Connection establishConnection(String databaseURL, String databaseUserName, String databaseUserPassword, String databaseName) throws SQLException {
        //pøímé vytvoøení spojení
        //Connection con = DriverManager.getConnection(DATABASE_URL_PREFIX + databaseURL + "/" + DATABASE_NAME, databaseUserName, databaseUserPassword);
        //actualConnection = con;

        String connectionString = createEmbeddedDBUrl(databaseURL, databaseName);
        if (Settings.getDatabaseLocation().equalsIgnoreCase("remote")) {
            connectionString = createStandAloneDBUrl(databaseURL, databaseName);
        }

        // vyuívá connection pool
        connectionPool = new ConnectionPool("local", 0, 0, 0, connectionString, databaseUserName, databaseUserPassword);
        actualConnection = connectionPool.getConnection();
        
        // ulo pøihlašovací údaje 
        DatabaseAccess.databaseURL = databaseURL;
        DatabaseAccess.databaseUserName = databaseUserName;
        DatabaseAccess.databaseUserPassword = databaseUserPassword;
        DatabaseAccess.databaseName = databaseName;
        
        return actualConnection;
    }
    
    /**
     * Vytvoøí pøipojovcí øetìzec pro stand-alone instanci
     * databáze
     */
    private static String createStandAloneDBUrl(String databaseURL, String databaseName) {
        return "jdbc:mysql://" + 
            databaseURL + "/" + databaseName;
    }
    
    /**
     * Vytvoøí pøipojovacíá øetìzec pro embedded databázi
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
     * Provede znovupøipojení k databázi s pouitím pøedchozích pøihlašovacích 
     * hodnot
     */
    public static final void reconnect() throws SQLException {
        actualConnection = establishConnection(databaseName);
        //actualConnection = connectionPool.getConnection();
    }
    

    
    
}
