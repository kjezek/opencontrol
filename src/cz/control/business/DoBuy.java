/*
 * DoBuy.java
 *
 * Vytvo�eno 3. listopad 2005, 19:59
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
 * Program Control - Skladov� syst�m
 *
 * T��da pro vytvo�en� p��jemky. T��da umo��uje nastavit ve�ker� pot�ebn� �daje o jedn�
 * p��jemce a n�sledn� j� zapsat do datab�ze 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 *
 */

public class DoBuy {
    
    private static final String BUY_NAME = DatabaseAccess.BUY_TABLE_NAME; // ulo� n�zev datab�ze
    private static final String BUY_LISTING_NAME = DatabaseAccess.BUY_LISITNG_TABLE_NAME; // ulo� n�zev datab�ze
    static final String GOODS_NAME = DatabaseAccess.GOODS_TABLE_NAME; // ulo� n�zev datab�ze
    
    /**
     * Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t N�kupn� cena (NC)
     */
    public static final int USE_NC_FOR_SUM = 0;
    /**
     * Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t prvn� Prodejn� cena (PC A)
     */
    public static final int USE_PCA_FOR_SUM = 1;
    /**
     * Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t druh� Prodejn� cena (PC B)
     */
    public static final int USE_PCB_FOR_SUM = 2;
    /**
     * Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t t�et� Prodejn� cena (PC C)
     */
    public static final int USE_PCC_FOR_SUM = 3;
    /**
     * Konstoanta ��kaj�c�, �e se m� pro sou�et ceny na p��jemce pou��t �tvrt� Prodejn� cena (PC D)
     */
    public static final int USE_PCD_FOR_SUM = 4;
    
    /**
     *  Konstanta ur�uj�c� zaokrouhlen�. Ur�uje, �e se m� zaokrouhlovat na pades�tn�ky
     */
    public static final double ROUND_SCALE_TO_050 = 2.0;
    /**
     *  Konstanta ur�uj�c� zaokrouhlen�. Ur�uje, �e se m� zaokrouhlovat cel� koruny
     */
    public static final double ROUND_SCALE_TO_100 = 1.0;
    /**
     *  Konstanta ur�uj�c� zaokrouhlen�. Ur�uje, �e se NEBUDE zaokrouhlovat
     */
    public static final double ROUND_SCALE_UNNECESSARY = -1;
    
    
    private BigDecimal reduction = BigDecimal.ZERO; // celkov� sleva 
    private Calendar date; // datum a �as proveden� p��jemky 
    private BigDecimal totalDPH = BigDecimal.ZERO; // celkov� cena DPH v K� 
    private BigDecimal totalPrice = BigDecimal.ZERO;// celkov� cena zbo�� na p��jemce (sou�et v�ech cen * mno�stv�)
    private Suplier suplier = null;
    
    int tradeId = 0; // ��sluje polo�ky p��jemky 
    private boolean cancelCalled = false;
    private boolean stornoCalled = false; // indikuje, zda byla vstupn� p��jemka stornov�na
    private int priceType = USE_NC_FOR_SUM; // typ ceny, kter� se m� pou��t
    private String billNumber = ""; // ��slo faktury, nebo dodac�ho listu
    private boolean isCash = false; // true pokud bylo placeno hotov�

    private double roundScale = -1;
    
    private Client client = null; // Ukazatel na u�ivatele, kter� otev�el p��jemku 
    
    private TradeItemPreview oldTradeItemPreview = null; 
    
    /* Polo�ky p��jemky */
    private Map<TradeItem, ItemAttributes> items = new HashMap<TradeItem, ItemAttributes>();
    
    /* Mapa uchov�v�j�c� aktu�ln� mno�stv� zbo�� na sklad� */
    private Map<String, Double> availableQuantity = new HashMap<String, Double>();
    
    /** Typ ceny, kter� se pou�ije */
    private int lastUsePrice = USE_NC_FOR_SUM;
    
            
    /**
     * Vytvo�� nov� objekt DoBuy.
     * Tento konstruktor je vhodn� pro vytv��en� nov� p��jemky
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
     * Vytvo�� nov� objekt DoBuy. Pou�ije hodnoty z d��ve vytvo�en� p��jemky. 
     * Tento konstruktor je vhodn� pou��t pro bezpe�n� stornov�n� p��jemky, 
     * nebo pro editaci p��jemky
     * @param client klient, kter� otev�el p��jemku
     * @param oldTradeItemPreview D��ve vytvo�en� p��jemka
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
     *  Nastav� atributy p��jemky podle dan� vstupn� p��jemky
     */
    private void setTradeItemAttributes() throws SQLException {
        setDate( oldTradeItemPreview.getDate() );
        setReduction( new BigDecimal(oldTradeItemPreview.getReduction()).intValue() );
        setSuplier( new Supliers().getSuplierByID(oldTradeItemPreview.getId()) );
        billNumber = oldTradeItemPreview.getBillNumber();
        isCash = oldTradeItemPreview.isCash();
        
        // Nastav jednotliv� polo�ky
        Buy buy = new Buy();
// Ned� se pou��t, nebo� je t�eba i aktualizovat ceny        
//        items = buy.getAllBuyItem(oldTradeItemPreview);
        
        Store store = new Store();
        
        for (TradeItem i: buy.getAllBuyItem(oldTradeItemPreview)) {
            // Na�ti a ulo� do mapy pou�iteln� mno�stv� zbo��
            Goods goods = store.getGoodsByID(i.getGoodsId());
            setAvailableQuantity(goods.getGoodsID(), goods.getQuantity());
        }
        
        // Aktualizuj ceny
        for (TradeItem i: buy.getAllBuyItem(oldTradeItemPreview)) {
            Goods goods = store.getGoodsByID(i.getGoodsId());
            // Sestav nov� zbo�� tak, aby obsahovali NC z p��jemky a PC ze zbo�� na sklad�
            Goods newGoods = new Goods(
                    i.getGoodsId(), i.getName(), goods.getType(), i.getDph(), i.getUnit(), goods.getEan(),
                    i.getPrice(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(),
                    i.getQuantity());
            // D�le�it�, ID mus� souhlasit ze starou p��jemkou
            tradeId = i.getTradeId();
            lastUsePrice = i.getUsePrice();
            addTradeItem(newGoods, i.getQuantity());
            
            //Nakonec nastav vypo�tenou cenu, jako tu, kter� je uvedena u skladov� karty na sklad�
            //tedy byla vypo�tena v minul�m p��jmu
            ItemAttributes itemAttr = getTradeItemAttributes(i);
            itemAttr.setInputGoods(goods); //Na vstupu je zbo�� ze skladu
            itemAttr.setComputedNc(goods.getNc());
            itemAttr.setComputePrices(-1); //Z�m�rn� nastav "nesmysl" aby nedo�lo k p�epo�ten� hned po vlo�en�
        }
    }
    
    /**
     * Nastavuje, �e se m� kone�n� cena zaokrouhlit
     * @param roundScale z�klad zaokrouhlen�. Pou�ijde konstanty z DoBuy.java ROUND_SCALE_...
     */
    public void makeRound(double roundScale) {
        this.roundScale = roundScale;
    }
    

    
    /**
     * Vyma�e z p��jemky jeden z�znam
     * 
     * @param tradeItem Z�znam, kter� m� b�t vymaz�n
     * @return ��dek z kter�ho bylo vymaz�no
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
     * Zm�n� polo�ku p��jemku
     * @param oldTradeItem star� polo�ka, kter� bude nahrazena
     * @param newTradeItem nov� polo�ka, kterou bude nahrazeno
     * @return true jestl�e byla polo�ka zm�n�na. False vrac� jestli�e seznam ji� obsahuje 
     * polo�ku shodnou s newTradeItem, nebo polo�ka oldTradeItem v seznamu neexistuje 
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
     * Nastav� celkou slevu na zbo�� 
     * @param reduction ��slo p�edstavuj�c� cenu. 
     * Posledn� dv� ��slice jsou pova�ov�ny za hal��e
     */
    public void setReduction(int reduction) {
        this.reduction = new BigDecimal(reduction);
   } 
    
    /**
     * Nastavi datum a �as proveden� p��jemky
     * @param date Objekt p�edstavujc� datum a �as
     */
    public void setDate(Calendar date) {
    
        this.date = new GregorianCalendar(); // Zjisti aktu�ln� kalend�� kv�li �asu
        
        this.date.set(Calendar.YEAR, date.get(Calendar.YEAR)); // nastav rok
        this.date.set(Calendar.MONTH, date.get(Calendar.MONTH)); // nastav m�s�c
        this.date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH)); // nastav den
    }
    
    /**
     * Nastav� dodavatele, kter� dodal zbo�� uveden� v p��jemce
     * @param suplier Odkaz na dodavatele, kter� byl p�edem ulo�en do datab�ze 
     */
    public void setSuplier(Suplier suplier) {
    
        this.suplier = suplier;
    }
    
    /**
     * Vypo�te n�kupn� cenu podle nov� zadan�
     * @param tradeItem polo�ka p��jemky pro kterou po��tat
     * @param newNc nov� n�kupn� cena
     */
    public void computeNC(TradeItem tradeItem, int newNc) {
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setNewNc(newNc);
        
        Goods inputGoods = itemAttr.getInputGoods();
        // Podle toho jak� cena se m� pou�it, nastav p��slu�n� hodnoty
        switch (itemAttr.getComputePrices()) {
            case ItemAttributes.LAST_PRICE : // Pou��t posledn� cenu
                itemAttr.setComputedNc(itemAttr.getNewNc());
                break; 
            case ItemAttributes.EXPENSIVE_PRICE :  //Pou��t dra��� cenu
                int price = (inputGoods.getNc() > itemAttr.getNewNc()) ? inputGoods.getNc() : itemAttr.getNewNc();
                itemAttr.setComputedNc(price);
                break; 
            case ItemAttributes.CHEAPER_PRICE :  //Pou��t levn�j�� cenu
                price = (inputGoods.getNc() < itemAttr.getNewNc()) ? inputGoods.getNc() : itemAttr.getNewNc();
                itemAttr.setComputedNc(price);
                break; 
            case ItemAttributes.AVERAGE_PRICE : //Pou��t pr�m�rnou cenu
                // Po��t� v�en� pr�m�r (v�hy jsou mno�stv� na sklad� a mno�stv�, kter� se nakupuje
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
            case ItemAttributes.OLD_PRICE : //Pou��t starou cenu (ta kter� je u zbo�� na sklad�)
                price = itemAttr.getInputGoods().getNc();
                itemAttr.setComputedNc(price);
                break;
            default:
                break;
        }
    
    }
    
    /**
     * Nastav� nov� prodejn� ceny pro polo�ku p��jemky
     * @param tradeItem polo�ka p��jemky, pro kterou se m� nastavit
     * @param pcA vypo�ten� prodejn� cena A
     * @param pcB vypo�ten� prodejn� cena B
     * @param pcC vypo�ten� prodejn� cena C 
     * @param pcD vypo�ten� prodejn� cena D
     * @param usePrice typ ceny, kter� se pou�ije
     */
    public void setNewPcPrices(TradeItem tradeItem, 
            int pcA, int pcB, int pcC, int pcD) {
    
        // Nastav nov� hodnoty
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setNewPcA(pcA);
        itemAttr.setNewPcB(pcB);
        itemAttr.setNewPcC(pcC);
        itemAttr.setNewPcD(pcD);
        
        // Nastav novou prodejn� cenu
        editPrice(tradeItem, getPrice(
                getTradeItemAsGoods(itemAttr.getRow()),
                lastUsePrice), lastUsePrice);
    }
    
    /**
     * Nastav� nov� prodejn� ceny pro polo�ku p��jemky
     * @param tradeItem polo�ka p��jemky, pro kterou se m� nastavit
     * @param nc vypo�ten� n�kupn� cena
     * @param pcA vypo�ten� prodejn� cena A
     * @param pcB vypo�ten� prodejn� cena B
     * @param pcC vypo�ten� prodejn� cena C 
     * @param pcD vypo�ten� prodejn� cena D
     */
    public void setNewPrices(TradeItem tradeItem, 
            int nc, int pcA, int pcB, int pcC, int pcD,
            int usePrice) {
    
        // Nastav nov� hodnoty
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setNewNc(nc);
        itemAttr.setNewPcA(pcA);
        itemAttr.setNewPcB(pcB);
        itemAttr.setNewPcC(pcC);
        itemAttr.setNewPcD(pcD);

        this.lastUsePrice = usePrice;
        
        // Nastav novou prodejn� cenu
        editPrice(tradeItem, getPrice(
                getTradeItemAsGoods(itemAttr.getRow()),
                usePrice), usePrice);
    }    
    
    /**
     * Nastav� nov� typ ceny
     * @param tradeItem
     * @param usePrice 
     */
    public TradeItem setNewUsePrice(TradeItem tradeItem, int usePrice) {
        ItemAttributes itemAttr = items.get(tradeItem);
        this.lastUsePrice = usePrice;
        // Nastav novou prodejn� cenu
        return editPrice(tradeItem, getPrice(
                getTradeItemAsGoods(itemAttr.getRow()),
                usePrice), usePrice);        
    }
    
    /**
     * Nastavuje, jak se maj� po��tat prodejn� ceny
     * z n�kupn�ch cen
     * @param tradeItem Polo�ka p��jemky, pro kterou se m� pou��t nov� v�po�et
     * @param computePrice ud�v� jak se m� po��tat prodejn� cena s n�kupn� ceny. 
     * Pro vstup pou�ijte konstantu
     * ItemAttributes.AVERAGE_PRICE - pro zpr�m�rov�n� n�kupn� ceny
     * ItemAttributes.LAST_PRICE - pro pou�it� posledn� n�kupn� ceny
     * ItemAttributes.EXPENSIVE_PRICE - pro pou�it� dra��� n�kupn� ceny
     */
    public void setComputeNCPrice(TradeItem tradeItem, int computePrice) {
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setComputePrices(computePrice);
    }
    
    /**
     * Nastav� cenu, kter� bude uvedena na p��jemce u zbo��
     * 
     * @param price nov� cena
     * @param tradeItem Plo�ka u kter� m� b�t zm�n�na cena
     * @param typ ceny,  kter� se pou�ije
     * @return Skladovou polo�ku se zm�n�nou cenou, nebo null, jestli�e do�lo k chyb�
     */
    private TradeItem editPrice(TradeItem tradeItem, int price, int usePrice) {
        
        if (tradeItem == null) {
            return null;
        }
        
        // Vytvo� nov� objekt podle star�ho, ale ze zm�n�nou cenou
        TradeItem newTradeItem = new TradeItem(tradeItem.getTradeId(), 
                tradeItem.getTradeIdListing(), tradeItem.getGoodsId(), tradeItem.getName(),
                tradeItem.getDph(), price, tradeItem.getQuantity(), tradeItem.getUnit(),
                usePrice);
        
        editTradeItem(tradeItem, newTradeItem); // nahra�
        
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
     * Nastav� mno�stv� zbo�� u jedn� polo�ky pr�jemky, kolik se bude nakupovat
     * 
     * @param tradeItem Polo�ka p��jemky
     * @param quantity Mno�stv�, kolik se nakoup�
     * @return P�i �sp�chu vrac� true, false - jestli�e nenn� na sklad� dostatek zbo��
     */
    public boolean setQuantity(TradeItem tradeItem, double quantity) {
        
        if (tradeItem == null) {
            return false;
        }
        
        // Vytvo� nov� objekt podle star�ho, ale ze zm�n�m mno�stv�m 
        TradeItem newTradeItem = new TradeItem(tradeItem.getTradeId(), 
                tradeItem.getTradeIdListing(), tradeItem.getGoodsId(), tradeItem.getName(),
                tradeItem.getDph(), tradeItem.getPrice(), quantity, tradeItem.getUnit(),
                tradeItem.getUsePrice());

        // aktualizuj cenu
        totalPrice = totalPrice.add(
                ( new BigDecimal(tradeItem.getPrice())).multiply( new BigDecimal(quantity - tradeItem.getQuantity()) )
                );
        
        //aktualizuj da�
        totalDPH = totalDPH.add( 
                ( new BigDecimal(tradeItem.getDph())).divide(Store.CENT).multiply( new BigDecimal(tradeItem.getPrice())).multiply( new BigDecimal(quantity - tradeItem.getQuantity()) )
                );

        editTradeItem(tradeItem, newTradeItem); // nahra�
        
        BigDecimal oldQuantity = new BigDecimal(getAvailableQuantity(tradeItem.getGoodsId()));
        setAvailableQuantity(tradeItem.getGoodsId(), oldQuantity.add( new BigDecimal(tradeItem.getQuantity())).subtract( new BigDecimal(quantity)).doubleValue() );
        return true;
    }
    
    /**
     * Dopln� do p��jemky dal�� polo�ku
     * @param quantity Mno�stv� zbo��, kter� se m� nakoupit
     * @param goods Zbo��, kter� m� b�t dopln�no
     * @return Vrac� true jestli�e do�lo k chyb�. Nebo false, jestli�e je chybn� zadan� zbo��,
     *  nebo je zadan� z�porn� mno�stv�, nebo ji� p��jmka takov� zbo�� obsahuje 
     */
    public TradeItem addTradeItem(Goods goods, double quantity) {
        
        if (goods == null || goods.getGoodsID() == null || quantity < 0) {
            return null;
        }
        
        // Vytvo� novou polo�ku p��jemku 
        // hodnota id (tradeId, 0) slou�� jenom do�asn� a bude se m�nit p�i zapisov�n� do datab�ze
        TradeItem newItem = new TradeItem(tradeId++, 0, goods.getGoodsID(), goods.getName(),
                goods.getDph(), getPrice(goods, lastUsePrice), quantity, goods.getUnit(),
                lastUsePrice);
        
        // Nedovol duplicitn� polo�ky 
        if (items.containsKey(newItem)) {
            return null;
        }
        
        // p�i�ti novou cenu
        totalPrice = totalPrice.add(
                (new BigDecimal(getPrice(goods, lastUsePrice))).multiply( new BigDecimal(quantity))
                );
        totalDPH = totalDPH.add(
                ( new BigDecimal(newItem.getDph()) ).divide(Store.CENT).multiply( new BigDecimal( newItem.getPrice()) ).multiply( new BigDecimal(quantity) )
                );


        // Nastav atributy nov�ho zbo��
        setNewGoodsAttributes(goods, newItem);
        
        double oldQuantity = getAvailableQuantity(goods.getGoodsID());
        //jestli�e polo�ka v seznamu neexistuje
        if (oldQuantity == -1) {
            // Dopl� polo�ku
            setAvailableQuantity(goods.getGoodsID(), new BigDecimal(goods.getQuantity()).subtract( new BigDecimal(quantity)).doubleValue() );
        } else {
            //p�i vlo�en�m stejn� polo�ky sni� pou�iteln� mno�stv�
            setAvailableQuantity(goods.getGoodsID(), new BigDecimal(oldQuantity).subtract( new BigDecimal(quantity)).doubleValue() );
        }

        return newItem;
    }
    
    /**
     *  Nastav� atributy nov� vkl�dan�ho zbo��. Jestli�e u� v�ak v seznamu je stejn�
     *  zbo�� nastav� vlastnosti podle n�j. To proto, aby u�ivatel nemusel nap��klad 
     *  neust�le zad�vat novou n�kupn� cenu p�i zad�v�n� v�ce polo�ek
     */
    private void setNewGoodsAttributes(Goods goods, TradeItem newItem) {
        Goods newGoods = goods;

        //Nastav ceny ze zbo��
        ItemAttributes itAttr = new ItemAttributes();
        itAttr.setNewNc(newGoods.getNc());
        itAttr.setComputedNc(newGoods.getNc()); // Proza��tek je vypo�ten� cena stejn� jako p�vodn�
        
        //Jestli�e u� stejn� zbo�� v p��jemce existuje, nastav atributy podle n�j,
        //jinak nastav atributy podle nov� vkl�dan�ho zbo��
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
     *  Vrac� n�kupn� cenu zbo�� 
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
     * Vyhled� dal�� ��slo pou�iteln� pro p��jemku podle datumu proveden� p��jemky 
     */
    private int getNextNumber() throws SQLException{
      
        // Jestli�e existuje star� p��jemka, kter� byla stornov�na
        // a m� se vytvo�it p��jemka stejn�ho data, p�evezmi jej� ��slo
        // nebo� se jedn� o n�hradu star� p��jemky
        if (stornoCalled && oldTradeItemPreview != null &&
                getDate().get(Calendar.YEAR) == oldTradeItemPreview.getDate().get(Calendar.YEAR) &&
                getDate().get(Calendar.MONTH) == oldTradeItemPreview.getDate().get(Calendar.MONTH) &&
                getDate().get(Calendar.DAY_OF_MONTH) == oldTradeItemPreview.getDate().get(Calendar.DAY_OF_MONTH) ) {
            
            return oldTradeItemPreview.getNumber();
        }
        
        Buy buy = new Buy();
        
        int result = buy.getMaxBuyNumber(date);
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
     * Zap�e jednotliv� polo�ky p��jemky do datab�ze.
     * Z�rove� aktualizuje stav zbo�� na sklad�
     */
    private void writeItemsToDatabase(int id) throws SQLException {
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(
                    "INSERT INTO " + BUY_NAME + " (id_buy_listing, goods_id, name, dph, nc, quantity, unit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
        Store store = new Store(); // vytvo� t��du pro pr�ci ze skladem
        // zjist� jak� ID bylo p�i�azeno p��jemce
        
        // Projdi v�echnyu polo�ky p��jemky a zapi� je do datab�ze
        // a zaroven aktualizuj mnozstvi zbozi na sklade
        for (TradeItem i: items.keySet()) {
            // zapi� polo�ku p��jemky 
            pstm.setInt(1, id);
            pstm.setString(2, i.getGoodsId());
            pstm.setString(3, i.getName());
            pstm.setInt(4, i.getDph());
            pstm.setDouble(5,  (new BigDecimal(i.getPrice())).divide(Store.CENT).doubleValue() );
            pstm.setDouble(6, i.getQuantity());
            pstm.setString(7, i.getUnit());
            pstm.executeUpdate();
            
            // Aktualizuj stav zbo�� na sklad�
            Goods goods = store.getGoodsByID(i.getGoodsId(), true);
            BigDecimal newQuantity = new BigDecimal(goods.getQuantity()).add( new BigDecimal(i.getQuantity()) ); 
            
            store.editQuantity(i.getGoodsId(), newQuantity.doubleValue());

            ItemAttributes itemAttr = items.get(i);
            // zkontroluj, zda se u zbo�� zm�nily ceny 
            if (itemAttr.getComputedNc() != goods.getNc()
                || itemAttr.getNewPcA() != goods.getPcA()
                || itemAttr.getNewPcB() != goods.getPcB()
                || itemAttr.getNewPcC() != goods.getPcC()
                || itemAttr.getNewPcD() != goods.getPcD()
                ) {
                // Aktualizuj ceny na sklad�
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
     *  Provede Storno p��jemky, kter� byla d��ve provedena.
     *  Aktualizuje stav zbo�� na sklad�.
     *  Vol�n� m� smysl pouze, kdy� byl vol�n konstruktor
     *  <code>DoBuy(Client client, TradeItemPreview oldTradeItemPreview)</code>
     *  jinak nen� co stornovat
     * @throws java.sql.SQLException Vyvol�, jestli�e dojde k chyb� p�i pr�ci s datab�z�
     */
    public void storno() throws SQLException {
        
        // Jestli�e nen� ��dn� p�edchoz� p��jemka, nen� co stornovat.
        if (oldTradeItemPreview == null) {
            return;
        }
        
        try {
            DatabaseAccess.setAutoCommit(false);
            
            Store store = new Store();
            Buy buy = new Buy();
            ArrayList<TradeItem> itemsList = buy.getAllBuyItem(oldTradeItemPreview);

            // Projdi v�echny polo�ky p��jemky
            for (TradeItem i: itemsList) {
                // Ode�ti od zbo�� mno�stv� kter� je na p��jemce
                BigDecimal oldQuantity = new BigDecimal(store.getGoodsByID(i.getGoodsId(), true).getQuantity());
                BigDecimal newQuantity = oldQuantity.subtract( new BigDecimal(i.getQuantity()) );
                store.editQuantity(i.getGoodsId(), newQuantity.doubleValue()); // aktualizuj cenu
            }

            buy.deleteBuy(oldTradeItemPreview); // Vyma� p��jemku
            stornoCalled = true;
            
        } catch (SQLException e) {
            DatabaseAccess.rollBack();
            throw e;
        }
    }
    
    /**
     * Vrac�, zda je obdob� uzam�eno. V uzam�en�m obdob� nen� mo�no vytv��et ��dn� obchody
     * @return true jestli�e je obdob� uzam�eno
     *  false jestli�e je obdob� odem�eno
     */
    public boolean isStockingLock() throws SQLException {
        
        return (new Stockings()).isStockingLock(getDate());
    }
    
    /**
     *  Provede kontrolu, zda jsou vypln�ny pot�ebn� �daje
     *  a p��jemka by mohla b�t potvrzena
     * @throws java.lang.Exception vyvol� jestli�e nen� mo�n� potvrdit p��jemku
     */
    public void check() throws Exception {
        
        if (suplier == null || suplier.getId() == -1) {
            throw new Exception("Nen� vypln�no pole Dodavatel");
        }

        if (items.isEmpty()) {
            throw new Exception("P��jemka neobsahuje ��dn� polo�ky zbo��");
        }
        
        if (isStockingLock()){
            throw new Exception("Obdob� je uzam�eno inventurou. <br>" +
                    "V uzam�en�m obdob� nem��ete m�nit stav na sklad�");
        }
        
    }
            
    /**
     * Provede potvrzen� opreace. 
     * P�ed zaps�m�, prov�d� kontrolu,
     * zda jsou v�echny pot�ebn� �daje zad�ny.
     * Kontroluje zda bylo dopln�n alespo� jedno zbo��. Zda je spr�vn� zad�n datum.
     * A zda je zad�n dodavatel
     * 
     * 
     * @throws java.lang.Exception 
     * @return Vrac� vytvo�enou p��jemku
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
            
            check(); // zkontroluj, zda je mo�no potvrdit
            
            int number = getNextNumber();

            // Zapi� p�ehled p��jemky
            pstm.setInt(1, number);
            pstm.setTimestamp(2, new java.sql.Timestamp(date.getTimeInMillis()));
            pstm.setInt(3, getSuplier().getId());
            pstm.setDouble(4, getTotalPriceDPH().doubleValue() );
            pstm.setDouble(5, getTotalDPH().doubleValue() );
            pstm.setDouble(6, getTotalPrice().doubleValue() );
            pstm.setDouble(7, getReduction().doubleValue() );
            pstm.setString(8, client.getName());
            if (client.getUserId() == -1)  // V�choz� p�ihl�en� 
                pstm.setObject(9, null); // nastav na null
            else
                pstm.setInt(9, client.getUserId()); // nastav na skute�n� ��slo u�ivatele
            pstm.setInt(10, priceType);
            pstm.setString(11, billNumber);
            pstm.setBoolean(12, isCash);
            
            pstm.executeUpdate();
            
            int id = getLastId(stm);
            writeItemsToDatabase(id); // zapi� polo�ky p��jemky do datab�ze 
            
            stm.close();
            pstm.close();
            
            return new Buy().getBuy(id);
            
        } catch (SQLException e) {
            // P�i chyb� v�e vr�t�me
            DatabaseAccess.rollBack();
            throw e;
        }
        
    }
    
    /**
     * Zru�� vytv��enou p��jemku. Vr�t� v�echny p��padn� zm�ny v datab�zi
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
     * Vr�t� celkovou slevu na p��jemce 
     * 1% - 100% 
     * @return Celkovou slevu. 
     */
    public BigDecimal getReduction() {
        return reduction.divide(Store.CENT);
    }

    /**
     * Vrac� datum proveden� p��jemky
     * @return Datum proveden�
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Vrac� celkovou cenu DPH na p��jemce v K�
     * @return Celkov� cena v K�. 
     */
    public BigDecimal getTotalDPH() {
        
        if ( getTotalPrice().doubleValue() > 0 && 
             (getSuplier() == null || getSuplier().isDph()) ) {
            
            // V�slednn� DPH = DPH - DPH * sleva
            return totalDPH.subtract(
                    totalDPH.multiply(
                    getReduction().divide(Store.CENT)
                    )
                   ).divide(Store.CENT); // U pl�tce DPH vr�t�me da�
        } else {
            return new BigDecimal(0); // U nepl�tce nastav�me da� na nula
        }
    }

    /**
     * Vrac� celkovou n�kupn� cenu p��jemky s DPH
     * Tj. cena za jednotliv� polo�ky * jednotliv� mno�stv� + DPH - sleva
     * @return Celkov� n�kupn� cena v K�. 
     */
    public BigDecimal getTotalPriceDPH() {
        // Vysledn� cena s DPH = cena bez DPH + DPH
        BigDecimal result =  getTotalPrice().add( getTotalDPH() );
        
        // Jestli�e se m� zaokrouhlit
        if (roundScale != -1) {
            BigDecimal scale = new BigDecimal( roundScale);
         
            // vypo�ti zaokrouhlen� jako round(scale * X ) / scale;
            result = result.multiply(scale);
            result = result.setScale(0, RoundingMode.HALF_UP); // Zaokrouhluj jako v matematice
            result = result.divide(scale);
        }
        
        return result;
    }
    
    /**
     * Vrac� celkovou n�kupn� cenu p��jemky bez DPH
     * Tj. cena za jednotliv� polo�ky * jednotliv� mno�stv� - sleva
     * @return Celkov� n�kupn� cena v K�. 
     */
    public BigDecimal getTotalPrice() {
        // Vsledn� cena = cena * sleva
        return totalPrice.subtract( 
                totalPrice.multiply(
                getReduction().divide(Store.CENT)) 
               ).divide(Store.CENT);
        
    }

    /**
     * Vrac� dodavatele p��jemky
     * @return dodavatel
     */
    public Suplier getSuplier() {
        return suplier;
    }

    /**
     * Nastav� u polo�ky na jak�m le�� ��dku v tabulce
     * @param tradeItem polo�ka u kter� se m� nastavit
     * @param row ��slo ��dku, na kter�m le��
     */
    public void setRowNumber(TradeItem tradeItem, int row) {
        ItemAttributes ia = items.get(tradeItem);
        ia.setRow(row);
        items.put(tradeItem, ia);
    }
    
    /**
     * Nastav� cen�k, kter� se pou�ije pro tuto polo�ku p��jemky
     * @param tradeItem polo�ka p��jemky
     * @param priceList cen�k, kter� se m� pou��t, nebo null, jestli se nem� cen�k pou��t
     */
    public void setPriceList(TradeItem tradeItem, PriceList priceList) {
        ItemAttributes itemAttr = items.get(tradeItem);
        itemAttr.setPriceList(priceList);
    }
    
    /**
     * Provede p�e��slov�n� ��dku od zadan�ho a� do konce, tak �e posune hodnotu o -1
     * @param row ��slo ��dku od kter�ho se m� p�e��slovat
     */
    public void clearRowNumber(int row) {
        for (ItemAttributes i: items.values()) {
            if (i.getRow() >= row) {
                i.setRow(i.getRow() - 1);
            }
        }
    }

    /**
     * Vrac� p��jemku le��c� na p��slu�n�m ��dku v tabulce.
     * ��sla ��dk� mus� b�t nejprve asociov�ny metodou <code>setRowNumber()</code>
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
     * Vrac� atributy dan� polo�ky p��jemky
     * @param tradeItem Polo�ka, pro kterou zjistit atributy
     * @return Atributy p��jemky
     */
    public ItemAttributes getTradeItemAttributes(TradeItem tradeItem) {
        return items.get(tradeItem);
    }
    
    /**
     * Vrac� polo�ku p��jemky jako objekt typu Goods, kter� obsahuje
     * n�zev mno�stv� a ceny podle polo�ky p��jemky
     * 
     * @param row ��dek v tabulce, z kter�ho se m� na��st
     * @return vrac� objket Goods, nebo null, jestli�e n dan�m ��dku nic nen�
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
     * Nastav� pou�iteln� mno�stv� zbo�� 
     * @param goodsId skladov� ��slo zbo��
     * @param quantity mno�stv� zbo��, kter� je mo�no prodat
     */
    public void setAvailableQuantity(String goodsId, double quantity) {
        availableQuantity.put(goodsId, quantity);
    }
    
    /**
     * Vrat� kolik zbo�� je k dispozici ve skladu
     * @param goodsId skladov� ��slo zbo��
     * @return mno�st� zbo�� k dispozici, jestli�e �daj o zbo�� v seznamu nen�, vrac� -1
     */
    public double getAvailableQuantity(String goodsId) {
        double result = -1;
        if (availableQuantity.get(goodsId) != null) {
            result = availableQuantity.get(goodsId);
        }
        
        return result;
    }
    
    /**
     * Vrac� jednotliv� polo�ky p��jemky
     * @return Jednotliv� polo�ky p��jemky
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
     * Vrac� n�zev ceny, kter� se pou��v� pro sou�et
     * @return n�zev ceny, kter� se pou��v� pro sou�et
     */
    public String getPriceName() {
        return DoBuy.getPriceName(priceType);
    }
    
    /**
     * Vrac� n�zev ceny, kter� se pou��v� pro sou�et
     * @param priceType index p�edstavuj�c� cenu. Jedn� se o jednu z konstatn DoBuy.USE_...
     * @return n�zev ceny, kter� se pou��v� pro sou�et
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
     * Vrac�, kter� cena se pou��v� pro sou�et
     * @return kter� cena se pou��v� pro sou�et
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
