/*
 * CustomerPanel.java
 *
 * Vytvoøeno 7. listopad 2005, 20:19
 *
 * Autor: Kamil Ježek
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.database.DatabaseAccess;
import cz.control.data.Customer;
import javax.swing.*;
import java.net.*;

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
import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;

/**
 * Program Control - Skladový systém
 *
 * Vytvoøí panel pro zobrazení a editaci odbìratelù
 *
 * (C) 2005, ver. 1.0
 */
public class CustomerPanel extends JPanel {

    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private Component owner;
    
    private User user; //Uživatel, pro kterého byl panel vytvoøen
    private Customers customers = null; // ukazatel na dodavatele

    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastavenÝ seznamu */ 
    private JList list = new JList(); // JList s pøehledem všech pøíjemek
    
    private Customer lastSelectedCustomer = null;
    
    /* Komponenty pro zobrazaní podrobností o dodavateli */
    private JTextField nameTextField = new JTextField();
    private JTextField icoTextField = new JTextField();
    private JTextField dicTextField = new JTextField();
    
    private JLabel streetLabel = new JLabel();
    private JLabel cityLabel = new JLabel();
    private JLabel pscLabel = new JLabel();
    private JLabel kontaktPersonLabel = new JLabel();
    
    private JLabel streetPayLabel = new JLabel();
    private JLabel cityPayLabel = new JLabel();
    private JLabel pscPayLabel = new JLabel();
    
    private JLabel telLabel = new JLabel();
    private JLabel faxLabel = new JLabel();
    private JLabel mailLabel = new JLabel();
    private JLabel webLabel = new JLabel();
    
    private JLabel accountLabel = new JLabel();
    private JCheckBox isDPHBox = new JCheckBox();
    private JTextArea noteTextField = new JTextArea();
    
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    
    /**
     * Jméno adresáøe ve kterém jsou uloženy ikony programu
     */
    public static final String ICON_URL = Settings.ICON_URL;

    
    /**
     * Vytvoøí nový objekt SuplierPanel
     * 
     * @param owner Vlastník panelu
     * @param user uživatel, pro kterého byl panel vytvoøen
     */
    public CustomerPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        //test();
        
        try {
            customers = user.openCustomers(); /* Otevøi dodavatele pro pøihlášeného uživatele */
        } catch (InvalidPrivilegException e) {
            // Ukonèi zavádìní okna a nastav záložku jako nepøístupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.CUSTOMERS.getIndex(), false);
            return;

        }
        
        setPanel();
        
        refresh(); // Zobraz pøehled pøíjemek 
    }
    
    /**
     * Vytvoøí nový objekt SuplierPanel
     * 
     * @param owner Vlastník panelu
     * @param user uživatel, pro kterého byl panel vytvoøen
     */
    public CustomerPanel(Dialog owner, User user) {
        this.user = user;
        this.owner = owner;
        //test();
        
        try {
            customers = user.openCustomers(); /* Otevøi dodavatele pro pøihlášeného uživatele */
        } catch (InvalidPrivilegException e) {
            // Ukonèi zavádìní okna a nastav záložku jako nepøístupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.CUSTOMERS.getIndex(), false);
            return;

        }
        
        setPanel();
        
        refresh(); // Zobraz pøehled pøíjemek 
    }
    
    /**
     *  Nastaví obecní vlastnosti panelu
     */
    private void setPanel() {
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        this.setLayout( new BorderLayout() );
        
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s pøehledem pøíjemek
        this.add(createInfoPanel(), BorderLayout.CENTER); // Panel s podrobnostmi        
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
     * Vytvoøí panel s pøehledem všech pøíjemek 
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Pøehled odbìratelù"));
        
        getList().setModel(getListModel());
        getList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new ListingLSTListener());
        list.addKeyListener( new CustomerKeyListener() );
        list.addFocusListener( new ItemTableListener() );

        JScrollPane scrollPane = new JScrollPane(getList());
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 0.0, 1.0, VERTICAL, CENTER));
        
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        buttons.setMinimumSize( new Dimension(150, 115) );
        buttons.setPreferredSize( new Dimension(150, 115) );
        
        iconURL = CustomerPanel.class.getResource(ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Nový", imageIcon);
        newButton.setToolTipText("Vytvoží nového odbìratele");
        newButton.addActionListener( new NewBTNListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = CustomerPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zmìnu odbìratele");
        editButton.addActionListener( new EditBTNListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = CustomerPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstraní odbìratele");
        deleteButton.addActionListener( new DelBTNListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     *  Vytvoøí hlavní panel s podrobnostmy o ododavateli
     */
    private JPanel createInfoPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Informace o dodavateli"));
        
        content.add(setComponent(basicInfoPanel(), 0, 0, 2, 1, 1.0, 0.0, HORIZONTAL, WEST));
        content.add(setComponent(addressPanel(), 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(kontaktPanel(), 1, 1, 1, 1, 0.0, 0.0, BOTH, WEST));
        content.add(setComponent(addressPayPanel(), 0, 2, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(otherPanel(), 1, 2, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(notePanel(), 0, 3, 2, 1, 1.0, 1.0, BOTH, WEST));
        
        
        // Prozatím prázdný panel - Místo nìj budou další funkce, napø vyhledání
        //JPanel empty = new JPanel();
        //empty.setMinimumSize( new Dimension(250, 115));
        //empty.setPreferredSize( new Dimension(250, 115));
        //content.add(setComponent(empty, 0, 3, 2, 1, 1.0, 0.0, BOTH, WEST));

        return content;
    }
    
    /**
     *  Vytvoøí panel se základními údaji o dodavateli
     */
    private JPanel basicInfoPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        // Nastav jednotlivé vlastnosti komponent
        nameTextField.setFont(font);
        icoTextField.setFont(font);
        dicTextField.setFont(font);
        
        nameTextField.setEditable(false);
        icoTextField.setEditable(false);
        dicTextField.setEditable(false);

        nameTextField.setFocusable(false);
        icoTextField.setFocusable(false);
        dicTextField.setFocusable(false);
        
        nameTextField.setPreferredSize( new Dimension(500, 20) );
        icoTextField.setPreferredSize( new Dimension(150, 20) );
        icoTextField.setMinimumSize( new Dimension(150, 20) );
        dicTextField.setPreferredSize( new Dimension(150, 20) );
        dicTextField.setMinimumSize( new Dimension(150, 20) );
        
        // Naskládej jednotlivé komponenty
        
        label = new JLabel(" Název: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(nameTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
        
        label = new JLabel(" IÈO: ");
        content.add(setComponent(label, 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(icoTextField, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        label = new JLabel(" DIÈ: ");
        content.add(setComponent(label, 4, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(dicTextField, 5, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        return content;
    }
     
    /**
     *  Vytvoøí panel s adresou dodavatele
     */
    private JPanel addressPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Kontaktní adresa"));

        // Tohle donutí Layout stále nemìnit šíøku
        //content.setPreferredSize( new Dimension(300, 100));
        //content.setMinimumSize(new Dimension(300, 100));
        
        kontaktPersonLabel.setFont(font);
        label = new JLabel(" Kontaktní osoba: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(kontaktPersonLabel, 1, 0, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        streetLabel.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetLabel, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        cityLabel.setFont(font);
        label = new JLabel(" Mìsto: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        pscLabel.setFont(font);
        label = new JLabel(" PSÈ: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscLabel, 1, 3, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        return content;
    }
    
    /**
     *  Vytvoøí panel s fakturaèní adresou dodavatele
     */
    private JPanel addressPayPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Fakturaèní adresa"));

        content.setMinimumSize(new Dimension(300, 90));
        content.setPreferredSize( new Dimension(300, 90));
        
        streetPayLabel.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetPayLabel, 1, 0, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        cityPayLabel.setFont(font);
        label = new JLabel(" Mìsto: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityPayLabel, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        pscPayLabel.setFont(font);
        label = new JLabel(" PSÈ: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscPayLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvoøí panel s kontaktními informacemi o  dodavateli
     */
    private JPanel kontaktPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Kontakt"));

        // Tohle donutí Layout stále nemìnit šíøku
        content.setMinimumSize(new Dimension(500, 115));
        // Pøi vetším oknì zpùsobí zvìtšení pøestože se nemá roztahovat
        content.setPreferredSize( new Dimension(600, 115));
        
        telLabel.setFont(font);
        label = new JLabel(" Telefon: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(telLabel, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        faxLabel.setFont(font);
        label = new JLabel(" Fax: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(faxLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        mailLabel.setFont(font);
        label = new JLabel(" E-mail: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(mailLabel, 1, 3, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        webLabel.setFont(font);
        label = new JLabel(" Web: ");
        content.add( setComponent(label, 0, 4, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(webLabel, 1, 4, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvoøí panel s dalšími informacemi o  dodavateli
     */
    private JPanel otherPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;

        isDPHBox.setFocusable(false);
        isDPHBox.setEnabled(false);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ostatní"));
        
        accountLabel.setFont(font);
        label = new JLabel(" Bankovní spojení: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(accountLabel, 1, 0, 1, 1, 1.0, 0.0, NONE, WEST) );
        
        isDPHBox.setText("Plátce DPH");
        isDPHBox.setSelected(true);
        content.add( setComponent(isDPHBox, 0, 1, 1, 1, 0.0, 1.0, NONE, NORTHWEST) );
        
        return content;
    }    
    
    /**
     *  Panel s poznámkou
     */
    private JPanel notePanel() {
        JPanel content = new JPanel( new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Další informace"));
        
        noteTextField.setEditable(false);
        Font font =  new Font("Dialog", Font.PLAIN, Settings.getMainItemsFontSize());
        noteTextField.setFont(font);
        noteTextField.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(noteTextField);
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }
     
    
    /**
     *  Zvýrazní konkrétního dodavatele
     */
    private void refresh(Customer customer) {
        refresh();
        list.setSelectedValue(customer, true);
        refreshCustomer(customer);
    }
    
    /**   
     *  Aktualizuje pøehledy dodavatelù
     *  Tato metoda by mìla být volána, jestliže se zmìní pøehled dodavatelù v databázi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<Customer> items = customers.getAllCustomers(); // Naèti všechny pøehledy
//            list.setListData( items.toArray() ); // nelze použít, je tøeba vložit pøes listModel
                                                    // aby se data objevila i v ListModelu

            listModel.clear();
            for (Customer i: items) {
                getListModel().addElement(i);
            }
            
            // Oznaè naposledy oznaèený øádek. Jestliže nebyl žádný oznaèen
            // nic se nedìjì, nebo JList se zachová korektnì a nic neoznaèí
            list.setSelectedIndex(row); 
            
            // Znovuzobraz ještì dodavatele
            refreshCustomer( (Customer) list.getSelectedValue());
            
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Znovu zobrazí podrobné informace o dodavateli
     */
    private void refreshCustomer(Customer customer) {
        
        if (customer == null)
            return;
        
        /* Základní info*/
        nameTextField.setText(customer.getName());
        icoTextField.setText(customer.getIco());
        dicTextField.setText(customer.getDic());
        
        /* Adresa*/
        kontaktPersonLabel.setText(customer.getPerson());
        streetLabel.setText(customer.getSendStreet());
        cityLabel.setText(customer.getSendCity());
        pscLabel.setText(customer.getSendPsc());
        
        /* Fakturaèní adresa */
        streetPayLabel.setText(customer.getPayStreet());
        cityPayLabel.setText(customer.getPayCity());
        pscPayLabel.setText(customer.getPayPsc());
        
        /* Kontakt*/
        telLabel.setText(customer.getTel());
        faxLabel.setText(customer.getFax());
        mailLabel.setText(customer.getMail());
        webLabel.setText(customer.getWeb());
        
        /* Ostatní */
        accountLabel.setText(customer.getAccount());
        isDPHBox.setSelected(customer.isDph());
        noteTextField.setText(customer.getNote());
        
        /* Nastav Tool Tipy */
        nameTextField.setToolTipText("Název spoleènosti: " + nameTextField.getText());
        icoTextField.setToolTipText("IÈO: " + icoTextField.getText());
        dicTextField.setToolTipText("DIÈ: " + dicTextField.getText());
    }
    
    /**
     * Oznaèí øádek výbìru, ve kterém je dodavatel s uvedeným ID
     * @param suplier dodavatel, který má být oznaèen
     * @return true, jestliže dodavatele nalezl a oznaèil ho, jinak false
     */
    public boolean highlightRow(Customer customer) {

        if (getListModel().contains(customer)) {
            getList().setSelectedValue(customer, true);
            return true;
        }

        return false;
    }
    
    /**
     *  Vytvoøí novou pøíjemku -> pøijme nové zboží na sklad
     */
    public void newItem() {
        
        if (!MainWindow.getInstance()
                .getLicence().checkLicenseWithDialog(
                CustomerPanel.this)) {
            return;
        }
        
        Customer newCustomer;
        
        if (owner instanceof Frame)
            newCustomer = EditCustomerDialog.openDialog((Frame) owner);
        else 
            newCustomer = EditCustomerDialog.openDialog((Dialog) owner);
        
        // Jestliže neuživatel potvrdil dialog, skonèi
        if (newCustomer == null)
            return;
        
        // Vytvoø nového dodavatele
        try {
            user.createCustomer(newCustomer);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        JOptionPane.showMessageDialog(this, "<html><center>Editace odbìratele provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 

        // Obnov tento panel
        refresh(newCustomer);
        // Obnov i panel v hlavním oknì, kdyby náhodou tento nebyl v hlavním oknì
        MainWindow.getInstance().getCustomerPanel().refresh(newCustomer);
        // Onnov zobrazení v pøíjemkách
        MainWindow.getInstance().getSalePanel().refresh();
    } 
    
    /**
     *  Edituje vybranou pøíjemku
     */
    private void editItem() {
        Customer newCustomer;
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète odbìratele, kterého chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        Customer cust  = (Customer) list.getSelectedValue();
        
        if (cust == null)
            return;
        
        
        // Otevøi dialog pro editaci dodavatele
        if (owner instanceof Frame)
            newCustomer = EditCustomerDialog.openDialog((Frame) owner, cust);
        else 
            newCustomer = EditCustomerDialog.openDialog((Dialog) owner, cust);
        
        // Jestliže uživatel nepotvrdil dialog, zkonèi
        if (newCustomer == null)
            return;
        
        // Vytvoø nového dodavatele
        try {
            user.editCustomer(cust, newCustomer);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        refresh(newCustomer);
        MainWindow.getInstance().getCustomerPanel().refresh(newCustomer);
    }
    
   /**
     *  Vymaže oznaèenou položku 
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat oznaèené odbìratele?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète odbìratele, kterého chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání dodavatele",
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
            DatabaseAccess.setAutoCommit(false);
            // Vyber oznaèené položky 
            Object[] items =  list.getSelectedValues();
            
            // projdi pole a vymaž pøíjemky
            for (int i = 0; i < items.length; i++) {
                Customer cust = (Customer) items[i];
                user.deleteCustomer(cust);
            }

            DatabaseAccess.commit();
            DatabaseAccess.setAutoCommit(true);
            
            refresh(); // Obnov výbìr
            // Nastav pro zobrazení prázdou položku
            refreshCustomer( new Customer() ); // Obnov pro prázdný objekt
            
            MainWindow.getInstance().getCustomerPanel().refresh();
            MainWindow.getInstance().getCustomerPanel().refresh(new Customer());

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
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
         
    }
   
    /**
     *  Stisk tlaèítka pro nového dodavatele
     */
    private class NewBTNListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            
            newItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro editaci dodavatele 
     */
    private class EditBTNListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro smazání dodavatele
     */
    private class DelBTNListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    


    /**
     * Vrací model JListu s dodavateli
     * @return model JListu dodavatelù
     */
    public DefaultListModel getListModel() {
        return listModel;
    }

    /**
     *  Vrací JList s dodavateli
     *  @return JList s dodavateli
     */
    public JList getList() {
        return list;
    }

    /**
     *  Vrací objekt pro práci s dodavateli
     *  @return objekt pro práci s dodavateli
     */
    public Customers getCustomers() {
        return customers;
    }


    /**
     *  Zmìna v list selection
     */
    private class ListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // zábrání zdvojenému vyvolání uudálosti (význam mi není pøesnì znám)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastSelectedCustomer = (Customer) list.getSelectedValue();
           refreshCustomer(lastSelectedCustomer);
        }
    } 
    
    private class CustomerKeyListener implements KeyListener {
        private boolean altPress = false;
                
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
            }        
        }
        
    }    
    
    private class ItemTableListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.CUSTOMER_TIP);
        }

        public void focusLost(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.EMPTY);
        }
        
    }
    
  
    
}
