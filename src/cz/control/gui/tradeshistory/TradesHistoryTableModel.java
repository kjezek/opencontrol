/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui.tradeshistory;

import cz.control.data.GoodsTradesHistory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author kamilos
 */
public class TradesHistoryTableModel extends AbstractTableModel {
    private String[] columnNames = new String[] {
        "Èíslo", "Datum", "DPH [%]", "Cena bez DPH", "Množství", "Jednotka", "Dodavatel/Odbìratel", "Doklad"};
    
    private List<GoodsTradesHistory> data = new ArrayList<GoodsTradesHistory>();

    public void clean() {
        data.clear();
    }
    
    public void addRows(List<GoodsTradesHistory> list) {
        
        this.data.addAll(list);
        
        fireTableDataChanged();     
    }
    
    public GoodsTradesHistory getItemAt(int rowIndex) {
        return data.get(rowIndex);
    }
    
    /**
     * Vrátí název sloupce v tabulce
     * @param column èislo sloucpe
     * @return název sloupce
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }
    
    /**
     * Vrátí typ objektu, jaký sloupec obsahuje
     * @param column èíslo sloupce
     * @return Tøída, kterou sloupec obsahuje
     */
    @Override
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
    
    
    /**
     * Pøevede objekt GoodsTradesHistory na pole pro lepší zobrazení
     * @param item
     * @return
     */
    private Object[] dataAsArray(GoodsTradesHistory item) {
        return new Object[] {
                item.getNumber(),
                item.getDate(),
                item.getDph(),
                item.getPrice(),
                item.getQuantityWithSign(),
                item.getUnit(),
                item.getSuplierRealName(),
                item.getItemType().getDescription()
            };
    }
    
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return dataAsArray( data.get(rowIndex) )[columnIndex];
    }
    
}
