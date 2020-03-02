/*
 * Errors.java
 *
 * Created on 31. øíjen 2005, 20:17
 *
 */

package cz.control.errors;


/**
 * Vıètovı typ pøedstavující chyby, které mohou nastat v programu 
 * @author Kamil
 */
public enum Errors {
    /* 0 - Obecné chyby */
    NO_ERROR(0, "Bez chyby. ", ""),
    
    /* 100 - Chyby uivatelskıch vstupù */
    BAD_PRIVILEG(100, "Pøístup zamítnut", "Vaše uivatelská práva neumoòují provést tuto akci."),
    MISSING_FIELDS(130, "Nevyplnìné poloky", "Pro dokonèení je tøeba vyplnit všechny poloky. "),
    NO_GOODS_FOUND(131, "Zboí nenalezeno", "Zboí nebylo ve skladì nalezeno. "),
    NO_SUPLIER_FOUND(132, "Dodavatel nenalezen", "Dodavatel nebyl nalezen. "),
    NO_CUSTOMER_FOUND(133, "Odbìratel nenalezen", "Odbìratel nebyl nalezen. "),
    NOT_EXEPT_VALUE(134, "Neoèekávaná hodnota", "Chybnì zadaná hodnota."),
    NOT_EXEPT_DATE(135, "Neoèekávané datum", "Chybnì zadané datum."),
    NO_SELECTED_VALUE(136, "Není nic vybráno", "Nevybrali jste ádnou poloku."),
    DUPLICIT_VALUE(137, "Duplicitní hodnota", "Není moné vícekrát vloit stejnou hodnotu."),
    NOT_POSIBLE_CONFIRM_SALE(138, "Není mono potvrdit", "Není moné potvrdit vıdejku"),
    NOT_POSIBLE_CONFIRM_BUY(139, "Není mono potvrdit", "Není moné potvrdit pøíjemku"),
    NOT_ENOUGHT_QUANTITY(140, "Nedostateèné mnoství", "Na skladì není dostatek zboí."), 
    NOT_ENOUGHT_CASH(141, "Nedostateèná hotovost", "Zadali jste nedostateènou èástku k zaplacení."),
    NOT_EQUALS_PASSWORD(142, "Heslo nesouhlasí", "Zadali jste chybné heslo."),
    BAD_LOGIN(143, "Pøístup zamítnut", "Zadali jste chybné pøihlašovací údaje."),
    NOT_POSIBLE_CONFIRM_STOCKING(144, "Není mono potvrdit", "Není moné potvrdit inventuru"),
    NOT_POSIBLE_CONFIRM_RECAP(145, "Není mono potvrdit", "Není moné potvrdit uzávìrku"),
    INCORECT_LICENSE(146, "Chybnı licenèní klíè", "Zadanı licenèní klíè je chybnı"),

    /* 200 - Chyby nastavení */
    LOOK_AND_FEEL(200, "Chyba vzhledu", "Nepodaøilo se nastavit vzhled programu. Bude pouito vıchozí nastavení. "),
    READ_SETTINGS(201, "Chyba nastavení", "Nepodaøilo se naèíst ze souboru nastavení programu."),
    WRITE_SETTINGS(202, "Chyba ukládání", "Nepodaøilo se uloit do souboru nastavení programu."),
    
    /* 300 - chyby pøi práci s databází */
    SQL_ERROR(300, "Chyba databáze", "Pøi práci s databází došlo k neoèekávané chybì. "),
    DUPL_KEY(301, "Ukládání selhalo", "Pokus o uloení dupliciní poloky do databáze. "),
    CON_FAILED(302, "Chyba pøipojení", "Pøipojení k databázi selhalo. "),
    BAD_JDBC(303, "Chybí ovladaè", "Nepodaøilo se naèíst potøebnı JDBC ovladaè pro pøipojení k databázi. "),
    CONNECTING_FAILED(303, "Neúspìšné pøipojení", "Nepodaøilo se pøipojit k databázi. Proveïte prosím korekci pøihlašovacích údajù."),
    DELETE_BANNED(304, "Vymazání zakázáno", "Není moné vymazat poloku, nebo k ní existuje odkaz."),
    
    /* 400 - chyby tisku */
    PRINT_ERROR(400, "Chyba tisku", "Poadovanou sestavu bohuel není mono vytisknout"),
    PRINT_COMPILE_ERROR(401, "Chyba pøípravy sestav", "Nepodaøilo se pøipravit šablony tiskovıch sestav. " +
            "Nìkteré sestavy nemusí bıt moné vytisknout."),
    
    /* 500 - chyby IO */
    FILE_IO_ERROR(500, "Chyba souboru", "Pøi práci ze souborem došlo k chybì"),
    SHA1_ERRO(501, "Chyba dekódování", "Došlo k chybì pøi ovìøení licenèního klíèe"),
    
    /** 600 - chyby licence */
    NOT_LICENSED(600, "Nelicencovaná verze", "Tato funkce je dostupná pouze v licencované verzi."),
    
    /* 2000 - Ladící chyby */
    NO_COMPLET_PART(2000, "Není dokonèeno", "Bohuel, tato funkce není ještì funkèí, bude dokonèena pozdìji.");
    
    private int code; // èíslo chyby 
    private String text = ""; // popis chyby
    private String textCode = ""; // textovı kód chyby  
    
    Errors(int code, String textCode, String text) {
        this.code = code;
        this.text = text;
        this.textCode = textCode;
    }
    

    /**
     * Vrací èíslo a popis chyby
     * @return Popis chyby
     */
    public String toString() {
        String result = "Chyba: " + getCode() + ". " + getCodeText() + " - " + getErrorText();
        
        return result;
    }

    /**
     * Vrací èíselnı kód chyby
     * @return èíselnı kód chyby
     */
    public int getCode() {
        
        return code;
    }
    
    /**
     * Vrací textovı kód chyby
     * @return textovı kód chyby
     */
    public String getCodeText() {
        return this.textCode;
    }
    
    /**
     * Vrací textovı popis chyby
     * @return textovı popis chyby
     */
    public String getErrorText() {
        return text;
    }
   
}
