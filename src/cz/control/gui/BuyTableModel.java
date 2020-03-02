/*
 * BuyTableModel.java
 *
 * Vytvoøeno 12. listopad 2005, 23:39
 *
 
 */

package cz.control.gui;

import cz.control.data.TradeItem;
import cz.control.business.*;
import java.math.BigDecimal;

import java.util.*;
import javax.swing.table.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pro definici modelu tabulky, která zobrazuje položky pøíjemky 
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil Ježek
 */
public class BuyTableModel extends AbstractTableModel {
    private String[] columnNames = {"Skladové èíslo", "Název", "DPH [%]", "Cena bez DPH", "Množství", "Jednotka" };        
    private Object[][] data = {};              

    
    /** Vytvoøí nový objekt BuyTableModel */
    public BuyTableModel() {
    }
    
    /**
     * Vytvoøí model tabulky
     * @param tradeItem položky pøíjemky, které se zobrazí v tabulce
     */
    public BuyTableModel(List<TradeItem> tradeItem) {
        setData(tradeItem);
    }

    /**
     * Nastaví seznam zboží, které se obìví v tabulce
     * @param tradeItem položky pøíjemky, které se zobrazí v tabulce
     */
    public void setData(List<TradeItem> tradeItem) {
        data = new Object[tradeItem.size()][]; // vytvoø pole s poètem øádek jako je zboží na skladì
        int index = 0;

        /* Projdi seznam zboží a ulož hodnoty do tabulky */
        for (TradeItem i: tradeItem) {
            data[index] = new Object[6]; // Vytvoø pole pro 4 položky 
            data[index][0] = i.getGoodsId(); // Ulož skladové èíslo
            data[index][1] = i.getName(); // Ulož název zboží
            data[index][2] = i.getDph(); // Ulož název zboží
            data[index][3] = (new BigDecimal(i.getPrice())).divide(Store.CENT).toString(); // Ulož název zboží
            data[index][4] = i.getQuantity(); // Ulož množství 
            data[index][5] = i.getUnit(); // Ulož množstevní jednotku 
            index++;
        }

        fireTableDataChanged();     
    }
    
    public String getGoodsIdAt(int row) {
        return (String) getValueAt(row, 0);
    }
    
    /**
     * Vrátí poèet sloupcù tabulky
     * @return poèet sloupcù tabulky
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Vrátí poèet øádkù tabulky
     * @return poèet sloupcù tabulky
     */
    public int getRowCount() {
        return data.length;
    }

    /**
     * Vrátí hodnotu v pøíslušné buòce tabulky
     * @param row øádek v tabulce
     * @param column sloupec v tabulce
     * @return objekt uložený na dané pozici
     */
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    /**
     * Nastaví hodnotu do pøíslušného øádku v tabulce
     * @param val objekt, který s má uložit
     * @param row øádek kam uložit
     * @param column sloupec kam uložit
     */
    @Override
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        fireTableCellUpdated(row, column);
    }

    /**
     * Vrátí název sloupce v tabulce
     * @param column èislo sloucpe
     * @return název sloupce
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Vrátí typ objektu, jaký sloupec obsahuje
     * @param column èíslo sloupce
     * @return Tøída, kterou sloupec obsahuje
     */
    @Override
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
 }
    

