/*
 * Goods.java
 *
 * Created on 21. záøí 2005, 14:05
 */

package cz.control.data;

/**
 * Program Control - Skladový systém
 *
 * Tøída obsahující informace o jedné skladové položce (kartì). Slouží pro uložení øádku
 * naèteného z tabulky, nebo øádku, který se bude ukládat do tabulky
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public final class Goods implements Comparable<Goods> {
    private int pcD;
    private int pcC;
    private int pcB;
    private int pcA;
    private int nc;
    private String ean;
    private String unit;
    private int dph;
    private int type;
    private String name;
    private String goodsID;
    private double quantity;
    
    
    /**
     *  Vytvoøí prázdnou skladovou kartu
     */
    public Goods() {
        this(null, "", -1, -1, "", "", -1, -1, -1, -1, -1, -1);
    }
    
    /**
     * Vytvoøí objekt pøedstavující jednu skladovou kartu. Obsahuje v sobì všechnyu hodnoty,
     * které je potøeba u skladové karty uchovat
     * @param goodsID Skladové èíslo zboží
     * @param name Název zboží
     * @param type Typ zboží
     * 1 - klasické zboží
     * @param dph Daò z pøidané hodnoty
     * @param unit Množstevní jednotka
     * @param ean èárový kód
     * @param nc nákupní cena
     * @param pcA Prodejní cena A
     * @param pcB Prodejní cena B
     * @param pcC prodejní cena C
     * @param pcD prodejní cena D
     * @param quantity Množství zboží na skladì
     */
    public Goods(String goodsID, String name, int type, int dph, String unit, String ean, int nc,
            int pcA, int pcB, int pcC, int pcD, double quantity) {
        this.goodsID = goodsID;
        this.name = name;
        this.type = type;
        this.dph = dph;
        this.unit = unit;
        this.ean = ean;
        this.nc = nc;
        this.pcA = pcA;
        this.pcB = pcB;
        this.pcC = pcC;
        this.pcD = pcD;
        this.setQuantity(quantity);
    }
    
    /**
     * Vrací skladové èíslo zboží
     * @return Skladové èíslo zboží
     */
    public String getGoodsID() {
        return goodsID;
    }
    
    /**
     * Vrací jméno zboží
     * @return Jméno zboží
     */
    public String getName() {
        return name;
    }
    
    /**
     * Vrací typ zboží
     * @return typ zboží
     */
    public int getType() {
        return type;
    }
    
    /**
     * Vrací daò
     * @return daò
     */
    public int getDph() {
        return dph;
    }
    
    /**
     * Vrací množsteví jednotku
     * [ks, kg, m, ...]
     * @return množstevní jednotka
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * Vrací èárový kód
     * @return èárový kód
     */
    public String getEan() {
        return ean;
    }
    
    /**
     * Vrací nákupní cenu
     * @return nákupní cena
     */
    public int getNc() {
        return nc;
    }
    
    /**
     * Vrací prodejní cenu A
     * @return prodejní cena A
     */
    public int getPcA() {
        return pcA;
    }
    
    /**
     * Vrací prodejní cenu B
     * @return prodejní cena b
     */
    public int getPcB() {
        return this.pcB;
    }
    
    /**
     * Vrací prodejní cenu C
     * @return prodejní cena C
     */
    public int getPcC() {
        return this.pcC;
    }
    
    /**
     * Vrací prodejní cenu D
     * @return prodejní cena D
     */
    public int getPcD() {
        return this.pcD;
    }
    
    /**
     * Vrací množství zboží na skladì
     * @return množství zboží
     */
    public double getQuantity() {
        return this.quantity;
    }
    
    /**
     * Provede porovnání dvou objektù.
     * Porovnává skladová èísla
     * @param o porovnávný objekt
     * @return -1, 0, 1 podle výsledku porovnání
     */
    public int compareTo(Goods o) {
        int resultName = name.compareToIgnoreCase(o.name);
        
        if (resultName == 0) {
            return goodsID.compareToIgnoreCase(o.getGoodsID());
        }
        
        return resultName;
    }
    
    /**
     * Porovnává objekty podle skladového èísla
     * @param o objekt, se kterým se má porovnat
     * @return true jestliže jsou si objekty rovny
     */
    public boolean equals(Object o) {
        
        if (o == null)
            return false;
        
        return goodsID.equalsIgnoreCase( ((Goods) o).goodsID);
    }
    
    /**
     * Vrací hashovací kód.
     * @return hashovací kód
     */
    public int hashCode() {
        return goodsID.length();
    }
    
    public String toString() {
        return goodsID + " " + name + " ";
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
