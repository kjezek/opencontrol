/*
 * UsePriceTableCellEditor.java
 *
 * Vytvo�eno 15. �nor 2006, 16:45
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da nastavuje, jak se budou editovat soupce pro v�b�r ceny
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class UsePriceTableCellEditor  extends AbstractCellEditor implements TableCellEditor{

    private JRadioButton button = new JRadioButton();
    
    private static ButtonGroup group = new ButtonGroup();

    /**
     * Vytvo�� nov� objekt QuantityCellEditor. Slou�� jako editor v tabulce zbo��
     */
    public  UsePriceTableCellEditor() {
        group.add(button);
        button.setHorizontalAlignment(JRadioButton.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder());

    }
    
    /**
     * Vrac� hodnotu v tabulce po editaci
     * @return Hodnota u�ivatelem nastaven� v tabulce
     */
    public Object getCellEditorValue() {

        return button.isSelected();
    }
    
    /**
     * Vrac� komponentu, kter� tvo�� editor bu�ky tabulky
     * @param table Reference na tabulku
     * @param value Hodnota v bu�ce tabulky
     * @param isSelected stav vybr�n� bu�ky v tabulce
     * @param row ��dek tabulky
     * @param column sloupec tabulky
     * @return komponentu p�edstaujc� editor tabulky
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        button.setSelected( Boolean.valueOf( String.valueOf(value) ) );
        
        button.setBackground(table.getBackground()); // Barva p�i vybr�n�

        return button;
    }
        
}