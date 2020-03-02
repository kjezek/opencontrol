/*
 * AccountManagerPanel.java
 *
 * Vytvoøeno 20. únor 2006, 21:33
 *
 * Autor: Kamil Ježek
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
 * Program Control - Skladový systém
 *
 * Panel s uživatelským úètem pro jednoho uživatele
 *
 * @author Kamil Ježek
 */
public class AccountManagerPanel extends JPanel {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
   
    private JList list = new JList(); // JList s pøehledem všech uživatelù
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastavení seznamu */ 
    
    // Informace o uživateli
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
     * Vytvoøí novou instanci AccountCommonPanel
     * @param owner vlastník panelu 
     * @param user uživatel, pro kterého byl panel vytvoøen
     */
    public AccountManagerPanel(Frame owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     * Vytvoøí novou instanci AccountCommonPanel
     * @param owner vlastník panelu 
     * @param user uživatel, pro kterého byl panel vytvoøen
     */
    public AccountManagerPanel(Dialog owner, User user) {
        this.owner = owner;
        this.user = user;
        
        createContent();
    }
    
    /**
     *  Vytvoøí obsah panelu
     */
    private void createContent() {
    
        try {
            account = user.openAccount(); /* Otevøi uživatele pro pøihlášeného uživatele */
        } catch (InvalidPrivilegException e) {
            // Ukonèi zavádìní okna a nastav záložku jako nepøístupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.ACCOUNT.getIndex(), false);
            return;

        }
        
        this.setLayout( new BorderLayout() );
        
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s pøehledem pøíjemek
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotlivými položkami pøíjemky 
        
        refresh();
        refreshClient(user.getClient());
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
     * Vytvoøí panel s pøehledem všech uživatelù
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Pøehled uživatelù"));
        
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
        newButton = new JButton("Nový", imageIcon);
        newButton.setToolTipText("Vytvoøí nového uživatele systému");
        newButton.addActionListener( new NewClientListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = BuyPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zmìnu uživatele");
        editButton.addActionListener( new EditClientListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = BuyPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstraní uživatele");
        deleteButton.addActionListener( new DelClientListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     * Vytvoøí panel s podrobnostmi o uživateli
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Pøihlášený uživatel"));
        
        Font font =  new Font("Serif", Font.BOLD, 25);
        logedUserLabel.setFont(font);
        
        content.add(setComponent(logedUserLabel, 0, 0, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createEditPanel(), 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createConfirmPanel(), 0, 2, 1, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        content.add(setComponent(createTipPanel(), 0, 3, 1, 1, 1.0, 1.0, BOTH, NORTHWEST));
        
        return content;
    }    
    
    /**
     *  Vytvoøí panel s položkami pro editaci pøihlášeného uživatele
     */
    private JPanel createEditPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Údaje o pøihlášeném uživateli"));
        
        mainPanel.add(setComponent(new JLabel("Jméno:"), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userNameTF, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Pøihlašovací jméno:"), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userLoginNameTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));

        mainPanel.add(setComponent(new JLabel("Staré heslo:"), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userOldPasswordF, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Nové heslo:"), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userPasswordF, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        mainPanel.add(setComponent(new JLabel("Potvrzení hesla:"), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(userRetypePasswordF, 1, 4, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        return mainPanel;
    }
    
    /**
     *  Vytvoøí panel pro potvrzení zmìn
     */
    private JPanel createConfirmPanel() {
        JPanel buttonPanel = new JPanel();
        JButton button;
        URL iconURL;
        Icon imageIcon;
        
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení zmìn"));
        buttonPanel.setPreferredSize(new Dimension(250, 60));
        
        iconURL = AccountManagerPanel.class.getResource(Settings.ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zmìnit", imageIcon);
        button.setToolTipText("Potvrdí zmìnu hodnot");
        button.addActionListener( new ChangeCurrentUserBL() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);        
        
        return buttonPanel;
    }
    
    /**
     *  Vypíše panel s tipem
     */
    private JPanel createTipPanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Nápovìda"));
        content.setMinimumSize(new Dimension(500, 60));
        content.setMinimumSize(new Dimension(500, 170));
        
        label = new JLabel("<html><p>" +
                "Tip: " +
                "<ol>" +
                "<li>Pokud si pøejete využít uživatelské úèty, v seznamu nalevo " +
                "vytvoøte tolik úètù, kolik potøebujete." +
                "<li>Jednotlivým uživatelùm nastavte pøihlašovací údaje a uživatelská práva" +
                "<li>Pro správnou funkci mìjte minimálnì jednoho uživatele s oprávnìním 'Vedoucí'" +
                "<li>Pole nahoøe slouží pro nastavení údajù právì pøihlášeného uživatele" +
                "<li>Pokud nechcete využívat uživatelské úèty, nechte seznam prázdný. <br>Každý uživatel pak " +
                "bude pøihlášen s oprávnìním 'Vedoucí'." +
                "</ol>" +
                "</p></html>"
                );
        
        content.add(setComponent(label, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, NORTHWEST));
        
        return content;
    }
    
    
    /**   
     *  Aktualizuje pøehledy uživatelù
     *  Tato metoda by mìla být volána, jestliže se zmìní pøehled uživatelù v databázi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<Client> items = account.getAllUser(); // Naèti všechny uživatele
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
     *  Aktualizuje zobrazeného uživatele
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
     *  Vytvoøí novou pøíjemku -> pøijme nové zboží na sklad
     */
    public void newItem() {
        
        Client newClient = null;
        if (owner instanceof Frame)
            newClient = EditAccountDialog.openDialog( (Frame) owner);
        else 
            newClient = EditAccountDialog.openDialog( (Dialog) owner);
        
        // Jestliže uživatel nepotvrdil výbìr
        if (newClient == null)
            return;
        
        // Vytvoø nového uživatele
        try {
            account.createUser(newClient);
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }        

        // Obnov výbìr
        refresh();
    }
    
    /**
     *  Vymaže oznaèenou pøíjemku 
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat oznaèené uživatele?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète úèet, který chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání uživatelù",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }
        
        /* Proveï fyzické vymazání */
        try {
            // Vyber oznaèené položky 
            Object[] items =  list.getSelectedValues();
            
            DatabaseAccess.setAutoCommit(false);
            // projdi pole a vymaž pøíjemky
            for (int i = 0; i < items.length; i++) {
                Client client = (Client) items[i];
                account.deleteUser(client);
            }
            DatabaseAccess.commit();
            DatabaseAccess.setAutoCommit(true);

            refresh(); // Obnov výbìr
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
     *  Edituje vybranou pøíjemku
     */
    private void editItem() {
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète uživatele, kterého chcete upravit.");
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
        
        // Jestliže uživatel nepotvrdil dialog
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
     *  Zmìní souèasného klienta
     */
    private void changeCurrentUser() {
        String oldPassword = String.valueOf( userOldPasswordF.getPassword() ).trim();
        String newPassword = String.valueOf( userPasswordF.getPassword() ).trim();
        String retypePassword = String.valueOf( userRetypePasswordF.getPassword() ).trim();
        
        Client oldClient = user.getClient();
        
        // Jestliže se jedná o výchozí pøihlášení, nedovol cokoliv mìnit
        if (oldClient.getUserId() == -1) {
            JOptionPane.showMessageDialog(this, "" +
                    "<html><center>" +
                    "U výchozího pøihlášení nelze mìnit údaje<br>" +
                    "Vytvoøte uživatelský úèet a u nìj nastavte potøebné hodnoty" +
                    "</center></html>", 
                    "Nelze mìnit", JOptionPane.ERROR_MESSAGE); 
            
            return;
        }
        
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
        
        // Proveï zmìnu
        try {
            user.editUser(oldClient.getLoginName(),
                    userLoginNameTF.getText().trim(),
                    userNameTF.getText().trim(),
                    newPassword);
            refresh();
            
            JOptionPane.showMessageDialog(this, "" +
                    "<html><center>" +
                    "Zmìna údajù provedena<br>" +
                    "Zmìna se projeví pøi dalším spuštìní programu" +
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
     *  Stisk tlaèítka pro vytvoøení uživatele
     */
    private class NewClientListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro smazání uživatele
     */
    private class DelClientListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro editaci uživatele 
     */
    private class EditClientListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    }    
    
    /**
     *  Stisk tlaèítka pro potvrzení zmìn souèasného klienta 
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
