/*
 * Client.java
 *
 * Created on 13. záøí 2005, 22:27
 */

package cz.control.data;

/**
 * Program Control - Skladový systém
 *
 * Tøída uchovávající informace o jednom uživateli systému.
 * Je využívána tøídou Account
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public final class Client implements Comparable<Client> {
    private int userId;
    private int type;
    private String name;
    private String loginName;
    private String password;
    
    
    /**
     *  Vytvoøí prázný objekt s uživatelskými úèty
     */
    public Client() {
        this(-1, 0, "", "", "");
    }
    
    /**
     * Vytvoøí objekt pracující s uživatelskými úèty
     * @param userId Jednoznaèné identifikaèní èíslo uživatele 
     * @param type Typ uživatele. 1 - vedoucí, 2 - prodavaè, 3 - skladník
     * @param name jméno uživatele
     * @param loginName pøihlašovací jméno uživatele
     * @param password heslo uživatele
     */
    public Client(int userId, int type, String name, String loginName, String password) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.loginName = loginName;
        this.password = password;
    }
    
    /**
     * Vrací typ uživatele
     * @return typ uživatele
     */
    public int getType() {
        return type;
    }
    /**
     * Vrací jméno uživatele
     * @return jméno uživatele
     */
    public String getName() {
        return name;
    }
    /**
     * Vrací pøihlašovací jméno uživatele
     * @return pøuhlašovací jméno uživatele
     */
    public String getLoginName() {
        return loginName;
    }
    /**
     * Vrací pøihlašovací heslo uživatele
     * @return pøihlašovací heslo uživatele
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Provede porovnání dvou objektù.
     * Porovnává uživatelská jména klientù
     * @param o porovnávný objekt
     * @return -1, 0, 1 podle výsledku porovnání
     */
    public int compareTo(Client o) {
        return loginName.compareToIgnoreCase(o.loginName);
    }
    
    /**
     * Vrací hashovací kód.
     * @return hashovací kód
     */
    public int hashCode() {
        return type;
    }
    
    /**
     * Porovnává objekty podle uživatelského jména
     * @param o objekt, se kterým se má porovnat
     * @return true jestliže jsou si objekty rovny
     */
    public boolean equals(Object o) {
        
        if (o == null) {
            return false;
        }
        
        return loginName.equalsIgnoreCase( ((Client)o).loginName);
    }
    
    /**
     * Vrací jméno pøihlášeného uživatele
     * @return jméno pøihlášeného uživatele
     */
    public String toString() {
        return "" + name + " (" + ClientType.getName(type) + ")";
    }

    /**
     * Vrací jednoznaèné identifikaèní èíslo uživatele
     * @return Id uživatele
     */
    public int getUserId() {
        return userId;
    }
    
    
}
