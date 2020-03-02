/*
 * EditableGoodsTableModel.java
 *
 * Vytvoøeno 7. listopad 2005, 0:47
 *
 
 */

package cz.control.gui;

import cz.control.data.Goods;
import cz.control.business.*;
import cz.control.gui.*;

import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pro definici modelu tabulky se zbožím
 * Umožòuje editovat sloupec "skladové èíslo" a "mnnožství"
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class EditableGoodsTableModel extends GoodsTableModel {
    
    /** Vytvoøí nový objekt EditableGoodsTableModel */
    public EditableGoodsTableModel() {
    }
    
    /**
     * Vytvoøí model tabulky
     * @param goods Seznam zboží, které se vloží do tabulky
     */
    public EditableGoodsTableModel(ArrayList<Goods> goods) {
        super(goods);
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
