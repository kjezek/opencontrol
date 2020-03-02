/*
 * EditAccountDialog.java
 *
 * Vytvoøeno 26. únor 2006, 9:58
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.data.ClientType;
import cz.control.data.Client;
import cz.control.business.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí dialogové okno pro editace, nebo zavedení nového uživatele 
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class EditAccountDialog extends JDialog  implements WindowListener {
    
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    // Informace o uživateli
    private JTextField userNameTF = new JTextField();
    private JTextField userLoginNameTF = new JTextField();
    private JPasswordField userOldPasswordF = new JPasswordField();
    private JPasswordField userPasswordF = new JPasswordField();
    private JPasswordField userRetypePasswordF = new JPasswordField();
    
    //Uživatelská práva
    private JRadioButton managerRB = new JRadioButton("Vedoucí");
    private JRadioButton storeManRB = new JRadioButton("Skladník");
    private JRadioButton cashRB = new JRadioButton("Pokladní");
    
    private Component owner;
    private Client oldClient = null;
    
    private static Client result = null;
    
    /**
     * Vytvoøí novou instanci EditAccountDialog
     */
    private EditAccountDialog(Frame owner, Client oldClient) {
        super(owner, "Control - Editace uživatele", true);
        
        this.owner = owner;
        this.oldClient = oldClient;
        
        setDialog();
    }
    
    /**
     * Vytvoøí novou instanci EditAccountDialog
     */
    private EditAccountDialog(Dialog owner, Client oldClient) {
        super(owner, "Control - Editace uživatele", true);
        
        this.owner = owner;
        this.oldClient = oldClient;
        
        setDialog();
    }   
    
    /**
     * Otevøe dialog pro editaci uživatele 
     * @param owner vlastník diaogu
     * @return nového klienta
     */
    public static Client openDialog(Dialog owner) {
        result = null;
        new EditAccountDialog(owner, null);
        return result;
    }
    
    /**
     * Otevøe dialog pro editaci uživatele 
     * @param owner vlastník diaogu
     * @return nového klienta
     */
    public static Client openDialog(Frame owner) {
        result = null;
        new EditAccountDialog(owner, null);
        return result;
    }
    
    /**
     * Otevøe dialog pro editaci uživatele 
     * 
     * @return nového klienta
     * @param oldClient starý klient, který se bude editovat
     * @param owner vlastník diaogu
     */
    public static Client openDialog(Frame owner, Client oldClient) {
        result = null;
        new EditAccountDialog(owner, oldClient);
        return result;
    }
    
    /**
     * Otevøe dialog pro editaci uživatele 
     * 
     * @return nového klienta
     * @param oldClient starý klient, který se bude editovat
     * @param owner vlastník diaogu
     */
    public static Client openDialog(Dialog owner, Client oldClient) {
        result = null;
        new EditAccountDialog(owner, oldClient);
        return result;
    }
    
    /*
     * Implementace metod z window listener
     */
    /**
     * Událost aktivování okna 
     * @param e Událost okna
     */
    public void windowActivated(WindowEvent e) {}
    /**
     * Událost zavøení okna
     * @param e Událost okna
     */
    public void windowClosed(WindowEvent e) {
    }
    /**
     * Událost vyvolaná pøi zavírání okna
     * Provede uložení nastavení
     * @param e Událost okna
     */
    public void windowClosing(WindowEvent e) {
        cancel();
    }
    /**
     * Událost deaktivování okna
     * @param e Událost okna
     */
    public void windowDeactivated(WindowEvent e) {}
    /**
     * ??
     * @param e Událost okna
     */
    public void windowDeiconified(WindowEvent e) {}
    /**
     * Okno ikonizováno
     * @param e Událost okna
     */
    public void windowIconified(WindowEvent e) {}
    /**
     * Událost otevøení okna 
     * @param e Událost okna
     */
    public void windowOpened(WindowEvent e) {}     
 
    
    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {
        this.addWindowListener(this);
        
        setContentPane(createContent());
        
        setInitialValues();
        
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); 
        pack();
        setVisible(true);
        
    }   
    
    /**
     *  Nastaví poèáteèní hodnoty dialogu
     */
    private void setInitialValues() {
        
        if (oldClient == null)
            return;
        
        userNameTF.setText(oldClient.getName());
        userLoginNameTF.setText(oldClient.getLoginName());
        
        // hesla nebude pøedvyplòovat
        //userOldPasswordF.setText(oldClient.getPassword());
        //userPasswordF.setText(oldClient.getPassword());
        //userRetypePasswordF.setText(oldClient.getPassword());
        
        if (oldClient.getType() == ClientType.MANAGER.getOrdinal()) {
            managerRB.setSelected(true);
        }
        if (oldClient.getType() == ClientType.STORE_MAN.getOrdinal()) {
            storeManRB.setSelected(true);
        }
        if (oldClient.getType() == ClientType.CASH.getOrdinal()) {
            cashRB.setSelected(true);
        }
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
      *  Vytvoøí obsah dialogu 
      */
    private Container createContent() {
        JPanel content = new JPanel(new BorderLayout());
        
        JPanel mainPart = new JPanel(); // Obsah hlavního okna
        mainPart.setPreferredSize(new Dimension(300, 200));
        mainPart.add(createMainPanel(), BorderLayout.CENTER); // Vlož èást s edit boxy
        
        content.add(mainPart, BorderLayout.CENTER); // Vlož panel s hlavní èástí okna 
        
        JPanel buttonPanel = new JPanel();
            
        JButton button = new JButton("Zrušit");
        button.addActionListener( new CancelButtonListener());
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);

        button = new JButton("Potvrdit");
        button.addActionListener( new ConfirmButtonListener());
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);
            
        content.add(buttonPanel, BorderLayout.SOUTH);
        return content;
    
    }     
    
    /**
     * Vytvoøí hlavní èást dialogu
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Údaje o uživateli"));
        mainPanel.setPreferredSize(new Dimension(300, 200));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(managerRB);
        buttonGroup.add(storeManRB);
        buttonGroup.add(cashRB);
        
        managerRB.setSelected(true);
     
        mainPanel.add(setComponent(new JLabel("Jméno:"), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userNameTF, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Pøihlašovací jméno:"), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userLoginNameTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        // Na staré heslo se dotazuj pouze pøi editaci starého uživatele
        if (oldClient != null) {
            mainPanel.add(setComponent(new JLabel("Staré heslo:"), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
            mainPanel.add(setComponent(userOldPasswordF, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        }
        
        mainPanel.add(setComponent(new JLabel("Nové heslo:"), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userPasswordF, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Potvrzení hesla:"), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userRetypePasswordF, 1, 4, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        // Panel s nastavením uživatelských privilegií
        JPanel privilegPanel = new JPanel();
        privilegPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Uživatelská práva"));
        privilegPanel.add(managerRB);
        privilegPanel.add(storeManRB);
        privilegPanel.add(cashRB);
        mainPanel.add(setComponent(privilegPanel, 0, 5, 2, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        
        return mainPanel;
    }
    
    /**
     *  Potvrdí dialog. 
     *  Provede kontrolu zadaných údajù a nastaví nového uživatele jako výsledek dialogu
     */
    private void confirm() {
        String oldPassword = String.valueOf( userOldPasswordF.getPassword() ).trim();
        String newPassword = String.valueOf( userPasswordF.getPassword() ).trim();
        String retypePassword = String.valueOf( userRetypePasswordF.getPassword() ).trim();
        
        // Jestliže uživatel nezadal hesla, zadáme mu je sami podle starého klienta
        if (oldClient != null &&
            oldPassword.length() == 0 &&
            newPassword.length() == 0 &&
            retypePassword.length() == 0) {
        
            oldPassword = newPassword = retypePassword = oldClient.getPassword();
        }
        
        // Zkontroluj správnost starého hesla
        if (oldClient != null && !oldPassword.equals(oldClient.getPassword()) ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_EQUALS_PASSWORD, 
                    "Staré heslo se neshoduje se zadáním.<br>" +
                    "Opravte prosím zadání.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        // Zkontroluj správnost starého hesla
        if (!newPassword.equals(retypePassword) ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_EQUALS_PASSWORD, 
                    "Nové heslo a potvrzení hesla se neshoduje .<br>" +
                    "Opravte prosím zadání.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        int clientType = ClientType.MANAGER.getOrdinal();
        
        // Zjisti o jaké privilegium se jedná
        if (managerRB.isSelected()) {
            clientType = ClientType.MANAGER.getOrdinal();
        } else if (storeManRB.isSelected()) {
            clientType = ClientType.STORE_MAN.getOrdinal();
        } else if (cashRB.isSelected()) {
            clientType = ClientType.CASH.getOrdinal();
        }
        
        // Vytvoø nového klienta
        result = new Client(0, 
                clientType,
                userNameTF.getText().trim(),
                userLoginNameTF.getText().trim(),
                newPassword
                );
        
        this.dispose();
    }
    
    /**
     * Zruší dialog
     */
    private void cancel() {
        String text = "Zavøít okno bez uložení zmìn?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Uzavøení okna",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }  
        
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
