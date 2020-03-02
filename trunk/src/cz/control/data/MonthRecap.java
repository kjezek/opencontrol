/*
 * MonthRecap.java
 *
 * Vytvoøeno 7. bøezen 2006, 21:34
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída obsahuje data z jedné mìsíèní rekapitulace
 *
 * @author Kamil Ježek
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
     * Vytvoøí prázdnou instanci mìsíèní rekapitulace
     */
    public MonthRecap() {
        this(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", "", 0);
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
            this.date.setTimeInMillis(date.getTimeInMillis()); // defenzivní kopírování
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
     * vrací atum ke kterému náleží rekapitulace
     * @return  atum ke kterému náleží rekapitulace
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Vrací poèáteèní finanèní stav na skladì. Nebo-li koncový stav pøedchozí
     * rekapitulace 
     * @return poèáteèní finanèní stav na skladì. Nebo-li koncový stav pøedchozí 
     * rekapitulace
     */
    public long getStart() {
        return start;
    }

    /**
     * Vrací poèáteèní finanèní stav na skladì s DPH. Nebo-li koncový stav pøedchozí
     * rekapitulace 
     * @return poèáteèní finanèní stav na skladì s DPH. Nebo-li koncový stav pøedchozí 
     * rekapitulace
     */
    public long getStartAndDPH() {
        return startAndDPH;
    }
    
    /**
     * Vrací DPH pro poèáteèní finanèní stav na skladì. Nebo-li koncový stav pøedchozí
     * rekapitulace 
     * @return DPH pro poèáteèní finanèní stav na skladì. Nebo-li koncový stav pøedchozí 
     * rekapitulace
     */
    public long getStartDPH() {
        return startDPH;
    }
    
    /**
     * vrací zisk za dané období. To je souèet všech výdejek za dané 
     * @return Zisk za dané období. To je souèet všech výdejek za dané  
     */
    public long getProfit() {
        return profit;
    }
    
    /**
     * vrací zisk za dané období s DPH. To je souèet všech výdejek za dané 
     * @return Zisk za dané období s DPH. To je souèet všech výdejek za dané  
     */
    public long getProfitAndDPH() {
        return profitAndDPH;
    }
    
    /**
     * vrací DPH pro zisk za dané období. To je souèet všech výdejek za dané 
     * @return DPH pro Zisk za dané období. To je souèet všech výdejek za dané  
     */
    public long getProfitDPH() {
        return profitDPH;
    }
        
    /**
     * Vrací objem vydaných prostøedkù za dané období. To je souèet za všechny pøíjemky 
     * @return Objem vydaných prostøedkù za dané období. To je souèet za všechny pøíjemky
     */
    public long getRelease() {
        return release;
    }
        
    /**
     * Vrací objem vydaných prostøedkù za dané období s DPH. To je souèet za všechny pøíjemky 
     * @return Objem vydaných prostøedkù za dané období s DPH. To je souèet za všechny pøíjemky
     */
    public long getReleaseAndDPH() {
        return releaseAndDPH;
    }

        
    /**
     * Vrací DPH pro objem vydaných prostøedkù za dané období. To je souèet za všechny pøíjemky 
     * @return DPH pro Objem vydaných prostøedkù za dané období. To je souèet za všechny pøíjemky
     */
    public long getReleaseDPH() {
        return releaseDPH;
    }

    /**
     * vrací koneèný stav za dané období , nebo-li finance získané výpoètem
     * start + profit - release
     * @return Koneèný stav za dané období, nebo-li finance získané výpoètem
     * start + profit - release
     */
    public long getEnd() {
        return end;
    }

    /**
     * vrací koneèný stav za dané období s DPH, nebo-li finance získané výpoètem
     * start + profit - release
     * @return Koneèný stav za dané období s DPH, nebo-li finance získané výpoètem
     * start + profit - release
     */
    public long getEndAndDPH() {
        return endAndDPH;
    }

    /**
     * vrací DPH pro koneèný stav za dané období, nebo-li finance získané výpoètem
     * start + profit - release
     * @return DPH pro Koneèný stav za dané období, nebo-li finance získané výpoètem
     * start + profit - release
     */
    public long getEndDPH() {
        return endDPH;
    }

    /**
     * vrací textovou poznámku k dané rekapitulaci
     * @return Textovou poznámku k dané rekapitulaci
     */
    public String getText() {
        return text;
    }

    /**
     * vrací jméno osoby, která provedla uzávìrku
     * @return Jméno osoby, která provedla uzávìrku 
     */
    public String getAuthor() {
        return author;
    }

    /**
     * vrací odkaz do tabulky uživatelských úètù k autorovi rekapitualce
     * @return odkaz do tabulky uživatelských úètù k autorovi rekapitualce 
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Vrací èíslo mìsíce v intervalu 0-11 ke kterému náleží tato 
     * rekapitulace. Nebo -1, jestliže je datum nastaveno na null
     * @return 
     */
    public int getMonth() {
        if (date == null)
            return -1;

        return date.get(Calendar.MONTH);
    }

    /**
     * Porovná dva objekty podle datumu
     * @param o objekt k porovnání
     * @return výsledek porovnání datumù
     */
    public int compareTo(MonthRecap o) {
        return date.compareTo(o.getDate());
    }
    
    /**
     * Hash kod urèený podle roku
     * @return hah kód podle roku
     */
    public int hasCode() {
        return date.get(Calendar.YEAR);
    }
    
    /**
     * Porovná objekty podle mìsíce a roku  
     * @param o objekt k porovnání
     * @return porovnání podle mìsíce a roku
     */
    public boolean equals(Object o) {
        return date.get(Calendar.MONTH) == ( (MonthRecap) o).getDate().get(Calendar.MONTH) &&
                date.get(Calendar.YEAR) == ( (MonthRecap) o).getDate().get(Calendar.YEAR);
    }
    
    public String toString() {
        return "Rok: " + date.get(Calendar.YEAR) + ", mìsíc: " + (date.get(Calendar.MONTH)+1);
    }
    
    
}
