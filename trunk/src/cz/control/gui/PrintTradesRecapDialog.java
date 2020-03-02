/*
 * PrintTradesRecapDialog.java
 *
 * Vytvo�eno 16. kv�ten 2006, 20:20
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da otev�e dialog, kter� u�ivateli umo�n� vytisknout p�ehled 
 * obchodu (p��jemky/v�dejky). 
 * U�ivatel zad� parametry po��te�n� a koncov� datum a co si p�eje vytisknout.
 *
 * T��da provede tisk
 *
 *
 * @author Kamil Je�ek
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
    
    // panel se filtrem zobrazen�
    private JDateChooser startDate;
    private JDateChooser endDate;    
    
    private JCheckBox printBuy;
    private JCheckBox printSale;
    
    /**
     * Vytvo�� novou instanci 
     */
    private PrintTradesRecapDialog(Frame owner) {
        super(owner, "Tisk p�ehledu obchod�", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� novou instanci 
     */
    private PrintTradesRecapDialog(Dialog owner) {
        super(owner, "Tisk p�ehledu obchod�", true);
        this.owner = owner;
        setDialog();
    }    
    
    /**
     * Otev�e dialog
     * 
     * @param owner vlastn�k dialogu
     * @return potvrzen�
     */
    public static boolean openDiaog(Frame owner) {
        new PrintTradesRecapDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog
     * @param owner vlastn�k dialogu
     * @return potvrzen�
     */
    public static boolean  openDiaog(Dialog owner) {
        new PrintTradesRecapDialog(owner);
        return result;
    }
    
    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {

//        this.addWindowListener(this);

        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        // Nutno volat p�ed zji��ov�n�m velikosti dialogu
        pack();
        
        // Sou�adnice posunu nastav na st�ed
        int translateX = owner.getWidth() / 2 - this.getWidth() / 2;
        int translateY = owner.getHeight() / 2 - this.getHeight() / 2;
        
        setLocation( owner.getX() + translateX, owner.getY() + translateY);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        
        setVisible(true);
        
    }    
    
    /**
     *  Nastav� vlastnosti vkl�dan� komponenty 
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
     *  Vytvo�� vlastn� obsah okna
     */
    private JPanel getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.add(createMainPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);
        
        return content;
    }
    
    /**
     *  Vytvo�� hlavn� panel 
     */
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Parametry tisku"));
        content.setPreferredSize( new Dimension(500, 100));
        
        JCalendar calendar = new JCalendar();
        Calendar date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMinimum(Calendar.DAY_OF_MONTH)); //Za��tek m�s�ce
        startDate = new JDateChooser(calendar);
        startDate.setDate(date.getTime());
        
        calendar = new JCalendar();
        date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec m�s�ce
        endDate = new JDateChooser(calendar);
        endDate.setDate(date.getTime());
        
        content.add( setComponent(new JLabel("  Od: "), 0, 0, 1, 1, 0.0, 0.0, NONE, EAST) );
        content.add( setComponent(startDate, 1, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add( setComponent(new JLabel("  Do: ") , 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add( setComponent(endDate, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        printBuy = new JCheckBox("Tisknout p�ehled p��jemek", true);
        printSale = new JCheckBox("Tisknout p�ehled v�dejek", true);
        
        content.add( setComponent(printBuy, 0, 1, 4, 1, 1.0, 1.0, NONE, SOUTHWEST));
        content.add( setComponent(printSale, 0, 2, 4, 1, 1.0, 0.0, NONE, WEST));
        
        return content;
    }
    
    /**
     *  Vytvo�� spodn� panel s potvrzuj�c�m tla��tkem
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen�"));

        cancelButton = new JButton("Zru�it");
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
     *  Potvrd� dialog
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
     *  Zru�� dialog
     */
    private void cancel() {
    
        result = false;
        this.dispose();
    }    
    
  
    /**
     *  Poslucha� stisku tla��tka Potvzen� 
     */
    private class ConfirmButtonListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            confirm();
       }
    }   
    
   /**
     *  Poslucha� stisku tla��tka Zru�en� 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            cancel();
        }

    }   
    


}

