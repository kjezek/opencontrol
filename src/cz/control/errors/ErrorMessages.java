/*
 * ErrorMessages.java
 *
 * Vytvoøeno 31. øíjen 2005, 21:49
 *
 
 */

package cz.control.errors;

import com.mysql.jdbc.SQLError;
import java.sql.SQLException;

/**
 * Program Control - Skladový systém
 *
 * Tøída poskytující chybové texty 
 * @author Kamil Ježek
 * 
 * (C) 2005, ver. 1.0
 */
public class ErrorMessages {
    
    private int code; // èíslo chyby 
    private String text = ""; // popis chyby
    private String textCode = ""; // textový kód chyby  
    private String report = "";
    
    /**
     * Vyrvoøí nový objekt obsahující chybové zprávy
     * @param code kód chyby
     * @param textCode Textový kód chyby
     * @param text Popis chyby
     * @param completReport Podrobný popis chyby
     */
    private ErrorMessages(int code, String textCode, String text, String completReport) {
        this.code = code;
        this.textCode = textCode;
        this.text = text;
        this.report = completReport;
    }
    
    /**
     * Vyrvoøí nový objekt obsahující chybové zprávy
     * @param code Výètový typ Errors reprezentující vzniklou chybu
     * @param completReport Podrobný popis chyby
     */
    public ErrorMessages(Errors code, String completReport) {
        this.text = code.getErrorText();
        this.textCode = code.getCodeText();
        this.code = code.getCode();
        this.report = completReport;
    }
    
    /**
     * Vrací tøídu obsahující informace o vzniklé vyjímce pøi práci s databází
     * @param e chyba vrácená databází
     * @return Tøída obsahující informace o vzniklé vyjímce
     */
    public static ErrorMessages getDatabaseErrorMessages(SQLException e) {
        ErrorMessages result = new ErrorMessages(Errors.NO_ERROR, e.getLocalizedMessage());
        
        //System.out.println(e.getErrorCode());
        e.printStackTrace();
        
        switch (e.getErrorCode()) {
            case 0 : // chyba pøipojení 
                result = new ErrorMessages(Errors.CON_FAILED, e.getLocalizedMessage());
                break;
            case 1062 : // Duplicitní klíè 
                result = new ErrorMessages(Errors.DUPL_KEY, "Nìkteré hodnoty, jako tøeba skladová èísla, se nesmìjí v databázi opakovat");
                break;
            case 1217 : // Nedevolené smazání u foreign key
                result = new ErrorMessages(Errors.DELETE_BANNED, "Položku mùžete vymazat v pøípadì, že na ní není jinde odkazováno.");
                break;
            default : 
                e.printStackTrace(); // popis neoèekávané chyby vypiš na konzoli
                return new ErrorMessages(Errors.SQL_ERROR, e.getLocalizedMessage());
        }
        
        return result;
    }
    
    /**
     * Vrací tøídu obsahující informace o vzniklé vyjímce pøi chybném pøihlášení uživatele
     * @param e chyba vrácená vyjímkou
     * @return Tøída obsahující informace o vzniklé vyjímce
     */
    public static ErrorMessages getPrivilegErrorMessages(InvalidPrivilegException e) {
        ErrorMessages result = new ErrorMessages(Errors.BAD_PRIVILEG, e.getLocalizedMessage());
        
        return result;
    }
    
    /**
     * Vratí tøídu obsahující chybové výpisy 
     * @param e vyjímka, která byla vyvolána
     * @return tøída s informacemi o chybì
     */
    public static ErrorMessages getErrorMessages(Exception e) {
        
        if (e instanceof SQLException) {
            return getDatabaseErrorMessages((SQLException) e);
        } else 
        if (e instanceof InvalidPrivilegException) {
            return getPrivilegErrorMessages((InvalidPrivilegException) e);
        }
        
        e.printStackTrace(); // popis neoèekávané chyby vypiš na konzoli
        return new ErrorMessages(1, "Neoèekávaná chyba", "Pøi bìhu programu došlo k neoèekávané chybì ",
                e.getLocalizedMessage());
    }

    /**
     * Vrací èíslo vzniklé chyby
     * @return èíslo chyby
     */
    public int getError() {
        return code;
    }

    /**
     * vrací popis chyby
     * @return popis chyby
     */
    public String getText() {
        return text;
    }

    /**
     * Vrací textový kód chyby 
     * @return textový kód chyby
     */
    public String getTextCode() {
        return textCode;
    }
    
    /**
     * Vrací úhlednì zformátovaný text obsahující 
     * Popis chyby a detailní hlášení o chybì
     * @return poskytuje "hezky naformátovaný" textový popis chyby
     */
    public String getFormatedText() {
        return "<html><center>" +
                text + " <br> " + 
                report + 
                "</center></html>";
    }
    
    /**
     * Vrací popis chyby
     * @return Popis chyby
     */
    public String toString() {
        
        return "Chyba (" + code + "): " + textCode + " - " + text;
    }

    /**
     * Vrátí podrobné hlášení o vzniklé chybì
     * @return podrobné hlášení o chybì
     */
    public String getReport() {
        return report;
    }
   
    
}
