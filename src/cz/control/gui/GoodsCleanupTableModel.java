package cz.control.gui;

import cz.control.data.Goods;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for GoodsCleanupDialog. 
 * 
 * @author kamilos
 */
public class GoodsCleanupTableModel extends AbstractTableModel {
    
    /** Index of the column with the swith to delete / not delete*/
    public static final int SWITCH_COLUMN_INDEX = 2;
    
    private String[] columnNames = {"Skladov� ��slo", "N�zev", "Vymazat" };        
    private Object[][] data = {};    
    
    private Map<String, Goods> goodsMap = new HashMap<String, Goods>();

    
    /**
     * Nastav� seznam zbo��, kter� se ob�v� v tabulce
     * @param goods Seznam zbo��
     */
    public void setGoodsData(List<Goods> goods) {
        
        data = new Object[goods.size()][]; // vytvo� pole s po�tem ��dek jako je zbo�� na sklad�
        int index = 0;

        /* Projdi seznam zbo�� a ulo� hodnoty do tabulky */
        for (Goods i: goods) {
            data[index] = new Object[4]; // Vytvo� pole pro 4 polo�ky 
            data[index][0] = i.getGoodsID(); // Ulo� skladov� ��slo
            data[index][1] = i.getName(); // Ulo� n�zev zbo��
            data[index][2] = Boolean.TRUE;
            index++;
            
            goodsMap.put(i.getGoodsID(), i);
        }

        fireTableDataChanged();     
    }
    

    public int getRowCount() {
        return data.length;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int column) {
        return data[row][column];
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public boolean isCellEditable(int row, int column)  {
        return (column == SWITCH_COLUMN_INDEX);    
    }    
    
    @Override
    public Class getColumnClass(int column) {
        if (getRowCount() == 0) {
            return Object.class;
        } 
        
        return getValueAt(0, column).getClass();
    }      
    
    @Override
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        fireTableCellUpdated(row, column);
    }
    
    /**
     * It returns all selected goods from the table.
     * @return  the goods
     */
    public List<Goods> getSelectedGoods() {
        
        List<Goods> result = new LinkedList<Goods>();
        
        for (int i = 0; i < getRowCount(); i++) {
            Boolean selected = (Boolean) getValueAt(i, SWITCH_COLUMN_INDEX);
            if (Boolean.TRUE.equals(selected)) {
                String id = (String) getValueAt(i, 0);
                result.add(goodsMap.get(id));
            }
        }
        
        return result;
    }
    
    
}
