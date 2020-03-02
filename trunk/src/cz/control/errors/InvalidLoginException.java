/*
 * InvalidLoginException.java
 *
 * Created on 14. záøí 2005, 10:06
 */

package cz.control.errors;

/**
 *
 * @author Kamil Ježek
 */
public class InvalidLoginException extends Exception {
    
    /** 
     *  Vytvoøí vyjímku, která je vyvolána, jestliže se nepovedlo uživatele pøihlásit
     */
    public InvalidLoginException() {
        super();
    }
    
    /** 
     *  Vytvoøí vyjímku, která je vyvolána, jestliže se nepovedlo uživatele pøihlásit
     *  @param message Popis vyjímky
     */
    public InvalidLoginException(String message) {
        super(message);
    }
    
    
    public String toString() {
        return "Chybné pøhlášení.";
    }
}
