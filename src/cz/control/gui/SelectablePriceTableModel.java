/*
 * SelectablePriceTableModel.java
 *
 * Vytvoøeno 15. únor 2006, 13:12
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.business.*;

/**
 * Program Control - Skladový systém
 *
 *  Model pro tabulku cen u které lze nastavovat, která z cen se má použít pro prodej
 *
 * (C) 2005, ver. 1.0
 */
public class SelectablePriceTableModel extends EditablePriceTableModel {           
    
    boolean confirmEdit = true;
    private int usePrice = 0;
    
    /**
     *  Vytvoøí prázdnou tabulku
     */ 
    public SelectablePriceTableModel() {
        super();
    }

    /**
     * Vytvoøí tabulka s použítím jmen slooupcù a o zadaném poètu øádkù
     * @param names Názvy sloupcù
     * @param rowCount Poèet øádkù
     */
    public SelectablePriceTableModel(String[] names, int rowCount) {
        super(names, rowCount);
    }  
    
    /**
     * Vytvoøí model tabulky s odpovídajícími cenami pro konkrétní zboží.
     * Zvýrazní defaultní prodejní cenu
     * @param nc nákupní cena
     * @param pcA prdejní cena A
     * @param pcB prdejní cena B
     * @param pcC prdejní cena C
     * @param pcD prdejní cena D
     * @param DPH DPH
     */
    public SelectablePriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        this(nc, pcA, pcB, pcC, pcD, DPH, Settings.getDefaultSalePrice());
    }
    
    /**
     * Vytvoøí model tabulky s odpovídajícími cenami pro konkrétní zboží.
     * Zvýrazní defaultní prodejní cenu
     * @param nc nákupní cena
     * @param pcA prdejní cena A
     * @param pcB prdejní cena B
     * @param pcC prdejní cena C
     * @param pcD prdejní cena D
     * @param DPH DPH
     */
    public SelectablePriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH, int usePrice) {
        // Nastav poèáteèní hodnoty
        String[] colNames = {"Název", "Kè (bez DPH)", "Kè (s DPH) ", "Použít cenu"};        
        createTable(colNames, PriceTableModel.DEFAULT_ROW_COUNT);

        setData(nc, pcA, pcB, pcC, pcD, DPH, usePrice);
        
        this.usePrice = usePrice;
    }
    
    /**
     *  Nastaví ceny v tabulce. Zvýraznìní ceny nemìní
     * @param nc nákupní cena
     * @param pcA prdejní cena A
     * @param pcB prdejní cena B
     * @param pcC prdejní cena C
     * @param pcD prdejní cena D
     * @param DPH DPH     
     * @param usePrice konstanta urèující jaká cena se má zvýraznit
     * DoBuy.USE_NC_FOR_SUM -  Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít Nákupní cena (NC)
     * DoBuy.USE_PCA_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít první Prodejní cena (PC A)
     * DoBuy.USE_PCB_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít druhá Prodejní cena (PC B)
     * DoBuy.USE_PCC_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít tøetí Prodejní cena (PC C)
     * DoBuy.USE_PCD_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít ètvrtá Prodejní cena (PC D)
     */
    public void setData(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        super.setData(nc, pcA, pcB, pcC, pcD, DPH);
    }
    
    /**
     *  Nastaví data v tabulce a vybere pøíslušnou cenu u které se objeví zaškrtnutí
     * @param nc nákupní cena
     * @param pcA prdejní cena A
     * @param pcB prdejní cena B
     * @param pcC prdejní cena C
     * @param pcD prdejní cena D
     * @param DPH DPH
     * @param usePrice konstanta urèující jaká cena se má zvýraznit
     * DoBuy.USE_NC_FOR_SUM -  Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít Nákupní cena (NC)
     * DoBuy.USE_PCA_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít první Prodejní cena (PC A)
     * DoBuy.USE_PCB_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít druhá Prodejní cena (PC B)
     * DoBuy.USE_PCC_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít tøetí Prodejní cena (PC C)
     * DoBuy.USE_PCD_FOR_SUM - Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít ètvrtá Prodejní cena (PC D)

     */
    public void setData(int nc, int pcA, int pcB, int pcC, int pcD, int DPH, int usePrice) {
        this.usePrice = usePrice;
        
        super.setData(nc, pcA, pcB, pcC, pcD, DPH);
        
        // Vyznaè správnì zaškrtnutí
        for (int i = 0; i < getRowCount(); i++ ) {
            
            if (i == usePrice) {
                // Nastav vlastnosti posledního sloupce
                super.setValueAt(Boolean.TRUE, i, 3);
            } else {
                super.setValueAt(Boolean.FALSE, i, 3);
            }
        }
        
    }    
    
    /**
     *  Zakáža editovat tabulku 
     */
    public void banEditing() {
        confirmEdit = false;
    }
    
    /**
     * Nastaví editovatelné sloupce.
     * Editovat je možné sloupce s cenou
     * Avšak byla-li volána metoda banEditing(), není možné editovat
     * @param row øádek
     * @param column sloupec
     * tr
     * @return true, jestliže je možné sloupec editovat
     */
    public boolean isCellEditable(int row, int column)  {

        if (confirmEdit && ((Boolean) getValueAt(row, 3)) == true ) {
            return super.isCellEditable(row, column);
        } else {
            
            if (column == PriceTableColumns.USE_PRICE.getNumber()) {
                return true; 
            } else {
                return false;
            }
        }
    }    
    
}
