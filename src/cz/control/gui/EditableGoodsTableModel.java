/*
 * EditableGoodsTableModel.java
 *
 * Vytvo�eno 7. listopad 2005, 0:47
 *
 
 */

package cz.control.gui;

import cz.control.data.Goods;
import cz.control.business.*;
import cz.control.gui.*;

import java.util.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro definici modelu tabulky se zbo��m
 * Umo��uje editovat sloupec "skladov� ��slo" a "mnno�stv�"
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class EditableGoodsTableModel extends GoodsTableModel {
    
    /** Vytvo�� nov� objekt EditableGoodsTableModel */
    public EditableGoodsTableModel() {
    }
    
    /**
     * Vytvo�� model tabulky
     * @param goods Seznam zbo��, kter� se vlo�� do tabulky
     */
    public EditableGoodsTableModel(ArrayList<Goods> goods) {
        super(goods);
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
