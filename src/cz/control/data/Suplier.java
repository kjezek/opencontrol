/*
 * Suplier.java
 *
 * Created on 23. záøí 2005, 18:14
 */

package cz.control.data;

import cz.control.business.*;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Program Control - Skladový systém
 *
 * Tvoøí položky karty dodavatele
 *
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public class Suplier implements Comparable<Suplier> {
    private int id;
    private String name;
    private String person;
    private String sendStreet;
    private String sendCity;
    private String sendPsc;
    private String tel;
    private String fax;
    private String mail;
    private String web;
    private String ico;
    private String dic;
    private boolean isDph;
    private String account;
    private String note;
    
    /**
     *  Vytvoøí prázdnou kartu odbìratele
     */
    public Suplier() {
        this(-1, "", "", "", "", "", "", "", "", "", "", "", false, "", "");
    }
    
    /**
     * Vytvoøí kartu odbìratele (zákazníka).
     * @param id jednoznaèné èíslo odbìratele. Pøi ukládání do databáze se 
     * však urøuje podle èíselné øady, nezávisle na zadané hodnotì.
     * @param name jméno odbìratele
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
    public Suplier(int id, String name, String person, 
            String sendStreet, String sendCity, String sendPsc, 
            String tel, String fax, String mail, String web, 
            String ico, String dic, boolean isDph, String account,
            String note) {
        
        this.id = id;
        this.name = name;
        this.person = person;
        this.sendStreet = sendStreet;
        this.sendCity = sendCity;
        this.sendPsc = sendPsc;
        this.tel = tel;
        this.fax = fax;
        this.mail = mail;
        this.web = web;
        this.ico = ico;
        this.dic = dic;
        this.isDph = isDph;
        this.account = account;
        this.note = note;
    }

    
    /**
     * Provede porovnání dvou objektù.
     * Porovnává název a IÈO
     * @param o porovnávný objekt
     * @return -1, 0, 1 podle výsledku porovnání
     */
    public int compareTo(Suplier o) {
        int result = name.compareToIgnoreCase(o.name);
        if (result == 0) {
            return  ico.compareToIgnoreCase(o.ico);
        }
        return result;
    }
    
    /**
     * Porovnává objekty podle id
     * @param o objekt, se kterým se má porovnat
     * @return true jestliže jsou si objekty rovny
     */
    public boolean equals(Object o) {
        
        if (o == null)
            return false;
        
        return name.equals( ((Suplier) o).getName()) &&
                person.equals( ((Suplier) o).getPerson()) &&
                sendStreet.equals( ((Suplier) o).getSendStreet()) &&
                sendCity.equals( ((Suplier) o).getSendCity()) &&
                sendPsc.equals( ((Suplier) o).getSendPsc()) &&
                tel.equals( ((Suplier) o).getTel()) &&
                fax.equals( ((Suplier) o).getFax()) &&
                mail.equals( ((Suplier) o).getMail()) &&
                web.equals( ((Suplier) o).getWeb()) &&
                ico.equals( ((Suplier) o).getIco()) &&
                dic.equals( ((Suplier) o).getDic()) &&
                isDph == ((Suplier) o).isDph() &&
                account.equals( ((Suplier) o).getAccount());
    }
    
    /**
     * Vrací hashovací kód. Ten se urèuje podle id dodavatele
     * @return hashovací kód
     */
    public int hashCode() {
        return id;
    }

    /**
     * Vrací èíslo úètu
     * @return èíslo úètu
     */
    public String getAccount() {
        return this.account;
    }

    /**
     * Vrací DIÈ
     * @return DIÈ
     */
    public String getDic() {
        return this.dic;
    }

    /**
     * Vrací faxové èíslo
     * @return fax
     */
    public String getFax() {
        return this.fax;
    }

    /**
     * Vrací IÈO
     * @return IÈO
     */
    public String getIco() {
        return this.ico;
    }

    /**
     * Vrací identifikaèní èíslo.
     * Pozor: Pøi ukládání do databáze se vytvoøí identifikaèní èíslo následující v øadì
     * bez ohledu na to, která hodnota (id) byla zadána v konstruktoru. Proto se mùže vrácená
     * hodnota lišit od hodnoty, která byla použita v konstruktoru pøi vytváøení objektu.
     * @return id, které odpovídá hodnotì z databáze
     */
    public int getId() {
        return this.id;
    }

    /**
     * Vrací, zda je uživatel plátcem DPH
     * @return true jestliže je plátcem DPH, jinak false
     */
    public boolean isDph() {
        return this.isDph;
    }

    /**
     * Vrací mailovou adresu
     * @return e-mailovou adresu
     */
    public String getMail() {
        return this.mail;
    }

    /**
     * Vrací jméno zákazníka
     * @return jméno odbìratele
     */
    public String getName() {
        return this.name;
    }

    /**
     * Vrací odpovìdnou osobu
     * @return kontaktní adresu
     */
    public String getPerson() {
        return this.person;
    }

    /**
     * Vrací mìsto z dodací adresy
     * @return mìsto
     */
    public String getSendCity() {
        return this.sendCity;
    }

    /**
     * Vrací PSC z dodací adresy
     * @return PSÈ
     */
    public String getSendPsc() {
        return this.sendPsc;
    }

    /**
     * Vrací uloci s dodací adresy
     * @return ulici
     */
    public String getSendStreet() {
        return this.sendStreet;
    }

    /**
     * Vrací telefon
     * @return telefon
     */
    public String getTel() {
        return this.tel;
    }

    /**
     * vrací internetovou adresu
     * @return webovou adresu
     */
    public String getWeb() {
        return this.web;
    }
    
    public String toString() {
        DecimalFormat df = Settings.getPriceFormat();
        df.applyPattern("00");

        String personPart = (person == null || person.length() == 0) ? "" : " (" + person + ")";
        
        return name + personPart;
    }

    public String getNote() {
        return note;
    }
    
}
 