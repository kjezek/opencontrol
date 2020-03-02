/*
 * About.java
 *
 * Vytvo�eno 17. b�ezen 2006, 19:21
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da uchov�vaj�c� informace o spole�nosti pou��vaj�c� syst�m
 *
 * @author Kamil Je�ek
 *
 * (C) 2006, ver. 1.0
 */
public class About {
    private Customer customer;
    private byte[] logoPath = {};
    
    /**
     *  Vytvo�� pr�zdn� objekt
     */
    public About() {
        this.customer = new Customer();
        logoPath = "".getBytes();
    }
    
    /**
     * Vytvo�� informace o N�s
     * @param logoPath cesta ud�vaj�c� logo spole�nosti
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
    public About(int id, String name, String person, 
            String sendStreet, String sendCity, String sendPsc, 
            String payStreet, String payCity, String payPsc, 
            String tel, String fax, String mail, String web, 
            String ico, String dic, 
            boolean isDph, 
            String account,
            byte[] logoPath,
            String note) {
        
        customer = new Customer(id, name, person, sendStreet, sendCity, sendPsc, 
                payStreet, payCity, payPsc, 
                tel, fax, mail, web, ico, dic, isDph, account, note);

        this.logoPath = logoPath;
    }
    
    /**
     * Vytvo�� nov� objekt
     * @param customer Objekt s informacemi o spole�nosti
     * @param logoPath cesta ud�vaj�c� logo spole�nosti
     */
    public About(Customer customer, byte[] logoPath) {
        this(customer.getId(), customer.getName(), customer.getPerson(),
                customer.getSendStreet(), customer.getSendCity(), customer.getSendPsc(),
                customer.getPayStreet(), customer.getPayCity(), customer.getPayPsc(),
                customer.getTel(), customer.getFax(), customer.getMail(), customer.getWeb(),
                customer.getIco(), customer.getDic(),
                customer.isDph(),
                customer.getAccount(),
                logoPath,
                customer.getNote());
    }

    /**
     * Vrac� objekt, kter� reperzentuje �daje o spole�nosti
     * @return objekt, kter� reperzentuje �daje o spole�nosti
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Vrac� pole byte s daty obr�zku
     * @return Vrac� pole byte s daty obr�zku
     */
    public byte[] getLogoPath() {
        return logoPath;
    }
    
    public String toString() {
        return customer.getId() + ". " + customer.getName() + " (" + customer.getPerson()+")"; 
    }
    
}
