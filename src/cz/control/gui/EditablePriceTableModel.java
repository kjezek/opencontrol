/*
 * EditablePriceTableModel.java
 *
 * Vytvo�eno 1. listopad 2005, 10:02
 *
 */

package cz.control.gui;

import cz.control.business.Store;
import java.math.BigDecimal;

/**
 * Program Control - Skladov� syst�m
 *
 *  Model pro tabulku cen, kterou lze editovat
 *
 * (C) 2005, ver. 1.0
 */
public class EditablePriceTableModel extends PriceTableModel {

    /**
     *  Vytvo�� pr�zdnou tabulku
     */ 
    public EditablePriceTableModel() {
        super();
    }

    /**
     * Vytvo�� tabulka s pou��t�m jmen slooupc� a o zadan�m po�tu ��dk�
     * @param names N�zvy sloupc�
     * @param rowCount Po�et ��dk�
     */
    public EditablePriceTableModel(String[] names, int rowCount) {
        super(names, rowCount);
    }    
    
    /**
     * Vytvo�� model tabulky s odpov�daj�c�mi cenami pro konkr�tn� zbo��
     * @param nc n�kupn� cena
     * @param pcA prdejn� cena A
     * @param pcB prdejn� cena B
     * @param pcC prdejn� cena C
     * @param pcD prdejn� cena D
     * @param DPH DPH
     */
    public EditablePriceTableModel(int nc, int pcA, int pcB, int pcC, int pcD, int DPH) {
        super(nc, pcA, pcB, pcC, pcD, DPH);
    }

    /**
     *  Nastav� editovateln� sloupce.
     *  Editovat je mo�n� sloupce s cenou
     * @param row ��dek
     * @param column sloupec
     * tr
     * @return true, jestli�e je mo�n� sloupec editovat
     */
    public boolean isCellEditable(int row, int column)  {
        if (column == PriceTableColumns.PRICE.getNumber() || 
            column == PriceTableColumns.PRICE_DPH.getNumber() ||
            column == PriceTableColumns.USE_PRICE.getNumber()) {
            
            return true;
        }
        return false;
    }

    /**
     * Nastavuje hodnotu na pozici v tabulce
     * @param val objekt, kter� s m� ulo�it
     * @param row ��dek kam ulo�it
     * @param column sloupec kam ulo�it 
     */
/*     public void setValueAt(Object val, int row, int column) {
        super.setValueAt(val, row, column); // Nastav ��dek a sloupec, kter� se zm�nil 
    }*/

     /**
     * Provede aktualizaci DPH a p�epo�te tabulku
     * @param dph nov� cena DPH
     */
     public void changeDph(int dph) {
         super.setDph( new BigDecimal(dph).divide(Store.CENT) ); // aktualizuj DPH
         int index = PriceTableColumns.PRICE.getNumber();

         /* Aktualizuj v�echny sloupce */
         setValueAt( getValueAt(0, index), 0, index);
         setValueAt( getValueAt(1, index), 1, index);
         setValueAt( getValueAt(2, index), 2, index);
         setValueAt( getValueAt(3, index), 3, index);
         setValueAt( getValueAt(4, index), 4, index);
     }
     
}

