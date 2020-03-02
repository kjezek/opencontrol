/*
 * ErrorMessages.java
 *
 * Vytvo�eno 31. ��jen 2005, 21:49
 *
 
 */

package cz.control.errors;

import com.mysql.jdbc.SQLError;
import java.sql.SQLException;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da poskytuj�c� chybov� texty 
 * @author Kamil Je�ek
 * 
 * (C) 2005, ver. 1.0
 */
public class ErrorMessages {
    
    private int code; // ��slo chyby 
    private String text = ""; // popis chyby
    private String textCode = ""; // textov� k�d chyby  
    private String report = "";
    
    /**
     * Vyrvo�� nov� objekt obsahuj�c� chybov� zpr�vy
     * @param code k�d chyby
     * @param textCode Textov� k�d chyby
     * @param text Popis chyby
     * @param completReport Podrobn� popis chyby
     */
    private ErrorMessages(int code, String textCode, String text, String completReport) {
        this.code = code;
        this.textCode = textCode;
        this.text = text;
        this.report = completReport;
    }
    
    /**
     * Vyrvo�� nov� objekt obsahuj�c� chybov� zpr�vy
     * @param code V��tov� typ Errors reprezentuj�c� vzniklou chybu
     * @param completReport Podrobn� popis chyby
     */
    public ErrorMessages(Errors code, String completReport) {
        this.text = code.getErrorText();
        this.textCode = code.getCodeText();
        this.code = code.getCode();
        this.report = completReport;
    }
    
    /**
     * Vrac� t��du obsahuj�c� informace o vznikl� vyj�mce p�i pr�ci s datab�z�
     * @param e chyba vr�cen� datab�z�
     * @return T��da obsahuj�c� informace o vznikl� vyj�mce
     */
    public static ErrorMessages getDatabaseErrorMessages(SQLException e) {
        ErrorMessages result = new ErrorMessages(Errors.NO_ERROR, e.getLocalizedMessage());
        
        //System.out.println(e.getErrorCode());
        e.printStackTrace();
        
        switch (e.getErrorCode()) {
            case 0 : // chyba p�ipojen� 
                result = new ErrorMessages(Errors.CON_FAILED, e.getLocalizedMessage());
                break;
            case 1062 : // Duplicitn� kl�� 
                result = new ErrorMessages(Errors.DUPL_KEY, "N�kter� hodnoty, jako t�eba skladov� ��sla, se nesm�j� v datab�zi opakovat");
                break;
            case 1217 : // Nedevolen� smaz�n� u foreign key
                result = new ErrorMessages(Errors.DELETE_BANNED, "Polo�ku m��ete vymazat v p��pad�, �e na n� nen� jinde odkazov�no.");
                break;
            default : 
                e.printStackTrace(); // popis neo�ek�van� chyby vypi� na konzoli
                return new ErrorMessages(Errors.SQL_ERROR, e.getLocalizedMessage());
        }
        
        return result;
    }
    
    /**
     * Vrac� t��du obsahuj�c� informace o vznikl� vyj�mce p�i chybn�m p�ihl�en� u�ivatele
     * @param e chyba vr�cen� vyj�mkou
     * @return T��da obsahuj�c� informace o vznikl� vyj�mce
     */
    public static ErrorMessages getPrivilegErrorMessages(InvalidPrivilegException e) {
        ErrorMessages result = new ErrorMessages(Errors.BAD_PRIVILEG, e.getLocalizedMessage());
        
        return result;
    }
    
    /**
     * Vrat� t��du obsahuj�c� chybov� v�pisy 
     * @param e vyj�mka, kter� byla vyvol�na
     * @return t��da s informacemi o chyb�
     */
    public static ErrorMessages getErrorMessages(Exception e) {
        
        if (e instanceof SQLException) {
            return getDatabaseErrorMessages((SQLException) e);
        } else 
        if (e instanceof InvalidPrivilegException) {
            return getPrivilegErrorMessages((InvalidPrivilegException) e);
        }
        
        e.printStackTrace(); // popis neo�ek�van� chyby vypi� na konzoli
        return new ErrorMessages(1, "Neo�ek�van� chyba", "P�i b�hu programu do�lo k neo�ek�van� chyb� ",
                e.getLocalizedMessage());
    }

    /**
     * Vrac� ��slo vznikl� chyby
     * @return ��slo chyby
     */
    public int getError() {
        return code;
    }

    /**
     * vrac� popis chyby
     * @return popis chyby
     */
    public String getText() {
        return text;
    }

    /**
     * Vrac� textov� k�d chyby 
     * @return textov� k�d chyby
     */
    public String getTextCode() {
        return textCode;
    }
    
    /**
     * Vrac� �hledn� zform�tovan� text obsahuj�c� 
     * Popis chyby a detailn� hl�en� o chyb�
     * @return poskytuje "hezky naform�tovan�" textov� popis chyby
     */
    public String getFormatedText() {
        return "<html><center>" +
                text + " <br> " + 
                report + 
                "</center></html>";
    }
    
    /**
     * Vrac� popis chyby
     * @return Popis chyby
     */
    public String toString() {
        
        return "Chyba (" + code + "): " + textCode + " - " + text;
    }

    /**
     * Vr�t� podrobn� hl�en� o vznikl� chyb�
     * @return podrobn� hl�en� o chyb�
     */
    public String getReport() {
        return report;
    }
   
    
}
