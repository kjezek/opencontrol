/*
 * PriceTableModel.java
 *
 * Created on 4. ��jen 2005, 23:05
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
 * T��da p�edstavuj�c� model pro komponentu JTabel. 
 * Slou�� pro tabulku cen, zobrazovanou v programu
 *
 * @author Kamil Je�ek
 */
public class PriceTableModel extends AbstractTableModel {
    
    public static final int DEFAULT_ROW_COUNT = 5;
    public static final int DEFAULT_COLUMN_COUNT = 3;
        
    public static final BigDecimal ONE = new BigDecimal(1);
    
    private String[] columnNames;
    private Object[][] data = { {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f} };              
    private BigDecimal dph = new BigDecimal(0.0f); // Hodnota DPH zvolen�ho zbo�� 
    private PriceList priceList = null; // cen�k
    
    private boolean computeDPH = false;

    /**
     * Vytvo�� model tabulky s odpov�daj�c�mi cenami pro konkr�tn� zbo��
     * @param goods zbo�� pro kter� maj� b�t ceny nastaveny
    */
    public PriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        // Nastav defaultn� hodnoty
        String[] colName = {"N�zev", "K� (bez DPH)", "K� (s DPH) "};        
        setColumnNames(colName);
        setRowCount(DEFAULT_ROW_COUNT);
        
        setData(nc, pcA, pcB, pcC, pcD, DPH);
    }
    
    /**
     *  Vytvo�� pr�zdnou tabulku
     */
    public PriceTableModel() {
        
    }
    
    /**
     * Vytvo�� tabulka s pou��t�m jmen slooupc� a o zadan�m po�tu ��dk�
     * @param names N�zvy sloupc�
     * @param rowCount Po�et ��dk�
     */
    public PriceTableModel(String[] names, int rowCount) {
        setColumnNames(names);
        setRowCount(rowCount);        
    }
    
    /**
     * Vytvo�� tabulu, tak �e pou�ije n�kupn� cenu a s n� dopo�te podle cen�ku
     * prodejn� ceny
     * @param nc n�kupn� cena
     * @param DPH DPH
     * @param priceList cen�k
     */
    public PriceTableModel(int nc, int DPH, PriceList priceList) {
        this.priceList = priceList;
        this.setDph(new BigDecimal(DPH).divide(Store.CENT));
        
        setValueAt(nc, 0, PriceTableColumns.PRICE.getNumber()); //Nastav NC, zbytek se dopo��t�
    }
    
    /**
     * Vytvo�� tabulka s pou��t�m jmen sloupce a o zadan�m po�tu ��dk�
     * @param names N�zvy sloupc�
     * @param rowCount Po�et ��dk�
     */
    public void createTable(String[] names, int rowCount) {
        setColumnNames(names);
        setRowCount(rowCount);        
    }
    
    /**
     * Nastav� n�zvy sloupc� tabulky
     * @param names pole s n�zvy sloupc�
     */
    public void setColumnNames(String[] names) {
        columnNames = new String[names.length];
        
        // vytvo� pole n�zv�
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = names[i];
        }
    }
    
    
    /**
     * Nastav� po�et ��dk� v tabulce. P�ed vol�n�m t�to metody je vhodn� 
     * nastavit n�zvy sloupc� metodou <code>setColumnNames(String[] names)</code>,
     * nebo� podle po�tu n�zv� tato funkce rozpozn�v� po�et sloupc�
     *
     * @param count po�et ��dk� v tabulce
     */
    private void setRowCount(int count) {
        data = new Object[count][]; // Pole velik� podle ��dk�
        
        // Pro ka�d� ��dek vytvo� jednotliv� bu�ku
        for (int i = 0; i < data.length; i++) {
            // Bu�ek bude podle toho, kolik je n�zv� sloupc�
            data[i] = new Object[columnNames.length];
        }
    }
        
    /**
     * Nastav� tabulku odpov�daj�c�mi cenami pro konkr�tn� zbo��
     * @param nc n�kupn� cena
     * @param pcA prodejn� cena A
     * @param pcB prodejn� cena B
     * @param pcC prodejn� cena C
     * @param pcD prodejn� cena D
     * @param DPH da�
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
     * Nastav� cen�k, podle kter�ho se bude po��tat PC ze zadan� NC.
     * Z�rove� provede p�epo�et cen podle nov�ho cen�ku 
     * @param priceList cen�k, kter� se pou�ije
     */
    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }
    
    /**
     * Aktualizuje ceny v tabulce po zm�ne cen�ku. 
     * Znovunastav� bu�ku s NC, co� zp�sob� p�epo�et PC
     */
    public void refreschPrices() {
        setValueAt( getValueAt(0, 1), 0, 1); //Aktualizuj jednu bu�ku NC -> aktualizuje zbytek tabulky
    }
    
    /**
     *  Provede znovuna�ten� jmen u cen zbo��
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
     * Nastav� do tabulky pouze n�kupn� cenu bez DPH. Neprov�d� v�po�et PC,
     * ani kdy� je zad�n cen�k
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
     * Vrac� po�et sloupc� tabulky
     * @return po�et sloupc� tabulky 
     */
    public int getColumnCount() {
        return columnNames.length;
    }
        
    /**
     * Vrac� po�et ��dk� tabulky
     * @return po�et sloupc� tabulky 
     */
    public int getRowCount() {
        return data.length;
    }
        
    /**
     * Vrac� hodnotu na pozici v tabulce
     * @param row ��dek v tabulce
     * @param column sloupec v tabulce
     * @return objekt ulo�en� na dan� pozici
     */
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }
        
    /**
     * Nastavuje hodnotu na pozici v tabulce
     * @param val objekt, kter� s m� ulo�it
     * @param row ��dek kam ulo�it
     * @param column sloupec kam ulo�it 
     */
    public void setValueAt(Object val, int row, int column) {
        data[row][column] = val;
        
        BigDecimal dphPlusOne = getDph().add(ONE);
        
        
        // Jestli�e se edituje cena bez dan�
        if (column == PriceTableColumns.PRICE.getNumber() && !computeDPH ) { 
            Double result = (new BigDecimal( String.valueOf(val)) ).multiply( dphPlusOne ).doubleValue(); // p�i�ti da�
            computeDPH = true; //zabra�uje zacyklen�
            setValueAt(result, row, column + 1); // Zm�n i n�sleduj�c� sloupec
        }
        
        // Jestli�e se edituje cena s dan�
        if (column == PriceTableColumns.PRICE_DPH.getNumber() && !computeDPH ){
            if (column == PriceTableColumns.PRICE_DPH.getNumber()) { // Jestli�e se edituje cena s dan�
                BigDecimal bd = (new BigDecimal( String.valueOf(val)) ).divide( dphPlusOne, new MathContext(100) );
                Double result = bd.doubleValue(); // ode�ti da�
                computeDPH = true; //zabra�uje zacyklen�
                setValueAt(result, row, column - 1); // Zm�n i p�edchoz� sloupec
            }
        }
        
        //Jestli�e je nastaven cen�k a edituje se NC, dopo��tej PC
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
     * Vrac� jm�no sloupce
     * @param column �islo sloucpe
     * @return n�zev sloupce
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }
        
    /**
     * Vrac� typ sloupce v tabulce
     * @param column ��slo sloupce
     * @return T��da p�edstavuj�c� typ hodnoty ve sloupci
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
