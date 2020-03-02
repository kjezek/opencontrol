/*
 * StoreMan.java
 *
 * Created on 13. záøí 2005, 23:20
 */

package cz.control.business;

import cz.control.errors.InvalidPrivilegException;
import cz.control.data.PriceList;
import cz.control.data.Customer;
import cz.control.data.Goods;
import cz.control.data.About;
import cz.control.business.Sale;
import cz.control.business.Buy;
import cz.control.business.Account;
import cz.control.data.Client;
import cz.control.data.StockingPreview;
import cz.control.data.Suplier;
import cz.control.data.TradeItemPreview;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

/**
 * Program Control - Skladový systém
 *
 * Tøída dìdí od tøídy User a implementuje její abstraktní metody, 
 * tak aby mìl uživatel pøístup pouze k akcím ke kterým je oprávnìn
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public final class StoreMan extends User {
    
    /**
     * Vytvoøí nového uživatele 
     * @param userName uživatelské jméno
     */
    StoreMan(Client client) {
        super(client);
    }
    
    public Recaps openRecaps() throws InvalidPrivilegException {
        throw new InvalidPrivilegException();
    }
    
    public void deleteMonthRecap(Calendar calendar)  throws InvalidPrivilegException, SQLException  {
        throw new InvalidPrivilegException();
    }
    
    public Statistik openStatistik() throws InvalidPrivilegException{
        return new Statistik();
    }
    
    public Buy openBuy() throws InvalidPrivilegException {
        return buy = new Buy();
    }
    
    public Sale openSale() throws InvalidPrivilegException{
        throw new InvalidPrivilegException();
    }
    
    public Account openAccount() throws InvalidPrivilegException {
        throw new InvalidPrivilegException();
    }
    
    public DoStocking openDoStocking() throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public DoStocking openDoStocking(StockingPreview oldStockingPreview) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
    
    public DoRecap openDoRecap() throws InvalidPrivilegException {
        throw new InvalidPrivilegException();
    }
    
    public void editUser(String loginName, String newName, String newLoginName, String newPassword) throws InvalidPrivilegException, SQLException {
        new Account().editUser(loginName, newLoginName, newName, newPassword);
    }
    
    public Store openStore() throws InvalidPrivilegException {
        return store = new Store();
    }
    
    public void editGoods(Goods oldGoods, Goods newGoods) throws InvalidPrivilegException, SQLException {
        store.editGoods(oldGoods, newGoods);
    }
    
    public void deleteGoods(Goods goods) throws InvalidPrivilegException, SQLException {
        store.deleteGoods(goods);
    }
    
    @Override
    public void deleteGoods(List<Goods> goods) throws InvalidPrivilegException, SQLException {
        store.deleteGoods(goods);
    }
    
    
    public void createGoods(Goods goods) throws InvalidPrivilegException, SQLException {
        store.createGoods(goods);
    }
    
    public void editPrice(String goodsId, int nc, int pcA, int pcB, int pcC, int pcD) throws InvalidPrivilegException, SQLException {
        store.editPrice(goodsId, nc, pcA, pcB, pcC, pcD);
    }
    
    public DoBuy openDoBuy() throws InvalidPrivilegException, SQLException {
        return new DoBuy(super.getClient());
    }
    
    public DoBuy openDoBuy(TradeItemPreview tradeItemPreview) throws InvalidPrivilegException, SQLException {
        return new DoBuy(super.getClient(), tradeItemPreview);
    }
    
    public DoSale openDoSale() throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public DoSale openDoSale(TradeItemPreview tradeItemPreview) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
    
    public Customers openCustomers() throws InvalidPrivilegException {
        throw new InvalidPrivilegException();
    }
    
    public void createCustomer(Customer customer) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
    
    public void editCustomer(Customer oldCustomer, Customer newCustomer) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
            
    public void deleteCustomer(Customer customer) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
    
    public Supliers openSupliers() throws InvalidPrivilegException {
        return supliers = new Supliers();
    }
    
    public void createSuplier(Suplier suplier) throws InvalidPrivilegException, SQLException {
        supliers.createSuplier(suplier);
    }
    
    public void editSuplier(Suplier oldSuplier, Suplier newSuplier) throws InvalidPrivilegException, SQLException {
        supliers.editSuplier(oldSuplier, newSuplier);
    }
            
    public void deleteSuplier(Suplier suplier) throws InvalidPrivilegException, SQLException {
        supliers.deleteSuplier(suplier);
    }
    
    public Stockings openStockings() throws InvalidPrivilegException {
        throw new InvalidPrivilegException();
    }
    
    public void changeStockingLock(StockingPreview stockingPreview, boolean lock) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public PriceListEditor openPriceListEditor() throws InvalidPrivilegException {
        return priceListEditor = new PriceListEditor();
    }
    
    public void deletePriceList(PriceList priceList) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public void createPriceList(PriceList priceList)throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
    
    public void editPriceList(PriceList oldPriceList, PriceList newPriceList ) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public AboutEditor openAboutEditor() throws InvalidPrivilegException {
        return aboutEditor = new AboutEditor();
    }
    
    public void deleteAbout(About about) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public void createAbout(About about) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }

    public void editAbout(About oldAbout, About newAbout) throws InvalidPrivilegException, SQLException {
        throw new InvalidPrivilegException();
    }
    
    /**
     * Vrací øetìzec obsahující jméno uživatele
     * @return Jméno uživatele
     */
    public String toString() { 
        return super.toString() + " (Skladník)";
    }
}
