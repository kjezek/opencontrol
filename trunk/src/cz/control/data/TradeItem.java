/*
 * TradeItem.java
 *
 * Created on 24. záøí 2005, 21:13
 */

package cz.control.data;

import cz.control.business.*;
import java.util.*;

/**
 * Program Control - Skladovı systém
 *
 * Tøída uchovává informace o jedné poloce obchodu. Tj. jedna poloka 
 * vıdejky, nebo pøíjemky.
 *
 * @author Kamil Jeek
 * 
 * (C) 2005, ver. 1.0
 */
public final class TradeItem implements Comparable<TradeItem> {
    private int tradeIdListing;
    private int tradeId;
    private String goodsId;
    private String name;
    private int dph;
    private int price;
    private double quantity;
    private String unit;
    private int usePrice;
    
    private Goods representGoods;
    
    /**
     * Vytvoøí prázdnou poloku
     */
    public TradeItem() {
        this(-1, -1, null, "", -1, -1, -1, "", DoBuy.USE_NC_FOR_SUM);
    }
    
    /**
     * Vytvoøí objekt uchovávající data o jedné poloce obchodu
     * @param tradeIdListing Odkaz do tabulky pøíjemek/vıdejek. Oznaèuje k èemu  tento øádek náleí
     * @param tradeId Jednoznaèné èíslo pøíjemky
     * @param goodsId odkaz na zboí se kterım se obchodovalo do karty zboí
     * @param name jméno zboí se kterım se obchodovalo
     * @param dph daò z pøidané hodnoty v %
     * @param price cena zboí (nákupní, nebo prodejní podle provedené operace)
     * @param quantity mnoství prodaného, nebo nakoupeného zboí
     * @param unit mnostevní jednotka
     * @param usePrice typ ceny za kterou se prodalo
     */
    public TradeItem(int tradeId, int tradeIdListing, String goodsId, String name, int dph, int price, double quantity, String unit,
            int usePrice) {
        this.tradeId = tradeId;
        this.tradeIdListing = tradeIdListing;
        this.goodsId = goodsId;
        this.name = name;
        this.dph = dph;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.usePrice = usePrice;
        
        representGoods = new Goods(goodsId, name, TypeOfGoods.GOODS.getType(), dph, unit, "", 
                price, price, price, price, price, quantity);
    }

    /**
     * Vrací daò v procentech
     * @return daò v procentech
     */
    public int getDph() {
        return this.dph;
    }

    /**
     * Vrací øetìzec pøedstavující odkaz do karty zboí
     * @return skladové èíslo zboí
     */
    public String getGoodsId() {
        return this.goodsId;
    }

    /**
     * Vrací jméno zboí se kterım se obchodovalo
     * @return jméno zboí
     */
    public String getName() {
        return this.name;
    }

    /**
     * Vrací nákupní, nebo prodejní cenu bez danì se kterou se obchodovalo
     * @return cena bez danì - nákupní, nebo prodejní
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * Vrací mnoství zboí kolik se prodalo, nebo nakoupilo
     * @return mnoství zboí které se prodalo/nakoupilo
     */
    public double getQuantity() {
        return this.quantity;
    }

    /**
     * Vrací mnostevní jednotku
     * @return mnostevní jednotka
     */
    public String getUnit() {
        return this.unit;
    }
    
    /**
     * Porovná objekty
     * @param o porovnávanı objekt
     * @return vrací zápornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(TradeItem o) {
        
        int result = name.compareToIgnoreCase(o.getName());
        
        result = (result == 0) ? goodsId.compareToIgnoreCase(o.getGoodsId()) : result;
        result = (result == 0) ? tradeId - o.getTradeId() : result;

        return result;
    }
    
    /**
     * Vrací hashovací kód
     * @return hashovací kód, kterı odpovídá instanci <code>number</code>
     */
    public int hashCode() {
        return tradeId;
    }
    
    /**
     * Porovná objekty
     * @param o objekt k porovnání
     * @return true, jestlie jsou ojekty shodné
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        
        return ((TradeItem) o).getGoodsId().equalsIgnoreCase(goodsId) &&
               ((TradeItem) o).getTradeId() == tradeId;
    }
    
    /**
     * Vrací popis objektu
     * @return popis objektu
     */
    public String toString() {
        return tradeId + " : " + name + ", " + price + "Kè " + quantity + " " + unit;
    }

    /**
     *  Vrací identifikaèní èíslo poloky
     * @return identifikaèní èíslo
     */
    public int getBuyId() {
        return tradeIdListing;
    }

    /**
     * Vrací identifikaèní èíslo
     * @return identifikaèní èíslo
     */
    public int getTradeIdListing() {
        return tradeIdListing;
    }

    /**
     * Vrací identifikaèní èíslo
     * @return identifikaèní èíslo
     */
    public int getTradeId() {
        return tradeId;
    }
    
    /**
     * Vrací instanci pøedstavující tuto tøídu, jako instanci zboí.
     * Ovšem nejedná se o pøesnou kopii skladové karty,
     * nebo tento objekt neobsahuje veškeré vlastnosti.
     * Konkrétnì chybı údaj EAN. Dále jsou všechny ceny nastaveny na stejnou hodnotu,
     * tak jak jí vrací metoda <code>getPrice()</code>
     * @return Objekt v podobì skladové karty
     */
    public Goods getAsGoods() {
        
        //representGoods = new Store().getGoodsByID(goodsId);
        
        //return result;
        return representGoods;
    }

    /**
     * Nastaví ukazatel na zboí, které reprezentuje tuto poloku obchodu
     * @param representGoods 
     */
    public void setRepresentGoods(Goods representGoods) {
        this.representGoods = representGoods;
    }

    /**
     * Typ ceny za kterou bylo zboí prodáno.
     * @return 
     */
    public int getUsePrice() {
        return usePrice;
    }
    
    

}
