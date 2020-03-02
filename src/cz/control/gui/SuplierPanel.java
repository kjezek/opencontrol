/*
 * SuplierPanel.java
 *
 * Vytvo�eno 7. listopad 2005, 20:22
 *
 
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.database.DatabaseAccess;
import cz.control.data.Suplier;
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
 * Vytvo�� panel pro zobrazen� a editaci dodavatel�
 *
 * (C) 2005, ver. 1.0
 */
public class SuplierPanel extends JPanel {

    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private Component owner;
    
    private User user; //U�ivatel, pro kter�ho byl panel vytvo�en
    private Supliers supliers = null; // ukazatel na dodavatele

    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastaven� seznamu */ 
    private JList list = new JList(); // JList s p�ehledem v�ech p��jemek
    
    private Suplier lastSelectedSuplier = null;
    
    /* Komponenty pro zobrazan� podrobnost� o dodavateli */
    private JTextField nameTextField = new JTextField();
    private JTextField icoTextField = new JTextField();
    private JTextField dicTextField = new JTextField();
    
    private JLabel streetLabel = new JLabel();
    private JLabel cityLabel = new JLabel();
    private JLabel pscLabel = new JLabel();
    
    private JLabel kontaktPersonLabel = new JLabel();
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
    public SuplierPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        //test();
        
        try {
            supliers = user.openSupliers(); /* Otev�i dodavatele pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.SUPLIERS.getIndex(), false);
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
    public SuplierPanel(Dialog owner, User user) {
        this.user = user;
        this.owner = owner;
        //test();
        
        try {
            supliers = user.openSupliers(); /* Otev�i dodavatele pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.SUPLIERS.getIndex(), false);
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
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ehled dodavatel�"));
        
        getList().setModel(getListModel());
        getList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new ListingLSTListener());
        list.addKeyListener( new SuplierKeyListener() );
        list.addFocusListener( new ItemTableListener() );

        JScrollPane scrollPane = new JScrollPane(getList());
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 0.0, 1.0, VERTICAL, CENTER));
        
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        buttons.setMinimumSize( new Dimension(150, 115) );
        buttons.setPreferredSize( new Dimension(150, 115) );
        
        iconURL = SuplierPanel.class.getResource(ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Nov�", imageIcon);
        newButton.setToolTipText("Vytvo�� nov�ho dodavatele");
        newButton.addActionListener( new NewBTNListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = SuplierPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zm�nu dodavatele");
        editButton.addActionListener( new EditBTNListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = SuplierPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstran� dodavatele");
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
        content.add(setComponent(otherPanel(), 0, 2, 2, 1, 1.0, 0.0, BOTH, WEST));
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
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Adresa"));

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
    private void refresh(Suplier suplier) {
        refresh();
        list.setSelectedValue(suplier, true);
        refreshSuplier(suplier);
    }
    
    /**   
     *  Aktualizuje p�ehledy dodavatel�
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� p�ehled dodavatel� v datab�zi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<Suplier> items = getSupliers().getAllSupliers(); // Na�ti v�echny p�ehledy
//            list.setListData( items.toArray() ); // nelze pou��t, je t�eba vlo�it p�es listModel
                                                    // aby se data objevila i v ListModelu

            listModel.clear();
            for (Suplier i: items) {
                getListModel().addElement(i);
            }
            
            // Ozna� naposledy ozna�en� ��dek. Jestli�e nebyl ��dn� ozna�en
            // nic se ned�j�, nebo� JList se zachov� korektn� a nic neozna��
            list.setSelectedIndex(row); 
            
            // Znovuzobraz je�t� dodavatele
            refreshSuplier( (Suplier) list.getSelectedValue());
            
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(SuplierPanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Znovu zobraz� podrobn� informace o dodavateli
     */
    private void refreshSuplier(Suplier suplier) {
        
        if (suplier == null)
            return;
        
        /* Z�kladn� info*/
        nameTextField.setText(suplier.getName());
        icoTextField.setText(suplier.getIco());
        dicTextField.setText(suplier.getDic());
        
        /* Adresa*/
        kontaktPersonLabel.setText(suplier.getPerson());
        streetLabel.setText(suplier.getSendStreet());
        cityLabel.setText(suplier.getSendCity());
        pscLabel.setText(suplier.getSendPsc());
        
        /* Kontakt*/
        telLabel.setText(suplier.getTel());
        faxLabel.setText(suplier.getFax());
        mailLabel.setText(suplier.getMail());
        webLabel.setText(suplier.getWeb());
        
        /* Ostatn� */
        accountLabel.setText(suplier.getAccount());
        isDPHBox.setSelected(suplier.isDph());
        noteTextField.setText(suplier.getNote());
        
        /* Nastav Tool Tipy*/
        nameTextField.setToolTipText("N�zev spole�nosti: " + nameTextField.getText());
        icoTextField.setToolTipText("I�O: " + icoTextField.getText());
        dicTextField.setToolTipText("DI�: " + dicTextField.getText());

    }
    
    /**
     * Ozna�� ��dek v�b�ru, ve kter�m je dodavatel s uveden�m ID
     * @param suplier dodavatel, kter� m� b�t ozna�en
     * @return true, jestli�e dodavatele nalezl a ozna�il ho, jinak false
     */
    public boolean highlightRow(Suplier suplier) {

        if (getListModel().contains(suplier)) {
            getList().setSelectedValue(suplier, true);
            return true;
        }

        return false;
    }
    
    /**
     *  Vytvo�� novou p��jemku -> p�ijme nov� zbo�� na sklad
     */
    public void newItem() {
        Suplier newSuplier;
        
        if (owner instanceof Frame)
            newSuplier = EditSuplierDialog.openDialog((Frame) owner);
        else 
            newSuplier = EditSuplierDialog.openDialog((Dialog) owner);
        
        // Jestli�e neu�ivatel potvrdil dialog, skon�i
        if (newSuplier == null)
            return;
        
        // Vytvo� nov�ho dodavatele
        try {
            user.createSuplier(newSuplier);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }

        JOptionPane.showMessageDialog(this, "<html><center>Editace dodavatele provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
        
        // Obnov tento panel
        refresh(newSuplier);
        // Obnov i panel v hlavn�m okn�, kdyby n�hodou tento nebyl v hlavn�m okn�
        MainWindow.getInstance().getSuplierPanel().refresh(newSuplier);
        // Onnov zobrazen� ve p��jemk�ch
        MainWindow.getInstance().getBuyPanel().refresh();
    } 
    
    /**
     *  Edituje vybranou p��jemku
     */
    private void editItem() {
        Suplier newSuplier;
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te dodavatele, kter�ho chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        Suplier sup  = (Suplier) list.getSelectedValue();
        
        if (sup == null)
            return;
        
        
        // Otev�i dialog pro editaci dodavatele
        if (owner instanceof Frame)
            newSuplier = EditSuplierDialog.openDialog((Frame) owner, sup);
        else 
            newSuplier = EditSuplierDialog.openDialog((Dialog) owner, sup);
        
        // Jestli�e neu�ivatel potvrdil dialog, skon�i
        if (newSuplier == null)
            return;
        
        // Vytvo� nov�ho dodavatele
        try {
            user.editSuplier(sup, newSuplier);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        refresh(newSuplier);
        MainWindow.getInstance().getSuplierPanel().refresh(newSuplier);
    }
    
   /**
     *  Vyma�e ozna�enou polo�ku 
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� dodavatele?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te dodavatele, kter�ho chcete vymazat.");
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
                Suplier sup = (Suplier) items[i];
                user.deleteSuplier(sup);
            }
            
            DatabaseAccess.commit();
            DatabaseAccess.setAutoCommit(true);

            refresh(); // Obnov v�b�r
            // Nastav pro zobrazen� pr�zdou polo�ku
            refreshSuplier( new Suplier() ); // Obnov pro pr�zdn� objekt
            
            MainWindow.getInstance().getSuplierPanel().refresh();
            MainWindow.getInstance().getSuplierPanel().refresh( new Suplier());

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
    public Supliers getSupliers() {
        return supliers;
    }


    /**
     *  Zm�na v list selection
     */
    private class ListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastSelectedSuplier = (Suplier) list.getSelectedValue();
           refreshSuplier(lastSelectedSuplier);
        }
    } 
    
    private class SuplierKeyListener implements KeyListener {
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
