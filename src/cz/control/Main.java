/*
 * Main.java
 *
 * Created on 13. z��� 2005, 19:00
 */

package cz.control;

import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.PageViewHit;
import cz.control.business.Schemas;
import cz.control.business.Settings;
import cz.control.database.DatabaseAccess;
import cz.control.database.DatabaseVersions;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.gui.MainWindow;
import cz.control.gui.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Program Control - Skladov� syst�m
 *
 * Hlavn� t��da spou�t�j�c� cel� program. 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class Main {

    /**
     *  Nastav� vzhled programu p�ed spu�t�n�m hlavn�ho okna 
     */
    private static void setLookAndFeel() {
        
        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            /* Nastav Look and Feel podle OS */
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            ErrorMessages er = new ErrorMessages(Errors.LOOK_AND_FEEL, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        //JFrame.setDefaultLookAndFeelDecorated(true);
        //JDialog.setDefaultLookAndFeelDecorated(true);
    }
    
    /**
     *  Provede p�ipojen� k datab�zi. Jestli�e se to nepovede, 
     *  zobraz� dialog pro nastaven� p��stupov�ch parametr�.
     *  Zkus� se p�ipojit s nov�mi parametry.
     * @param databaseName jm�no datab�ze, ke kter� se p�ipoj�
     */
    private static void establishConnection(String databaseName) throws SQLException {
        
        while (true) {
            try { // Vytvo� spojen�
                DatabaseAccess.establishConnection(
                        Settings.getDatabaseHost(),
                        Settings.getDatabaseUserName(),
                        Settings.getDatabaseUserPassword(),
                        databaseName);

                return;
            } catch (SQLException exception) {

                ErrorMessages er = new ErrorMessages(Errors.CONNECTING_FAILED,
                        "Nen� mo�n� p�ipojen� k u�ivateli: \"<b>" + Settings.getDatabaseUserName() + "\"</b> " +
                        "na stanici: \"<b>" + Settings.getDatabaseURL() + "\"</b> ");
                JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            
                // Umo�ni znovunastavit p�ihla�ovac� �daje
                if ( SettingsDialog.openSettingsDialog((Frame) null, SettingsDialog.SettingsItems.DATABASE.getIndex()) == false) {
                    throw exception; // P�i nepotvrzen� formul��e propaguj vyj�mku
                }
            
            } //end try
        } // end while
        
    }
    
    /**
     *  Na�te MySQL ovlada�.
     *  Provede p�ipojen� do datab�ze a p��padn� vytvo�en� tabulek
     */
    private static void connectToDatabase() throws Exception {
        try { // Zave� ovlada� pro pr�ci s datab�z� 
            DatabaseAccess.loadJDBCDriver();   
        } catch (Exception e) {
            ErrorMessages er = new ErrorMessages(Errors.BAD_JDBC, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(null, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            throw e;
        }

        // p�ipoj se bez ur�en� sch�ma
        establishConnection("");

        // pokud sch�ma existuje, p�ipoj se k n�mu
        if (new Schemas().schemaExists()) {
            establishConnection(Settings.getDatabaseName());
        }
        
        try { // Zkontroluj a p��padn� aktualizuj tabulky
            new DatabaseVersions().runUpdates();
        } catch (SQLException exception) {
            ErrorMessages er = new ErrorMessages(Errors.SQL_ERROR, "Popis chyby: <i>"
                    + exception.getLocalizedMessage() + "</i>");
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            throw exception;
        }

    }

    
    /**
     * @param args parametry p��kazov� ��dky
     */
    public static void main(String[] args) throws Exception {

        trackUsage();

        setLookAndFeel();
        try {
            Settings.loadSettings();
        } catch (Exception e) {
            ErrorMessages er = new ErrorMessages(Errors.READ_SETTINGS, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        connectToDatabase();

      
        MainWindow.openMainWindow();
        
    }


    /**
     * Track usage of application.
     */
    private static void trackUsage() {
        GoogleAnalytics ga = new GoogleAnalytics("UA-63201428-1");
        ga.postAsync(new PageViewHit("http://opencontrol-desktop.cz", "Open Control", "Desktop Client Activated"));
    }

}
