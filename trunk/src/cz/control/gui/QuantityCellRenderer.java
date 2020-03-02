/*
 * QuantityCellRenderer.java
 *
 * Created on 8. ��jen 2005, 22:06
 */

package cz.control.gui;

import cz.control.business.Settings;
import cz.control.gui.*;
import java.math.BigDecimal;
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
 * T��da nastavuje, jak se bude zobrazovat sloupec Mno�stv� v tabulce
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class QuantityCellRenderer  extends JTextField implements TableCellRenderer {
    
    private DecimalFormat df = Settings.getFloatFormat();
    
    private boolean useSign;
    
    /* P��stup pouze v r �mci bal�ku */
    public QuantityCellRenderer() {
        this(false);
    }
    
    public QuantityCellRenderer(boolean useSign) {
        this.useSign = useSign;

        this.setHorizontalAlignment(JTextField.RIGHT);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setFont(new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        setOpaque(true); // Zp�sob� p�ebarven� pozad� p�i vybr�n� ��dku
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
        BigDecimal number;
        number = new BigDecimal( String.valueOf(value) ); // P�eve� na desetin� ��slo
        String s = df.format(number); // Vytvo� �et�zec 
        
        // pokud se m� v�dy zobrazovat znam�nko, u kladn�ho ��sla dopl� plus
        if (useSign && number.signum() == 1) {
            s = "+" + s;
        }
        
        this.setText(s);
        
        /* Nastav barvy p�i vybr�n� ��dku - pou�ij defaultn� barvy z tabulky */
        if (isSelected) {
            setBackground(table.getSelectionBackground()); // Barva p�i vybr�n�
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground()); 
            setForeground(table.getForeground());
        }
        
        return this;
    }
    
    
}
