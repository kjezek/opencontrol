/*
 * CustomerDialog.java
 *
 * Vytvo�eno 13. �nor 2006, 23:13
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialog odb�ratele a umo�n� vybrat a vlo�it odb�ratele
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */

public class CustomerDialog extends JDialog{
    
    private CustomerPanel customerPanel;
    private Component owner;
    private static Customer customer = null;
    
    private User user;
    
    /** Vytvo�� nov� objekt customerDialog */
    private CustomerDialog(Dialog owner, User user) {
        super(owner, "Control - Odb�ratel�", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
    }
    
    /** Vytvo�� nov� objekt SuplierDialog */
    private CustomerDialog(Frame owner, User user) {
        super(owner, "Control - Odb�ratel�", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� dialogov� okno. Jestli�e u�ivatel vybere dodavatele
     * a potvrd� ho, vrac� instanci dodavatele, jinak null.
     * @param owner Vlastn�k dialogu 
     * @return P�i potvrzen� dialogu, vrac� dodavatele.
     *         P�i nepotvzen� vrac� null
     */
    public static Customer openDialog(Frame owner, User user) {
        new CustomerDialog(owner, user);
        return customer;
    }
    
    /**
     * Vytvo�� dialogov� okno. Jestli�e u�ivatel vybere dodavatele
     * a potvrd� ho, vrac� instanci dodavatele, jinak null.
     * @param owner Vlastn�k dialogu 
     * @return P�i potvrzen� dialogu, vrac� dodavatele.
     *         P�i nepotvzen� vrac� null
     */
    public static Customer openDialog(Dialog owner, User user) {
        new CustomerDialog(owner, user);
        return customer;
    }
    
    /*
     * provede pot�ebn� nastaven� 
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
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
        setVisible(true);
    }
    
    /*
     * Vytvo�� obsah okna
     */
    private JComponent getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        URL iconURL;
        Icon imageIcon;
        JButton button;
        
        customerPanel = new CustomerPanel(this, user);
        content.add(customerPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� v�b�ru dodavatele"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        iconURL = CustomerDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zru�it", imageIcon);
        button.setToolTipText("Zav�e okno a nic neprov�d�");
        button.addActionListener( new CancelButtonListener() );
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);
        
        iconURL = CustomerDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vybrat", imageIcon);
        button.setToolTipText("Pou�ije vybran�ho odb�ratele");
        button.addActionListener( new ConfirmButtonListener() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);
        
        return content;
    }
     
   /**
     *  Poslucha� stisku tla��tka Potvrzen� v�b�ru  
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            // zjisti vybranou polo�ku
            customer = (Customer) customerPanel.getList().getSelectedValue();
            
            if (customer == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte odb�ratele.");
                JOptionPane.showMessageDialog(CustomerDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            CustomerDialog.this.dispose();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka zru�en� v�b�ru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            customer = null;
            CustomerDialog.this.dispose();
        }
    }
 }