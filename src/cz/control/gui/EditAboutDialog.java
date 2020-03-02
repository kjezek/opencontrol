/*
 * EditAboutDialog.java
 *
 * Vytvoøeno 17. bøezen 2006, 20:34
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.data.Customer;
import cz.control.data.About;
import cz.control.business.*;
import cz.control.gui.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí dialogové okno pro editace informací o spoleènosti, která používá program
 *
 * @author Kamil Ježek
 *
 * (C) 2006, ver. 1.0
 */
public class EditAboutDialog extends JDialog implements WindowListener {
    
    private Component owner;
    
    /** Obsahuje objekt About, jestliže byl dialog potvrzen, jinak obsahuje null */
    private static About result = null; 
    
    /** Vstupní objekt, kterým se pøedvyplní dialog */
    private static About inputAbout = null;

    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    /* Komponenty pro zobrazaní podrobností */
    private JTextField nameTextField = new JTextField();
    private JTextField icoTextField = new JTextField();
    private JTextField dicTextField = new JTextField();
    
    private JTextField streetTextField = new JTextField();
    private JTextField cityTextField = new JTextField();
    private JTextField pscTextField = new JTextField();
    
    private JTextField streetPayTextField = new JTextField(20);
    private JTextField cityPayTextField = new JTextField(20);
    private JTextField pscPayTextField = new JTextField(20);
    
    private JTextField kontaktPersonTextField = new JTextField();
    private JTextField telTextField = new JTextField();
    private JTextField faxTextField = new JTextField();
    private JTextField mailTextField = new JTextField();
    private JTextField webTextField = new JTextField();
    
    private JTextField accountTextField = new JTextField();
    private JCheckBox isDPHBox = new JCheckBox();
    private JTextArea noteTextField = new JTextArea();
    
    private JTextField logoPathTextField = new JTextField();
    private JLabel logoPicture = new JLabel();
    
    private static String defaultDirectory = ".";
    
    /**
     * Vytvoøí nový dialog
     */
    private EditAboutDialog(Frame owner) {
        super(owner, "Editace informací o nás", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvoøí nový dialog
     */
    private EditAboutDialog(Dialog owner) {
        super(owner, "Editace informací o nás", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøený objekt About.
     * @return vytvoøeý objekt, jestliže byl dialog potvrzen.
     *          null - jestliže byl dialog nepotvrzen
     * @param owner Vlastník okna
     */
    public static About openDialog(Frame owner) {
        
        inputAbout = null;
        new EditAboutDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøený objekt About.
     * @return vytvoøeý objekt, jestliže byl dialog potvrzen.
     *          null - jestliže byl dialog nepotvrzen
     * @param owner Vlastník okna
     */
    public static About openDialog(Dialog owner) {
        
        inputAbout = null;
        new EditAboutDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøený objekt About.
     * @param owner Vlastník okna
     * @param about, objekt kterým se má dialog pøedvyplnit
     * @return vytvoøeý dodavatel, jestliže byl dialog potvrzen.
     * null - jestliže byl dialog nepotvrzen
     */
    public static About openDialog(Frame owner, About about) {
        
        inputAbout = about;
        new EditAboutDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøený objekt About.
     * @param owner Vlastník okna
     * @param about, objekt kterým se má dialog pøedvyplnit
     * @return vytvoøeý dodavatel, jestliže byl dialog potvrzen.
     * null - jestliže byl dialog nepotvrzen
     */
    public static About openDialog(Dialog owner, About about) {
        
        inputAbout = about;
        new EditAboutDialog(owner);
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
    
    /*
     * provede potøebné nastavení 
     */
    private void setDialog() {
        
        this.addWindowListener(this);
        
        result = null;
        setContentPane(createContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        setResizable(true);
        setMinimumSize(new Dimension(500, 380));

        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        
        // Pokuï byl zadán vstupní dodavatel, nastav potøebné hodnoty
        if (inputAbout  != null) {
            setInitialValues();
        }
        
        setVisible(true);
        
        inputAbout = null; // Napøíštì nastav na null
    }    
    
    /**
     * Nastaví vstupní hodnoty editaèním kompnentám
     */
    private void setInitialValues() {
        // Vyplò jednotlivá políèka 
        nameTextField.setText(inputAbout.getCustomer().getName());
        icoTextField.setText(inputAbout.getCustomer().getIco());
        dicTextField.setText(inputAbout.getCustomer().getDic());
        streetTextField.setText(inputAbout.getCustomer().getSendStreet());
        cityTextField.setText(inputAbout.getCustomer().getSendCity());
        pscTextField.setText(inputAbout.getCustomer().getSendPsc());
        streetPayTextField.setText(inputAbout.getCustomer().getPayStreet());
        cityPayTextField.setText(inputAbout.getCustomer().getPayCity());
        pscPayTextField.setText(inputAbout.getCustomer().getPayPsc());
        kontaktPersonTextField.setText(inputAbout.getCustomer().getPerson());
        telTextField.setText(inputAbout.getCustomer().getTel());
        faxTextField.setText(inputAbout.getCustomer().getFax());
        mailTextField.setText(inputAbout.getCustomer().getMail());
        webTextField.setText(inputAbout.getCustomer().getWeb());
        accountTextField.setText(inputAbout.getCustomer().getAccount());
        
        isDPHBox.setSelected( (inputAbout.getCustomer().isDph() ? true : false));        
        //logoPathTextField.setText(inputAbout.getLogoPath());
        noteTextField.setText(inputAbout.getCustomer().getNote());
        
        defaultDirectory = logoPathTextField.getText();
        ImageIcon imageIcon = new ImageIcon(inputAbout.getLogoPath());
        logoPicture.setIcon(imageIcon); 

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
     *  Vytvoøí základní obsah dialogu
     */
    private JPanel createContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.add(createInfoPanel(), BorderLayout.CENTER);

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
     *  Vytvoøí hlavní panel s podrobnostmy 
     */
    private JPanel createInfoPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Informace o nás"));
        
        content.add(setComponent(basicInfoPanel(), 0, 0, 2, 1, 1.0, 0.0, HORIZONTAL, WEST));
        content.add(setComponent(addressPanel(), 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(kontaktPanel(), 1, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(addressPayPanel(), 0, 2, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(otherPanel(), 1, 2, 2, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(notePanel(), 0, 3, 2, 1, 1.0, 1.0, BOTH, WEST));
        content.add(setComponent(picturePanel(), 0, 4, 2, 1, 1.0, 1.0, BOTH, WEST));
        
        // Prozatím prázdný panel - Místo nìj budou další funkce, napø vyhledání
        //JPanel empty = new JPanel();
        //empty.setMinimumSize( new Dimension(250, 115));
        //empty.setPreferredSize( new Dimension(250, 115));
        //content.add(setComponent(empty, 0, 3, 2, 1, 1.0, 0.0, BOTH, WEST));

        return content;
    }
    
    /**
     *  Vytvoøí panel se základními údaji o 
     */
    private JPanel basicInfoPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        // Nastav jednotlivé vlastnosti komponent
        //nameTextField.setFont( font );
        //icoTextField.setFont( font );
        //dicTextField.setFont( font );
        
        //nameTextField.setPreferredSize( new Dimension(600, 30) );
        icoTextField.setPreferredSize( new Dimension(100, 20) );
        icoTextField.setMinimumSize( new Dimension(100, 20) );
        dicTextField.setPreferredSize( new Dimension(100, 20) );
        dicTextField.setMinimumSize( new Dimension(100, 20) );
        
        // Naskládej jednotlivé komponenty
        
        label = new JLabel(" Název: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(nameTextField, 1, 0, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        label = new JLabel(" IÈO: ");
        content.add(setComponent(label, 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(icoTextField, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        label = new JLabel(" DIÈ: ");
        content.add(setComponent(label, 4, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(dicTextField, 5, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        return content;
    }
     
    /**
     *  Vytvoøí panel s adresou 
     */
    private JPanel addressPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Dodací adresa"));

        // Tohle donutí Layout stále nemìnit šíøku
        //content.setPreferredSize( new Dimension(400, 150));
        //content.setMinimumSize(new Dimension(400, 150));
        
        //kontaktPersonTextField.setFont(font);
        label = new JLabel(" Kontaktní osoba: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(kontaktPersonTextField, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //streetTextField.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetTextField, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //cityTextField.setFont(font);
        label = new JLabel(" Mìsto: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityTextField, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //pscTextField.setFont(font);
        label = new JLabel(" PSÈ: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscTextField, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        return content;
    }
    
   /**
     *  Vytvoøí panel s fakturaèní adresou 
     */
    private JPanel addressPayPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("SansSerif", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Fakturaèní adresa"));

        //content.setPreferredSize( new Dimension(260, 90));
        //content.setMinimumSize(new Dimension(260, 90));
        
        //streetPayTextField.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetPayTextField, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //cityPayTextField.setFont(font);
        label = new JLabel(" Mìsto: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityPayTextField, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //pscPayTextField.setFont(font);
        label = new JLabel(" PSÈ: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscPayTextField, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        return content;
    }    
     
    /**
     *  Vytvoøí panel s kontaktními informacemi o 
     */
    private JPanel kontaktPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Kontakt"));

        // Tohle donutí Layout stále nemìnit šíøku
        content.setPreferredSize( new Dimension(230, 115));
        content.setMinimumSize(new Dimension(230, 115));
        
        //telTextField.setFont(font);
        label = new JLabel(" Telefon: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(telTextField, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //faxTextField.setFont(font);
        label = new JLabel(" Fax: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(faxTextField, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //mailTextField.setFont(font);
        label = new JLabel(" E-mail: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(mailTextField, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //webTextField.setFont(font);
        label = new JLabel(" Web: ");
        content.add( setComponent(label, 0, 4, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(webTextField, 1, 4, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvoøí panel se zobrazením loga
     */
    private JPanel picturePanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        JButton button;

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Obrázek s logem"));
        
        logoPicture.setHorizontalAlignment(JLabel.CENTER);
        logoPicture.setVerticalAlignment(JLabel.CENTER);
        //logoPicture.setPreferredSize(new Dimension(200, 200));
        logoPicture.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JScrollPane scrollPane = new JScrollPane(logoPicture);
        content.add( setComponent(scrollPane, 0, 0, 3, 1, 1.0, 1.0, BOTH, CENTER) );
        
        label = new JLabel(" Soubor s logem: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        logoPathTextField.addActionListener( new LogoTextFieldActionListener());
        logoPathTextField.addFocusListener( new LogoTextFieldFocusListener() );
        content.add( setComponent(logoPathTextField, 1, 1, 1, 1, 1.0, 0.0, HORIZONTAL, WEST) );
        
        button = new JButton("Procházet");
        button.addActionListener( new OpenItemListener() );
        content.add( setComponent(button, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }
    
    /**
     *  Vytvoøí panel s dalšími informacemi  
     */
    private JPanel otherPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ostatní"));
        
        content.setPreferredSize( new Dimension(230, 90));
        content.setMinimumSize(new Dimension(230, 90));
        
        //accountTextField.setFont(font);
        label = new JLabel(" Bankovní spojení: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(accountTextField, 1, 0, 2, 1, 1.0, 0.0, HORIZONTAL, WEST) );
        
        isDPHBox.setText("Plátce DPH");
        isDPHBox.setSelected(true);
        content.add( setComponent(isDPHBox, 0, 1, 2, 1, 1.0, 1.0, NONE, NORTHWEST) );

        return content;
    }    
    
    /**
     *  Panel s poznámkou
     */
    private JPanel notePanel() {
        JPanel content = new JPanel( new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Další informace"));
        
        content.setPreferredSize( new Dimension(230, 100) );
        
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        noteTextField.setFont(font);
        JScrollPane scrollPane = new JScrollPane(noteTextField);
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }    
    
    private byte[] getImgData() {
        
        String logoPath = logoPathTextField.getText().trim();
        
        // Jestliže nebyl zadán obrázek, použij starý
        if (logoPath.length() == 0) {
            return inputAbout.getLogoPath();
        }
        
        // Naèti data ze souboru s obrázkem
        byte[] buffer = new byte[1024*1024];
        byte[] imgBytes = null;
        try {
            //Naèti data ze souboru
            BufferedInputStream bfr = new BufferedInputStream( new FileInputStream(logoPath) );
            
            int n = -1;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            while ( (n = bfr.read(buffer)) != -1) {
                byteStream.write(buffer, 0, n);
            }

            imgBytes = byteStream.toByteArray();
        } catch (IOException ex) {
            ErrorMessages er = new ErrorMessages(Errors.FILE_IO_ERROR, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return inputAbout.getLogoPath();
        }
        
        return imgBytes;
    }
    
    /**
     *  Potvrdí dialog a uzavøe ho.
     *  Do promìnné result uloží dodaatele
     */
    private void confirm() {
        
        // Vytvoø objekt podle zadaných hodnot
        Customer customer = new Customer(0,
                    nameTextField.getText().trim(), 
                    kontaktPersonTextField.getText().trim(), 
                    streetTextField.getText().trim(), 
                    cityTextField.getText().trim(), 
                    pscTextField.getText().trim(), 
                    streetPayTextField.getText().trim(), 
                    cityPayTextField.getText().trim(), 
                    pscPayTextField.getText().trim(), 
                    telTextField.getText().trim(), 
                    faxTextField.getText().trim(), 
                    mailTextField.getText().trim(), 
                    webTextField.getText().trim(), 
                    icoTextField.getText().trim(), 
                    dicTextField.getText().trim(), 
                    isDPHBox.isSelected(),
                    accountTextField.getText().trim(),
                    noteTextField.getText().trim()
                );

        
        byte[] imgBytes = getImgData();
        
        result = new About(customer, imgBytes);
        
        this.dispose();
    }
    
    /**
     *  Strornuje dialog a uzavøe ho
     *  Do promìnné result uloží null
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
        
        result = null;
        this.dispose();
    }
    
    /**
     *  Nataví obrázek s logem 
     */
    private void setLogoPicture() {
        defaultDirectory = logoPathTextField.getText().trim();
        ImageIcon imageIcon = new ImageIcon(defaultDirectory);
        logoPicture.setIcon(imageIcon); 
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
    
    /**
     *  Stisknuto tlaèítko Procházet 
     */
    private class OpenItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(); 
            
            fileChooser.setFileFilter(new PictureFileFilter()); /* Nastav filtr pro xml soubory*/
            fileChooser.setCurrentDirectory(new File(defaultDirectory)); /* Nastav ukazatel na aktuální adresáø */
            
            /* Jestliže byl soubor vybrán a potvrzen */
            if (fileChooser.showOpenDialog(EditAboutDialog.this) == JFileChooser.APPROVE_OPTION)  {
                String fileName = fileChooser.getSelectedFile().getPath(); /* Ulož jméno souboru */
                defaultDirectory = fileChooser.getSelectedFile().getPath(); /* Ulož jméno souboru */
                
                logoPathTextField.setText(fileName);
                setLogoPicture();
            }
        }
    }   
    
    /**
     *  Vytvoøi filtr povolující pouze .xml soubory
     */
    private class PictureFileFilter extends javax.swing.filechooser.FileFilter {
        
        public boolean accept(File file) {
            
            if (file.getName().toLowerCase().endsWith(".gif") == true ||
                file.getName().toLowerCase().endsWith(".png") == true ||
                file.getName().toLowerCase().endsWith(".jpg") == true ||
                file.isDirectory()) {
                return true;
            }
            return false;
        }
        
        public String getDescription() {
            
            return "JPG, PNG, a GIF obrázky";
        }
    } 
    
    /**
     *  Zmìna v názvu loga
     */
    private class LogoTextFieldActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setLogoPicture();
        }
        
    }
    
    /**
     *  Zmìna v názvu loga 
     */
    private class LogoTextFieldFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            setLogoPicture();
        }
        
    }
}
