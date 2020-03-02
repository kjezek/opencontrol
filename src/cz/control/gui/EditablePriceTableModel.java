/*
 * EditablePriceTableModel.java
 *
 * Vytvoøeno 1. listopad 2005, 10:02
 *
 */

package cz.control.gui;

import cz.control.business.Store;
import java.math.BigDecimal;

/**
 * Program Control - Skladový systém
 *
 *  Model pro tabulku cen, kterou lze editovat
 *
 * (C) 2005, ver. 1.0
 */
public class EditablePriceTableModel extends PriceTableModel {

    /**
     *  Vytvoøí prázdnou tabulku
     */ 
    public EditablePriceTableModel() {
        super();
    }

    /**
     * Vytvoøí tabulka s použítím jmen slooupcù a o zadaném poètu øádkù
     * @param names Názvy sloupcù
     * @param rowCount Poèet øádkù
     */
    public EditablePriceTableModel(String[] names, int rowCount) {
        super(names, rowCount);
    }    
    
    /**
     * Vytvoøí model tabulky s odpovídajícími cenami pro konkrétní zboží
     * @param nc nákupní cena
     * @param pcA prdejní cena A
     * @param pcB prdejní cena B
     * @param pcC prdejní cena C
     * @param pcD prdejní cena D
     * @param DPH DPH
     */
    public EditablePriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        super(nc, pcA, pcB, pcC, pcD, DPH);
    }

    /**
     *  Nastaví editovatelné sloupce.
     *  Editovat je možné sloupce s cenou
     * @param row øádek
     * @param column sloupec
     * tr
     * @return true, jestliže je možné sloupec editovat
     */
    public boolean isCellEditable(int row, int column)  {
        if (column == PriceTableColumns.PRICE.getNumber() || 
            column == PriceTableColumns.PRICE_DPH.getNumber() ||
            column == PriceTableColumns.USE_PRICE.getNumber()) {
            
            return true;
        }
        return false;
    }

    /**
     * Nastavuje hodnotu na pozici v tabulce
     * @param val objekt, který s má uložit
     * @param row øádek kam uložit
     * @param column sloupec kam uložit 
     */
/*     public void setValueAt(Object val, int row, int column) {
        super.setValueAt(val, row, column); // Nastav øádek a sloupec, který se zmìnil 
    }*/

     /**
     * Provede aktualizaci DPH a pøepoète tabulku
     * @param dph nová cena DPH
     */
     public void changeDph(int dph) {
         super.setDph( new BigDecimal(dph).divide(Store.CENT) ); // aktualizuj DPH
         int index = PriceTableColumns.PRICE.getNumber();

         /* Aktualizuj všechny sloupce */
         setValueAt( getValueAt(0, index), 0, index);
         setValueAt( getValueAt(1, index), 1, index);
         setValueAt( getValueAt(2, index), 2, index);
         setValueAt( getValueAt(3, index), 3, index);
         setValueAt( getValueAt(4, index), 4, index);
     }
     
}

