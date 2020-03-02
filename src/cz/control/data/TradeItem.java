/*
 * TradeItem.java
 *
 * Created on 24. z��� 2005, 21:13
 */

package cz.control.data;

import cz.control.business.*;
import java.util.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da uchov�v� informace o jedn� polo�ce obchodu. Tj. jedna polo�ka 
 * v�dejky, nebo p��jemky.
 *
 * @author Kamil Je�ek
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
     * Vytvo�� pr�zdnou polo�ku
     */
    public TradeItem() {
        this(-1, -1, null, "", -1, -1, -1, "", DoBuy.USE_NC_FOR_SUM);
    }
    
    /**
     * Vytvo�� objekt uchov�vaj�c� data o jedn� polo�ce obchodu
     * @param tradeIdListing Odkaz do tabulky p��jemek/v�dejek. Ozna�uje k �emu  tento ��dek n�le��
     * @param tradeId Jednozna�n� ��slo p��jemky
     * @param goodsId odkaz na zbo�� se kter�m se obchodovalo do karty zbo��
     * @param name jm�no zbo�� se kter�m se obchodovalo
     * @param dph da� z p�idan� hodnoty v %
     * @param price cena zbo�� (n�kupn�, nebo prodejn� podle proveden� operace)
     * @param quantity mno�stv� prodan�ho, nebo nakoupen�ho zbo��
     * @param unit mno�stevn� jednotka
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
     * Vrac� da� v procentech
     * @return da� v procentech
     */
    public int getDph() {
        return this.dph;
    }

    /**
     * Vrac� �et�zec p�edstavuj�c� odkaz do karty zbo��
     * @return skladov� ��slo zbo��
     */
    public String getGoodsId() {
        return this.goodsId;
    }

    /**
     * Vrac� jm�no zbo�� se kter�m se obchodovalo
     * @return jm�no zbo��
     */
    public String getName() {
        return this.name;
    }

    /**
     * Vrac� n�kupn�, nebo prodejn� cenu bez dan� se kterou se obchodovalo
     * @return cena bez dan� - n�kupn�, nebo prodejn�
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * Vrac� mno�stv� zbo�� kolik se prodalo, nebo nakoupilo
     * @return mno�stv� zbo�� kter� se prodalo/nakoupilo
     */
    public double getQuantity() {
        return this.quantity;
    }

    /**
     * Vrac� mno�stevn� jednotku
     * @return mno�stevn� jednotka
     */
    public String getUnit() {
        return this.unit;
    }
    
    /**
     * Porovn� objekty
     * @param o porovn�van� objekt
     * @return vrac� z�pornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(TradeItem o) {
        
        int result = name.compareToIgnoreCase(o.getName());
        
        result = (result == 0) ? goodsId.compareToIgnoreCase(o.getGoodsId()) : result;
        result = (result == 0) ? tradeId - o.getTradeId() : result;

        return result;
    }
    
    /**
     * Vrac� hashovac� k�d
     * @return hashovac� k�d, kter� odpov�d� instanci <code>number</code>
     */
    public int hashCode() {
        return tradeId;
    }
    
    /**
     * Porovn� objekty
     * @param o objekt k porovn�n�
     * @return true, jestli�e jsou ojekty shodn�
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        
        return ((TradeItem) o).getGoodsId().equalsIgnoreCase(goodsId) &&
               ((TradeItem) o).getTradeId() == tradeId;
    }
    
    /**
     * Vrac� popis objektu
     * @return popis objektu
     */
    public String toString() {
        return tradeId + " : " + name + ", " + price + "K� " + quantity + " " + unit;
    }

    /**
     *  Vrac� identifika�n� ��slo polo�ky
     * @return identifika�n� ��slo
     */
    public int getBuyId() {
        return tradeIdListing;
    }

    /**
     * Vrac� identifika�n� ��slo
     * @return identifika�n� ��slo
     */
    public int getTradeIdListing() {
        return tradeIdListing;
    }

    /**
     * Vrac� identifika�n� ��slo
     * @return identifika�n� ��slo
     */
    public int getTradeId() {
        return tradeId;
    }
    
    /**
     * Vrac� instanci p�edstavuj�c� tuto t��du, jako instanci zbo��.
     * Ov�em nejedn� se o p�esnou kopii skladov� karty,
     * nebo� tento objekt neobsahuje ve�ker� vlastnosti.
     * Konkr�tn� chyb� �daj EAN. D�le jsou v�echny ceny nastaveny na stejnou hodnotu,
     * tak jak j� vrac� metoda <code>getPrice()</code>
     * @return Objekt v podob� skladov� karty
     */
    public Goods getAsGoods() {
        
        //representGoods = new Store().getGoodsByID(goodsId);
        
        //return result;
        return representGoods;
    }

    /**
     * Nastav� ukazatel na zbo��, kter� reprezentuje tuto polo�ku obchodu
     * @param representGoods 
     */
    public void setRepresentGoods(Goods representGoods) {
        this.representGoods = representGoods;
    }

    /**
     * Typ ceny za kterou bylo zbo�� prod�no.
     * @return 
     */
    public int getUsePrice() {
        return usePrice;
    }
    
    

}
