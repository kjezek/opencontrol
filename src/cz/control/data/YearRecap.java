/*
 * YearRecap.java
 *
 * Vytvo�eno 8. b�ezen 2006, 11:27
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import cz.control.business.*;
import java.util.Calendar;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da obsahuje data z jedn� ro�n� rekapitulace
 *
 * @author Kamil Je�ek
 * 
 * (C) 2006, ver. 1.0
 */
public class YearRecap extends MonthRecap {

    /**
     * Vytvo�� pr�zdnou instanci m�s��n� rekapitulace
     */
    public YearRecap() {
        super();
    }
    
    /**
     * Vytvo�� novou instanci MonthRecap
     * 
     * @param date Datum ke kter�mu n�le�� rekapitulace
     * @param start po��te�n� finan�n� stav na sklad�. Nebo-li koncov� stav p�edchoz� 
     * rekapitulace
     * @param startAndDPH po��te�n� stav s DPH
     * @param startDPH DPH po��tee�n�ho stavu
     * @param profit Zisk za dan� obdob�. To je sou�et v�ech v�dejek za dan� 
     * obdob�
     * @param profitAndDPH zisk s DPH
     * @param profitDPH DPH za zisk
     * @param release Objem vydan�ch prost�edk� za dan� obdob�. To je sou�et za v�echny p��jemky
     * @param releaseAndDPH v�daje s DPH
     * @param releaseDPH DPH za v�daje
     * @param end Kone�n� stav za dan� obdob�, nebo-li finance z�skan� v�po�tem
     * start + profit - release
     * @param endAndDPH Kone�n� stav s DPH
     * @param endDPH DPH kone�n�ho stavu
     * @param text Textov� pozn�mka k dan� rekapitulaci
     * @param author Jm�no osoby, kter� provedla uz�v�rku
     * @param userId odkaz do tabulky u�ivatelsk�ch ��t� k autorovi rekapitualce
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
     * Porovn� objekty podle roku  
     * @param o objekt k porovn�n�
     * @return porovn�n� podle roku
     */
    public boolean equals(Object o) {
        return super.getDate().get(Calendar.YEAR) == ( (MonthRecap) o).getDate().get(Calendar.YEAR);
    }
    
    public String toString() {
        return "" + super.getDate().get(Calendar.YEAR) + " ";

    }    
    
    /**
     * Vrac� o jak� se jedn� rok
     * @return ��slo roku, kter� tento objekt p�edstavuje
     */
    public int getYear() {
        return super.getDate().get(Calendar.YEAR);
    }
}
