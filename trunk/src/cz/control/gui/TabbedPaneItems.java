/*
 * TabbedPaneItems.java
 *
 * Vytvo�eno 18. listopad 2005, 16:02
 *
 
 */
package cz.control.gui;

import cz.control.gui.*;

/**
 * Program Control - Skladov� syst�m
 *
 * V��tov� typ reprezentuj�c� jednotliv� z�lo�ky v hlavn�m okn� programu
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public enum TabbedPaneItems {
    /**
     * Z�lo�ka sklad
     */
    STORE(0),
    /**
     * Z�lo�ka dodavatel�
     */
    SUPLIERS(1),
    /**
     * Z�lo�ka odb�ratel�
     */
    CUSTOMERS(2),
    /**
     * Z�lo�ka p��jemky
     */
    BUY(3),
    /**
     * Z�lo�ka v�dejky
     */
    SALE(4),
    /**
     * Z�lo�ka maloobchod
     */
    DISCOUNT(5), 
    /**
     * Z�lo�ka inventura
     */
    STOCKING(6),
    /**
     * Z�lo�ka rekapitulace
     */
    RECAP(7),
    /**
     * Z�lo�ka u�ivatelsk� u�ty
     */
    ACCOUNT(8);
    

    private final int index;
    
    TabbedPaneItems(int index) {
        this.index = index;
    }

    /**
     * Vrac� ��slo indexu
     * @return ��slo indexu
     */
    public int getIndex() {
        return index;
    }
    
    
}
