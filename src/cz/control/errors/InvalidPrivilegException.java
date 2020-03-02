/*
 * InvalidPrivilegException.java
 *
 * Created on 13. záøí 2005, 23:01
 */

package cz.control.errors;

/**
 * Program Control - Skladový systém
 *
 * Tøída tvoøí vyjímku, která je vyvolána, jestliže nemá pøihlášený uživatel oprávnìní 
 * provádìt pøíslušnou operaci
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class InvalidPrivilegException extends Exception {
    
    /**
     *  Vytvoøí vyjímku
     */
    public InvalidPrivilegException() {
        super("");
    }
    
    public InvalidPrivilegException(String message) {
        super(message);
    }
    
    public String toString() {
        return "Nemáte dostateèná práva na provedení této operace.";
    }
    
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }
}
