/*
 * ClientType.java
 *
 * Created on 15. z��� 2005, 22:33
 */

package cz.control.data;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da tvo�� typov� zabezpe�en� ordin�ln� v��et. Poskytuje statick� �lensk� atributy
 * odpov�daj�c� typ�m klient�, kte�� vystupuj� v syst�mu.
 *
 * viz. Josuha Bloch, 57 z�sad softwarov�ho experta, rada 21 
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public class ClientType implements Comparable {
    private String name;
    
    /* zaka� vytvo�en� instance */
    private ClientType(String name) {
        this.name = name;
    }
    
    private static int nextOrd = 0;
    private final int ordinal = ++nextOrd;
    
    /**
     * Vrac� �et�zec obsahuj�c� n�zev p�ihl�en�ho u�ivatele
     * @return N�zev p�ihl�en�ho u�ivatele
     */
    public String toString() {
        return name;
    }
    
    /**
     * Porovn� dva objekty
     * @param o Objekt s kter�m se m� porovn�vat
     * @return hodnota ur�uj�c� v�sledek porovn�n�
     */
    public int compareTo(Object o) {
        return ordinal - ((ClientType) o).ordinal;
    }
    
    /**
     * Zkotroluje dva objekty na shodu
     * @param o Objekt se kter�m se m� testovat shoda
     * @return true, jestli�e jsou objekty shodn�, jinak vrac� false
     */
    public boolean equals(Object o) {
        if (ordinal - ((Integer) o).intValue() == 0) {
            return true; 
        }
        
        return false; 
    }
    
    /**
     * Vrac� po�adov� ��slo u�ivatele
     * @return Po�adov� ��slo u�ivatele
     */
    public int getOrdinal() {
        return ordinal;
    }
    
    /**
     * Vrac� jm�no u�ivatele
     * @param ordinal ordin�ln� ��slo u�ivatele
     * @return pojmenov�n� u�ivatele
     */
    public static String getName(int ordinal) {
        String result = "?";
        
        if (ordinal ==  MANAGER.getOrdinal()) {
            result = MANAGER.toString();
        } else if (ordinal ==  STORE_MAN.getOrdinal()) {
            result = STORE_MAN.toString();
        } else if (ordinal ==  CASH.getOrdinal()) {
            result = CASH.toString();
        }  

        return result;
    }
    
    /**
     * Objekt p�edstavuj�c� vedouc�ho
     */
    public static final ClientType MANAGER = new ClientType("Vedouc�");
    /**
     * Objekt p�edstavuj�c� pokladn�ka
     */
    public static final ClientType CASH = new ClientType("Pokladn�");
    /**
     * Objekt p�edstavuj�c� skladn�ka
     */
    public static final ClientType STORE_MAN = new ClientType("Skladn�k");
    
}
