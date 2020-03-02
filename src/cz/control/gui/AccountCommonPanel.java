/*
 * AccountCommonPanel.java
 *
 * Vytvo¯eno 20. ˙nor 2006, 20:50
 *
 * Autor: Kamil Jeûek
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
 * Program Control - Skladov˝ systÈm
 *
 * Panel s uûivatelsk˝m ˙Ëtem pro jednoho uûivatele
 *
 * @author Kamil Jeûek
 */
public class AccountCommonPanel extends JPanel {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    // Informace o uûivateli
    private JTextField userNameTF = new JTextField();
    private JTextField userLoginNameTF = new JTextField();
    private JPasswordField userOldPasswordF = new JPasswordField();
    private JPasswordField userPasswordF = new JPasswordField();
    private JPasswordField userRetypePasswordF = new JPasswordField();
    private JLabel logedUserLabel = new JLabel();   
    private Component owner;
    private User user;
    
    /**
     * Vytvo¯Ì novou instanci AccountCommonPanel
     * @param owner vlastnÌk panelu 
     * @param user uûivatel, pro kterÈho byl panel vytvo¯en
     */
    public AccountCommonPanel(Frame owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     * Vytvo¯Ì novou instanci AccountCommonPanel
     * @param owner vlastnÌk panelu 
     * @param user uûivatel, pro kterÈho byl panel vytvo¯en
     */
    public AccountCommonPanel(Dialog owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     *  Vytvo¯Ì obsah panelu
     */
    private void createContent() {
    
        this.setLayout( new BorderLayout() );
        
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotliv˝mi poloûkami p¯Ìjemky 
        
        refreshClient(user.getClient());
    }
    
    /**
     *  NastavÌ vlastnosti vkl·danÈ komponenty 
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
     * Vytvo¯Ì panel s podrobnostmi o uûivateli
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P¯ihl·öen˝ uûivatel"));
        
        Font font =  new Font("Serif", Font.BOLD, 25);
        logedUserLabel.setFont(font);
        
        content.add(setComponent(logedUserLabel, 0, 0, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createEditPanel(), 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createConfirmPanel(), 0, 2, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        
        return content;
    }    
    
    /**
     *  Vytvo¯Ì panel s poloûkami pro editaci p¯ihl·öenÈho uûivatele
     */
    private JPanel createEditPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "⁄daje o p¯ihl·öenÈm uûivateli"));
        mainPanel.setMinimumSize(new Dimension(550, 170));
        
        mainPanel.add(setComponent(new JLabel("JmÈno:"), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userNameTF, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("P¯ihlaöovacÌ jmÈno:"), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userLoginNameTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        mainPanel.add(setComponent(new JLabel("StarÈ heslo:"), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userOldPasswordF, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("NovÈ heslo:"), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userPasswordF, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("PotvrzenÌ hesla:"), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userRetypePasswordF, 1, 4, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        return mainPanel;
    }
    
    /**
     *  Vytvo¯Ì panel pro potvrzenÌ zmÏn
     */
    private JPanel createConfirmPanel() {
        JPanel buttonPanel = new JPanel();
        JButton button;
        URL iconURL;
        Icon imageIcon;
        
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "PotvrzenÌ zmÏn"));
        
        iconURL = AccountManagerPanel.class.getResource(Settings.ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("ZmÏnit", imageIcon);
        button.setToolTipText("PotvrdÌ zmÏnu hodnot");
        button.addActionListener( new ChangeCurrentUserBL() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);        
        
        return buttonPanel;
    }
    
    /**
     * Provede znovuzobrazenÌ hodnot v tomto panelu
     */
    public void refresh() {
        refreshClient(user.getClient());
    }
    
    /**
     *  Aktualizuje zobrazenÈho uûivatele
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
     *  ZmÏnÌ souËasnÈho klienta
     */
    private void changeCurrentUser() {
        String oldPassword = String.valueOf( userOldPasswordF.getPassword() ).trim();
        String newPassword = String.valueOf( userPasswordF.getPassword() ).trim();
        String retypePassword = String.valueOf( userRetypePasswordF.getPassword() ).trim();
        
        Client oldClient = user.getClient();
        
        // Jestliûe se jedn· o v˝chozÌ p¯ihl·öenÌ, nedovol cokoliv mÏnit
        // K tÈto situaci by vöak nemÏlo v tomto panelu nikdy dojÌt (neboù je
        // vytv·¯en pr·vÏ pro konkrÈtnÌho uûivatele)
        if (oldClient.getUserId() == -1) {
            JOptionPane.showMessageDialog(this, "" +
                    "<html><center>" +
                    "U v˝chozÌho p¯ihl·öenÌ nelze mÏnit ˙daje<br>" +
                    "Vytvo¯te uûivatelsk˝ ˙Ëet a u nÏj nastavte pot¯ebnÈ hodnoty" +
                    "</center></html>", 
                    "Nelze mÏnit", JOptionPane.ERROR_MESSAGE); 
            
            return;
        }
        
        // Jestliûe uûivatel nezadal hesla, zad·me mu je sami podle starÈho klienta
        if (oldClient != null &&
            oldPassword.length() == 0 &&
            newPassword.length() == 0 &&
            retypePassword.length() == 0) {
        
            oldPassword = newPassword = retypePassword = oldClient.getPassword();
        }
        
        // Zkontroluj spr·vnost starÈho hesla
        if (oldClient != null && !oldPassword.equals(oldClient.getPassword()) ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_EQUALS_PASSWORD, 
                    "StarÈ heslo se neshoduje se zad·nÌm.<br>" +
                    "Opravte prosÌm zad·nÌ.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        // Zkontroluj spr·vnost starÈho hesla
        if (!newPassword.equals(retypePassword) ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_EQUALS_PASSWORD, 
                    "NovÈ heslo a potvrzenÌ hesla se neshoduje .<br>" +
                    "Opravte prosÌm zad·nÌ.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }        
        
        // ProveÔ zmÏnu
        try {
            user.editUser(oldClient.getLoginName(),
                    userLoginNameTF.getText().trim(),
                    userNameTF.getText().trim(),
                    newPassword);

            JOptionPane.showMessageDialog(this, "" +
                    "<html><center>" +
                    "ZmÏna ˙daj˘ provedena<br>" +
                    "ZmÏna se projevÌ p¯i dalöÌm spuötÏnÌ programu" +
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
     *  Stisk tlaËÌtka pro potvrzenÌ zmÏn souËasnÈho klienta 
     */
    private class ChangeCurrentUserBL implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            changeCurrentUser();
        }
    }     
}
