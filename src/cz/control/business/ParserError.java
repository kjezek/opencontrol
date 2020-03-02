/*
 * ParserError.java
 *
 * Created on 11. bøezen 2005, 0:05
 */

package cz.control.business;

import org.xml.sax.*;

/**

 *
 * @author Kamil Ježek
 *
 *  Trida pro osetreni chyb, ktere muzou nastat pri cteni parserem
 */
public class ParserError implements ErrorHandler {
    
    /*
     *  Zformatuje chybovy vypis 
     */
    private String errorMessages(SAXParseException e) {
        return e.getSystemId() + "\n" +
               "Radka: " + e.getLineNumber() +
               " Sloupec: " + e.getColumnNumber() + 
               "\n" + e.getMessage();
    }
    
    /**
     *  Vypise chybove hlaseni
     */
    public void warning(SAXParseException e) {
        
        System.err.println("Varovani: " + errorMessages(e));
    }

    /**
     *  Reakce na chybu hlaseni
     */
    
    public void error(SAXParseException e) throws SAXException {
        
        throw new SAXException("Chyba: " + errorMessages(e));
    }
    
    /**
     *  Reakce na fatalni chybu
     */
    public void fatalError(SAXParseException e) throws SAXException {
        
        throw new SAXException("Fatalni chyba: " + errorMessages(e));
    }
    
}