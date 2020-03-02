/*
 *
 * Vytvo�eno 3. Leden, 2010
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */
package cz.control.errors;

/**
 * Program Control - Skladov� syst�m
 *
 * Obecn� aplika�n� v�jimka
 *
 * @author Kamil Je�ek
 *
 * (C) 2010, ver. 1.1
 */
public class ApplicationException extends Exception {

    public ApplicationException(Exception ex) {
        super(ex);
    }

}
