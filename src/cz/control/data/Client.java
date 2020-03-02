/*
 * Client.java
 *
 * Created on 13. z��� 2005, 22:27
 */

package cz.control.data;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da uchov�vaj�c� informace o jednom u�ivateli syst�mu.
 * Je vyu��v�na t��dou Account
 *
 * @author Kamil Je�ek
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
     *  Vytvo�� pr�zn� objekt s u�ivatelsk�mi ��ty
     */
    public Client() {
        this(-1, 0, "", "", "");
    }
    
    /**
     * Vytvo�� objekt pracuj�c� s u�ivatelsk�mi ��ty
     * @param userId Jednozna�n� identifika�n� ��slo u�ivatele 
     * @param type Typ u�ivatele. 1 - vedouc�, 2 - prodava�, 3 - skladn�k
     * @param name jm�no u�ivatele
     * @param loginName p�ihla�ovac� jm�no u�ivatele
     * @param password heslo u�ivatele
     */
    public Client(int userId, int type, String name, String loginName, String password) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.loginName = loginName;
        this.password = password;
    }
    
    /**
     * Vrac� typ u�ivatele
     * @return typ u�ivatele
     */
    public int getType() {
        return type;
    }
    /**
     * Vrac� jm�no u�ivatele
     * @return jm�no u�ivatele
     */
    public String getName() {
        return name;
    }
    /**
     * Vrac� p�ihla�ovac� jm�no u�ivatele
     * @return p�uhla�ovac� jm�no u�ivatele
     */
    public String getLoginName() {
        return loginName;
    }
    /**
     * Vrac� p�ihla�ovac� heslo u�ivatele
     * @return p�ihla�ovac� heslo u�ivatele
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Provede porovn�n� dvou objekt�.
     * Porovn�v� u�ivatelsk� jm�na klient�
     * @param o porovn�vn� objekt
     * @return -1, 0, 1 podle v�sledku porovn�n�
     */
    public int compareTo(Client o) {
        return loginName.compareToIgnoreCase(o.loginName);
    }
    
    /**
     * Vrac� hashovac� k�d.
     * @return hashovac� k�d
     */
    public int hashCode() {
        return type;
    }
    
    /**
     * Porovn�v� objekty podle u�ivatelsk�ho jm�na
     * @param o objekt, se kter�m se m� porovnat
     * @return true jestli�e jsou si objekty rovny
     */
    public boolean equals(Object o) {
        
        if (o == null) {
            return false;
        }
        
        return loginName.equalsIgnoreCase( ((Client)o).loginName);
    }
    
    /**
     * Vrac� jm�no p�ihl�en�ho u�ivatele
     * @return jm�no p�ihl�en�ho u�ivatele
     */
    public String toString() {
        return "" + name + " (" + ClientType.getName(type) + ")";
    }

    /**
     * Vrac� jednozna�n� identifika�n� ��slo u�ivatele
     * @return Id u�ivatele
     */
    public int getUserId() {
        return userId;
    }
    
    
}
