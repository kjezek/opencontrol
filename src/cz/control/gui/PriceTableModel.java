/*
 * PriceTableModel.java
 *
 * Created on 4. øíjen 2005, 23:05
 */

package cz.control.gui;

import cz.control.data.PriceList;
import cz.control.business.*;
import cz.control.gui.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

import javax.swing.table.*;

import static cz.control.business.Settings.*;


/**
 * Tøída pøedstavující model pro komponentu JTabel. 
 * Slouí pro tabulku cen, zobrazovanou v programu
 *
 * @author Kamil Jeek
 */
public class PriceTableModel extends AbstractTableModel {
    
    public static final int DEFAULT_ROW_COUNT = 5;
    public static final int DEFAULT_COLUMN_COUNT = 3;
        
    public static final BigDecimal ONE = new BigDecimal(1);
    
    private String[] columnNames;
    private Object[][] data = { {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f} };              
    private BigDecimal dph = new BigDecimal(0.0f); // Hodnota DPH zvoleného zboí 
    private PriceList priceList = null; // ceník
    
    private boolean computeDPH = false;

    /**
     * Vytvoøí model tabulky s odpovídajícími cenami pro konkrétní zboí
     * @param goods zboí pro které mají bıt ceny nastaveny
    */
    public PriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        // Nastav defaultní hodnoty
        String[] colName = {"Název", "Kè (bez DPH)", "Kè (s DPH) "};        
        setColumnNames(colName);
        setRowCount(DEFAULT_ROW_COUNT);
        
        setData(nc, pcA, pcB, pcC, pcD, DPH);
    }
    
    /**
     *  Vytvoøí prázdnou tabulku
     */
    public PriceTableModel() {
        
    }
    
    /**
     * Vytvoøí tabulka s pouítím jmen slooupcù a o zadaném poètu øádkù
     * @param names Názvy sloupcù
     * @param rowCount Poèet øádkù
     */
    public PriceTableModel(String[] names, int rowCount) {
        setColumnNames(names);
        setRowCount(rowCount);        
    }
    
    /**
     * Vytvoøí tabulu, tak e pouije nákupní cenu a s ní dopoète podle ceníku
     * prodejní ceny
     * @param nc nákupní cena
     * @param DPH DPH
     * @param priceList ceník
     */
    public PriceTableModel(int nc, int DPH, PriceList priceList) {
        this.priceList = priceList;
        this.setDph(new BigDecimal(DPH).divide(Store.CENT));
        
        setValueAt(nc, 0, PriceTableColumns.PRICE.getNumber()); //Nastav NC, zbytek se dopoèítá
    }
    
    /**
     * Vytvoøí tabulka s pouítím jmen sloupce a o zadaném poètu øádkù
     * @param names Názvy sloupcù
     * @param rowCount Poèet øádkù
     */
    public void createTable(String[] names, int rowCount) {
        setColumnNames(names);
        setRowCount(rowCount);        
    }
    
    /**
     * Nastaví názvy sloupcù tabulky
     * @param names pole s názvy sloupcù
     */
    public void setColumnNames(String[] names) {
        columnNames = new String[names.length];
        
        // vytvoø pole názvù
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = names[i];
        }
    }
    
    
    /**
     * Nastaví poèet øádkù v tabulce. Pøed voláním této metody je vhodné 
     * nastavit názvy sloupcù metodou <code>setColumnNames(String[] names)</code>,
     * nebo podle poètu názvù tato funkce rozpoznává poèet sloupcù
     *
     * @param count poèet øádkù v tabulce
     */
    private void setRowCount(int count) {
        data = new Object[count][]; // Pole veliké podle øádkù
        
        // Pro kadı øádek vytvoø jednotlivé buòku
        for (int i = 0; i < data.length; i++) {
            // Buòek bude podle toho, kolik je názvù sloupcù
            data[i] = new Object[columnNames.length];
        }
    }
        
    /**
     * Nastaví tabulku odpovídajícími cenami pro konkrétní zboí
     * @param nc nákupní cena
     * @param pcA prodejní cena A
     * @param pcB prodejní cena B
     * @param pcC prodejní cena C
     * @param pcD prodejní cena D
     * @param DPH daò
     */
    public void setData(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
             
        this.setDph(new BigDecimal(DPH).divide(Store.CENT));
        BigDecimal cellValue;
                
        data[0][0] = getNcName(); 
        data[1][0] = getPcAName(); 
        data[2][0] = getPcBName(); 
        data[3][0] = getPcCName(); 
        data[4][0] = getPcDName(); 
        
        cellValue = (new BigDecimal(nc)).divide(Store.CENT);
        data[0][1] = cellValue.doubleValue(); 
        data[0][2] = cellValue.multiply( getDph().add(ONE) ).doubleValue();
        
        cellValue = (new BigDecimal(pcA)).divide(Store.CENT);
        data[1][1] = cellValue.doubleValue(); 
        data[1][2] = cellValue.multiply( getDph().add(ONE) ).doubleValue();
        
        cellValue = (new BigDecimal(pcB)).divide(Store.CENT);
        data[2][1] = cellValue.doubleValue(); 
        data[2][2] = cellValue.multiply( getDph().add(ONE) ).doubleValue();
        
        cellValue = (new BigDecimal(pcC)).divide(Store.CENT);
        data[3][1] = cellValue.doubleValue(); 
        data[3][2] = cellValue.multiply( getDph().add(ONE) ).doubleValue();
        
        cellValue = (new BigDecimal(pcD)).divide(Store.CENT);
        data[4][1] = cellValue.doubleValue(); 
        data[4][2] = cellValue.multiply( getDph().add(ONE) ).doubleValue();
        
        
        fireTableDataChanged();
    }
    
    /**
     * Nastaví ceník, podle kterého se bude poèítat PC ze zadané NC.
     * Zároveò provede pøepoèet cen podle nového ceníku 
     * @param priceList ceník, kterı se pouije
     */
    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }
    
    /**
     * Aktualizuje ceny v tabulce po zmìne ceníku. 
     * Znovunastaví buòku s NC, co zpùsobí pøepoèet PC
     */
    public void refreschPrices() {
        setValueAt( getValueAt(0, 1), 0, 1); //Aktualizuj jednu buòku NC -> aktualizuje zbytek tabulky
    }
    
    /**
     *  Provede znovunaètení jmen u cen zboí
     */
    public void refreshPriceNames() {
        data[0][0] = getNcName(); 
        data[1][0] = getPcAName(); 
        data[2][0] = getPcBName(); 
        data[3][0] = getPcCName(); 
        data[4][0] = getPcDName();   
        fireTableDataChanged();
    }
    
    /**
     * Nastaví do tabulky pouze nákupní cenu bez DPH. Neprovádí vıpoèet PC,
     * ani kdy je zadán ceník
     * @param nc 
     */
    public void setOnlyNC(int nc) {
        PriceList tmp = priceList;
        
        setPriceList(null);
        double cellValue = (new BigDecimal(nc)).divide(Store.CENT).doubleValue();
        setValueAt(cellValue, 0, PriceTableColumns.PRICE.getNumber());
        setPriceList(tmp);
        
    }
        
    /**
     * Vrací poèet sloupcù tabulky
     * @return poèet sloupcù tabulky 
     */
    public int getColumnCount() {
        return columnNames.length;
    }
        
    /**
     * Vrací poèet øádkù tabulky
     * @return poèet sloupcù tabulky 
     */
    public int getRowCount() {
        return data.length;
    }
        
    /**
     * Vrací hodnotu na pozici v tabulce
     * @param row øádek v tabulce
     * @param column sloupec v tabulce
     * @return objekt uloenı na dané pozici
     */
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }
        
    /**
     * Nastavuje hodnotu na pozici v tabulce
     * @param val objekt, kterı s má uloit
     * @param row øádek kam uloit
     * @param column sloupec kam uloit 
     */
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        
        BigDecimal dphPlusOne = getDph().add(ONE);
        
        
        // Jestlie se edituje cena bez danì
        if (column == PriceTableColumns.PRICE.getNumber() && !computeDPH ) { 
            Double result = (new BigDecimal( String.valueOf(val)) ).multiply( dphPlusOne ).doubleValue(); // pøièti daò
            computeDPH = true; //zabraòuje zacyklení
            setValueAt(result, row, column + 1); // Zmìn i následující sloupec
        }
        
        // Jestlie se edituje cena s daní
        if (column == PriceTableColumns.PRICE_DPH.getNumber() && !computeDPH ){
            if (column == PriceTableColumns.PRICE_DPH.getNumber()) { // Jestlie se edituje cena s daní
                BigDecimal bd = (new BigDecimal( String.valueOf(val)) ).divide( dphPlusOne, new MathContext(100) );
                Double result = bd.doubleValue(); // odeèti daò
                computeDPH = true; //zabraòuje zacyklení
                setValueAt(result, row, column - 1); // Zmìn i pøedchozí sloupec
            }
        }
        
        //Jestlie je nastaven ceník a edituje se NC, dopoèítej PC
        if (row == 0 && priceList != null && 
           (column == PriceTableColumns.PRICE.getNumber() || column == PriceTableColumns.PRICE_DPH.getNumber())) {
            
           BigDecimal nc = new BigDecimal( String.valueOf(val) );
           BigDecimal squareCent = Store.CENT.multiply(Store.CENT);
           Double pcA = nc.add(nc.multiply( new BigDecimal(priceList.getPcA()).divide(squareCent) )).doubleValue();
           Double pcB = nc.add(nc.multiply( new BigDecimal(priceList.getPcB()).divide(squareCent) )).doubleValue();
           Double pcC = nc.add(nc.multiply( new BigDecimal(priceList.getPcC()).divide(squareCent) )).doubleValue();
           Double pcD = nc.add(nc.multiply( new BigDecimal(priceList.getPcD()).divide(squareCent) )).doubleValue();
           
           // Nastav ceny
           setValueAt(pcA, 1, column);
           setValueAt(pcB, 2, column);
           setValueAt(pcC, 3, column);
           setValueAt(pcD, 4, column);
        }

        computeDPH = false;
        fireTableCellUpdated(row, column);
    }
        
    /**
     * Vrací jméno sloupce
     * @param column èislo sloucpe
     * @return název sloupce
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }
        
    /**
     * Vrací typ sloupce v tabulce
     * @param column èíslo sloupce
     * @return Tøída pøedstavující typ hodnoty ve sloupci
     */
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }        

    BigDecimal getDph() {
        return dph;
    }

    void setDph(BigDecimal dph) {
        this.dph = dph;
    }
        
}
