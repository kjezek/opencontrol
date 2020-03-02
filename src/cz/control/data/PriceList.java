/*
 * PriceList.java
 *
 * Vytvoøeno 11. bøezen 2006, 17:24
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import cz.control.business.*;
import java.math.BigDecimal;

/**
 * Program Control - Skladový systém
 *
 * Tøída obahuje údaje o jednom ceníku.
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public class PriceList implements Comparable<PriceList> {
    private int id;
    private int supID;
    private int pcA;
    private int pcB;
    private int pcC;
    private int pcD;
        
    /**
     * Vytvoøí prázdný objekt ceníku
     */
    public PriceList() {
        this(-1, 0, 0, 0, 0, 0);
    }
    
    /**
     * Vytvoøí novou instanci PriceList
     * @param id identifikaèní èíslo v tabulce v databázi
     * @param supID odkaz na dodavatele, pro kterého je ceník urèen, nebo 0 pro výchozí 
     * ceník
     * @param pcA procentní vztah pc A k NC
     * @param pcB procentní vztah pc B k NC
     * @param pcC procentní vztah pc C k NC
     * @param pcD procentní vztah pc D k NC
     */
    public PriceList(int id, int supID, int pcA, int pcB, int pcC, int pcD) {
        this.id = id;
        this.supID = supID;
        this.pcA = pcA;
        this.pcB = pcB;
        this.pcC = pcC;
        this.pcD = pcD;
    }

    /**
     * Vrací id ceníku
     * @return id ceníku
     */
    public int getId() {
        return id;
    }

    /**
     * Vrací ID dodavatele, kterému ceník patøí, nebo 0, jestliže se jedná o 
     * výchozí ceník
     * @return ID dodavatele, kterému ceník patøí, nebo 0, jestliže se jedná o 
     * výchozí ceník
     */
    public int getSupID() {
        return supID;
    }

    /**
     * Vrací procentní vztah PC A k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     * @return rocentní vztah PC A k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     */
    public int getPcA() {
        return pcA;
    }

    /**
     * Vrací procentní vztah PC B k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     * @return rocentní vztah PC B k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     */
    public int getPcB() {
        return pcB;
    }
    
    /**
     * Vrací procentní vztah PC C k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     * @return rocentní vztah PC C k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     */
    public int getPcC() {
        return pcC;
    }

    /**
     * Vrací procentní vztah PC D k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     * @return rocentní vztah PC D k NC. Vyjádøený tak, že poslední
     * dvì místa jsou považovány za desetiná
     */
    public int getPcD() {
        return pcD;
    }
    
    public String toString() {
        String result = "";
        
        if (supID == 0) {
            result += "Výchozí ceník ";
        } else {
            result += "Ceník pro: " + supID + " ";
        }
        
        result += "" +
                "pc A = " + new BigDecimal(pcA).divide(Store.CENT).toString() + ", " +
                "pc B = " + new BigDecimal(pcB).divide(Store.CENT).toString() + ", " +
                "pc C = " + new BigDecimal(pcC).divide(Store.CENT).toString() + ", " +
                "pc D = " + new BigDecimal(pcD).divide(Store.CENT).toString();
        
        return result;
    }
    
    public boolean equals(Object o) {
        return supID == ((PriceList) o).getId() &&
                 pcA == ((PriceList) o).getPcA() &&
                 pcB == ((PriceList) o).getPcB() &&
                 pcC == ((PriceList) o).getPcC() &&
                 pcD == ((PriceList) o).getPcD();
    }
    
    /**
     *  Vrací hash kód jako atribut supID
     */
    public int hashCode() {
        return supID;
    }

    /**
     *  Vrací atribut ID
     */
    public int compareTo(PriceList o) {
        return id;
    }
    
}
