/*
 * LoginDialog.java
 *
 * Vytvo�eno 26. �nor 2006, 20:27
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.gui.about.ProgramInfoPanel;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidLoginException;
import cz.control.business.*;
import java.net.URL;

import java.sql.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialogov� okno p��hl�en� u�ivatele k syst�mu
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class LoginDialog extends JDialog {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
            
    private static Login login = null;
    
    private JTextField userLoginName = new JTextField();
    private JPasswordField userPassword = new JPasswordField();
    
    private JButton confirmButton;
    private JButton cancelButton;
    
    private Component owner;
    
    /**
     * Vytvo�� novou instanci LoginDialog
     */
    private LoginDialog(Frame owner) {
        super( owner, "Control - P�ihl�en�", true);
        
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� novou instanci LoginDialog
     */
    private LoginDialog(Dialog owner) {
        super( owner, "Control - P�ihl�en�", true);
        
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Otev�e dialog pro p�ihl�en�
     * @param owner Vlastn�k dialogu
     * @return objekt s p�ihl�en�m u�ivatelem
     */
    public static Login openDialog(Frame owner) {
    
        new LoginDialog(owner);
        
        return login;
    }
    
    /**
     * Otev�e dialog pro p�ihl�en�
     * @param owner Vlastn�k dialogu
     * @return objekt s p�ihl�en�m u�ivatelem
     */
    public static Login openDialog(Dialog owner) {
    
        new LoginDialog(owner);
        
        return login;
    }

    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
        setLocationByPlatform(true);
        
//        if (owner != null) {
//            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
//        }
        
        this.setAlwaysOnTop(true);
        setResizable(true);
        setPreferredSize(new Dimension(900, 500));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
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
     * Vytvo�� obsah okna
     */
    private JComponent getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        URL iconURL = null;
        Icon imageIcon = null;
        JButton button;

        content.add(ProgramInfoPanel.createOpenSourceInfo(), BorderLayout.NORTH);
        
        content.add(createMainPanel(), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        //buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen�"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        //iconURL = LoginDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        cancelButton = new JButton("Zru�it", imageIcon);
        cancelButton.setToolTipText("Nep��hl�s� se a ukon�� program");
        cancelButton.addActionListener( new CancelButtonListener() );
        cancelButton.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(cancelButton);
        
        //iconURL = LoginDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        confirmButton = new JButton("P�ihl�sit", imageIcon);
        confirmButton.setToolTipText("P�ihl�s� u�ivatele.");
        confirmButton.addActionListener( new ConfirmButtonListener() );
        confirmButton.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(confirmButton);
        
        return content;
    }    

    /**
     *  Vytvo�� hlavn� panel dialogu
     */
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ihla�ovac� �daje"));
        
        content.setToolTipText("<html>" +
                "Vyplnt� sv� <b>P�ihla�ovac� jm�no</b> a <b>Heslo</b>.<br>" +
                "Jestli�e nepou��v�te u�ivatelsk� ��ty do kolonky " +
                "<b>P�ihla�ovac� jm�no</b> vlo�te Va�e <b>Jm�no</b>." +
                "</html>");
        
        content.setPreferredSize(new Dimension(300, 300));
        content.setMinimumSize(new Dimension(300, 300));
        
        label = new JLabel("Jm�no: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));

        userLoginName.addKeyListener( new LoginKeyListener() );
        content.add(setComponent(userLoginName, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        label = new JLabel("Heslo: ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));

        userPassword.addKeyListener( new LoginKeyListener() );
        content.add(setComponent(userPassword, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        return content;
    }
    
    /**
     *  Poslucha� stisku tla��tka Potvrzen� v�b�ru  
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            String logName = userLoginName.getText().trim();
            String password = String.valueOf( userPassword.getPassword() ).trim();
            
            try {
                login = new Login(logName, password);
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(LoginDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (InvalidLoginException ex) {
                ErrorMessages er = new ErrorMessages(Errors.BAD_LOGIN, "" +
                        "Vlo�te spr�vn� <b>U�ivatelsk� jm�no</b> a <b>heslo</b>");
                JOptionPane.showMessageDialog(LoginDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }
            
            LoginDialog.this.dispose();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka zru�en� v�b�ru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            login = null;
            
            LoginDialog.this.dispose();
        }
    }    
    
    private class LoginKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    confirmButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE :
                    cancelButton.doClick();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }
    
}
