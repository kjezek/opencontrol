/*
 * StockingPreview.java
 *
 * Vytvoøeno 28. únor 2006, 23:13
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import cz.control.business.*;
import java.text.*;

import java.util.*;
/**
 * Program Control - Skladový systém
 *
 * Uchovává informace pøehledù o inventurách
 *
 * @author Kamil Ježek
 *
 * (C) 2006, ver. 1.0
 */
public class StockingPreview implements Comparable<StockingPreview> {
    private int stockingIdListing;
    private int number;
    private Calendar date;
    private long difer;
    private String author;
    private int userId;
    private String text;
    private boolean isLock;
    private int usePrice;
    
    /**
     * Vytvoøí novou prázdnou instanci StockingPreview
     */
    public StockingPreview() {
        this( 0, -1, new GregorianCalendar(), -1, "", -1, "", false, DoBuy.USE_NC_FOR_SUM);
    }
    
    /**
     * Vytvoøí nový objekt pøehledu inventur
     * @param usePrice která cena byla použita pro souèet na pøíjemce. Využívá konstanty s
     * DoBuy
     * @param stockingIdListing jednoznaèné ID inventury
     * @param number poøadové èíslo inventury
     * @param date datum uskuteènìní inventury
     * @param difer celkový penìžní rozdíl vyjádøený hodnotou, kde poslední
     * dvì èíslice mají význam haléøu
     * @param author Autor inventury
     * @param userID Odkaz do tabulky uživatelských úètù, kde je uveden autor
     * inventury
     * @param text Textová poznámka k inventuøe
     * @param isLock nastavuje, zda má být období pøed inventurou uzamèeno.
     */
    public StockingPreview(int stockingIdListing, int number, Calendar date,
            long difer, String author, int userID, String text, boolean isLock,
            int usePrice) {
        
        this.stockingIdListing = stockingIdListing;
        this.number = number;
        this.date = date;
        this.difer = difer;
        this.author = author;
        this.userId = userID;
        this.text = text;
        this.isLock = isLock;
        this.usePrice = usePrice;
    }
    
    /**
     * Vrací identifikaèní èíslo
     * @return identifikaèní èíslo
     */
    public int getStockingIdListing() {
        return stockingIdListing;
    }
    
    /**
     * Vrací poøadové èíslo obchodu
     * @return poøadové èíslo obchodu
     */
    public int getNumber() {
        return number;
    }
    
    /**
     * Vrací datum provedení obchodu
     * @return datum provedení obchodu
     */
    public Calendar getDate() {
        return date;
    }
    
    /**
     * Vrací celkový rozdíl inventury vyjádøený v penìzích
     * @return celkový rozdíl vyjádøený v penìzích. Vrací hodnotu, kde poslední
     * dvì èíslice mají význam haléøù
     */
    public long getDifer() {
        return difer;
    }
    
    /**
     * Vrací jmno autora pøíjemky/výdejky
     * @return jméno autora provedeného obchodu
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * Vrací textovou poznámku u inventury
     * @return Textová poznámka inventury
     */
    public String getText() {
        return text;
    }
    
    /**
     * Vrací odkaz do tabulky uživatelských úètù
     * @return  odkaz do tabulky uživatelských úètù
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Vrací, zda je nastaveno zamèení období pøed inventurou
     * @return true, jestliže je obddobíp øed inventurou uzamèené
     */
    public boolean isLock() {
        return isLock;
    }
    
    /**
     * Porovná objekty
     * @param o porovnávaný objekt
     * @return vrací zápornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(StockingPreview o) {
        
        int result = date.compareTo( ((StockingPreview) o).getDate());
        
        if (result == 0) {
            return number - ((StockingPreview) o).getNumber();
        }
        
        return result;
    }
    
    
    /**
     * Vrací hashovací kód
     * @return hashovací kód, který odpovídá instanci <code>number</code>
     */
    public int hashCode() {
        return stockingIdListing;
    }
    
    
    /**
     * Porovná objekty
     * @param o objekt k porovnání
     * @return true, jestliže jsou ojekty shodné
     */
    public boolean equals(Object o) {
        
        if (o == null)
            return false;
        
        return ( (StockingPreview) o).getStockingIdListing() == stockingIdListing &&
                ( (StockingPreview) o).getDate().get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) &&
                ( (StockingPreview) o).getDate().get(Calendar.MONTH+1) == date.get(Calendar.MONTH+1) &&
                ( (StockingPreview) o).getDate().get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                ( (StockingPreview) o).number == number;
    }
    
    
    /**
     *
     * @return
     */
    public String toString() {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance( new Locale("cs", "CZ"));
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        
        return " " +
                df.format(number) + " / " +
                // použij formátování textu podle prostøedí
                dateFormat.format( new Date(date.getTimeInMillis()) ) + " " +
                // .. radìji než takto
//               df.format(date.get(Calendar.DAY_OF_MONTH)) + "." +
//               df.format(date.get(Calendar.MONTH)+1) + "." +
//               df.format(date.get(Calendar.YEAR)) + " " +
//               df.format(date.get(Calendar.HOUR)) + ":" +
//               df.format(date.get(Calendar.MINUTE)) + " " +
                "(" + author + ")" +
                "";
    }
    
    /**
     * Vrací, která cena byla použita pro souèet
     * @return cena, která byla použita pro souèet.
     * Používá konstanty s DoBuy
     * (DoBuy.USE_NC_FOR_SUM, atd.)
     */
    public int getUsedPrice() {
        return usePrice;
    }
}
