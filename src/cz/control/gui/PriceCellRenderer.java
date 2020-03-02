/*
 * PriceCellRenderer.java
 *
 * Created on 8. ��jen 2005, 20:29
 */

package cz.control.gui;


import cz.control.business.Settings;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

import java.text.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da nastavuje, jak se budou zobrazovat ceny v tabulk�ch
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class PriceCellRenderer extends JTextField implements TableCellRenderer {
    
    
    public static DecimalFormat df = Settings.getPriceFormat();
    private boolean useSign = false;
    
    /* P��stup pouze v r�mci bal�ku */
    public PriceCellRenderer() {
        this(false);
    }

    public PriceCellRenderer(boolean useSign) {
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
        if (value == null) {
            this.setText("null");
            return this;
        }
        BigDecimal number;
        number = new BigDecimal( String.valueOf(value) ); // P�eve� na desetin� ��slo
        String s = df.format(number); // Vytvo� �et�zec 
        
        // pokud se m� v�dy zobrazovat znam�nko, u kladn�ho ��sla dopl� plus
        if (useSign && number.signum() == 1) {
            s = "+" + s;
        }
        
        this.setText(s);
        
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
