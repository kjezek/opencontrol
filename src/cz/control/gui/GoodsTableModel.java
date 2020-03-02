/*
 * GoodsTableModel.java
 *
 * Vytvoøeno 6. listopad 2005, 22:59
 *
 
 */

package cz.control.gui;

import cz.control.data.Goods;
import cz.control.business.*;

import java.util.*;
import javax.swing.table.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pro definici modelu tabulky se zbožím
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class GoodsTableModel extends AbstractTableModel {
    private String[] columnNames = {"Skladové èíslo", "Název", "Množství", "Jednotka" };        
    private Object[][] data = {};    
    
    private List<Goods> items = new ArrayList<Goods>();

    /**
     * Vytvoøí model tabulky
     */
    public GoodsTableModel() {
    }
    
    /**
     * Vytvoøí model tabulky
     * @param goods Seznam zboží, které se vloží do tabulky
     */
    public GoodsTableModel(List<Goods> goods) {
        setGoodsData(goods);
    }

    /**
     * Nastaví seznam zboží, které se obìví v tabulce
     * @param goods Seznam zboží
     */
    public void setGoodsData(List<Goods> goods) {
        this.items = new ArrayList<Goods>(goods);
        
        data = new Object[goods.size()][]; // vytvoø pole s poètem øádek jako je zboží na skladì
        int index = 0;

        /* Projdi seznam zboží a ulož hodnoty do tabulky */
        for (Goods i: goods) {
            data[index] = new Object[4]; // Vytvoø pole pro 4 položky 
            data[index][0] = i.getGoodsID(); // Ulož skladové èíslo
            data[index][1] = i.getName(); // Ulož název zboží
            data[index][2] = i.getQuantity(); // Ulož množství 
            data[index][3] = i.getUnit(); // Ulož množstevní jednotku 
            index++;
        }

        fireTableDataChanged();     
    }
    
    /**
     * Vloží jeden øádek na konec tabulky 
     * @param goods Zboží, které se má vložit
     * @return na který øádek bylo zboží vloženo
     */
    public int inserRow(Goods goods) {
        items.add(goods);
        Object[][] tmp = new Object[data.length + 1][]; // Vytvoø nové pole o jedna vìtší
        
        for (int i = 0; i < data.length; i++) {
            tmp[i] = new Object[4];
            tmp[i][0] = data[i][0]; // pøekopíruj pole
            tmp[i][1] = data[i][1];
            tmp[i][2] = data[i][2];
            tmp[i][3] = data[i][3];
        }
        tmp[data.length] = new Object[4];
        tmp[data.length][0] = goods.getGoodsID();
        tmp[data.length][1] = goods.getName();
        tmp[data.length][2] = goods.getQuantity();
        tmp[data.length][3] = goods.getUnit();
        
        data = tmp; // nastav nová data
        
        fireTableDataChanged(); 
        
        return (data.length - 1);
    }
    
    /**
     * Nastaví na pøíslušný øádek hodnoty nového zboží
     * @param goods zboží, které se vloží na øádek
     * @param row èíslo øádku
     */
    public void replaceRow(Goods goods, int row) {

        // Nahradí staré zboží
        items.set(row, goods);

        setValueAt(goods.getGoodsID(), row, 0);
        setValueAt(goods.getName(), row, 1);
        setValueAt(goods.getQuantity(),row, 2);
        setValueAt(goods.getUnit(), row, 3);
    }
    
    /**
     * Zamìní øádek se starým zbožím za nové. Jestliže staré zboží neexistuje,
     * doplní nový øádek
     *
     * @param newGoods zboží, které se vloží na øádek
     * @param oldGoods staré zboží, které se má nahradit
     */
    public void replaceRow(Goods oldGoods, Goods newGoods) {

        int row = items.indexOf(oldGoods);
        
        if (row == -1) {
            inserRow(newGoods);
            return;
        }
        items.set(row, newGoods);

        setValueAt(newGoods.getGoodsID(), row, 0);
        setValueAt(newGoods.getName(), row, 1);
        setValueAt(newGoods.getQuantity(),row, 2);
        setValueAt(newGoods.getUnit(), row, 3);
    }
    
    
    /**
     * Vymaže jeden øádek z tabulky
     * @param row èíslo øádky pošítáno od jednièky
     */
    public void deleteRow(int row) {
        items.remove(row);
        Object[][] tmp = new Object[data.length - 1][]; // Vytvoø nové pole o jedna menší
        
        int index = 0;
        
        for (int i = 0; i < data.length; i++) {
            if (i != row) { // Jestliže není na øádku, který se má vymazt
                tmp[index] = new Object[4];
                tmp[index][0] = data[i][0]; // pøekopíruj pole
                tmp[index][1] = data[i][1];
                tmp[index][2] = data[i][2];
                tmp[index][3] = data[i][3];
                index++;
            }
        }
        
        data = tmp; // nastav nová data
        
        fireTableDataChanged(); 
    }
    
    
    /**
     * Vrací položku na pøíslušném øádku
     * @param row 
     * @return 
     */
    public Goods getGoodsAt(int row) {
    
        return items.get(row);
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
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        fireTableCellUpdated(row, column);
    }

    /**
     * Vrátí název sloupce v tabulce
     * @param column èislo sloucpe
     * @return název sloupce
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Vrátí typ objektu, jaký sloupec obsahuje
     * @param column èíslo sloupce
     * @return Tøída, kterou sloupec obsahuje
     */
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
 }
    
