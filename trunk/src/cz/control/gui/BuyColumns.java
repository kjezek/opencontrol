/*
 * BuyColumns.java
 *
 * Vytvo�eno 1. listopad 2005, 10:02
 *
 */

package cz.control.gui;

/**
 * Program Control - Skladov� syst�m
 *
 *  Vytvo�� v��tov� typ obsahuj�c� ��sla sloupc� 
 *  a jejich p��slu�nou ���ku pro tabulku p��jemek
 *
 * (C) 2005, ver. 1.0
 */
public enum BuyColumns {
    /**
     * Sloupec se skladov�m ��slem
     */
    ID(0, 100),
    /**
     * Sleoupec se jm�nem zbo��
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
     * Sloupec s mno�stv�m zbo��
     */
    QUANTITY(4, 50),
    /**
     * Sloupec s mno�stevn� jednotkou
     */
    UNIT(5, 50);

    private int columnNumber;
    private int columnWidth;
    /**
     *  Vytvo�� objek uchov�vaj�c� po�ad� a ���ku sloupce
     */
    BuyColumns(int columnNumber, int columnWidth) {
        this.columnNumber = columnNumber;
        this.columnWidth = columnWidth;
    }

    /**
     *  Vr�t� ��slo sloupce
     * @return ��slo sloupce
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     *  Vr�t� ���ku sloupce
     * @return ���ka sloupce
     */
    public int getColumnWidth() {
        return columnWidth;
    }

}