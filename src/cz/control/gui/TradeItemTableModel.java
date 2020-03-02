/*
 * TradeItemTableModel.java
 *
 * Vytvoøeno 6. listopad 2005, 23:15
 *
 
 */

package cz.control.gui;

import cz.control.data.TradeItem;
import cz.control.gui.*;

import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pro definici modelu tabulky s obchodem. Slouží pro zobrazení tabulek zboží 
 * u nákupu a prodeje
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class TradeItemTableModel extends GoodsTableModel {
    private ArrayList<TradeItem> items = new ArrayList<TradeItem>();

    /**
     * Vytvoøí model tabulky
     */
    public TradeItemTableModel() {
        super();
    }
    
    /**
     * Vytvoøí model tabulky
     * @param tradeItems Seznam obchodù, které se vloží do tabulky
     */
    public TradeItemTableModel(ArrayList<TradeItem> tradeItems) {
        items = new ArrayList<TradeItem>(tradeItems);
        for (TradeItem i: tradeItems) {
            super.inserRow(i.getAsGoods());
        }
    }

    /**
     * Nastaví seznam obchodù, které se obìví v tabulce
     * @param tradeItem Seznam obchodù
     */
    public void setTradeItemData(ArrayList<TradeItem> tradeItems) {
        items = new ArrayList<TradeItem>(tradeItems);
        for (TradeItem i: tradeItems) {
            super.inserRow(i.getAsGoods());
        }
    }
    
    /**
     * Vloží jeden øádek na konec tabulky 
     * @param tradeItam položka, které se má vložit
     * @return na který øádek bylo zboží vloženo
     */
    public int inserRow(TradeItem tradeItem) {
        items.add(tradeItem);
        return super.inserRow(tradeItem.getAsGoods());
    }
    
    /**
     * Vymaže jeden øádek z tabulky
     * @param row èíslo øádky pošítáno od jednièky
     */
    @Override
    public void deleteRow(int row) {
        items.remove(row);
        super.deleteRow(row);
    }
 
    
    /**
     * Vrací položku na pøíslušném øádku
     * @param row øádek
     * @return položka
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
