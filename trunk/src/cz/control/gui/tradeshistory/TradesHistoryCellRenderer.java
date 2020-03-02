/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui.tradeshistory;

import cz.control.business.Settings;
import cz.control.data.GoodsTradesHistory;
import cz.control.data.GoodsTradesHistory.ItemTypes;
import cz.control.gui.PriceCellRenderer;
import cz.control.gui.QuantityCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 * Cell render pro tabulku historie
 * @author kamilos
 */
public class TradesHistoryCellRenderer extends JTextField implements TableCellRenderer {
    public static QuantityCellRenderer QUANTITY_CELL_RENDERER = new QuantityCellRenderer(true);
    public static PriceCellRenderer PRICE_CELL_RENDERER = new PriceCellRenderer();
    
    public static Color DISCOUNT_COLOR = Color.BLUE;
    public static Color SALE_COLOR = new Color(0xFF0000);
    public static Color BUY_COLOR = new Color(0x008000);

    public TradesHistoryCellRenderer() {
//        this.setHorizontalAlignment(JTextField.RIGHT);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setFont(new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        setOpaque(true); // Zpùsobí pøebarvení pozadí pøi vybrání øádku
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (value == null) {
            this.setText("null");
            return this;
        }
        
        Component result;
        
        // Rozhodni zda použít nadøízený renderer, èi pøímo tento
        if ( column == TradesTableColumns.QUANTITY.getColumnNumber()) {
            
            result = QUANTITY_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (column == TradesTableColumns.PRICE.getColumnNumber()) {
            
            result = PRICE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
            this.setText(value + "");
            result = this;
        }
            
        TradesHistoryTableModel model = (TradesHistoryTableModel) table.getModel();
        GoodsTradesHistory itemAtRow =  model.getItemAt(row);
        
        // urèi barvu dle typu øádky
        switch (itemAtRow.getItemType()) {
            case BUY :
                result.setForeground(BUY_COLOR);
                break;
            case SALE :
                result.setForeground(SALE_COLOR);
                break;
            case DISCOUNT :
                result.setForeground(DISCOUNT_COLOR);
                break;
        }
        
        /* Nastav barvy pøi vybrání øádku - použij defaultní barvy z tabulky */
        if (isSelected) {
            result.setBackground(table.getSelectionBackground()); // Barva pøi vybrání
        } else {
            result.setBackground(table.getBackground()); 
        }
        
        // zarovnání
        if (value instanceof Number) {
            this.setHorizontalAlignment(JTextField.RIGHT);
        } else if (value instanceof Date ) {
            this.setHorizontalAlignment(JTextField.CENTER);
            String dateText = Settings.DATE_FORMAT.format(value);
            this.setText(dateText);
        } else {
            this.setHorizontalAlignment(JTextField.LEFT);
        } 

        return result;
    }

}
