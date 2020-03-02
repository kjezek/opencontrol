/*
 * AccountManagerPanel.java
 *
 * Vytvo�eno 20. �nor 2006, 21:33
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.database.DatabaseAccess;
import cz.control.data.Client;
import cz.control.business.*;
import cz.control.gui.*;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;

import java.sql.SQLException;
import java.util.*;

import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;

/**
 * Program Control - Skladov� syst�m
 *
 * Panel s u�ivatelsk�m ��tem pro jednoho u�ivatele
 *
 * @author Kamil Je�ek
 */
public class AccountManagerPanel extends JPanel {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
   
    private JList list = new JList(); // JList s p�ehledem v�ech u�ivatel�
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastaven� seznamu */ 
    
    // Informace o u�ivateli
    private JTextField userNameTF = new JTextField();
    private JTextField userLoginNameTF = new JTextField();
    private JPasswordField userOldPasswordF = new JPasswordField();
    private JPasswordField userPasswordF = new JPasswordField();
    private JPasswordField userRetypePasswordF = new JPasswordField();
    private JLabel logedUserLabel = new JLabel();
    
    private Client lastSelectedClient = null;
    
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    
    private Component owner;
    private User user;
    private Account account;
    
    /**
     * Vytvo�� novou instanci AccountCommonPanel
     * @param owner vlastn�k panelu 
     * @param user u�ivatel, pro kter�ho byl panel vytvo�en
     */
    public AccountManagerPanel(Frame owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     * Vytvo�� novou instanci AccountCommonPanel
     * @param owner vlastn�k panelu 
     * @param user u�ivatel, pro kter�ho byl panel vytvo�en
     */
    public AccountManagerPanel(Dialog owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     *  Vytvo�� obsah panelu
     */
    private void createContent() {
    
        try {
            account = user.openAccount(); /* Otev�i u�ivatele pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.ACCOUNT.getIndex(), false);
            return;

        }
        
        this.setLayout( new BorderLayout() );
        
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s p�ehledem p��jemek
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotliv�mi polo�kami p��jemky 
        
        refresh();
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
     * Vytvo�� panel s p�ehledem v�ech u�ivatel�
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ehled u�ivatel�"));
        
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //list.addListSelectionListener( new buyListingLSTListener());
        list.addKeyListener( new AccountKeyListener() );
        list.addFocusListener( new ItemTableListener() );
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 0.0, 1.0, VERTICAL, CENTER));
        
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        buttons.setMinimumSize( new Dimension(150, 115) );
        buttons.setPreferredSize( new Dimension(150, 115) );
        
        iconURL = BuyPanel.class.getResource(ICON_URL + "NewAccount16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Nov�", imageIcon);
        newButton.setToolTipText("Vytvo�� nov�ho u�ivatele syst�mu");
        newButton.addActionListener( new NewClientListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = BuyPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zm�nu u�ivatele");
        editButton.addActionListener( new EditClientListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = BuyPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstran� u�ivatele");
        deleteButton.addActionListener( new DelClientListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
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
        content.add(setComponent(createTipPanel(), 0, 3, 1, 1, 1.0, 1.0, BOTH, NORTHWEST));
        
        return content;
    }    
    
    /**
     *  Vytvo�� panel s polo�kami pro editaci p�ihl�en�ho u�ivatele
     */
    private JPanel createEditPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�daje o p�ihl�en�m u�ivateli"));
        
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
        buttonPanel.setPreferredSize(new Dimension(250, 60));
        
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
     *  Vyp�e panel s tipem
     */
    private JPanel createTipPanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "N�pov�da"));
        content.setMinimumSize(new Dimension(500, 60));
        content.setMinimumSize(new Dimension(500, 170));
        
        label = new JLabel("<html><p>" +
                "Tip: " +
                "<ol>" +
                "<li>Pokud si p�ejete vyu��t u�ivatelsk� ��ty, v seznamu nalevo " +
                "vytvo�te tolik ��t�, kolik pot�ebujete." +
                "<li>Jednotliv�m u�ivatel�m nastavte p�ihla�ovac� �daje a u�ivatelsk� pr�va" +
                "<li>Pro spr�vnou funkci m�jte minim�ln� jednoho u�ivatele s opr�vn�n�m 'Vedouc�'" +
                "<li>Pole naho�e slou�� pro nastaven� �daj� pr�v� p�ihl�en�ho u�ivatele" +
                "<li>Pokud nechcete vyu��vat u�ivatelsk� ��ty, nechte seznam pr�zdn�. <br>Ka�d� u�ivatel pak " +
                "bude p�ihl�en s opr�vn�n�m 'Vedouc�'." +
                "</ol>" +
                "</p></html>"
                );
        
        content.add(setComponent(label, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        return content;
    }
    
    
    /**   
     *  Aktualizuje p�ehledy u�ivatel�
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� p�ehled u�ivatel� v datab�zi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<Client> items = account.getAllUser(); // Na�ti v�echny u�ivatele
            list.setListData( items.toArray() );
            
            list.setSelectedIndex(row); 
            
            if (lastSelectedClient != null) {
                refreshClient(lastSelectedClient);
            }
                
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
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
     *  Vytvo�� novou p��jemku -> p�ijme nov� zbo�� na sklad
     */
    public void newItem() {
        
        Client newClient = null;
        if (owner instanceof Frame)
            newClient = EditAccountDialog.openDialog( (Frame) owner);
        else 
            newClient = EditAccountDialog.openDialog( (Dialog) owner);
        
        // Jestli�e u�ivatel nepotvrdil v�b�r
        if (newClient == null)
            return;
        
        // Vytvo� nov�ho u�ivatele
        try {
            account.createUser(newClient);
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }        

        // Obnov v�b�r
        refresh();
    }
    
    /**
     *  Vyma�e ozna�enou p��jemku 
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� u�ivatele?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te ��et, kter� chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� u�ivatel�",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }
        
        /* Prove� fyzick� vymaz�n� */
        try {
            // Vyber ozna�en� polo�ky 
            Object[] items =  list.getSelectedValues();
            
            DatabaseAccess.setAutoCommit(false);
            // projdi pole a vyma� p��jemky
            for (int i = 0; i < items.length; i++) {
                Client client = (Client) items[i];
                account.deleteUser(client);
            }
            DatabaseAccess.commit();
            DatabaseAccess.setAutoCommit(true);

            refresh(); // Obnov v�b�r
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            try {
                DatabaseAccess.rollBack();
                DatabaseAccess.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return;
        }
         
    }
        
    /**
     *  Edituje vybranou p��jemku
     */
    private void editItem() {
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te u�ivatele, kter�ho chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        Client client = (Client) list.getSelectedValue();
        
        if (client == null)
            return;

        Client newClient = null;
        if (owner instanceof Frame)
            newClient = EditAccountDialog.openDialog( (Frame) owner, client);
        else 
            newClient = EditAccountDialog.openDialog( (Dialog) owner, client);
        
        // Jestli�e u�ivatel nepotvrdil dialog
        if (newClient == null)
            return;

        try {
            account.editUser(client, newClient);
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }        
        
        refresh();
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
            refresh();
            
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
     *  Stisk tla��tka pro vytvo�en� u�ivatele
     */
    private class NewClientListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro smaz�n� u�ivatele
     */
    private class DelClientListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro editaci u�ivatele 
     */
    private class EditClientListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            editItem();
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
    
    private class AccountKeyListener implements KeyListener {
        private boolean altPress = false;
        private boolean ctrlPress = false;
                
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DELETE :
                    deleteButton.doClick();
                    break;
                case KeyEvent.VK_ALT :
                    altPress = true;
                    break;
                case KeyEvent.VK_CONTROL :
                    ctrlPress = true;
                    break;
            }
            
            if (altPress && e.getKeyCode() == KeyEvent.VK_INSERT) {
                editButton.doClick();
                altPress = false;
                return;
            }
            
            if (!altPress && e.getKeyCode() == KeyEvent.VK_INSERT) {
                newButton.doClick();
                return;
            }
            
        }

        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ALT :
                    altPress = false;
                    break;
                case KeyEvent.VK_CONTROL :
                    ctrlPress = false;
                    break;
            }        
        }
    } 

        private class ItemTableListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.ACCOUNT_TIP);
        }

        public void focusLost(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.EMPTY);
        }
        
    }
}
