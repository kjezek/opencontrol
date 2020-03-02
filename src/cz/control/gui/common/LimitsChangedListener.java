/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui.common;

import java.util.Date;

/**
 * Rozhran� implementuje ka�d� GUI komponenta, kter� um� obnovit sv�j obsah
 * Po zm�ne rozsahu datumu nebo mno�stv� zbo��
 * 
 * @author kamilos
 */
public interface LimitsChangedListener {

    /**
     * Zavol�no poka�d� kdy se zm�n� rozsah datum�, nebo max mno�sstv�
     * @param startDate
     * @param endDate
     * @param max
     */
    public void refresh(Date startDate, Date endDate, Integer max);

}
