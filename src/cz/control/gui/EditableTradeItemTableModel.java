/*
 * EditableTradeItemTableModel.java
 *
 * Vytvo�eno 17. �nor 2006, 16:35
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;


import cz.control.data.TradeItem;
import cz.control.business.*;
import cz.control.gui.*;

import java.util.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro definici modelu tabulky se zbo��m
 * Umo��uje editovat sloupec "mnno�stv�"
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil Je�ek
 */
public class EditableTradeItemTableModel extends TradeItemTableModel {
    
    /** Vytvo�� nov� objekt EditableTradeItemTableModel */
    public EditableTradeItemTableModel() {
    }
    
    /**
     * Vytvo�� model tabulky
     * @param goods Seznam zbo��, kter� se vlo�� do tabulky
     */
    public EditableTradeItemTableModel(ArrayList<TradeItem> tradeItems) {
        super(tradeItems);
    }
    

    /**
     *  Nastav� editovateln� sloupce.
     *  Editovat je mo�n� sloupce se skladov�m ��slem a s mno�stv�m
     * @param row ��dek
     * @param column sloupec
     * tr
     * @return true, jestli�e je sloupec mo�n� editovat, jinak false
     */
    public boolean isCellEditable(int row, int column)  {
        if (column == Columns.QUANTITY.getColumnNumber()) {
            return true;
        }
        return false;
    }
}