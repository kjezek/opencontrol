/*
 * DoSale.java
 *
 * Vytvo�eno 29. listopad 2005, 12:43
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
 * Program Control - Skladov� syst�m
 *
 * T��da pro vytvo�en� v�dejky. T��da umo��uje nastavit ve�ker� pot�ebn� �daje o jedn�
 * v�dejce a n�sledn� j� zapsat do datab�ze 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 * @author Kamil
 */
public class DoSale extends DoBuy {
    private static final String SALE_NAME = DatabaseAccess.SALE_TABLE_NAME; // ulo� n�zev datab�ze
    private static final String SALE_LISTING_NAME = DatabaseAccess.SALE_LISTING_TABLE_NAME; // ulo� n�zev datab�ze
    
    /** Hodnota ud�vaj�c�, zda je spracov�v�n maloobchod */
    private boolean discount = false;
    
    /**
     * Vytvo�� nov� objekt DoSale
     * @param client reference na klienta, kter� vytv��� v�dejku
     */
    DoSale(Client client) throws SQLException {
        super(client);
        // defaultn� cena pro obchod
    }

    /**
     * Vytvo�� nov� objekt DoSale. Pou�ije hodnoty z d��ve vytvo�en� v�dejky. 
     * Tento konstruktor je vhodn� pou��t pro bezpe�n� stornov�n� v�dejky, 
     * nebo pro editaci v�dejky
     * @param client klient, kter� otev�el p��jemku
     * @param oldTradeItemPreview D��ve vytvo�en� p��jemka
     * @throws java.sql.SQLException 
     */
    DoSale(Client client, TradeItemPreview oldTradeItemPreview) throws SQLException{
        super(client);
        super.setOldTradeItemPreview(oldTradeItemPreview);
        setTradeItemAttributes();
    }     
    
    /**
     * Nastav� odb�ratele, kter� nakoupil zbo�� uveden� ve v�dejce
     * @param customer odb�ratel, kter�mu se prod�v�
     */
    public void setCustomer(Customer customer) {
        super.setSuplier(customer);
    }
    
    /**
     * Tuto metodu nen� mo�n� volat z t�to t��dy.
     * Pou�ijte metodu <code>setSuplier()</code>
     */
//    public void setSuplier(Suplier suplier) {
//        throw new ClassCastException("Not possible method. You have to use method setCustomer()");
//    }
    
    /**
     *  Nastav� atributy p��jemky podle dan� vstupn� p��jemky
     */
    private void setTradeItemAttributes() throws SQLException {
        super.setDate( super.getOldTradeItemPreview().getDate() );
        super.setReduction( new BigDecimal(super.getOldTradeItemPreview().getReduction()).intValue() );

        Customer customer;
        
        // Podle customera rozhodni, zda se jedn� o maloobchod
        if (super.getOldTradeItemPreview().getId() == 0) {
            customer = null;
            setDiscount(true);
        } else {
            customer = new Customers().getCustomerByID(super.getOldTradeItemPreview().getId());
        }

        super.setSuplier( customer );
        
        // Nastav jednotliv� polo�ky
        Sale sale = new Sale();
// Ned� se pou��t, nebo� je t�eba i aktualizovat ceny        
//        items = buy.getAllBuyItem(oldTradeItemPreview);
        
        Store store = new Store();
        
        // Aktualizuj ceny
        for (TradeItem i: sale.getAllSaleItem(super.getOldTradeItemPreview()) ) {
            // Na�ti a ulo� do mapy pou�iteln� mno�stv� zbo��
            Goods goods = store.getGoodsByID(i.getGoodsId());
            
            //Vytvo� nov� zbo��, kter� bude obsahovat ceny ze skladu a jednu zm�n�nou 
            // cenu, za kterou bylo prod�no
            Goods newGoods = new Goods(i.getGoodsId(), i.getName(), goods.getType(), i.getDph(),
                    i.getUnit(), goods.getEan(), 
                    // Rozhodni zda pou��t ceny s v�dejky, nebo ze skladov� karty
                    (i.getUsePrice() == DoBuy.USE_NC_FOR_SUM) ? i.getPrice() : goods.getNc(),
                    (i.getUsePrice() == DoBuy.USE_PCA_FOR_SUM) ? i.getPrice() : goods.getPcA(),
                    (i.getUsePrice() == DoBuy.USE_PCB_FOR_SUM) ? i.getPrice() : goods.getPcB(),
                    (i.getUsePrice() == DoBuy.USE_PCC_FOR_SUM) ? i.getPrice() : goods.getPcC(),
                    (i.getUsePrice() == DoBuy.USE_PCD_FOR_SUM) ? i.getPrice() : goods.getPcD(),
                    i.getQuantity()
                    );
            
            super.tradeId = i.getTradeId();
            // Pou�iteln� mno�stv� je to na sklad� + to na p��jemce
            super.setAvailableQuantity(goods.getGoodsID(), i.getQuantity());

            super.setLastUsePrice(i.getUsePrice());
            super.addTradeItem(newGoods, i.getQuantity());
            
            //Explicitn� p�enastav parametry, tak aby korespondovali s vlo�en�m zbo��m
            ItemAttributes itemAttr = super.getTradeItemAttributes(i);
            itemAttr.setNewNc(newGoods.getNc());
            itemAttr.setNewPcA(newGoods.getPcA());
            itemAttr.setNewPcB(newGoods.getPcB());
            itemAttr.setNewPcC(newGoods.getPcC());
            itemAttr.setNewPcD(newGoods.getPcD());
        }
        
        // Dopl� mno�stv� ze skladu
        for (String i: super.getAvailableQuantityMap().keySet()) {
            Goods goods = store.getGoodsByID(i);
            BigDecimal oldQuantity = new BigDecimal(super.getAvailableQuantity(i));
            super.setAvailableQuantity(i, oldQuantity.add( new BigDecimal(goods.getQuantity())).doubleValue() );
        }
    }
        
    /**
     * Vyhled� dal�� ��slo pou�iteln� pro p��jemku podle datumu proveden� p��jemky 
     */
    private int getNextNumber() throws SQLException{
        TradeItemPreview oldTradeItemPreview = super.getOldTradeItemPreview();
        
        // Jestli�e existuje star� p��jemka, kter� byla stornov�na
        // a m� se vytvo�it p��jemka stejn�ho data, p�evezmi jej� ��slo
        // nebo� se jedn� o n�hradu star� p��jemky
        if (super.isStornoCalled() && oldTradeItemPreview != null &&
                getDate().get(Calendar.YEAR) == oldTradeItemPreview.getDate().get(Calendar.YEAR) &&
                getDate().get(Calendar.MONTH) == oldTradeItemPreview.getDate().get(Calendar.MONTH) &&
                getDate().get(Calendar.DAY_OF_MONTH) == oldTradeItemPreview.getDate().get(Calendar.DAY_OF_MONTH) ) {
            
            return oldTradeItemPreview.getNumber();
        }
        
        Sale sale = new Sale();
        
        int result = sale.getMaxSaleNumber(super.getDate());
        return result + 1; // minim�ln� ��slo je jedna
    }
    
    
    /*
     * Zap�e jednotliv� polo�ky v�dejky do datab�ze.
     * Z�rove� aktualizuje stav zbo�� na sklad�
     */
    private void writeItemsToDatabase(int id) throws Exception {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                    "INSERT INTO " + SALE_NAME + " (id_sale_listing, goods_id, name, dph, pc, quantity, unit, use_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        
        Store store = new Store(); // vytvo� t��du pro pr�ci ze skladem
        // zjist� jak� ID bylo p�i�azeno p��jemce
        
        // Projdi v�echny polo�ky p��jemky a zapi� je do datab�ze
        // a zaroven aktualizuj mnozstvi zbozi na sklade
        for (TradeItem i: super.getItems()) {
            // zapi� polo�ku p��jemky 
            pstm.setInt(1, id);
            pstm.setString(2, i.getGoodsId()); 
            pstm.setString(3, i.getName());
            pstm.setInt(4, i.getDph());
            pstm.setDouble(5,  (new BigDecimal(i.getPrice())).divide(Store.CENT).doubleValue() );
            pstm.setDouble(6, i.getQuantity());
            pstm.setString(7, i.getUnit());
            pstm.setInt(8, i.getUsePrice());
            pstm.executeUpdate();
            
            // Aktualizuj stav zbo�� na sklad�
            Goods goods = store.getGoodsByID(i.getGoodsId(), true);
            // Mno�stv� je t�eba ode��st -> vyd�v� se ze skladu
            BigDecimal newQuantity = new BigDecimal(goods.getQuantity()).subtract( new BigDecimal(i.getQuantity())); 
            
            // Nechceme prodat v�ce ne� je na sklad�
            if (newQuantity.signum() == -1) {
                throw new Exception("Na sklad� nen� dostatek mno�stv� pro zbo��: <br>" +
                        "<b>" + i.getGoodsId() + " - " + i.getName() + "</b><br> " +
                        "Maxim�ln� pou�iteln� mno�stv� je: <b> " +
                        Settings.getFloatFormat().format(goods.getQuantity()) + "" +
                        "</b>.<br>" +
                        "Opravte pros�m zad�n�.");
            }
                
            store.editQuantity(i.getGoodsId(), newQuantity.doubleValue());
        }   
        pstm.close();
    }
    
    /**
     *  Provede kontrolu, zda jsou vypln�ny pot�ebn� �daje
     *  a p��jemka by mohla b�t potvrzena
     * @throws java.lang.Exception vyvol� jestli�e nen� mo�n� potvrdit v�dejku
     */
    public void check() throws Exception {
        
        // Jestli�e nen� vypln�n dodavatel a nejedn� se o maloobchod
        if (discount == false && (super.getSuplier() == null || super.getSuplier().getId() == -1) ) {
            throw new Exception("Nen� vypln�no pole Odb�ratel");
        }

        if (super.getItems().isEmpty()) {
            throw new Exception("V�dejka neobsahuje ��dn� polo�ky zbo��");
        }
        
        if (super.isStockingLock()){
            throw new Exception("Obdob� je uzam�eno inventurou. <br>" +
                    "V uzam�en�m obdob� nem��ete m�nit stav na sklad�");
        }
        
    }
    
    /**
     * Vrac� senzma polo�ek, kter� se prod�vaj� ze str�tou
     * Tedy u kter�ch je PC < NC
     * @return seznam ztr�tov�ch polo�ek
     */
    public ArrayList<TradeItem> getLossPriceItems() {
        ArrayList<TradeItem> result = new ArrayList<TradeItem>();
        
        // Projdi v�echny polo�ky, najdi ztr�tov� prodej a ulo� do pole
        for (TradeItem i: super.getItems()) {
            ItemAttributes itemAtt = super.getTradeItemAttributes(i);
            if (i.getPrice() < itemAtt.getInputGoods().getNc()) {
                result.add(i);
            }
        }
        
        return result;
    }
    
    /**
     * Potvrd� celou operaci a zap�e data do datab�ze. P�ed zaps�m�, prov�d� kontrolu,
     * zda jsou v�echny pot�ebn� �daje zad�ny.
     * Kontroluje zda bylo dopln�n alespo� jedno zbo��. Zda je spr�vn� zad�n datum.
     * A zda je zad�n dodavatel
     * @throws java.lang.Exception Vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z�, nebo nejsou vypln�ny v�echny pot�ebn� polo�ky
     * @return vrac� vytvo�enou v�dejku
     */
    public TradeItemPreview confirm() throws Exception {
        try {
            DatabaseAccess.setAutoCommit(false);

            Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
            PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                        "INSERT INTO " + SALE_LISTING_NAME + "(number, date, cust_id, total_pc_dph, total_dph, total_pc, reduction, author, user_id" +
                        " ) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            check(); // prove� kontroly
            
            int number = getNextNumber();

            // Zapi� p�ehled p��jemky
            pstm.setInt(1, number);
            pstm.setTimestamp(2, new java.sql.Timestamp(super.getDate().getTimeInMillis()));
            if (discount) { // prov�d� se maloobchod
                pstm.setObject(3, null); // podle hodnoty null v odb�rateli rozezn� maloobchod
            } else {
                pstm.setInt(3, super.getSuplier().getId());
            }
            pstm.setDouble(4, super.getTotalPriceDPH().doubleValue() );
            pstm.setDouble(5, super.getTotalDPH().doubleValue() );
            pstm.setDouble(6, super.getTotalPrice().doubleValue() );
            pstm.setDouble(7, super.getReduction().doubleValue() );
            pstm.setString(8, super.getClient().getName());
            if (super.getClient().getUserId() == -1)  // V�choz� p�ihl�en� 
                pstm.setObject(9, null); // nastav na null
            else
                pstm.setInt(9, super.getClient().getUserId()); // nastav na skute�n� ��slo u�ivatele
            pstm.executeUpdate();
            
            int id = super.getLastId(stm);
            writeItemsToDatabase(id); // zapi� polo�ky p��jemky do datab�ze 
            
            pstm.close();
            stm.close();
            
            return new Sale().getSale(id);
            
        } catch (Exception e) {
            DatabaseAccess.rollBack();
            throw e;
        }
    }
    
    /**
     *  Provede Storno V�dejky, kter� byla d��ve provedena.
     *  Aktualizuje stav zbo�� na sklad�.
     *  Vol�n� m� smysl pouze, kdy� byl vol�n konstruktor
     *  <code>DoBuy(Client client, TradeItemPreview oldTradeItemPreview)</code>
     *  jinak nen� co stornovat
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z�
     */
    public void storno() throws SQLException {
        
        // Jestli�e nen� ��dn� p�edchoz� p��jemka, nen� co stornovat.
        if (super.getOldTradeItemPreview() == null)
            return;
        
        try {
            DatabaseAccess.setAutoCommit(false);
            Store store = new Store();
            Sale sale = new Sale();
            ArrayList<TradeItem> items = sale.getAllSaleItem(super.getOldTradeItemPreview());

            // Projdi v�echny polo�ky p��jemky
            for (TradeItem i: items) {
                // Ode�ti od zbo�� mno�stv� kter� je na p��jemce
                BigDecimal oldQuantity = new BigDecimal(store.getGoodsByID(i.getGoodsId(), true).getQuantity());
                // D�le�it� - no�stv� je t�eba p�i��st, nebo� se ru�� v�dejka
                BigDecimal newQuantity = oldQuantity.add( new BigDecimal(i.getQuantity()) );
                store.editQuantity(i.getGoodsId(), newQuantity.doubleValue()); // aktualizuj cenu
            }

            sale.deleteSale(super.getOldTradeItemPreview()); // Vyma� p��jemku
            super.setStornoCalled(true);
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        }
    }    
    
    /**
     * Vrac� odb�ratele p��jemky
     * @return Odb�ratel p��jemky
     */
    public Customer getCustomer() {
        return (Customer) super.getSuplier();
    }

    /**
     * Vrac�, zda je zpracov�v�n maloobchodn� prodej
     * @return true jestli�e je zpracov�v�n malooobchodn� prodej
     * false jestli�e se jedn� o velkoobchodn� prodej
     */
    public boolean isDiscount() {
        return discount;
    }

    /**
     * Nastavuje, zda je zpracov�v�n maloobchodn� prodej
     * 
     * @param discount Nastavuje, zda je zpracov�v�n maloobchodn� prodej
     * true jestli�e je zpracov�v�n malooobchodn� prodej
     * false jestli�e se jedn� o velkoobchodn� prodej
     * @throws java.sql.SQLException chyba s datab�z�
     */
    public void setDiscount(boolean discount) throws SQLException {
        this.discount = discount;
        this.setLastUsePrice(DoBuy.USE_PCD_FOR_SUM);
    }
      
}
