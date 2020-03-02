/*
 * Customer.java
 *
 * Created on 23. záøí 2005, 18:15
 */

package cz.control.data;

import cz.control.data.Suplier;

/**
 * Program Control - Skladový systém
 *
 * Obsahuje položky karty odbìrate.
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public final class Customer extends Suplier {
    
    private String payStreet;
    private String payCity;
    private String payPsc;
    
    public static final Suplier DISCOUNT_CUSTOMER = 
            new Suplier(-1, "Maloobchod", "", "", "", "", "", "", "", "", "", "", false, "", "");
    
    /**
     *  Vyvtvoøí prázdný objekt karty dodavatele
     */
    public Customer() {
        this(-1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", true, "", "");
    }
    
    /**
     * Vytvoøí kartu dodavatele .
     * @param payStreet ulice fakturaèní adresy
     * @param payCity mìsto fakturaèní adresy
     * @param payPsc PSÈ fakturaèní adresy
     * @param id jednoznaèné èíslo dodavatele. Pøi ukládání do databáze se 
     * však urøuje podle èíselné øady, nezávisle na zadané hodnotì.
     * @param name jméno dodavatele
     * @param person jméno kontaktní osoby
     * @param sendStreet ulice dodací adresy
     * @param sendCity mìsto dodací adresy
     * @param sendPsc PSÈ dodací adresy
     * @param tel telefon
     * @param fax faxové èíslo
     * @param mail e-mail
     * @param web internetová adresa
     * @param ico IÈO
     * @param dic DIÈ
     * @param isDph údaj, zda je plátce DPH
     * @param account èíslo úètu
     */
    public Customer(int id, String name, String person, 
            String sendStreet, String sendCity, String sendPsc, String payStreet, 
            String payCity, String payPsc, 
            String tel, String fax, String mail, String web, 
            String ico, String dic, boolean isDph, String account,
            String note) {
        
        super(id, name, person, sendStreet, sendCity, sendPsc, tel, fax, mail, web, ico, dic, isDph, account, note);
        this.payStreet = payStreet;
        this.payCity = payCity;
        this.payPsc = payPsc;
    }
    

    /**
     * Vrátí mìsto fakturaèní adresy
     * @return mìsto fakturaèní adresy
     */
    public String getPayCity() {
        return this.payCity;
    }

    /**
     * vrací smìrovací èíslo fakturaèní adresy
     * @return smìrovací èíslo fakturaèní adresy
     */
    public String getPayPsc() {
        return this.payPsc;
    }

    /**
     * Vrací ulici fakturaèní adresy
     * @return ulice fakturaèní adresy
     */
    public String getPayStreet() {
        return this.payStreet;
    }
    
    
}
