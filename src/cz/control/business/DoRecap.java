/*
 * DoRecap.java
 *
 * Vytvo�eno 8. b�ezen 2006, 12:03
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� rekapitulace skladu
 *
 * @author Kamil Je�ek
 * 
 * (C) 2006, ver. 1.0
 */
public class DoRecap {
    private Client client;
    private String text = "";
    private Calendar date = null;
    
    // Hodnotu pot�ebn� z�skat z datab�ze pro vytvo�en� rekapitulace
    // hodnoty zisku
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal profitAndDPH = BigDecimal.ZERO;
    private BigDecimal profitDPH = BigDecimal.ZERO;
    // hodnoty pou�it�ch prost�edk�
    private BigDecimal release = BigDecimal.ZERO;
    private BigDecimal releaseAndDPH = BigDecimal.ZERO;
    private BigDecimal releaseDPH = BigDecimal.ZERO;
    // Hodnoty za��tku obdob�
    private BigDecimal start = BigDecimal.ZERO;
    private BigDecimal startAndDPH = BigDecimal.ZERO;
    private BigDecimal startDPH = BigDecimal.ZERO;
    // Hodnoty na konci obdob�
    private BigDecimal end = BigDecimal.ZERO;
    private BigDecimal endAndDPH = BigDecimal.ZERO;
    private BigDecimal endDPH = BigDecimal.ZERO;
    
    //V�sledn� rekapitulace
/*    private MonthRecap recap = null;*/
    
    private Recaps recaps = new Recaps();
        
    /**
     * Vytvo�� novou instanci DoRecap
     */
    DoRecap(Client client) {
        this.client = client;
    }
    
    /**
     * Provede m�si�n� rekapitualci vyu�it�m nastaven�ch hodnot.
     * Rekapitulaci zap�e do datab�ze.
     * @throws java.lang.Exception 
     */
    public void doMonthRecap() throws Exception {
        // jestli�e nen� vypln�no datum
        if (date == null) {
            throw new Exception("Nen� vypln�no datum, pro kter� prov�st uz�v�rku");
        }
        
        // Jestli�e v syst�mu existuje stejn� p��jemka
        if (recaps.readMonthRecap(date).getDate() != null) {
            throw new Exception("Tento m�s�c ji� byl uzav�en");
        }
        
/*        readStart();*/
        
        // Budeme po��tat ne z prodejn�ch cen.. 
        //readProfitAsPC();
        //.. ale z n�kupn�ch cen na sklad�
        readProfitAsNC();
        readRelease();
        
/*        computeEnd();*/
        
        // V�sledn� rekapitulace
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
        
        // Zap�e do datab�ze vytvo�enou rekapitulaci
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
        if (client.getUserId() == -1)  // V�choz� p�ihl�en� 
            pstm.setObject(10, null); // nastav na null
        else
            pstm.setInt(10, client.getUserId()); // nastav na skute�n� ��slo u�ivatele
        pstm.executeUpdate();
        
    }
    
    /**
     * Na�te hodnoty z p�ede�l�ho obdob�, kter� pou�ije jako po��te�n� obdob�
     */
    // Oprava: V datab�zi nebudeme uchov�vat po��te�n� stav, nebo� se d� zjistit z p�edchoz�ch rekapitulac�
/*    private void readStart() throws SQLException {
        // Vyber nejbli��� d��vej�� rekapitulaci
        String command = "SELECT MAX(date_month), " + // vyber nejvy��� datum
                "start + profit - release, " +
                "start_and_dph + profit_and_dph - release_and_dph, " +
                "start_dph + profit_dph - release_dph " + 
                "FROM ( " +
                    // Vyber nejbli��� ni��� m�s�c
                    "SELECT * FROM " + RECAP_MONTH_TABLE_NAME + " " +
                    "WHERE DATE(date_month) < ? " +
                    " ) AS sub_tab GROUP BY YEAR(date_month)";

                // Verze po��taj�c� pouze s aktu�ln�m rokem    
                // "WHERE MONTH(date_month) < " + (date.get(Calendar.MONTH)+1) + " AND " +
                // "YEAR(date_month) = " + date.get(Calendar.YEAR) + " " +
        
        PreparedStatement stm = DatabaseAccess.getCurrentConnection().prepareStatement(command);
        stm.setTimestamp(1, new java.sql.Timestamp(date.getTimeInMillis()));
        ResultSet rs = stm.executeQuery(); 
        
        // Po��te�n� hodnota je nulov�
        start = BigDecimal.ZERO;
        startAndDPH = BigDecimal.ZERO;
        startDPH = BigDecimal.ZERO;
        
        // Jestli�e n�co na�etl, ulo� do prom�nn�ch
        if (rs.next()) {
            start = rs.getBigDecimal(2);
            startAndDPH = rs.getBigDecimal(3);
            startDPH = rs.getBigDecimal(4);
        }
    
        rs.close();
        stm.close();        
    } */
    
    /**
     *  Na�te zisk s prodejek za dan� obdob� 
     *  Po��t� cenu, za kterou bylo prod�no
     */
    private void readProfitAsPC() throws SQLException {
        // Se�ti zisk ze v�ech prodejek
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT " +
                "SUM(total_pc_dph) AS sum_pc_dph, " +
                "SUM(total_dph) AS sum_dph, " +
                "SUM(total_pc) AS sum_pc " +
                "FROM " + SALE_LISTING_TABLE_NAME + " " +
                "WHERE MONTH(date) = " + (date.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + (date.get(Calendar.YEAR)) + " " +
                "HAVING sum_pc_dph IS NOT NULL AND " +
                "sum_dph IS NOT NULL AND sum_pc IS NOT NULL"; //eviduj pouze nenulov� po�ty
        ResultSet rs = stm.executeQuery(command); 
        
        // Po��te�n� hodnota je nulov�
        profit = BigDecimal.ZERO;
        profitAndDPH = BigDecimal.ZERO;
        profitDPH = BigDecimal.ZERO;
        
        // Jestli�e n�co na�etl, ulo� do prom�nn�ch
        if (rs.next()) {
            profitAndDPH = rs.getBigDecimal(1);
            profitDPH = rs.getBigDecimal(2);
            profit = rs.getBigDecimal(3);
        }
    
        rs.close();
        stm.close();
    }
    
    /**
     *  Na�te celkovou cenu prodan�ho zbo�� z n�kupn�ch cen uveden�ch u zbo��
     *  Po��t� se n�kupn� cena, kter� je sou�asn� uvedena u zbo��. Proto nemus�
     *  b�t shodn� s n�kupn� cenou, kter� byla uvedena u zbo�� v dob�, kdy prob�hl prodej
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
                "sale.goods_id = store.goods_id AND " + //spoje zbo�� z polo�ky p��jemky s tabulkou skladu
                "sale_list.id_sale_listing = sale.id_sale_listing AND " +  // spoje p�ehled v�dejky s polo�kami v�dejky
                "MONTH(date) = " + (date.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date) = " + (date.get(Calendar.YEAR)) + " " +
                "" +
                "HAVING sum_nc_dph IS NOT NULL AND " +
                "sum_dph IS NOT NULL AND sum_nc IS NOT NULL"; //eviduj pouze nenulov� po�ty
        ResultSet rs = stm.executeQuery(command); 
        
        // Po��te�n� hodnota je nulov�
        profit = BigDecimal.ZERO;
        profitAndDPH = BigDecimal.ZERO;
        profitDPH = BigDecimal.ZERO;
        
        // Jestli�e n�co na�etl, ulo� do prom�nn�ch
        if (rs.next()) {
            profitAndDPH = rs.getBigDecimal(1);
            profitDPH = rs.getBigDecimal(2);
            profit = rs.getBigDecimal(3);
        }
    
        rs.close();
        stm.close();
    }    
    
    /**
     *  Na�te objem pou�it�ch prost�edk� ze v�ech p��jemek
     */
    private void readRelease() throws SQLException {
        // Se�ti po�et poskytnut�ch prost�edk�
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
        
        // Po��te�n� hodnota je nulov�
        release = BigDecimal.ZERO;
        releaseAndDPH = BigDecimal.ZERO;
        releaseDPH = BigDecimal.ZERO;
        
        // Jestli�e n�co na�etl, ulo� do prom�nn�ch
        if (rs.next()) {
            releaseAndDPH = rs.getBigDecimal(1);
            releaseDPH = rs.getBigDecimal(2);
            release = rs.getBigDecimal(3);
        }
    
        rs.close();
        stm.close();        
    }
    
    /**
     *  Vypo�te kone�n� hodnoty pro dan� obdob�
     */
    // Oprava: V datab�zi nebudem uchov�vat koncov� stav. D� se dopo��tat z p�edchoz�ch uz�v�rek
 /*   private void computeEnd() {

        end = start.add(profit).subtract(release);
        endAndDPH = startAndDPH.add(profitAndDPH).subtract(releaseAndDPH);
        endDPH = startDPH.add(profitDPH).subtract(releaseDPH);
        
    }*/
    
    /**
     * Vyam�e z datab�ze m�s��n� prekapitulaci dan�ho data
     */
    public void storno() throws SQLException {
        recaps.deleteMonthRecap(date);
    }
    
    /*
     * Vyma�e z datab�ze rekapitulace za cel� rok, tedy 12 m�s�c�
     */
    public void stornoYear() throws SQLException {
        recaps.deleteYearRecap(date);
    }
    
    /**
     * Vcrac� textovou pozn�mku k rekapitulaci
     * @return textovou pozn�mku k rekapitulaci
     */
    public String getText() {
        return text;
    }

    /**
     * Nastavuje textovou pozn�mku inventury
     * @param text Textov� pozn�mka inventury
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Vrac� datum rekapitulace
     * @return datum rekapitulace
     */
    public Calendar getDate() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(cal.getTimeInMillis());
        return cal;
    }

    /**
     * Nastavuje datum rekapitulace
     * Datum v�ak upravuje, tak aby den byl v�dy nastaven na "1",
     * nebo� ka�d� m�s�c m� minim�ln� jeden den.
     * To zabra�uje pokusu o vytvo�en� rekapitulace nap� v den 30. �nora
     * @param date datum rekapitulace
     */
    public void setDate(Calendar date) {
        this.date = new GregorianCalendar();
        this.date.set(Calendar.DAY_OF_MONTH, 1);
        this.date.set(Calendar.MONTH, date.get(Calendar.MONTH));
        this.date.set(Calendar.YEAR, date.get(Calendar.YEAR));
    }

    /**
     *  Vrac� rekapitulaci, kterou vytvo�ol po vol�n� metody 
     *  <code>doMontRecap()</code>
     *  @return vytvo�en� rekapitulace, nebo null jestli�e nen� je�t� rekapitulace vytvo�ena
     */
/*    public MonthRecap getRecap() {
        return recap;
    }*/

    
}
