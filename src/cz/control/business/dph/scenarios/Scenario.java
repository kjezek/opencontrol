/*
 *
 * Vytvoøeno 3. Leden, 2010
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business.dph.scenarios;

import cz.control.errors.ApplicationException;

/**
 * Program Control - Skladový systém
 *
 * Popisuje jeden scénáø úpravy DPH
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public interface Scenario {


    /**
     *
     * @return vrací lidsky èitelný popis scénáøe
     */
    String getDescription();

    /**
     *
     * @return název scénáøe
     */
    String getLabel();

    /**
     * Provede scénáø
     */
    void procced() throws ApplicationException;
}
