/*
 * StockingPreview.java
 *
 * Vytvo�eno 28. �nor 2006, 23:13
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

import cz.control.business.*;
import java.text.*;

import java.util.*;
/**
 * Program Control - Skladov� syst�m
 *
 * Uchov�v� informace p�ehled� o inventur�ch
 *
 * @author Kamil Je�ek
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
     * Vytvo�� novou pr�zdnou instanci StockingPreview
     */
    public StockingPreview() {
        this( 0, -1, new GregorianCalendar(), -1, "", -1, "", false, DoBuy.USE_NC_FOR_SUM);
    }
    
    /**
     * Vytvo�� nov� objekt p�ehledu inventur
     * @param usePrice kter� cena byla pou�ita pro sou�et na p��jemce. Vyu��v� konstanty s
     * DoBuy
     * @param stockingIdListing jednozna�n� ID inventury
     * @param number po�adov� ��slo inventury
     * @param date datum uskute�n�n� inventury
     * @param difer celkov� pen�n� rozd�l vyj�d�en� hodnotou, kde posledn�
     * dv� ��slice maj� v�znam hal��u
     * @param author Autor inventury
     * @param userID Odkaz do tabulky u�ivatelsk�ch ��t�, kde je uveden autor
     * inventury
     * @param text Textov� pozn�mka k inventu�e
     * @param isLock nastavuje, zda m� b�t obdob� p�ed inventurou uzam�eno.
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
     * Vrac� identifika�n� ��slo
     * @return identifika�n� ��slo
     */
    public int getStockingIdListing() {
        return stockingIdListing;
    }
    
    /**
     * Vrac� po�adov� ��slo obchodu
     * @return po�adov� ��slo obchodu
     */
    public int getNumber() {
        return number;
    }
    
    /**
     * Vrac� datum proveden� obchodu
     * @return datum proveden� obchodu
     */
    public Calendar getDate() {
        return date;
    }
    
    /**
     * Vrac� celkov� rozd�l inventury vyj�d�en� v pen�z�ch
     * @return celkov� rozd�l vyj�d�en� v pen�z�ch. Vrac� hodnotu, kde posledn�
     * dv� ��slice maj� v�znam hal���
     */
    public long getDifer() {
        return difer;
    }
    
    /**
     * Vrac� jmno autora p��jemky/v�dejky
     * @return jm�no autora proveden�ho obchodu
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * Vrac� textovou pozn�mku u inventury
     * @return Textov� pozn�mka inventury
     */
    public String getText() {
        return text;
    }
    
    /**
     * Vrac� odkaz do tabulky u�ivatelsk�ch ��t�
     * @return  odkaz do tabulky u�ivatelsk�ch ��t�
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Vrac�, zda je nastaveno zam�en� obdob� p�ed inventurou
     * @return true, jestli�e je obddob�p �ed inventurou uzam�en�
     */
    public boolean isLock() {
        return isLock;
    }
    
    /**
     * Porovn� objekty
     * @param o porovn�van� objekt
     * @return vrac� z�pornou, kladnou, nebo nulovou hodnotu
     */
    public int compareTo(StockingPreview o) {
        
        int result = date.compareTo( ((StockingPreview) o).getDate());
        
        if (result == 0) {
            return number - ((StockingPreview) o).getNumber();
        }
        
        return result;
    }
    
    
    /**
     * Vrac� hashovac� k�d
     * @return hashovac� k�d, kter� odpov�d� instanci <code>number</code>
     */
    public int hashCode() {
        return stockingIdListing;
    }
    
    
    /**
     * Porovn� objekty
     * @param o objekt k porovn�n�
     * @return true, jestli�e jsou ojekty shodn�
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
                // pou�ij form�tov�n� textu podle prost�ed�
                dateFormat.format( new Date(date.getTimeInMillis()) ) + " " +
                // .. rad�ji ne� takto
//               df.format(date.get(Calendar.DAY_OF_MONTH)) + "." +
//               df.format(date.get(Calendar.MONTH)+1) + "." +
//               df.format(date.get(Calendar.YEAR)) + " " +
//               df.format(date.get(Calendar.HOUR)) + ":" +
//               df.format(date.get(Calendar.MINUTE)) + " " +
                "(" + author + ")" +
                "";
    }
    
    /**
     * Vrac�, kter� cena byla pou�ita pro sou�et
     * @return cena, kter� byla pou�ita pro sou�et.
     * Pou��v� konstanty s DoBuy
     * (DoBuy.USE_NC_FOR_SUM, atd.)
     */
    public int getUsedPrice() {
        return usePrice;
    }
}
