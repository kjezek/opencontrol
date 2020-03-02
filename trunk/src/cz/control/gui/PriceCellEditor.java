/*
 * PriceCellEditor.java
 *
 * Created on 9. ��jen 2005, 12:14
 */

package cz.control.gui;

import cz.control.business.Settings;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*; 

import java.text.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da nastavuje, jak se budou editovat ceny v tabulk�ch cen
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class PriceCellEditor extends AbstractCellEditor implements TableCellEditor {
    
    private double previousValue = 0.0f;
    
    /** Signalizuje, �e hodnota byla pou�ita */
    private boolean wasUse = false; 
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private JTextField textField = new JTextField(); // Tuto komponentu vyu�ij k editaci 
    private JTable goodsTable;
    
    /* P��stup pouze v r �mci bal�ku */
    PriceCellEditor() {
        this( new CommonTable());
    }
    
    /* Vytvo�� objekt s informac� o tabulce zbo��, se kterou se spolupracuje */
    PriceCellEditor(JTable goodsTable) {
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        textField.setFont(new Font("DialogInput", Font.BOLD, Settings.getMainItemsFontSize()));
        textField.addFocusListener( new TextFieldFocusListener() );
        textField.setToolTipText("Zadejte ��stku a potvr�te kl�vesou Enter");
        
        this.goodsTable = goodsTable;
    }
    
    /**
     * Vrac� hodnotu v tabulce cen po editaci
     * @return Hodnota u�ivatelem nastaven� v tabulce
     */
    public Object getCellEditorValue() {
        Number number = df.parse(textField.getText(), new ParsePosition(0));
        wasUse = true;

        /* Jestli�e byla zad�na chybn� hodnota */
        if (number == null || number.floatValue() < 0)  {
            /* Na chybu "pr�zdn� �et�zec" neupozor�uj, na ostatn� chyby ano */
            if (textField.getText().equals("") == false ) {
                JOptionPane.showMessageDialog(null, "Chybn� zadan� hodnota \"" + textField.getText() + "\". Opravte zad�n�", "Neo�ek�van� hodnota", JOptionPane.ERROR_MESSAGE); 
            }
            return previousValue; // Vra� posledn� apr�vnou hodnotu 
        }
        
        return number.doubleValue();
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
        
        String s = String.valueOf(value);
        previousValue = Double.valueOf(s).doubleValue(); // Zapamatuj si hodnotu, pro p��pad, �e u�ivatel zad� chybn�
        textField.setText("");
//        textField.setText(s);
        
        textField.setBackground(table.getSelectionBackground()); // Barva p�i vybr�n�
        textField.setForeground(table.getSelectionForeground());
        
        return textField;
    }
    
    
    /**
     *  Poslucha� zm�ny fokusu 
     */
    private class TextFieldFocusListener implements FocusListener {
        int lastRow = -1;
        public void focusGained(FocusEvent e) {
            wasUse = false;
            lastRow = goodsTable.getSelectedRow();
        }

        public void focusLost(FocusEvent e) {
            
            // Jesti�e ji� byla hodnota pou�ita, nesignalizuj nic 
            // (byla by toti� pou�ita 2x)
            if (wasUse)
                return;
            
            // Jestli�e byl naposled vybr�n ��dek, vra� se p�ed potvrzen�m hodnoty
            // na n�j
            if (lastRow != -1) {
                goodsTable.setRowSelectionInterval(lastRow, lastRow);
            }
            
            // signalizuj �e se m� pou��t nov� hodnota 
            fireEditingStopped();

            // signalizuj zru�en� zm�ny
            //fireEditingCanceled();
        }
        
    }      
}