/*
 * User.java
 *
 * Created on 13. z��� 2005, 20:09
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
 * Program Control - Skladov� syst�m
 *
 * T��da zp��stup�uj�c� moduly syst�mu jednotliv�m u�ivatel�m. Obsahuje sadu abstraktn�ch funkc�,
 * kter� je t�eba impkementovat u potomk�, tak aby prov�d�li pot�ebn� k�d, nebo aby vyvol�vali 
 * vyj�mku InvalidPrivilegException podle toho, zd� m� u�ivatel p��slu�n� pr�va
 *
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
abstract public class User {
    private String userName;
        
    Buy buy = null;
    Sale sale = null;
//    Account account = null; // T��da u�ivatelsk�ch ��t�
    Store store = null; // T��da skladu
    Customers customers = null; // T��da z�kazn�k�
    Supliers supliers = null; // T��da dodavatel�
    Stockings stockings = null; // t��da inventur
    Recaps recaps = null; // t��da rekapitulac�
    PriceListEditor priceListEditor = null; // T��da pro cen�ky
    AboutEditor aboutEditor = null;
    
    private Client client;
    
    /** 
     *  Vytvo�� nov�ho u�ivatele
     */
    User(Client client) {
        this.client = client;
        this.userName = client.getName();
    }
    
    /**
     * Zp��stupn� t��du pracuj�c� s rekapitulacemi v datab�zi
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @return t��du pracuj�c� s rekapitulacemi v datab�zi
     */
    abstract public Recaps openRecaps() throws InvalidPrivilegException;
    
    /**
     * Vyma�e jednu m�s��n� inventuru podle zadan�ho data z datab�ze
     * @param date datum ur�ij�c�, kter� m�s�c se m� vymazat
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException Vyvol� jestli�e dojde p�i pr�ci s datab�z� k chyb�
     */
    abstract public void deleteMonthRecap(Calendar date) throws InvalidPrivilegException, SQLException;
    
    /**
     * Vr�t� t��du poskytuj�c� statistick� v�po�ty
     * 
     * @return T��du pracuj�c� s p��jemkami
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Statistik openStatistik() throws InvalidPrivilegException;
    
    /**
     * Vr�t� t��du pracuj�c� s p��jemkami
     * 
     * @return T��du pracuj�c� s p��jemkami
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Buy openBuy() throws InvalidPrivilegException;
    
    /**
     * Vr�t� t��du pracuj�c� s v�dejkami
     * 
     * @return T��du pracuj�c� s p��jemkami
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Sale openSale() throws InvalidPrivilegException;
    
    /**
     *  Zp��stupn� t��du pro editaci u�ivatelsk�ch ��t�
     * 
     * @return instanci u�ivatelsk�ch ��t�
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Account openAccount() throws InvalidPrivilegException;
    
    /**
     * Zp��stupn� t��du pro tvorbu inventur
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     * @return instanci t��dy pro tvorbu inventur
     */
    abstract public DoStocking openDoStocking() throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro tvorbu inventur
     * 
     * @return instanci t��dy pro tvorbu inventur
     * @param oldStockingPreview d��ve proveden� inventura, kter� se bude editovat
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public DoStocking openDoStocking(StockingPreview oldStockingPreview) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro tvorbu rekapitulac�
     * @return Vrac� t��du tvo��c� rekapitulace
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public DoRecap openDoRecap() throws InvalidPrivilegException;
    
    /**
     * Zm�n� u�ivatele syst�mu
     * 
     * @param loginName star� p�ihla�ovac� jm�no
     * @param newName nov� jm�no u�ivatele
     * @param newLoginName nov� p�ihla�ovac� jm�no
     * @param newPassword nov� heslo
     *
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editUser(String loginName, String newName, String newLoginName, String newPassword) throws InvalidPrivilegException, SQLException;
    
    /**
     *  Zp��stupn� t��du pro editaci skladu
     * 
     * @return instanci skladu
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Store openStore() throws InvalidPrivilegException;
    
    /**
     *  Edituje kartu zbo�� na sklad�
     * @param oldGoods ukazatel na instanci star� zbo��
     * @param newGoods ukazatel na instanci nov� zbo��
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editGoods(Goods oldGoods, Goods newGoods) throws InvalidPrivilegException, SQLException;
    
    /**
     * Vyma�e kartu zbo�� na sklad�
     * @param goods ukazatel na instanci zbo��
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void deleteGoods(Goods goods) throws InvalidPrivilegException, SQLException;

    /**
     * Vyma�e karty zbo�� na sklad�
     * @param goods ukazatel na instanci zbo��
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void deleteGoods(List<Goods> goods) throws InvalidPrivilegException, SQLException;
    
    
    /**
     *  Vytvo�� novou kartu zbo��
     * @param goods ukazatel na instanci zbo��
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void createGoods(Goods goods) throws InvalidPrivilegException, SQLException;
    
    /**
     *  Nastav� cenu zbo��
     * @param goodsId ukazatel na instanci zbo��
     * @param nc n�kupn� cena
     * @param pcA prodejn� cena A
     * @param pcB prodejn� cena B
     * @param pcC prodejn� cena C
     * @param pcD prodejn� cena D
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editPrice(String goodsId, int nc, int pcA, int pcB, int pcC, int pcD) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro proveden� p��jmu zbo�� 
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     * @return t��du vytv��ej�c� p��jemky 
     */
    abstract public DoBuy openDoBuy() throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro proveden� p��jmu zbo��. P�edvypln� se podle ji� vytvo�en� p��jemky
     * 
     * @return t��du vytv��ej�c� p��jemky
     * @param tradeItemPreview p��jemka, kter� bude slou�it pro z�sk�n� po��ta�n�ch �daj�
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public DoBuy openDoBuy(TradeItemPreview tradeItemPreview) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro proveden� expedice zbo�� 
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     * @return t��du vytv��ej�c� v�dejky 
     */
    abstract public DoSale openDoSale() throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro proveden� expedice zbo�� 
     * @param tradeItemPreview p��jemka, kter� bude slou�it pro z�sk�n� po��ta�n�ch �daj�
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     * @return t��du vytv��ej�c� v�dejky 
     */
    abstract public DoSale openDoSale(TradeItemPreview tradeItemPreview) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pracuj�c� s kartami odb�ratel� (z�kazn�k�)
     * 
     * @return T��du pracuj�c� se z�kazn�ky
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Customers openCustomers() throws InvalidPrivilegException;
    
    /**
     * Vytvo�� nov�ho odb�ratele v datab�zi
     * @param customer instance ukazuj�c� na odb�ratele
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void createCustomer(Customer customer) throws InvalidPrivilegException, SQLException;
    
    /**
     * Edituje odb�ratele 
     * @param oldCustomer instance ukazuj�c� na star�ho odb�ratele
     * @param newCustomer instance ukazuj�c� na nov�ho odb�ratele
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editCustomer(Customer oldCustomer, Customer newCustomer) throws InvalidPrivilegException, SQLException;
            
    /**
     * Vyma�e odb�ratele
     * @param customer Vyma�e z�kazn�ka z datab�ze 
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void deleteCustomer(Customer customer) throws InvalidPrivilegException, SQLException;

    /**
     *  Zp��stupn� t��du pro pr�ci se z�kaznick�mi kartami
     * 
     * @return instanci ukazuj�c� na objekt pracuj�c� s odb�rateli
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Supliers openSupliers() throws InvalidPrivilegException;
    
    /**
     * Vytvo�� nov�ho dodavatele v datab�zi
     * @param suplier instance ukazuj�c� na dodavatele
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void createSuplier(Suplier suplier) throws InvalidPrivilegException, SQLException;
    
    /**
     * Edituje dodavatele
     * @param oldSuplier instance ukazuj�c� na star�ho dodavatele
     * @param newSuplier instance ukazuj�c� na nov�ho dodavatele
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editSuplier(Suplier oldSuplier, Suplier newSuplier) throws InvalidPrivilegException, SQLException;
            
    /**
     * Vyma�e dodavatele
     * @param suplier instance ukazuj�c� na existuj�c�ho dodavatele
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void deleteSuplier(Suplier suplier) throws InvalidPrivilegException, SQLException;
    
    
    /**
     * Zp��stupn� t��du pro p��stup k p�ehledu inventur
     * 
     * @return t��du pro p��stup k p�ehledu inventur
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public Stockings openStockings() throws InvalidPrivilegException;
    
    /**
     * Zm�n� z�mek u inventury
     * 
     * @param stockingPreview inventura, u kter� se m� zn�nit z�mek
     * @param lock z�mek
     * @throws control.business.InvalidPrivilegException Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void changeStockingLock(StockingPreview stockingPreview, boolean lock) throws InvalidPrivilegException, SQLException;
    
    
    /**
     * Otev�e t��du pracuj�c� s cen�ky
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     */
    abstract public PriceListEditor openPriceListEditor() throws InvalidPrivilegException;
    
    /**
     * Vyma�e z datab�ze cen�k
     * @param priceList cen�k k vymaz�n�
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z� 
     */
    abstract public void deletePriceList(PriceList priceList) throws InvalidPrivilegException, SQLException;

    /**
     *  Zap�e do datab�ze cen�k podle vzstupn�ho parametru.
     * @param priceList cen�k k vytvo�en�
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void createPriceList(PriceList priceList)throws InvalidPrivilegException, SQLException;
    
    /**
     * Provede zm�nu cen�ku
     * @param oldPriceList star� cen�k
     * @param newPriceList nov� cen�k
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editPriceList(PriceList oldPriceList, PriceList newPriceList ) throws InvalidPrivilegException, SQLException;
    
    /**
     * Zp��stupn� t��du pro �daje o spole�nosti
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @return 
     */
    abstract public AboutEditor openAboutEditor() throws InvalidPrivilegException;
    
    /**
     * Vyma�e objekt z datab�ze
     * @param about objekt, kter� se m� vymazat
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void deleteAbout(About about) throws InvalidPrivilegException, SQLException;
    /**
     * Vytvo�� objekt v datab�zi
     * @param about objekt, kter� se m� vytvo�it
     * @throws control.business.InvalidPrivilegException  Vyvol� jestli�e u�ivatel, nem� dostate�n� pr�va
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void createAbout(About about) throws InvalidPrivilegException, SQLException;
    /**
     * Zm�n� objekt v datab�z� 
     *
     * @param oldAbout star� objekt
     * @param newAbout nov� objekt
     * @throws control.business.InvalidPrivilegException 
     * @throws java.sql.SQLException vyvol� jestli�e do�lo k chyb� p�i pr�ci s datab�z�
     */
    abstract public void editAbout(About oldAbout, About newAbout) throws InvalidPrivilegException, SQLException;
  
    /**
     * Vrac� jm�no p�ihl�en�ho u�ivatele
     * @return jm�no p�ihl�en�ho u�ivatele
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Vrac� �et�zec obsahuj�c� jm�no u�ivatele
     * @return Jm�no u�ivatele
     */
    public String toString() { 
        return userName;
    }

    /**
     * Vr�t� odkaz na p�ihl�en�ho klienta
     * @return P�ihl�en� klient
     */
    public Client getClient() {
        return client;
    }
}
