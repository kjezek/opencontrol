/*
 * User.java
 *
 * Created on 13. záøí 2005, 20:09
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
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

/**
 * Program Control - Skladový systém
 *
 * Tøída zpøístupòující moduly systému jednotlivým uživatelùm. Obsahuje sadu abstraktních funkcí,
 * které je tøeba impkementovat u potomkù, tak aby provádìli potøebný kód, nebo aby vyvolávali 
 * vyjímku InvalidPrivilegException podle toho, zdá má uživatel pøíslušná práva
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
abstract public class User {
    private String userName;
        
    Buy buy = null;
    Sale sale = null;
//    Account account = null; // Tøída uživatelských úètù
    Store store = null; // Tøída skladu
    Customers customers = null; // Tøída zákazníkù
    Supliers supliers = null; // Tøída dodavatelù
    Stockings stockings = null; // tøída inventur
    Recaps recaps = null; // tøída rekapitulací
    PriceListEditor priceListEditor = null; // Tøída pro ceníky
    AboutEditor aboutEditor = null;
    
    private Client client;
    
    /** 
     *  Vytvoøí nového uživatele
     */
    User(Client client) {
        this.client = client;
        this.userName = client.getName();
    }
    
    /**
     * Zpøístupní tøídu pracující s rekapitulacemi v databázi
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @return tøídu pracující s rekapitulacemi v databázi
     */
    abstract public Recaps openRecaps() throws InvalidPrivilegException;
    
    /**
     * Vymaže jednu mìsíèní inventuru podle zadaného data z databáze
     * @param date datum urèijící, který mìsíc se má vymazat
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException Vyvolá jestliže dojde pøi práci s databází k chybì
     */
    abstract public void deleteMonthRecap(Calendar date) throws InvalidPrivilegException, SQLException;
    
    /**
     * Vrátí tøídu poskytující statistické výpoèty
     * 
     * @return Tøídu pracující s pøíjemkami
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Statistik openStatistik() throws InvalidPrivilegException;
    
    /**
     * Vrátí tøídu pracující s pøíjemkami
     * 
     * @return Tøídu pracující s pøíjemkami
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Buy openBuy() throws InvalidPrivilegException;
    
    /**
     * Vrátí tøídu pracující s výdejkami
     * 
     * @return Tøídu pracující s pøíjemkami
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Sale openSale() throws InvalidPrivilegException;
    
    /**
     *  Zpøístupní tøídu pro editaci uživatelských úètù
     * 
     * @return instanci uživatelských úètù
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Account openAccount() throws InvalidPrivilegException;
    
    /**
     * Zpøístupní tøídu pro tvorbu inventur
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     * @return instanci tøídy pro tvorbu inventur
     */
    abstract public DoStocking openDoStocking() throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro tvorbu inventur
     * 
     * @return instanci tøídy pro tvorbu inventur
     * @param oldStockingPreview døíve provedená inventura, která se bude editovat
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public DoStocking openDoStocking(StockingPreview oldStockingPreview) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro tvorbu rekapitulací
     * @return Vrací tøídu tvoøící rekapitulace
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public DoRecap openDoRecap() throws InvalidPrivilegException;
    
    /**
     * Zmìní uživatele systému
     * 
     * @param loginName staré pøihlašovací jméno
     * @param newName nové jméno uživatele
     * @param newLoginName nové pøihlašovací jméno
     * @param newPassword nové heslo
     *
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editUser(String loginName, String newName, String newLoginName, String newPassword) throws InvalidPrivilegException, SQLException;
    
    /**
     *  Zpøístupní tøídu pro editaci skladu
     * 
     * @return instanci skladu
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Store openStore() throws InvalidPrivilegException;
    
    /**
     *  Edituje kartu zboží na skladì
     * @param oldGoods ukazatel na instanci staré zboží
     * @param newGoods ukazatel na instanci nové zboží
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editGoods(Goods oldGoods, Goods newGoods) throws InvalidPrivilegException, SQLException;
    
    /**
     * Vymaže kartu zboží na skladì
     * @param goods ukazatel na instanci zboží
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void deleteGoods(Goods goods) throws InvalidPrivilegException, SQLException;

    /**
     * Vymaže karty zboží na skladì
     * @param goods ukazatel na instanci zboží
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void deleteGoods(List<Goods> goods) throws InvalidPrivilegException, SQLException;
    
    
    /**
     *  Vytvoøí novou kartu zboží
     * @param goods ukazatel na instanci zboží
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void createGoods(Goods goods) throws InvalidPrivilegException, SQLException;
    
    /**
     *  Nastaví cenu zboží
     * @param goodsId ukazatel na instanci zboží
     * @param nc nákupní cena
     * @param pcA prodejní cena A
     * @param pcB prodejní cena B
     * @param pcC prodejní cena C
     * @param pcD prodejní cena D
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editPrice(String goodsId, int nc, int pcA, int pcB, int pcC, int pcD) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro provedení pøíjmu zboží 
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     * @return tøídu vytváøející pøíjemky 
     */
    abstract public DoBuy openDoBuy() throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro provedení pøíjmu zboží. Pøedvyplní se podle již vytvoøené pøíjemky
     * 
     * @return tøídu vytváøející pøíjemky
     * @param tradeItemPreview pøíjemka, která bude sloužit pro získání poèátaèních údajù
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public DoBuy openDoBuy(TradeItemPreview tradeItemPreview) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro provedení expedice zboží 
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     * @return tøídu vytváøející výdejky 
     */
    abstract public DoSale openDoSale() throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro provedení expedice zboží 
     * @param tradeItemPreview pøíjemka, která bude sloužit pro získání poèátaèních údajù
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     * @return tøídu vytváøející výdejky 
     */
    abstract public DoSale openDoSale(TradeItemPreview tradeItemPreview) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pracující s kartami odbìratelù (zákazníkù)
     * 
     * @return Tøídu pracující se zákazníky
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Customers openCustomers() throws InvalidPrivilegException;
    
    /**
     * Vytvoøí nového odbìratele v databázi
     * @param customer instance ukazující na odbìratele
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void createCustomer(Customer customer) throws InvalidPrivilegException, SQLException;
    
    /**
     * Edituje odbìratele 
     * @param oldCustomer instance ukazující na starého odbìratele
     * @param newCustomer instance ukazující na nového odbìratele
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editCustomer(Customer oldCustomer, Customer newCustomer) throws InvalidPrivilegException, SQLException;
            
    /**
     * Vymaže odbìratele
     * @param customer Vymaže zákazníka z databáze 
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void deleteCustomer(Customer customer) throws InvalidPrivilegException, SQLException;

    /**
     *  Zpøístupní tøídu pro práci se zákaznickými kartami
     * 
     * @return instanci ukazující na objekt pracující s odbìrateli
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Supliers openSupliers() throws InvalidPrivilegException;
    
    /**
     * Vytvoøí nového dodavatele v databázi
     * @param suplier instance ukazující na dodavatele
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void createSuplier(Suplier suplier) throws InvalidPrivilegException, SQLException;
    
    /**
     * Edituje dodavatele
     * @param oldSuplier instance ukazující na starého dodavatele
     * @param newSuplier instance ukazující na nového dodavatele
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editSuplier(Suplier oldSuplier, Suplier newSuplier) throws InvalidPrivilegException, SQLException;
            
    /**
     * Vymaže dodavatele
     * @param suplier instance ukazující na existujícího dodavatele
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void deleteSuplier(Suplier suplier) throws InvalidPrivilegException, SQLException;
    
    
    /**
     * Zpøístupní tøídu pro pøístup k pøehledu inventur
     * 
     * @return tøídu pro pøístup k pøehledu inventur
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public Stockings openStockings() throws InvalidPrivilegException;
    
    /**
     * Zmìní zámek u inventury
     * 
     * @param stockingPreview inventura, u které se má znìnit zámek
     * @param lock zámek
     * @throws control.business.InvalidPrivilegException Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void changeStockingLock(StockingPreview stockingPreview, boolean lock) throws InvalidPrivilegException, SQLException;
    
    
    /**
     * Otevøe tøídu pracující s ceníky
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     */
    abstract public PriceListEditor openPriceListEditor() throws InvalidPrivilegException;
    
    /**
     * Vymaže z databáze ceník
     * @param priceList ceník k vymazání
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází 
     */
    abstract public void deletePriceList(PriceList priceList) throws InvalidPrivilegException, SQLException;

    /**
     *  Zapíše do databáze ceník podle vzstupního parametru.
     * @param priceList ceník k vytvoøení
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void createPriceList(PriceList priceList)throws InvalidPrivilegException, SQLException;
    
    /**
     * Provede zmìnu ceníku
     * @param oldPriceList starý ceník
     * @param newPriceList nový ceník
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editPriceList(PriceList oldPriceList, PriceList newPriceList ) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zpøístupní tøídu pro údaje o spoleènosti
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @return 
     */
    abstract public AboutEditor openAboutEditor() throws InvalidPrivilegException;
    
    /**
     * Vymaže objekt z databáze
     * @param about objekt, který se má vymazat
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void deleteAbout(About about) throws InvalidPrivilegException, SQLException;
    /**
     * Vytvoøí objekt v databázi
     * @param about objekt, který se má vytvoøit
     * @throws control.business.InvalidPrivilegException  Vyvolá jestliže uživatel, nemá dostateèná práva
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void createAbout(About about) throws InvalidPrivilegException, SQLException;
    /**
     * Zmìní objekt v databází 
     *
     * @param oldAbout starý objekt
     * @param newAbout nový objekt
     * @throws control.business.InvalidPrivilegException 
     * @throws java.sql.SQLException vyvolá jestliže došlo k chybì pøi práci s databází
     */
    abstract public void editAbout(About oldAbout, About newAbout) throws InvalidPrivilegException, SQLException;
  
    /**
     * Vrací jméno pøihlášeného uživatele
     * @return jméno pøihlášeného uživatele
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Vrací øetìzec obsahující jméno uživatele
     * @return Jméno uživatele
     */
    public String toString() { 
        return userName;
    }

    /**
     * Vrátí odkaz na pøihlášeného klienta
     * @return Pøihlášený klient
     */
    public Client getClient() {
        return client;
    }
}
