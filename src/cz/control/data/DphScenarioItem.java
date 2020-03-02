/*
 *
 * Vytvo�eno 3. Leden, 2010
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */
package cz.control.data;

import java.math.BigDecimal;

/**
 * Program Control - Skladov� syst�m
 *
 * Value Objekt jedn� polo�ky sc�n��e pro hromadnou �pravu DPH
 *
 * @author Kamil Je�ek
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
