/*
 *
 * Vytvoøeno 3. Leden, 2010
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business.dph;

import cz.control.business.Store;
import cz.control.business.dph.scenarios.Scenario;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Program Control - Skladový systém
 *
 * Obsahuje seznam pøedpøipravených staticky definovaných scénáøù pro zmìnu DPH
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public class StaticDphSrenarios {


    /**
     * Vytvoøí seznam staticky definovaných scénáøù pro zmìnu dph
     * @param store sklad
     * @return seznam scénáøù
     */
    public static List<Scenario> scenarios(Store store) {

        List<Scenario> result = new LinkedList<Scenario>();

        UpdatableScenario sc = new UpdatableScenario("rok 2010", store);

        sc.addScenario( new BigDecimal(9), new BigDecimal(10) );
        sc.addScenario( new BigDecimal(19), new BigDecimal(20) );
        result.add(sc);

        sc = new UpdatableScenario("rok 2010 (zpìt)", store);

        sc.addScenario( new BigDecimal(10), new BigDecimal(9) );
        sc.addScenario( new BigDecimal(20), new BigDecimal(19) );
        result.add(sc);
        
        sc = new UpdatableScenario("rok 2012", store);

        sc.addScenario( new BigDecimal(10), new BigDecimal(14) );
        result.add(sc);

        sc = new UpdatableScenario("rok 2012 (zpìt)", store);

        sc.addScenario( new BigDecimal(14), new BigDecimal(10) );
        result.add(sc);

        sc = new UpdatableScenario("rok 2013 (15%)", store);

        sc.addScenario( new BigDecimal(14), new BigDecimal(15) );
        result.add(sc);

        sc = new UpdatableScenario("rok 2013 (21%)", store);

        sc.addScenario( new BigDecimal(20), new BigDecimal(21) );
        result.add(sc);
        
        
        return result;
    }
}
