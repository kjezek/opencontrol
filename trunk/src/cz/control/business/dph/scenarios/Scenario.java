/*
 *
 * Vytvo�eno 3. Leden, 2010
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business.dph.scenarios;

import cz.control.errors.ApplicationException;

/**
 * Program Control - Skladov� syst�m
 *
 * Popisuje jeden sc�n�� �pravy DPH
 *
 * @author Kamil Je�ek
 *
 * (C) 2010, ver. 1.1
 */
public interface Scenario {


    /**
     *
     * @return vrac� lidsky �iteln� popis sc�n��e
     */
    String getDescription();

    /**
     *
     * @return n�zev sc�n��e
     */
    String getLabel();

    /**
     * Provede sc�n��
     */
    void procced() throws ApplicationException;
}
