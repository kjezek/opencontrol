/*
 * DoStocking.java
 *
 * Vytvoøeno 1. bøezen 2006, 16:08
 *
 * Autor: Kamil Jeek
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
 * Program Control - Skladovı systém
 *
 * Tøída pro vytvoøení inventury. Tøída umoòuje nastavit veškeré potøebnì údaje o jedné
 * inventuøe a následnì jí zapsat do databáze 
 *
 * @author Kamil Jeek
 *
 * (C) 2006, ver. 1.0
 *
 */
public class DoStocking {
    
    private Client client = null;
    private Calendar date = new GregorianCalendar();
    
    private int priceType = DoBuy.USE_NC_FOR_SUM; // typ ceny, kterı se má pouít
    
    /** Uchovává jednotlivé poloky inventury */
    private Map<Goods, Stocking> stockingItems = new HashMap<Goods, Stocking>();
    
    private boolean stornoCalled = false; // zda byla stornována pøedchozí inventura
    private StockingPreview oldStockingPreview; //pøedchozí inventura
    private int id = 0;
    
    private BigDecimal totalDiferPrice = BigDecimal.ZERO; // celkovı finanèní rozdíl
    private String text = "Bìná inventura"; // Textová poznámka
    private boolean lock = false; // zámek inventury

    private boolean cancelCalled = false;
    
    /**
     * Vytvoøí novou instanci DoStocking
     */
    DoStocking(Client client) throws SQLException {
        this.client = client;
        //DatabaseAccess.setAutoCommit(false);
    }
    
    /**
     * Vytvoøí novı objekt DoStocking. Pouije hodnoty z døíve vytvoøené inventury. 
     * Tento konstruktor je vhodnı pouít pro bezpeèné stornování inventury, 
     * nebo pro editaci inventury
     * @param client klient, kterı otevøel inventuru
     * @param oldTradeItemPreview Døíve vytvoøená inventura
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
     *  Nastaví atributy inventury podle dané vstupní inventury
     */
    private void setStockingItemAttributes() throws SQLException {
        setDate( oldStockingPreview.getDate() );
        setText(oldStockingPreview.getText());
        setLock(oldStockingPreview.isLock());
        forSumUsePrice(oldStockingPreview.getUsedPrice());
        
        // Nastav jednotlivé poloky
        Stockings stockings = new Stockings();
        Store store = new Store();
        // Nastav hodnoty
        for (Stocking i: stockings.getAllStockingItems(oldStockingPreview)) {
            Goods goods = store.getGoodsByID(i.getGoodsId());
            
            // Vytvoø zboí s mnostvím 
            // stav na skladì - rozdíl inventury
            Goods newGoods = new Goods(goods.getGoodsID(), goods.getName(), goods.getType(),
                    goods.getDph(), goods.getUnit(), goods.getEan(),
                    goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(),
                    goods.getPcD(), new BigDecimal(goods.getQuantity()).subtract( new BigDecimal(i.getDifer()) ).doubleValue() );
            
            //Doplò zboí podle hodnoty po odeètení rozdílu pøedchozí inventury
            addStockingItem(newGoods);
            
            // Nastav mnoství podle hodnoty z pøedchozí inventury
            setQuantity(goods.getGoodsID(), goods.getQuantity());
        }
    }
      
    /**
     *  Nastaví, která cena se má pouít pro souèet na pøíjemce
     * 
     * @param priceType udává jaká cena se má pouít. 
     * Pouítelné konstanty jsou:
     * DoBuy.USE_NC_FOR_SUM -  Konstoanta øíkající, e se má pro souèet ceny na pøíjemce pouít Nákupní cena (NC)
     * DoBuy.USE_PCA_FOR_SUM - Konstoanta øíkající, e se má pro souèet ceny na pøíjemce pouít první Prodejní cena (PC A)
     * DoBuy.USE_PCB_FOR_SUM - Konstoanta øíkající, e se má pro souèet ceny na pøíjemce pouít druhá Prodejní cena (PC B)
     * DoBuy.USE_PCC_FOR_SUM - Konstoanta øíkající, e se má pro souèet ceny na pøíjemce pouít tøetí Prodejní cena (PC C)
     * DoBuy.USE_PCD_FOR_SUM - Konstoanta øíkající, e se má pro souèet ceny na pøíjemce pouít ètvrtá Prodejní cena (PC D)
     * @throws java.sql.SQLException 
     */
    public void forSumUsePrice(int priceType) throws SQLException {
        
        if (this.priceType == priceType)
            return; // Jestlie není potøeba, cenu nepøenastavuj
        
        this.priceType = priceType;

        // Nastaví mnoství na stejné hodnoty
        // vzhledem k tomu, e je však pøepnuto na jinou cenu, zpùsobí tato akce 
        // pøepoèet celkové ceny
        for (Goods i: stockingItems.keySet()) {
            Stocking stocking = stockingItems.get(i);
            setQuantity(i.getGoodsID(), new BigDecimal(i.getQuantity()).add( new BigDecimal(stocking.getDifer())).doubleValue() );
        }
    }
    
    
    /**
     * Vymae poloku inventury ze seznamu
     * @param goods Zboí, které nemá bıt obsaeno v inventuøe
     * @return Vrací vymazanou poloku inventury, nebo null, jestlie bylo vymazání 
     * neúspìšné
     */
    public Stocking deleteStockingItem(Goods goods) {
 
        Stocking result = stockingItems.remove(goods);
        
        if (result != null) {
            // Odeèti cenu rozdílu
            totalDiferPrice = totalDiferPrice
                    .subtract( new BigDecimal(result.getPrice() )
                    .multiply( new BigDecimal(result.getDifer()) )
                    .divide(Store.CENT) 
                    );
        }
        
        return result;
    }
    
    /**
     * Nastavi datum a èas provedení inventury
     * @param date Objekt pøedstavujcí datum a èas
     */
    public void setDate(Calendar date) {
    
        this.date = new GregorianCalendar(); // Zjisti aktuální kalendáø kvùli èasu
        
        this.date.set(Calendar.YEAR, date.get(Calendar.YEAR)); // nastav rok
        this.date.set(Calendar.MONTH, date.get(Calendar.MONTH)); // nastav mìsíc
        this.date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH)); // nastav den
    }    
    
    /**
     * Nastaví spoèítané mnoství zboí. Podle zadání a mnoství zboí na skladì 
     * vypoète zjištìnı rozdíl.
     * @param goodsID Zboí, které se poèítalo
     * @param quantity spoètené mnoství
     * @return true - jestlie bylo nové mnoství správnì nastaveno
     */
    public boolean setQuantity(String goodsID, double quantity) {
        Goods goods = null;
        
        // Vyhledej pøíslušnı klíè
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
        
        // Nastav diferenci, podle rozdílu mnoství zboí na skladì a uivatelovo zadání
        BigDecimal difer = new BigDecimal(quantity).subtract( new BigDecimal(goods.getQuantity()) );
        Stocking newStocking = new Stocking(stocking.getStockingId(), stocking.getStockingIdListing(),
                stocking.getGoodsId(), stocking.getName(), stocking.getDph(),
                getPrice(goods), difer.doubleValue(), stocking.getUnit());
        stockingItems.put(goods, newStocking);
        
        // Odeèti pøedchozí cenu
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
     * Vloí další zboí do inventury
     * @param goods zboí, které se bude poèítat
     * @return V pøípadì úspìšného vloení vrací inventurní poloku, jinak vrací null
     */
    public Stocking addStockingItem(Goods goods) {
        if (goods == null)
            return null;
        
        // Nedovol pøepsání ji existující poloky
        if (stockingItems.containsKey(goods)) {
            return null;
        }
        
        //Vytvoø novou poloku
        Stocking newStocking = new Stocking(id++, 0,
                goods.getGoodsID(), goods.getName(), goods.getDph(),
                getPrice(goods), 0, goods.getUnit());      
        
        stockingItems.put(goods, newStocking);
        
        return newStocking;
    }
    
    /**
     * Nahradí nové zboí za staré v inventuøe
     * @param oldGoods staré zboí
     * @param newGoods nové zboí
     * @return novou poloku inventury
     */
    public Stocking replaceStockingItem(Goods oldGoods, Goods newGoods) {
        if (deleteStockingItem(oldGoods) != null) {
            return addStockingItem(newGoods);
        } else  {
            return null;
        }
            
    }
    
    /**
     *  Vrací nákupní cenu zboí 
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
     * Vyhledá další èíslo pouitelné pro inventuru podle datumu provedení 
     */
    private int getNextNumber() throws SQLException{
      
        // Jestlie existuje stará pøíjemka, která byla stornována
        // a má se vytvoøit pøíjemka stejného data, pøevezmi její èíslo
        // nebo se jedná o náhradu staré pøíjemky
        if (stornoCalled && oldStockingPreview != null &&
                getDate().get(Calendar.YEAR) == oldStockingPreview.getDate().get(Calendar.YEAR) &&
                getDate().get(Calendar.MONTH) == oldStockingPreview.getDate().get(Calendar.MONTH) &&
                getDate().get(Calendar.DAY_OF_MONTH) == oldStockingPreview.getDate().get(Calendar.DAY_OF_MONTH) ) {
            
            return oldStockingPreview.getNumber();
        }
        
        Stockings stockings = new Stockings();
        
        int result = stockings.getMaxStockingNumber(date);
        return result + 1; // minimální èíslo je jedna
    }    
    
    /**
     * Zjistí naposledy pouité ID v transakci
     */
    int getLastId(Statement stm) throws SQLException{
    
        // zjisti naposledy pouité ID
        ResultSet rs = stm.executeQuery("SELECT last_insert_id()");
        
        rs.next();
        int result = rs.getInt(1);
        rs.close();
        return result;
    }
    
    /**
     * Zapíše jednotlivé poloky inventury do databáze.
     * Zároveò aktualizuje stav zboí na skladì
     */
    private void writeItemsToDatabase(int id) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                    "INSERT INTO " + STOCKING_TABLE_NAME + " (id_stocking_listing, goods_id, name, dph, price, difer, unit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
        Store store = new Store(); // vytvoø tøídu pro práci ze skladem
        // zjistí jaké ID bylo pøiøazeno pøíjemce
        
        // Projdi všechny poloky pøíjemky a zapiš je do databáze
        // a zaroven aktualizuj mnozstvi zbozi na sklade
        for (Stocking i: stockingItems.values()) {
            // zapiš poloku pøíjemky 
            pstm.setInt(1, id);
            pstm.setString(2, i.getGoodsId());
            pstm.setString(3, i.getName());
            pstm.setInt(4, i.getDph());
            pstm.setDouble(5,  (new BigDecimal(i.getPrice())).divide(Store.CENT).doubleValue() );
            pstm.setDouble(6, i.getDifer());
            pstm.setString(7, i.getUnit());
            pstm.executeUpdate();
            
            // Aktualizuj stav zboí na skladì
            Goods goods = store.getGoodsByID(i.getGoodsId(), true);
            BigDecimal newQuantity = new BigDecimal(goods.getQuantity()).add( new BigDecimal(i.getDifer()) ); 
            store.editQuantity(i.getGoodsId(), newQuantity.doubleValue());
        }        
        pstm.close();
    }
    
    /**
     *  Provede Storno inventury, která byla døíve provedena.
     *  Aktualizuje stav zboí na skladì.
     *  Volání má smysl pouze, kdy byl volán konstruktor
     *  <code>DoStocking(Client client, Stocking preview stockingPreview)</code>
     *  jinak není co stornovat
     * @throws java.sql.SQLException Vyvolá, jestlie dojde k chybì pøi práci s databází
     */
    public void storno() throws SQLException {
        
        // Jestlie není ádná pøedchozí inventura, není co stornovat.
        if (oldStockingPreview == null)
            return;
        
        try {
            DatabaseAccess.setAutoCommit(false);
            Store store = new Store();
            Stockings stockings = new Stockings();
            ArrayList<Stocking> items = stockings.getAllStockingItems(oldStockingPreview);

            // Projdi všechny poloky pøíjemky
            for (Stocking i: items) {
                // Odeèti od zboí rozdíl v minulé inventuøe nalezenı
                BigDecimal oldQuantity = new BigDecimal(store.getGoodsByID(i.getGoodsId(), true).getQuantity());
                BigDecimal newQuantity = oldQuantity.subtract( new BigDecimal(i.getDifer()) );
                store.editQuantity(i.getGoodsId(), newQuantity.doubleValue()); // aktualizuj cenu
            }

            stockings.deleteStocking(oldStockingPreview); // Vyma pøíjemku
            stornoCalled = true;
            
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        } 
    }    
    
    /**
     * Provede potvrzení opreace. 
     * Pøed zapsámí, provádí kontrolu,
     * zda jsou všechny potøebné údaje zadány.
     * Kontroluje zda bylo doplnìn alespoò jedno zboí. Zda je správnì zadán datum.
     * 
     * @throws java.lang.Exception V pøípadì chyby
     * @return vrací vytvoøenou inventuru
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
                throw new Exception("Inventura neobsahuje ádné poloky zboí");
            }
        
            
            int number = getNextNumber();

            // Zapiš pøehled pøíjemky
            pstm.setInt(1, number);
            pstm.setTimestamp(2, new java.sql.Timestamp(date.getTimeInMillis()));
            pstm.setDouble(3, getTotalDiferPrice().doubleValue() );
            pstm.setString(4, client.getName());
            if (client.getUserId() == -1)  // Vıchozí pøihlášení 
                pstm.setObject(5, null); // nastav na null
            else
                pstm.setInt(5, client.getUserId()); // nastav na skuteèné èíslo uivatele
            pstm.setString(6, text);
            pstm.setBoolean(7, lock);
            pstm.setInt(8, priceType); //jaká cena byla pouita pro souèet
            pstm.executeUpdate();
            
            int id = getLastId(stm);
            writeItemsToDatabase(id); // zapiš poloky pøíjemky do databáze 

            stm.close();
            pstm.close();
            
            return new Stockings().getStocking(id);
            
        } catch (SQLException e) {
            // Pøi chybì vše vrátíme
            DatabaseAccess.rollBack();
            throw e;
        }
    }    
    
    /**
     * Zruší vytváøenou inventuru. Vrátí všechny pøípadné zmìny v databázi
     * cancel() je tøeba volat vdy, kdy nemá bıt pøíjemka potvrzena
     * @throws java.sql.SQLException Vyvolá, jestlie dojde k chybì pøi práci s databází 
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
     * Potvrdí všechny zmìny a zapíše data do databáze.
     * Volání metod <CODE>confirm()</CODE> a <CODE>storno()</CODE> bude mít efekt a po volání této metody
     * @throws java.sql.SQLException Vyvolá, jestlie dojde k chybì pøi práci s databází
     */
    public void update() throws SQLException {
        
        DatabaseAccess.setAutoCommit(false);
        DatabaseAccess.commit();
        DatabaseAccess.setAutoCommit(true);
    }    
    
    /**
     * Vrací datum provedení inventury
     * @return datum provedení inventury
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Vrací celkovı rozdíl inventury v penìzích
     * @return rozdíl inventury v penìzích
     */
    public BigDecimal getTotalDiferPrice() {
        return totalDiferPrice;
    }

    /**
     * Vrací textovou poznámku inventury
     * @return textová poznámka inventury
     */
    public String getText() {
        return text;
    }

    /**
     * Nastaví textovou poznámku inventury
     * @param text textová poznámku inventury
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Vrací, zda je nastaven zámek inventury
     * @return zámek
     */
    public boolean isLock() {
        return lock;
    }

    /**
     * Nastavuje zámek inventury
     * @param lock zámek inventury
     */
    public void setLock(boolean lock) {
        this.lock = lock;
    }
    
    /**
     * Vrací poloky zboí na inventuøe
     * @return poloky zboí
     */
    public TreeSet<Goods> getAllGoodsItems() {
        TreeSet<Goods> result = new TreeSet<Goods>(stockingItems.keySet());

        return result;
    }
    
    /**
     * Vrací, která cena se pouívá pro souèet
     * @return která cena se pouívá pro souèet
     */
    public int getUsePrice() {
        return priceType;
    }

}
