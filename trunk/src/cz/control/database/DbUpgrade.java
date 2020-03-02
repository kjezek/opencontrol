
package cz.control.database;

import cz.control.business.Settings;
import cz.control.business.Schemas;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static cz.control.database.DatabaseAccess.*;

/**
 * Tøída obsahuje jednotlivé verze upgrade databáze.
 * Název metody pro upgrade je ve tvaru upgrade_[èíslo verze].
 * <br>
 * Tøída {@link #DatabaseVersions} potom automaticky volá
 * jednlivé upgrade metody.
 *
 *
 * @author Kamil Ježek
 */
public class DbUpgrade {

    private Schemas schemas = new Schemas();
    
    public void upgrade_5() throws SQLException {
        runSQLUpdate("alter table " + SALE_TABLE_NAME + " add column `use_price` INTEGER");
        runSQLUpdate("update " + SALE_TABLE_NAME + " M, "
                + "(select B.use_price, A.id_sale "
                + "from " + SALE_TABLE_NAME + " A join " + SALE_LISTING_TABLE_NAME 
                + " B on A.id_sale_listing = B.id_sale_listing) as TMP "
                + "set M.use_price = TMP.use_price  where TMP.id_sale = M.id_sale");
        runSQLUpdate("alter table "  + SALE_LISTING_TABLE_NAME  + " drop column use_price");
    }
    
    /**
     * Upravuje pøeklep "goods" -&gt; "goods" ve všech tabulkách
     * @throws SQLException
     */
    public void upgrade_4() throws SQLException {

    }

    public void upgrade_3() throws SQLException {
    }
    
    public void upgrade_2() throws SQLException {
    }
    
    public void upgrade_1() throws SQLException {
    }

    public void upgrade_0() throws SQLException {

        Statement stm = null;
        
        Connection con = getCurrentConnection();    
        stm = con.createStatement();
        String cmd = null;

        
        if (!schemas.schemaExists()) {

            String dbName = Settings.getDatabaseName();

            cmd = "CREATE DATABASE IF NOT EXISTS " + dbName + " ";
            stm.executeUpdate(cmd); // Vytvoø databázi
            
            // pøipoj se na založené schéma
            DatabaseAccess.establishConnection(dbName);
            DatabaseAccess.setAutoCommit(false);
        }
        
        con = getCurrentConnection();
        stm = con.createStatement();

        // tabulka parametrù aplikace
        cmd = "CREATE TABLE IF NOT EXISTS `" + PARAMETER_TABLE_NAME +"` ( " +
          "`db_version` integer  NOT NULL " +
          ") " + 
        "ENGINE = InnoDB " +
        "CHARACTER SET utf8 COLLATE utf8_czech_ci " +
        "COMMENT = 'Parametry aplikace' " +
        "ROW_FORMAT = FIXED";        
        stm.execute(cmd);

        /* Tabulka karty zboží */
        cmd = "CREATE TABLE IF NOT EXISTS `" + GOODS_TABLE_NAME + "` (" + 
            "`goods_id` VARCHAR(20) character set utf8 DEFAULT '?', " +
            "`name` TEXT character set utf8 NOT NULL, " +
            "`type` INTEGER UNSIGNED NOT NULL DEFAULT 0, " +
            "`DPH` INTEGER UNSIGNED NOT NULL DEFAULT 19, " + 
            "`unit` CHAR(5) NOT NULL DEFAULT 'ks', " +
            "`EAN` CHAR(13) NOT NULL, " +
            "`NC` DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0, " +
            "`PC_A` DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0, " +
            "`PC_B` DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0, " +
            "`PC_C` DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0, " +
            "`PC_D` DECIMAL(12,2) NOT NULL DEFAULT 0, " +
            "`quantity` DECIMAL(12,5) NOT NULL DEFAULT 0, " +
            "PRIMARY KEY(`goods_id`) " +
            ") " + 
            "ENGINE = InnoDB " + 
            "COMMENT = 'Skladová karta' DEFAULT CHARSET = utf8 COLLATE utf8_czech_ci";
        stm.execute(cmd);
        
        cmd = "CREATE TABLE IF NOT EXISTS `" + SUPLIER_TABLE_NAME + "` ( " + 
            "`sup_id` INTEGER UNSIGNED  AUTO_INCREMENT, " + 
            "`name` TEXT  CHARACTER SET utf8 COLLATE utf8_czech_ci NOT NULL, " +
            "`person` VARCHAR(50) NOT NULL DEFAULT '', " +
            "`send_street` VARCHAR(50) NOT NULL DEFAULT '', " + 
            "`send_city` VARCHAR(30) NOT NULL DEFAULT '', " + 
            "`send_PSC` CHAR(5) NOT NULL DEFAULT '0', " +
            "`tel` CHAR(255) NOT NULL DEFAULT '+420 ', " +
            "`fax` CHAR(20) NOT NULL DEFAULT '+420 ', " +
            "`mail` VARCHAR(50) NOT NULL DEFAULT '@', " +
            "`web` VARCHAR(100) NOT NULL DEFAULT 'http://', " +
            "`ICO` VARCHAR(20) NOT NULL DEFAULT '0', " +
            "`DIC` VARCHAR(20) NOT NULL DEFAULT '', " +
            "`is_DPH` TINYINT UNSIGNED NOT NULL DEFAULT 1, " +
            "`account` VARCHAR(20) NOT NULL DEFAULT '0', " +
            "`note` TEXT, " +    
            "PRIMARY KEY(`sup_id`), " +
            "INDEX (`sup_id`) " +
            ") " +
            "ENGINE = InnoDB " + 
            "DEFAULT CHARSET = utf8 COLLATE utf8_czech_ci " + 
            "COMMENT = 'Karta dodavatele' ";
        stm.execute(cmd);

        cmd = "CREATE TABLE IF NOT EXISTS `" + CUSTOMER_TABLE_NAME + "` ( " + 
            "`cust_id` INTEGER UNSIGNED  AUTO_INCREMENT, " + 
            "`name` TEXT  CHARACTER SET utf8 COLLATE utf8_czech_ci NOT NULL, " +
            "`person` VARCHAR(50) NOT NULL DEFAULT '', " +
            "`send_street` VARCHAR(50) NOT NULL DEFAULT '', " + 
            "`send_city` VARCHAR(30) NOT NULL DEFAULT '', " + 
            "`send_PSC` CHAR(5) NOT NULL DEFAULT '0', " +
            "`pay_street` VARCHAR(50) NOT NULL DEFAULT '', " + 
            "`pay_city` VARCHAR(30) NOT NULL DEFAULT '', " + 
            "`pay_PSC` CHAR(5) NOT NULL DEFAULT '0', " +
            "`tel` CHAR(255) NOT NULL DEFAULT '+420 ', " +
            "`fax` CHAR(20) NOT NULL DEFAULT '+420 ', " +
            "`mail` VARCHAR(50) NOT NULL DEFAULT '@', " +
            "`web` VARCHAR(100) NOT NULL DEFAULT 'http://', " +
            "`ICO` VARCHAR(20) NOT NULL DEFAULT '0', " +
            "`DIC` VARCHAR(20) NOT NULL DEFAULT '', " +
            "`is_DPH` TINYINT UNSIGNED NOT NULL DEFAULT 1, " +
            "`account` VARCHAR(20) NOT NULL DEFAULT '0', " +
            "`note` TEXT, " +    
            "PRIMARY KEY(`cust_id`) " +
            ") " +
            "ENGINE = InnoDB " + 
            "CHARACTER SET utf8 COLLATE  utf8_czech_ci " + 
            "COMMENT = 'Karta odbìratele' ";
        stm.execute(cmd);
        
        /* Tabulka uživatelských úètù */
        cmd = "CREATE TABLE IF NOT EXISTS `" + ACCOUNT_TABLE_NAME + "` (" +
            "`user_id` INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY, " + 
            "`type` INTEGER DEFAULT '1', " + 
            "`name` VARCHAR(50) DEFAULT '', " + 
            "`login_name` VARCHAR(50) UNIQUE DEFAULT '', " +
            "`password` CHAR(20) DEFAULT '', " +
            "INDEX (`user_id`) " +
            ") " +
            "ENGINE = InnoDB " +
            "COMMENT = 'Uživatelské úèty' DEFAULT CHARSET = utf8 COLLATE utf8_czech_ci";
        stm.executeUpdate(cmd);
        
        /* Vytvoøí tabulku pøehledu pøíjemek */
        cmd = "CREATE TABLE IF NOT EXISTS `" + BUY_LISITNG_TABLE_NAME + "` ( " + 
            "`id_buy_listing` int(10) unsigned NOT NULL auto_increment, " +
            "`number` int(10) unsigned NOT NULL default '1', " +  
            "`date` datetime NOT NULL default '0000-00-00 00:00:00', " +
            "`sup_id` int(10) unsigned default '0', " + // Dodavatele zaèínají od è. 1, nula signalizuje neplatnou relaci
            "`total_nc_dph` decimal(20,2) UNSIGNED NOT NULL default '0.00', " +
            "`total_dph` decimal(20,2) UNSIGNED NOT NULL default '0.00', " +
            "`total_nc` decimal(20,2) UNSIGNED NOT NULL default '0.00', " +
            "`reduction` decimal(20,2) NOT NULL default '0.00', " +
            "`author` varchar(50) NOT NULL default '', " +
            "`user_id` int unsigned,  " +
            "`use_price` INTEGER, " + 
            "`bill_number` varchar(50), " +    
            "`is_cash` boolean, "  +   
            "PRIMARY KEY  (`id_buy_listing`)," +
            "KEY `sup_id` (`sup_id`), " +
            "KEY `user_id` (`user_id`), " + 
            "INDEX (`sup_id`), " +
            "INDEX (`user_id`), " +     
            "FOREIGN KEY (`sup_id`) REFERENCES `" + SUPLIER_TABLE_NAME + "` (`sup_id`) " +
            "ON DELETE RESTRICT " + 
            "ON UPDATE CASCADE, " + 
            "FOREIGN KEY (`user_id`) REFERENCES `" + ACCOUNT_TABLE_NAME + "` (`user_id`) " +
            "ON DELETE SET NULL " + 
            "ON UPDATE CASCADE " + 
            " ) " +
            "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_czech_ci COMMENT='Prehled prijemek' ";
        stm.executeUpdate(cmd);

        cmd = "CREATE TABLE IF NOT EXISTS `" + BUY_TABLE_NAME + "` ( " +
            "`id_buy` int(10) unsigned NOT NULL auto_increment, " + 
            "`id_buy_listing` int(10) unsigned NOT NULL default '1', " + 
            "`goods_id` varchar(20) character set utf8 default '?', " +
            "`name` text NOT NULL, " +
            "`dph` int(10) unsigned NOT NULL default '0', " +
            "`nc` decimal(12,2) NOT NULL default '0.00', " +
            "`quantity` DECIMAL(12,5) unsigned NOT NULL default '0', " +
            "`unit` varchar(5) NOT NULL default '', " +
            "PRIMARY KEY  (`id_buy`), " +
            "KEY `goods_id` (`goods_id`), " +
            "KEY `id_buy_listing` (`id_buy_listing`), " +
            "INDEX (`goods_id`), " +
            "INDEX (`id_buy_listing`), " +
            "FOREIGN KEY (`goods_id`) REFERENCES " + GOODS_TABLE_NAME + "(`goods_id`) " +
            "ON DELETE RESTRICT " + 
            "ON UPDATE CASCADE, " +
            "FOREIGN KEY (`id_buy_listing`) REFERENCES " + BUY_LISITNG_TABLE_NAME + "(`id_buy_listing`) " +
            "ON DELETE CASCADE " +
            "ON UPDATE CASCADE " + // po smazání pøíjemky smaž i všechny položky
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_czech_ci COMMENT='Polozky prijemky' "; 

        stm.executeUpdate(cmd);

        /* Vytvoøí tabulku pøehledu výdejek */
        cmd = "CREATE TABLE IF NOT EXISTS `" + SALE_LISTING_TABLE_NAME + "` ( " + 
            "`id_sale_listing` int(10) unsigned NOT NULL auto_increment, " +
            "`number` int(10) unsigned NOT NULL default '1', " +  
            "`date` datetime NOT NULL default '0000-00-00 00:00:00', " +
            "`cust_id` int(10) unsigned default '0', " + // Dodavatele zaèínají od è. 1, nula signalizuje neplatnou relaci
            "`total_pc_dph` decimal(20,2) UNSIGNED NOT NULL default '0.00', " +
            "`total_dph` decimal(20,2) UNSIGNED NOT NULL default '0.00', " +
            "`total_pc` decimal(20,2) UNSIGNED NOT NULL default '0.00', " +
            "`reduction` decimal(20,2) NOT NULL default '0.00', " +
            "`author` varchar(50) NOT NULL default '', " +
            "`user_id` int unsigned,  " + 
            "`use_price` INTEGER, " + 
            "PRIMARY KEY  (`id_sale_listing`)," +
            "KEY `cust_id` (`cust_id`), " +
            "KEY `user_id` (`user_id`), " + 
            "INDEX (`cust_id`), " +
            "INDEX (`user_id`), " +     
            "FOREIGN KEY (`cust_id`) REFERENCES `" + CUSTOMER_TABLE_NAME + "`(`cust_id`) " +
            "ON DELETE RESTRICT " + 
            "ON UPDATE CASCADE, " + 
            "FOREIGN KEY (`user_id`) REFERENCES `" + ACCOUNT_TABLE_NAME + "`(`user_id`) " +
            "ON DELETE SET NULL " + 
            "ON UPDATE CASCADE " + 
            " ) " +
            "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_czech_ci COMMENT='Prehled výdejek' ";
        stm.executeUpdate(cmd);

        cmd = "CREATE TABLE IF NOT EXISTS `" + SALE_TABLE_NAME + "` ( " +
            "`id_sale` int(10) unsigned NOT NULL auto_increment, " + 
            "`id_sale_listing` int(10) unsigned NOT NULL default '1', " + 
            "`goods_id` varchar(20) character set utf8 default '?', " +
            "`name` text NOT NULL, " +
            "`dph` int(10) unsigned NOT NULL default '0', " +
            "`pc` decimal(12,2) NOT NULL default '0.00', " +
            "`quantity` DECIMAL(12,5) unsigned NOT NULL default '0', " +
            "`unit` varchar(5) NOT NULL default '', " +
            "PRIMARY KEY  (`id_sale`), " +
            "KEY `goods_id` (`goods_id`), " +
            "KEY `id_sale_listing` (`id_sale_listing`), " +
            "INDEX (`goods_id`), " +
            "INDEX (`id_sale_listing`), " +
            "FOREIGN KEY (`goods_id`) REFERENCES `" + GOODS_TABLE_NAME + "`(`goods_id`) " +
            "ON DELETE RESTRICT " + 
            "ON UPDATE CASCADE, " +
            "FOREIGN KEY (`id_sale_listing`) REFERENCES `" + SALE_LISTING_TABLE_NAME + "`(`id_sale_listing`) " +
            "ON DELETE CASCADE " +
            "ON UPDATE CASCADE " + 
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_czech_ci COMMENT='Polozky výdejky' "; 

        stm.executeUpdate(cmd);
        
        /**
         *  Tabulka pøehledù inventur
         */
       cmd = "CREATE TABLE IF NOT EXISTS `" + STOCKING_LISTING_TABLE_NAME + "` ( " + 
          "`id_stocking_listing` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, " + 
          "`number` INTEGER UNSIGNED NOT NULL DEFAULT 0, " + 
          "`date` DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00', " + 
          "`difer` DECIMAL(20, 2) NOT NULL DEFAULT 0, " + 
          "`author` VARCHAR(50) NOT NULL DEFAULT '', " + 
          "`user_id` INTEGER UNSIGNED, " + 
          "`text` VARCHAR(255) NOT NULL DEFAULT '', " + 
          "`is_lock` BOOLEAN NOT NULL DEFAULT 0, " + 
          "`use_price` INTEGER, " + 
          "PRIMARY KEY(`id_stocking_listing`), " + 
          "KEY `user_id` (`user_id`), " +
          "INDEX (`user_id`), " +
          "CONSTRAINT `user_id` FOREIGN KEY `user_id` (`user_id`) " +
           "REFERENCES `" + ACCOUNT_TABLE_NAME + "` (`user_id`) " +
           "ON DELETE SET NULL " +
           "ON UPDATE CASCADE " +
          ") " +
          "ENGINE = InnoDB " +
          "CHARACTER SET utf8 COLLATE utf8_czech_ci " +
          "COMMENT = 'Tabulka pøehledù inventur' ";
        stm.executeUpdate(cmd);

        cmd = "CREATE TABLE IF NOT EXISTS `" + STOCKING_TABLE_NAME + "` ( " +
          "`id_stocking` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, " +
          "`id_stocking_listing` INTEGER UNSIGNED NOT NULL DEFAULT 0, " +
          "`goods_id` varchar(20) character set utf8 DEFAULT '?', " +
          "`name` VARCHAR(255) NOT NULL DEFAULT '', " +
          "`dph` INTEGER UNSIGNED NOT NULL DEFAULT 19, " +
          "`price` DECIMAL(12,2) NOT NULL DEFAULT 0, " +
          "`difer` DECIMAL(12,5) NOT NULL DEFAULT 0, " +
          "`unit` CHAR(5) NOT NULL DEFAULT 'ks', " +
          "PRIMARY KEY(`id_stocking`), " +
          "KEY `goods_id` (`goods_id`), " +
          "KEY `id_stocking_listing` (`id_stocking_listing`), " +
          "INDEX (`goods_id`), " +
          "INDEX (`id_stocking_listing`), " +
          "CONSTRAINT `goods_id` FOREIGN KEY `goods_id` (`goods_id`) " +
            "REFERENCES `" + GOODS_TABLE_NAME + "` (`goods_id`) " +
            "ON DELETE RESTRICT " +
            "ON UPDATE CASCADE, " +
          "CONSTRAINT `id_stocking_listing` FOREIGN KEY `id_stocking_listing` (`id_stocking_listing`) " +
            "REFERENCES `" + STOCKING_LISTING_TABLE_NAME + "` (`id_stocking_listing`) " +
            "ON DELETE CASCADE " +
            "ON UPDATE CASCADE " +
          ") " +
          "ENGINE = InnoDB " +
          "DEFAULT CHARSET=utf8 COLLATE utf8_czech_ci " +
          "COMMENT = 'Tabulka položek inventur' ";
        stm.executeUpdate(cmd);
        
        // Tabulka rekapitulací
        cmd = "CREATE TABLE IF NOT EXISTS `" + RECAP_MONTH_TABLE_NAME + "` ( " +
          "`date_month` DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00', " +
          "`profit` DECIMAL(20, 2) NOT NULL DEFAULT 0, " +
          "`profit_and_dph` DECIMAL(20, 2) NOT NULL DEFAULT 0, " +
          "`profit_dph` DECIMAL(20, 2) NOT NULL DEFAULT 0, " +
          "`release` DECIMAL(20,2) NOT NULL DEFAULT 0, " +
          "`release_and_dph` DECIMAL(20,2) NOT NULL DEFAULT 0, " +
          "`release_dph` DECIMAL(20,2) NOT NULL DEFAULT 0, " +
          "`text` VARCHAR(255) NOT NULL DEFAULT '', " +
          "`author` VARCHAR(50) NOT NULL DEFAULT '', " +
          "`user_id` INTEGER UNSIGNED DEFAULT 0,  " + 
          "PRIMARY KEY(`date_month`), " +
          "KEY `user_id_fk` (`user_id`), " +
          "INDEX (`user_id`), " +
          "CONSTRAINT `user_id_constr` FOREIGN KEY `user_id_fk` (`user_id`) " +
            "REFERENCES " + ACCOUNT_TABLE_NAME + " (`user_id`) " +
            "ON DELETE SET NULL " +
            "ON UPDATE CASCADE " +
          ") " +
          "ENGINE = InnoDB " +
          "DEFAULT CHARSET=utf8 COLLATE utf8_czech_ci " +
          "COMMENT = 'Tabulka mìsíèních rekapitulací' ";
        stm.executeUpdate(cmd);

         cmd = "CREATE TABLE IF NOT EXISTS `" + PRICELIST_TABLE_NAME + "` ( " + 
          "`id` int(10) unsigned NOT NULL auto_increment, " + 
          "`sup_id` int(10) unsigned default '0', " + 
          "`pc_a` decimal(12,2) NOT NULL default '1.00', " + 
          "`pc_b` decimal(12,2) NOT NULL default '1.00', " + 
          "`pc_c` decimal(12,2) NOT NULL default '1.00', " + 
          "`pc_d` decimal(12,2) NOT NULL default '1.00', " + 
          "PRIMARY KEY  (`id`), " + 
          "KEY `pricelist_sup` (`sup_id`), " + 
            "CONSTRAINT `pricelist_sup` FOREIGN KEY (`sup_id`) REFERENCES `"+SUPLIER_TABLE_NAME + "` (`sup_id`) " +
            "ON DELETE CASCADE " +
            "ON UPDATE CASCADE " + 
          ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci COMMENT='Tabulka ceníkù'";

         stm.executeUpdate(cmd);
      
        cmd = "CREATE TABLE IF NOT EXISTS `" + ABOUT_TABLE_NAME + "` ( " + 
            "`id` INTEGER UNSIGNED  AUTO_INCREMENT, " + 
            "`name` VARCHAR(256) NOT NULL, " +
            "`person` VARCHAR(50) NOT NULL DEFAULT '', " +
            "`send_street` VARCHAR(50) NOT NULL DEFAULT '', " + 
            "`send_city` VARCHAR(30) NOT NULL DEFAULT '', " + 
            "`send_PSC` CHAR(5) NOT NULL DEFAULT '0', " +
            "`pay_street` VARCHAR(50) NOT NULL DEFAULT '', " + 
            "`pay_city` VARCHAR(30) NOT NULL DEFAULT '', " + 
            "`pay_PSC` CHAR(5) NOT NULL DEFAULT '0', " +
            "`tel` CHAR(255) NOT NULL DEFAULT '+420 ', " +
            "`fax` CHAR(20) NOT NULL DEFAULT '+420 ', " +
            "`mail` VARCHAR(50) NOT NULL DEFAULT '@', " +
            "`web` VARCHAR(100) NOT NULL DEFAULT 'http://', " +
            "`ICO` VARCHAR(20) NOT NULL DEFAULT '0', " +
            "`DIC` VARCHAR(20) NOT NULL DEFAULT '', " +
            "`is_DPH` TINYINT UNSIGNED NOT NULL DEFAULT 1, " +
            "`account` VARCHAR(20) NOT NULL DEFAULT '0', " +
            "`logopath` LONGBLOB NOT NULL, " +
            "`note` TEXT, " +    
            "PRIMARY KEY(`id`)" +
            ") " +
            "ENGINE = InnoDB " + 
            "CHARACTER SET utf8 COLLATE utf8_general_ci " + 
            "COMMENT = 'Karta odbìratele' ";
        stm.executeUpdate(cmd);

        
        // initial "about" record
        cmd = "insert into `" + ABOUT_TABLE_NAME + "`"
                + "(name, person, send_street, send_city, send_PSC, "
                + "pay_street, pay_city, pay_PSC, tel, fax, mail, web, ICO,"
                + "DIC, is_DPH, account, logopath) "
                + "VALUES ("
                + "'','','','',0,'','','','','','@','','','',1,'','')";
        stm.executeUpdate(cmd);
        
        stm.close();
        
    }
    
    /**
     * Spustí SQL pøíkaz
     * @param sql slq pøíkaz
     * @throws java.sql.SQLException chyba vykonání pøíkazu
     */
    private void runSQLUpdate(String sql) throws SQLException {
        Connection con = getCurrentConnection();    
        
        Statement stm = con.createStatement();
        stm.executeUpdate(sql); 
        
        stm.close();
    }
}
