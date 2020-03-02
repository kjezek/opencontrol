/*
 * EditableTradeItemTableModel.java
 *
 * Vytvoøeno 17. únor 2006, 16:35
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;


import cz.control.data.TradeItem;
import cz.control.business.*;
import cz.control.gui.*;

import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pro definici modelu tabulky se zbožím
 * Umožòuje editovat sloupec "mnnožství"
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil Ježek
 */
public class EditableTradeItemTableModel extends TradeItemTableModel {
    
    /** Vytvoøí nový objekt EditableTradeItemTableModel */
    public EditableTradeItemTableModel() {
    }
    
    /**
     * Vytvoøí model tabulky
     * @param goods Seznam zboží, které se vloží do tabulky
     */
    public EditableTradeItemTableModel(ArrayList<TradeItem> tradeItems) {
        super(tradeItems);
    }
    

    /**
     *  Nastaví editovatelné sloupce.
     *  Editovat je možné sloupce se skladovým èíslem a s množstvím
     * @param row øádek
     * @param column sloupec
     * tr
     * @return true, jestliže je sloupec možné editovat, jinak false
     */
    public boolean isCellEditable(int row, int column)  {
        if (column == Columns.QUANTITY.getColumnNumber()) {
            return true;
        }
        return false;
    }
}