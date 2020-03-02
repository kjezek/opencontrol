/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui;

import cz.control.business.Settings;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 * Spoleèný cell renderer pro bìžné položky v tabulce
 * @author kamilos
 */
public class CommonItemCellRenderer extends JTextField implements TableCellRenderer {

    /**
     * Instance. 
     */
    public CommonItemCellRenderer() {
        this.setHorizontalAlignment(JTextField.LEFT);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setFont(new Font("Dialog", Font.PLAIN, Settings.getMainItemsFontSize()));
        setOpaque(true); // Zpùsobí pøebarvení pozadí pøi vybrání øádku
    }


    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (value == null) {
            this.setText("Nevyplnìno");
            this.setFont(new Font("Dialog", Font.ITALIC, Settings.getMainItemsFontSize()));
        } else {
            this.setText(value.toString());
        }

        /* Nastav barvy pøi vybrání øádku - použij defaultní barvy z tabulky */
        if (isSelected) {
            setBackground(table.getSelectionBackground()); // Barva pøi vybrání
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }

}
