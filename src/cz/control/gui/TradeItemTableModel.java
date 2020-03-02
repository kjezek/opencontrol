/*
 * TradeItemTableModel.java
 *
 * Vytvo�eno 6. listopad 2005, 23:15
 *
 
 */

package cz.control.gui;

import cz.control.data.TradeItem;
import cz.control.gui.*;

import java.util.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro definici modelu tabulky s obchodem. Slou�� pro zobrazen� tabulek zbo�� 
 * u n�kupu a prodeje
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class TradeItemTableModel extends GoodsTableModel {
    private ArrayList<TradeItem> items = new ArrayList<TradeItem>();

    /**
     * Vytvo�� model tabulky
     */
    public TradeItemTableModel() {
        super();
    }
    
    /**
     * Vytvo�� model tabulky
     * @param tradeItems Seznam obchod�, kter� se vlo�� do tabulky
     */
    public TradeItemTableModel(ArrayList<TradeItem> tradeItems) {
        items = new ArrayList<TradeItem>(tradeItems);
        for (TradeItem i: tradeItems) {
            super.inserRow(i.getAsGoods());
        }
    }

    /**
     * Nastav� seznam obchod�, kter� se ob�v� v tabulce
     * @param tradeItem Seznam obchod�
     */
    public void setTradeItemData(ArrayList<TradeItem> tradeItems) {
        items = new ArrayList<TradeItem>(tradeItems);
        for (TradeItem i: tradeItems) {
            super.inserRow(i.getAsGoods());
        }
    }
    
    /**
     * Vlo�� jeden ��dek na konec tabulky 
     * @param tradeItam polo�ka, kter� se m� vlo�it
     * @return na kter� ��dek bylo zbo�� vlo�eno
     */
    public int inserRow(TradeItem tradeItem) {
        items.add(tradeItem);
        return super.inserRow(tradeItem.getAsGoods());
    }
    
    /**
     * Vyma�e jeden ��dek z tabulky
     * @param row ��slo ��dky po��t�no od jedni�ky
     */
    @Override
    public void deleteRow(int row) {
        items.remove(row);
        super.deleteRow(row);
    }
 
    
    /**
     * Vrac� polo�ku na p��slu�n�m ��dku
     * @param row ��dek
     * @return polo�ka
     */
    public TradeItem getTradeItemAt(int row) {
        TradeItem tradeItem = items.get(row);
        
        // Obnov data v seznamu
        String goodsId = String.valueOf( super.getValueAt(row, 0) );
        int quantity = Integer.valueOf( String.valueOf( super.getValueAt(row, 2) ));
        TradeItem newTradeItem = new TradeItem(tradeItem.getTradeId(), 
                tradeItem.getTradeIdListing(), goodsId, tradeItem.getName(),
                tradeItem.getDph(), tradeItem.getPrice(), quantity, tradeItem.getUnit(),
                tradeItem.getUsePrice());
        
        items.set(row, newTradeItem);

        return newTradeItem;
    }

 }
