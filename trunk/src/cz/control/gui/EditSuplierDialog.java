/*
 * EditSuplierDialog.java
 *
 * Vytvoøeno 9. únor 2006, 18:45
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.data.Suplier;
import cz.control.business.*;
import cz.control.gui.*;
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
 * Tøída vytváøí dialogové okno pro editace, nebo zavedení nového dodavatele
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class EditSuplierDialog extends JDialog implements WindowListener {
    
    private Component owner;
    
    /** Obsahuje dodavatele, jestliže byl dialog potvrzen, jinak obsahuje null */
    private static Suplier result = null; 
    
    /** Vstupní dodavatel, kterým se pøedvyplní dialog */
    private static Suplier inputSupl = null;

    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    /* Komponenty pro zobrazaní podrobností o dodavateli */
    private JTextField nameTextField = new JTextField();
    private JTextField icoTextField = new JTextField();
    private JTextField dicTextField = new JTextField();
    
    private JTextField streetTextField = new JTextField();
    private JTextField cityTextField = new JTextField();
    private JTextField pscTextField = new JTextField();
    
    private JTextField kontaktPersonTextField = new JTextField();
    private JTextField telTextField = new JTextField();
    private JTextField faxTextField = new JTextField();
    private JTextField mailTextField = new JTextField();
    private JTextField webTextField = new JTextField();
    
    private JTextField accountTextField = new JTextField();
    private JCheckBox isDPHBox = new JCheckBox();
    private JTextArea noteTextField = new JTextArea();
    
    /**
     * Vytvoøí nový dialog
     */
    private EditSuplierDialog(Frame owner) {
        super(owner, "Editace dodavatele", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvoøí nový dialog
     */
    private EditSuplierDialog(Dialog owner) {
        super(owner, "Editace dodavatele", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøeného dodavatele.
     * @return vytvoøeý dodavatel, jestliže byl dialog potvrzen.
     *          null - jestliže byl dialog nepotvrzen
     * @param owner Vlastník okna
     */
    public static Suplier openDialog(Frame owner) {
        
        inputSupl = null;
        new EditSuplierDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøeného dodavatele.
     * @return vytvoøeý dodavatel, jestliže byl dialog potvrzen.
     *          null - jestliže byl dialog nepotvrzen
     * @param owner Vlastník okna
     */
    public static Suplier openDialog(Dialog owner) {
        
        inputSupl = null;
        new EditSuplierDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøeného dodavatele.
     * @param owner Vlastník okna
     * @param suplier dodavatel, kterým se má dialog pøedvyplnit
     * @return vytvoøeý dodavatel, jestliže byl dialog potvrzen.
     * null - jestliže byl dialog nepotvrzen
     */
    public static Suplier openDialog(Frame owner, Suplier suplier) {
        
        inputSupl = suplier;
        new EditSuplierDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog a vrátí vytvoøeného dodavatele.
     * @return vytvoøeý dodavatel, jestliže byl dialog potvrzen.
     *          null - jestliže byl dialog nepotvrzen
     * @param owner Vlastník okna
     * @param suplier dodavatel, kterým se má dialog pøedvyplnit
     */
    public static Suplier openDialog(Dialog owner, Suplier suplier) {
        
        inputSupl = suplier;
        new EditSuplierDialog(owner);
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
        
        if (owner  != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(true);
        setMinimumSize(new Dimension(500, 380));

        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        
        // Pokuï byl zadán vstupní dodavatel, nastav potøebné hodnoty
        if (inputSupl  != null) {
            setInitialValues();
        }
        
        setVisible(true);
        
        inputSupl = null; // Napøíštì nastav na null
    }    
    
    /**
     * Nastaví vstupní hodnoty editaèním kompnentám
     */
    private void setInitialValues() {
        // Vyplò jednotlivá políèka 
        nameTextField.setText(inputSupl.getName());
        icoTextField.setText(inputSupl.getIco());
        dicTextField.setText(inputSupl.getDic());
        streetTextField.setText(inputSupl.getSendStreet());
        cityTextField.setText(inputSupl.getSendCity());
        pscTextField.setText(inputSupl.getSendPsc());
        kontaktPersonTextField.setText(inputSupl.getPerson());
        telTextField.setText(inputSupl.getTel());
        faxTextField.setText(inputSupl.getFax());
        mailTextField.setText(inputSupl.getMail());
        webTextField.setText(inputSupl.getWeb());
        accountTextField.setText(inputSupl.getAccount());
        noteTextField.setText(inputSupl.getNote());
        
        isDPHBox.setSelected( (inputSupl.isDph() ? true : false));        
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
     *  Vytvoøí hlavní panel s podrobnostmy o ododavateli
     */
    private JPanel createInfoPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Informace o dodavateli"));
        
        content.add(setComponent(basicInfoPanel(), 0, 0, 2, 1, 1.0, 0.0, HORIZONTAL, WEST));
        content.add(setComponent(addressPanel(), 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        content.add(setComponent(kontaktPanel(), 1, 1, 1, 1, 0.0, 0.0, BOTH, WEST));
        content.add(setComponent(otherPanel(), 0, 2, 2, 1, 1.0, 0.0, BOTH, WEST));
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
     *  Vytvoøí panel s adresou dodavatele
     */
    private JPanel addressPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Adresa"));

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
     *  Vytvoøí panel s kontaktními informacemi o  dodavateli
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
     *  Vytvoøí panel s dalšími informacemi o  dodavateli
     */
    private JPanel otherPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ostatní"));
        
        //accountTextField.setFont(font);
        label = new JLabel(" Bankovní spojení: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(accountTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, WEST) );
        
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
        
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        noteTextField.setFont(font);
        JScrollPane scrollPane = new JScrollPane(noteTextField);
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }

    /**
     *  Potvrdí dialog a uzavøe ho.
     *  Do promìnné result uloží dodaatele
     */
    private void confirm() {
        
        // Vytvoø dodavatele podle zadaných hodnot
        result = new Suplier(0,
                    nameTextField.getText().trim(), 
                    kontaktPersonTextField.getText().trim(), 
                    streetTextField.getText().trim(), 
                    cityTextField.getText().trim(), 
                    pscTextField.getText().trim(), 
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
