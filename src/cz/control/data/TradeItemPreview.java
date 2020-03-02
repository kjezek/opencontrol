/*
 * TradeItemPreview.java
 *
 * Created on 24. z��� 2005, 23:04
 */

package cz.control.data;

import cz.control.business.*;
import java.text.*;

import java.util.*;
/**
 * Program Control - Skladov� syst�m
 *
 * Uchov�v� informace p�ehled� o obchodech - v�dejek a p��jemek
 *
 * @author Kamil Je�ek
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
     *  Vytvo�� pr�zdn� p�ehled p��jmky
     */
    public TradeItemPreview() {
        this(0, -1, new GregorianCalendar(), -1, -1, -1, -1, -1, "", -1, "", false);    
    }
    
    /**
     * Vytvo�� nov� objekt uchov�vaj�c� p�ehled o obchodech
     * @param usePrice kter� cena byla pou�ita pro sou�et na p��jemce. Vyu��v� konstanty s
     * @param totalDPH celkov� DPH (Hodnota totalPrice + totalDPH m��e b�t f�ky zaokrouhlen� jin� ne� totalPriceDPH)
     * @param totalPriceDPH celkov� cena s DPH (Hodnota totalPrice + totalDPH m��e b�t f�ky zaokrouhlen� jin� ne� totalPriceDPH)
     * @param reduction celkov� sleva v K� (Hodnota totalPrice + totalDPH m��e b�t f�ky zaokrouhlen� jin� ne� totalPriceDPH)
     * @param tradeIdListing Jednozna�n� ��slo p��jemky z�skan� p�i na��t�n� z datab�ze
     * @param userId Odkaz do tabulky u�ivatel�. Odkazuje na autora transakce
     * @param number ��slo obchodu
     * @param date datum proveden� obchodu
     * @param id odkaz na odb�ratele, �i dodavatele
     * @param totalPrice celkov� cena prodejky, �i v�dejky, (Hodnota totalPrice + totalDPH m��e b�t f�ky zaokrouhlen� jin� ne� totalPriceDPH)
     * @param author podpis osoby, kter� provedla obchod
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
     * Vrac� jmno autora p��jemky/v�dejky
     * @return jm�no autora proveden�ho obchodu
     */
    public String getAuthor() {
        return this.author;
    }
    
    /**
     * Vrac� odkaz do tabulky dodavatel� �i odb�ratel�
     * @return ��slo dodavatele/odb�ratele
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Vrac� datum proveden� obchodu
     * @return datum proveden� obchodu
     */
    public Calendar getDate() {
        return this.date;
    }
    
    /**
     * Vrac� po�adov� ��slo obchodu
     * @return po�adov� ��slo obchodu
     */
    public int getNumber() {
        return this.number;
    }
    
    /**
     * Vrac� celkovou cenu za proveden� ochod
     * @return celkovou cenu za proveden� ochod
     */
    public long getTotalPrice() {
        return this.totalPrice;
    }
    
    /**
     * Vrac� odkaz do tabulky u�ivatelsk�ch ��t�
     * @return  odkaz do tabulky u�ivatelsk�ch ��t�
     */
    public int getUserId() {
        return this.userId;
    }
    
    /**
     * Porovn� objekty
     * @param o porovn�van� objekt
     * @return vrac� z�pornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(TradeItemPreview o) {

        int result = date.compareTo( ((TradeItemPreview) o).getDate());
        
        if (result == 0) {
            return number - ((TradeItemPreview) o).getNumber();
        }
        
        return result;
    }
    
    /**
     * Vrac� hashovac� k�d
     * @return hashovac� k�d, kter� odpov�d� instanci <code>number</code>
     */
    @Override
    public int hashCode() {
        return tradeIdListing;
    }
    
    /**
     * Porovn� objekty
     * @param o objekt k porovn�n�
     * @return true, jestli�e jsou ojekty shodn�
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
     * Vrac� identifika�n� ��slo
     * @return identifika�n� ��slo
     */
    public int getTradeIdListing() {
        return tradeIdListing;
    }

    /**
     * Vrac� celkovou cenu s DPH
     * @return celkov� cena s DPH
     */
    public long getTotalPriceDPH() {
        return totalPriceDPH;
    }

    /**
     * Vrac� celkouvou slevu v korun�ch
     * @return sleva v korun�ch
     */
    public long getReduction() {
        return reduction;
    }

    
    /**
     * Vrac� celkov� DPH
     * (Hodnota totalPrice + totalDPH m��e b�t f�ky zaokrouhlen� jin� ne� totalPriceDPH)
     * @return celkov� DPH
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
