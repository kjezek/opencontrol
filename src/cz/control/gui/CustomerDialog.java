/*
 * CustomerDialog.java
 *
 * Vytvoøeno 13. únor 2006, 23:13
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.data.Customer;
import cz.control.business.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.net.*;

/**
 * Program Control - Skladový systém
 *
 * Zobrazí dialog odbìratele a umožní vybrat a vložit odbìratele
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */

public class CustomerDialog extends JDialog{
    
    private CustomerPanel customerPanel;
    private Component owner;
    private static Customer customer = null;
    
    private User user;
    
    /** Vytvoøí nový objekt customerDialog */
    private CustomerDialog(Dialog owner, User user) {
        super(owner, "Control - Odbìratelé", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
    }
    
    /** Vytvoøí nový objekt SuplierDialog */
    private CustomerDialog(Frame owner, User user) {
        super(owner, "Control - Odbìratelé", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvoøí dialogové okno. Jestliže uživatel vybere dodavatele
     * a potvrdí ho, vrací instanci dodavatele, jinak null.
     * @param owner Vlastník dialogu 
     * @return Pøi potvrzení dialogu, vrací dodavatele.
     *         Pøi nepotvzení vrací null
     */
    public static Customer openDialog(Frame owner, User user) {
        new CustomerDialog(owner, user);
        return customer;
    }
    
    /**
     * Vytvoøí dialogové okno. Jestliže uživatel vybere dodavatele
     * a potvrdí ho, vrací instanci dodavatele, jinak null.
     * @param owner Vlastník dialogu 
     * @return Pøi potvrzení dialogu, vrací dodavatele.
     *         Pøi nepotvzení vrací null
     */
    public static Customer openDialog(Dialog owner, User user) {
        new CustomerDialog(owner, user);
        return customer;
    }
    
    /*
     * provede potøebné nastavení 
     */
    private void setDialog() {
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        if (owner  != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(true);
        setMinimumSize(new Dimension(750, 450));
        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        setVisible(true);
    }
    
    /*
     * Vytvoøí obsah okna
     */
    private JComponent getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        URL iconURL;
        Icon imageIcon;
        JButton button;
        
        customerPanel = new CustomerPanel(this, user);
        content.add(customerPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení výbìru dodavatele"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        iconURL = CustomerDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zrušit", imageIcon);
        button.setToolTipText("Zavøe okno a nic neprovádí");
        button.addActionListener( new CancelButtonListener() );
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);
        
        iconURL = CustomerDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vybrat", imageIcon);
        button.setToolTipText("Použije vybraného odbìratele");
        button.addActionListener( new ConfirmButtonListener() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);
        
        return content;
    }
     
   /**
     *  Posluchaè stisku tlaèítka Potvrzení výbìru  
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            // zjisti vybranou položku
            customer = (Customer) customerPanel.getList().getSelectedValue();
            
            if (customer == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte odbìratele.");
                JOptionPane.showMessageDialog(CustomerDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            CustomerDialog.this.dispose();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka zrušení výbìru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            customer = null;
            CustomerDialog.this.dispose();
        }
    }
 }