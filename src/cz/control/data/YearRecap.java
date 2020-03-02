/*
 * YearRecap.java
 *
 * Vytvoøeno 8. bøezen 2006, 11:27
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import cz.control.business.*;
import java.util.Calendar;

/**
 * Program Control - Skladový systém
 *
 * Tøída obsahuje data z jedné roèní rekapitulace
 *
 * @author Kamil Ježek
 * 
 * (C) 2006, ver. 1.0
 */
public class YearRecap extends MonthRecap {

    /**
     * Vytvoøí prázdnou instanci mìsíèní rekapitulace
     */
    public YearRecap() {
        super();
    }
    
    /**
     * Vytvoøí novou instanci MonthRecap
     * 
     * @param date Datum ke kterému náleží rekapitulace
     * @param start poèáteèní finanèní stav na skladì. Nebo-li koncový stav pøedchozí 
     * rekapitulace
     * @param startAndDPH poèáteèní stav s DPH
     * @param startDPH DPH poèáteeèního stavu
     * @param profit Zisk za dané období. To je souèet všech výdejek za dané 
     * období
     * @param profitAndDPH zisk s DPH
     * @param profitDPH DPH za zisk
     * @param release Objem vydaných prostøedkù za dané období. To je souèet za všechny pøíjemky
     * @param releaseAndDPH výdaje s DPH
     * @param releaseDPH DPH za výdaje
     * @param end Koneèný stav za dané období, nebo-li finance získané výpoètem
     * start + profit - release
     * @param endAndDPH Koneèný stav s DPH
     * @param endDPH DPH koneèného stavu
     * @param text Textová poznámka k dané rekapitulaci
     * @param author Jméno osoby, která provedla uzávìrku
     * @param userId odkaz do tabulky uživatelských úètù k autorovi rekapitualce
     */
    public YearRecap(Calendar date, 
            long start, long startAndDPH, long startDPH,
            long profit, long profitAndDPH, long profitDPH,
            long release, long releaseAndDPH, long releaseDPH,
            long end, long endAndDPH, long endDPH,
            String text, String author, int userId) {
        
        super(date,
                start, startAndDPH, startDPH,
                profit, profitAndDPH, profitDPH,
                release, releaseAndDPH, releaseDPH,
                end, endAndDPH, endDPH,
                text, author, userId);
    }
    
    /**
     * Porovná objekty podle roku  
     * @param o objekt k porovnání
     * @return porovnání podle roku
     */
    public boolean equals(Object o) {
        return super.getDate().get(Calendar.YEAR) == ( (MonthRecap) o).getDate().get(Calendar.YEAR);
    }
    
    public String toString() {
        return "" + super.getDate().get(Calendar.YEAR) + " ";

    }    
    
    /**
     * Vrací o jaký se jedná rok
     * @return èíslo roku, který tento objekt pøedstavuje
     */
    public int getYear() {
        return super.getDate().get(Calendar.YEAR);
    }
}
