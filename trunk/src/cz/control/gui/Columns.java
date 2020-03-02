/*
 * Columns.java
 *
 * Vytvo�eno 1. listopad 2005, 10:02
 *
 */

package cz.control.gui;

/**
 * Program Control - Skladov� syst�m
 *
 *  Vytvo�� v��tov� typ obsahuj�c� ��sla sloupc� 
 *  a jejich p��slu�nou ���ku
 *
 * (C) 2005, ver. 1.0
 */
public enum Columns {
    /**
     * Sloupec se skladov�m ��slem
     */
    ID(0, 140),
    /**
     * Sleoupec se jm�nem zbo��
     */
    NAME(1, 600),
    /**
     * Sloupec s mno�stv�m zbo��
     */
    QUANTITY(2, 100),
    /**
     * Sloupec s mno�stevn� jednotkou
     */
    UNIT(3, 100);

    private int columnNumber;
    private int columnWidth;
    /**
     *  Vytvo�� objek uchov�vaj�c� po�ad� a ���ku sloupce
     */
    Columns(int columnNumber, int columnWidth) {
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
