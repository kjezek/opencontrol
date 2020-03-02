/*
 * UsePriceTableCellEditor.java
 *
 * Vytvoøeno 15. únor 2006, 16:45
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.gui.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*; 

import java.text.*;
import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída nastavuje, jak se budou editovat soupce pro výbìr ceny
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class UsePriceTableCellEditor  extends AbstractCellEditor implements TableCellEditor{

    private JRadioButton button = new JRadioButton();
    
    private static ButtonGroup group = new ButtonGroup();

    /**
     * Vytvoøí nový objekt QuantityCellEditor. Slouží jako editor v tabulce zboží
     */
    public  UsePriceTableCellEditor() {
        group.add(button);
        button.setHorizontalAlignment(JRadioButton.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder());

    }
    
    /**
     * Vrací hodnotu v tabulce po editaci
     * @return Hodnota uživatelem nastavená v tabulce
     */
    public Object getCellEditorValue() {

        return button.isSelected();
    }
    
    /**
     * Vrací komponentu, která tvoøí editor buòky tabulky
     * @param table Reference na tabulku
     * @param value Hodnota v buòce tabulky
     * @param isSelected stav vybrání buòky v tabulce
     * @param row øádek tabulky
     * @param column sloupec tabulky
     * @return komponentu pøedstaujcí editor tabulky
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        button.setSelected( Boolean.valueOf( String.valueOf(value) ) );
        
        button.setBackground(table.getBackground()); // Barva pøi vybrání

        return button;
    }
        
}