/*
 * EditGoodsDialog.java
 *
 * Created on 7. øíjen 2005, 22:59
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.PriceList;
import cz.control.data.Goods;
import cz.control.business.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí dialogové okno pro editace, nebo zavedení nové skladové karty
 * Dialog otevírá tøída StorePanel
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public final class EditGoodsDialog extends JDialog  implements WindowListener {
    
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private static int MATH_ROUND = 0;
    
    private Store store;
    private User user;
    private Component owner;
    
    private String[] dphBoxItems = Settings.getDphLayers();
    private String[] typeBoxItems = { "Zboží", "Komplet"};
    private double initialQuantity = 0; // poèáteèní množství zboží
    
    /* Jednotlivé vstupní pole */
    private JTextField idTextField = new JTextField(); 
    private JTextField nameTextField = new JTextField();
    private JComboBox dphTextField = new JComboBox(dphBoxItems);
    private JTextField unitTextField = new JTextField("ks"); 
    private JTextField eanTextField = new JTextField(); 
    private JComboBox typeTextField = new JComboBox(typeBoxItems); // Bude využito v další verzi
    private JCheckBox usePriceList = new JCheckBox("Použít ceník", true);
    private JButton openPriceList = new JButton("Ceník");
    
    private EditablePriceTableModel priceTableModel;
    private JTable priceTable;
    
    private Object dphPrevValue = Settings.getHighLayer();
    private boolean appendMode = true; //uchovává zda se bude zboží doplòovat, èi editovat
    private Goods oldGoods; // zboží, které se bude editovat
    private PriceList priceList = null; // ceník pro výpoèet PC z NC
    private PriceListEditor priceListEditor;
    
    private static Goods resultGoods = null;
            
    /**
      * Vytvoøí dialog pro editaci nebo vytvoøení zboží
      * @param store reference na sklad, do kterého má být zboží doplnìno
      * @param user reference na pøihlášeného uživatele
      * @param goods reference na zboží, které se bude v dialogu editovat
      */
    private EditGoodsDialog(Frame owner, User user, Goods goods) {
        super(owner, "Control - Editace skladové karty", true);
        
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }      
        
        this.user = user;
        this.owner = owner;
        this.initialQuantity = goods.getQuantity();
        
        appendMode = false; // Zboží se bude editovat
        oldGoods = goods;
    
        dphPrevValue = new Integer(goods.getDph());
        
        dphTextField.setEditable(true);
        
        /* Nastav hodnoty do okének */
        idTextField.setText(goods.getGoodsID());
        nameTextField.setText(goods.getName());
        dphTextField.setSelectedItem(dphPrevValue); 
        unitTextField.setText(goods.getUnit());
        eanTextField.setText(goods.getEan());

        /* Nastav ceny */
        priceTableModel = new EditablePriceTableModel(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph());
        priceTableModel.setPriceList(null);
        usePriceList.setSelected(false);
        
        setDialog();
    }
    
    /**
      * Vytvoøí dialog pro editaci nebo vytvoøení zboží
      * @param store reference na sklad, do kterého má být zboží doplnìno
      * @param user reference na pøihlášeného uživatele
      * @param goods reference na zboží, které se bude v dialogu editovat
      */
    private EditGoodsDialog(Dialog owner, User user, Goods goods) {
        super(owner, "Control - Editace skladové karty", true);
    
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }        
        
        this.user = user;
        this.owner = owner;
        this.initialQuantity = goods.getQuantity();
        
        appendMode = false; // Zboží se bude editovat
        oldGoods = goods;
    
        dphPrevValue = new Integer(goods.getDph());
        
        dphTextField.setEditable(true);
        
        /* Nastav hodnoty do okének */
        idTextField.setText(goods.getGoodsID());
        nameTextField.setText(goods.getName());
        dphTextField.setSelectedItem(dphPrevValue); 
        unitTextField.setText(goods.getUnit());
        eanTextField.setText(goods.getEan());

        /* Nastav ceny */
        priceTableModel = new EditablePriceTableModel(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph());
        priceTableModel.setPriceList(null);
        usePriceList.setSelected(false);
        
        setDialog();
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
    
     /**
      *  Vytvoøí dialog pro editaci nebo vytvoøení zboží
      * @param store reference na sklad, do kterého má být zboží doplnìno
      * @param user reference na pøihlášeného uživatele
      */
    private EditGoodsDialog(Frame owner, User user) {
        super(owner, "Vytvoøení skladové karty", true);
        
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }      
        
        this.user = user;
        this.owner = owner;
        
        /* Umožni pøímé zadání danì*/
        dphTextField.setEditable(true);
        dphTextField.setSelectedItem(dphPrevValue); //implicitnì nastavena hodnota 19%
        priceTableModel = new EditablePriceTableModel(0, 0, 0, 0, 0, Integer.valueOf( String.valueOf(dphTextField.getSelectedItem())) );
        priceTableModel.setPriceList(priceList);
        usePriceList.setSelected(true);
        
        setDialog();
    }
    
     /**
      *  Vytvoøí dialog pro editaci nebo vytvoøení zboží
      * @param store reference na sklad, do kterého má být zboží doplnìno
      * @param user reference na pøihlášeného uživatele
      */
    private EditGoodsDialog(Dialog owner, User user) {
        
        super(owner, "Vytvoøení skladové karty", true);
        
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }      
        
        this.user = user;
        this.owner = owner;
        
        /* Umožni pøímé zadání danì*/
        dphTextField.setEditable(true);
        dphTextField.setSelectedItem(dphPrevValue); //implicitnì nastavena hodnota 19%
        priceTableModel = new EditablePriceTableModel(0, 0, 0, 0, 0, Integer.valueOf( String.valueOf(dphTextField.getSelectedItem())) );
        priceTableModel.setPriceList(priceList);
        usePriceList.setSelected(true);
        
        setDialog();
    }
    
    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {
        dphTextField.setPreferredSize(new Dimension(40, 20));
        dphTextField.addItemListener(new DphChangeListener());
        
        typeTextField.setSelectedItem("Zboží"); //implicitnì nastavena hodnota "Zboží"

        /* Vytvoø tabulku a nastav hodnotu DPH */
        priceTable = new CommonTable(priceTableModel);

        this.addWindowListener(this);
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); 
        pack();
        setVisible(true);
        
    }
    
    /**
     * Otevøe dialog. Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu
     * Nebo null, jestliže uživatel nepotvrdil dialog
     *
     * @param owner Vlastník dialogu
     * @param user Uživatel, pro kterého byl dialog otevøeno
     * @return Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu.
     * Nebo null, jestliže uživatel nepotvrdil dialog
     *
     */
    public static Goods openDialog(Frame owner, User user) {
        resultGoods = null;
        new EditGoodsDialog(owner, user);
        return resultGoods;
    }
    
    /**
     * Otevøe dialog. Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu
     * Nebo null, jestliže uživatel nepotvrdil dialog
     *
     * @param owner Vlastník dialogu
     * @param user Uživatel, pro kterého byl dialog otevøeno
     * @return Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu.
     * Nebo null, jestliže uživatel nepotvrdil dialog
     *
     */
    public static Goods openDialog(Dialog owner, User user) {
        resultGoods = null;
        new EditGoodsDialog(owner, user);
        return resultGoods;
    }
    
    /**
     * Otevøe dialog. Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu
     * Nebo null, jestliže uživatel nepotvrdil dialog
     * 
     * 
     * @return Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu
     * Nebo null, jestliže uživatel nepotvrdil dialog
     * @param oldGoods Staré zboží, které se má editovat
     * @param owner Vlastník dialogu
     * @param user Uživatel, pro kterého byl dialog otevøeno
     */
    public static Goods openDialog(Frame owner, User user, Goods oldGoods) {
        resultGoods = null;
        new EditGoodsDialog(owner, user, oldGoods);
        return resultGoods;
    }
    
    /**
     * Otevøe dialog. Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu
     * Nebo null, jestliže uživatel nepotvrdil dialog
     * 
     * 
     * @return Vrací vytvoøené zboží, které zapsal do databáze pøed uzavøením dialogu
     * Nebo null, jestliže uživatel nepotvrdil dialog
     * @param oldGoods Staré zboží, které se má editovat
     * @param owner Vlastník dialogu
     * @param user Uživatel, pro kterého byl dialog otevøeno
     */
    public static Goods openDialog(Dialog owner, User user, Goods oldGoods) {
        resultGoods = null;
        new EditGoodsDialog(owner, user, oldGoods);
        return resultGoods;
    }
    
    /**
      *  Vytvoøí obsah dialogu 
      */
    private Container getContent() {
        JPanel content = new JPanel(new BorderLayout());
        
        JPanel mainPart = new JPanel(); // Obsah hlavního okna
        mainPart.setPreferredSize(new Dimension(430, 350));
        mainPart.add(createMainPanel()); // Vlož èást s edit boxy
        mainPart.add(createTablePanel()); // Vlož tabulku
        
        content.add(mainPart, BorderLayout.CENTER); // Vlož panel s hlavní èástí okna 
        
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
     *  Vytvoøí hlavní panel dialogu
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Údaje o zboží"));
        mainPanel.setPreferredSize(new Dimension(430, 136));
        
        idTextField.setPreferredSize( new Dimension(250, 20) );
        nameTextField.setPreferredSize( new Dimension(250, 20) );
        dphTextField.setPreferredSize( new Dimension(50, 20) );
        unitTextField.setPreferredSize( new Dimension(250, 20) );
        eanTextField.setPreferredSize( new Dimension(250, 20) );
        
        idTextField.setMinimumSize( new Dimension(250, 20) );
        nameTextField.setMinimumSize( new Dimension(250, 20) );
        dphTextField.setMinimumSize( new Dimension(50, 20) );
        unitTextField.setMinimumSize( new Dimension(250, 20) );

        mainPanel.add(setComponent(new JLabel("Skladové èíslo:     "), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(idTextField, 1, 0, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("Název: "), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(nameTextField, 1, 1, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("EAN: "), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(eanTextField, 1, 2, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("Jednotka: "), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(unitTextField, 1, 3, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("DPH [%]: "), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(dphTextField, 1, 4, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        
        return mainPanel;
    }
    
    /**
     *  Vytvoøí panel s tabulkou cen
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(gbl);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        tablePanel.setPreferredSize(new Dimension(420, 200));
        
        /* Nastav editory bunìk pro zmìnu ceny*/
        TableColumnModel columnModel = priceTable.getColumnModel();
        columnModel.getColumn(1).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(2).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(1).setCellEditor(new PriceCellEditor());
        columnModel.getColumn(2).setCellEditor(new PriceCellEditor());
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());

        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(350, 130)); //Minimalní velikost panelu
        
        tablePanel.add(setComponent(scrollPane, 0, 0, 2, 1, 1.0, 0.0, NONE, NORTH));
        
        openPriceList.addActionListener(new OpenPriceListListener());
        openPriceList.setToolTipText("Umožní nastavit výpoèet Prodejních cen pro tuto skladovou kartu");
        tablePanel.add(setComponent(openPriceList, 0, 1, 1, 1, 1.0, 1.0, NONE, EAST));
        usePriceList.addActionListener( new UsePriceListListener() );
        usePriceList.addActionListener( new UsePriceListListener() );
        tablePanel.add(setComponent(usePriceList, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST));
        
        return tablePanel;
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
     *  Zruší dialog bez uloení zmìn
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
        
        resultGoods = null;

        EditGoodsDialog.this.dispose(); // Zavøi dialog
    }
    
    /**
     *  Potvrzení zmìn ve formuláøi
     */
    private void confirm() {
            String id = idTextField.getText();
            String name = nameTextField.getText();
            int type = 0; //Integer.valueOf( String.valueOf(typeTextField.getSelectedItem()) );
            int dph = Integer.valueOf( String.valueOf(dphTextField.getSelectedItem()) );
            String ean = eanTextField.getText();
            String unit = unitTextField.getText();
            
            int column = PriceTableColumns.PRICE.getNumber();
            
//            float d = (Float) priceTableModel.getValueAt(0, column);
//            float ext = d *  Store.CENT;
//            int ii = Math.round(ext);
            BigDecimal
                nc = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(0, column)) ) ).multiply(Store.CENT), 
                pcA = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(1, column)) ) ).multiply(Store.CENT),     
                pcB = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(2, column)) ) ).multiply(Store.CENT),     
                pcC = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(3, column)) ) ).multiply(Store.CENT),     
                pcD = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(4, column)) ) ).multiply(Store.CENT);   
            
            /* Vytvoø zboží */
            resultGoods = new Goods(id, name, type, dph, unit, ean, 
                    nc.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcA.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcB.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcC.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcD.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    initialQuantity);

            /* Pokus se zapsat zboží do databáze*/
            try {
                if (appendMode) {
                    user.createGoods(resultGoods); // doplò zboží
                } else {
                    user.editGoods(oldGoods, resultGoods); // edituj zboží
                }

                EditGoodsDialog.this.dispose(); // Uzavøi okno

            } catch (InvalidPrivilegException exception) {
                ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                JOptionPane.showMessageDialog(EditGoodsDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            } catch (SQLException exception) {
                ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                JOptionPane.showMessageDialog(EditGoodsDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            } 
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
     *  Posluchaè zmìny hodnoty v DPH
     */
    private class DphChangeListener implements ItemListener {
        
        public void itemStateChanged(ItemEvent e) {
            
            /* Zmìnu pøi "odznaèení" neber v úvahu*/
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            
            try {
                int dph = Integer.valueOf( String.valueOf(e.getItem())); // naèti dph
                // Jestliže byla zadána záporná hodnota 
                if (dph < 0) {
                    throw new NumberFormatException("Záporná hodnota DPH");
                }
                priceTableModel.changeDph(dph);
                dphPrevValue = e.getItem(); // Ulož naposled zadanou hodnotu
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(EditGoodsDialog.this, "<html><center>Chybnì zadaná hodnota \"" + e.getItem() + "\". <br>Zadávejte celá èísla v rozsahu 0 - 100 [%]</center></html>", "Neoèekávaná hodnota", JOptionPane.ERROR_MESSAGE); 
                dphTextField.setSelectedItem(dphPrevValue); // Nastav na poslední správnou volbu
            }
        }
    }
    
    /**
     *  Zmìna checkboxu urèující, zda se má použít ceník
     */
    private class UsePriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if (usePriceList.isSelected() == true) {
                priceTableModel.setPriceList(priceList);
                priceTableModel.refreschPrices();
            } else {
                priceTableModel.setPriceList(null);
            }
        }

    }
    
    /**
     *  Stisk tlaèítka pro otevøení ceníku
     */
    private class OpenPriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PriceList tmp = priceList;
            priceList = PriceListDialog.openPriceListDialog(EditGoodsDialog.this, priceList);
            
            // Jestliže potvrdil dialog, zapni používání ceníku
            if (priceList != null) {
                usePriceList.setSelected(true); 
                priceTableModel.setPriceList(priceList); //zároveò nastav používání ceníku
                priceTableModel.refreschPrices();
            } else {
                priceList = tmp;
            }
        }
        
    }
            
}
