/*
 * PriceList.java
 *
 * Vytvo�eno 11. b�ezen 2006, 17:24
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import cz.control.business.*;
import java.math.BigDecimal;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da obahuje �daje o jednom cen�ku.
 *
 * @author Kamil Je�ek
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
     * Vytvo�� pr�zdn� objekt cen�ku
     */
    public PriceList() {
        this(-1, 0, 0, 0, 0, 0);
    }
    
    /**
     * Vytvo�� novou instanci PriceList
     * @param id identifika�n� ��slo v tabulce v datab�zi
     * @param supID odkaz na dodavatele, pro kter�ho je cen�k ur�en, nebo 0 pro v�choz� 
     * cen�k
     * @param pcA procentn� vztah pc A k NC
     * @param pcB procentn� vztah pc B k NC
     * @param pcC procentn� vztah pc C k NC
     * @param pcD procentn� vztah pc D k NC
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
     * Vrac� id cen�ku
     * @return id cen�ku
     */
    public int getId() {
        return id;
    }

    /**
     * Vrac� ID dodavatele, kter�mu cen�k pat��, nebo 0, jestli�e se jedn� o 
     * v�choz� cen�k
     * @return ID dodavatele, kter�mu cen�k pat��, nebo 0, jestli�e se jedn� o 
     * v�choz� cen�k
     */
    public int getSupID() {
        return supID;
    }

    /**
     * Vrac� procentn� vztah PC A k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     * @return rocentn� vztah PC A k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     */
    public int getPcA() {
        return pcA;
    }

    /**
     * Vrac� procentn� vztah PC B k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     * @return rocentn� vztah PC B k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     */
    public int getPcB() {
        return pcB;
    }
    
    /**
     * Vrac� procentn� vztah PC C k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     * @return rocentn� vztah PC C k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     */
    public int getPcC() {
        return pcC;
    }

    /**
     * Vrac� procentn� vztah PC D k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     * @return rocentn� vztah PC D k NC. Vyj�d�en� tak, �e posledn�
     * dv� m�sta jsou pova�ov�ny za desetin�
     */
    public int getPcD() {
        return pcD;
    }
    
    public String toString() {
        String result = "";
        
        if (supID == 0) {
            result += "V�choz� cen�k ";
        } else {
            result += "Cen�k pro: " + supID + " ";
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
     *  Vrac� hash k�d jako atribut supID
     */
    public int hashCode() {
        return supID;
    }

    /**
     *  Vrac� atribut ID
     */
    public int compareTo(PriceList o) {
        return id;
    }
    
}
