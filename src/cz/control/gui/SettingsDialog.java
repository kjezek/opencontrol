/*
 * SettingsDialog.java
 *
 * Vytvoøeno 30. leden 2006, 0:27
 *
 
 */

package cz.control.gui;

import com.toedter.components.JSpinField;
import cz.control.business.Settings;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.VERTICAL;
import static java.awt.GridBagConstraints.WEST;

/**
 * Program Control - Skladový systém
 *
 * Zobrazí dialog z nastavením
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class SettingsDialog extends JDialog  {
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private Component owner;
    private JTabbedPane tabbedPane = new JTabbedPane();;
    
    /* Editory vlastností */
    /* Sklad */
    private JTextField ncTextField = new JTextField(Settings.getNcName(), 15);
    private JTextField pcATextField = new JTextField(Settings.getPcAName(), 15);
    private JTextField pcBTextField = new JTextField(Settings.getPcBName(),15);
    private JTextField pcCTextField = new JTextField(Settings.getPcCName(),15);
    private JTextField pcDTextField = new JTextField(Settings.getPcDName(),15);
    private JCheckBox zeroCardsCheckBox = new JCheckBox("Zobrazit nulové karty", Settings.isShowZeroCards());
    
    /* Databáze */
    private JRadioButton defaultLocationRB = new JRadioButton("Na tomto poèítaèi");
    private JRadioButton setLocationRB = new JRadioButton("Na jiném poèítaèi v síti");
    private JTextField setLocationTF = new JTextField(Settings.getDatabaseURL(),15);
    
    private JRadioButton defaultUserRB = new JRadioButton("Výchozí uživatel");
    private JRadioButton setUserRB = new JRadioButton("Konkrétní uživatel");
    private JTextField setUserTF = new JTextField(Settings.getDatabaseUserName(),15);
    private JPasswordField passwordTF = new JPasswordField(Settings.getDatabaseUserPassword(),15);

    private JSpinField mainItemFontSize = new JSpinField();
    private JSpinField labelsFontSize = new JSpinField();
    private JSpinField textFieldsFontSize = new JSpinField();
    
    private JButton confirmButton;
    private JButton cancelButton;
    
    private int showPanel = 0; // indikuje, který panel se zobrazí
    private boolean dialogResult = false;

    /**
     *  Vyjmenovaný typ pøedstavující jednotlivé záložky 
     *  dialogu nastavení
     */
    public static enum SettingsItems {
        /**
         * Záložka udávající nastavení prostøedí
         */
        ENVIRONMENT(0),
        /**
         * Záložka udávající nastavení databáze
         */
        DATABASE(2);
        
        private int index = 0;
        
        private SettingsItems(int index) {
            this.index = index;
        }
        
        /**
         * Vrací index pøíslušné záložky
         * @return index pøíslušné záložky
         */
        public int getIndex() {
            return index;
        }
        
    }
        
    /**
     * Vytvoøí nový objekt SettingsDialog
     * @param owner Vlastník dialogu
     */
    private SettingsDialog(Frame owner) {
        super(owner, "Control - Nastavení", true);

        this.owner = owner;
        setDialog();
    }
    /**
     * Vytvoøí nový objekt SettingsDialog
     * @param owner Vlastník dialogu
     */
    private SettingsDialog(Dialog owner) {
        super(owner, "Control - Nastavení", true);

        this.owner = owner;
        setDialog();
    }        
    
    /**
     * Vytvoøí nový objekt SettingsDialog
     * @param owner Vlastník dialogu
     * @param showPanel Udává, který panel se má zobrazit v dialogu
     */
    private SettingsDialog(Dialog owner, int showPanel) {
        super(owner, "Control - Nastavení", true);

        this.owner = owner;
        this.showPanel = showPanel;
        setDialog();
    }
    
    /**
     * Vytvoøí nový objekt SettingsDialog
     * @param owner Vlastník dialogu
     * @param showPanel Udává, který panel se má zobrazit v dialogu
     */
    private SettingsDialog(Frame owner, int showPanel) {
        super(owner, "Control - Nastavení", true);

        this.owner = owner;
        this.showPanel = showPanel;
        setDialog();
    }
    
    
    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        dialogResult = false;
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(false);
        setPreferredSize(new Dimension(520, 260));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        setVisible(true);
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
     * Vytvoøí obsah okna
     */
    private JComponent getContent() {
        JPanel main = new JPanel(new BorderLayout());
        
        tabbedPane.addKeyListener( new SettingsKeyListener() );
        
        tabbedPane.addTab("Prostøedí", null, createEnvironment(), "Konfigurace uživatelského prostøedí");
        tabbedPane.addTab("Písmo", null, createFonts(), "Nastavení velikosti písma");
        tabbedPane.addTab("Databáze", null, createDatabase(), "Nastavení pøístupu do databáze");
        //tabbedPane.addTab("Prodej", null, new JPanel(), "Prodejní statistiky");
        tabbedPane.setSelectedIndex(showPanel);
        
        main.add(tabbedPane, BorderLayout.CENTER);
        
        
        JPanel buttonPanel = new JPanel();
            
        cancelButton = new JButton("Zrušit");
        cancelButton.addActionListener( new CancelButtonListener());
        cancelButton.setMnemonic(KeyEvent.VK_CANCEL);
        buttonPanel.add(cancelButton);

        confirmButton = new JButton("Potvrdit");
        confirmButton.addActionListener( new ConfirmButtonListener());
        confirmButton.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(confirmButton);
        
        main.add(buttonPanel, BorderLayout.SOUTH);
        
        return main;
    }       
    
    /**
     *  Vytvoøí panel s nastavením prostøedí
     */
    private JPanel createEnvironment() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.add(setComponent(storeSettings(), 0, 0, 1, 1, 1.0, 1.0, BOTH, NORTH));
        
        return content;
    }
    
    /**
     *  Vytvoøí panel pro nastavení vlastností skladu
     */
    private JPanel storeSettings() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Vlastnosti skladu"));
        
        JLabel label = new JLabel("Nákupní cena: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(ncTextField, 1, 0, 1, 1, 1.0, 1.0, WEST, WEST));
        
        label = new JLabel("Prodejní cena A: ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcATextField, 1, 1, 1, 1, 1.0, 1.0, WEST, WEST));
        
        label = new JLabel("Prodejní cena B: ");
        content.add(setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcBTextField, 1, 2, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Prodejní cena C: ");
        content.add(setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcCTextField, 1, 3, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Prodejní cena D: ");
        content.add(setComponent(label, 0, 4, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcDTextField, 1, 4, 1, 1, 1.0, 1.0, WEST, WEST));

        content.add(setComponent(zeroCardsCheckBox, 1, 5, 1, 1, 1.0, 1.0, WEST, WEST));
        
        return content;
    }

    private Component createFonts() {
        JPanel content = new JPanel();
        content.setLayout(gbl);

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Velikost písma"));

        mainItemFontSize.setMinimum(5);
        mainItemFontSize.setMaximum(25);
        mainItemFontSize.setValue(Settings.getMainItemsFontSize());

        labelsFontSize.setMinimum(5);
        labelsFontSize.setMaximum(25);
        labelsFontSize.setValue(Settings.getLabelsFontSize());

        textFieldsFontSize.setMinimum(5);
        textFieldsFontSize.setMaximum(25);
        textFieldsFontSize.setValue(Settings.getTextFieldsFontSize());

        JLabel label = new JLabel("Hlavní popisky: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(mainItemFontSize, 1, 0, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Bìžné Popisky: ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(labelsFontSize, 1, 1, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Editaèní pole: ");
        content.add(setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(textFieldsFontSize, 1, 2, 1, 1, 1.0, 1.0, WEST, WEST));

        return content;

    }


    /**
     *  Vytvoøí panel s nastavením databáze
     */
    private JPanel createDatabase() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.add(setComponent(databaseLocation(), 0, 0, 1, 1, 1.0, 1.0, BOTH, NORTH));
        content.add(setComponent(databaseAccess(), 0, 1, 1, 1, 1.0, 1.0, BOTH, NORTH));
        
        return content;
    }
    
    /**
     *  Vyvtoøí panel s nastavením umístìní databáze
     */
    private JPanel databaseLocation() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Umístìní databázového serveru v síti"));
        
        content.add(setComponent(defaultLocationRB, 0, 0, 2, 1, 0.0, 1.0, BOTH, WEST));
        defaultLocationRB.addActionListener( new defaultLocationAL() );
        
        content.add(setComponent(setLocationRB, 0, 1, 1, 1, 1.0, 1.0, VERTICAL, WEST));
        setLocationRB.addActionListener( new setLocationAL() );
        
        content.add(setComponent(setLocationTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(defaultLocationRB);
        buttonGroup.add(setLocationRB);
        
        // Nastav tlaèítka podle toho, zda je nastavena cesta jako "localhost"
        if (Settings.getDatabaseLocation().equalsIgnoreCase("local")) {
            defaultLocationRB.setSelected(true);
            setLocationTF.setEnabled(false);
        } else {
            setLocationRB.setSelected(true);
            setLocationTF.setEnabled(true);
        }
        
        return content; 
    }
    
    /**
     *  Vyvtoøí panel s nastavením umístìní databáze
     */
    private JPanel databaseAccess() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Zadání pøístupového hesla"));

        content.add(setComponent(defaultUserRB, 0, 0, 2, 1, 1.0, 1.0, VERTICAL, WEST));
        defaultUserRB.addActionListener( new defaultUserAL() );
        
        content.add(setComponent(setUserRB, 0, 1, 1, 1, 1.0, 1.0, VERTICAL, WEST));
        setUserRB.addActionListener( new setUserAL() );
        
        content.add(setComponent(setUserTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(defaultUserRB);
        buttonGroup.add(setUserRB);
        
        content.add(setComponent( new JLabel("Heslo: "), 0, 2, 1, 1, 0.0, 1.0, NONE, CENTER));
        content.add(setComponent(passwordTF, 1, 2, 1, 1, 0.0, 1.0, HORIZONTAL, WEST));
        
        // Nastav tlaèítka podle toho, zda je nastaven uživatel jako root "root"
        if (Settings.getDatabaseUserName().equalsIgnoreCase("root") ) {
            defaultUserRB.setSelected(true);
            setUserTF.setEnabled(false);
        } else {
            setUserRB.setSelected(true);
            setUserTF.setEnabled(true);
        }
        
        return content; 
    }    
    
    /**
     *  Zruší dialog bez uloení zmìn
     */ 
    private void cancel() {

        /*   Nejedná se o tak dùležitou akci, aby se muselo zobrazovat varování
        String text = "Zavøít okno bez uložení nastavení?";
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
        }  */         

        dialogResult = false;
        this.dispose(); // Zavøi dialog
    }
    
    /**
     *  Provede nastavení zmnìn po potvrzení dialogu 
     */
    private void confirm() {
            // Sklad
            Settings.setNcName(ncTextField.getText());
            Settings.setPcAName(pcATextField.getText());
            Settings.setPcBName(pcBTextField.getText());
            Settings.setPcCName(pcCTextField.getText());
            Settings.setPcDName(pcDTextField.getText());
            Settings.setShowZeroCards(zeroCardsCheckBox.isSelected());
            
            if (MainWindow.getInstance() != null) {
                // Obnov názvy cen 
                MainWindow.getInstance().getStorePanel().refreshPriceNames();
                // Obnov filtr zobrazení
                MainWindow.getInstance().getStorePanel().refresh();
            }
            
            // Databáze    
            
            if (defaultLocationRB.isSelected()) {
                Settings.setDatabaseURL("127.0.0.1");
                Settings.setDatabaseLocation("local");
            } else {
                Settings.setDatabaseURL(setLocationTF.getText().trim());
                Settings.setDatabaseLocation("remote");
            }
            
            if (defaultUserRB.isSelected()) {
                Settings.setDatabaseUserName("root");
            } else {
                Settings.setDatabaseUserName(setUserTF.getText().trim());
            }
            
            String password = String.valueOf(passwordTF.getPassword());
            Settings.setDatabaseUserPassword(password.trim());

            // fonty
            Settings.setMainItemsFontSize(mainItemFontSize.getValue());
            Settings.setLabelsFontSize(labelsFontSize.getValue());
            Settings.setTextFieldsFontSize(textFieldsFontSize.getValue());

            dialogResult = true;
            
        try {
            Settings.saveSettings();
        } catch (Exception e) {
            ErrorMessages er = new ErrorMessages(Errors.WRITE_SETTINGS, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
         
    /**
     *  Posluchaè stisku tlaèítka Potvzení 
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
            confirm();

            SettingsDialog.this.dispose();
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
     *  Posluchaè stisku Radio buttonu Výchozí umístšní
     */
    private class defaultLocationAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            defaultLocationRB.setSelected(true);
            //setLocationTF.setText("127.0.0.1");
            setLocationTF.setEnabled(false);
        }
    }

    /**
     *  Posluchaè stisku Radio buttonu Nastavit poèitaè
     */
    private class setLocationAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setLocationRB.setSelected(true);
            setLocationTF.setEnabled(true);
        }
        
    }
    
    /**
     *  Posluchaè stisku Radio buttonu Výchozí uživatel
     */
    private class defaultUserAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            defaultUserRB.setSelected(true);
            //setUserTF.setText("root");
            setUserTF.setEnabled(false);
        }
    }

    /**
     *  Posluchaè stisku Radio buttonu Nastavit uživatele
     */
    private class setUserAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setUserRB.setSelected(true);
            setUserTF.setEnabled(true);
        }
        
    }
    
    private class SettingsKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    confirmButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE :
                    cancelButton.doClick();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }     

    /**
     * Vrací, zda byl dialog potvrzen
     * @return true jestliže byl dialog potvrzen stiskem tlaèítka "Potvrdit", jinak vrací false
     */
    public static boolean openSettingsDialog(Frame owner, int showPane) {
        
        SettingsDialog std = new SettingsDialog(owner, showPane);
        return std.dialogResult;
    }

    /**
     * Vrací, zda byl dialog potvrzen
     * @return true jestliže byl dialog potvrzen stiskem tlaèítka "Potvrdit", jinak vrací false
     */
    public static boolean openSettingsDialog(Frame owner) {
        
        return openSettingsDialog(owner, SettingsDialog.SettingsItems.ENVIRONMENT.getIndex());
    }
    

    /**
     * Vrací, zda byl dialog potvrzen
     * @return true jestliže byl dialog potvrzen stiskem tlaèítka "Potvrdit", jinak vrací false
     */
    public static boolean openSettingsDialog(Dialog owner, int showPane) {
        
        SettingsDialog std = new SettingsDialog(owner, showPane);
        return std.dialogResult;
    }

    /**
     * Vrací, zda byl dialog potvrzen
     * @return true jestliže byl dialog potvrzen stiskem tlaèítka "Potvrdit", jinak vrací false
     */
    public static boolean openSettingsDialog(Dialog owner) {
        
        return openSettingsDialog(owner, SettingsDialog.SettingsItems.ENVIRONMENT.getIndex());
    }
}
