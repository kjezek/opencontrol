/*
 * EditAccountDialog.java
 *
 * Vytvo�eno 26. �nor 2006, 9:58
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialogov� okno pro editace, nebo zaveden� nov�ho u�ivatele 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class EditAccountDialog extends JDialog  implements WindowListener {
    
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    // Informace o u�ivateli
    private JTextField userNameTF = new JTextField();
    private JTextField userLoginNameTF = new JTextField();
    private JPasswordField userOldPasswordF = new JPasswordField();
    private JPasswordField userPasswordF = new JPasswordField();
    private JPasswordField userRetypePasswordF = new JPasswordField();
    
    //U�ivatelsk� pr�va
    private JRadioButton managerRB = new JRadioButton("Vedouc�");
    private JRadioButton storeManRB = new JRadioButton("Skladn�k");
    private JRadioButton cashRB = new JRadioButton("Pokladn�");
    
    private Component owner;
    private Client oldClient = null;
    
    private static Client result = null;
    
    /**
     * Vytvo�� novou instanci EditAccountDialog
     */
    private EditAccountDialog(Frame owner, Client oldClient) {
        super(owner, "Control - Editace u�ivatele", true);
        
        this.owner = owner;
        this.oldClient = oldClient;
        
        setDialog();
    }
    
    /**
     * Vytvo�� novou instanci EditAccountDialog
     */
    private EditAccountDialog(Dialog owner, Client oldClient) {
        super(owner, "Control - Editace u�ivatele", true);
        
        this.owner = owner;
        this.oldClient = oldClient;
        
        setDialog();
    }   
    
    /**
     * Otev�e dialog pro editaci u�ivatele 
     * @param owner vlastn�k diaogu
     * @return nov�ho klienta
     */
    public static Client openDialog(Dialog owner) {
        result = null;
        new EditAccountDialog(owner, null);
        return result;
    }
    
    /**
     * Otev�e dialog pro editaci u�ivatele 
     * @param owner vlastn�k diaogu
     * @return nov�ho klienta
     */
    public static Client openDialog(Frame owner) {
        result = null;
        new EditAccountDialog(owner, null);
        return result;
    }
    
    /**
     * Otev�e dialog pro editaci u�ivatele 
     * 
     * @return nov�ho klienta
     * @param oldClient star� klient, kter� se bude editovat
     * @param owner vlastn�k diaogu
     */
    public static Client openDialog(Frame owner, Client oldClient) {
        result = null;
        new EditAccountDialog(owner, oldClient);
        return result;
    }
    
    /**
     * Otev�e dialog pro editaci u�ivatele 
     * 
     * @return nov�ho klienta
     * @param oldClient star� klient, kter� se bude editovat
     * @param owner vlastn�k diaogu
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
     * Ud�lost aktivov�n� okna 
     * @param e Ud�lost okna
     */
    public void windowActivated(WindowEvent e) {}
    /**
     * Ud�lost zav�en� okna
     * @param e Ud�lost okna
     */
    public void windowClosed(WindowEvent e) {
    }
    /**
     * Ud�lost vyvolan� p�i zav�r�n� okna
     * Provede ulo�en� nastaven�
     * @param e Ud�lost okna
     */
    public void windowClosing(WindowEvent e) {
        cancel();
    }
    /**
     * Ud�lost deaktivov�n� okna
     * @param e Ud�lost okna
     */
    public void windowDeactivated(WindowEvent e) {}
    /**
     * ??
     * @param e Ud�lost okna
     */
    public void windowDeiconified(WindowEvent e) {}
    /**
     * Okno ikonizov�no
     * @param e Ud�lost okna
     */
    public void windowIconified(WindowEvent e) {}
    /**
     * Ud�lost otev�en� okna 
     * @param e Ud�lost okna
     */
    public void windowOpened(WindowEvent e) {}     
 
    
    /**
     * provede pot�ebn� nastaven� 
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
     *  Nastav� po��te�n� hodnoty dialogu
     */
    private void setInitialValues() {
        
        if (oldClient == null)
            return;
        
        userNameTF.setText(oldClient.getName());
        userLoginNameTF.setText(oldClient.getLoginName());
        
        // hesla nebude p�edvypl�ovat
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
      *  Vytvo�� obsah dialogu 
      */
    private Container createContent() {
        JPanel content = new JPanel(new BorderLayout());
        
        JPanel mainPart = new JPanel(); // Obsah hlavn�ho okna
        mainPart.setPreferredSize(new Dimension(300, 200));
        mainPart.add(createMainPanel(), BorderLayout.CENTER); // Vlo� ��st s edit boxy
        
        content.add(mainPart, BorderLayout.CENTER); // Vlo� panel s hlavn� ��st� okna 
        
        JPanel buttonPanel = new JPanel();
            
        JButton button = new JButton("Zru�it");
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
     * Vytvo�� hlavn� ��st dialogu
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�daje o u�ivateli"));
        mainPanel.setPreferredSize(new Dimension(300, 200));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(managerRB);
        buttonGroup.add(storeManRB);
        buttonGroup.add(cashRB);
        
        managerRB.setSelected(true);
     
        mainPanel.add(setComponent(new JLabel("Jm�no:"), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userNameTF, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("P�ihla�ovac� jm�no:"), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userLoginNameTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        // Na star� heslo se dotazuj pouze p�i editaci star�ho u�ivatele
        if (oldClient != null) {
            mainPanel.add(setComponent(new JLabel("Star� heslo:"), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
            mainPanel.add(setComponent(userOldPasswordF, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        }
        
        mainPanel.add(setComponent(new JLabel("Nov� heslo:"), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userPasswordF, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Potvrzen� hesla:"), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userRetypePasswordF, 1, 4, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        // Panel s nastaven�m u�ivatelsk�ch privilegi�
        JPanel privilegPanel = new JPanel();
        privilegPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "U�ivatelsk� pr�va"));
        privilegPanel.add(managerRB);
        privilegPanel.add(storeManRB);
        privilegPanel.add(cashRB);
        mainPanel.add(setComponent(privilegPanel, 0, 5, 2, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        
        return mainPanel;
    }
    
    /**
     *  Potvrd� dialog. 
     *  Provede kontrolu zadan�ch �daj� a nastav� nov�ho u�ivatele jako v�sledek dialogu
     */
    private void confirm() {
        String oldPassword = String.valueOf( userOldPasswordF.getPassword() ).trim();
        String newPassword = String.valueOf( userPasswordF.getPassword() ).trim();
        String retypePassword = String.valueOf( userRetypePasswordF.getPassword() ).trim();
        
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
        
        int clientType = ClientType.MANAGER.getOrdinal();
        
        // Zjisti o jak� privilegium se jedn�
        if (managerRB.isSelected()) {
            clientType = ClientType.MANAGER.getOrdinal();
        } else if (storeManRB.isSelected()) {
            clientType = ClientType.STORE_MAN.getOrdinal();
        } else if (cashRB.isSelected()) {
            clientType = ClientType.CASH.getOrdinal();
        }
        
        // Vytvo� nov�ho klienta
        result = new Client(0, 
                clientType,
                userNameTF.getText().trim(),
                userLoginNameTF.getText().trim(),
                newPassword
                );
        
        this.dispose();
    }
    
    /**
     * Zru�� dialog
     */
    private void cancel() {
        String text = "Zav��t okno bez ulo�en� zm�n?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Uzav�en� okna",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }  
        
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
