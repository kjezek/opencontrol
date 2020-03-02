/*
 * BuyColumns.java
 *
 * Vytvoøeno 1. listopad 2005, 10:02
 *
 */

package cz.control.gui;

/**
 * Program Control - Skladový systém
 *
 *  Vytvoøí výètový typ obsahující èísla sloupcù 
 *  a jejich pøíslušnou šíøku pro tabulku pøíjemek
 *
 * (C) 2005, ver. 1.0
 */
public enum BuyColumns {
    /**
     * Sloupec se skladovým èíslem
     */
    ID(0, 100),
    /**
     * Sleoupec se jménem zboží
     */
    NAME(1, 230),
    /**
     * Sloupec s DPH
     */
    DPH(2, 20),
    /**
     * Sloupec s Cenou bez DPH
     */
    PRICE(3, 250),
    /**
     * Sloupec s množstvím zboží
     */
    QUANTITY(4, 50),
    /**
     * Sloupec s množstevní jednotkou
     */
    UNIT(5, 50);

    private int columnNumber;
    private int columnWidth;
    /**
     *  Vytvoøí objek uchovávající poøadí a šíøku sloupce
     */
    BuyColumns(int columnNumber, int columnWidth) {
        this.columnNumber = columnNumber;
        this.columnWidth = columnWidth;
    }

    /**
     *  Vrátí èíslo sloupce
     * @return èíslo sloupce
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     *  Vrátí šíøku sloupce
     * @return šíøka sloupce
     */
    public int getColumnWidth() {
        return columnWidth;
    }

}