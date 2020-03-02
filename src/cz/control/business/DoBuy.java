/*
 * DoBuy.java
 *
 * Vytvoøeno 3. listopad 2005, 19:59
 *
 
 */
package cz.control.business; 

import cz.control.data.PriceList;
import cz.control.data.Goods;
import cz.control.data.Client;
import cz.control.data.Suplier;
import cz.control.data.TradeItem;
import cz.control.data.TradeItemPreview;
import cz.control.database.DatabaseAccess;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída pro vytvoøení pøíjemky. Tøída umožòuje nastavit veškeré potøebnì údaje o jedné
 * pøíjemce a následnì jí zapsat do databáze 
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 *
 */

public class DoBuy {
    
    private static final String BUY_NAME = DatabaseAccess.BUY_TABLE_NAME; // ulož název databáze
    private static final String BUY_LISTING_NAME = DatabaseAccess.BUY_LISITNG_TABLE_NAME; // ulož název databáze
    static final String GOODS_NAME = DatabaseAccess.GOODS_TABLE_NAME; // ulož název databáze
    
    /**
     * Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít Nákupní cena (NC)
     */
    public static final int USE_NC_FOR_SUM = 0;
    /**
     * Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít první Prodejní cena (PC A)
     */
    public static final int USE_PCA_FOR_SUM = 1;
    /**
     * Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít druhá Prodejní cena (PC B)
     */
    public static final int USE_PCB_FOR_SUM = 2;
    /**
     * Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít tøetí Prodejní cena (PC C)
     */
    public static final int USE_PCC_FOR_SUM = 3;
    /**
     * Konstoanta øíkající, že se má pro souèet ceny na pøíjemce použít ètvrtá Prodejní cena (PC D)
     */
    public static final int USE_PCD_FOR_SUM = 4;
    
    /**
     *  Konstanta urèující zaokrouhlení. Urèuje, že se má zaokrouhlovat na padesátníky
     */
    public static final double ROUND_SCALE_TO_050 = 2.0;
    /**
     *  Konstanta urèující zaokrouhlení. Urèuje, že se má zaokrouhlovat celé koruny
     */
    public static final double ROUND_SCALE_TO_100 = 1.0;
    /**
     *  Konstanta urèující zaokrouhlení. Urèuje, že se NEBUDE zaokrouhlovat
     */
    public static final double ROUND_SCALE_UNNECESSARY = -1;
    
    
    private BigDecimal reduction = BigDecimal.ZERO; // celková sleva 
    private Calendar date; // datum a èas provedení pøíjemky 
    private BigDecimal totalDPH = BigDecimal.ZERO; // celková cena DPH v Kè 
    private BigDecimal totalPrice = BigDecimal.ZERO;// celková cena zboží na pøíjemce (souèet všech cen * množství)
    private Suplier suplier = null;
    
    int tradeId = 0; // èísluje položky pøíjemky 
    private boolean cancelCalled = false;
    private boolean stornoCalled = false; // indikuje, zda byla vstupní pøíjemka stornována
    private int priceType = USE_NC_FOR_SUM; // typ ceny, který se má použít
    private String billNumber = ""; // èíslo faktury, nebo dodacího listu
    private boolean isCash = false; // true pokud bylo placeno hotovì

    private double roundScale = -1;
    
    private Client client = null; // Ukazatel na uživatele, který otevøel pøíjemku 
    
    private TradeItemPreview oldTradeItemPreview = null; 
    
    /* Položky pøíjemky */
    private Map<TradeItem, ItemAttributes> items = new HashMap<TradeItem, ItemAttributes>();
    
    /* Mapa uchovávájící aktuální množství zboží na skladì */
    private Map<String, Double> availableQuantity = new HashMap<String, Double>();
    
    /** Typ ceny, který se použije */
    private int lastUsePrice = USE_NC_FOR_SUM;
    
            
    /**
     * Vytvoøí nový objekt DoBuy.
     * Tento konstruktor je vhodný pro vytváøení nové pøíjemky
     * @param client 
     * @throws java.sql.SQLException 
     */
    DoBuy(Client client) throws SQLException{
        this.date = new GregorianCalendar();
        this.client = client;
        this.cancelCalled = false;
        //DatabaseAccess.setAutoCommit(false);
    }
    
    /**
     * Vytvoøí nový objekt DoBuy. Použije hodnoty z døíve vytvoøené pøíjemky. 
     * Tento konstruktor je vhodný použít pro bezpeèné stornování pøíjemky, 
     * nebo pro editaci pøíjemky
     * @param client klient, který otevøel pøíjemku
     * @param oldTradeItemPreview Døíve vytvoøená pøíjemka
     * @throws java.sql.SQLException 
     */
    DoBuy(Client client, TradeItemPreview oldTradeItemPreview) throws SQLException{
        this.oldTradeItemPreview = oldTradeItemPreview;
        this.client = client;
        this.cancelCalled = false;
        //DatabaseAccess.setAutoCommit(false);
        setTradeItemAttributes();
    } 
    
    /**
     *  Nastaví atributy pøíjemky podle dané vstupní pøíjemky
     */
    private void setTradeItemAttributes() throws SQLException {
        setDate( oldTradeItemPreview.getDate() );
        setReduction( new BigDecimal(oldTradeItemPreview.getReduction()).intValue() );
        setSuplier( new Supliers().getSuplierByID(oldTradeItemPreview.getId()) );
        billNumber = oldTradeItemPreview.getBillNumber();
        isCash = oldTradeItemPreview.isCash();
        
        // Nastav jednotlivé položky
        Buy buy = new Buy();
// Nedá se použít, nebo je tøeba i aktualizovat ceny        
//        items = buy.getAllBuyItem(oldTradeItemPreview);
        
        Store store = new Store();
        
        for (TradeItem i: buy.getAllBuyItem(oldTradeItemPreview)) {
            // Naèti a ulož do mapy použitelné množství zboží
            Goods goods = store.getGoodsByID(i.getGoodsId());
            setAvailableQuantity(goods.getGoodsID(), goods.getQuantity());
        }
        
        // Aktualizuj ceny
        for (TradeItem i: buy.getAllBuyItem(oldTradeItemPreview)) {
            Goods goods = store.getGoodsByID(i.getGoodsId());
            // Sestav nové zboží tak, aby obsahovali NC z pøíjemky a PC ze zboží na skladì
            Goods newGoods = new Goods(
                    i.getGoodsId(), i.getName(), goods.getType(), i.getDph(), i.getUnit(), goods.getEan(),
                    i.getPrice(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(),
                    i.getQuantity());
            // Dùležité, ID musí souhlasit ze starou pøíjemkou
            tradeId = i.getTradeId();
            lastUsePrice = i.getUsePrice();
            addTradeItem(newGoods, i.getQuantity());
            
            //Nakonec nastav vypoètenou cenu, jako tu, která je uvedena u skladové karty na skladì
            //tedy byla vypoètena v minulém pøíjmu
            ItemAttributes itemAttr = getTradeItemAttributes(i);
            itemAttr.setInputGoods(goods); //Na vstupu je zboží ze skladu
            itemAttr.setComputedNc(goods.getNc());
            itemAttr.setComputePrices(-1); //Zámìrnì nastav "nesmysl" aby nedošlo k pøepoètení hned po vložení
        }
    }
    
    /**
     * Nastavuje, že se má koneèná cena zaokrouhlit
     * @param roundScale základ zaokrouhlení. Použijde konstanty z DoBuy.java ROUND_SCALE_...
     */
    public void makeRound(double roundScale) {
        this.roundScale = roundScale;
    }
    

    
    /**
     * Vymaže z pøíjemky jeden záznam
     * 
     * @param tradeItem Záznam, který má být vymazán
     * @return øádek z kterého bylo vymazáno
     */
    public int deleteTradeItem(TradeItem tradeItem) {
        ItemAttributes b = items.remove(tradeItem);
        if (b != null) {
            totalDPH = totalDPH.subtract(
                    (new BigDecimal(tradeItem.getDph()) ).divide(Store.CENT).multiply( new BigDecimal(tradeItem.getPrice()) ).multiply( new BigDecimal(tradeItem.getQuantity()) )
                    );
            totalPrice = totalPrice.subtract(
                    ( new BigDecimal(tradeItem.getPrice()) ).multiply( new BigDecimal(tradeItem.getQuantity()) )
                    );
            
            BigDecimal oldQuantity = new BigDecimal(getAvailableQuantity(tradeItem.getGoodsId()));
            setAvailableQuantity(tradeItem.getGoodsId(), oldQuantity.add( new BigDecimal(tradeItem.getQuantity())).doubleValue() );
            return b.getRow();
        }
        
        return -1;
    }
    
    /**
     * Zmìní položku pøíjemku
     * @param oldTradeItem stará položka, která bude nahrazena
     * @param newTradeItem nová položka, kterou bude nahrazeno
     * @return true jestlže byla položka zmìnìna. False vrací jestliže seznam již obsahuje 
     * položku shodnou s newTradeItem, nebo položka oldTradeItem v seznamu neexistuje 
     */
    private boolean editTradeItem(TradeItem oldTradeItem, TradeItem newTradeItem) {
    

        if (!items.containsKey(oldTradeItem)) {
            return false;
        }
        
        ItemAttributes tmp = items.remove(oldTradeItem);
        items.put(newTradeItem, tmp);
        return true;
    }
    
    /**
     * Nastaví celkou slevu na zboží 
     * @param reduction èíslo pøedstavující cenu. 
     * Poslední dvì èíslice jsou považovány za haléøe
     */
    public void setReduction(int reduction) {
        this.reduction = new BigDecimal(reduction);
   } 
    
    /**
     * Nastavi datum a èas provedení pøíjemky
     * @param date Objekt pøedstavujcí datum a èas
     */
    public void setDate(Calendar date) {
    
        this.date = new GregorianCalendar(); // Zjisti aktuální kalendáø kvùli èasu
        
        this.date.set(Calendar.YEAR, date.get(Calendar.YEAR)); // nastav rok
        this.date.set(Calendar.MONTH, date.get(Calendar.MONTH)); // nastav mìsíc
        this.date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH)); // nastav den
    }
    
    /**
     * Nastaví dodavatele, který dodal zboží uvedené v pøíjemce
     * @param suplier Odkaz na dodavatele, který byl pøedem uložen do databáze 
     */
    public void setSuplier(Suplier suplier) {
    
        this.suplier = suplier;
    }
    
    /**
     * Vypoète nákupní cenu podle nové zadané
     * @param tradeItem položka pøíjemky pro kterou poèítat
     * @param newNc nová nákupní cena
     */
    public void computeNC(TradeItem tradeItem, int newNc) {
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setNewNc(newNc);
        
        Goods inputGoods = itemAttr.getInputGoods();
        // Podle toho jaká cena se má použit, nastav pøíslušné hodnoty
        switch (itemAttr.getComputePrices()) {
            case ItemAttributes.LAST_PRICE : // Použít poslední cenu
                itemAttr.setComputedNc(itemAttr.getNewNc());
                break; 
            case ItemAttributes.EXPENSIVE_PRICE :  //Použít dražší cenu
                int price = (inputGoods.getNc() > itemAttr.getNewNc()) ? inputGoods.getNc() : itemAttr.getNewNc();
                itemAttr.setComputedNc(price);
                break; 
            case ItemAttributes.CHEAPER_PRICE :  //Použít levnìjší cenu
                price = (inputGoods.getNc() < itemAttr.getNewNc()) ? inputGoods.getNc() : itemAttr.getNewNc();
                itemAttr.setComputedNc(price);
                break; 
            case ItemAttributes.AVERAGE_PRICE : //Použít prùmìrnou cenu
                // Poèítá vážený prùmìr (výhy jsou množství na skladì a množství, které se nakupuje
                BigDecimal lastNc = new BigDecimal(inputGoods.getNc()).divide(Store.CENT);
                BigDecimal lastQuantity = new BigDecimal(inputGoods.getQuantity());
                BigDecimal nc = new BigDecimal(itemAttr.getNewNc()).divide(Store.CENT);
                BigDecimal newQuantity = new BigDecimal(tradeItem.getQuantity());
                
                if (lastQuantity.doubleValue() < 0) {
                    lastQuantity = BigDecimal.ZERO;
                }
                
                BigDecimal citatel = lastNc.multiply(lastQuantity).add( nc.multiply(newQuantity) ); 
                BigDecimal jmenovatel = lastQuantity.add(newQuantity);
                BigDecimal result = citatel.divide(jmenovatel, 2, RoundingMode.HALF_UP);

                price = result.multiply(Store.CENT).setScale(0).intValue();

                itemAttr.setComputedNc(price);
                break;
            case ItemAttributes.OLD_PRICE : //Použít starou cenu (ta která je u zboží na skladì)
                price = itemAttr.getInputGoods().getNc();
                itemAttr.setComputedNc(price);
                break;
            default:
                break;
        }
    
    }
    
    /**
     * Nastaví nové prodejní ceny pro položku pøíjemky
     * @param tradeItem položka pøíjemky, pro kterou se má nastavit
     * @param pcA vypoètená prodejní cena A
     * @param pcB vypoètená prodejní cena B
     * @param pcC vypoètená prodejní cena C 
     * @param pcD vypoètená prodejní cena D
     * @param usePrice typ ceny, která se použije
     */
    public void setNewPcPrices(TradeItem tradeItem, 
            int pcA, int pcB, int pcC, int pcD) {
    
        // Nastav nové hodnoty
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setNewPcA(pcA);
        itemAttr.setNewPcB(pcB);
        itemAttr.setNewPcC(pcC);
        itemAttr.setNewPcD(pcD);
        
        // Nastav novou prodejní cenu
        editPrice(tradeItem, getPrice(
                getTradeItemAsGoods(itemAttr.getRow()),
                lastUsePrice), lastUsePrice);
    }
    
    /**
     * Nastaví nové prodejní ceny pro položku pøíjemky
     * @param tradeItem položka pøíjemky, pro kterou se má nastavit
     * @param nc vypoètená nákupní cena
     * @param pcA vypoètená prodejní cena A
     * @param pcB vypoètená prodejní cena B
     * @param pcC vypoètená prodejní cena C 
     * @param pcD vypoètená prodejní cena D
     */
    public void setNewPrices(TradeItem tradeItem, 
            int nc, int pcA, int pcB, int pcC, int pcD,
            int usePrice) {
    
        // Nastav nové hodnoty
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setNewNc(nc);
        itemAttr.setNewPcA(pcA);
        itemAttr.setNewPcB(pcB);
        itemAttr.setNewPcC(pcC);
        itemAttr.setNewPcD(pcD);

        this.lastUsePrice = usePrice;
        
        // Nastav novou prodejní cenu
        editPrice(tradeItem, getPrice(
                getTradeItemAsGoods(itemAttr.getRow()),
                usePrice), usePrice);
    }    
    
    /**
     * Nastaví nový typ ceny
     * @param tradeItem
     * @param usePrice 
     */
    public TradeItem setNewUsePrice(TradeItem tradeItem, int usePrice) {
        ItemAttributes itemAttr = items.get(tradeItem);
        this.lastUsePrice = usePrice;
        // Nastav novou prodejní cenu
        return editPrice(tradeItem, getPrice(
                getTradeItemAsGoods(itemAttr.getRow()),
                usePrice), usePrice);        
    }
    
    /**
     * Nastavuje, jak se mají poèítat prodejní ceny
     * z nákupních cen
     * @param tradeItem Položka pøíjemky, pro kterou se má použít nový výpoèet
     * @param computePrice udává jak se má poèítat prodejní cena s nákupní ceny. 
     * Pro vstup použijte konstantu
     * ItemAttributes.AVERAGE_PRICE - pro zprùmìrování nákupní ceny
     * ItemAttributes.LAST_PRICE - pro použití poslední nákupní ceny
     * ItemAttributes.EXPENSIVE_PRICE - pro použití dražší nákupní ceny
     */
    public void setComputeNCPrice(TradeItem tradeItem, int computePrice) {
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setComputePrices(computePrice);
    }
    
    /**
     * Nastaví cenu, která bude uvedena na pøíjemce u zboží
     * 
     * @param price nová cena
     * @param tradeItem Pložka u které má být zmìnìna cena
     * @param typ ceny,  který se použije
     * @return Skladovou položku se zmìnìnou cenou, nebo null, jestliže došlo k chybì
     */
    private TradeItem editPrice(TradeItem tradeItem, int price, int usePrice) {
        
        if (tradeItem == null) {
            return null;
        }
        
        // Vytvoø nový objekt podle starého, ale ze zmìnìnou cenou
        TradeItem newTradeItem = new TradeItem(tradeItem.getTradeId(), 
                tradeItem.getTradeIdListing(), tradeItem.getGoodsId(), tradeItem.getName(),
                tradeItem.getDph(), price, tradeItem.getQuantity(), tradeItem.getUnit(),
                usePrice);
        
        editTradeItem(tradeItem, newTradeItem); // nahraï
        
        // aktualizuj cenu 
        totalPrice = totalPrice.add(
                ( new BigDecimal(tradeItem.getQuantity()) ).multiply( new BigDecimal(price - tradeItem.getPrice()) )
                );
        totalDPH = totalDPH.add(
                ( new BigDecimal(tradeItem.getDph())).divide(Store.CENT).multiply( new BigDecimal(tradeItem.getQuantity()) ).multiply( new BigDecimal( price - tradeItem.getPrice()) )
                );
        
        return newTradeItem;
    }
    
    /**
     * Nastaví množství zboží u jedné položky príjemky, kolik se bude nakupovat
     * 
     * @param tradeItem Položka pøíjemky
     * @param quantity Množství, kolik se nakoupí
     * @return Pøi úspìchu vrací true, false - jestliže nenní na skladì dostatek zboží
     */
    public boolean setQuantity(TradeItem tradeItem, double quantity) {
        
        if (tradeItem == null) {
            return false;
        }
        
        // Vytvoø nový objekt podle starého, ale ze zmìným množstvím 
        TradeItem newTradeItem = new TradeItem(tradeItem.getTradeId(), 
                tradeItem.getTradeIdListing(), tradeItem.getGoodsId(), tradeItem.getName(),
                tradeItem.getDph(), tradeItem.getPrice(), quantity, tradeItem.getUnit(),
                tradeItem.getUsePrice());

        // aktualizuj cenu
        totalPrice = totalPrice.add(
                ( new BigDecimal(tradeItem.getPrice())).multiply( new BigDecimal(quantity - tradeItem.getQuantity()) )
                );
        
        //aktualizuj daò
        totalDPH = totalDPH.add( 
                ( new BigDecimal(tradeItem.getDph())).divide(Store.CENT).multiply( new BigDecimal(tradeItem.getPrice())).multiply( new BigDecimal(quantity - tradeItem.getQuantity()) )
                );

        editTradeItem(tradeItem, newTradeItem); // nahraï
        
        BigDecimal oldQuantity = new BigDecimal(getAvailableQuantity(tradeItem.getGoodsId()));
        setAvailableQuantity(tradeItem.getGoodsId(), oldQuantity.add( new BigDecimal(tradeItem.getQuantity())).subtract( new BigDecimal(quantity)).doubleValue() );
        return true;
    }
    
    /**
     * Doplní do pøíjemky další položku
     * @param quantity Množství zboží, které se má nakoupit
     * @param goods Zboží, které má být doplnìno
     * @return Vrací true jestliže došlo k chybì. Nebo false, jestliže je chybnì zadané zboží,
     *  nebo je zadané záporné množství, nebo již pøíjmka takové zboží obsahuje 
     */
    public TradeItem addTradeItem(Goods goods, double quantity) {
        
        if (goods == null || goods.getGoodsID() == null || quantity < 0) {
            return null;
        }
        
        // Vytvoø novou položku pøíjemku 
        // hodnota id (tradeId, 0) slouží jenom doèasnì a bude se mìnit pøi zapisování do databáze
        TradeItem newItem = new TradeItem(tradeId++, 0, goods.getGoodsID(), goods.getName(),
                goods.getDph(), getPrice(goods, lastUsePrice), quantity, goods.getUnit(),
                lastUsePrice);
        
        // Nedovol duplicitní položky 
        if (items.containsKey(newItem)) {
            return null;
        }
        
        // pøièti novou cenu
        totalPrice = totalPrice.add(
                (new BigDecimal(getPrice(goods, lastUsePrice))).multiply( new BigDecimal(quantity))
                );
        totalDPH = totalDPH.add(
                ( new BigDecimal(newItem.getDph()) ).divide(Store.CENT).multiply( new BigDecimal( newItem.getPrice()) ).multiply( new BigDecimal(quantity) )
                );


        // Nastav atributy nového zboží
        setNewGoodsAttributes(goods, newItem);
        
        double oldQuantity = getAvailableQuantity(goods.getGoodsID());
        //jestliže položka v seznamu neexistuje
        if (oldQuantity == -1) {
            // Doplò položku
            setAvailableQuantity(goods.getGoodsID(), new BigDecimal(goods.getQuantity()).subtract( new BigDecimal(quantity)).doubleValue() );
        } else {
            //pøi vložením stejné položky sniž použitelné množství
            setAvailableQuantity(goods.getGoodsID(), new BigDecimal(oldQuantity).subtract( new BigDecimal(quantity)).doubleValue() );
        }

        return newItem;
    }
    
    /**
     *  Nastaví atributy novì vkládaného zboží. Jestliže už však v seznamu je stejné
     *  zboží nastaví vlastnosti podle nìj. To proto, aby uživatel nemusel napøíklad 
     *  neustále zadávat novou nákupní cenu pøi zadávání více položek
     */
    private void setNewGoodsAttributes(Goods goods, TradeItem newItem) {
        Goods newGoods = goods;

        //Nastav ceny ze zboží
        ItemAttributes itAttr = new ItemAttributes();
        itAttr.setNewNc(newGoods.getNc());
        itAttr.setComputedNc(newGoods.getNc()); // Prozaèátek je vypoètená cena stejná jako pùvodní
        
        //Jestliže už stejné zboží v pøíjemce existuje, nastav atributy podle nìj,
        //jinak nastav atributy podle novì vkládaného zboží
        for (TradeItem i: items.keySet()) {
            if (items.get(i).getInputGoods().equals(goods)) {
                newGoods = items.get(i).getInputGoods();
                //newGoods = getTradeItemAsGoods(items.get(i).getRow());
                itAttr.setComputePrices(items.get(i).getComputePrices());
                itAttr.setComputedNc(items.get(i).getComputedNc());
                itAttr.setNewNc(items.get(i).getNewNc());
                break;
            }
        }
        
        itAttr.setNewPcA(newGoods.getPcA());
        itAttr.setNewPcB(newGoods.getPcB());
        itAttr.setNewPcC(newGoods.getPcC());
        itAttr.setNewPcD(newGoods.getPcD());
        itAttr.setInputGoods(newGoods);
        
        items.put(newItem, itAttr);        
    }
    
    /**
     *  Vrací nákupní cenu zboží 
     */
    private int getPrice(Goods goods, int priceType) {
        int result = 0;
        switch (priceType) {
            case USE_NC_FOR_SUM :
                result = goods.getNc();
                break;
            case USE_PCA_FOR_SUM :
                result = goods.getPcA();
                break;
            case USE_PCB_FOR_SUM :
                result = goods.getPcB();
                break;
            case USE_PCC_FOR_SUM :
                result = goods.getPcC();
                break;
            case USE_PCD_FOR_SUM :
                result = goods.getPcD();
                break;
            default:
                result = goods.getNc();
        }

        return result;
    }
    
    /**
     * Vyhledá další èíslo použitelné pro pøíjemku podle datumu provedení pøíjemky 
     */
    private int getNextNumber() throws SQLException{
      
        // Jestliže existuje stará pøíjemka, která byla stornována
        // a má se vytvoøit pøíjemka stejného data, pøevezmi její èíslo
        // nebo se jedná o náhradu staré pøíjemky
        if (stornoCalled && oldTradeItemPreview != null &&
                getDate().get(Calendar.YEAR) == oldTradeItemPreview.getDate().get(Calendar.YEAR) &&
                getDate().get(Calendar.MONTH) == oldTradeItemPreview.getDate().get(Calendar.MONTH) &&
                getDate().get(Calendar.DAY_OF_MONTH) == oldTradeItemPreview.getDate().get(Calendar.DAY_OF_MONTH) ) {
            
            return oldTradeItemPreview.getNumber();
        }
        
        Buy buy = new Buy();
        
        int result = buy.getMaxBuyNumber(date);
        return result + 1; // minimální èíslo je jedna
    }
    
    /**
     * Zjistí naposledy použité ID v transakci
     */
    int getLastId(Statement stm) throws SQLException{
    
        // zjisti naposledy použité ID
        ResultSet rs = stm.executeQuery("SELECT last_insert_id()");
        
        rs.next();
        int result = rs.getInt(1);
        rs.close();
        return result;
    }
    
    /**
     * Zapíše jednotlivé položky pøíjemky do databáze.
     * Zároveò aktualizuje stav zboží na skladì
     */
    private void writeItemsToDatabase(int id) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                    "INSERT INTO " + BUY_NAME + " (id_buy_listing, goods_id, name, dph, nc, quantity, unit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
        Store store = new Store(); // vytvoø tøídu pro práci ze skladem
        // zjistí jaké ID bylo pøiøazeno pøíjemce
        
        // Projdi všechnyu položky pøíjemky a zapiš je do databáze
        // a zaroven aktualizuj mnozstvi zbozi na sklade
        for (TradeItem i: items.keySet()) {
            // zapiš položku pøíjemky 
            pstm.setInt(1, id);
            pstm.setString(2, i.getGoodsId());
            pstm.setString(3, i.getName());
            pstm.setInt(4, i.getDph());
            pstm.setDouble(5,  (new BigDecimal(i.getPrice())).divide(Store.CENT).doubleValue() );
            pstm.setDouble(6, i.getQuantity());
            pstm.setString(7, i.getUnit());
            pstm.executeUpdate();
            
            // Aktualizuj stav zboží na skladì
            Goods goods = store.getGoodsByID(i.getGoodsId(), true);
            BigDecimal newQuantity = new BigDecimal(goods.getQuantity()).add( new BigDecimal(i.getQuantity()) ); 
            
            store.editQuantity(i.getGoodsId(), newQuantity.doubleValue());

            ItemAttributes itemAttr = items.get(i);
            // zkontroluj, zda se u zboží zmìnily ceny 
            if (itemAttr.getComputedNc() != goods.getNc()
                || itemAttr.getNewPcA() != goods.getPcA()
                || itemAttr.getNewPcB() != goods.getPcB()
                || itemAttr.getNewPcC() != goods.getPcC()
                || itemAttr.getNewPcD() != goods.getPcD()
                ) {
                // Aktualizuj ceny na skladì
                store.editPrice(i.getGoodsId(), 
                        itemAttr.getComputedNc(),
                        itemAttr.getNewPcA(),
                        itemAttr.getNewPcB(),
                        itemAttr.getNewPcC(),
                        itemAttr.getNewPcD()
                        );
            }
            
        }        
        pstm.close();
    }
    
    /**
     *  Provede Storno pøíjemky, která byla døíve provedena.
     *  Aktualizuje stav zboží na skladì.
     *  Volání má smysl pouze, když byl volán konstruktor
     *  <code>DoBuy(Client client, TradeItemPreview oldTradeItemPreview)</code>
     *  jinak není co stornovat
     * @throws java.sql.SQLException Vyvolá, jestliže dojde k chybì pøi práci s databází
     */
    public void storno() throws SQLException {
        
        // Jestliže není žádná pøedchozí pøíjemka, není co stornovat.
        if (oldTradeItemPreview == null) {
            return;
        }
        
        try {
            DatabaseAccess.setAutoCommit(false);
            
            Store store = new Store();
            Buy buy = new Buy();
            ArrayList<TradeItem> itemsList = buy.getAllBuyItem(oldTradeItemPreview);

            // Projdi všechny položky pøíjemky
            for (TradeItem i: itemsList) {
                // Odeèti od zboží množství které je na pøíjemce
                BigDecimal oldQuantity = new BigDecimal(store.getGoodsByID(i.getGoodsId(), true).getQuantity());
                BigDecimal newQuantity = oldQuantity.subtract( new BigDecimal(i.getQuantity()) );
                store.editQuantity(i.getGoodsId(), newQuantity.doubleValue()); // aktualizuj cenu
            }

            buy.deleteBuy(oldTradeItemPreview); // Vymaž pøíjemku
            stornoCalled = true;
            
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        }
    }
    
    /**
     * Vrací, zda je období uzamèeno. V uzamèeném období není možno vytváøet žádné obchody
     * @return true jestliže je období uzamèeno
     *  false jestliže je období odemèeno
     */
    public boolean isStockingLock() throws SQLException {
        
        return (new Stockings()).isStockingLock(getDate());
    }
    
    /**
     *  Provede kontrolu, zda jsou vyplnìny potøebné údaje
     *  a pøíjemka by mohla být potvrzena
     * @throws java.lang.Exception vyvolá jestliže není možné potvrdit pøíjemku
     */
    public void check() throws Exception {
        
        if (suplier == null || suplier.getId() == -1) {
            throw new Exception("Není vyplnìno pole Dodavatel");
        }

        if (items.isEmpty()) {
            throw new Exception("Pøíjemka neobsahuje žádné položky zboží");
        }
        
        if (isStockingLock()){
            throw new Exception("Období je uzamèeno inventurou. <br>" +
                    "V uzamèeném období nemùžete mìnit stav na skladì");
        }
        
    }
            
    /**
     * Provede potvrzení opreace. 
     * Pøed zapsámí, provádí kontrolu,
     * zda jsou všechny potøebné údaje zadány.
     * Kontroluje zda bylo doplnìn alespoò jedno zboží. Zda je správnì zadán datum.
     * A zda je zadán dodavatel
     * 
     * 
     * @throws java.lang.Exception 
     * @return Vrací vytvoøenou pøíjemku
     */
    public TradeItemPreview confirm() throws Exception {

        
        try {
            DatabaseAccess.setAutoCommit(false);
            
            Statement stm = DatabaseAccess.getCurrentConnection().createStatement();
            PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                        "INSERT INTO " + BUY_LISTING_NAME + "" +
                        "(" +
                            "number, date, sup_id, total_nc_dph, total_dph, total_nc, reduction, author, user_id, use_price," +
                            "bill_number, is_cash" +
                        " ) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            check(); // zkontroluj, zda je možno potvrdit
            
            int number = getNextNumber();

            // Zapiš pøehled pøíjemky
            pstm.setInt(1, number);
            pstm.setTimestamp(2, new java.sql.Timestamp(date.getTimeInMillis()));
            pstm.setInt(3, getSuplier().getId());
            pstm.setDouble(4, getTotalPriceDPH().doubleValue() );
            pstm.setDouble(5, getTotalDPH().doubleValue() );
            pstm.setDouble(6, getTotalPrice().doubleValue() );
            pstm.setDouble(7, getReduction().doubleValue() );
            pstm.setString(8, client.getName());
            if (client.getUserId() == -1)  // Výchozí pøihlášení 
                pstm.setObject(9, null); // nastav na null
            else
                pstm.setInt(9, client.getUserId()); // nastav na skuteèné èíslo uživatele
            pstm.setInt(10, priceType);
            pstm.setString(11, billNumber);
            pstm.setBoolean(12, isCash);
            
            pstm.executeUpdate();
            
            int id = getLastId(stm);
            writeItemsToDatabase(id); // zapiš položky pøíjemky do databáze 
            
            stm.close();
            pstm.close();
            
            return new Buy().getBuy(id);
            
        } catch (SQLException e) {
            // Pøi chybì vše vrátíme
            DatabaseAccess.rollBack();
            throw e;
        }
        
    }
    
    /**
     * Zruší vytváøenou pøíjemku. Vrátí všechny pøípadné zmìny v databázi
     * cancel() je tøeba volat vždy, když nemá být pøíjemka potvrzena
     * @throws java.sql.SQLException Vyvolá, jestliže dojde k chybì pøi práci s databází 
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
     * Volání metod <CODE>confirm()</CODE> a <CODE>storno()</CODE> bude mít efekt až po volání této metody
     * @throws java.sql.SQLException Vyvolá, jestliže dojde k chybì pøi práci s databází
     */
    public void update() throws SQLException {
        
        DatabaseAccess.setAutoCommit(false);
        DatabaseAccess.commit();
        DatabaseAccess.setAutoCommit(true);
    }

    
    /**
     * Vrátí celkovou slevu na pøíjemce 
     * 1% - 100% 
     * @return Celkovou slevu. 
     */
    public BigDecimal getReduction() {
        return reduction.divide(Store.CENT);
    }

    /**
     * Vrací datum provedení pøíjemky
     * @return Datum provedení
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Vrací celkovou cenu DPH na pøíjemce v Kè
     * @return Celková cena v Kè. 
     */
    public BigDecimal getTotalDPH() {
        
        if ( getTotalPrice().doubleValue() > 0 && 
             (getSuplier() == null || getSuplier().isDph()) ) {
            
            // Výslednné DPH = DPH - DPH * sleva
            return totalDPH.subtract(
                    totalDPH.multiply(
                    getReduction().divide(Store.CENT)
                    )
                   ).divide(Store.CENT); // U plátce DPH vrátíme daò
        } else {
            return new BigDecimal(0); // U neplátce nastavíme daò na nula
        }
    }

    /**
     * Vrací celkovou nákupní cenu pøíjemky s DPH
     * Tj. cena za jednotlivé položky * jednotlivé množství + DPH - sleva
     * @return Celková nákupní cena v Kè. 
     */
    public BigDecimal getTotalPriceDPH() {
        // Vysledná cena s DPH = cena bez DPH + DPH
        BigDecimal result =  getTotalPrice().add( getTotalDPH() );
        
        // Jestliže se má zaokrouhlit
        if (roundScale != -1) {
            BigDecimal scale = new BigDecimal( roundScale);
         
            // vypoèti zaokrouhlení jako round(scale * X ) / scale;
            result = result.multiply(scale);
            result = result.setScale(0, RoundingMode.HALF_UP); // Zaokrouhluj jako v matematice
            result = result.divide(scale);
        }
        
        return result;
    }
    
    /**
     * Vrací celkovou nákupní cenu pøíjemky bez DPH
     * Tj. cena za jednotlivé položky * jednotlivé množství - sleva
     * @return Celková nákupní cena v Kè. 
     */
    public BigDecimal getTotalPrice() {
        // Vsledná cena = cena * sleva
        return totalPrice.subtract( 
                totalPrice.multiply(
                getReduction().divide(Store.CENT)) 
               ).divide(Store.CENT);
        
    }

    /**
     * Vrací dodavatele pøíjemky
     * @return dodavatel
     */
    public Suplier getSuplier() {
        return suplier;
    }

    /**
     * Nastaví u položky na jakém leží øádku v tabulce
     * @param tradeItem položka u které se má nastavit
     * @param row èíslo øádku, na kterém leží
     */
    public void setRowNumber(TradeItem tradeItem, int row) {
        ItemAttributes ia = items.get(tradeItem);
        ia.setRow(row);
        items.put(tradeItem, ia);
    }
    
    /**
     * Nastaví ceník, který se použije pro tuto položku pøíjemky
     * @param tradeItem položka pøíjemky
     * @param priceList ceník, který se má použít, nebo null, jestli se nemá ceník použít
     */
    public void setPriceList(TradeItem tradeItem, PriceList priceList) {
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setPriceList(priceList);
    }
    
    /**
     * Provede pøeèíslování øádku od zadaného až do konce, tak že posune hodnotu o -1
     * @param row èíslo øádku od kterého se má pøeèíslovat
     */
    public void clearRowNumber(int row) {
        for (ItemAttributes i: items.values()) {
            if (i.getRow() >= row) {
                i.setRow(i.getRow() - 1);
            }
        }
    }

    /**
     * Vrací pøíjemku ležící na pøíslušném øádku v tabulce.
     * èísla øádkù musí být nejprve asociovány metodou <code>setRowNumber()</code>
     * @param row 
     * @return 
     */
    public TradeItem getTradeItem(int row) {
        for (TradeItem i: items.keySet()) {
            if (items.get(i).getRow() == row) {
                return i;
            }
        }
        
        return null;
    }
    
    /**
     * Vrací atributy dané položky pøíjemky
     * @param tradeItem Položka, pro kterou zjistit atributy
     * @return Atributy pøíjemky
     */
    public ItemAttributes getTradeItemAttributes(TradeItem tradeItem) {
        return items.get(tradeItem);
    }
    
    /**
     * Vrací položku pøíjemky jako objekt typu Goods, který obsahuje
     * název množství a ceny podle položky pøíjemky
     * 
     * @param row øádek v tabulce, z kterého se má naèíst
     * @return vrací objket Goods, nebo null, jestliže n daném øádku nic není
     */
    public Goods getTradeItemAsGoods(int row) {
        TradeItem tradeItem = getTradeItem(row);

        if (tradeItem == null) {
            return null;
        }
        
        ItemAttributes itemAttr = items.get(tradeItem);
        
        Goods result = new Goods(
                tradeItem.getGoodsId(),
                tradeItem.getName(),
                0,
                tradeItem.getDph(),
                tradeItem.getUnit(),
                "",
                itemAttr.getNewNc(),
                itemAttr.getNewPcA(), 
                itemAttr.getNewPcB(),
                itemAttr.getNewPcC(), 
                itemAttr.getNewPcD(),                
                tradeItem.getQuantity()
                );
        
        return result;
    }
    
    
    /**
     * Nastaví použitelné množství zboží 
     * @param goodsId skladové èíslo zboží
     * @param quantity množství zboží, které je možno prodat
     */
    public void setAvailableQuantity(String goodsId, double quantity) {
        availableQuantity.put(goodsId, quantity);
    }
    
    /**
     * Vratí kolik zboží je k dispozici ve skladu
     * @param goodsId skladové èíslo zboží
     * @return množstí zboží k dispozici, jestliže údaj o zboží v seznamu není, vrací -1
     */
    public double getAvailableQuantity(String goodsId) {
        double result = -1;
        if (availableQuantity.get(goodsId) != null) {
            result = availableQuantity.get(goodsId);
        }
        
        return result;
    }
    
    /**
     * Vrací jednotlivé položky pøíjemky
     * @return Jednotlivé položky pøíjemky
     */
    public Set<TradeItem> getItems() {
        return new TreeSet<TradeItem>(items.keySet());
    }
    
    Client getClient() {
        return client;
    }

    boolean isStornoCalled() {
        return stornoCalled;
    }

    public TradeItemPreview getOldTradeItemPreview() {
        return oldTradeItemPreview;
    }

    void setStornoCalled(boolean stornoCalled) {
        this.stornoCalled = stornoCalled;
    }

    /**
     * Vrací název ceny, která se používá pro souèet
     * @return název ceny, která se používá pro souèet
     */
    public String getPriceName() {
        return DoBuy.getPriceName(priceType);
    }
    
    /**
     * Vrací název ceny, která se používá pro souèet
     * @param priceType index pøedstavující cenu. Jedná se o jednu z konstatn DoBuy.USE_...
     * @return název ceny, která se používá pro souèet
     */
    public static String getPriceName(int priceType) {
        switch (priceType) {
            case USE_NC_FOR_SUM :
                return Settings.getNcName();
            case USE_PCA_FOR_SUM :
                return Settings.getPcAName();
            case USE_PCB_FOR_SUM :
                return Settings.getPcBName();
            case USE_PCC_FOR_SUM :
                return Settings.getPcCName();
            case USE_PCD_FOR_SUM :
                return Settings.getPcDName();
            default :
                return "?";
        }
    }
    
    /**
     * Vrací, která cena se používá pro souèet
     * @return která cena se používá pro souèet
     */
    public int getUsePrice() {
        return priceType;
    }    


    Map<String, Double> getAvailableQuantityMap() {
        return availableQuantity;
    }

    void setOldTradeItemPreview(TradeItemPreview oldTradeItemPreview) {
        this.oldTradeItemPreview = oldTradeItemPreview;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public void setIsCash(boolean isCash) {
        this.isCash = isCash;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public boolean isIsCash() {
        return isCash;
    }

    public void setLastUsePrice(int lastUsePrice) {
        this.lastUsePrice = lastUsePrice;
    }
    
    
        
}
