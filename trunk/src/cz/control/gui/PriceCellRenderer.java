/*
 * PriceCellRenderer.java
 *
 * Created on 8. øíjen 2005, 20:29
 */

package cz.control.gui;


import cz.control.business.Settings;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

import java.text.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída nastavuje, jak se budou zobrazovat ceny v tabulkách
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class PriceCellRenderer extends JTextField implements TableCellRenderer {
    
    
    public static DecimalFormat df = Settings.getPriceFormat();
    private boolean useSign = false;
    
    /* Pøístup pouze v rámci balíku */
    public PriceCellRenderer() {
        this(false);
    }

    public PriceCellRenderer(boolean useSign) {
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
        if (value == null) {
            this.setText("null");
            return this;
        }
        BigDecimal number;
        number = new BigDecimal( String.valueOf(value) ); // Pøeveï na desetiné èíslo
        String s = df.format(number); // Vytvoø øetìzec 
        
        // pokud se má vždy zobrazovat znaménko, u kladného èísla doplò plus
        if (useSign && number.signum() == 1) {
            s = "+" + s;
        }
        
        this.setText(s);
        
        if (hasFocus || isSelected) {
            setBackground(table.getSelectionBackground()); // Barva pøi vybrání
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground()); 
            setForeground(table.getForeground());
        }
        
        return this;
    }

}
