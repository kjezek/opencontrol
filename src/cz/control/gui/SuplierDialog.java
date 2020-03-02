/*
 * SuplierDialog.java
 *
 * Vytvo�eno 18. listopad 2005, 22:33
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
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialog dodavatel� a umo�n� vybrat a vlo�it dodavatele
 *
 * @author Kamil Je�ek
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
    
    /** Vytvo�� nov� objekt SuplierDialog */
    private SuplierDialog(Dialog owner, User user) {
        super(owner, "Control - Dodavatel�", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
    }
    
    /** Vytvo�� nov� objekt SuplierDialog */
    private SuplierDialog(Frame owner, User user) {
        super(owner, "Control - Dodavatel�", true);
        
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
    public static Suplier openDialog(Frame owner, User user) {
        new SuplierDialog(owner, user);
        return suplier;
    }
    
    /**
     * Vytvo�� dialogov� okno. Jestli�e u�ivatel vybere dodavatele
     * a potvrd� ho, vrac� instanci dodavatele, jinak null.
     * @param owner Vlastn�k dialogu 
     * @return P�i potvrzen� dialogu, vrac� dodavatele.
     *         P�i nepotvzen� vrac� null
     */
    public static Suplier openDialog(Dialog owner, User user) {
        new SuplierDialog(owner, user);
        return suplier;
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
        
        suplierPanel = new SuplierPanel(this, user);
        content.add(suplierPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� v�b�ru odb�ratele"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        iconURL = SuplierDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zru�it", imageIcon);
        button.setToolTipText("Zav�e okno a nic neprov�d�");
        button.addActionListener( new CancelButtonListener() );
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);
        
        iconURL = SuplierDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vybrat", imageIcon);
        button.setToolTipText("Vybere dodavatele a vlo�� ho do p��jemky.");
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
            suplier = (Suplier) suplierPanel.getList().getSelectedValue();
            
            if (suplier == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Vyberte dodavatele p��jemky.");
                JOptionPane.showMessageDialog(SuplierDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            SuplierDialog.this.dispose();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka zru�en� v�b�ru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            suplier = null;
            SuplierDialog.this.dispose();
        }
    }
 }
