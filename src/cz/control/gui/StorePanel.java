/*
 * StorePanel.java
 *
 * Created on 26. z·¯Ì 2005, 22:07
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.database.DatabaseAccess;
import cz.control.business.*;
import cz.control.business.TypeOfGoods;
import cz.control.data.Goods;

import java.sql.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*; 
import javax.swing.table.*;

import java.net.URL;
import java.util.*;

import static java.awt.GridBagConstraints.*;

/**
 * T¯Ìda tvo¯ÌcÌ panel pro zobrazenÌ v˝pisu zboûÌ na skladÏ. UmoûÚuje 
 * zobrazenÌ, editaci, vytv·¯enÌ a maz·nÌ zboûÌ na skladÏ
 * @author Kamil Jeûek
 */
public class StorePanel extends JPanel {
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // p¯ihl·öen˝ uûivatel 
    private Store store = null; // Ukazatel na sklad 
    private ArrayList<Goods> goods = new ArrayList<Goods>(); // Pole zboûÌ 

    private PriceTableModel priceTableModel;
    private GoodsTableModel goodsTableModel;
    private JTable goodsTable; // tabulka zboûÌ
    private JTable priceTable;
    private JTextField searchTextField; // okÈnko pro zad·nÌ slova pro vyhled·v·nÌ 
    private JButton searchButton;
    private JCheckBox filtrCheckBox = new JCheckBox("Zobrazit nulovÈ karty");
    
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
            
    /* Poloûky v detailu skladovÈ karty */
    private JTextField idLabel = new JTextField(""); // skladovÈ ËÌslo
    private JTextField nameLabel = new JTextField(""); // jmÈno zboûÌ
    private JTextField typeLabel = new JTextField(""); // typ poloûky (zboûÌ, komplet, ...)
    private JTextField dphLabel = new JTextField(""); // DPH
    private JTextField eanLabel = new JTextField(""); // ean
    private JTextField unitLabel = new JTextField(""); // mnoûstevnÌ jednota 
    
    /* NalezenÈ zboûÌ ze skladu */
    private static java.util.List<Goods> searchedItems = new ArrayList<Goods>(); 
    /* poslednÌ vr·cen· poloûka */
    private static Goods lastGoods = null;
    /* PoslednÌ klÌËovÈ slovo pro vyhled·v·nÌ */
    private static String lastKeyword = "";

    // limit zobrazenÌ
    private JSpinner limitSpinner = new JSpinner(
            new SpinnerNumberModel(Settings.LIMIT, 0, Integer.MAX_VALUE, 100));
    private static int currentLimit = 1000;
    private static String currentKeyword = "";
    
    
    private Component owner;
    
    /**
     * 
     * @param user 
     */
    public StorePanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        setPanel();
    }
    
    /**
     * 
     * @param user 
     */
    public StorePanel(Dialog owner, User user) {
        this.user = user;
        this.owner = owner;
        setPanel();
    }
    
    /**
     * NastavÌ vlastnosti okna 
     */
    private void setPanel() {
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        filtrCheckBox.setSelected(Settings.isShowZeroCards());
        
        try {
            store = user.openStore(); /* Otev¯i sklad pro p¯ihl·öenÈho uûivatele */
            // Nahrazeno vol·nÌ metody refresh() na konci tÈto metody            
            // goods = getStore().getAllGoods(); /* NaËti seznam zboûÌ */
        } catch (InvalidPrivilegException e) {
            // UkonËi zav·dÏnÌ okna a nastav z·loûku jako nep¯Ìstupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.STORE.getIndex(), false);
            return;

        }
        
        this.setLayout(gbl);
        this.add(setComponent(createGoodsTablePanel(), 0, 0, 3, 1, 1.0, 1.0, BOTH, NORTH));
        this.add(setComponent(createPricePanel(), 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        this.add(setComponent(createGoodsPanel(), 1, 1, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        this.add(setComponent(createEditButtonPanel(), 2, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        this.add(setComponent(createSearchPanel(), 0, 2, 3, 1, 1.0, 0.0, HORIZONTAL, WEST));
        
        refresh(); // Zobraz zboûÌ v tabulce      
    }
    
    /**
     *  NastavÌ vlastnosti vkl·danÈ komponenty 
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
     * Vytvo¯Ì tabulku s v˝pisem zboûÌ na skladÏ
     */
    private JPanel createGoodsTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Seznam zboûÌ"));

        goodsTableModel = new GoodsTableModel(goods);
        goodsTable = new CommonTable(getGoodsTableModel()); // vytvo¯enÌ tabulky
        getGoodsTable().setShowVerticalLines(false);  // Nastav neviditelnÈ vertik·lnÌ linky v tabulce
        TableColumnModel columnModel = getGoodsTable().getColumnModel();
        
        goodsTable.addKeyListener( new GoodsTableKeyListener() );
        goodsTable.addFocusListener( new ItemTableListener() );

        /* Nastav öÌ¯ky sloupc˘ */
        columnModel.getColumn(Columns.ID.getColumnNumber()).setPreferredWidth(Columns.ID.getColumnWidth()); // öÌ¯ka slouce "skladovÈ ËÌslo 
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setPreferredWidth(Columns.NAME.getColumnWidth()); // öÌ¯ka slouce "skladovÈ ËÌslo 
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setPreferredWidth(Columns.QUANTITY.getColumnWidth()); // öÌ¯ka slouce "skladovÈ ËÌslo 
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setPreferredWidth(Columns.UNIT.getColumnWidth()); // öÌ¯ka slouce "skladovÈ ËÌslo 

        /** Default renderer */
//        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
//        renderer.setFont(new Font("Dialog", Font.PLAIN, Settings.getMainItemsFontSize()));

        TableCellRenderer renderer = new CommonItemCellRenderer();

        /* Nastav zobrazenÌ slouc˘ */
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new QuantityCellRenderer());
        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(renderer);
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setCellRenderer(renderer);
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setCellRenderer(renderer);
 
        JScrollPane scrollPane = new JScrollPane(getGoodsTable());
        content.add(scrollPane, BorderLayout.CENTER);
        
        ListSelectionModel rowSM = getGoodsTable().getSelectionModel();
        rowSM.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rowSM.addListSelectionListener(new SelectRowGoodsTableListener());
        return content;
    }
    
    /*
     *  Panel s v˝pisem cen
     */
    private JPanel createPricePanel() {
        JPanel content = new JPanel(new GridLayout(1,1)); // GridLayer proto, aby bylo moûno nastavit velikost panelu
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        
        priceTableModel = new PriceTableModel(0, 0, 0, 0, 0, 0);
        priceTable = new CommonTable(priceTableModel);
        
        TableColumnModel columnModel = priceTable.getColumnModel();
        /* Nastav zobrazenÌ slouc˘ */
        columnModel.getColumn(PriceTableColumns.PRICE.getNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(PriceTableColumns.PRICE_DPH.getNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());
        
        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        priceTable.setVisible(false);
        priceTable.setFocusable(false);
        
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(300, 140)); //MinimalnÌ velikost panelu
        content.add(scrollPane);
        
        return content;
    }
    
    /*
     * Panel s v˝pisem karty zboûÌ
     */
    private JPanel createGoodsPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Skladov· karta"));
        content.setMinimumSize( new Dimension(200, 127));
        content.setLayout(gbl);
        JPanel panel;
        JLabel label;
        
        /* Nastav vlastnosti okÈnek pro zobrazenÌ hodnot */
        idLabel.setEditable(false);
        nameLabel.setEditable(false);
        dphLabel.setEditable(false);
        unitLabel.setEditable(false);
        typeLabel.setEditable(false);
        eanLabel.setEditable(false);

        idLabel.setFocusable(false);
        nameLabel.setFocusable(false);
        dphLabel.setFocusable(false);
        unitLabel.setFocusable(false);
        typeLabel.setFocusable(false);
        eanLabel.setFocusable(false);
        
        /* Vytvo¯ jednotlivÈ poloûky */
        
        label = new JLabel("SkladovÈ ËÌslo: ");
        idLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        idLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 0, 0, 1, 1, 1.0, 1.0, NONE, WEST) );
        content.add( setComponent(idLabel, 1, 0, 3, 1, 0.0, 0.0, HORIZONTAL, WEST) );

        label = new JLabel("N·zev zboûÌ: ");
        nameLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        nameLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 1.0, NONE, WEST) );
        content.add( setComponent(nameLabel, 1, 1, 3, 1, 0.0, 0.0, HORIZONTAL, WEST) );
        
        label = new JLabel("EAN: ");
        eanLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        eanLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 0, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        content.add( setComponent(eanLabel, 0, 3, 1, 1, 1.0, 0.0, HORIZONTAL, WEST) );
        
        label = new JLabel("Jednotka: ");
        unitLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        unitLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 2, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        content.add( setComponent(unitLabel, 2, 3, 1, 1, 1.0, 0.0, HORIZONTAL, WEST) );

        label = new JLabel("DPH: ");
        dphLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        dphLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 3, 2, 1, 1, 1.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 3, 3, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        label = new JLabel("Typ: ");
        typeLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        typeLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 1, 2, 1, 1, 1.0, 1.0, NONE, WEST) );
        content.add( setComponent(typeLabel, 1, 3, 1, 1, 1.0, 0.0, HORIZONTAL, WEST) );

        return content;
    }
    
    /*
     * TlaËÌtka pro editace
     */
    private JPanel createEditButtonPanel () {
        JPanel content = new JPanel();
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Editace zboûÌ"));
        content.setMinimumSize(new Dimension(170, 140)); //MinimalnÌ velikost panelu

        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Nov·", imageIcon);
        newButton.setToolTipText("Vytvo¯Ì novou skladovou kartu");
        newButton.addActionListener(new NewButtonListener());
        newButton.setPreferredSize( new Dimension(150, 24) );
        content.add(newButton);
        
        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("UpravÌ oznaËenou skladovou kartu");
        editButton.addActionListener(new EditButtonListener());
        editButton.setPreferredSize( new Dimension(150, 24) );
        content.add(editButton);
        
        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vymaûe oznaËenou skladovou kartu");
        deleteButton.setPreferredSize( new Dimension(150, 24) );
        deleteButton.addActionListener(new DelButtonListener());
        content.add(deleteButton);
        
        return content;
    }
    
    /*
     *  DoplnÌ panel s volbami pro nalezenÌ skladovÈ karty
     */
    private JPanel createSearchPanel() {
        JPanel content = new JPanel();
        URL iconURL;
        ImageIcon imageIcon;
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "NalezenÌ zboûÌ"));
        
        content.add( new JLabel("  Max. poloûek: ") );
        limitSpinner.setPreferredSize( new Dimension(80, 20) );
        limitSpinner.setValue(currentLimit);
        content.add( limitSpinner );
        
        content.add( new JLabel(" KlÌËovÈ slovo: ") );
        
        /* Kolonka pro nastavenÌ filtru zobrazenÌ*/
        searchTextField = new JTextField(20);
        searchTextField.addKeyListener( new SearchTFKeyListener() );
        searchTextField.setText(currentKeyword);
        content.add(searchTextField);
        
        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "view-refresh16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        searchButton = new JButton("Obnovit", imageIcon);
        searchButton.addActionListener(new SearchButtonListener());
        searchButton.setToolTipText("ZobrazÌ zboûÌ jehoû n·zev, nebo skladovÈ ËÌslo obsahuje zadanÈ slovo");
        content.add(searchButton);
        
        /* Vybr·nÌ zobrazenÌ nulov˝ch karet */
        filtrCheckBox.addActionListener(new FiltrActionListener());
        content.add(filtrCheckBox);
        
        return content;
    }
    
    /**
     * Provede p¯ekreslenÌ tabulky 
     * 
     * @param keyword klÌËovÈ slovo, pro filtrov·nÌ obsahu
     * @param showNull true, jestliûe se majÌ zobrazit i nulovÈ karty, jinak false
     */
    public void refresh(String keyword, boolean showNull) {
        String key = keyword.trim();
        searchTextField.setText(keyword);
        filtrCheckBox.setSelected(showNull);
        
        int row = goodsTable.getSelectedRow();
        
        currentLimit = ((Integer) limitSpinner.getValue()).intValue();
        currentKeyword = keyword;
        
        try {
            if (showNull) {
                getGoodsTableModel().setGoodsData(getStore().getGoodsByKeyword(key, (Integer) limitSpinner.getValue()) ); // Zonraz pouze nenulove
            } else {
                getGoodsTableModel().setGoodsData(getStore().getAllNotZeroGoodsByKeyword(key, (Integer) limitSpinner.getValue()) ); // Zobraz vöe
            }
            
            if (row < goodsTableModel.getRowCount() && row >= 0)
                goodsTable.setRowSelectionInterval(row, row);
            
       } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(StorePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
       }

    }
    
    /**
     *  ZnovunaËte a zobrazÌ ceny zboûÌ
     */
    public void refreshPriceNames() {
        priceTableModel.refreshPriceNames();
    }
    
    /**
     *  Provede p¯ekreslenÌ tabulky za pouûitÌ hodnot zadan˝ch ve formul·¯i
     */
    public void refresh() {
        filtrCheckBox.setSelected(Settings.isShowZeroCards());
        refresh(searchTextField.getText(), Settings.isShowZeroCards());
    }
    
    /**
     * OznaËÌ ¯·dek tabulky, ve kterÈm je zboûÌ s uveden·m skladov˝m ËÌslem
     * @param goodsID skladovÈ ËÌslo
     * @return true, jestliûe zboûÌ nalezl a oznaËil ho, jinak false
     */
    public boolean highlightRow(String goodsID) {

        ListSelectionModel rowSM = getGoodsTable().getSelectionModel();
        
        int rowCount = getGoodsTable().getRowCount();
        
        for (int i = 0; i < rowCount; i++) {
            // Jestliûe naöel odpovÌdajÌcÌ ¯·dek
            if (getGoodsTableModel().getValueAt(i, Columns.ID.getColumnNumber()).equals(goodsID)) {
                getGoodsTable().setRowSelectionInterval(i, i); // oznaË ¯·dek
                return true; // konec
            }
        }
        return false;
    }
    
    
    /**
     *  Vytvo¯Ì novou skladovou kartu a zobrazÌ jÌ v tabulce
     */
    public void newItem() {
        Goods goods = null;
        
        if (owner instanceof Frame )
            goods = EditGoodsDialog.openDialog( (Frame) owner, user); // otev¯i dialog pro editaci
        else 
            goods = EditGoodsDialog.openDialog( (Dialog) owner, user); // otev¯i dialog pro editaci

        if (goods == null) {
            return;
        }

        JOptionPane.showMessageDialog(this, "<html><center>Vytvo¯enÌ skladovÈ karty provedeno</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
            
        /* Obnov data ve vöech tabulk·ch - zobraz i nulovÈ karty (neboù nov· bude mÌt nulovÈ mnoûstvÌ*/
        this.refresh(searchTextField.getText().trim(), true);

    }
    
    /**
     * VracÌ prvnÌ oznaËenÈ zboûÌ, nebo null, pokud nenÌ nic oznaËeno
     * @return 
     */
    public Goods getFirstSelectedGoods() {
        ListSelectionModel listSM = getGoodsTable().getSelectionModel(); /* Zjisti onzaËenÈ ¯·dky */
        int firstRow = listSM.getMinSelectionIndex();
        
        if (firstRow == -1) {
            return null;
        } else {
            return goodsTableModel.getGoodsAt(firstRow);
        }
    }
    
    /**
     *  Vymaûe skladovou kartu zboûÌ, kterÈ je pr·vÏ oznaËeno v tabulce.
     *  V p¯ÌpadÏ, ûe je oznaËeno vÌce zboûÌ, vymaûe vöechny poloûky
     */
    public void editItem() {
        int firstRow = -1;
        Goods goods = null;
        
        ListSelectionModel listSM = getGoodsTable().getSelectionModel(); /* Zjisti onzaËenÈ ¯·dky */
        if ( (firstRow = listSM.getMinSelectionIndex()) == -1 ) {
            JOptionPane.showMessageDialog(this, "<html><center>Nejprve oznaËte zboûÌ, kterÈ chcete upravit.</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
            return; // jestliûe nebylo nic vybr·no, konec
        }


        if (owner instanceof Frame )
            goods = EditGoodsDialog.openDialog( (Frame) owner, user, getGoodsTableModel().getGoodsAt(firstRow) ); // otev¯i dialog pro editaci
        else 
            goods = EditGoodsDialog.openDialog( (Dialog) owner, user, getGoodsTableModel().getGoodsAt(firstRow) ); // otev¯i dialog pro editaci


        if (goods == null) {
            return;
        }
        JOptionPane.showMessageDialog(this, "<html><center>Editace skladovÈ karty provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 

        /* Obnov data ve vöech tabulk·ch */
        // Jestliûe se edituje zboûÌ s nulov˝m stavem, zobraz nulovÈ karty
        if (goodsTableModel.getGoodsAt(firstRow).getQuantity() == 0) {
            this.refresh(searchTextField.getText().trim(), true);
        } else {
            this.refresh();
            // V opaËnÈm p¯ÌpadÏ zobraz i nulovÈ, podle toho co je nastaveno
        }
        
        // P¯i zmÏnÏ zboûÌ, znovuzobraz p¯Ìjemky a v˝dejky
        MainWindow.getInstance().getBuyPanel().refresh();
        MainWindow.getInstance().getSalePanel().refresh();

    }
    
    /**
     *  Vymaûe oznaËenÈ zboûÌ ze skladu a aktualizuje data v tabulce
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat oznaËenÈ skladovÈ karty?";
        Object[] options = {"Ano", "Ne"};
        
        
        /* Zjisti onzaËenÈ ¯·dky */
        ListSelectionModel listSM = getGoodsTable().getSelectionModel();
        int firstRow = listSM.getMinSelectionIndex();
        int lastRow = listSM.getMaxSelectionIndex();
        
        if (firstRow == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaËte zboûÌ, kterÈ chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz·nÌ skladov˝ch karet",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // û·dna vlastnÌ ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliûe nebyl v˝bÏr potvrzen - konec
        }
        
        /* ProveÔ fyzickÈ vymaz·nÌ */
        try {
            DatabaseAccess.setAutoCommit(false); //operaci provedeme aû celou najenou 
            /* Projdi ¯·dky a vymaû odpovÌdajÌcÌ poloûky z datab·ze*/
            for (int i = firstRow; i <= lastRow; i++) {
                
                /* Zkontroluj jestli je tento ¯·dek oznaËen (mezi prvnÌm a poslednÌm oznaËen˝m mohou b˝t i neoznaËenÈ */
                if (listSM.isSelectedIndex(i)) {
                    // Vybere z prvnÌho sloupce skladovÈ ËÌslo a vymaûe p¯ÌsluönÈ zboûÌ 
                    getUser().deleteGoods( getGoodsTableModel().getGoodsAt(i) );
                }
            }
            
            DatabaseAccess.commit(); // ProveÔ veökerÈ vymaz·nÌ najednou
            DatabaseAccess.setAutoCommit(true);
            
            // Obnov zobrazenÌ
            refresh();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(StorePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            try {
                DatabaseAccess.rollBack();
                DatabaseAccess.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(StorePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     * Nalezne prvnÌ zboûÌ v tabulce s v˝pisem skladu a zv˝raznÌ ho. 
     * P¯ÌpadnÏ zobrazÌ chybovou hl·öku p¯i ne˙spÏÏchu
     * 
     * @param keyword klÌËovÈ slovo pro kterÈ se m· vyhledat zboûÌ
     */
    public void findFirstGoods(String keyword) {
        Goods resultGoods = null;
        
        try {
            searchedItems = store.getGoodsByKeyword(keyword, (Integer) limitSpinner.getValue());
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
           
        /* Zkontroluj, zda nÏco nalezl */
        if (searchedItems.isEmpty() || keyword.length() == 0) {
            // Vraù pr·zdn˝ objekt
            resultGoods = new Goods(null, "", -1, -1, "", "", -1, -1, -1, -1, -1, -1); 
        } else {
            lastGoods = searchedItems.get(0); // V˝sledek je prvnÌ nalezen· hodnota 
            resultGoods = lastGoods;
        }
        lastKeyword = keyword;
        
        // Jestliûe nejsou v tabulce zobrazeny hledanÈ hodnoty, doplÚ je
        // nebo p¯epiö, starÈ, jestliûe se mezitÌm v tabulce objevily
        //for (Goods i: searchedItems) {
        //    goodsTableModel.replaceRow(i, i);
        //}
        
        /* Zkus zboûÌ oznaËit, v p¯ÌpadÏ ne˙spÏchu vypiö informaci o nenalezenÈm zboûÌ */
        if (highlightRow(resultGoods.getGoodsID()) == false ) {
            String report = (searchTextField.getText().trim().length() == 0) ? "" : "Zkuste zmÏnit filtr";
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, report);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    /**
     * Nalezne dalöÌ zboûÌ ve skladÏ, kterÈ odpovÌd· hledanÈmu slovu.
     * Hled· od naposledy nalezenÈho v˝skytu.
     * @param keyword 
     */
    public void findNextGoods(String keyword) {
        Goods resultGoods = null;
        
        /* Jestliûe jeötÏ nebylo hled·no, 
         * nebo se zmÏnilo klÌËovÈ slovo 
         * chovej se stejnÏ, jako kdyby hledal prvnÌ
         */ 
        if (searchedItems.isEmpty() || 
            !lastKeyword.equalsIgnoreCase(keyword) ) {
               
            findFirstGoods(keyword);
            return;
        }
           
        int lastIndex = searchedItems.indexOf(lastGoods);
           
        /* Jestliûe naposledy byla vybÌr·na poslednÌ poloûka pole*/
        if (lastIndex+1 >= searchedItems.size()) {
            resultGoods = searchedItems.get(0); // Vyber prvnÌ z pole
        } else {
            resultGoods = searchedItems.get(lastIndex + 1); // Vyber n·sledujÌcÌ 
        }
           
        lastGoods = resultGoods;        
        lastKeyword = keyword;
        
        /* Zkus zboûÌ oznaËit, v p¯ÌpadÏ ne˙spÏchu vypiö informaci o nenalezenÈm zboûÌ */
        if (highlightRow(resultGoods.getGoodsID()) == false ) {
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
   
    /**
     *  PosluchaË v˝bÏru ¯·dky v tabulce zboûÌ 
     */
    private class SelectRowGoodsTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // z·br·nÌ zdvojenÈmu vyvol·nÌ uud·losti (v˝znam mi nenÌ p¯esnÏ zn·m)
           if (e.getValueIsAdjusting()) return;
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // zÌskej model v˝bÏru
           /* Zjisti index naËtenÈho ¯·dku */
           int selectedRow;
           if ( (selectedRow = lsm.getMinSelectionIndex()) == -1) {
                priceTable.setVisible(false);
                return;
           }
            priceTable.setVisible(true);
           
           Goods goods = getGoodsTableModel().getGoodsAt(selectedRow); // vyber hodnotu  na p¯ÌsluönÈm ¯·dku

            /* Nastav ceny zboûÌ */
            priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph()); 

            /* Nastav text v panelu skladov· karta */
            idLabel.setText(goods.getGoodsID());
            nameLabel.setText(goods.getName()); 
            typeLabel.setText( TypeOfGoods.getTypeOfGoods(goods.getType()).toString() );
            dphLabel.setText( String.valueOf(goods.getDph()) + "%" );
            eanLabel.setText(goods.getEan());
            unitLabel.setText(goods.getUnit());
        }
    }
    
    /**
     *  PosluchaË pro tlaËÌtko nalezenÌ poloûky
     */
    private class SearchButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }
    
    /**
     *  PosluchaË zmÏny zobrazenÌ nulov˝ch karet
     */
    private class FiltrActionListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            Settings.setShowZeroCards(filtrCheckBox.isSelected());
            refresh();
        }
    }
    
    /**
     *  PosluchaË stisku tlaËÌtka NovÈ zoûÌ
     */
    private class NewButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            newItem(); // Vytbo¯ novÈ zboûÌ
        }
    }
    
    /**
     *  PosluchaË stisku tlaËÌtka Editace zoûÌ
     */
    private class EditButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            editItem(); // ZmÏÚ zboûÌ
        }
    }
    
    /**
     *  PosluchaË stisku tlaËÌtka NovÈ zoûÌ
     */
    private class DelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            deleteItem(); // Vymaû zboûÌ
        }
    }
    
    
    private class SearchTFKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    searchButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE :
                    searchTextField.setText("");
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }   
    
    private class GoodsTableKeyListener implements KeyListener {
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
            
            if (ctrlPress && e.getKeyCode() == KeyEvent.VK_P) {
                MainWindow.getInstance().printStore();
                altPress = false;
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
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.GOODS_TIP);
        }

        public void focusLost(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.EMPTY);
        }
        
    }
    
    
    /*
     * NastavÌ data v tabulce zboûÌ
     */
    void setGoodsTableData(ArrayList<Goods> goods) {
        getGoodsTableModel().setGoodsData(goods);
    }
    
    /**
     * Vr·tÌ tabulku se zboûÌm
     * @return tabulka s v˝pisem skladu 
     */
    JTable getGoodsTable() {
        return goodsTable;
    }

    
    /**
     * VracÌ model tabulky se zboûÌm
     * @return model tabulky
     */
    GoodsTableModel getGoodsTableModel() {
        return goodsTableModel;
    }

    /**
     * VracÌ odkaz na sklad
     * @return odkaz na sklad
     */
    public Store getStore() {
        return store;
    }

    /**
     * VracÌ p¯ihl·öenÈho uûivatele
     * @return p¯ihl·öen˝ uûivatel 
     */
    public User getUser() {
        return user;
    }
    
    /**
     *  VracÌ souËasn˝ limit zobrazenÌ poloûek
     *  retrun souËasn˝ limit
     */
    public static int getCurrentLimit() {
        return currentLimit;
    }


    public static String getCurrentKeyword() {
        return currentKeyword;
    }

}
