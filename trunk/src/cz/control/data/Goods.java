/*
 * Goods.java
 *
 * Created on 21. z��� 2005, 14:05
 */

package cz.control.data;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da obsahuj�c� informace o jedn� skladov� polo�ce (kart�). Slou�� pro ulo�en� ��dku
 * na�ten�ho z tabulky, nebo ��dku, kter� se bude ukl�dat do tabulky
 *
 * @author Kamil Je�ek
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
     *  Vytvo�� pr�zdnou skladovou kartu
     */
    public Goods() {
        this(null, "", -1, -1, "", "", -1, -1, -1, -1, -1, -1);
    }
    
    /**
     * Vytvo�� objekt p�edstavuj�c� jednu skladovou kartu. Obsahuje v sob� v�echnyu hodnoty,
     * kter� je pot�eba u skladov� karty uchovat
     * @param goodsID Skladov� ��slo zbo��
     * @param name N�zev zbo��
     * @param type Typ zbo��
     * 1 - klasick� zbo��
     * @param dph Da� z p�idan� hodnoty
     * @param unit Mno�stevn� jednotka
     * @param ean ��rov� k�d
     * @param nc n�kupn� cena
     * @param pcA Prodejn� cena A
     * @param pcB Prodejn� cena B
     * @param pcC prodejn� cena C
     * @param pcD prodejn� cena D
     * @param quantity Mno�stv� zbo�� na sklad�
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
     * Vrac� skladov� ��slo zbo��
     * @return Skladov� ��slo zbo��
     */
    public String getGoodsID() {
        return goodsID;
    }
    
    /**
     * Vrac� jm�no zbo��
     * @return Jm�no zbo��
     */
    public String getName() {
        return name;
    }
    
    /**
     * Vrac� typ zbo��
     * @return typ zbo��
     */
    public int getType() {
        return type;
    }
    
    /**
     * Vrac� da�
     * @return da�
     */
    public int getDph() {
        return dph;
    }
    
    /**
     * Vrac� mno�stev� jednotku
     * [ks, kg, m, ...]
     * @return mno�stevn� jednotka
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * Vrac� ��rov� k�d
     * @return ��rov� k�d
     */
    public String getEan() {
        return ean;
    }
    
    /**
     * Vrac� n�kupn� cenu
     * @return n�kupn� cena
     */
    public int getNc() {
        return nc;
    }
    
    /**
     * Vrac� prodejn� cenu A
     * @return prodejn� cena A
     */
    public int getPcA() {
        return pcA;
    }
    
    /**
     * Vrac� prodejn� cenu B
     * @return prodejn� cena b
     */
    public int getPcB() {
        return this.pcB;
    }
    
    /**
     * Vrac� prodejn� cenu C
     * @return prodejn� cena C
     */
    public int getPcC() {
        return this.pcC;
    }
    
    /**
     * Vrac� prodejn� cenu D
     * @return prodejn� cena D
     */
    public int getPcD() {
        return this.pcD;
    }
    
    /**
     * Vrac� mno�stv� zbo�� na sklad�
     * @return mno�stv� zbo��
     */
    public double getQuantity() {
        return this.quantity;
    }
    
    /**
     * Provede porovn�n� dvou objekt�.
     * Porovn�v� skladov� ��sla
     * @param o porovn�vn� objekt
     * @return -1, 0, 1 podle v�sledku porovn�n�
     */
    public int compareTo(Goods o) {
        int resultName = name.compareToIgnoreCase(o.name);
        
        if (resultName == 0) {
            return goodsID.compareToIgnoreCase(o.getGoodsID());
        }
        
        return resultName;
    }
    
    /**
     * Porovn�v� objekty podle skladov�ho ��sla
     * @param o objekt, se kter�m se m� porovnat
     * @return true jestli�e jsou si objekty rovny
     */
    public boolean equals(Object o) {
        
        if (o == null)
            return false;
        
        return goodsID.equalsIgnoreCase( ((Goods) o).goodsID);
    }
    
    /**
     * Vrac� hashovac� k�d.
     * @return hashovac� k�d
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
