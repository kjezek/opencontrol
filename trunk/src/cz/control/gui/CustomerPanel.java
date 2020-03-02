/*
 * CustomerPanel.java
 *
 * Vytvo�eno 7. listopad 2005, 20:19
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * Vytvo�� panel pro zobrazen� a editaci odb�ratel�
 *
 * (C) 2005, ver. 1.0
 */
public class CustomerPanel extends JPanel {

    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private Component owner;
    
    private User user; //U�ivatel, pro kter�ho byl panel vytvo�en
    private Customers customers = null; // ukazatel na dodavatele

    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastaven� seznamu */ 
    private JList list = new JList(); // JList s p�ehledem v�ech p��jemek
    
    private Customer lastSelectedCustomer = null;
    
    /* Komponenty pro zobrazan� podrobnost� o dodavateli */
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
     * Jm�no adres��e ve kter�m jsou ulo�eny ikony programu
     */
    public static final String ICON_URL = Settings.ICON_URL;

    
    /**
     * Vytvo�� nov� objekt SuplierPanel
     * 
     * @param owner Vlastn�k panelu
     * @param user u�ivatel, pro kter�ho byl panel vytvo�en
     */
    public CustomerPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        //test();
        
        try {
            customers = user.openCustomers(); /* Otev�i dodavatele pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.CUSTOMERS.getIndex(), false);
            return;

        }
        
        setPanel();
        
        refresh(); // Zobraz p�ehled p��jemek 
    }
    
    /**
     * Vytvo�� nov� objekt SuplierPanel
     * 
     * @param owner Vlastn�k panelu
     * @param user u�ivatel, pro kter�ho byl panel vytvo�en
     */
    public CustomerPanel(Dialog owner, User user) {
        this.user = user;
        this.owner = owner;
        //test();
        
        try {
            customers = user.openCustomers(); /* Otev�i dodavatele pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.CUSTOMERS.getIndex(), false);
            return;

        }
        
        setPanel();
        
        refresh(); // Zobraz p�ehled p��jemek 
    }
    
    /**
     *  Nastav� obecn� vlastnosti panelu
     */
    private void setPanel() {
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        this.setLayout( new BorderLayout() );
        
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s p�ehledem p��jemek
        this.add(createInfoPanel(), BorderLayout.CENTER); // Panel s podrobnostmi        
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
     * Vytvo�� panel s p�ehledem v�ech p��jemek 
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ehled odb�ratel�"));
        
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
        newButton = new JButton("Nov�", imageIcon);
        newButton.setToolTipText("Vytvo�� nov�ho odb�ratele");
        newButton.addActionListener( new NewBTNListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = CustomerPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zm�nu odb�ratele");
        editButton.addActionListener( new EditBTNListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = CustomerPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstran� odb�ratele");
        deleteButton.addActionListener( new DelBTNListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     *  Vytvo�� hlavn� panel s podrobnostmy o ododavateli
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
        
        
        // Prozat�m pr�zdn� panel - M�sto n�j budou dal�� funkce, nap� vyhled�n�
        //JPanel empty = new JPanel();
        //empty.setMinimumSize( new Dimension(250, 115));
        //empty.setPreferredSize( new Dimension(250, 115));
        //content.add(setComponent(empty, 0, 3, 2, 1, 1.0, 0.0, BOTH, WEST));

        return content;
    }
    
    /**
     *  Vytvo�� panel se z�kladn�mi �daji o dodavateli
     */
    private JPanel basicInfoPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        // Nastav jednotliv� vlastnosti komponent
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
        
        // Naskl�dej jednotliv� komponenty
        
        label = new JLabel(" N�zev: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(nameTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
        
        label = new JLabel(" I�O: ");
        content.add(setComponent(label, 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(icoTextField, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        label = new JLabel(" DI�: ");
        content.add(setComponent(label, 4, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(dicTextField, 5, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        return content;
    }
     
    /**
     *  Vytvo�� panel s adresou dodavatele
     */
    private JPanel addressPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Kontaktn� adresa"));

        // Tohle donut� Layout st�le nem�nit ���ku
        //content.setPreferredSize( new Dimension(300, 100));
        //content.setMinimumSize(new Dimension(300, 100));
        
        kontaktPersonLabel.setFont(font);
        label = new JLabel(" Kontaktn� osoba: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(kontaktPersonLabel, 1, 0, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        streetLabel.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetLabel, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        cityLabel.setFont(font);
        label = new JLabel(" M�sto: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        pscLabel.setFont(font);
        label = new JLabel(" PS�: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscLabel, 1, 3, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        return content;
    }
    
    /**
     *  Vytvo�� panel s faktura�n� adresou dodavatele
     */
    private JPanel addressPayPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Faktura�n� adresa"));

        content.setMinimumSize(new Dimension(300, 90));
        content.setPreferredSize( new Dimension(300, 90));
        
        streetPayLabel.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetPayLabel, 1, 0, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        cityPayLabel.setFont(font);
        label = new JLabel(" M�sto: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityPayLabel, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        pscPayLabel.setFont(font);
        label = new JLabel(" PS�: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscPayLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvo�� panel s kontaktn�mi informacemi o  dodavateli
     */
    private JPanel kontaktPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Kontakt"));

        // Tohle donut� Layout st�le nem�nit ���ku
        content.setMinimumSize(new Dimension(500, 115));
        // P�i vet��m okn� zp�sob� zv�t�en� p�esto�e se nem� roztahovat
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
     *  Vytvo�� panel s dal��mi informacemi o  dodavateli
     */
    private JPanel otherPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;

        isDPHBox.setFocusable(false);
        isDPHBox.setEnabled(false);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ostatn�"));
        
        accountLabel.setFont(font);
        label = new JLabel(" Bankovn� spojen�: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(accountLabel, 1, 0, 1, 1, 1.0, 0.0, NONE, WEST) );
        
        isDPHBox.setText("Pl�tce DPH");
        isDPHBox.setSelected(true);
        content.add( setComponent(isDPHBox, 0, 1, 1, 1, 0.0, 1.0, NONE, NORTHWEST) );
        
        return content;
    }    
    
    /**
     *  Panel s pozn�mkou
     */
    private JPanel notePanel() {
        JPanel content = new JPanel( new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Dal�� informace"));
        
        noteTextField.setEditable(false);
        Font font =  new Font("Dialog", Font.PLAIN, Settings.getMainItemsFontSize());
        noteTextField.setFont(font);
        noteTextField.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(noteTextField);
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }
     
    
    /**
     *  Zv�razn� konkr�tn�ho dodavatele
     */
    private void refresh(Customer customer) {
        refresh();
        list.setSelectedValue(customer, true);
        refreshCustomer(customer);
    }
    
    /**   
     *  Aktualizuje p�ehledy dodavatel�
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� p�ehled dodavatel� v datab�zi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<Customer> items = customers.getAllCustomers(); // Na�ti v�echny p�ehledy
//            list.setListData( items.toArray() ); // nelze pou��t, je t�eba vlo�it p�es listModel
                                                    // aby se data objevila i v ListModelu

            listModel.clear();
            for (Customer i: items) {
                getListModel().addElement(i);
            }
            
            // Ozna� naposledy ozna�en� ��dek. Jestli�e nebyl ��dn� ozna�en
            // nic se ned�j�, nebo� JList se zachov� korektn� a nic neozna��
            list.setSelectedIndex(row); 
            
            // Znovuzobraz je�t� dodavatele
            refreshCustomer( (Customer) list.getSelectedValue());
            
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Znovu zobraz� podrobn� informace o dodavateli
     */
    private void refreshCustomer(Customer customer) {
        
        if (customer == null)
            return;
        
        /* Z�kladn� info*/
        nameTextField.setText(customer.getName());
        icoTextField.setText(customer.getIco());
        dicTextField.setText(customer.getDic());
        
        /* Adresa*/
        kontaktPersonLabel.setText(customer.getPerson());
        streetLabel.setText(customer.getSendStreet());
        cityLabel.setText(customer.getSendCity());
        pscLabel.setText(customer.getSendPsc());
        
        /* Faktura�n� adresa */
        streetPayLabel.setText(customer.getPayStreet());
        cityPayLabel.setText(customer.getPayCity());
        pscPayLabel.setText(customer.getPayPsc());
        
        /* Kontakt*/
        telLabel.setText(customer.getTel());
        faxLabel.setText(customer.getFax());
        mailLabel.setText(customer.getMail());
        webLabel.setText(customer.getWeb());
        
        /* Ostatn� */
        accountLabel.setText(customer.getAccount());
        isDPHBox.setSelected(customer.isDph());
        noteTextField.setText(customer.getNote());
        
        /* Nastav Tool Tipy */
        nameTextField.setToolTipText("N�zev spole�nosti: " + nameTextField.getText());
        icoTextField.setToolTipText("I�O: " + icoTextField.getText());
        dicTextField.setToolTipText("DI�: " + dicTextField.getText());
    }
    
    /**
     * Ozna�� ��dek v�b�ru, ve kter�m je dodavatel s uveden�m ID
     * @param suplier dodavatel, kter� m� b�t ozna�en
     * @return true, jestli�e dodavatele nalezl a ozna�il ho, jinak false
     */
    public boolean highlightRow(Customer customer) {

        if (getListModel().contains(customer)) {
            getList().setSelectedValue(customer, true);
            return true;
        }

        return false;
    }
    
    /**
     *  Vytvo�� novou p��jemku -> p�ijme nov� zbo�� na sklad
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
        
        // Jestli�e neu�ivatel potvrdil dialog, skon�i
        if (newCustomer == null)
            return;
        
        // Vytvo� nov�ho dodavatele
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
        JOptionPane.showMessageDialog(this, "<html><center>Editace odb�ratele provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 

        // Obnov tento panel
        refresh(newCustomer);
        // Obnov i panel v hlavn�m okn�, kdyby n�hodou tento nebyl v hlavn�m okn�
        MainWindow.getInstance().getCustomerPanel().refresh(newCustomer);
        // Onnov zobrazen� v p��jemk�ch
        MainWindow.getInstance().getSalePanel().refresh();
    } 
    
    /**
     *  Edituje vybranou p��jemku
     */
    private void editItem() {
        Customer newCustomer;
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te odb�ratele, kter�ho chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        Customer cust  = (Customer) list.getSelectedValue();
        
        if (cust == null)
            return;
        
        
        // Otev�i dialog pro editaci dodavatele
        if (owner instanceof Frame)
            newCustomer = EditCustomerDialog.openDialog((Frame) owner, cust);
        else 
            newCustomer = EditCustomerDialog.openDialog((Dialog) owner, cust);
        
        // Jestli�e u�ivatel nepotvrdil dialog, zkon�i
        if (newCustomer == null)
            return;
        
        // Vytvo� nov�ho dodavatele
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
     *  Vyma�e ozna�enou polo�ku 
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� odb�ratele?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te odb�ratele, kter�ho chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� dodavatele",
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
            DatabaseAccess.setAutoCommit(false);
            // Vyber ozna�en� polo�ky 
            Object[] items =  list.getSelectedValues();
            
            // projdi pole a vyma� p��jemky
            for (int i = 0; i < items.length; i++) {
                Customer cust = (Customer) items[i];
                user.deleteCustomer(cust);
            }

            DatabaseAccess.commit();
            DatabaseAccess.setAutoCommit(true);
            
            refresh(); // Obnov v�b�r
            // Nastav pro zobrazen� pr�zdou polo�ku
            refreshCustomer( new Customer() ); // Obnov pro pr�zdn� objekt
            
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
     *  Stisk tla��tka pro nov�ho dodavatele
     */
    private class NewBTNListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            
            newItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro editaci dodavatele 
     */
    private class EditBTNListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro smaz�n� dodavatele
     */
    private class DelBTNListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    


    /**
     * Vrac� model JListu s dodavateli
     * @return model JListu dodavatel�
     */
    public DefaultListModel getListModel() {
        return listModel;
    }

    /**
     *  Vrac� JList s dodavateli
     *  @return JList s dodavateli
     */
    public JList getList() {
        return list;
    }

    /**
     *  Vrac� objekt pro pr�ci s dodavateli
     *  @return objekt pro pr�ci s dodavateli
     */
    public Customers getCustomers() {
        return customers;
    }


    /**
     *  Zm�na v list selection
     */
    private class ListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
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
