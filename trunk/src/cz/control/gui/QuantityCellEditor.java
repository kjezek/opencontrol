/*
 * QuantityCellEditor.java
 *
 * Created on 9. ��jen 2005, 12:14
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
 * Program Control - Skladov� syst�m
 *
 * T��da nastavuje, jak se budou editovat soupce v tabulce zbo��
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class QuantityCellEditor  extends AbstractCellEditor implements TableCellEditor{
    private String previousValue;
    private JTextField textField = new JTextField();
    
    /** Signalizuje, �e hodnota byla pou�ita */
    private boolean wasUse = false; 
    
    private DecimalFormat df = Settings.getFloatFormat();

    /**
     * Vytvo�� nov� objekt QuantityCellEditor. Slou�� jako editor v tabulce zbo��
     */
    public  QuantityCellEditor() {
        textField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        textField.setFont(new Font("DialogInput", Font.BOLD, Settings.getMainItemsFontSize()));
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.addFocusListener( new TextFieldFocusListener() );
    }
    
    /**
     * Vrac� hodnotu v tabulce po editaci
     * @return Hodnota u�ivatelem nastaven� v tabulce
     */
    public Object getCellEditorValue() {

        String s = textField.getText();
        wasUse = true;
        
       if (s.equals("") || s.trim().length() == 0) {
            return previousValue; // Vra� posledn� spr�vnou hodnotu 
        }
        
        Number number;
        try { // zkus p�ev�st na ��slo 
            number = df.parse(textField.getText(), new ParsePosition(0));
            //double number = Double.valueOf(s); // p�eve� na ��slo
            s = String.valueOf(number); // p�eve� nazp�t

            if (number.doubleValue() < 0 ) // zaka� z�porn� hodnoty 
                throw new NumberFormatException();

        } catch (NumberFormatException e) {
            ErrorMessages errorMessages = new ErrorMessages(Errors.NOT_EXEPT_VALUE, "Hodnota \"" + textField.getText() + "\" je chybn�. <br> Zad�vejte nez�porn� ��sla");
            JOptionPane.showMessageDialog(null, errorMessages.getFormatedText(), errorMessages.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            
            return previousValue;
        }
        
        return s;
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
        previousValue = s; // Zapamatuj si hodnotu, pro p��pad, �e u�ivatel zad� chybn�
        textField.setText("");
//        textField.setText(s);
        
//        textField.setBackground(table.getSelectionBackground()); // Barva p�i vybr�n�
//        textField.setForeground(table.getSelectionForeground());
        
        textField.setBackground(Color.WHITE); // Barva p�i vybr�n�
        textField.setForeground(Color.BLACK);
        
        return textField;
    }
    
    /**
     *  Poslucha� zm�ny fokusu 
     */
    private class TextFieldFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            wasUse = false;
        }

        public void focusLost(FocusEvent e) {
            
            // Jesti�e ji� byla hodnota pou�ita, nesignalizuj nic 
            // (byla by toti� pou�ita 2x)
            if (wasUse)
                return;
            
            // jestli�e ztr�c� fokus, signalizuj �e se m� pou��t nov� hodnota 
            fireEditingStopped();
            
            // Tohle by signalizovalo zru�en� zm�ny
            //fireEditingCanceled()
        }
        
    }       
}
