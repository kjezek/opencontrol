/*
 * Customer.java
 *
 * Created on 23. z��� 2005, 18:15
 */

package cz.control.data;

import cz.control.data.Suplier;

/**
 * Program Control - Skladov� syst�m
 *
 * Obsahuje polo�ky karty odb�rate.
 *
 * @author Kamil Je�ek
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
     *  Vyvtvo�� pr�zdn� objekt karty dodavatele
     */
    public Customer() {
        this(-1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", true, "", "");
    }
    
    /**
     * Vytvo�� kartu dodavatele .
     * @param payStreet ulice faktura�n� adresy
     * @param payCity m�sto faktura�n� adresy
     * @param payPsc PS� faktura�n� adresy
     * @param id jednozna�n� ��slo dodavatele. P�i ukl�d�n� do datab�ze se 
     * v�ak ur�uje podle ��seln� �ady, nez�visle na zadan� hodnot�.
     * @param name jm�no dodavatele
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
     * Vr�t� m�sto faktura�n� adresy
     * @return m�sto faktura�n� adresy
     */
    public String getPayCity() {
        return this.payCity;
    }

    /**
     * vrac� sm�rovac� ��slo faktura�n� adresy
     * @return sm�rovac� ��slo faktura�n� adresy
     */
    public String getPayPsc() {
        return this.payPsc;
    }

    /**
     * Vrac� ulici faktura�n� adresy
     * @return ulice faktura�n� adresy
     */
    public String getPayStreet() {
        return this.payStreet;
    }
    
    
}
