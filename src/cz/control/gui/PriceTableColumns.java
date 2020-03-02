/*
 * PriceTableColumns.java
 *
 * Vytvo�eno 1. listopad 2005, 10:02
 *
 
 */

package cz.control.gui;

import cz.control.gui.*;

/**
 * Program Control - Skladov� syst�m
 *
 *  V��tov� typ p�edstavuj�c� sloupce tabulky cen
 *
 * (C) 2005, ver. 1.0
 */
enum PriceTableColumns {
    NAME (0),
    PRICE (1),
    PRICE_DPH (2),
    USE_PRICE (3);

    private int number;

    PriceTableColumns(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
    