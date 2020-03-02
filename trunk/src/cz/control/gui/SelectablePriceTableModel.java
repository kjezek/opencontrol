/*
 * SelectablePriceTableModel.java
 *
 * Vytvo�eno 15. �nor 2006, 13:12
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.business.*;

/**
 * Program Control - Skladov� syst�m
 *
 *  Model pro tabulku cen u kter� lze nastavovat, kter� z cen se m� pou��t pro prodej
 *
 * (C) 2005, ver. 1.0
 */
public class SelectablePriceTableModel extends EditablePriceTableModel {           
    
    boolean confirmEdit = true;
    private int usePrice = 0;
    
    /**
     *  Vytvo�� pr�zdnou tabulku
     */ 
    public SelectablePriceTableModel() {
        super();
    }

    /**
     * Vytvo�� tabulka s pou��t�m jmen slooupc� a o zadan�m po�tu ��dk�
     * @param names N�zvy sloupc�
     * @param rowCount Po�et ��dk�
     */
    public SelectablePriceTableModel(String[] names, int rowCount) {
        super(names, rowCount);
    }  
    
    /**
     * Vytvo�� model tabulky s odpov�daj�c�mi cenami pro konkr�tn� zbo��.
     * Zv�razn� defaultn� prodejn� cenu
     * @param nc n�kupn� cena
     * @param pcA prdejn� cena A
     * @param pcB prdejn� cena B
     * @param pcC prdejn� cena C
     * @param pcD prdejn� cena D
     * @param DPH DPH
     */
    public SelectablePriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        this(nc, pcA, pcB, pcC, pcD, DPH, Settings.getDefaultSalePrice());
    }
    
    /**
     * Vytvo�� model tabulky s odpov�daj�c�mi cenami pro konkr�tn� zbo��.
     * Zv�razn� defaultn� prodejn� cenu
     * @param nc n�kupn� cena
     * @param pcA prdejn� cena A
     * @param pcB prdejn� cena B
     * @param pcC prdejn� cena C
     * @param pcD prdejn� cena D
     * @param DPH DPH
     */
    public SelectablePriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH, int usePrice) {
        // Nastav po��te�n� hodnoty
        String[] colNames = {"N�zev", "K� (bez DPH)", "K� (s DPH) ", "Pou��t cenu"};        
        createTable(colNames, PriceTableModel.DEFAULT_ROW_COUNT);

        setData(nc, pcA, pcB, pcC, pcD, DPH, usePrice);
        
        this.usePrice = usePrice;
    }
    
    /**
     *  Nastav� ceny v tabulce. Zv�razn�n� ceny nem�n�
     * @param nc n�kupn� cena
     * @param pcA prdejn� cena A
     * @param pcB prdejn� cena B
     * @param pcC prdejn� cena C
     * @param pcD prdejn� cena D
     * @param DPH DPH     
     * @param usePrice konstanta ur�uj�c� jak� cena se m� zv�raznit
     * DoBuy.USE_NC_FOR_SUM -  Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t N�kupn� cena (NC)
     * DoBuy.USE_PCA_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t prvn� Prodejn� cena (PC A)
     * DoBuy.USE_PCB_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t druh� Prodejn� cena (PC B)
     * DoBuy.USE_PCC_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t t�et� Prodejn� cena (PC C)
     * DoBuy.USE_PCD_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t �tvrt� Prodejn� cena (PC D)
     */
    public void setData(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        super.setData(nc, pcA, pcB, pcC, pcD, DPH);
    }
    
    /**
     *  Nastav� data v tabulce a vybere p��slu�nou cenu u kter� se objev� za�krtnut�
     * @param nc n�kupn� cena
     * @param pcA prdejn� cena A
     * @param pcB prdejn� cena B
     * @param pcC prdejn� cena C
     * @param pcD prdejn� cena D
     * @param DPH DPH
     * @param usePrice konstanta ur�uj�c� jak� cena se m� zv�raznit
     * DoBuy.USE_NC_FOR_SUM -  Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t N�kupn� cena (NC)
     * DoBuy.USE_PCA_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t prvn� Prodejn� cena (PC A)
     * DoBuy.USE_PCB_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t druh� Prodejn� cena (PC B)
     * DoBuy.USE_PCC_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t t�et� Prodejn� cena (PC C)
     * DoBuy.USE_PCD_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t �tvrt� Prodejn� cena (PC D)

     */
    public void setData(int nc, int pcA, int pcB, int pcC, int pcD, int DPH, int usePrice) {
        this.usePrice = usePrice;
        
        super.setData(nc, pcA, pcB, pcC, pcD, DPH);
        
        // Vyzna� spr�vn� za�krtnut�
        for (int i = 0; i < getRowCount(); i++ ) {
            
            if (i == usePrice) {
                // Nastav vlastnosti posledn�ho sloupce
                super.setValueAt(Boolean.TRUE, i, 3);
            } else {
                super.setValueAt(Boolean.FALSE, i, 3);
            }
        }
        
    }    
    
    /**
     *  Zak�a editovat tabulku 
     */
    public void banEditing() {
        confirmEdit = false;
    }
    
    /**
     * Nastav� editovateln� sloupce.
     * Editovat je mo�n� sloupce s cenou
     * Av�ak byla-li vol�na metoda banEditing(), nen� mo�n� editovat
     * @param row ��dek
     * @param column sloupec
     * tr
     * @return true, jestli�e je mo�n� sloupec editovat
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
