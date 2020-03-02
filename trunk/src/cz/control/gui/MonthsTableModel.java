/*
 * MonthsTableModel.java
 *
 * Vytvo�eno 9. b�ezen 2006, 16:56
 *
 * Autor: Kamil Je�ek
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
public class MonthsTableModel extends AbstractTableModel {
    private String[] columnNames = {
        "M�s�c", "Za��tek", "P��jem", "V�dej", "Konec", "Uzav�eno" };        
    private Object[][] data = 
    {
        {" Leden ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" �nor ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" B�ezen ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Duben ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Kv�ten ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" �erven ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" �ervenec ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Srpen ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Z��� ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" ��jen ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Listopad ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Prosinec ", EMPTY, EMPTY, EMPTY, EMPTY, false },
        {" Celkem za rok", EMPTY, EMPTY, EMPTY, EMPTY, false },
    };    
    
    /**
     * ��slo sloupe�ku s potvrzov�n�m rekapitulace
     */
    public static final int CONFIRM_COLUMN = 5;
    /**
     * ��dek se �ou�tem rekapitulac� za rok. Z�rove� posledn� ��dek tabulky
     */
    public static final int YEAR_ROW = 12;
    
    /**
     * Konstanta ur�uj�c�, �e pro zobrazen� se m� pou��t cena s DPH
     */
    public static final int PRICE_DPH = 1;
    /**
     * Konstanta ur�uj�c�, �e pro zobrazen� se m� pou��t cena bez DPH
     */
    public static final int PRICE = 0;
    /**
     * Konstanta ur�uj�c�, �e pro zobrazen� se m� pou��t DPH
     */
    public static final int DPH = 2;
    
    private int usePrice = PRICE;
    
    private static final double EMPTY = 0f;
    
    /**
     * Vytvo�� model tabulky
     */
    public MonthsTableModel() {
    }
    
    /**
     * Nastav� data pro celou tabulku
     * @param monthRecaps m�s��n� rekapitulace pro zobrazen� v tabulce
     */
    public void setData(ArrayList<MonthRecap> monthRecaps) {
        
        // Jako prvn� vyma� celou tabulku (krom� ��dku se sou�tem za rok)
        for (int i = 0; i < YEAR_ROW; i++) {
            deleteRow(i);
        }
        
        // Projdi v�echny m�s�ce
        for (MonthRecap i: monthRecaps) {
            //Nastav jednu rekapitulaci do ��dky
            setMonth(i);
        }
    }
    
    /**
     * Nastav� jednu m�s��n� rekapitulaci 
     * na p��slu�n� ��dek. Jako ��dek se pou�ije p��slu�n� m�s�c podle data rekapitulace
     * @param monthRecap rekapitulace, kter� se nastav� na ��dek
     */
    public void setMonth(MonthRecap monthRecap) {
        setMonth(monthRecap, monthRecap.getMonth());
    }
    
    /**
     * Nastav� jednu m�s��n� rekapitulaci 
     * na p��slu�n� ��dek. Jako ��dek se pou�ije vstupn� parametr (nez�visle na datu rekapitulace)
     * 
     * @param row ��dek, na kter� se m� nastavit rekapitulace. Jestli�e je rozdah mimo
     *  interval 0-12, nic ned�l�
     * @param monthRecap rekapitulace, kter� se nastav� na ��dek. Jestli�e je na vstupu
     * pr�zdn� rekapitulace, vyma�e ��dek
     */
    public void setMonth(MonthRecap monthRecap, int row) {
        if (row < 0 && row > YEAR_ROW)
            return;
        
        if (monthRecap == null || monthRecap.getMonth() == -1) {
            deleteRow(row); // vynuluj ��dek
        } else {
            fillRow(monthRecap, row); //vypl� ��dek
        }
        
    }
    
    /**
     * Zp�sob� vymaz�n� konkr�tn�ho ��dku. Respektive vymaz�n� hodnot na ��dku
     * @param row ��dek, kde se maj� vymazat hodnoty
     */
    public void deleteRow(int row) {
        // Vypl� pole pr�zdn�mi �et�zci
        data[row][1] = EMPTY;
        data[row][2] = EMPTY;
        data[row][3] = EMPTY;
        data[row][4] = EMPTY;  
        
        data[row][CONFIRM_COLUMN] = false;
        
        fireTableDataChanged();
    }
    
    /**
     *  Vypln� ��dek hodnotami z rekapitulace.
     *  Nesm� se jednat o pr�zdnou rekapitulaci
     */
    private void fillRow(MonthRecap monthRecap, int row) {
        
        long start = 0;
        long profit = 0;
        long release = 0;
        long end = 0;
        
        // Rozhodni kterou cenu pou��t
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
                
        // Vypl� pole �et�zci
        data[row][1] = new BigDecimal(start).divide(Store.CENT).doubleValue();
        data[row][2] = new BigDecimal(release).divide(Store.CENT).doubleValue();
        data[row][3] = new BigDecimal(profit).divide(Store.CENT).doubleValue();
        data[row][4] = new BigDecimal(end).divide(Store.CENT).doubleValue();
        
        data[row][CONFIRM_COLUMN] = true;
        
        fireTableDataChanged();
    }
    
    /**
     *  Nastav�, kter� cena se m� pou��vat pro zobrazen� 
     */
    public void usePrice(int usePrice) {
        this.usePrice = usePrice;
    }
    
    /**
     * Vrac� zda je sloupec editovateln�.
     * Editovateln� je posledn� slouec. Ale ne v posledn�m ��dku
     * @param row ��dek 
     * @param column sloupec
     * @return true/false
     */
    public boolean isCellEditable(int row, int column) {
        if (column == CONFIRM_COLUMN) 
            return true;
        
        return false;
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
