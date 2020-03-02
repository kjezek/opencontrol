/*
 *
 * Vytvoøeno 3. Leden, 2010
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */
package cz.control.data;

import java.math.BigDecimal;

/**
 * Program Control - Skladový systém
 *
 * Value Objekt jedné položky scénáøe pro hromadnou úpravu DPH
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public class DphScenarioItem {

    BigDecimal oldDph;
    BigDecimal newDph;

    public DphScenarioItem(BigDecimal oldDph, BigDecimal newDph) {
        this.oldDph = oldDph;
        this.newDph = newDph;
    }

    public BigDecimal getNewDph() {
        return newDph;
    }

    public BigDecimal getOldDph() {
        return oldDph;
    }



}
