/*
 * TradeItemPreview.java
 *
 * Created on 24. záøí 2005, 23:04
 */

package cz.control.data;

import cz.control.business.*;
import java.text.*;

import java.util.*;
/**
 * Program Control - Skladový systém
 *
 * Uchovává informace pøehledù o obchodech - výdejek a pøíjemek
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public final class TradeItemPreview implements Comparable<TradeItemPreview> {
    private int tradeIdListing;
    private int number;
    private Calendar date;
    private int id;
    private long totalPriceDPH;
    private long totalDPH;
    private long totalPrice;
    private long reduction;
    private String author;
    private int userId;
    private String billNumber;
    private boolean isCash;
    
    
    /**
     *  Vytvoøí prázdný pøehled pøíjmky
     */
    public TradeItemPreview() {
        this(0, -1, new GregorianCalendar(), -1, -1, -1, -1, -1, "", -1, "", false);    
    }
    
    /**
     * Vytvoøí nový objekt uchovávající pøehled o obchodech
     * @param usePrice která cena byla použita pro souèet na pøíjemce. Využívá konstanty s
     * @param totalDPH celkové DPH (Hodnota totalPrice + totalDPH mùže být fíky zaokrouhlení jiná než totalPriceDPH)
     * @param totalPriceDPH celková cena s DPH (Hodnota totalPrice + totalDPH mùže být fíky zaokrouhlení jiná než totalPriceDPH)
     * @param reduction celková sleva v Kè (Hodnota totalPrice + totalDPH mùže být fíky zaokrouhlení jiná než totalPriceDPH)
     * @param tradeIdListing Jednoznaèné èíslo pøíjemky získané pøi naèítání z databáze
     * @param userId Odkaz do tabulky uživatelù. Odkazuje na autora transakce
     * @param number èíslo obchodu
     * @param date datum provedení obchodu
     * @param id odkaz na odbìratele, èi dodavatele
     * @param totalPrice celková cena prodejky, èi výdejky, (Hodnota totalPrice + totalDPH mùže být fíky zaokrouhlení jiná než totalPriceDPH)
     * @param author podpis osoby, která provedla obchod
     */
    public TradeItemPreview(int tradeIdListing, int number, Calendar date, int id, 
            long totalPriceDPH, long totalDPH, long totalPrice, long reduction, 
            String author, int userId,
            String billNumber,
            boolean isCash) {
        
        this.tradeIdListing = tradeIdListing;
        this.number = number;
        this.date = date;
        this.id = id;
        this.totalPrice = totalPrice;
        this.totalDPH = totalDPH;
        this.totalPriceDPH = totalPriceDPH;
        this.reduction = reduction;
        this.author = author;
        this.userId = userId;
        this.billNumber = billNumber;
        this.isCash = isCash;
    }
    
    
    
    /**
     * Vrací jmno autora pøíjemky/výdejky
     * @return jméno autora provedeného obchodu
     */
    public String getAuthor() {
        return this.author;
    }
    
    /**
     * Vrací odkaz do tabulky dodavatelù èi odbìratelù
     * @return èíslo dodavatele/odbìratele
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Vrací datum provedení obchodu
     * @return datum provedení obchodu
     */
    public Calendar getDate() {
        return this.date;
    }
    
    /**
     * Vrací poøadové èíslo obchodu
     * @return poøadové èíslo obchodu
     */
    public int getNumber() {
        return this.number;
    }
    
    /**
     * Vrací celkovou cenu za provedený ochod
     * @return celkovou cenu za provedený ochod
     */
    public long getTotalPrice() {
        return this.totalPrice;
    }
    
    /**
     * Vrací odkaz do tabulky uživatelských úètù
     * @return  odkaz do tabulky uživatelských úètù
     */
    public int getUserId() {
        return this.userId;
    }
    
    /**
     * Porovná objekty
     * @param o porovnávaný objekt
     * @return vrací zápornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(TradeItemPreview o) {

        int result = date.compareTo( ((TradeItemPreview) o).getDate());
        
        if (result == 0) {
            return number - ((TradeItemPreview) o).getNumber();
        }
        
        return result;
    }
    
    /**
     * Vrací hashovací kód
     * @return hashovací kód, který odpovídá instanci <code>number</code>
     */
    @Override
    public int hashCode() {
        return tradeIdListing;
    }
    
    /**
     * Porovná objekty
     * @param o objekt k porovnání
     * @return true, jestliže jsou ojekty shodné
     */
    @Override
    public boolean equals(Object o) {
        
        if (o == null)
            return false;
        
        return ( (TradeItemPreview) o).getTradeIdListing() == tradeIdListing &&
               ( (TradeItemPreview) o).getDate().get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) &&
               ( (TradeItemPreview) o).getDate().get(Calendar.MONTH+1) == date.get(Calendar.MONTH+1) &&
               ( (TradeItemPreview) o).getDate().get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               ( (TradeItemPreview) o).number == number;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String toString() {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance( new Locale("cs", "CZ"));
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        
        String authorPart = (author == null || author.length() == 0) ? "" : "(" + author + ")";
        
        return " " +
               df.format(number) + " / " + 
               dateFormat.format( new Date(date.getTimeInMillis()) ) + " " +
               authorPart +
               "";
    }

    /**
     * Vrací identifikaèní èíslo
     * @return identifikaèní èíslo
     */
    public int getTradeIdListing() {
        return tradeIdListing;
    }

    /**
     * Vrací celkovou cenu s DPH
     * @return celková cena s DPH
     */
    public long getTotalPriceDPH() {
        return totalPriceDPH;
    }

    /**
     * Vrací celkouvou slevu v korunách
     * @return sleva v korunách
     */
    public long getReduction() {
        return reduction;
    }

    
    /**
     * Vrací celkové DPH
     * (Hodnota totalPrice + totalDPH mùže být fíky zaokrouhlení jiná než totalPriceDPH)
     * @return celkové DPH
     */
    public long getTotalDPH() {
        return totalDPH;
    }


    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public boolean isCash() {
        return isCash;
    }

    public void setIsCash(boolean isCash) {
        this.isCash = isCash;
    }
    
}
