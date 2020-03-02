/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui.common;

import java.util.Date;

/**
 * Rozhraní implementuje každá GUI komponenta, která umí obnovit svùj obsah
 * Po zmìne rozsahu datumu nebo množství zboží
 * 
 * @author kamilos
 */
public interface LimitsChangedListener {

    /**
     * Zavoláno pokaždé kdy se zmìní rozsah datumù, nebo max množsství
     * @param startDate
     * @param endDate
     * @param max
     */
    public void refresh(Date startDate, Date endDate, Integer max);

}
