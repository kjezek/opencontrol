/*
 * DoStocking.java
 *
 * Vytvo�eno 1. b�ezen 2006, 16:08
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;

import cz.control.data.Stocking;
import cz.control.data.Goods;
import cz.control.data.Client;
import cz.control.data.StockingPreview;
import cz.control.database.DatabaseAccess;
import java.util.*;
import java.math.*;
import java.sql.*;

import static cz.control.database.DatabaseAccess.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da pro vytvo�en� inventury. T��da umo��uje nastavit ve�ker� pot�ebn� �daje o jedn�
 * inventu�e a n�sledn� j� zapsat do datab�ze 
 *
 * @author Kamil Je�ek
 *
 * (C) 2006, ver. 1.0
 *
 */
public class DoStocking {
    
    private Client client = null;
    private Calendar date = new GregorianCalendar();
    
    private int priceType = DoBuy.USE_NC_FOR_SUM; // typ ceny, kter� se m� pou��t
    
    /** Uchov�v� jednotliv� polo�ky inventury */
    private Map<Goods, Stocking> stockingItems = new HashMap<Goods, Stocking>();
    
    private boolean stornoCalled = false; // zda byla stornov�na p�edchoz� inventura
    private StockingPreview oldStockingPreview; //p�edchoz� inventura
    private int id = 0;
    
    private BigDecimal totalDiferPrice = BigDecimal.ZERO; // celkov� finan�n� rozd�l
    private String text = "B�n� inventura"; // Textov� pozn�mka
    private boolean lock = false; // z�mek inventury

    private boolean cancelCalled = false;
    
    /**
     * Vytvo�� novou instanci DoStocking
     */
    DoStocking(Client client) throws SQLException {
        this.client = client;
        //DatabaseAccess.setAutoCommit(false);
    }
    
    /**
     * Vytvo�� nov� objekt DoStocking. Pou�ije hodnoty z d��ve vytvo�en� inventury. 
     * Tento konstruktor je vhodn� pou��t pro bezpe�n� stornov�n� inventury, 
     * nebo pro editaci inventury
     * @param client klient, kter� otev�el inventuru
     * @param oldTradeItemPreview D��ve vytvo�en� inventura
     * @throws java.sql.SQLException 
     */
    DoStocking(Client client, StockingPreview oldStockingPreview) throws SQLException{
        this.oldStockingPreview = oldStockingPreview;
        this.client = client;
        this.cancelCalled = false;
        //DatabaseAccess.setAutoCommit(false);

        setStockingItemAttributes();
    } 
    
    /**
     *  Nastav� atributy inventury podle dan� vstupn� inventury
     */
    private void setStockingItemAttributes() throws SQLException {
        setDate( oldStockingPreview.getDate() );
        setText(oldStockingPreview.getText());
        setLock(oldStockingPreview.isLock());
        forSumUsePrice(oldStockingPreview.getUsedPrice());
        
        // Nastav jednotliv� polo�ky
        Stockings stockings = new Stockings();
        Store store = new Store();
        // Nastav hodnoty
        for (Stocking i: stockings.getAllStockingItems(oldStockingPreview)) {
            Goods goods = store.getGoodsByID(i.getGoodsId());
            
            // Vytvo� zbo�� s mno�stv�m 
            // stav na sklad� - rozd�l inventury
            Goods newGoods = new Goods(goods.getGoodsID(), goods.getName(), goods.getType(),
                    goods.getDph(), goods.getUnit(), goods.getEan(),
                    goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(),
                    goods.getPcD(), new BigDecimal(goods.getQuantity()).subtract( new BigDecimal(i.getDifer()) ).doubleValue() );
            
            //Dopl� zbo�� podle hodnoty po ode�ten� rozd�lu p�edchoz� inventury
            addStockingItem(newGoods);
            
            // Nastav mno�stv� podle hodnoty z p�edchoz� inventury
            setQuantity(goods.getGoodsID(), goods.getQuantity());
        }
    }
      
    /**
     *  Nastav�, kter� cena se m� pou��t pro sou�et na p��jemce
     * 
     * @param priceType ud�v� jak� cena se m� pou��t. 
     * Pou��teln� konstanty jsou:
     * DoBuy.USE_NC_FOR_SUM -  Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t N�kupn� cena (NC)
     * DoBuy.USE_PCA_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t prvn� Prodejn� cena (PC A)
     * DoBuy.USE_PCB_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t druh� Prodejn� cena (PC B)
     * DoBuy.USE_PCC_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t t�et� Prodejn� cena (PC C)
     * DoBuy.USE_PCD_FOR_SUM - Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t �tvrt� Prodejn� cena (PC D)
     * @throws java.sql.SQLException 
     */
    public void forSumUsePrice(int priceType) throws SQLException {
        
        if (this.priceType == priceType)
            return; // Jestli�e nen� pot�eba, cenu nep�enastavuj
        
        this.priceType = priceType;

        // Nastav� mno�stv� na stejn� hodnoty
        // vzhledem k tomu, �e je v�ak p�epnuto na jinou cenu, zp�sob� tato akce 
        // p�epo�et celkov� ceny
        for (Goods i: stockingItems.keySet()) {
            Stocking stocking = stockingItems.get(i);
            setQuantity(i.getGoodsID(), new BigDecimal(i.getQuantity()).add( new BigDecimal(stocking.getDifer())).doubleValue() );
        }
    }
    
    
    /**
     * Vyma�e polo�ku inventury ze seznamu
     * @param goods Zbo��, kter� nem� b�t obsa�eno v inventu�e
     * @return Vrac� vymazanou polo�ku inventury, nebo null, jestli�e bylo vymaz�n� 
     * ne�sp�n�
     */
    public Stocking deleteStockingItem(Goods goods) {
 
        Stocking result = stockingItems.remove(goods);
        
        if (result != null) {
            // Ode�ti cenu rozd�lu
            totalDiferPrice = totalDiferPrice
                    .subtract( new BigDecimal(result.getPrice() )
                    .multiply( new BigDecimal(result.getDifer()) )
                    .divide(Store.CENT) 
                    );
        }
        
        return result;
    }
    
    /**
     * Nastavi datum a �as proveden� inventury
     * @param date Objekt p�edstavujc� datum a �as
     */
    public void setDate(Calendar date) {
    
        this.date = new GregorianCalendar(); // Zjisti aktu�ln� kalend�� kv�li �asu
        
        this.date.set(Calendar.YEAR, date.get(Calendar.YEAR)); // nastav rok
        this.date.set(Calendar.MONTH, date.get(Calendar.MONTH)); // nastav m�s�c
        this.date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH)); // nastav den
    }    
    
    /**
     * Nastav� spo��tan� mno�stv� zbo��. Podle zad�n� a mno�stv� zbo�� na sklad� 
     * vypo�te zji�t�n� rozd�l.
     * @param goodsID Zbo��, kter� se po��talo
     * @param quantity spo�ten� mno�stv�
     * @return true - jestli�e bylo nov� mno�stv� spr�vn� nastaveno
     */
    public boolean setQuantity(String goodsID, double quantity) {
        Goods goods = null;
        
        // Vyhledej p��slu�n� kl��
        for (Goods i: stockingItems.keySet()) {
            if (i.getGoodsID().equalsIgnoreCase(goodsID)) {
                goods = i;
                break;
            }
        }
        
        if (goods == null)
            return false;
        
        Stocking stocking = null;
        
        if ( (stocking = stockingItems.get(goods)) == null)
            return false;
        
        // Nastav diferenci, podle rozd�lu mno�stv� zbo�� na sklad� a u�ivatelovo zad�n�
        BigDecimal difer = new BigDecimal(quantity).subtract( new BigDecimal(goods.getQuantity()) );
        Stocking newStocking = new Stocking(stocking.getStockingId(), stocking.getStockingIdListing(),
                stocking.getGoodsId(), stocking.getName(), stocking.getDph(),
                getPrice(goods), difer.doubleValue(), stocking.getUnit());
        stockingItems.put(goods, newStocking);
        
        // Ode�ti p�edchoz� cenu
        totalDiferPrice = totalDiferPrice
                .subtract( new BigDecimal( stocking.getPrice() )
                .multiply( new BigDecimal(stocking.getDifer()) )
                .divide(Store.CENT)
                );
        // Nastav novou cenu
        totalDiferPrice = totalDiferPrice
                .add( new BigDecimal( newStocking.getPrice() )
                .multiply( new BigDecimal( newStocking.getDifer()) )
                .divide(Store.CENT)
                );
        
        return true;
    }
    
    /**
     * Vlo�� dal�� zbo�� do inventury
     * @param goods zbo��, kter� se bude po��tat
     * @return V p��pad� �sp�n�ho vlo�en� vrac� inventurn� polo�ku, jinak vrac� null
     */
    public Stocking addStockingItem(Goods goods) {
        if (goods == null)
            return null;
        
        // Nedovol p�eps�n� ji� existuj�c� polo�ky
        if (stockingItems.containsKey(goods)) {
            return null;
        }
        
        //Vytvo� novou polo�ku
        Stocking newStocking = new Stocking(id++, 0,
                goods.getGoodsID(), goods.getName(), goods.getDph(),
                getPrice(goods), 0, goods.getUnit());      
        
        stockingItems.put(goods, newStocking);
        
        return newStocking;
    }
    
    /**
     * Nahrad� nov� zbo�� za star� v inventu�e
     * @param oldGoods star� zbo��
     * @param newGoods nov� zbo��
     * @return novou polo�ku inventury
     */
    public Stocking replaceStockingItem(Goods oldGoods, Goods newGoods) {
        if (deleteStockingItem(oldGoods) != null) {
            return addStockingItem(newGoods);
        } else  {
            return null;
        }
            
    }
    
    /**
     *  Vrac� n�kupn� cenu zbo�� 
     */
    private int getPrice(Goods goods) {
        int result = 0;
        switch (priceType) {
            case DoBuy.USE_NC_FOR_SUM :
                result = goods.getNc();
                break;
            case DoBuy.USE_PCA_FOR_SUM :
                result = goods.getPcA();
                break;
            case DoBuy.USE_PCB_FOR_SUM :
                result = goods.getPcB();
                break;
            case DoBuy.USE_PCC_FOR_SUM :
                result = goods.getPcC();
                break;
            case DoBuy.USE_PCD_FOR_SUM :
                result = goods.getPcD();
                break;
        }

        return result;
    }    
    
    /**
     * Vyhled� dal�� ��slo pou�iteln� pro inventuru podle datumu proveden� 
     */
    private int getNextNumber() throws SQLException{
      
        // Jestli�e existuje star� p��jemka, kter� byla stornov�na
        // a m� se vytvo�it p��jemka stejn�ho data, p�evezmi jej� ��slo
        // nebo� se jedn� o n�hradu star� p��jemky
        if (stornoCalled && oldStockingPreview != null &&
                getDate().get(Calendar.YEAR) == oldStockingPreview.getDate().get(Calendar.YEAR) &&
                getDate().get(Calendar.MONTH) == oldStockingPreview.getDate().get(Calendar.MONTH) &&
                getDate().get(Calendar.DAY_OF_MONTH) == oldStockingPreview.getDate().get(Calendar.DAY_OF_MONTH) ) {
            
            return oldStockingPreview.getNumber();
        }
        
        Stockings stockings = new Stockings();
        
        int result = stockings.getMaxStockingNumber(date);
        return result + 1; // minim�ln� ��slo je jedna
    }    
    
    /**
     * Zjist� naposledy pou�it� ID v transakci
     */
    int getLastId(Statement stm) throws SQLException{
    
        // zjisti naposledy pou�it� ID
        ResultSet rs = stm.executeQuery("SELECT last_insert_id()");
        
        rs.next();
        int result = rs.getInt(1);
        rs.close();
        return result;
    }
    
    /**
     * Zap�e jednotliv� polo�ky inventury do datab�ze.
     * Z�rove� aktualizuje stav zbo�� na sklad�
     */
    private void writeItemsToDatabase(int id) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                    "INSERT INTO " + STOCKING_TABLE_NAME + " (id_stocking_listing, goods_id, name, dph, price, difer, unit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
        Store store = new Store(); // vytvo� t��du pro pr�ci ze skladem
        // zjist� jak� ID bylo p�i�azeno p��jemce
        
        // Projdi v�echny polo�ky p��jemky a zapi� je do datab�ze
        // a zaroven aktualizuj mnozstvi zbozi na sklade
        for (Stocking i: stockingItems.values()) {
            // zapi� polo�ku p��jemky 
            pstm.setInt(1, id);
            pstm.setString(2, i.getGoodsId());
            pstm.setString(3, i.getName());
            pstm.setInt(4, i.getDph());
            pstm.setDouble(5,  (new BigDecimal(i.getPrice())).divide(Store.CENT).doubleValue() );
            pstm.setDouble(6, i.getDifer());
            pstm.setString(7, i.getUnit());
            pstm.executeUpdate();
            
            // Aktualizuj stav zbo�� na sklad�
            Goods goods = store.getGoodsByID(i.getGoodsId(), true);
            BigDecimal newQuantity = new BigDecimal(goods.getQuantity()).add( new BigDecimal(i.getDifer()) ); 
            store.editQuantity(i.getGoodsId(), newQuantity.doubleValue());
        }        
        pstm.close();
    }
    
    /**
     *  Provede Storno inventury, kter� byla d��ve provedena.
     *  Aktualizuje stav zbo�� na sklad�.
     *  Vol�n� m� smysl pouze, kdy� byl vol�n konstruktor
     *  <code>DoStocking(Client client, Stocking preview stockingPreview)</code>
     *  jinak nen� co stornovat
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z�
     */
    public void storno() throws SQLException {
        
        // Jestli�e nen� ��dn� p�edchoz� inventura, nen� co stornovat.
        if (oldStockingPreview == null)
            return;
        
        try {
            DatabaseAccess.setAutoCommit(false);
            Store store = new Store();
            Stockings stockings = new Stockings();
            ArrayList<Stocking> items = stockings.getAllStockingItems(oldStockingPreview);

            // Projdi v�echny polo�ky p��jemky
            for (Stocking i: items) {
                // Ode�ti od zbo�� rozd�l v minul� inventu�e nalezen�
                BigDecimal oldQuantity = new BigDecimal(store.getGoodsByID(i.getGoodsId(), true).getQuantity());
                BigDecimal newQuantity = oldQuantity.subtract( new BigDecimal(i.getDifer()) );
                store.editQuantity(i.getGoodsId(), newQuantity.doubleValue()); // aktualizuj cenu
            }

            stockings.deleteStocking(oldStockingPreview); // Vyma� p��jemku
            stornoCalled = true;
            
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        } 
    }    
    
    /**
     * Provede potvrzen� opreace. 
     * P�ed zaps�m�, prov�d� kontrolu,
     * zda jsou v�echny pot�ebn� �daje zad�ny.
     * Kontroluje zda bylo dopln�n alespo� jedno zbo��. Zda je spr�vn� zad�n datum.
     * 
     * @throws java.lang.Exception V p��pad� chyby
     * @return vrac� vytvo�enou inventuru
     */
    public StockingPreview confirm() throws Exception {
        try {
            DatabaseAccess.setAutoCommit(false);

            Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
            PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                        "INSERT INTO " + STOCKING_LISTING_TABLE_NAME + "(number, date, difer, author, user_id, text, is_lock, use_price" +
                        " ) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                
            if (stockingItems.isEmpty()) {
                throw new Exception("Inventura neobsahuje ��dn� polo�ky zbo��");
            }
        
            
            int number = getNextNumber();

            // Zapi� p�ehled p��jemky
            pstm.setInt(1, number);
            pstm.setTimestamp(2, new java.sql.Timestamp(date.getTimeInMillis()));
            pstm.setDouble(3, getTotalDiferPrice().doubleValue() );
            pstm.setString(4, client.getName());
            if (client.getUserId() == -1)  // V�choz� p�ihl�en� 
                pstm.setObject(5, null); // nastav na null
            else
                pstm.setInt(5, client.getUserId()); // nastav na skute�n� ��slo u�ivatele
            pstm.setString(6, text);
            pstm.setBoolean(7, lock);
            pstm.setInt(8, priceType); //jak� cena byla pou�ita pro sou�et
            pstm.executeUpdate();
            
            int id = getLastId(stm);
            writeItemsToDatabase(id); // zapi� polo�ky p��jemky do datab�ze 

            stm.close();
            pstm.close();
            
            return new Stockings().getStocking(id);
            
        } catch (SQLException e) {
            // P�i chyb� v�e vr�t�me
            DatabaseAccess.rollBack();
            throw e;
        }
    }    
    
    /**
     * Zru�� vytv��enou inventuru. Vr�t� v�echny p��padn� zm�ny v datab�zi
     * cancel() je t�eba volat v�dy, kdy� nem� b�t p��jemka potvrzena
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z� 
     */
    public void cancel() throws SQLException {
        
        if (cancelCalled) {
            return;
        }
                    
        DatabaseAccess.setAutoCommit(false);
        DatabaseAccess.rollBack();
        DatabaseAccess.setAutoCommit(true);
        cancelCalled = true;
    }    
    
    /**
     * Potvrd� v�echny zm�ny a zap�e data do datab�ze.
     * Vol�n� metod <CODE>confirm()</CODE> a <CODE>storno()</CODE> bude m�t efekt a� po vol�n� t�to metody
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z�
     */
    public void update() throws SQLException {
        
        DatabaseAccess.setAutoCommit(false);
        DatabaseAccess.commit();
        DatabaseAccess.setAutoCommit(true);
    }    
    
    /**
     * Vrac� datum proveden� inventury
     * @return datum proveden� inventury
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Vrac� celkov� rozd�l inventury v pen�z�ch
     * @return rozd�l inventury v pen�z�ch
     */
    public BigDecimal getTotalDiferPrice() {
        return totalDiferPrice;
    }

    /**
     * Vrac� textovou pozn�mku inventury
     * @return textov� pozn�mka inventury
     */
    public String getText() {
        return text;
    }

    /**
     * Nastav� textovou pozn�mku inventury
     * @param text textov� pozn�mku inventury
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Vrac�, zda je nastaven z�mek inventury
     * @return z�mek
     */
    public boolean isLock() {
        return lock;
    }

    /**
     * Nastavuje z�mek inventury
     * @param lock z�mek inventury
     */
    public void setLock(boolean lock) {
        this.lock = lock;
    }
    
    /**
     * Vrac� polo�ky zbo�� na inventu�e
     * @return polo�ky zbo��
     */
    public TreeSet<Goods> getAllGoodsItems() {
        TreeSet<Goods> result = new TreeSet<Goods>(stockingItems.keySet());

        return result;
    }
    
    /**
     * Vrac�, kter� cena se pou��v� pro sou�et
     * @return kter� cena se pou��v� pro sou�et
     */
    public int getUsePrice() {
        return priceType;
    }

}
