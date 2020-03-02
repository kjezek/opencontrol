/*
 *
 * Vytvo�eno 3. Leden, 2010
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business.dph;

import cz.control.business.Store;
import cz.control.data.DphScenarioItem;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Program Control - Skladov� syst�m
 *
 * Podpora pro tvorbu sc�n��e
 *
 * @author Kamil Je�ek
 *
 * (C) 2010, ver. 1.1
 */
public class UpdatableScenario extends AbstractDphScenario {

    private List<DphScenarioItem> scenarios = new LinkedList<DphScenarioItem>();

    public UpdatableScenario(String label, Store store) {
        super(label, store);
    }

    /**
     * Dopln� dal�� polo�ku sc�n��e zm�ny DPH
     * @param oldDph star� hodnota Dph
     * @param newDph nov� hodnota dph
     */
    public void addScenario(BigDecimal oldDph, BigDecimal newDph) {

        scenarios.add( new DphScenarioItem(oldDph, newDph));
    }

    @Override
    public List<DphScenarioItem> scenarios() {
        return scenarios;
    }


}
