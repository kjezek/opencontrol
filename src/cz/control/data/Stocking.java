/*
 * Stocking.java
 *
 * Vytvoøeno 28. únor 2006, 23:58
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída uchovává informace o jedné položce inventury
 *
 * @author Kamil Ježek
 * 
 * (C) 2006, ver. 1.0
 */
public class Stocking implements Comparable<Stocking> {
    private int stockingIdListing;
    private int stockingId;
    private String goodsId;
    private String name;
    private int dph;
    private int price;
    private double difer;
    private String unit;
    
    /**
     * Vytvoøí novou instanci Stocking
     */
    public Stocking() {
        this(-1, -1, null, "", -1, -1, 0, "");
    }
    
    public Stocking(int stockingId, int stockingIdListing, String goodsId, String name, int dph, 
            int price, double difer, String unit) {
        
        this.stockingIdListing = stockingIdListing;
        this.stockingId = stockingId;
        this.goodsId = goodsId;
        this.name = name;
        this.dph = dph;
        this.price = price;
        this.difer = difer;
        this.unit = unit;
    }
    

    public int getStockingIdListing() {
        return stockingIdListing;
    }

    public int getStockingId() {
        return stockingId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public String getName() {
        return name;
    }

    public int getDph() {
        return dph;
    }

    public int getPrice() {
        return price;
    }

    public double getDifer() {
        return difer;
    }

    public String getUnit() {
        return unit;
    }
    
    /**
     * Porovná objekty
     * @param o porovnávaný objekt
     * @return vrací zápornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(Stocking o) {
        return goodsId.compareToIgnoreCase(o.getGoodsId());
    }    
    
    /**
     * Vrací hashovací kód
     * @return hashovací kód, který odpovídá instanci <code>number</code>
     */
    public int hashCode() {
        return stockingId;
    }
    
    /**
     * Porovná objekty
     * @param o objekt k porovnání
     * @return true, jestliže jsou ojekty shodné
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        
        return ((Stocking) o).getGoodsId().equalsIgnoreCase(goodsId) /*&&
               ((Stocking) o).getTradeId() == tradeId */;
    }    

    /**
     * Vrací popis objektu
     * @return popis objektu
     */
    public String toString() {
        return stockingIdListing + ": " + name + ", " + price + "Kè " + difer + " " + unit;
    }    
}
