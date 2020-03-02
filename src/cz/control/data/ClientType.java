/*
 * ClientType.java
 *
 * Created on 15. záøí 2005, 22:33
 */

package cz.control.data;

/**
 * Program Control - Skladový systém
 *
 * Tøída tvoøí typovì zabezpeèený ordinální výèet. Poskytuje statické èlenské atributy
 * odpovídající typùm klientù, kteøí vystupují v systému.
 *
 * viz. Josuha Bloch, 57 zásad softwarového experta, rada 21 
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public class ClientType implements Comparable {
    private String name;
    
    /* zakaž vytvoøení instance */
    private ClientType(String name) {
        this.name = name;
    }
    
    private static int nextOrd = 0;
    private final int ordinal = ++nextOrd;
    
    /**
     * Vrací øetìzec obsahující název pøihlášeného uživatele
     * @return Název pøihlášeného uživatele
     */
    public String toString() {
        return name;
    }
    
    /**
     * Porovná dva objekty
     * @param o Objekt s kterým se má porovnávat
     * @return hodnota urèující výsledek porovnání
     */
    public int compareTo(Object o) {
        return ordinal - ((ClientType) o).ordinal;
    }
    
    /**
     * Zkotroluje dva objekty na shodu
     * @param o Objekt se kterým se má testovat shoda
     * @return true, jestliže jsou objekty shodné, jinak vrací false
     */
    public boolean equals(Object o) {
        if (ordinal - ((Integer) o).intValue() == 0) {
            return true; 
        }
        
        return false; 
    }
    
    /**
     * Vrací poøadové èíslo uživatele
     * @return Poøadové èíslo uživatele
     */
    public int getOrdinal() {
        return ordinal;
    }
    
    /**
     * Vrací jméno uživatele
     * @param ordinal ordinální èíslo uživatele
     * @return pojmenování uživatele
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
     * Objekt pøedstavující vedoucího
     */
    public static final ClientType MANAGER = new ClientType("Vedoucí");
    /**
     * Objekt pøedstavující pokladníka
     */
    public static final ClientType CASH = new ClientType("Pokladní");
    /**
     * Objekt pøedstavující skladníka
     */
    public static final ClientType STORE_MAN = new ClientType("Skladník");
    
}
