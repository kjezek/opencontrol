/*
 * PrintTradesRecapDialog.java
 *
 * Vytvoøeno 16. kvìten 2006, 20:20
 *
 * Autor: Kamil Jeek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.business.*;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.*;

import static java.awt.GridBagConstraints.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladovı systém
 *
 * Tøída otevøe dialog, kterı uivateli umoní vytisknout pøehled 
 * obchodu (pøíjemky/vıdejky). 
 * Uivatel zadá parametry poèáteèní a koncovı datum a co si pøeje vytisknout.
 *
 * Tøída provede tisk
 *
 *
 * @author Kamil Jeek
 *
 * (C) 2006, ver. 1.0
 */
public class PrintTradesRecapDialog extends JDialog {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
 
    private Component owner;
    private static boolean result;
    
    private JButton confirmButton;
    private JButton cancelButton;
    
    // panel se filtrem zobrazení
    private JDateChooser startDate;
    private JDateChooser endDate;    
    
    private JCheckBox printBuy;
    private JCheckBox printSale;
    
    /**
     * Vytvoøí novou instanci 
     */
    private PrintTradesRecapDialog(Frame owner) {
        super(owner, "Tisk pøehledu obchodù", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvoøí novou instanci 
     */
    private PrintTradesRecapDialog(Dialog owner) {
        super(owner, "Tisk pøehledu obchodù", true);
        this.owner = owner;
        setDialog();
    }    
    
    /**
     * Otevøe dialog
     * 
     * @param owner vlastník dialogu
     * @return potvrzení
     */
    public static boolean openDiaog(Frame owner) {
        new PrintTradesRecapDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog
     * @param owner vlastník dialogu
     * @return potvrzení
     */
    public static boolean  openDiaog(Dialog owner) {
        new PrintTradesRecapDialog(owner);
        return result;
    }
    
    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {

//        this.addWindowListener(this);

        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        // Nutno volat pøed zjišováním velikosti dialogu
        pack();
        
        // Souøadnice posunu nastav na støed
        int translateX = owner.getWidth() / 2 - this.getWidth() / 2;
        int translateY = owner.getHeight() / 2 - this.getHeight() / 2;
        
        setLocation( owner.getX() + translateX, owner.getY() + translateY);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        
        setVisible(true);
        
    }    
    
    /**
     *  Nastaví vlastnosti vkládané komponenty 
     */
    private Component setComponent(Component c, int x, int y, int s, int v, double rs, double rv, int fill, int anchor) {

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = s;
        gbc.gridheight = v;
        gbc.weightx = rs;
        gbc.weighty = rv;
        gbc.fill = fill;
        gbc.anchor = anchor;
        gbl.setConstraints(c, gbc);
        
        return c;
    } 
    
    
    /**
     *  Vytvoøí vlastní obsah okna
     */
    private JPanel getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.add(createMainPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);
        
        return content;
    }
    
    /**
     *  Vytvoøí hlavní panel 
     */
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Parametry tisku"));
        content.setPreferredSize( new Dimension(500, 100));
        
        JCalendar calendar = new JCalendar();
        Calendar date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMinimum(Calendar.DAY_OF_MONTH)); //Zaèátek mìsíce
        startDate = new JDateChooser(calendar);
        startDate.setDate(date.getTime());
        
        calendar = new JCalendar();
        date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec mìsíce
        endDate = new JDateChooser(calendar);
        endDate.setDate(date.getTime());
        
        content.add( setComponent(new JLabel("  Od: "), 0, 0, 1, 1, 0.0, 0.0, NONE, EAST) );
        content.add( setComponent(startDate, 1, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add( setComponent(new JLabel("  Do: ") , 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add( setComponent(endDate, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        printBuy = new JCheckBox("Tisknout pøehled pøíjemek", true);
        printSale = new JCheckBox("Tisknout pøehled vıdejek", true);
        
        content.add( setComponent(printBuy, 0, 1, 4, 1, 1.0, 1.0, NONE, SOUTHWEST));
        content.add( setComponent(printSale, 0, 2, 4, 1, 1.0, 0.0, NONE, WEST));
        
        return content;
    }
    
    /**
     *  Vytvoøí spodní panel s potvrzujícím tlaèítkem
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení"));

        cancelButton = new JButton("Zrušit");
        cancelButton.addActionListener( new CancelButtonListener());
        cancelButton.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(cancelButton);
        
        confirmButton = new JButton("Tisknout");
        confirmButton.addActionListener( new ConfirmButtonListener());
        confirmButton.setMnemonic(KeyEvent.VK_ENTER);
        content.add(confirmButton);
        
        confirmButton.requestFocus();
                
        return content;
    }
    
    
    /**
     *  Potvrdí dialog
     */
    private void confirm() {
    
        Date start = startDate.getDate();
        Date end = endDate.getDate();
        
        try {
            
            if (printBuy.isSelected()) {
                Print.printBuyRecap(start, end);
            }

            if (printSale.isSelected()) {
                Print.printSaleRecap(start, end);
            }

        } catch (JRException ex) {
            ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }

        result = true;
        
        
        this.dispose();
    }
    
    /**
     *  Zruší dialog
     */
    private void cancel() {
    
        result = false;
        this.dispose();
    }    
    
  
    /**
     *  Posluchaè stisku tlaèítka Potvzení 
     */
    private class ConfirmButtonListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            confirm();
       }
    }   
    
   /**
     *  Posluchaè stisku tlaèítka Zrušení 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            cancel();
        }

    }   
    


}

