/*
 * GoodsTableModel.java
 *
 * Vytvo�eno 6. listopad 2005, 22:59
 *
 
 */

package cz.control.gui;

import cz.control.data.Goods;
import cz.control.business.*;

import java.util.*;
import javax.swing.table.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro definici modelu tabulky se zbo��m
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class GoodsTableModel extends AbstractTableModel {
    private String[] columnNames = {"Skladov� ��slo", "N�zev", "Mno�stv�", "Jednotka" };        
    private Object[][] data = {};    
    
    private List<Goods> items = new ArrayList<Goods>();

    /**
     * Vytvo�� model tabulky
     */
    public GoodsTableModel() {
    }
    
    /**
     * Vytvo�� model tabulky
     * @param goods Seznam zbo��, kter� se vlo�� do tabulky
     */
    public GoodsTableModel(List<Goods> goods) {
        setGoodsData(goods);
    }

    /**
     * Nastav� seznam zbo��, kter� se ob�v� v tabulce
     * @param goods Seznam zbo��
     */
    public void setGoodsData(List<Goods> goods) {
        this.items = new ArrayList<Goods>(goods);
        
        data = new Object[goods.size()][]; // vytvo� pole s po�tem ��dek jako je zbo�� na sklad�
        int index = 0;

        /* Projdi seznam zbo�� a ulo� hodnoty do tabulky */
        for (Goods i: goods) {
            data[index] = new Object[4]; // Vytvo� pole pro 4 polo�ky 
            data[index][0] = i.getGoodsID(); // Ulo� skladov� ��slo
            data[index][1] = i.getName(); // Ulo� n�zev zbo��
            data[index][2] = i.getQuantity(); // Ulo� mno�stv� 
            data[index][3] = i.getUnit(); // Ulo� mno�stevn� jednotku 
            index++;
        }

        fireTableDataChanged();     
    }
    
    /**
     * Vlo�� jeden ��dek na konec tabulky 
     * @param goods Zbo��, kter� se m� vlo�it
     * @return na kter� ��dek bylo zbo�� vlo�eno
     */
    public int inserRow(Goods goods) {
        items.add(goods);
        Object[][] tmp = new Object[data.length + 1][]; // Vytvo� nov� pole o jedna v�t��
        
        for (int i = 0; i < data.length; i++) {
            tmp[i] = new Object[4];
            tmp[i][0] = data[i][0]; // p�ekop�ruj pole
            tmp[i][1] = data[i][1];
            tmp[i][2] = data[i][2];
            tmp[i][3] = data[i][3];
        }
        tmp[data.length] = new Object[4];
        tmp[data.length][0] = goods.getGoodsID();
        tmp[data.length][1] = goods.getName();
        tmp[data.length][2] = goods.getQuantity();
        tmp[data.length][3] = goods.getUnit();
        
        data = tmp; // nastav nov� data
        
        fireTableDataChanged(); 
        
        return (data.length - 1);
    }
    
    /**
     * Nastav� na p��slu�n� ��dek hodnoty nov�ho zbo��
     * @param goods zbo��, kter� se vlo�� na ��dek
     * @param row ��slo ��dku
     */
    public void replaceRow(Goods goods, int row) {

        // Nahrad� star� zbo��
        items.set(row, goods);

        setValueAt(goods.getGoodsID(), row, 0);
        setValueAt(goods.getName(), row, 1);
        setValueAt(goods.getQuantity(),row, 2);
        setValueAt(goods.getUnit(), row, 3);
    }
    
    /**
     * Zam�n� ��dek se star�m zbo��m za nov�. Jestli�e star� zbo�� neexistuje,
     * dopln� nov� ��dek
     *
     * @param newGoods zbo��, kter� se vlo�� na ��dek
     * @param oldGoods star� zbo��, kter� se m� nahradit
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
     * Vyma�e jeden ��dek z tabulky
     * @param row ��slo ��dky po��t�no od jedni�ky
     */
    public void deleteRow(int row) {
        items.remove(row);
        Object[][] tmp = new Object[data.length - 1][]; // Vytvo� nov� pole o jedna men��
        
        int index = 0;
        
        for (int i = 0; i < data.length; i++) {
            if (i != row) { // Jestli�e nen� na ��dku, kter� se m� vymazt
                tmp[index] = new Object[4];
                tmp[index][0] = data[i][0]; // p�ekop�ruj pole
                tmp[index][1] = data[i][1];
                tmp[index][2] = data[i][2];
                tmp[index][3] = data[i][3];
                index++;
            }
        }
        
        data = tmp; // nastav nov� data
        
        fireTableDataChanged(); 
    }
    
    
    /**
     * Vrac� polo�ku na p��slu�n�m ��dku
     * @param row 
     * @return 
     */
    public Goods getGoodsAt(int row) {
    
        return items.get(row);
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
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        fireTableCellUpdated(row, column);
    }

    /**
     * Vr�t� n�zev sloupce v tabulce
     * @param column �islo sloucpe
     * @return n�zev sloupce
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Vr�t� typ objektu, jak� sloupec obsahuje
     * @param column ��slo sloupce
     * @return T��da, kterou sloupec obsahuje
     */
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
 }
    
