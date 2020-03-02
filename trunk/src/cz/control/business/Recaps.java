/*
 * Recaps.java
 *
 * Vytvo�eno 8. b�ezen 2006, 17:55
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;

import cz.control.data.MonthRecap;
import cz.control.data.YearRecap;
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
public class Recaps {
    
    private ArrayList<YearRecap> yearsRacaps = new ArrayList<YearRecap>();
    private HashMap<Integer, ArrayList<MonthRecap>> monthRecaps = new HashMap<Integer, ArrayList<MonthRecap>>();
    
    //pro�nn� pro v�po�et po��te�n�ho stavu m�s�ce z koncov�ho stavu p�ede�l�ho m�s�ce
    private long startMonth = 0;
    private long startAndDPHMonth = 0;
    private long startDPHMonth = 0;
    
    /**
     * Vytvo�� novou instanci Recaps
     */
    public Recaps() {
    }
    
    /**
     * Vyma�e z datab�se m�s��n� rekapitulaci odpov�daj�c� dan�mu datu
     * @param date datum ud�vaj�c� m�s�c, kter� se m� vymazat
     */
    void deleteMonthRecap(Calendar date) throws SQLException {
        Calendar tmp = new GregorianCalendar();
        tmp.set(Calendar.DAY_OF_MONTH, 1);
        tmp.set(Calendar.MONTH, date.get(Calendar.MONTH));
        tmp.set(Calendar.YEAR, date.get(Calendar.YEAR));
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + RECAP_MONTH_TABLE_NAME + " " +
                "WHERE MONTH(date_month) = " + (tmp.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date_month) = " + tmp.get(Calendar.YEAR) + " ";
        stm.executeUpdate(command);
        stm.close();    
    }
    
    /**
     * Vyma�e z datab�ze rekapitulace za jeden rok. Tedy vyma�e 12 m�s�c� dan�ho roku
     * @param date rok, kter� se m� vymazat
     * @throws java.sql.SQLException chyba datab�ze
     */
    void deleteYearRecap(Calendar date) throws SQLException {
        Calendar tmp = new GregorianCalendar();
        tmp.set(Calendar.DAY_OF_MONTH, 1);
        tmp.set(Calendar.MONTH, date.get(Calendar.MONTH));
        tmp.set(Calendar.YEAR, date.get(Calendar.YEAR));
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "DELETE FROM " + RECAP_MONTH_TABLE_NAME + " " +
                "WHERE " +
                "YEAR(date_month) = " + tmp.get(Calendar.YEAR) + " ";
        stm.executeUpdate(command);
        stm.close();         
    }
    
    /**
     * Na�te m�s��n� uz�v�rku odpov�daj�c� dan�mu datu z datab�ze. 
     * Vypo�te rekapitulaci. Hodnoty po��te�n�ho stavu pou�ije z p�edchoz�h vol�n�
     * t�to metody. Je proto vhodn� volat tuto metodu pro jednotliv� m�s�ce tak jak
     * jdou za sebou v roce, od Ledna do Prosince
     * @param date datum podle kter�ho se ur�� m�s�c rekapitulace
     * @throws java.sql.SQLException chyba datab�ze
     * @return rekapitulace odpov�daj�c�dan�mu datu. Nebo pr�zdn� objekt, jestli�e nen� rekapitulace vytvo�ena
     */
    MonthRecap readMonthRecap(Calendar date) throws SQLException {
        Calendar tmp = new GregorianCalendar();
        tmp.set(Calendar.DAY_OF_MONTH, 1);
        tmp.set(Calendar.MONTH, date.get(Calendar.MONTH));
        tmp.set(Calendar.YEAR, date.get(Calendar.YEAR));
        
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT * FROM " + RECAP_MONTH_TABLE_NAME + " " +
                "WHERE MONTH(date_month) = " + (tmp.get(Calendar.MONTH)+1) + " AND " +
                "YEAR(date_month) = " + tmp.get(Calendar.YEAR) + " ";
        ResultSet rs = stm.executeQuery(command);
        
        MonthRecap result = new MonthRecap();
        
        if (rs.next()) {
            long dateMillis =  rs.getTimestamp(1).getTime();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(dateMillis);
            
            // Na�ti hodnoty z datab�ze
            long profit = (new BigDecimal(rs.getDouble(2))).multiply(Store.CENT).longValue();
            long profitAndDPH = (new BigDecimal(rs.getDouble(3))).multiply(Store.CENT).longValue();
            long profitDPH = (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue();
            
            long release = (new BigDecimal(rs.getDouble(5))).multiply(Store.CENT).longValue();
            long releaseAndDPH = (new BigDecimal(rs.getDouble(6))).multiply(Store.CENT).longValue();
            long releaseDPH = (new BigDecimal(rs.getDouble(7))).multiply(Store.CENT).longValue();
            
            // Vypo�ti stav na konci obdob�
            long end = startMonth - profit + release;
            long endAndDPH = startAndDPHMonth - profitAndDPH + releaseAndDPH;
            long endDPH = startDPHMonth - profitDPH + releaseDPH;
             
            // vytvo� objekt rekapitulace 
            result = new MonthRecap(
                    calendar,
                    startMonth, startAndDPHMonth, startDPHMonth,
                    profit,profitAndDPH, profitDPH,
                    release, releaseAndDPH, releaseDPH,
                    end, endAndDPH, endDPH,
                    rs.getString(8),
                    rs.getString(9),
                    rs.getInt(10)
                    );
            
            // koncov� hodnoty nastav jako po��te�n� pro dal�� m�s�c
            startMonth = end;
            startAndDPHMonth = endAndDPH;
            startDPHMonth = endDPH;
        } 

        rs.close();
        stm.close();         
        
        return result;
    }
    
    /**
     * Vrac� seznam rok�, ke kter�m existuje n�jak� rekapitulace
     * @return seznam rok�, ke kter� existuje minim�ln� jedna m�s��n� rekapitulace
     * @throws java.sql.SQLException chyba datab�ze
     */
    public ArrayList<Integer> getAllYears() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT YEAR(date_month) FROM " + RECAP_MONTH_TABLE_NAME + " " +
                "GROUP BY YEAR(date_month) ORDER BY YEAR(date_month)";
        ResultSet rs = stm.executeQuery(command); // Vyhledej roky
        
        ArrayList<Integer> result = new ArrayList<Integer>();
        
        // Na�ti v�echny roky
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        
        rs.close();
        stm.close();   
        
        return result;
    }
    
    /**
     * Vypo�te v�echny ro�n� rekapitulace. K v�po�tu vyu�ije roky, kter� vr�tila metoda
     * <code>getAllYears</code>. Po��t� rekapitulace postupn� od nejni���ho k nejvy���mu roku.
     * Koncov� stav jednoho roku vyu�ije jako po��te�n� dal��ho roku
     * @throws java.sql.SQLException chyba datab�ze
     * @return Ve�ker� ro�n� rekapitulace
     */
    public ArrayList<YearRecap> computeAllRecaps(String author) throws SQLException {
        ArrayList<Integer> years = getAllYears(); // zjisti si v�echny roky
        Calendar date = new GregorianCalendar();
        
        //vy�isti mapu m�s��n�ch rekapitulac� a poe ro�n�ch rekapitulac�
        yearsRacaps.clear();
        monthRecaps.clear();
        startAndDPHMonth = 0;
        startDPHMonth = 0;
        startMonth = 0;
        
        // Prom�nn� pro v�po�et rekapitulace
        long start = 0, startAndDPH = 0, startDPH = 0;
        long end = 0, endAndDPH = 0, endDPH = 0;
                
        // Projdi v�echny roky ke kter�m existuj� rekapitulace
        // Pole obsahuje roky od nejni���ho k nejvy���m
        for (Integer year: years) {
            
            long profit = 0, profitAndDPH = 0, profitDPH = 0;
            long release = 0, releaseAndDPH = 0, releaseDPH = 0;
        
            // Na za��tku obdob� je start shodn� s koncem minul�ho obdob�
            start = end;
            startAndDPH = endAndDPH;
            startDPH = endDPH;
            
            // Zjisti v�echny m�s��n� rekapitulace tohoto roku podle data
            date.set(Calendar.YEAR, year);
            ArrayList<MonthRecap> months = computeMonthRecaps(date);
            
            // Projdi jednotliv� m�s�ce a vypo�ti v�sledek za cel� rok
            // se�ti zisky a v�daje
            for (MonthRecap month: months) {
                profit += month.getProfit();
                profitAndDPH += month.getProfitAndDPH();
                profitDPH += month.getProfitDPH();
                
                release += month.getRelease();
                releaseAndDPH += month.getReleaseAndDPH();
                releaseDPH += month.getReleaseDPH();
            }
            
            // Vypo�ti stav na konci obdob�
            end = start - profit + release;
            endAndDPH = startAndDPH - profitAndDPH + releaseAndDPH;
            endDPH = startDPH - profitDPH + releaseDPH;
            
            //Vytvo� objekt p�edstavuj�c� ro�n� rekapitulaci
            YearRecap yearRecap = new YearRecap(date, 
                    start, startAndDPH, startDPH,
                    profit, profitAndDPH, profitDPH,
                    release, releaseAndDPH, releaseDPH,
                    end, endAndDPH, endDPH,
                    "Ro�n� rekapitulace",
                    author, 0);
            
            yearsRacaps.add(yearRecap); // ulo� v�sledek
              
        }
        
        return yearsRacaps;
    }
    
    /**
     * Vypo�te ve�ker� m�s��n� rekapitulace p��slu�n�ho roku. Po��te�n� stav jednoho
     * m�s�ce vyu�ije jako koncov� stav n�sleduj�c�ho m�s�ce. Pro leden pou�ije
     * hodnotu z p�edchoz�ho roku, proto je vhodn� p�ed prvn�m vol�n�m
     * t�to funkce volat nav�c <code>computeAllRecaps()</code>
     * 
     * @return pole m�s��n�ch rekapitulac�
     * @param date datum p�edstavuj�c� rok, pro kter� je t�eba zjistit jednotliv� m�s�ce
     * @throws java.sql.SQLException 
     */
    private ArrayList<MonthRecap> computeMonthRecaps(Calendar date) throws SQLException {
         ArrayList<MonthRecap> result = new ArrayList<MonthRecap>();
         
         // Defenzivn� kop�rov�n�
         Calendar findDate = new GregorianCalendar();
         findDate.setTimeInMillis(date.getTimeInMillis());
         findDate.set(Calendar.DAY_OF_MONTH, 1); // Nastav�me na prvn� den m�s�ce (nebo� minim�ln� jeden den m� kad� m�s�c
         
         // Zjisti rekapitulace pro v�echny m�s�ce v roce
         for (int month = 0; month < 12; month++) {
            findDate.set(Calendar.MONTH, month); // Nastav p��slu�n� m�s�c

            // Zjisti rekapitulaci, jestli�e nalezl, ulo� ji do v�sledku
            MonthRecap monthRecap = readMonthRecap(findDate);
            if (monthRecap.getDate() != null) {
                result.add(monthRecap);
            }
         }
         
         // Vlo� do mapy v�ech m�s��n�ch rekapitulac�
         monthRecaps.put(findDate.get(Calendar.YEAR), result);
         
         return result;
    }

    /**
     * Vrac� pole s vypo�ten�mi ro�n�mi rekapitulacemi, kter� vypo��tala metoda
     * <code>computeAllRecaps()</code>. Jestli�e nebyla je�t� vol�na vr�t� se pr�zd� pole
     * 
     * 
     * @return pole s vypo�ten�mi ro�n�my rekapitulacemi, nebo pr�zdn� pole, jestli�e je�t� nebyly 
     *  rekapitulace po��t�ny
     */
    public ArrayList<YearRecap> getYearsRacaps() {
        if (yearsRacaps == null) {
            return new ArrayList<YearRecap>();
        }
        return yearsRacaps;
    }
    
    /**
     * Vrac� jednu ro�n� rekapitulace. Rekapitulace museli b�t p�edem vypo��t�ny 
     * metodou <CODE>computeAllRecaps</CODE> a pro dan� rok mus� rekapitulace existovat. 
     * Jestli�e ne vrac� null.
     * @param year Rok aktualizace
     * @return rekapitulaci za p��slu�n� rok, nebo null, jestli�e nebyla spo�tena, �i neexistuje
     */
    public YearRecap getYearRacap(int year) {
        
        for (YearRecap i: yearsRacaps) {
            if (i.getYear() == year)
                return i;
        }
        
        return null;
    }
    
    /**
     * Vrac� pole rekapitulac� jednoho roku. Toto pole bylo vypo��t�no p�edchoz�m vol�n�m metody
     * <code>computeAllRecaps()</code>. Jestli�e
     * nebyly tyto metody je�t� vol�ny, vrac� se pr�zdn� pole
     * @return pole m�s��n�ch rekapitulac� konkr�tn�ho roku
     * @param year Rok, pro kter� se m� z�skat pole m�s��n�ch rekapitulac�
     */
    public ArrayList<MonthRecap> getMonthRecaps(int year) {
        
        // Jestli�e je mapa pr�zdn�, nebo neobsahuje po�adovan� kl��
        if (monthRecaps == null || !monthRecaps.containsKey(year)) {
            return new ArrayList<MonthRecap>();
        }
        
        return monthRecaps.get(year);
    }
    
    /**
     * Vrac� jednu m�s��n� rekapitulace. Rekapitulace museli b�t p�edem vypo��t�ny 
     * metodou <CODE>computeAllRecaps</CODE> a pro dan� m�s�c mus� rekapitulace existovat. 
     * Jestli�e ne vrac� null.
     * @param year Rok aktualizace
     * @param month m�s�c, pro kter� z�skat rekapitulaci
     * @return rekapitulaci za p��slu�n� m�s�c, nebo null, jestli�e nebyla spo�tena
     */
    public MonthRecap getMonthRecap(int year, int month) {
        
        for (MonthRecap i: getMonthRecaps(year)) {
            if (i.getDate().get(Calendar.MONTH)+1 == month) {
                return i;
            }
        }
        
        return null;
    }
    
     
}
