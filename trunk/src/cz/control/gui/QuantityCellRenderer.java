/*
 * QuantityCellRenderer.java
 *
 * Created on 8. øíjen 2005, 22:06
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
 * Program Control - Skladový systém
 *
 * Tøída nastavuje, jak se bude zobrazovat sloupec Množství v tabulce
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class QuantityCellRenderer  extends JTextField implements TableCellRenderer {
    
    private DecimalFormat df = Settings.getFloatFormat();
    
    private boolean useSign;
    
    /* Pøístup pouze v r ámci balíku */
    public QuantityCellRenderer() {
        this(false);
    }
    
    public QuantityCellRenderer(boolean useSign) {
        this.useSign = useSign;

        this.setHorizontalAlignment(JTextField.RIGHT);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setFont(new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        setOpaque(true); // Zpùsobí pøebarvení pozadí pøi vybrání øádku
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
        BigDecimal number;
        number = new BigDecimal( String.valueOf(value) ); // Pøeveï na desetiné èíslo
        String s = df.format(number); // Vytvoø øetìzec 
        
        // pokud se má vždy zobrazovat znaménko, u kladného èísla doplò plus
        if (useSign && number.signum() == 1) {
            s = "+" + s;
        }
        
        this.setText(s);
        
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
