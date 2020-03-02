/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui.tradeshistory;

/**
 * Enum uchovává meta informace o sloupeècích tabulky 
 * s informacemi o historii obchodování
 * 
 * @author kamilos
 */
public enum TradesTableColumns {
    NUMBER(0, 20),
    DATE(1, 100),
    DPH(2, 20),
    PRICE(3, 100),
    QUANTITY(4, 50),
    UNIT(5, 50),
    PARTNER_NAME(6, 150),
    TYP(7, 20)
    ;
    
    
    private int columnNumber;
    private int width;
    
    private TradesTableColumns(int index, int width) {
        this.columnNumber = index;
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

}
