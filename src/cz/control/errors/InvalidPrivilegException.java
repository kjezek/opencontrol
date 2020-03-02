/*
 * InvalidPrivilegException.java
 *
 * Created on 13. z��� 2005, 23:01
 */

package cz.control.errors;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da tvo�� vyj�mku, kter� je vyvol�na, jestli�e nem� p�ihl�en� u�ivatel opr�vn�n� 
 * prov�d�t p��slu�nou operaci
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class InvalidPrivilegException extends Exception {
    
    /**
     *  Vytvo�� vyj�mku
     */
    public InvalidPrivilegException() {
        super("");
    }
    
    public InvalidPrivilegException(String message) {
        super(message);
    }
    
    public String toString() {
        return "Nem�te dostate�n� pr�va na proveden� t�to operace.";
    }
    
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }
}
