/*
 * EditSuplierDialog.java
 *
 * Vytvo�eno 9. �nor 2006, 18:45
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialogov� okno pro editace, nebo zaveden� nov�ho dodavatele
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class EditSuplierDialog extends JDialog implements WindowListener {
    
    private Component owner;
    
    /** Obsahuje dodavatele, jestli�e byl dialog potvrzen, jinak obsahuje null */
    private static Suplier result = null; 
    
    /** Vstupn� dodavatel, kter�m se p�edvypln� dialog */
    private static Suplier inputSupl = null;

    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    /* Komponenty pro zobrazan� podrobnost� o dodavateli */
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
     * Vytvo�� nov� dialog
     */
    private EditSuplierDialog(Frame owner) {
        super(owner, "Editace dodavatele", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� nov� dialog
     */
    private EditSuplierDialog(Dialog owner) {
        super(owner, "Editace dodavatele", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Otev�e dialog a vr�t� vytvo�en�ho dodavatele.
     * @return vytvo�e� dodavatel, jestli�e byl dialog potvrzen.
     *          null - jestli�e byl dialog nepotvrzen
     * @param owner Vlastn�k okna
     */
    public static Suplier openDialog(Frame owner) {
        
        inputSupl = null;
        new EditSuplierDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog a vr�t� vytvo�en�ho dodavatele.
     * @return vytvo�e� dodavatel, jestli�e byl dialog potvrzen.
     *          null - jestli�e byl dialog nepotvrzen
     * @param owner Vlastn�k okna
     */
    public static Suplier openDialog(Dialog owner) {
        
        inputSupl = null;
        new EditSuplierDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog a vr�t� vytvo�en�ho dodavatele.
     * @param owner Vlastn�k okna
     * @param suplier dodavatel, kter�m se m� dialog p�edvyplnit
     * @return vytvo�e� dodavatel, jestli�e byl dialog potvrzen.
     * null - jestli�e byl dialog nepotvrzen
     */
    public static Suplier openDialog(Frame owner, Suplier suplier) {
        
        inputSupl = suplier;
        new EditSuplierDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog a vr�t� vytvo�en�ho dodavatele.
     * @return vytvo�e� dodavatel, jestli�e byl dialog potvrzen.
     *          null - jestli�e byl dialog nepotvrzen
     * @param owner Vlastn�k okna
     * @param suplier dodavatel, kter�m se m� dialog p�edvyplnit
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
    
    /*
     * provede pot�ebn� nastaven� 
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
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
        
        // Poku� byl zad�n vstupn� dodavatel, nastav pot�ebn� hodnoty
        if (inputSupl  != null) {
            setInitialValues();
        }
        
        setVisible(true);
        
        inputSupl = null; // Nap��t� nastav na null
    }    
    
    /**
     * Nastav� vstupn� hodnoty edita�n�m kompnent�m
     */
    private void setInitialValues() {
        // Vypl� jednotliv� pol��ka 
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
     *  Vytvo�� z�kladn� obsah dialogu
     */
    private JPanel createContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.add(createInfoPanel(), BorderLayout.CENTER);

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
        Font font =  new Font("DialogInput", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label;
        
        // Nastav jednotliv� vlastnosti komponent
        //nameTextField.setFont( font );
        //icoTextField.setFont( font );
        //dicTextField.setFont( font );
        
        //nameTextField.setPreferredSize( new Dimension(600, 30) );
        icoTextField.setPreferredSize( new Dimension(100, 20) );
        icoTextField.setMinimumSize( new Dimension(100, 20) );
        dicTextField.setPreferredSize( new Dimension(100, 20) );
        dicTextField.setMinimumSize( new Dimension(100, 20) );
        
        // Naskl�dej jednotliv� komponenty
        
        label = new JLabel(" N�zev: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(nameTextField, 1, 0, 1, 1, 1.0, 0.0, BOTH, WEST));
        
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
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Adresa"));

        // Tohle donut� Layout st�le nem�nit ���ku
        //content.setPreferredSize( new Dimension(400, 150));
        //content.setMinimumSize(new Dimension(400, 150));
        
        //kontaktPersonTextField.setFont(font);
        label = new JLabel(" Kontaktn� osoba: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(kontaktPersonTextField, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //streetTextField.setFont(font);
        label = new JLabel(" Ulice: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(streetTextField, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //cityTextField.setFont(font);
        label = new JLabel(" M�sto: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(cityTextField, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        //pscTextField.setFont(font);
        label = new JLabel(" PS�: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(pscTextField, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        return content;
    }
    
    /**
     *  Vytvo�� panel s kontaktn�mi informacemi o  dodavateli
     */
    private JPanel kontaktPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Kontakt"));

        // Tohle donut� Layout st�le nem�nit ���ku
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
     *  Vytvo�� panel s dal��mi informacemi o  dodavateli
     */
    private JPanel otherPanel()  {
        JPanel content = new JPanel(gbl);
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ostatn�"));
        
        //accountTextField.setFont(font);
        label = new JLabel(" Bankovn� spojen�: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(accountTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, WEST) );
        
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
        
        Font font =  new Font("DialogInput", Font.PLAIN, Settings.getMainItemsFontSize());
        noteTextField.setFont(font);
        JScrollPane scrollPane = new JScrollPane(noteTextField);
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }

    /**
     *  Potvrd� dialog a uzav�e ho.
     *  Do prom�nn� result ulo�� dodaatele
     */
    private void confirm() {
        
        // Vytvo� dodavatele podle zadan�ch hodnot
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
     *  Strornuje dialog a uzav�e ho
     *  Do prom�nn� result ulo�� null
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
        
        result = null;
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
