/*
 *
 * Vytvoøeno 3. Leden, 2010
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */
package cz.control.errors;

/**
 * Program Control - Skladový systém
 *
 * Obecná aplikaèní výjimka
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public class ApplicationException extends Exception {

    public ApplicationException(Exception ex) {
        super(ex);
    }

}
