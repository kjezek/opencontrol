/*
 * UsePriceTableRendere.java
 *
 * Vytvoøeno 15. únor 2006, 16:31
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;


/**
 * Program Control - Skladový systém
 *
 * Tøída nastavuje, jak se bude zobrazovat sloupec Informující, která cena se má oužít
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class UsePriceTableCellRenderer  extends JRadioButton implements TableCellRenderer {
    
    
    /**
     * Vytvoøí novou instanci UsePriceTableRendere
     * @param checked nastavuje, zda bude tlaèítko vybráno
     */
    public UsePriceTableCellRenderer() {
        this.setHorizontalAlignment(JRadioButton.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder());
    }
    
    /**
     * Vrací komponentu zobrazující buòku v tabulce
     * @param table tabulka
     * @param value hodnota, která se zobrazí v pøíslušné buòce
     * @param isSelected stav vybrání buòky
     * @param hasFocus fokus buòky
     * @param row øádek
     * @param column sloupec
     * @return Hodnota, která se zobrazí v tabulce
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        this.setSelected( Boolean.valueOf( String.valueOf(value) ) );

        
        if (hasFocus || isSelected) {
            setBackground(table.getSelectionBackground()); // Barva pøi vybrání
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground()); 
            setForeground(table.getForeground());
        }
        
        return this;
    }
    
}
