/*
 * DoSale.java
 *
 * Vytvoøeno 29. listopad 2005, 12:43
 *
 
 */

package cz.control.business;

import cz.control.data.Customer;
import cz.control.data.Goods;
import cz.control.business.Sale;
import cz.control.data.Client;
import cz.control.data.TradeItem;
import cz.control.data.TradeItemPreview;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladovı systém
 *
 * Tøída pro vytvoøení vıdejky. Tøída umoòuje nastavit veškeré potøebnì údaje o jedné
 * vıdejce a následnì jí zapsat do databáze 
 *
 * @author Kamil Jeek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class DoSale extends DoBuy {
    private static final String SALE_NAME = DatabaseAccess.SALE_TABLE_NAME; // ulo název databáze
    private static final String SALE_LISTING_NAME = DatabaseAccess.SALE_LISTING_TABLE_NAME; // ulo název databáze
    
    /** Hodnota udávající, zda je spracováván maloobchod */
    private boolean discount = false;
    
    /**
     * Vytvoøí novı objekt DoSale
     * @param client reference na klienta, kterı vytváøí vıdejku
     */
    DoSale(Client client) throws SQLException {
        super(client);
        // defaultní cena pro obchod
    }

    /**
     * Vytvoøí novı objekt DoSale. Pouije hodnoty z døíve vytvoøené vıdejky. 
     * Tento konstruktor je vhodnı pouít pro bezpeèné stornování vıdejky, 
     * nebo pro editaci vıdejky
     * @param client klient, kterı otevøel pøíjemku
     * @param oldTradeItemPreview Døíve vytvoøená pøíjemka
     * @throws java.sql.SQLException 
     */
    DoSale(Client client, TradeItemPreview oldTradeItemPreview) throws SQLException{
        super(client);
        super.setOldTradeItemPreview(oldTradeItemPreview);
        setTradeItemAttributes();
    }     
    
    /**
     * Nastaví odbìratele, kterı nakoupil zboí uvedené ve vıdejce
     * @param customer odbìratel, kterému se prodává
     */
    public void setCustomer(Customer customer) {
        super.setSuplier(customer);
    }
    
    /**
     * Tuto metodu není moné volat z této tøídy.
     * Pouijte metodu <code>setSuplier()</code>
     */
//    public void setSuplier(Suplier suplier) {
//        throw new ClassCastException("Not possible method. You have to use method setCustomer()");
//    }
    
    /**
     *  Nastaví atributy pøíjemky podle dané vstupní pøíjemky
     */
    private void setTradeItemAttributes() throws SQLException {
        super.setDate( super.getOldTradeItemPreview().getDate() );
        super.setReduction( new BigDecimal(super.getOldTradeItemPreview().getReduction()).intValue() );

        Customer customer;
        
        // Podle customera rozhodni, zda se jedná o maloobchod
        if (super.getOldTradeItemPreview().getId() == 0) {
            customer = null;
            setDiscount(true);
        } else {
            customer = new Customers().getCustomerByID(super.getOldTradeItemPreview().getId());
        }

        super.setSuplier( customer );
        
        // Nastav jednotlivé poloky
        Sale sale = new Sale();
// Nedá se pouít, nebo je tøeba i aktualizovat ceny        
//        items = buy.getAllBuyItem(oldTradeItemPreview);
        
        Store store = new Store();
        
        // Aktualizuj ceny
        for (TradeItem i: sale.getAllSaleItem(super.getOldTradeItemPreview()) ) {
            // Naèti a ulo do mapy pouitelné mnoství zboí
            Goods goods = store.getGoodsByID(i.getGoodsId());
            
            //Vytvoø nové zboí, které bude obsahovat ceny ze skladu a jednu zmìnìnou 
            // cenu, za kterou bylo prodáno
            Goods newGoods = new Goods(i.getGoodsId(), i.getName(), goods.getType(), i.getDph(),
                    i.getUnit(), goods.getEan(), 
                    // Rozhodni zda pouít ceny s vıdejky, nebo ze skladové karty
                    (i.getUsePrice() == DoBuy.USE_NC_FOR_SUM) ? i.getPrice() : goods.getNc(),
                    (i.getUsePrice() == DoBuy.USE_PCA_FOR_SUM) ? i.getPrice() : goods.getPcA(),
                    (i.getUsePrice() == DoBuy.USE_PCB_FOR_SUM) ? i.getPrice() : goods.getPcB(),
                    (i.getUsePrice() == DoBuy.USE_PCC_FOR_SUM) ? i.getPrice() : goods.getPcC(),
                    (i.getUsePrice() == DoBuy.USE_PCD_FOR_SUM) ? i.getPrice() : goods.getPcD(),
                    i.getQuantity()
                    );
            
            super.tradeId = i.getTradeId();
            // Pouitelné mnoství je to na skladì + to na pøíjemce
            super.setAvailableQuantity(goods.getGoodsID(), i.getQuantity());

            super.setLastUsePrice(i.getUsePrice());
            super.addTradeItem(newGoods, i.getQuantity());
            
            //Explicitnì pøenastav parametry, tak aby korespondovali s vloenım zboím
            ItemAttributes itemAttr = super.getTradeItemAttributes(i);
            itemAttr.setNewNc(newGoods.getNc());
            itemAttr.setNewPcA(newGoods.getPcA());
            itemAttr.setNewPcB(newGoods.getPcB());
            itemAttr.setNewPcC(newGoods.getPcC());
            itemAttr.setNewPcD(newGoods.getPcD());
        }
        
        // Doplò mnoství ze skladu
        for (String i: super.getAvailableQuantityMap().keySet()) {
            Goods goods = store.getGoodsByID(i);
            BigDecimal oldQuantity = new BigDecimal(super.getAvailableQuantity(i));
            super.setAvailableQuantity(i, oldQuantity.add( new BigDecimal(goods.getQuantity())).doubleValue() );
        }
    }
        
    /**
     * Vyhledá další èíslo pouitelné pro pøíjemku podle datumu provedení pøíjemky 
     */
    private int getNextNumber() throws SQLException{
        TradeItemPreview oldTradeItemPreview = super.getOldTradeItemPreview();
        
        // Jestlie existuje stará pøíjemka, která byla stornována
        // a má se vytvoøit pøíjemka stejného data, pøevezmi její èíslo
        // nebo se jedná o náhradu staré pøíjemky
        if (super.isStornoCalled() && oldTradeItemPreview != null &&
                getDate().get(Calendar.YEAR) == oldTradeItemPreview.getDate().get(Calendar.YEAR) &&
                getDate().get(Calendar.MONTH) == oldTradeItemPreview.getDate().get(Calendar.MONTH) &&
                getDate().get(Calendar.DAY_OF_MONTH) == oldTradeItemPreview.getDate().get(Calendar.DAY_OF_MONTH) ) {
            
            return oldTradeItemPreview.getNumber();
        }
        
        Sale sale = new Sale();
        
        int result = sale.getMaxSaleNumber(super.getDate());
        return result + 1; // minimální èíslo je jedna
    }
    
    
    /*
     * Zapíše jednotlivé poloky vıdejky do databáze.
     * Zároveò aktualizuje stav zboí na skladì
     */
    private void writeItemsToDatabase(int id) throws Exception {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                    "INSERT INTO " + SALE_NAME + " (id_sale_listing, goods_id, name, dph, pc, quantity, unit, use_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        
        Store store = new Store(); // vytvoø tøídu pro práci ze skladem
        // zjistí jaké ID bylo pøiøazeno pøíjemce
        
        // Projdi všechny poloky pøíjemky a zapiš je do databáze
        // a zaroven aktualizuj mnozstvi zbozi na sklade
        for (TradeItem i: super.getItems()) {
            // zapiš poloku pøíjemky 
            pstm.setInt(1, id);
            pstm.setString(2, i.getGoodsId()); 
            pstm.setString(3, i.getName());
            pstm.setInt(4, i.getDph());
            pstm.setDouble(5,  (new BigDecimal(i.getPrice())).divide(Store.CENT).doubleValue() );
            pstm.setDouble(6, i.getQuantity());
            pstm.setString(7, i.getUnit());
            pstm.setInt(8, i.getUsePrice());
            pstm.executeUpdate();
            
            // Aktualizuj stav zboí na skladì
            Goods goods = store.getGoodsByID(i.getGoodsId(), true);
            // Mnoství je tøeba odeèíst -> vydává se ze skladu
            BigDecimal newQuantity = new BigDecimal(goods.getQuantity()).subtract( new BigDecimal(i.getQuantity())); 
            
            // Nechceme prodat více ne je na skladì
            if (newQuantity.signum() == -1) {
                throw new Exception("Na skladì není dostatek mnoství pro zboí: <br>" +
                        "<b>" + i.getGoodsId() + " - " + i.getName() + "</b><br> " +
                        "Maximální pouitelné mnoství je: <b> " +
                        Settings.getFloatFormat().format(goods.getQuantity()) + "" +
                        "</b>.<br>" +
                        "Opravte prosím zadání.");
            }
                
            store.editQuantity(i.getGoodsId(), newQuantity.doubleValue());
        }   
        pstm.close();
    }
    
    /**
     *  Provede kontrolu, zda jsou vyplnìny potøebné údaje
     *  a pøíjemka by mohla bıt potvrzena
     * @throws java.lang.Exception vyvolá jestlie není moné potvrdit vıdejku
     */
    public void check() throws Exception {
        
        // Jestlie není vyplnìn dodavatel a nejedná se o maloobchod
        if (discount == false && (super.getSuplier() == null || super.getSuplier().getId() == -1) ) {
            throw new Exception("Není vyplnìno pole Odbìratel");
        }

        if (super.getItems().isEmpty()) {
            throw new Exception("Vıdejka neobsahuje ádnì poloky zboí");
        }
        
        if (super.isStockingLock()){
            throw new Exception("Období je uzamèeno inventurou. <br>" +
                    "V uzamèeném období nemùete mìnit stav na skladì");
        }
        
    }
    
    /**
     * Vrací senzma poloek, které se prodávají ze strátou
     * Tedy u kterıch je PC < NC
     * @return seznam ztrátovıch poloek
     */
    public ArrayList<TradeItem> getLossPriceItems() {
        ArrayList<TradeItem> result = new ArrayList<TradeItem>();
        
        // Projdi všechny poloky, najdi ztrátovı prodej a ulo do pole
        for (TradeItem i: super.getItems()) {
            ItemAttributes itemAtt = super.getTradeItemAttributes(i);
            if (i.getPrice() < itemAtt.getInputGoods().getNc()) {
                result.add(i);
            }
        }
        
        return result;
    }
    
    /**
     * Potvrdí celou operaci a zapíše data do databáze. Pøed zapsámí, provádí kontrolu,
     * zda jsou všechny potøebné údaje zadány.
     * Kontroluje zda bylo doplnìn alespoò jedno zboí. Zda je správnì zadán datum.
     * A zda je zadán dodavatel
     * @throws java.lang.Exception Vyvolá, jestlie dojde k chybì pøi práci s databází, nebo nejsou vyplnìny všechny potøebné poloky
     * @return vrací vytvoøenou vıdejku
     */
    public TradeItemPreview confirm() throws Exception {
        try {
            DatabaseAccess.setAutoCommit(false);

            Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
            PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                        "INSERT INTO " + SALE_LISTING_NAME + "(number, date, cust_id, total_pc_dph, total_dph, total_pc, reduction, author, user_id" +
                        " ) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            check(); // proveï kontroly
            
            int number = getNextNumber();

            // Zapiš pøehled pøíjemky
            pstm.setInt(1, number);
            pstm.setTimestamp(2, new java.sql.Timestamp(super.getDate().getTimeInMillis()));
            if (discount) { // provádí se maloobchod
                pstm.setObject(3, null); // podle hodnoty null v odbìrateli rozezná maloobchod
            } else {
                pstm.setInt(3, super.getSuplier().getId());
            }
            pstm.setDouble(4, super.getTotalPriceDPH().doubleValue() );
            pstm.setDouble(5, super.getTotalDPH().doubleValue() );
            pstm.setDouble(6, super.getTotalPrice().doubleValue() );
            pstm.setDouble(7, super.getReduction().doubleValue() );
            pstm.setString(8, super.getClient().getName());
            if (super.getClient().getUserId() == -1)  // Vıchozí pøihlášení 
                pstm.setObject(9, null); // nastav na null
            else
                pstm.setInt(9, super.getClient().getUserId()); // nastav na skuteèné èíslo uivatele
            pstm.executeUpdate();
            
            int id = super.getLastId(stm);
            writeItemsToDatabase(id); // zapiš poloky pøíjemky do databáze 
            
            pstm.close();
            stm.close();
            
            return new Sale().getSale(id);
            
        } catch (Exception e) {
            DatabaseAccess.rollBack();
            throw e;
        }
    }
    
    /**
     *  Provede Storno Vıdejky, která byla døíve provedena.
     *  Aktualizuje stav zboí na skladì.
     *  Volání má smysl pouze, kdy byl volán konstruktor
     *  <code>DoBuy(Client client, TradeItemPreview oldTradeItemPreview)</code>
     *  jinak není co stornovat
     * @throws java.sql.SQLException Vyvolá, jestlie dojde k chybì pøi práci s databází
     */
    public void storno() throws SQLException {
        
        // Jestlie není ádná pøedchozí pøíjemka, není co stornovat.
        if (super.getOldTradeItemPreview() == null)
            return;
        
        try {
            DatabaseAccess.setAutoCommit(false);
            Store store = new Store();
            Sale sale = new Sale();
            ArrayList<TradeItem> items = sale.getAllSaleItem(super.getOldTradeItemPreview());

            // Projdi všechny poloky pøíjemky
            for (TradeItem i: items) {
                // Odeèti od zboí mnoství které je na pøíjemce
                BigDecimal oldQuantity = new BigDecimal(store.getGoodsByID(i.getGoodsId(), true).getQuantity());
                // Dùleité - noství je tøeba pøièíst, nebo se ruší vıdejka
                BigDecimal newQuantity = oldQuantity.add( new BigDecimal(i.getQuantity()) );
                store.editQuantity(i.getGoodsId(), newQuantity.doubleValue()); // aktualizuj cenu
            }

            sale.deleteSale(super.getOldTradeItemPreview()); // Vyma pøíjemku
            super.setStornoCalled(true);
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        }
    }    
    
    /**
     * Vrací odbìratele pøíjemky
     * @return Odbìratel pøíjemky
     */
    public Customer getCustomer() {
        return (Customer) super.getSuplier();
    }

    /**
     * Vrací, zda je zpracováván maloobchodní prodej
     * @return true jestlie je zpracováván malooobchodní prodej
     * false jestlie se jedná o velkoobchodní prodej
     */
    public boolean isDiscount() {
        return discount;
    }

    /**
     * Nastavuje, zda je zpracováván maloobchodní prodej
     * 
     * @param discount Nastavuje, zda je zpracováván maloobchodní prodej
     * true jestlie je zpracováván malooobchodní prodej
     * false jestlie se jedná o velkoobchodní prodej
     * @throws java.sql.SQLException chyba s databází
     */
    public void setDiscount(boolean discount) throws SQLException {
        this.discount = discount;
        this.setLastUsePrice(DoBuy.USE_PCD_FOR_SUM);
    }
      
}
