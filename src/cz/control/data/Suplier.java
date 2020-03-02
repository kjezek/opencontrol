/*
 * Suplier.java
 *
 * Created on 23. z��� 2005, 18:14
 */

package cz.control.data;

import cz.control.business.*;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Program Control - Skladov� syst�m
 *
 * Tvo�� polo�ky karty dodavatele
 *
 * @author Kamil Je�ek
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
     *  Vytvo�� pr�zdnou kartu odb�ratele
     */
    public Suplier() {
        this(-1, "", "", "", "", "", "", "", "", "", "", "", false, "", "");
    }
    
    /**
     * Vytvo�� kartu odb�ratele (z�kazn�ka).
     * @param id jednozna�n� ��slo odb�ratele. P�i ukl�d�n� do datab�ze se 
     * v�ak ur�uje podle ��seln� �ady, nez�visle na zadan� hodnot�.
     * @param name jm�no odb�ratele
     * @param person jm�no kontaktn� osoby
     * @param sendStreet ulice dodac� adresy
     * @param sendCity m�sto dodac� adresy
     * @param sendPsc PS� dodac� adresy
     * @param tel telefon
     * @param fax faxov� ��slo
     * @param mail e-mail
     * @param web internetov� adresa
     * @param ico I�O
     * @param dic DI�
     * @param isDph �daj, zda je pl�tce DPH
     * @param account ��slo ��tu
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
     * Provede porovn�n� dvou objekt�.
     * Porovn�v� n�zev a I�O
     * @param o porovn�vn� objekt
     * @return -1, 0, 1 podle v�sledku porovn�n�
     */
    public int compareTo(Suplier o) {
        int result = name.compareToIgnoreCase(o.name);
        if (result == 0) {
            return  ico.compareToIgnoreCase(o.ico);
        }
        return result;
    }
    
    /**
     * Porovn�v� objekty podle id
     * @param o objekt, se kter�m se m� porovnat
     * @return true jestli�e jsou si objekty rovny
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
     * Vrac� hashovac� k�d. Ten se ur�uje podle id dodavatele
     * @return hashovac� k�d
     */
    public int hashCode() {
        return id;
    }

    /**
     * Vrac� ��slo ��tu
     * @return ��slo ��tu
     */
    public String getAccount() {
        return this.account;
    }

    /**
     * Vrac� DI�
     * @return DI�
     */
    public String getDic() {
        return this.dic;
    }

    /**
     * Vrac� faxov� ��slo
     * @return fax
     */
    public String getFax() {
        return this.fax;
    }

    /**
     * Vrac� I�O
     * @return I�O
     */
    public String getIco() {
        return this.ico;
    }

    /**
     * Vrac� identifika�n� ��slo.
     * Pozor: P�i ukl�d�n� do datab�ze se vytvo�� identifika�n� ��slo n�sleduj�c� v �ad�
     * bez ohledu na to, kter� hodnota (id) byla zad�na v konstruktoru. Proto se m��e vr�cen�
     * hodnota li�it od hodnoty, kter� byla pou�ita v konstruktoru p�i vytv��en� objektu.
     * @return id, kter� odpov�d� hodnot� z datab�ze
     */
    public int getId() {
        return this.id;
    }

    /**
     * Vrac�, zda je u�ivatel pl�tcem DPH
     * @return true jestli�e je pl�tcem DPH, jinak false
     */
    public boolean isDph() {
        return this.isDph;
    }

    /**
     * Vrac� mailovou adresu
     * @return e-mailovou adresu
     */
    public String getMail() {
        return this.mail;
    }

    /**
     * Vrac� jm�no z�kazn�ka
     * @return jm�no odb�ratele
     */
    public String getName() {
        return this.name;
    }

    /**
     * Vrac� odpov�dnou osobu
     * @return kontaktn� adresu
     */
    public String getPerson() {
        return this.person;
    }

    /**
     * Vrac� m�sto z dodac� adresy
     * @return m�sto
     */
    public String getSendCity() {
        return this.sendCity;
    }

    /**
     * Vrac� PSC z dodac� adresy
     * @return PS�
     */
    public String getSendPsc() {
        return this.sendPsc;
    }

    /**
     * Vrac� uloci s dodac� adresy
     * @return ulici
     */
    public String getSendStreet() {
        return this.sendStreet;
    }

    /**
     * Vrac� telefon
     * @return telefon
     */
    public String getTel() {
        return this.tel;
    }

    /**
     * vrac� internetovou adresu
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
 