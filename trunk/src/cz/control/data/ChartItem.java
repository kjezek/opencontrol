/*
 * ChartItem.java
 *
 * Vytvoøeno 18. prosinec 2005, 12:33
 *
 
 */

package cz.control.data;

/**
 * Program Control - Skladový systém
 *
 * Tøída tvoøící jednu položku žebøíèku zobrazovaného ve statistikách
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public class ChartItem {
    
    private Object item;
    private long count;
    
    /**
     * Vytvoøí nový objekt ChartItem
     * @param item Objekt, pøedstavující položku žebøíèku
     * @param count Hodnota položky - "poèet hlasù"
     */
    public ChartItem(Object item, long count) {
        this.item = item;
        this.count = count;
    }

    /**
     * Vrací položku žebøíèku
     * @return položka žebøíèku
     */
    public Object getItem() {
        return item;
    }

    /**
     * Vrací poèet hlasù
     * @return "poèet hlasù"
     */
    public long getCount() {
        return count;
    }
    
    /**
     * Textový popis
     * @return textový popis
     */
    public String toString() {
        return item.toString() + " (" + count + ")";
    }
    
}
