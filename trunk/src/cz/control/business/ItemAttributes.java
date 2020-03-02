/*
 * ItemAttributes.java
 *
 * Vytvoøeno 15. bøezen 2006, 21:12
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;

import cz.control.data.PriceList;
import cz.control.data.Goods;

/**
 * Program Control - Skladový systém
 *
 * Tøída slouží pro uchování atributù jendo položky pøíjemky/výdejky
 * Uchovává zejména ceny, které se mohou mìnit pøi nabírání nového zboží
 * a je potøeba poèítat nové ceny.
 *
 * @author Kamil Ježek
 *
 * (C) 2006, ver. 1.0
 */
public class ItemAttributes {
    public static final int AVERAGE_PRICE = 1; 
    public static final int LAST_PRICE = 0; 
    public static final int EXPENSIVE_PRICE = 2; 
    public static final int CHEAPER_PRICE = 3; 
    public static final int OLD_PRICE = 4; 

    private int row;
    private int newPcA;
    private int newPcB;
    private int newPcC;
    private int newPcD;
    private int newNc;
    private int computedNc;
    private int computePrices = LAST_PRICE;
    
    private PriceList priceList;
    
    private Goods inputGoods = null;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getNewPcA() {
        return newPcA;
    }

    public void setNewPcA(int newPcA) {
        this.newPcA = newPcA;
    }

    public int getNewPcB() {
        return newPcB;
    }

    public void setNewPcB(int newPcB) {
        this.newPcB = newPcB;
    }

    public int getNewPcC() {
        return newPcC;
    }

    public void setNewPcC(int newPcC) {
        this.newPcC = newPcC;
    }

    public int getNewPcD() {
        return newPcD;
    }

    public void setNewPcD(int newPcD) {
        this.newPcD = newPcD;
    }

    public int getNewNc() {
        return newNc;
    }

    public void setNewNc(int newNc) {
        this.newNc = newNc;
    }

    public int getComputePrices() {
        return computePrices;
    }

    public void setComputePrices(int computePrices) {
        this.computePrices = computePrices;
    }

    public int getComputedNc() {
        return computedNc;
    }

    public void setComputedNc(int computedNc) {
        this.computedNc = computedNc;
    }

    public Goods getInputGoods() {
        return inputGoods;
    }

    public void setInputGoods(Goods inputGoods) {
        this.inputGoods = inputGoods;
    }

    public PriceList getPriceList() {
        return priceList;
    }

    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }

}

