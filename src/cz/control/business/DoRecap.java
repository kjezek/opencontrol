/*
 * DoRecap.java
 *
 * Vytvoøeno 8. bøezen 2006, 12:03
 *
 * Autor: Kamil Jeek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;

import cz.control.data.Client;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static cz.control.database.DatabaseAccess.*;

/**
 * Program Control - Skladovı systém
 *
 * Tøída vytváøí rekapitulace skladu
 *
 * @author Kamil Jeek
 * 
 * (C) 2006, ver. 1.0
 */
public class DoRecap {
    private Client client;
    private String text = "";
    private Calendar date = null;
    
    // Hodnotu potøebné získat z databáze pro vytvoøení rekapitulace
    // hodnoty zisku
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal profitAndDPH = BigDecimal.ZERO;
    private BigDecimal profitDPH = BigDecimal.ZERO;
    // hodnoty pouitıch prostøedkù
    private BigDecimal release = BigDecimal.ZERO;
    private BigDecimal releaseAndDPH = BigDecimal.ZERO;
    private BigDecimal releaseDPH = BigDecimal.ZERO;
    // Hodnoty zaèátku období
    private BigDecimal start = BigDecimal.ZERO;
    private BigDecimal startAndDPH = BigDecimal.ZERO;
    private BigDecimal startDPH = BigDecimal.ZERO;
    // Hodnoty na konci období
    private BigDecimal end = BigDecimal.ZERO;
    private BigDecimal endAndDPH = BigDecimal.ZERO;
    private BigDecimal endDPH = BigDecimal.ZERO;
    
    //Vısledná rekapitulace
/*    private MonthRecap recap = null;*/
    
    private Recaps recaps = new Recaps();
        
    /**
     * Vytvoøí novou instanci DoRecap
     */
    DoRecap(Client client) {
        this.client = client;
    }
    
    /**
     * Provede mìsièní rekapitualci vyuitím nastavenıch hodnot.
     * Rekapitulaci zapíše do databáze.
     * @throws java.lang.Exception 
     */
    public void doMonthRecap() throws Exception {
        // jestlie není vyplnìno datum
        if (date == null) {
            throw new Exception("Není vyplnìno datum, pro které provést uzávìrku");
        }
        
        // Jestlie v systému existuje stejná pøíjemka
        if (recaps.readMonthRecap(date).getDate() != null) {
            throw new Exception("Tento mìsíc ji byl uzavøen");
        }
        
/*        readStart();*/
        
        // Budeme poèítat ne z prodejních cen.. 
        //readProfitAsPC();
        //.. ale z nákupních cen na skladì
        readProfitAsNC();
        readRelease();
        
/*        computeEnd();*/
        
        // Vısledná rekapitulace
/*        recap = new MonthRecap(date, 
                start.multiply(Store.CENT).longValue(),
                startAndDPH.multiply(Store.CENT).longValue(),
                startDPH.multiply(Store.CENT).longValue(),
                profit.multiply(Store.CENT).longValue(),
                profitAndDPH.multiply(Store.CENT).longValue(),
                profitDPH.multiply(Store.CENT).longValue(),
                release.multiply(Store.CENT).longValue(),
                releaseAndDPH.multiply(Store.CENT).longValue(),
                releaseDPH.multiply(Store.CENT).longValue(),
                end.multiply(Store.CENT).longValue(),
                endAndDPH.multiply(Store.CENT).longValue(),
                endDPH.multiply(Store.CENT).longValue(),
                text,
                client.getName(),
                client.getUserId()
                );*/
        
        // Zapíše do databáze vytvoøenou rekapitulaci
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement("" +
                "INSERT INTO " + RECAP_MONTH_TABLE_NAME + " " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        pstm.setTimestamp(1, new java.sql.Timestamp(date.getTimeInMillis()));
//        pstm.setBigDecimal(2, start);
//        pstm.setBigDecimal(3, startAndDPH);
//        pstm.setBigDecimal(4, startDPH);
        pstm.setBigDecimal(2, profit);
        pstm.setBigDecimal(3, profitAndDPH);
        pstm.setBigDecimal(4, profitDPH);
        pstm.setBigDecimal(5, release);
        pstm.setBigDecimal(6, releaseAndDPH);
        pstm.setBigDecimal(7, releaseDPH);
        pstm.setString(8, text);
        pstm.setString(9, client.getName());
        if (client.getUserId() == -1)  // Vıchozí pøihlášení 
            pstm.setObject(10, null); // nastav na null
        else
            pstm.setInt(10, client.getUserId()); // nastav na skuteèné èíslo uivatele
        pstm.executeUpdate();
        
    }
    
    /**
     * Naète hodnoty z pøedešlého období, které pouije jako poèáteèní období
     */
    // Oprava: V databázi nebudeme uchovávat poèáteèní stav, nebo se dá zjistit z pøedchozích rekapitulací
/*    private void readStart() throws SQLException {
        // Vyber nejbliší døívejší rekapitulaci
        String command = "SELECT MAX(date_month), " + // vyber nejvyšší datum
                "start + profit - release, " +
                "start_and_dph + profit_and_dph - release_and_dph, " +
                "start_dph + profit_dph - release_dph " + 
                "FROM ( " +
                    // Vyber nejbliší niší mìsíc
                    "SELECT * FROM " + RECAP_MONTH_TABLE_NAME + " " +
                    "WHERE DATE(date_month) < ? " +
                    " ) AS sub_tab GROUP BY YEAR(date_month)";

                // Verze poèítající pouze s aktuálním rokem    
                // "WHERE MONTH(date_month) < " + (date.get(Calendar.MONTH)+1) + " AND " +
                // "YEAR(date_month) = " + date.get(Calendar.YEAR) + " " +
        
        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        stm.setTimestamp(1, new java.sql.Timestamp(date.getTimeInMillis()));
        ResultSet rs = stm.executeQuery(); 
        
        // Poèáteèní hodnota je nulová
        start = BigDecimal.ZERO;
        startAndDPH = BigDecimal.ZERO;
        startDPH = BigDecimal.ZERO;
        
        // Jestlie nìco naèetl, ulo do promìnnıch
        if (rs.next()) {
            start = rs.getBigDecimal(2);
            startAndDPH = rs.getBigDecimal(3);
            startDPH = rs.getBigDecimal(4);
        }
    
        rs.close();
        stm.close();        
    } */
    
    /**
     *  Naète zisk s prodejek za dané období 
     *  Poèítá cenu, za kterou bylo prodáno
     */
    private void readProfitAsPC() throws SQLException {
        // Seèti zisk ze všech prodejek
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT " +
                "SUM(total_pc_dph) AS sum_pc_dph, " +
                "SUM(total_dph) AS sum_dph, " +
                "SUM(total_pc) AS sum_pc " +
                "FROM " + SALE_LISTING_TABLE_NAME + " " +
                "WHERE MONTH(date) = " + (date.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + (date.get(Calendar.YEAR)) + " " +
                "HAVING sum_pc_dph IS NOT NULL AND " +
                "sum_dph IS NOT NULL AND sum_pc IS NOT NULL"; //eviduj pouze nenulové poèty
        ResultSet rs = stm.executeQuery(command); 
        
        // Poèáteèní hodnota je nulová
        profit = BigDecimal.ZERO;
        profitAndDPH = BigDecimal.ZERO;
        profitDPH = BigDecimal.ZERO;
        
        // Jestlie nìco naèetl, ulo do promìnnıch
        if (rs.next()) {
            profitAndDPH = rs.getBigDecimal(1);
            profitDPH = rs.getBigDecimal(2);
            profit = rs.getBigDecimal(3);
        }
    
        rs.close();
        stm.close();
    }
    
    /**
     *  Naète celkovou cenu prodaného zboí z nákupních cen uvedenıch u zboí
     *  Poèítá se nákupní cena, která je souèasnì uvedena u zboí. Proto nemusí
     *  bıt shodná s nákupní cenou, která byla uvedena u zboí v dobì, kdy probìhl prodej
     */
    private void readProfitAsNC() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT " +
                "SUM(store.nc * sale.quantity * sale.dph / 100 + store.nc * sale.quantity) AS sum_nc_dph, " +
                "SUM(store.nc * sale.quantity * sale.dph / 100) AS sum_dph, " +
                "SUM(store.nc * sale.quantity) AS sum_nc " +
                "FROM " + SALE_TABLE_NAME + " as sale, " + GOODS_TABLE_NAME + " as store, " +
                  SALE_LISTING_TABLE_NAME + " as sale_list " +
                "WHERE " +
                "sale.goods_id = store.goods_id AND " + //spoje zboí z poloky pøíjemky s tabulkou skladu
                "sale_list.id_sale_listing = sale.id_sale_listing AND " +  // spoje pøehled vıdejky s polokami vıdejky
                "MONTH(date) = " + (date.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + (date.get(Calendar.YEAR)) + " " +
                "" +
                "HAVING sum_nc_dph IS NOT NULL AND " +
                "sum_dph IS NOT NULL AND sum_nc IS NOT NULL"; //eviduj pouze nenulové poèty
        ResultSet rs = stm.executeQuery(command); 
        
        // Poèáteèní hodnota je nulová
        profit = BigDecimal.ZERO;
        profitAndDPH = BigDecimal.ZERO;
        profitDPH = BigDecimal.ZERO;
        
        // Jestlie nìco naèetl, ulo do promìnnıch
        if (rs.next()) {
            profitAndDPH = rs.getBigDecimal(1);
            profitDPH = rs.getBigDecimal(2);
            profit = rs.getBigDecimal(3);
        }
    
        rs.close();
        stm.close();
    }    
    
    /**
     *  Naète objem pouitıch prostøedkù ze všech pøíjemek
     */
    private void readRelease() throws SQLException {
        // Seèti poèet poskytnutıch prostøedkù
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT " +
                "SUM(total_nc_dph) AS sum_nc_dph, " +
                "SUM(total_dph) AS sum_dph, " +
                "SUM(total_nc) AS sum_nc " +
                "FROM " + BUY_LISITNG_TABLE_NAME + " " +
                "WHERE MONTH(date) = " + (date.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + (date.get(Calendar.YEAR)) + " " +
                "HAVING sum_nc_dph IS NOT NULL AND " +
                "sum_dph IS NOT NULL AND sum_nc IS NOT NULL";
        ResultSet rs = stm.executeQuery(command); 
        
        // Poèáteèní hodnota je nulová
        release = BigDecimal.ZERO;
        releaseAndDPH = BigDecimal.ZERO;
        releaseDPH = BigDecimal.ZERO;
        
        // Jestlie nìco naèetl, ulo do promìnnıch
        if (rs.next()) {
            releaseAndDPH = rs.getBigDecimal(1);
            releaseDPH = rs.getBigDecimal(2);
            release = rs.getBigDecimal(3);
        }
    
        rs.close();
        stm.close();        
    }
    
    /**
     *  Vypoète koneèné hodnoty pro dané období
     */
    // Oprava: V databázi nebudem uchovávat koncovı stav. Dá se dopoèítat z pøedchozích uzávìrek
 /*   private void computeEnd() {

        end = start.add(profit).subtract(release);
        endAndDPH = startAndDPH.add(profitAndDPH).subtract(releaseAndDPH);
        endDPH = startDPH.add(profitDPH).subtract(releaseDPH);
        
    }*/
    
    /**
     * Vyame z databáze mìsíèní prekapitulaci daného data
     */
    public void storno() throws SQLException {
        recaps.deleteMonthRecap(date);
    }
    
    /*
     * Vymae z databáze rekapitulace za celı rok, tedy 12 mìsícù
     */
    public void stornoYear() throws SQLException {
        recaps.deleteYearRecap(date);
    }
    
    /**
     * Vcrací textovou poznámku k rekapitulaci
     * @return textovou poznámku k rekapitulaci
     */
    public String getText() {
        return text;
    }

    /**
     * Nastavuje textovou poznámku inventury
     * @param text Textová poznámka inventury
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Vrací datum rekapitulace
     * @return datum rekapitulace
     */
    public Calendar getDate() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(cal.getTimeInMillis());
        return cal;
    }

    /**
     * Nastavuje datum rekapitulace
     * Datum však upravuje, tak aby den byl vdy nastaven na "1",
     * nebo kadı mìsíc má minimálnì jeden den.
     * To zabraòuje pokusu o vytvoøení rekapitulace napø v den 30. února
     * @param date datum rekapitulace
     */
    public void setDate(Calendar date) {
        this.date = new GregorianCalendar();
        this.date.set(Calendar.DAY_OF_MONTH, 1);
        this.date.set(Calendar.MONTH, date.get(Calendar.MONTH));
        this.date.set(Calendar.YEAR, date.get(Calendar.YEAR));
    }

    /**
     *  Vrací rekapitulaci, kterou vytvoøol po volání metody 
     *  <code>doMontRecap()</code>
     *  @return vytvoøená rekapitulace, nebo null jestlie není ještì rekapitulace vytvoøena
     */
/*    public MonthRecap getRecap() {
        return recap;
    }*/

    
}
