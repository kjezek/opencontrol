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
import cz.control.data.DphScenarioItem;
import cz.control.errors.ApplicationException;
import java.sql.SQLException;
import java.util.List;

/**
 * Program Control - Skladový systém
 *
 * Podporuje tvodrbu scénáøe
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public abstract class AbstractDphScenario implements Scenario {

    private Store store;
    private String label;

    /**
     *
     * @param store objekt pracující se skladem
     */
    public AbstractDphScenario(String label, Store store) {
        this.store = store;
        this.label = label;
    }

    /**
     * 
     * @return vrací seznam scénáøù
     */
    public abstract List<DphScenarioItem> scenarios();

    /** {@inheritDoc} */
    public String getDescription() {

        StringBuffer buffer = new StringBuffer();

        buffer.append("<html>");

        buffer.append("Zmìna DPH: <br>");

        // projdi scénáøe a vygeneuj popis
        for (DphScenarioItem item: scenarios()) {

            buffer.append("<b>");
            buffer.append(item.getOldDph());
            buffer.append("</b>");

            buffer.append(" na ");

            buffer.append("<b>");
            buffer.append(item.getNewDph());
            buffer.append("</b>");

            buffer.append("<br>");
        }

        return buffer.toString();
    }

    /** {@inheritDoc} */
    public void procced() throws ApplicationException {

        try {
            // zpracuj postupnì všechny scénáøe
            for (DphScenarioItem item: scenarios()) {

                store.updateDphLayer(item.getOldDph(), item.getNewDph());
            }
        } catch (SQLException ex) {
            throw new ApplicationException(ex);
        }

    }

    public String getLabel() {
        return label;
    }



    @Override
    public String toString() {
        return getLabel();
    }


}
