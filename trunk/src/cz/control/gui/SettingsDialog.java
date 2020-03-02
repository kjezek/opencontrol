/*
 * SettingsDialog.java
 *
 * Vytvo�eno 30. leden 2006, 0:27
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
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialog z nastaven�m
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class SettingsDialog extends JDialog  {
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private Component owner;
    private JTabbedPane tabbedPane = new JTabbedPane();;
    
    /* Editory vlastnost� */
    /* Sklad */
    private JTextField ncTextField = new JTextField(Settings.getNcName(), 15);
    private JTextField pcATextField = new JTextField(Settings.getPcAName(), 15);
    private JTextField pcBTextField = new JTextField(Settings.getPcBName(),15);
    private JTextField pcCTextField = new JTextField(Settings.getPcCName(),15);
    private JTextField pcDTextField = new JTextField(Settings.getPcDName(),15);
    private JCheckBox zeroCardsCheckBox = new JCheckBox("Zobrazit nulov� karty", Settings.isShowZeroCards());
    
    /* Datab�ze */
    private JRadioButton defaultLocationRB = new JRadioButton("Na tomto po��ta�i");
    private JRadioButton setLocationRB = new JRadioButton("Na jin�m po��ta�i v s�ti");
    private JTextField setLocationTF = new JTextField(Settings.getDatabaseURL(),15);
    
    private JRadioButton defaultUserRB = new JRadioButton("V�choz� u�ivatel");
    private JRadioButton setUserRB = new JRadioButton("Konkr�tn� u�ivatel");
    private JTextField setUserTF = new JTextField(Settings.getDatabaseUserName(),15);
    private JPasswordField passwordTF = new JPasswordField(Settings.getDatabaseUserPassword(),15);

    private JSpinField mainItemFontSize = new JSpinField();
    private JSpinField labelsFontSize = new JSpinField();
    private JSpinField textFieldsFontSize = new JSpinField();
    
    private JButton confirmButton;
    private JButton cancelButton;
    
    private int showPanel = 0; // indikuje, kter� panel se zobraz�
    private boolean dialogResult = false;

    /**
     *  Vyjmenovan� typ p�edstavuj�c� jednotliv� z�lo�ky 
     *  dialogu nastaven�
     */
    public static enum SettingsItems {
        /**
         * Z�lo�ka ud�vaj�c� nastaven� prost�ed�
         */
        ENVIRONMENT(0),
        /**
         * Z�lo�ka ud�vaj�c� nastaven� datab�ze
         */
        DATABASE(2);
        
        private int index = 0;
        
        private SettingsItems(int index) {
            this.index = index;
        }
        
        /**
         * Vrac� index p��slu�n� z�lo�ky
         * @return index p��slu�n� z�lo�ky
         */
        public int getIndex() {
            return index;
        }
        
    }
        
    /**
     * Vytvo�� nov� objekt SettingsDialog
     * @param owner Vlastn�k dialogu
     */
    private SettingsDialog(Frame owner) {
        super(owner, "Control - Nastaven�", true);

        this.owner = owner;
        setDialog();
    }
    /**
     * Vytvo�� nov� objekt SettingsDialog
     * @param owner Vlastn�k dialogu
     */
    private SettingsDialog(Dialog owner) {
        super(owner, "Control - Nastaven�", true);

        this.owner = owner;
        setDialog();
    }        
    
    /**
     * Vytvo�� nov� objekt SettingsDialog
     * @param owner Vlastn�k dialogu
     * @param showPanel Ud�v�, kter� panel se m� zobrazit v dialogu
     */
    private SettingsDialog(Dialog owner, int showPanel) {
        super(owner, "Control - Nastaven�", true);

        this.owner = owner;
        this.showPanel = showPanel;
        setDialog();
    }
    
    /**
     * Vytvo�� nov� objekt SettingsDialog
     * @param owner Vlastn�k dialogu
     * @param showPanel Ud�v�, kter� panel se m� zobrazit v dialogu
     */
    private SettingsDialog(Frame owner, int showPanel) {
        super(owner, "Control - Nastaven�", true);

        this.owner = owner;
        this.showPanel = showPanel;
        setDialog();
    }
    
    
    /**
     * provede pot�ebn� nastaven� 
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
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
        setVisible(true);
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
     * Vytvo�� obsah okna
     */
    private JComponent getContent() {
        JPanel main = new JPanel(new BorderLayout());
        
        tabbedPane.addKeyListener( new SettingsKeyListener() );
        
        tabbedPane.addTab("Prost�ed�", null, createEnvironment(), "Konfigurace u�ivatelsk�ho prost�ed�");
        tabbedPane.addTab("P�smo", null, createFonts(), "Nastaven� velikosti p�sma");
        tabbedPane.addTab("Datab�ze", null, createDatabase(), "Nastaven� p��stupu do datab�ze");
        //tabbedPane.addTab("Prodej", null, new JPanel(), "Prodejn� statistiky");
        tabbedPane.setSelectedIndex(showPanel);
        
        main.add(tabbedPane, BorderLayout.CENTER);
        
        
        JPanel buttonPanel = new JPanel();
            
        cancelButton = new JButton("Zru�it");
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
     *  Vytvo�� panel s nastaven�m prost�ed�
     */
    private JPanel createEnvironment() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.add(setComponent(storeSettings(), 0, 0, 1, 1, 1.0, 1.0, BOTH, NORTH));
        
        return content;
    }
    
    /**
     *  Vytvo�� panel pro nastaven� vlastnost� skladu
     */
    private JPanel storeSettings() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Vlastnosti skladu"));
        
        JLabel label = new JLabel("N�kupn� cena: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(ncTextField, 1, 0, 1, 1, 1.0, 1.0, WEST, WEST));
        
        label = new JLabel("Prodejn� cena A: ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcATextField, 1, 1, 1, 1, 1.0, 1.0, WEST, WEST));
        
        label = new JLabel("Prodejn� cena B: ");
        content.add(setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcBTextField, 1, 2, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Prodejn� cena C: ");
        content.add(setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcCTextField, 1, 3, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Prodejn� cena D: ");
        content.add(setComponent(label, 0, 4, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(pcDTextField, 1, 4, 1, 1, 1.0, 1.0, WEST, WEST));

        content.add(setComponent(zeroCardsCheckBox, 1, 5, 1, 1, 1.0, 1.0, WEST, WEST));
        
        return content;
    }

    private Component createFonts() {
        JPanel content = new JPanel();
        content.setLayout(gbl);

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Velikost p�sma"));

        mainItemFontSize.setMinimum(5);
        mainItemFontSize.setMaximum(25);
        mainItemFontSize.setValue(Settings.getMainItemsFontSize());

        labelsFontSize.setMinimum(5);
        labelsFontSize.setMaximum(25);
        labelsFontSize.setValue(Settings.getLabelsFontSize());

        textFieldsFontSize.setMinimum(5);
        textFieldsFontSize.setMaximum(25);
        textFieldsFontSize.setValue(Settings.getTextFieldsFontSize());

        JLabel label = new JLabel("Hlavn� popisky: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(mainItemFontSize, 1, 0, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("B�n� Popisky: ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(labelsFontSize, 1, 1, 1, 1, 1.0, 1.0, WEST, WEST));

        label = new JLabel("Edita�n� pole: ");
        content.add(setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(textFieldsFontSize, 1, 2, 1, 1, 1.0, 1.0, WEST, WEST));

        return content;

    }


    /**
     *  Vytvo�� panel s nastaven�m datab�ze
     */
    private JPanel createDatabase() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.add(setComponent(databaseLocation(), 0, 0, 1, 1, 1.0, 1.0, BOTH, NORTH));
        content.add(setComponent(databaseAccess(), 0, 1, 1, 1, 1.0, 1.0, BOTH, NORTH));
        
        return content;
    }
    
    /**
     *  Vyvto�� panel s nastaven�m um�st�n� datab�ze
     */
    private JPanel databaseLocation() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Um�st�n� datab�zov�ho serveru v s�ti"));
        
        content.add(setComponent(defaultLocationRB, 0, 0, 2, 1, 0.0, 1.0, BOTH, WEST));
        defaultLocationRB.addActionListener( new defaultLocationAL() );
        
        content.add(setComponent(setLocationRB, 0, 1, 1, 1, 1.0, 1.0, VERTICAL, WEST));
        setLocationRB.addActionListener( new setLocationAL() );
        
        content.add(setComponent(setLocationTF, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(defaultLocationRB);
        buttonGroup.add(setLocationRB);
        
        // Nastav tla��tka podle toho, zda je nastavena cesta jako "localhost"
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
     *  Vyvto�� panel s nastaven�m um�st�n� datab�ze
     */
    private JPanel databaseAccess() {
        JPanel content = new JPanel();
        content.setLayout(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Zad�n� p��stupov�ho hesla"));

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
        
        // Nastav tla��tka podle toho, zda je nastaven u�ivatel jako root "root"
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
     *  Zru�� dialog bez uloen� zm�n
     */ 
    private void cancel() {

        /*   Nejedn� se o tak d�le�itou akci, aby se muselo zobrazovat varov�n�
        String text = "Zav��t okno bez ulo�en� nastaven�?";
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
        }  */         

        dialogResult = false;
        this.dispose(); // Zav�i dialog
    }
    
    /**
     *  Provede nastaven� zmn�n po potvrzen� dialogu 
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
                // Obnov n�zvy cen 
                MainWindow.getInstance().getStorePanel().refreshPriceNames();
                // Obnov filtr zobrazen�
                MainWindow.getInstance().getStorePanel().refresh();
            }
            
            // Datab�ze    
            
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
     *  Poslucha� stisku tla��tka Potvzen� 
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
            confirm();

            SettingsDialog.this.dispose();
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
    
    /**
     *  Poslucha� stisku Radio buttonu V�choz� um�st�n�
     */
    private class defaultLocationAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            defaultLocationRB.setSelected(true);
            //setLocationTF.setText("127.0.0.1");
            setLocationTF.setEnabled(false);
        }
    }

    /**
     *  Poslucha� stisku Radio buttonu Nastavit po�ita�
     */
    private class setLocationAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setLocationRB.setSelected(true);
            setLocationTF.setEnabled(true);
        }
        
    }
    
    /**
     *  Poslucha� stisku Radio buttonu V�choz� u�ivatel
     */
    private class defaultUserAL implements ActionListener {
        public void actionPerformed(ActionEvent e){
            defaultUserRB.setSelected(true);
            //setUserTF.setText("root");
            setUserTF.setEnabled(false);
        }
    }

    /**
     *  Poslucha� stisku Radio buttonu Nastavit u�ivatele
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
     * Vrac�, zda byl dialog potvrzen
     * @return true jestli�e byl dialog potvrzen stiskem tla��tka "Potvrdit", jinak vrac� false
     */
    public static boolean openSettingsDialog(Frame owner, int showPane) {
        
        SettingsDialog std = new SettingsDialog(owner, showPane);
        return std.dialogResult;
    }

    /**
     * Vrac�, zda byl dialog potvrzen
     * @return true jestli�e byl dialog potvrzen stiskem tla��tka "Potvrdit", jinak vrac� false
     */
    public static boolean openSettingsDialog(Frame owner) {
        
        return openSettingsDialog(owner, SettingsDialog.SettingsItems.ENVIRONMENT.getIndex());
    }
    

    /**
     * Vrac�, zda byl dialog potvrzen
     * @return true jestli�e byl dialog potvrzen stiskem tla��tka "Potvrdit", jinak vrac� false
     */
    public static boolean openSettingsDialog(Dialog owner, int showPane) {
        
        SettingsDialog std = new SettingsDialog(owner, showPane);
        return std.dialogResult;
    }

    /**
     * Vrac�, zda byl dialog potvrzen
     * @return true jestli�e byl dialog potvrzen stiskem tla��tka "Potvrdit", jinak vrac� false
     */
    public static boolean openSettingsDialog(Dialog owner) {
        
        return openSettingsDialog(owner, SettingsDialog.SettingsItems.ENVIRONMENT.getIndex());
    }
}
