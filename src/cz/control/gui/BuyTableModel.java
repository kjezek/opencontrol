/*
 * BuyTableModel.java
 *
 * Vytvo�eno 12. listopad 2005, 23:39
 *
 
 */

package cz.control.gui;

import cz.control.data.TradeItem;
import cz.control.business.*;
import java.math.BigDecimal;

import java.util.*;
import javax.swing.table.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro definici modelu tabulky, kter� zobrazuje polo�ky p��jemky 
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil Je�ek
 */
public class BuyTableModel extends AbstractTableModel {
    private String[] columnNames = {"Skladov� ��slo", "N�zev", "DPH [%]", "Cena bez DPH", "Mno�stv�", "Jednotka" };        
    private Object[][] data = {};              

    
    /** Vytvo�� nov� objekt BuyTableModel */
    public BuyTableModel() {
    }
    
    /**
     * Vytvo�� model tabulky
     * @param tradeItem polo�ky p��jemky, kter� se zobraz� v tabulce
     */
    public BuyTableModel(List<TradeItem> tradeItem) {
        setData(tradeItem);
    }

    /**
     * Nastav� seznam zbo��, kter� se ob�v� v tabulce
     * @param tradeItem polo�ky p��jemky, kter� se zobraz� v tabulce
     */
    public void setData(List<TradeItem> tradeItem) {
        data = new Object[tradeItem.size()][]; // vytvo� pole s po�tem ��dek jako je zbo�� na sklad�
        int index = 0;

        /* Projdi seznam zbo�� a ulo� hodnoty do tabulky */
        for (TradeItem i: tradeItem) {
            data[index] = new Object[6]; // Vytvo� pole pro 4 polo�ky 
            data[index][0] = i.getGoodsId(); // Ulo� skladov� ��slo
            data[index][1] = i.getName(); // Ulo� n�zev zbo��
            data[index][2] = i.getDph(); // Ulo� n�zev zbo��
            data[index][3] = (new BigDecimal(i.getPrice())).divide(Store.CENT).toString(); // Ulo� n�zev zbo��
            data[index][4] = i.getQuantity(); // Ulo� mno�stv� 
            data[index][5] = i.getUnit(); // Ulo� mno�stevn� jednotku 
            index++;
        }

        fireTableDataChanged();     
    }
    
    public String getGoodsIdAt(int row) {
        return (String) getValueAt(row, 0);
    }
    
    /**
     * Vr�t� po�et sloupc� tabulky
     * @return po�et sloupc� tabulky
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Vr�t� po�et ��dk� tabulky
     * @return po�et sloupc� tabulky
     */
    public int getRowCount() {
        return data.length;
    }

    /**
     * Vr�t� hodnotu v p��slu�n� bu�ce tabulky
     * @param row ��dek v tabulce
     * @param column sloupec v tabulce
     * @return objekt ulo�en� na dan� pozici
     */
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    /**
     * Nastav� hodnotu do p��slu�n�ho ��dku v tabulce
     * @param val objekt, kter� s m� ulo�it
     * @param row ��dek kam ulo�it
     * @param column sloupec kam ulo�it
     */
    @Override
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        fireTableCellUpdated(row, column);
    }

    /**
     * Vr�t� n�zev sloupce v tabulce
     * @param column �islo sloucpe
     * @return n�zev sloupce
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Vr�t� typ objektu, jak� sloupec obsahuje
     * @param column ��slo sloupce
     * @return T��da, kterou sloupec obsahuje
     */
    @Override
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
 }
    

