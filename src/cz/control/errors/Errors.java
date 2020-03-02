/*
 * Errors.java
 *
 * Created on 31. ��jen 2005, 20:17
 *
 */

package cz.control.errors;


/**
 * V��tov� typ p�edstavuj�c� chyby, kter� mohou nastat v programu 
 * @author Kamil
 */
public enum Errors {
    /* 0 - Obecn� chyby */
    NO_ERROR(0, "Bez chyby. ", ""),
    
    /* 100 - Chyby u�ivatelsk�ch vstup� */
    BAD_PRIVILEG(100, "P��stup zam�tnut", "Va�e u�ivatelsk� pr�va neumo��uj� prov�st tuto akci."),
    MISSING_FIELDS(130, "Nevypln�n� polo�ky", "Pro dokon�en� je t�eba vyplnit v�echny polo�ky. "),
    NO_GOODS_FOUND(131, "Zbo�� nenalezeno", "Zbo�� nebylo ve sklad� nalezeno. "),
    NO_SUPLIER_FOUND(132, "Dodavatel nenalezen", "Dodavatel nebyl nalezen. "),
    NO_CUSTOMER_FOUND(133, "Odb�ratel nenalezen", "Odb�ratel nebyl nalezen. "),
    NOT_EXEPT_VALUE(134, "Neo�ek�van� hodnota", "Chybn� zadan� hodnota."),
    NOT_EXEPT_DATE(135, "Neo�ek�van� datum", "Chybn� zadan� datum."),
    NO_SELECTED_VALUE(136, "Nen� nic vybr�no", "Nevybrali jste ��dnou polo�ku."),
    DUPLICIT_VALUE(137, "Duplicitn� hodnota", "Nen� mo�n� v�cekr�t vlo�it stejnou hodnotu."),
    NOT_POSIBLE_CONFIRM_SALE(138, "Nen� mo�no potvrdit", "Nen� mo�n� potvrdit v�dejku"),
    NOT_POSIBLE_CONFIRM_BUY(139, "Nen� mo�no potvrdit", "Nen� mo�n� potvrdit p��jemku"),
    NOT_ENOUGHT_QUANTITY(140, "Nedostate�n� mno�stv�", "Na sklad� nen� dostatek zbo��."), 
    NOT_ENOUGHT_CASH(141, "Nedostate�n� hotovost", "Zadali jste nedostate�nou ��stku k zaplacen�."),
    NOT_EQUALS_PASSWORD(142, "Heslo nesouhlas�", "Zadali jste chybn� heslo."),
    BAD_LOGIN(143, "P��stup zam�tnut", "Zadali jste chybn� p�ihla�ovac� �daje."),
    NOT_POSIBLE_CONFIRM_STOCKING(144, "Nen� mo�no potvrdit", "Nen� mo�n� potvrdit inventuru"),
    NOT_POSIBLE_CONFIRM_RECAP(145, "Nen� mo�no potvrdit", "Nen� mo�n� potvrdit uz�v�rku"),
    INCORECT_LICENSE(146, "Chybn� licen�n� kl��", "Zadan� licen�n� kl�� je chybn�"),

    /* 200 - Chyby nastaven� */
    LOOK_AND_FEEL(200, "Chyba vzhledu", "Nepoda�ilo se nastavit vzhled programu. Bude pou�ito v�choz� nastaven�. "),
    READ_SETTINGS(201, "Chyba nastaven�", "Nepoda�ilo se na��st ze souboru nastaven� programu."),
    WRITE_SETTINGS(202, "Chyba ukl�d�n�", "Nepoda�ilo se ulo�it do souboru nastaven� programu."),
    
    /* 300 - chyby p�i pr�ci s datab�z� */
    SQL_ERROR(300, "Chyba datab�ze", "P�i pr�ci s datab�z� do�lo k neo�ek�van� chyb�. "),
    DUPL_KEY(301, "Ukl�d�n� selhalo", "Pokus o ulo�en� duplicin� polo�ky do datab�ze. "),
    CON_FAILED(302, "Chyba p�ipojen�", "P�ipojen� k datab�zi selhalo. "),
    BAD_JDBC(303, "Chyb� ovlada�", "Nepoda�ilo se na��st pot�ebn� JDBC ovlada� pro p�ipojen� k datab�zi. "),
    CONNECTING_FAILED(303, "Ne�sp�n� p�ipojen�", "Nepoda�ilo se p�ipojit k datab�zi. Prove�te pros�m korekci p�ihla�ovac�ch �daj�."),
    DELETE_BANNED(304, "Vymaz�n� zak�z�no", "Nen� mo�n� vymazat polo�ku, nebo� k n� existuje odkaz."),
    
    /* 400 - chyby tisku */
    PRINT_ERROR(400, "Chyba tisku", "Po�adovanou sestavu bohu�el nen� mo�no vytisknout"),
    PRINT_COMPILE_ERROR(401, "Chyba p��pravy sestav", "Nepoda�ilo se p�ipravit �ablony tiskov�ch sestav. " +
            "N�kter� sestavy nemus� b�t mo�n� vytisknout."),
    
    /* 500 - chyby IO */
    FILE_IO_ERROR(500, "Chyba souboru", "P�i pr�ci ze souborem do�lo k chyb�"),
    SHA1_ERRO(501, "Chyba dek�dov�n�", "Do�lo k chyb� p�i ov��en� licen�n�ho kl��e"),
    
    /** 600 - chyby licence */
    NOT_LICENSED(600, "Nelicencovan� verze", "Tato funkce je dostupn� pouze v licencovan� verzi."),
    
    /* 2000 - Lad�c� chyby */
    NO_COMPLET_PART(2000, "Nen� dokon�eno", "Bohu�el, tato funkce nen� je�t� funk��, bude dokon�ena pozd�ji.");
    
    private int code; // ��slo chyby 
    private String text = ""; // popis chyby
    private String textCode = ""; // textov� k�d chyby  
    
    Errors(int code, String textCode, String text) {
        this.code = code;
        this.text = text;
        this.textCode = textCode;
    }
    

    /**
     * Vrac� ��slo a popis chyby
     * @return Popis chyby
     */
    public String toString() {
        String result = "Chyba: " + getCode() + ". " + getCodeText() + " - " + getErrorText();
        
        return result;
    }

    /**
     * Vrac� ��seln� k�d chyby
     * @return ��seln� k�d chyby
     */
    public int getCode() {
        
        return code;
    }
    
    /**
     * Vrac� textov� k�d chyby
     * @return textov� k�d chyby
     */
    public String getCodeText() {
        return this.textCode;
    }
    
    /**
     * Vrac� textov� popis chyby
     * @return textov� popis chyby
     */
    public String getErrorText() {
        return text;
    }
   
}
