/*
 *
 * Vytvoøeno 3. Leden, 2010
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business.dph;

import cz.control.business.Store;
import cz.control.data.DphScenarioItem;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Program Control - Skladový systém
 *
 * Podpora pro tvorbu scénáøe
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public class UpdatableScenario extends AbstractDphScenario {

    private List<DphScenarioItem> scenarios = new LinkedList<DphScenarioItem>();

    public UpdatableScenario(String label, Store store) {
        super(label, store);
    }

    /**
     * Doplní další položku scénáøe zmìny DPH
     * @param oldDph stará hodnota Dph
     * @param newDph nová hodnota dph
     */
    public void addScenario(BigDecimal oldDph, BigDecimal newDph) {

        scenarios.add( new DphScenarioItem(oldDph, newDph));
    }

    @Override
    public List<DphScenarioItem> scenarios() {
        return scenarios;
    }


}
