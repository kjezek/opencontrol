/*
 * Recaps.java
 *
 * Vytvoøeno 8. bøezen 2006, 17:55
 *
 * Autor: Kamil Jeek
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
 * Program Control - Skladovı systém
 *
 * Tøída vytváøí rekapitulace skladu
 *
 * @author Kamil Jeek
 * 
 * (C) 2006, ver. 1.0
 */
public class Recaps {
    
    private ArrayList<YearRecap> yearsRacaps = new ArrayList<YearRecap>();
    private HashMap<Integer, ArrayList<MonthRecap>> monthRecaps = new HashMap<Integer, ArrayList<MonthRecap>>();
    
    //proìnné pro vıpoèet poèáteèního stavu mìsíce z koncového stavu pøedešlého mìsíce
    private long startMonth = 0;
    private long startAndDPHMonth = 0;
    private long startDPHMonth = 0;
    
    /**
     * Vytvoøí novou instanci Recaps
     */
    public Recaps() {
    }
    
    /**
     * Vymae z databáse mìsíèní rekapitulaci odpovídající danému datu
     * @param date datum udávající mìsíc, kterı se má vymazat
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
     * Vymae z databáze rekapitulace za jeden rok. Tedy vymae 12 mìsícù daného roku
     * @param date rok, kterı se má vymazat
     * @throws java.sql.SQLException chyba databáze
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
     * Naète mìsíèní uzávìrku odpovídající danému datu z databáze. 
     * Vypoète rekapitulaci. Hodnoty poèáteèního stavu pouije z pøedchozíh volání
     * této metody. Je proto vhodné volat tuto metodu pro jednotlivé mìsíce tak jak
     * jdou za sebou v roce, od Ledna do Prosince
     * @param date datum podle kterého se urèí mìsíc rekapitulace
     * @throws java.sql.SQLException chyba databáze
     * @return rekapitulace odpovídajícídanému datu. Nebo prázdnı objekt, jestlie není rekapitulace vytvoøena
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
            
            // Naèti hodnoty z databáze
            long profit = (new BigDecimal(rs.getDouble(2))).multiply(Store.CENT).longValue();
            long profitAndDPH = (new BigDecimal(rs.getDouble(3))).multiply(Store.CENT).longValue();
            long profitDPH = (new BigDecimal(rs.getDouble(4))).multiply(Store.CENT).longValue();
            
            long release = (new BigDecimal(rs.getDouble(5))).multiply(Store.CENT).longValue();
            long releaseAndDPH = (new BigDecimal(rs.getDouble(6))).multiply(Store.CENT).longValue();
            long releaseDPH = (new BigDecimal(rs.getDouble(7))).multiply(Store.CENT).longValue();
            
            // Vypoèti stav na konci období
            long end = startMonth - profit + release;
            long endAndDPH = startAndDPHMonth - profitAndDPH + releaseAndDPH;
            long endDPH = startDPHMonth - profitDPH + releaseDPH;
             
            // vytvoø objekt rekapitulace 
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
            
            // koncové hodnoty nastav jako poèáteèní pro další mìsíc
            startMonth = end;
            startAndDPHMonth = endAndDPH;
            startDPHMonth = endDPH;
        } 

        rs.close();
        stm.close();         
        
        return result;
    }
    
    /**
     * Vrací seznam rokù, ke kterım existuje nìjaká rekapitulace
     * @return seznam rokù, ke kterı existuje minimálnì jedna mìsíèní rekapitulace
     * @throws java.sql.SQLException chyba databáze
     */
    public ArrayList<Integer> getAllYears() throws SQLException {
        Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
        String command = "SELECT YEAR(date_month) FROM " + RECAP_MONTH_TABLE_NAME + " " +
                "GROUP BY YEAR(date_month) ORDER BY YEAR(date_month)";
        ResultSet rs = stm.executeQuery(command); // Vyhledej roky
        
        ArrayList<Integer> result = new ArrayList<Integer>();
        
        // Naèti všechny roky
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        
        rs.close();
        stm.close();   
        
        return result;
    }
    
    /**
     * Vypoète všechny roèní rekapitulace. K vıpoètu vyuije roky, které vrátila metoda
     * <code>getAllYears</code>. Poèítá rekapitulace postupnì od nejnišího k nejvyššímu roku.
     * Koncovı stav jednoho roku vyuije jako poèáteèní dalšího roku
     * @throws java.sql.SQLException chyba databáze
     * @return Veškeré roèní rekapitulace
     */
    public ArrayList<YearRecap> computeAllRecaps(String author) throws SQLException {
        ArrayList<Integer> years = getAllYears(); // zjisti si všechny roky
        Calendar date = new GregorianCalendar();
        
        //vyèisti mapu mìsíèních rekapitulací a poe roèních rekapitulací
        yearsRacaps.clear();
        monthRecaps.clear();
        startAndDPHMonth = 0;
        startDPHMonth = 0;
        startMonth = 0;
        
        // Promìnné pro vıpoèet rekapitulace
        long start = 0, startAndDPH = 0, startDPH = 0;
        long end = 0, endAndDPH = 0, endDPH = 0;
                
        // Projdi všechny roky ke kterım existují rekapitulace
        // Pole obsahuje roky od nejnišího k nejvyšším
        for (Integer year: years) {
            
            long profit = 0, profitAndDPH = 0, profitDPH = 0;
            long release = 0, releaseAndDPH = 0, releaseDPH = 0;
        
            // Na zaèátku období je start shodnı s koncem minulého období
            start = end;
            startAndDPH = endAndDPH;
            startDPH = endDPH;
            
            // Zjisti všechny mìsíèní rekapitulace tohoto roku podle data
            date.set(Calendar.YEAR, year);
            ArrayList<MonthRecap> months = computeMonthRecaps(date);
            
            // Projdi jednotlivé mìsíce a vypoèti vısledek za celı rok
            // seèti zisky a vıdaje
            for (MonthRecap month: months) {
                profit += month.getProfit();
                profitAndDPH += month.getProfitAndDPH();
                profitDPH += month.getProfitDPH();
                
                release += month.getRelease();
                releaseAndDPH += month.getReleaseAndDPH();
                releaseDPH += month.getReleaseDPH();
            }
            
            // Vypoèti stav na konci období
            end = start - profit + release;
            endAndDPH = startAndDPH - profitAndDPH + releaseAndDPH;
            endDPH = startDPH - profitDPH + releaseDPH;
            
            //Vytvoø objekt pøedstavující roèní rekapitulaci
            YearRecap yearRecap = new YearRecap(date, 
                    start, startAndDPH, startDPH,
                    profit, profitAndDPH, profitDPH,
                    release, releaseAndDPH, releaseDPH,
                    end, endAndDPH, endDPH,
                    "Roèní rekapitulace",
                    author, 0);
            
            yearsRacaps.add(yearRecap); // ulo vısledek
              
        }
        
        return yearsRacaps;
    }
    
    /**
     * Vypoète veškeré mìsíèní rekapitulace pøíslušného roku. Poèáteèní stav jednoho
     * mìsíce vyuije jako koncovı stav následujícího mìsíce. Pro leden pouije
     * hodnotu z pøedchozího roku, proto je vhodné pøed prvním voláním
     * této funkce volat navíc <code>computeAllRecaps()</code>
     * 
     * @return pole mìsíèních rekapitulací
     * @param date datum pøedstavující rok, pro kterı je tøeba zjistit jednotlivé mìsíce
     * @throws java.sql.SQLException 
     */
    private ArrayList<MonthRecap> computeMonthRecaps(Calendar date) throws SQLException {
         ArrayList<MonthRecap> result = new ArrayList<MonthRecap>();
         
         // Defenzivní kopírování
         Calendar findDate = new GregorianCalendar();
         findDate.setTimeInMillis(date.getTimeInMillis());
         findDate.set(Calendar.DAY_OF_MONTH, 1); // Nastavíme na první den mìsíce (nebo minimálnì jeden den má kadı mìsíc
         
         // Zjisti rekapitulace pro všechny mìsíce v roce
         for (int month = 0; month < 12; month++) {
            findDate.set(Calendar.MONTH, month); // Nastav pøíslušnı mìsíc

            // Zjisti rekapitulaci, jestlie nalezl, ulo ji do vısledku
            MonthRecap monthRecap = readMonthRecap(findDate);
            if (monthRecap.getDate() != null) {
                result.add(monthRecap);
            }
         }
         
         // Vlo do mapy všech mìsíèních rekapitulací
         monthRecaps.put(findDate.get(Calendar.YEAR), result);
         
         return result;
    }

    /**
     * Vrací pole s vypoètenımi roèními rekapitulacemi, které vypoèítala metoda
     * <code>computeAllRecaps()</code>. Jestlie nebyla ještì volána vrátí se prázdé pole
     * 
     * 
     * @return pole s vypoètenımi roènímy rekapitulacemi, nebo prázdné pole, jestlie ještì nebyly 
     *  rekapitulace poèítány
     */
    public ArrayList<YearRecap> getYearsRacaps() {
        if (yearsRacaps == null) {
            return new ArrayList<YearRecap>();
        }
        return yearsRacaps;
    }
    
    /**
     * Vrací jednu roèní rekapitulace. Rekapitulace museli bıt pøedem vypoèítány 
     * metodou <CODE>computeAllRecaps</CODE> a pro danı rok musí rekapitulace existovat. 
     * Jestlie ne vrací null.
     * @param year Rok aktualizace
     * @return rekapitulaci za pøíslušnı rok, nebo null, jestlie nebyla spoètena, èi neexistuje
     */
    public YearRecap getYearRacap(int year) {
        
        for (YearRecap i: yearsRacaps) {
            if (i.getYear() == year)
                return i;
        }
        
        return null;
    }
    
    /**
     * Vrací pole rekapitulací jednoho roku. Toto pole bylo vypoèítáno pøedchozím voláním metody
     * <code>computeAllRecaps()</code>. Jestlie
     * nebyly tyto metody ještì volány, vrací se prázdné pole
     * @return pole mìsíèních rekapitulací konkrétního roku
     * @param year Rok, pro kterı se má získat pole mìsíèních rekapitulací
     */
    public ArrayList<MonthRecap> getMonthRecaps(int year) {
        
        // Jestlie je mapa prázdná, nebo neobsahuje poadovanı klíè
        if (monthRecaps == null || !monthRecaps.containsKey(year)) {
            return new ArrayList<MonthRecap>();
        }
        
        return monthRecaps.get(year);
    }
    
    /**
     * Vrací jednu mìsíèní rekapitulace. Rekapitulace museli bıt pøedem vypoèítány 
     * metodou <CODE>computeAllRecaps</CODE> a pro danı mìsíc musí rekapitulace existovat. 
     * Jestlie ne vrací null.
     * @param year Rok aktualizace
     * @param month mìsíc, pro kterı získat rekapitulaci
     * @return rekapitulaci za pøíslušnı mìsíc, nebo null, jestlie nebyla spoètena
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
