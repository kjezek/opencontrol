/*
 * Main.java
 *
 * Created on 13. záøí 2005, 19:00
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
 * Program Control - Skladový systém
 *
 * Hlavní tøída spouštìjící celý program. 
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class Main {

    /**
     *  Nastaví vzhled programu pøed spuštìním hlavního okna 
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
     *  Provede pøipojení k databázi. Jestliže se to nepovede, 
     *  zobrazí dialog pro nastavení pøístupových parametrù.
     *  Zkusí se pøipojit s novými parametry.
     * @param databaseName jméno databáze, ke které se pøipojí
     */
    private static void establishConnection(String databaseName) throws SQLException {
        
        while (true) {
            try { // Vytvoø spojení
                DatabaseAccess.establishConnection(
                        Settings.getDatabaseHost(),
                        Settings.getDatabaseUserName(),
                        Settings.getDatabaseUserPassword(),
                        databaseName);

                return;
            } catch (SQLException exception) {

                ErrorMessages er = new ErrorMessages(Errors.CONNECTING_FAILED,
                        "Není možné pøipojení k uživateli: \"<b>" + Settings.getDatabaseUserName() + "\"</b> " +
                        "na stanici: \"<b>" + Settings.getDatabaseURL() + "\"</b> ");
                JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            
                // Umožni znovunastavit pøihlašovací údaje
                if ( SettingsDialog.openSettingsDialog((Frame) null, SettingsDialog.SettingsItems.DATABASE.getIndex()) == false) {
                    throw exception; // Pøi nepotvrzení formuláøe propaguj vyjímku
                }
            
            } //end try
        } // end while
        
    }
    
    /**
     *  Naète MySQL ovladaè.
     *  Provede pøipojení do databáze a pøípadné vytvoøení tabulek
     */
    private static void connectToDatabase() throws Exception {
        try { // Zaveï ovladaè pro práci s databází 
            DatabaseAccess.loadJDBCDriver();   
        } catch (Exception e) {
            ErrorMessages er = new ErrorMessages(Errors.BAD_JDBC, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(null, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            throw e;
        }

        // pøipoj se bez urèení schéma
        establishConnection("");

        // pokud schéma existuje, pøipoj se k nìmu
        if (new Schemas().schemaExists()) {
            establishConnection(Settings.getDatabaseName());
        }
        
        try { // Zkontroluj a pøípadnì aktualizuj tabulky
            new DatabaseVersions().runUpdates();
        } catch (SQLException exception) {
            ErrorMessages er = new ErrorMessages(Errors.SQL_ERROR, "Popis chyby: <i>"
                    + exception.getLocalizedMessage() + "</i>");
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            throw exception;
        }

    }

    
    /**
     * @param args parametry pøíkazové øádky
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
