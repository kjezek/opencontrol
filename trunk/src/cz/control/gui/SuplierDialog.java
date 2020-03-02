/*
 * SuplierDialog.java
 *
 * Vytvoøeno 18. listopad 2005, 22:33
 *
 
 */

package cz.control.gui;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.data.Suplier;
import cz.control.business.*;
import cz.control.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

import javax.swing.*;
import java.net.*;

/**
 * Program Control - Skladový systém
 *
 * Zobrazí dialog dodavatelé a umožní vybrat a vložit dodavatele
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */

/**
 *
 * @author Kamil
 */
public class SuplierDialog extends JDialog{
    
    private SuplierPanel suplierPanel;
    private Component owner;
    private static Suplier suplier = null;
    private User user;
    
    /** Vytvoøí nový objekt SuplierDialog */
    private SuplierDialog(Dialog owner, User user) {
        super(owner, "Control - Dodavatelé", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
    }
    
    /** Vytvoøí nový objekt SuplierDialog */
    private SuplierDialog(Frame owner, User user) {
        super(owner, "Control - Dodavatelé", true);
        
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
    public static Suplier openDialog(Frame owner, User user) {
        new SuplierDialog(owner, user);
        return suplier;
    }
    
    /**
     * Vytvoøí dialogové okno. Jestliže uživatel vybere dodavatele
     * a potvrdí ho, vrací instanci dodavatele, jinak null.
     * @param owner Vlastník dialogu 
     * @return Pøi potvrzení dialogu, vrací dodavatele.
     *         Pøi nepotvzení vrací null
     */
    public static Suplier openDialog(Dialog owner, User user) {
        new SuplierDialog(owner, user);
        return suplier;
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
        
        suplierPanel = new SuplierPanel(this, user);
        content.add(suplierPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení výbìru odbìratele"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        iconURL = SuplierDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zrušit", imageIcon);
        button.setToolTipText("Zavøe okno a nic neprovádí");
        button.addActionListener( new CancelButtonListener() );
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);
        
        iconURL = SuplierDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vybrat", imageIcon);
        button.setToolTipText("Vybere dodavatele a vloží ho do pøíjemky.");
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
            suplier = (Suplier) suplierPanel.getList().getSelectedValue();
            
            if (suplier == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Vyberte dodavatele pøíjemky.");
                JOptionPane.showMessageDialog(SuplierDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            SuplierDialog.this.dispose();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka zrušení výbìru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            suplier = null;
            SuplierDialog.this.dispose();
        }
    }
 }
