/*
 * UsePriceTableRendere.java
 *
 * Vytvo�eno 15. �nor 2006, 16:31
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;


/**
 * Program Control - Skladov� syst�m
 *
 * T��da nastavuje, jak se bude zobrazovat sloupec Informuj�c�, kter� cena se m� ou��t
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class UsePriceTableCellRenderer  extends JRadioButton implements TableCellRenderer {
    
    
    /**
     * Vytvo�� novou instanci UsePriceTableRendere
     * @param checked nastavuje, zda bude tla��tko vybr�no
     */
    public UsePriceTableCellRenderer() {
        this.setHorizontalAlignment(JRadioButton.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder());
    }
    
    /**
     * Vrac� komponentu zobrazuj�c� bu�ku v tabulce
     * @param table tabulka
     * @param value hodnota, kter� se zobraz� v p��slu�n� bu�ce
     * @param isSelected stav vybr�n� bu�ky
     * @param hasFocus fokus bu�ky
     * @param row ��dek
     * @param column sloupec
     * @return Hodnota, kter� se zobraz� v tabulce
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        this.setSelected( Boolean.valueOf( String.valueOf(value) ) );

        
        if (hasFocus || isSelected) {
            setBackground(table.getSelectionBackground()); // Barva p�i vybr�n�
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground()); 
            setForeground(table.getForeground());
        }
        
        return this;
    }
    
}
