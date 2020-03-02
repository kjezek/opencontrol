/*
 * InvalidLoginException.java
 *
 * Created on 14. z��� 2005, 10:06
 */

package cz.control.errors;

/**
 *
 * @author Kamil Je�ek
 */
public class InvalidLoginException extends Exception {
    
    /** 
     *  Vytvo�� vyj�mku, kter� je vyvol�na, jestli�e se nepovedlo u�ivatele p�ihl�sit
     */
    public InvalidLoginException() {
        super();
    }
    
    /** 
     *  Vytvo�� vyj�mku, kter� je vyvol�na, jestli�e se nepovedlo u�ivatele p�ihl�sit
     *  @param message Popis vyj�mky
     */
    public InvalidLoginException(String message) {
        super(message);
    }
    
    
    public String toString() {
        return "Chybn� p�hl�en�.";
    }
}
