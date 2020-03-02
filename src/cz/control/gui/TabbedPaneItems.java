/*
 * TabbedPaneItems.java
 *
 * Vytvoøeno 18. listopad 2005, 16:02
 *
 
 */
package cz.control.gui;

import cz.control.gui.*;

/**
 * Program Control - Skladový systém
 *
 * Výètový typ reprezentující jednotlivé záložky v hlavním oknì programu
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public enum TabbedPaneItems {
    /**
     * Záložka sklad
     */
    STORE(0),
    /**
     * Záložka dodavatelé
     */
    SUPLIERS(1),
    /**
     * Záložka odbìratelé
     */
    CUSTOMERS(2),
    /**
     * Záloøka pøíjemky
     */
    BUY(3),
    /**
     * Záložka výdejky
     */
    SALE(4),
    /**
     * Záložka maloobchod
     */
    DISCOUNT(5), 
    /**
     * Záložka inventura
     */
    STOCKING(6),
    /**
     * Záložka rekapitulace
     */
    RECAP(7),
    /**
     * Záložka uživatelské uèty
     */
    ACCOUNT(8);
    

    private final int index;
    
    TabbedPaneItems(int index) {
        this.index = index;
    }

    /**
     * Vrací èíslo indexu
     * @return èíslo indexu
     */
    public int getIndex() {
        return index;
    }
    
    
}
