/*
 * QuantityCellEditor.java
 *
 * Created on 9. øíjen 2005, 12:14
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.business.Settings;
import cz.control.gui.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*; 

import java.text.*;
import java.util.*;

/**
 * Program Control - Skladovı systém
 *
 * Tøída nastavuje, jak se budou editovat soupce v tabulce zboí
 *
 * @author Kamil Jeek
 *
 * (C) 2005, ver. 1.0
 */
public class QuantityCellEditor  extends AbstractCellEditor implements TableCellEditor{
    private String previousValue;
    private JTextField textField = new JTextField();
    
    /** Signalizuje, e hodnota byla pouita */
    private boolean wasUse = false; 
    
    private DecimalFormat df = Settings.getFloatFormat();

    /**
     * Vytvoøí novı objekt QuantityCellEditor. Slouí jako editor v tabulce zboí
     */
    public  QuantityCellEditor() {
        textField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        textField.setFont(new Font("DialogInput", Font.BOLD, Settings.getMainItemsFontSize()));
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.addFocusListener( new TextFieldFocusListener() );
    }
    
    /**
     * Vrací hodnotu v tabulce po editaci
     * @return Hodnota uivatelem nastavená v tabulce
     */
    public Object getCellEditorValue() {

        String s = textField.getText();
        wasUse = true;
        
       if (s.equals("") || s.trim().length() == 0) {
            return previousValue; // Vra poslední správnou hodnotu 
        }
        
        Number number;
        try { // zkus pøevést na èíslo 
            number = df.parse(textField.getText(), new ParsePosition(0));
            //double number = Double.valueOf(s); // pøeveï na èíslo
            s = String.valueOf(number); // pøeveï nazpìt

            if (number.doubleValue() < 0 ) // zaka záporné hodnoty 
                throw new NumberFormatException();

        } catch (NumberFormatException e) {
            ErrorMessages errorMessages = new ErrorMessages(Errors.NOT_EXEPT_VALUE, "Hodnota \"" + textField.getText() + "\" je chybná. <br> Zadávejte nezáporná èísla");
            JOptionPane.showMessageDialog(null, errorMessages.getFormatedText(), errorMessages.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            
            return previousValue;
        }
        
        return s;
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
        previousValue = s; // Zapamatuj si hodnotu, pro pøípad, e uivatel zadá chybnì
        textField.setText("");
//        textField.setText(s);
        
//        textField.setBackground(table.getSelectionBackground()); // Barva pøi vybrání
//        textField.setForeground(table.getSelectionForeground());
        
        textField.setBackground(Color.WHITE); // Barva pøi vybrání
        textField.setForeground(Color.BLACK);
        
        return textField;
    }
    
    /**
     *  Posluchaè zmìny fokusu 
     */
    private class TextFieldFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            wasUse = false;
        }

        public void focusLost(FocusEvent e) {
            
            // Jestie ji byla hodnota pouita, nesignalizuj nic 
            // (byla by toti pouita 2x)
            if (wasUse)
                return;
            
            // jestlie ztrácí fokus, signalizuj e se má pouít nová hodnota 
            fireEditingStopped();
            
            // Tohle by signalizovalo zrušení zmìny
            //fireEditingCanceled()
        }
        
    }       
}
