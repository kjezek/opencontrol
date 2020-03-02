/*
 * PriceCellEditor.java
 *
 * Created on 9. øíjen 2005, 12:14
 */

package cz.control.gui;

import cz.control.business.Settings;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*; 

import java.text.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída nastavuje, jak se budou editovat ceny v tabulkách cen
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class PriceCellEditor extends AbstractCellEditor implements TableCellEditor {
    
    private double previousValue = 0.0f;
    
    /** Signalizuje, že hodnota byla použita */
    private boolean wasUse = false; 
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private JTextField textField = new JTextField(); // Tuto komponentu využij k editaci 
    private JTable goodsTable;
    
    /* Pøístup pouze v r ámci balíku */
    PriceCellEditor() {
        this( new CommonTable());
    }
    
    /* Vytvoøí objekt s informací o tabulce zboží, se kterou se spolupracuje */
    PriceCellEditor(JTable goodsTable) {
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        textField.setFont(new Font("DialogInput", Font.BOLD, Settings.getMainItemsFontSize()));
        textField.addFocusListener( new TextFieldFocusListener() );
        textField.setToolTipText("Zadejte èástku a potvrïte klávesou Enter");
        
        this.goodsTable = goodsTable;
    }
    
    /**
     * Vrací hodnotu v tabulce cen po editaci
     * @return Hodnota uživatelem nastavená v tabulce
     */
    public Object getCellEditorValue() {
        Number number = df.parse(textField.getText(), new ParsePosition(0));
        wasUse = true;

        /* Jestliže byla zadána chybná hodnota */
        if (number == null || number.floatValue() < 0)  {
            /* Na chybu "prázdný øetìzec" neupozoròuj, na ostatní chyby ano */
            if (textField.getText().equals("") == false ) {
                JOptionPane.showMessageDialog(null, "Chybnì zadaná hodnota \"" + textField.getText() + "\". Opravte zadání", "Neoèekávaná hodnota", JOptionPane.ERROR_MESSAGE); 
            }
            return previousValue; // Vra poslední aprávnou hodnotu 
        }
        
        return number.doubleValue();
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
        
        String s = String.valueOf(value);
        previousValue = Double.valueOf(s).doubleValue(); // Zapamatuj si hodnotu, pro pøípad, že uživatel zadá chybnì
        textField.setText("");
//        textField.setText(s);
        
        textField.setBackground(table.getSelectionBackground()); // Barva pøi vybrání
        textField.setForeground(table.getSelectionForeground());
        
        return textField;
    }
    
    
    /**
     *  Posluchaè zmìny fokusu 
     */
    private class TextFieldFocusListener implements FocusListener {
        int lastRow = -1;
        public void focusGained(FocusEvent e) {
            wasUse = false;
            lastRow = goodsTable.getSelectedRow();
        }

        public void focusLost(FocusEvent e) {
            
            // Jestiže již byla hodnota použita, nesignalizuj nic 
            // (byla by totiž použita 2x)
            if (wasUse)
                return;
            
            // Jestliže byl naposled vybrán øádek, vra se pøed potvrzením hodnoty
            // na nìj
            if (lastRow != -1) {
                goodsTable.setRowSelectionInterval(lastRow, lastRow);
            }
            
            // signalizuj že se má použít nová hodnota 
            fireEditingStopped();

            // signalizuj zrušení zmìny
            //fireEditingCanceled();
        }
        
    }      
}