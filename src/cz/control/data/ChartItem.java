/*
 * ChartItem.java
 *
 * Vytvo�eno 18. prosinec 2005, 12:33
 *
 
 */

package cz.control.data;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da tvo��c� jednu polo�ku �eb���ku zobrazovan�ho ve statistik�ch
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public class ChartItem {
    
    private Object item;
    private long count;
    
    /**
     * Vytvo�� nov� objekt ChartItem
     * @param item Objekt, p�edstavuj�c� polo�ku �eb���ku
     * @param count Hodnota polo�ky - "po�et hlas�"
     */
    public ChartItem(Object item, long count) {
        this.item = item;
        this.count = count;
    }

    /**
     * Vrac� polo�ku �eb���ku
     * @return polo�ka �eb���ku
     */
    public Object getItem() {
        return item;
    }

    /**
     * Vrac� po�et hlas�
     * @return "po�et hlas�"
     */
    public long getCount() {
        return count;
    }
    
    /**
     * Textov� popis
     * @return textov� popis
     */
    public String toString() {
        return item.toString() + " (" + count + ")";
    }
    
}
