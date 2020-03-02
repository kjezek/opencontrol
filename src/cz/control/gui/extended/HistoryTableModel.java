/*
 * HistoryTableModel.java
 *
 * Created on 8. duben 2007, 11:03
 *
 */

package cz.control.gui.extended;

import cz.control.data.Goods;
import cz.control.business.Settings;
import cz.control.business.Store;
import cz.control.gui.PriceCellRenderer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Kamilos
 */
public class HistoryTableModel extends AbstractTableModel {
    public static final DecimalFormat DF = PriceCellRenderer.df;
    
    private String[] header = new String[] {
        "Skladové èíslo", "Název", "Jednotka", "Typ", "DPH", "EAN",
        Settings.getNcName(), Settings.getPcAName(), Settings.getPcBName(),
        Settings.getPcCName(), Settings.getPcDName()
    };
    
    private List<Goods> items = new ArrayList<Goods>();
    private Object[][] itemsArray = {};
    
    /** Creates a new instance of HistoryTableModel */
    public HistoryTableModel() {
    }
    
    public void insertRows(List<Goods> rows) {
        items = rows;
        
        itemsArray = new Object[rows.size()][];
        int index = 0;
        for (Goods i: rows) {
            itemsArray[index] = new Object[header.length];
            itemsArray[index][0] = i.getGoodsID();
            itemsArray[index][1] = i.getName();
            itemsArray[index][2] = i.getUnit();
            itemsArray[index][3] = i.getType();
            itemsArray[index][4] = i.getDph();
            itemsArray[index][5] = i.getEan();
            itemsArray[index][6] = DF.format(new BigDecimal(i.getNc()).divide(Store.CENT));
            itemsArray[index][7] = DF.format(new BigDecimal(i.getPcA()).divide(Store.CENT));
            itemsArray[index][8] = DF.format(new BigDecimal(i.getPcB()).divide(Store.CENT));
            itemsArray[index][9] = DF.format(new BigDecimal(i.getPcC()).divide(Store.CENT));
            itemsArray[index][10] = DF.format(new BigDecimal(i.getPcD()).divide(Store.CENT));
            
            index++;
        }
        
        fireTableDataChanged();   
    }
    
    public Goods getGoodsAtRow(int row) {
        return items.get(row);
    }

    public int getRowCount() {
        return items.size();
    }

    public int getColumnCount() {
        return header.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return itemsArray[rowIndex][columnIndex];
    }
    
    /**
     * Vrátí název sloupce v tabulce
     * @param column èislo sloucpe
     * @return název sloupce
     */
    public String getColumnName(int column) {
        return header[column];
    }

    /**
     * Vrátí typ objektu, jaký sloupec obsahuje
     * @param column èíslo sloupce
     * @return Tøída, kterou sloupec obsahuje
     */
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }    
    
}
