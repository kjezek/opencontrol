/*
 * MonthRecap.java
 *
 * Vytvo�eno 7. b�ezen 2006, 21:34
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import java.util.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da obsahuje data z jedn� m�s��n� rekapitulace
 *
 * @author Kamil Je�ek
 * 
 * (C) 2006, ver. 1.0
 */
public class MonthRecap implements Comparable<MonthRecap> {
    private Calendar date;
    private long start;
    private long startAndDPH;
    private long startDPH;
    private long profit;
    private long profitAndDPH;
    private long profitDPH;
    private long release;
    private long releaseAndDPH;
    private long releaseDPH;
    private long end;
    private long endAndDPH;
    private long endDPH;
    private String text;
    private String author;
    private int userId;
    
    /**
     * Vytvo�� pr�zdnou instanci m�s��n� rekapitulace
     */
    public MonthRecap() {
        this(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", "", 0);
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
    public MonthRecap(Calendar date, 
            long start, long startAndDPH, long startDPH,
            long profit, long profitAndDPH, long profitDPH,
            long release, long releaseAndDPH, long releaseDPH,
            long end, long endAndDPH, long endDPH,
            String text, String author, int userId) {
        
        if (date == null) {
            this.date = null;
        } else {   
            this.date = new GregorianCalendar();
            this.date.setTimeInMillis(date.getTimeInMillis()); // defenzivn� kop�rov�n�
        }
        
        this.start = start;
        this.startAndDPH = startAndDPH;
        this.startDPH = startDPH;
        
        this.profit = profit;
        this.profitAndDPH = profitAndDPH;
        this.profitDPH = profitDPH;
        
        this.release = release;
        this.releaseAndDPH = releaseAndDPH;
        this.releaseDPH = releaseDPH;
        
        this.end = end;
        this.endAndDPH = endAndDPH;
        this.endDPH = endDPH;
        
        this.text = text;
        this.author = author;
        this.userId = userId;
    }

    /**
     * vrac� atum ke kter�mu n�le�� rekapitulace
     * @return  atum ke kter�mu n�le�� rekapitulace
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Vrac� po��te�n� finan�n� stav na sklad�. Nebo-li koncov� stav p�edchoz�
     * rekapitulace 
     * @return po��te�n� finan�n� stav na sklad�. Nebo-li koncov� stav p�edchoz� 
     * rekapitulace
     */
    public long getStart() {
        return start;
    }

    /**
     * Vrac� po��te�n� finan�n� stav na sklad� s DPH. Nebo-li koncov� stav p�edchoz�
     * rekapitulace 
     * @return po��te�n� finan�n� stav na sklad� s DPH. Nebo-li koncov� stav p�edchoz� 
     * rekapitulace
     */
    public long getStartAndDPH() {
        return startAndDPH;
    }
    
    /**
     * Vrac� DPH pro po��te�n� finan�n� stav na sklad�. Nebo-li koncov� stav p�edchoz�
     * rekapitulace 
     * @return DPH pro po��te�n� finan�n� stav na sklad�. Nebo-li koncov� stav p�edchoz� 
     * rekapitulace
     */
    public long getStartDPH() {
        return startDPH;
    }
    
    /**
     * vrac� zisk za dan� obdob�. To je sou�et v�ech v�dejek za dan� 
     * @return Zisk za dan� obdob�. To je sou�et v�ech v�dejek za dan�  
     */
    public long getProfit() {
        return profit;
    }
    
    /**
     * vrac� zisk za dan� obdob� s DPH. To je sou�et v�ech v�dejek za dan� 
     * @return Zisk za dan� obdob� s DPH. To je sou�et v�ech v�dejek za dan�  
     */
    public long getProfitAndDPH() {
        return profitAndDPH;
    }
    
    /**
     * vrac� DPH pro zisk za dan� obdob�. To je sou�et v�ech v�dejek za dan� 
     * @return DPH pro Zisk za dan� obdob�. To je sou�et v�ech v�dejek za dan�  
     */
    public long getProfitDPH() {
        return profitDPH;
    }
        
    /**
     * Vrac� objem vydan�ch prost�edk� za dan� obdob�. To je sou�et za v�echny p��jemky 
     * @return Objem vydan�ch prost�edk� za dan� obdob�. To je sou�et za v�echny p��jemky
     */
    public long getRelease() {
        return release;
    }
        
    /**
     * Vrac� objem vydan�ch prost�edk� za dan� obdob� s DPH. To je sou�et za v�echny p��jemky 
     * @return Objem vydan�ch prost�edk� za dan� obdob� s DPH. To je sou�et za v�echny p��jemky
     */
    public long getReleaseAndDPH() {
        return releaseAndDPH;
    }

        
    /**
     * Vrac� DPH pro objem vydan�ch prost�edk� za dan� obdob�. To je sou�et za v�echny p��jemky 
     * @return DPH pro Objem vydan�ch prost�edk� za dan� obdob�. To je sou�et za v�echny p��jemky
     */
    public long getReleaseDPH() {
        return releaseDPH;
    }

    /**
     * vrac� kone�n� stav za dan� obdob� , nebo-li finance z�skan� v�po�tem
     * start + profit - release
     * @return Kone�n� stav za dan� obdob�, nebo-li finance z�skan� v�po�tem
     * start + profit - release
     */
    public long getEnd() {
        return end;
    }

    /**
     * vrac� kone�n� stav za dan� obdob� s DPH, nebo-li finance z�skan� v�po�tem
     * start + profit - release
     * @return Kone�n� stav za dan� obdob� s DPH, nebo-li finance z�skan� v�po�tem
     * start + profit - release
     */
    public long getEndAndDPH() {
        return endAndDPH;
    }

    /**
     * vrac� DPH pro kone�n� stav za dan� obdob�, nebo-li finance z�skan� v�po�tem
     * start + profit - release
     * @return DPH pro Kone�n� stav za dan� obdob�, nebo-li finance z�skan� v�po�tem
     * start + profit - release
     */
    public long getEndDPH() {
        return endDPH;
    }

    /**
     * vrac� textovou pozn�mku k dan� rekapitulaci
     * @return Textovou pozn�mku k dan� rekapitulaci
     */
    public String getText() {
        return text;
    }

    /**
     * vrac� jm�no osoby, kter� provedla uz�v�rku
     * @return Jm�no osoby, kter� provedla uz�v�rku 
     */
    public String getAuthor() {
        return author;
    }

    /**
     * vrac� odkaz do tabulky u�ivatelsk�ch ��t� k autorovi rekapitualce
     * @return odkaz do tabulky u�ivatelsk�ch ��t� k autorovi rekapitualce 
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Vrac� ��slo m�s�ce v intervalu 0-11 ke kter�mu n�le�� tato 
     * rekapitulace. Nebo -1, jestli�e je datum nastaveno na null
     * @return 
     */
    public int getMonth() {
        if (date == null)
            return -1;

        return date.get(Calendar.MONTH);
    }

    /**
     * Porovn� dva objekty podle datumu
     * @param o objekt k porovn�n�
     * @return v�sledek porovn�n� datum�
     */
    public int compareTo(MonthRecap o) {
        return date.compareTo(o.getDate());
    }
    
    /**
     * Hash kod ur�en� podle roku
     * @return hah k�d podle roku
     */
    public int hasCode() {
        return date.get(Calendar.YEAR);
    }
    
    /**
     * Porovn� objekty podle m�s�ce a roku  
     * @param o objekt k porovn�n�
     * @return porovn�n� podle m�s�ce a roku
     */
    public boolean equals(Object o) {
        return date.get(Calendar.MONTH) == ( (MonthRecap) o).getDate().get(Calendar.MONTH) &&
                date.get(Calendar.YEAR) == ( (MonthRecap) o).getDate().get(Calendar.YEAR);
    }
    
    public String toString() {
        return "Rok: " + date.get(Calendar.YEAR) + ", m�s�c: " + (date.get(Calendar.MONTH)+1);
    }
    
    
}
