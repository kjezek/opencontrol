/*
 * About.java
 *
 * Vytvoøeno 17. bøezen 2006, 19:21
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.data;

/**
 * Program Control - Skladový systém
 *
 * Tøída uchovávající informace o spoleènosti používající systém
 *
 * @author Kamil Ježek
 *
 * (C) 2006, ver. 1.0
 */
public class About {
    private Customer customer;
    private byte[] logoPath = {};
    
    /**
     *  Vytvoøí prázdný objekt
     */
    public About() {
        this.customer = new Customer();
        logoPath = "".getBytes();
    }
    
    /**
     * Vytvoøí informace o Nás
     * @param logoPath cesta udávající logo spoleènosti
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
     * Vytvoøí nový objekt
     * @param customer Objekt s informacemi o spoleènosti
     * @param logoPath cesta udávající logo spoleènosti
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
     * Vrací objekt, který reperzentuje údaje o spoleènosti
     * @return objekt, který reperzentuje údaje o spoleènosti
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Vrací pole byte s daty obrázku
     * @return Vrací pole byte s daty obrázku
     */
    public byte[] getLogoPath() {
        return logoPath;
    }
    
    public String toString() {
        return customer.getId() + ". " + customer.getName() + " (" + customer.getPerson()+")"; 
    }
    
}
