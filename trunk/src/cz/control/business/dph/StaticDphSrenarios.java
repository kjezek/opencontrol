/*
 *
 * Vytvo�eno 3. Leden, 2010
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business.dph;

import cz.control.business.Store;
import cz.control.business.dph.scenarios.Scenario;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Program Control - Skladov� syst�m
 *
 * Obsahuje seznam p�edp�ipraven�ch staticky definovan�ch sc�n��� pro zm�nu DPH
 *
 * @author Kamil Je�ek
 *
 * (C) 2010, ver. 1.1
 */
public class StaticDphSrenarios {


    /**
     * Vytvo�� seznam staticky definovan�ch sc�n��� pro zm�nu dph
     * @param store sklad
     * @return seznam sc�n���
     */
    public static List<Scenario> scenarios(Store store) {

        List<Scenario> result = new LinkedList<Scenario>();

        UpdatableScenario sc = new UpdatableScenario("rok 2010", store);

        sc.addScenario( new BigDecimal(9), new BigDecimal(10) );
        sc.addScenario( new BigDecimal(19), new BigDecimal(20) );
        result.add(sc);

        sc = new UpdatableScenario("rok 2010 (zp�t)", store);

        sc.addScenario( new BigDecimal(10), new BigDecimal(9) );
        sc.addScenario( new BigDecimal(20), new BigDecimal(19) );
        result.add(sc);
        
        sc = new UpdatableScenario("rok 2012", store);

        sc.addScenario( new BigDecimal(10), new BigDecimal(14) );
        result.add(sc);

        sc = new UpdatableScenario("rok 2012 (zp�t)", store);

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
