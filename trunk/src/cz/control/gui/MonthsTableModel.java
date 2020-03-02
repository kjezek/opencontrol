/*
 * MonthsTableModel.java
 *
 * Vytvoøeno 9. bøezen 2006, 16:56
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.data.MonthRecap;
import cz.control.business.*;
import java.math.BigDecimal;

import java.util.*;
import java.util.ArrayList;
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
public class MonthsTableModel extends AbstractTableModel {
    private String[] columnNames = {
        "Mìsíc", "Zaèátek", "Pøíjem", "Výdej", "Konec", "Uzavøeno" };        
    private Object[][] data = 
    {
        {" Leden ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Únor ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Bøezen ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Duben ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Kvìten ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Èerven ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Èervenec ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Srpen ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Záøí ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Øíjen ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Listopad ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Prosinec ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Celkem za rok", EMPTY, EMPTY, EMPTY, EMPTY, false },
    };    
    
    /**
     * èíslo sloupeèku s potvrzováním rekapitulace
     */
    public static final int CONFIRM_COLUMN = 5;
    /**
     * øádek se šouètem rekapitulací za rok. Zároveò poslední øádek tabulky
     */
    public static final int YEAR_ROW = 12;
    
    /**
     * Konstanta urèující, že pro zobrazení se má použít cena s DPH
     */
    public static final int PRICE_DPH = 1;
    /**
     * Konstanta urèující, že pro zobrazení se má použít cena bez DPH
     */
    public static final int PRICE = 0;
    /**
     * Konstanta urèující, že pro zobrazení se má použít DPH
     */
    public static final int DPH = 2;
    
    private int usePrice = PRICE;
    
    private static final double EMPTY = 0f;
    
    /**
     * Vytvoøí model tabulky
     */
    public MonthsTableModel() {
    }
    
    /**
     * Nastaví data pro celou tabulku
     * @param monthRecaps mìsíèní rekapitulace pro zobrazení v tabulce
     */
    public void setData(ArrayList<MonthRecap> monthRecaps) {
        
        // Jako první vymaž celou tabulku (kromì øádku se souètem za rok)
        for (int i = 0; i < YEAR_ROW; i++) {
            deleteRow(i);
        }
        
        // Projdi všechny mìsíce
        for (MonthRecap i: monthRecaps) {
            //Nastav jednu rekapitulaci do øádky
            setMonth(i);
        }
    }
    
    /**
     * Nastaví jednu mìsíèní rekapitulaci 
     * na pøíslušný øádek. Jako øádek se použije pøíslušný mìsíc podle data rekapitulace
     * @param monthRecap rekapitulace, která se nastaví na øádek
     */
    public void setMonth(MonthRecap monthRecap) {
        setMonth(monthRecap, monthRecap.getMonth());
    }
    
    /**
     * Nastaví jednu mìsíèní rekapitulaci 
     * na pøíslušný øádek. Jako øádek se použije vstupní parametr (nezávisle na datu rekapitulace)
     * 
     * @param row øádek, na který se má nastavit rekapitulace. Jestliže je rozdah mimo
     *  interval 0-12, nic nedìlá
     * @param monthRecap rekapitulace, která se nastaví na øádek. Jestliže je na vstupu
     * prázdná rekapitulace, vymaže øádek
     */
    public void setMonth(MonthRecap monthRecap, int row) {
        if (row < 0 && row > YEAR_ROW)
            return;
        
        if (monthRecap == null || monthRecap.getMonth() == -1) {
            deleteRow(row); // vynuluj øádek
        } else {
            fillRow(monthRecap, row); //vyplò øádek
        }
        
    }
    
    /**
     * Zpùsobí vymazání konkrétního øádku. Respektive vymazání hodnot na øádku
     * @param row øádek, kde se mají vymazat hodnoty
     */
    public void deleteRow(int row) {
        // Vyplò pole prázdnými øetìzci
        data[row][1] = EMPTY;
        data[row][2] = EMPTY;
        data[row][3] = EMPTY;
        data[row][4] = EMPTY;  
        
        data[row][CONFIRM_COLUMN] = false;
        
        fireTableDataChanged();
    }
    
    /**
     *  Vyplní øádek hodnotami z rekapitulace.
     *  Nesmí se jednat o prázdnou rekapitulaci
     */
    private void fillRow(MonthRecap monthRecap, int row) {
        
        long start = 0;
        long profit = 0;
        long release = 0;
        long end = 0;
        
        // Rozhodni kterou cenu použít
        switch (usePrice) {
            case PRICE :
                start = monthRecap.getStart();
                profit = monthRecap.getProfit();
                release = monthRecap.getRelease();
                end = monthRecap.getEnd();
                break;
            case PRICE_DPH :  
                start = monthRecap.getStartAndDPH();
                profit = monthRecap.getProfitAndDPH();
                release = monthRecap.getReleaseAndDPH();
                end = monthRecap.getEndAndDPH();
                break;
            case DPH :    
                start = monthRecap.getStartDPH();
                profit = monthRecap.getProfitDPH();
                release = monthRecap.getReleaseDPH();
                end = monthRecap.getEndDPH();
                break;
        }
                
        // Vyplò pole øetìzci
        data[row][1] = new BigDecimal(start).divide(Store.CENT).doubleValue();
        data[row][2] = new BigDecimal(release).divide(Store.CENT).doubleValue();
        data[row][3] = new BigDecimal(profit).divide(Store.CENT).doubleValue();
        data[row][4] = new BigDecimal(end).divide(Store.CENT).doubleValue();
        
        data[row][CONFIRM_COLUMN] = true;
        
        fireTableDataChanged();
    }
    
    /**
     *  Nastaví, která cena se má používat pro zobrazení 
     */
    public void usePrice(int usePrice) {
        this.usePrice = usePrice;
    }
    
    /**
     * Vrací zda je sloupec editovatelný.
     * Editovatelný je poslední slouec. Ale ne v posledním øádku
     * @param row øádek 
     * @param column sloupec
     * @return true/false
     */
    public boolean isCellEditable(int row, int column) {
        if (column == CONFIRM_COLUMN) 
            return true;
        
        return false;
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
