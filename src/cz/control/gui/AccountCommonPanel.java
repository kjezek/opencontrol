/*
 * AccountCommonPanel.java
 *
 * Vytvo�eno 20. �nor 2006, 20:50
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.Client;
import cz.control.business.*;

import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;

import java.sql.SQLException;
import java.util.*;
import java.text.*;

import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;

/**
 * Program Control - Skladov� syst�m
 *
 * Panel s u�ivatelsk�m ��tem pro jednoho u�ivatele
 *
 * @author Kamil Je�ek
 */
public class AccountCommonPanel extends JPanel {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    // Informace o u�ivateli
    private JTextField userNameTF = new JTextField();
    private JTextField userLoginNameTF = new JTextField();
    private JPasswordField userOldPasswordF = new JPasswordField();
    private JPasswordField userPasswordF = new JPasswordField();
    private JPasswordField userRetypePasswordF = new JPasswordField();
    private JLabel logedUserLabel = new JLabel();   
    private Component owner;
    private User user;
    
    /**
     * Vytvo�� novou instanci AccountCommonPanel
     * @param owner vlastn�k panelu 
     * @param user u�ivatel, pro kter�ho byl panel vytvo�en
     */
    public AccountCommonPanel(Frame owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     * Vytvo�� novou instanci AccountCommonPanel
     * @param owner vlastn�k panelu 
     * @param user u�ivatel, pro kter�ho byl panel vytvo�en
     */
    public AccountCommonPanel(Dialog owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     *  Vytvo�� obsah panelu
     */
    private void createContent() {
    
        this.setLayout( new BorderLayout() );
        
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotliv�mi polo�kami p��jemky 
        
        refreshClient(user.getClient());
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
     * Vytvo�� panel s podrobnostmi o u�ivateli
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ihl�en� u�ivatel"));
        
        Font font =  new Font("Serif", Font.BOLD, 25);
        logedUserLabel.setFont(font);
        
        content.add(setComponent(logedUserLabel, 0, 0, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createEditPanel(), 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createConfirmPanel(), 0, 2, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        
        return content;
    }    
    
    /**
     *  Vytvo�� panel s polo�kami pro editaci p�ihl�en�ho u�ivatele
     */
    private JPanel createEditPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�daje o p�ihl�en�m u�ivateli"));
        mainPanel.setMinimumSize(new Dimension(550, 170));
        
        mainPanel.add(setComponent(new JLabel("Jm�no:"), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userNameTF, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("P�ihla�ovac� jm�no:"), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userLoginNameTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        mainPanel.add(setComponent(new JLabel("Star� heslo:"), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userOldPasswordF, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Nov� heslo:"), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userPasswordF, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Potvrzen� hesla:"), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userRetypePasswordF, 1, 4, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        return mainPanel;
    }
    
    /**
     *  Vytvo�� panel pro potvrzen� zm�n
     */
    private JPanel createConfirmPanel() {
        JPanel buttonPanel = new JPanel();
        JButton button;
        URL iconURL;
        Icon imageIcon;
        
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� zm�n"));
        
        iconURL = AccountManagerPanel.class.getResource(Settings.ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zm�nit", imageIcon);
        button.setToolTipText("Potvrd� zm�nu hodnot");
        button.addActionListener( new ChangeCurrentUserBL() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);        
        
        return buttonPanel;
    }
    
    /**
     * Provede znovuzobrazen� hodnot v tomto panelu
     */
    public void refresh() {
        refreshClient(user.getClient());
    }
    
    /**
     *  Aktualizuje zobrazen�ho u�ivatele
     */
    private void refreshClient(Client client) {
        if (client == null) {
            return;
        }
        
        userNameTF.setText(client.getName());
        userLoginNameTF.setText(client.getLoginName());
        
        logedUserLabel.setText(client.getName());
    }    
    
    /**
     *  Zm�n� sou�asn�ho klienta
     */
    private void changeCurrentUser() {
        String oldPassword = String.valueOf( userOldPasswordF.getPassword() ).trim();
        String newPassword = String.valueOf( userPasswordF.getPassword() ).trim();
        String retypePassword = String.valueOf( userRetypePasswordF.getPassword() ).trim();
        
        Client oldClient = user.getClient();
        
        // Jestli�e se jedn� o v�choz� p�ihl�en�, nedovol cokoliv m�nit
        // K t�to situaci by v�ak nem�lo v tomto panelu nikdy doj�t (nebo� je
        // vytv��en pr�v� pro konkr�tn�ho u�ivatele)
        if (oldClient.getUserId() == -1) {
            JOptionPane.showMessageDialog(this, "" +
                    "<html><center>" +
                    "U v�choz�ho p�ihl�en� nelze m�nit �daje<br>" +
                    "Vytvo�te u�ivatelsk� ��et a u n�j nastavte pot�ebn� hodnoty" +
                    "</center></html>", 
                    "Nelze m�nit", JOptionPane.ERROR_MESSAGE); 
            
            return;
        }
        
        // Jestli�e u�ivatel nezadal hesla, zad�me mu je sami podle star�ho klienta
        if (oldClient != null &&
            oldPassword.length() == 0 &&
            newPassword.length() == 0 &&
            retypePassword.length() == 0) {
        
            oldPassword = newPassword = retypePassword = oldClient.getPassword();
        }
        
        // Zkontroluj spr�vnost star�ho hesla
        if (oldClient != null && !oldPassword.equals(oldClient.getPassword()) ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_EQUALS_PASSWORD, 
                    "Star� heslo se neshoduje se zad�n�m.<br>" +
                    "Opravte pros�m zad�n�.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        // Zkontroluj spr�vnost star�ho hesla
        if (!newPassword.equals(retypePassword) ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_EQUALS_PASSWORD, 
                    "Nov� heslo a potvrzen� hesla se neshoduje .<br>" +
                    "Opravte pros�m zad�n�.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }        
        
        // Prove� zm�nu
        try {
            user.editUser(oldClient.getLoginName(),
                    userLoginNameTF.getText().trim(),
                    userNameTF.getText().trim(),
                    newPassword);

            JOptionPane.showMessageDialog(this, "" +
                    "<html><center>" +
                    "Zm�na �daj� provedena<br>" +
                    "Zm�na se projev� p�i dal��m spu�t�n� programu" +
                    "</center></html>", 
                    "Ok", JOptionPane.INFORMATION_MESSAGE); 
            
       } catch (InvalidPrivilegException exception) {
           ErrorMessages er = ErrorMessages.getErrorMessages(exception);
           JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
           return;
       } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
       }        
    }
 
    /**
     *  Stisk tla��tka pro potvrzen� zm�n sou�asn�ho klienta 
     */
    private class ChangeCurrentUserBL implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            changeCurrentUser();
        }
    }     
}
