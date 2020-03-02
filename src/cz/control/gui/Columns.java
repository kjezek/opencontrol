/*
 * Columns.java
 *
 * Vytvoøeno 1. listopad 2005, 10:02
 *
 */

package cz.control.gui;

/**
 * Program Control - Skladový systém
 *
 *  Vytvoøí výètový typ obsahující èísla sloupcù 
 *  a jejich pøíslušnou šíøku
 *
 * (C) 2005, ver. 1.0
 */
public enum Columns {
    /**
     * Sloupec se skladovým èíslem
     */
    ID(0, 140),
    /**
     * Sleoupec se jménem zboží
     */
    NAME(1, 600),
    /**
     * Sloupec s množstvím zboží
     */
    QUANTITY(2, 100),
    /**
     * Sloupec s množstevní jednotkou
     */
    UNIT(3, 100);

    private int columnNumber;
    private int columnWidth;
    /**
     *  Vytvoøí objek uchovávající poøadí a šíøku sloupce
     */
    Columns(int columnNumber, int columnWidth) {
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
