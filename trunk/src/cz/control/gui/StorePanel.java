/*
 * StorePanel.java
 *
 * Created on 26. z��� 2005, 22:07
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
 * T��da tvo��c� panel pro zobrazen� v�pisu zbo�� na sklad�. Umo��uje 
 * zobrazen�, editaci, vytv��en� a maz�n� zbo�� na sklad�
 * @author Kamil Je�ek
 */
public class StorePanel extends JPanel {
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // p�ihl�en� u�ivatel 
    private Store store = null; // Ukazatel na sklad 
    private ArrayList<Goods> goods = new ArrayList<Goods>(); // Pole zbo�� 

    private PriceTableModel priceTableModel;
    private GoodsTableModel goodsTableModel;
    private JTable goodsTable; // tabulka zbo��
    private JTable priceTable;
    private JTextField searchTextField; // ok�nko pro zad�n� slova pro vyhled�v�n� 
    private JButton searchButton;
    private JCheckBox filtrCheckBox = new JCheckBox("Zobrazit nulov� karty");
    
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
            
    /* Polo�ky v detailu skladov� karty */
    private JTextField idLabel = new JTextField(""); // skladov� ��slo
    private JTextField nameLabel = new JTextField(""); // jm�no zbo��
    private JTextField typeLabel = new JTextField(""); // typ polo�ky (zbo��, komplet, ...)
    private JTextField dphLabel = new JTextField(""); // DPH
    private JTextField eanLabel = new JTextField(""); // ean
    private JTextField unitLabel = new JTextField(""); // mno�stevn� jednota 
    
    /* Nalezen� zbo�� ze skladu */
    private static java.util.List<Goods> searchedItems = new ArrayList<Goods>(); 
    /* posledn� vr�cen� polo�ka */
    private static Goods lastGoods = null;
    /* Posledn� kl��ov� slovo pro vyhled�v�n� */
    private static String lastKeyword = "";

    // limit zobrazen�
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
     * Nastav� vlastnosti okna 
     */
    private void setPanel() {
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        filtrCheckBox.setSelected(Settings.isShowZeroCards());
        
        try {
            store = user.openStore(); /* Otev�i sklad pro p�ihl�en�ho u�ivatele */
            // Nahrazeno vol�n� metody refresh() na konci t�to metody            
            // goods = getStore().getAllGoods(); /* Na�ti seznam zbo�� */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.STORE.getIndex(), false);
            return;

        }
        
        this.setLayout(gbl);
        this.add(setComponent(createGoodsTablePanel(), 0, 0, 3, 1, 1.0, 1.0, BOTH, NORTH));
        this.add(setComponent(createPricePanel(), 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        this.add(setComponent(createGoodsPanel(), 1, 1, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        this.add(setComponent(createEditButtonPanel(), 2, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        this.add(setComponent(createSearchPanel(), 0, 2, 3, 1, 1.0, 0.0, HORIZONTAL, WEST));
        
        refresh(); // Zobraz zbo�� v tabulce      
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
     * Vytvo�� tabulku s v�pisem zbo�� na sklad�
     */
    private JPanel createGoodsTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Seznam zbo��"));

        goodsTableModel = new GoodsTableModel(goods);
        goodsTable = new CommonTable(getGoodsTableModel()); // vytvo�en� tabulky
        getGoodsTable().setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        TableColumnModel columnModel = getGoodsTable().getColumnModel();
        
        goodsTable.addKeyListener( new GoodsTableKeyListener() );
        goodsTable.addFocusListener( new ItemTableListener() );

        /* Nastav ���ky sloupc� */
        columnModel.getColumn(Columns.ID.getColumnNumber()).setPreferredWidth(Columns.ID.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setPreferredWidth(Columns.NAME.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setPreferredWidth(Columns.QUANTITY.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setPreferredWidth(Columns.UNIT.getColumnWidth()); // ���ka slouce "skladov� ��slo 

        /** Default renderer */
//        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
//        renderer.setFont(new Font("Dialog", Font.PLAIN, Settings.getMainItemsFontSize()));

        TableCellRenderer renderer = new CommonItemCellRenderer();

        /* Nastav zobrazen� slouc� */
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
     *  Panel s v�pisem cen
     */
    private JPanel createPricePanel() {
        JPanel content = new JPanel(new GridLayout(1,1)); // GridLayer proto, aby bylo mo�no nastavit velikost panelu
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        
        priceTableModel = new PriceTableModel(0, 0, 0, 0, 0, 0);
        priceTable = new CommonTable(priceTableModel);
        
        TableColumnModel columnModel = priceTable.getColumnModel();
        /* Nastav zobrazen� slouc� */
        columnModel.getColumn(PriceTableColumns.PRICE.getNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(PriceTableColumns.PRICE_DPH.getNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());
        
        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        priceTable.setVisible(false);
        priceTable.setFocusable(false);
        
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(300, 140)); //Minimaln� velikost panelu
        content.add(scrollPane);
        
        return content;
    }
    
    /*
     * Panel s v�pisem karty zbo��
     */
    private JPanel createGoodsPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Skladov� karta"));
        content.setMinimumSize( new Dimension(200, 127));
        content.setLayout(gbl);
        JPanel panel;
        JLabel label;
        
        /* Nastav vlastnosti ok�nek pro zobrazen� hodnot */
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
        
        /* Vytvo� jednotliv� polo�ky */
        
        label = new JLabel("Skladov� ��slo: ");
        idLabel.setFont( new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()));
        idLabel.setForeground(Color.BLACK);
        content.add( setComponent(label, 0, 0, 1, 1, 1.0, 1.0, NONE, WEST) );
        content.add( setComponent(idLabel, 1, 0, 3, 1, 0.0, 0.0, HORIZONTAL, WEST) );

        label = new JLabel("N�zev zbo��: ");
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
     * Tla��tka pro editace
     */
    private JPanel createEditButtonPanel () {
        JPanel content = new JPanel();
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Editace zbo��"));
        content.setMinimumSize(new Dimension(170, 140)); //Minimaln� velikost panelu

        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Nov�", imageIcon);
        newButton.setToolTipText("Vytvo�� novou skladovou kartu");
        newButton.addActionListener(new NewButtonListener());
        newButton.setPreferredSize( new Dimension(150, 24) );
        content.add(newButton);
        
        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Uprav� ozna�enou skladovou kartu");
        editButton.addActionListener(new EditButtonListener());
        editButton.setPreferredSize( new Dimension(150, 24) );
        content.add(editButton);
        
        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vyma�e ozna�enou skladovou kartu");
        deleteButton.setPreferredSize( new Dimension(150, 24) );
        deleteButton.addActionListener(new DelButtonListener());
        content.add(deleteButton);
        
        return content;
    }
    
    /*
     *  Dopln� panel s volbami pro nalezen� skladov� karty
     */
    private JPanel createSearchPanel() {
        JPanel content = new JPanel();
        URL iconURL;
        ImageIcon imageIcon;
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Nalezen� zbo��"));
        
        content.add( new JLabel("  Max. polo�ek: ") );
        limitSpinner.setPreferredSize( new Dimension(80, 20) );
        limitSpinner.setValue(currentLimit);
        content.add( limitSpinner );
        
        content.add( new JLabel(" Kl��ov� slovo: ") );
        
        /* Kolonka pro nastaven� filtru zobrazen�*/
        searchTextField = new JTextField(20);
        searchTextField.addKeyListener( new SearchTFKeyListener() );
        searchTextField.setText(currentKeyword);
        content.add(searchTextField);
        
        iconURL = StorePanel.class.getResource(Settings.ICON_URL + "view-refresh16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        searchButton = new JButton("Obnovit", imageIcon);
        searchButton.addActionListener(new SearchButtonListener());
        searchButton.setToolTipText("Zobraz� zbo�� jeho� n�zev, nebo skladov� ��slo obsahuje zadan� slovo");
        content.add(searchButton);
        
        /* Vybr�n� zobrazen� nulov�ch karet */
        filtrCheckBox.addActionListener(new FiltrActionListener());
        content.add(filtrCheckBox);
        
        return content;
    }
    
    /**
     * Provede p�ekreslen� tabulky 
     * 
     * @param keyword kl��ov� slovo, pro filtrov�n� obsahu
     * @param showNull true, jestli�e se maj� zobrazit i nulov� karty, jinak false
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
                getGoodsTableModel().setGoodsData(getStore().getAllNotZeroGoodsByKeyword(key, (Integer) limitSpinner.getValue()) ); // Zobraz v�e
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
     *  Znovuna�te a zobraz� ceny zbo��
     */
    public void refreshPriceNames() {
        priceTableModel.refreshPriceNames();
    }
    
    /**
     *  Provede p�ekreslen� tabulky za pou�it� hodnot zadan�ch ve formul��i
     */
    public void refresh() {
        filtrCheckBox.setSelected(Settings.isShowZeroCards());
        refresh(searchTextField.getText(), Settings.isShowZeroCards());
    }
    
    /**
     * Ozna�� ��dek tabulky, ve kter�m je zbo�� s uveden�m skladov�m ��slem
     * @param goodsID skladov� ��slo
     * @return true, jestli�e zbo�� nalezl a ozna�il ho, jinak false
     */
    public boolean highlightRow(String goodsID) {

        ListSelectionModel rowSM = getGoodsTable().getSelectionModel();
        
        int rowCount = getGoodsTable().getRowCount();
        
        for (int i = 0; i < rowCount; i++) {
            // Jestli�e na�el odpov�daj�c� ��dek
            if (getGoodsTableModel().getValueAt(i, Columns.ID.getColumnNumber()).equals(goodsID)) {
                getGoodsTable().setRowSelectionInterval(i, i); // ozna� ��dek
                return true; // konec
            }
        }
        return false;
    }
    
    
    /**
     *  Vytvo�� novou skladovou kartu a zobraz� j� v tabulce
     */
    public void newItem() {
        Goods goods = null;
        
        if (owner instanceof Frame )
            goods = EditGoodsDialog.openDialog( (Frame) owner, user); // otev�i dialog pro editaci
        else 
            goods = EditGoodsDialog.openDialog( (Dialog) owner, user); // otev�i dialog pro editaci

        if (goods == null) {
            return;
        }

        JOptionPane.showMessageDialog(this, "<html><center>Vytvo�en� skladov� karty provedeno</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
            
        /* Obnov data ve v�ech tabulk�ch - zobraz i nulov� karty (nebo� nov� bude m�t nulov� mno�stv�*/
        this.refresh(searchTextField.getText().trim(), true);

    }
    
    /**
     * Vrac� prvn� ozna�en� zbo��, nebo null, pokud nen� nic ozna�eno
     * @return 
     */
    public Goods getFirstSelectedGoods() {
        ListSelectionModel listSM = getGoodsTable().getSelectionModel(); /* Zjisti onza�en� ��dky */
        int firstRow = listSM.getMinSelectionIndex();
        
        if (firstRow == -1) {
            return null;
        } else {
            return goodsTableModel.getGoodsAt(firstRow);
        }
    }
    
    /**
     *  Vyma�e skladovou kartu zbo��, kter� je pr�v� ozna�eno v tabulce.
     *  V p��pad�, �e je ozna�eno v�ce zbo��, vyma�e v�echny polo�ky
     */
    public void editItem() {
        int firstRow = -1;
        Goods goods = null;
        
        ListSelectionModel listSM = getGoodsTable().getSelectionModel(); /* Zjisti onza�en� ��dky */
        if ( (firstRow = listSM.getMinSelectionIndex()) == -1 ) {
            JOptionPane.showMessageDialog(this, "<html><center>Nejprve ozna�te zbo��, kter� chcete upravit.</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
            return; // jestli�e nebylo nic vybr�no, konec
        }


        if (owner instanceof Frame )
            goods = EditGoodsDialog.openDialog( (Frame) owner, user, getGoodsTableModel().getGoodsAt(firstRow) ); // otev�i dialog pro editaci
        else 
            goods = EditGoodsDialog.openDialog( (Dialog) owner, user, getGoodsTableModel().getGoodsAt(firstRow) ); // otev�i dialog pro editaci


        if (goods == null) {
            return;
        }
        JOptionPane.showMessageDialog(this, "<html><center>Editace skladov� karty provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 

        /* Obnov data ve v�ech tabulk�ch */
        // Jestli�e se edituje zbo�� s nulov�m stavem, zobraz nulov� karty
        if (goodsTableModel.getGoodsAt(firstRow).getQuantity() == 0) {
            this.refresh(searchTextField.getText().trim(), true);
        } else {
            this.refresh();
            // V opa�n�m p��pad� zobraz i nulov�, podle toho co je nastaveno
        }
        
        // P�i zm�n� zbo��, znovuzobraz p��jemky a v�dejky
        MainWindow.getInstance().getBuyPanel().refresh();
        MainWindow.getInstance().getSalePanel().refresh();

    }
    
    /**
     *  Vyma�e ozna�en� zbo�� ze skladu a aktualizuje data v tabulce
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� skladov� karty?";
        Object[] options = {"Ano", "Ne"};
        
        
        /* Zjisti onza�en� ��dky */
        ListSelectionModel listSM = getGoodsTable().getSelectionModel();
        int firstRow = listSM.getMinSelectionIndex();
        int lastRow = listSM.getMaxSelectionIndex();
        
        if (firstRow == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te zbo��, kter� chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� skladov�ch karet",
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
            DatabaseAccess.setAutoCommit(false); //operaci provedeme a� celou najenou 
            /* Projdi ��dky a vyma� odpov�daj�c� polo�ky z datab�ze*/
            for (int i = firstRow; i <= lastRow; i++) {
                
                /* Zkontroluj jestli je tento ��dek ozna�en (mezi prvn�m a posledn�m ozna�en�m mohou b�t i neozna�en� */
                if (listSM.isSelectedIndex(i)) {
                    // Vybere z prvn�ho sloupce skladov� ��slo a vyma�e p��slu�n� zbo�� 
                    getUser().deleteGoods( getGoodsTableModel().getGoodsAt(i) );
                }
            }
            
            DatabaseAccess.commit(); // Prove� ve�ker� vymaz�n� najednou
            DatabaseAccess.setAutoCommit(true);
            
            // Obnov zobrazen�
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
     * Nalezne prvn� zbo�� v tabulce s v�pisem skladu a zv�razn� ho. 
     * P��padn� zobraz� chybovou hl�ku p�i ne�sp��chu
     * 
     * @param keyword kl��ov� slovo pro kter� se m� vyhledat zbo��
     */
    public void findFirstGoods(String keyword) {
        Goods resultGoods = null;
        
        try {
            searchedItems = store.getGoodsByKeyword(keyword, (Integer) limitSpinner.getValue());
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
           
        /* Zkontroluj, zda n�co nalezl */
        if (searchedItems.isEmpty() || keyword.length() == 0) {
            // Vra� pr�zdn� objekt
            resultGoods = new Goods(null, "", -1, -1, "", "", -1, -1, -1, -1, -1, -1); 
        } else {
            lastGoods = searchedItems.get(0); // V�sledek je prvn� nalezen� hodnota 
            resultGoods = lastGoods;
        }
        lastKeyword = keyword;
        
        // Jestli�e nejsou v tabulce zobrazeny hledan� hodnoty, dopl� je
        // nebo p�epi�, star�, jestli�e se mezit�m v tabulce objevily
        //for (Goods i: searchedItems) {
        //    goodsTableModel.replaceRow(i, i);
        //}
        
        /* Zkus zbo�� ozna�it, v p��pad� ne�sp�chu vypi� informaci o nenalezen�m zbo�� */
        if (highlightRow(resultGoods.getGoodsID()) == false ) {
            String report = (searchTextField.getText().trim().length() == 0) ? "" : "Zkuste zm�nit filtr";
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, report);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    /**
     * Nalezne dal�� zbo�� ve sklad�, kter� odpov�d� hledan�mu slovu.
     * Hled� od naposledy nalezen�ho v�skytu.
     * @param keyword 
     */
    public void findNextGoods(String keyword) {
        Goods resultGoods = null;
        
        /* Jestli�e je�t� nebylo hled�no, 
         * nebo se zm�nilo kl��ov� slovo 
         * chovej se stejn�, jako kdyby hledal prvn�
         */ 
        if (searchedItems.isEmpty() || 
            !lastKeyword.equalsIgnoreCase(keyword) ) {
               
            findFirstGoods(keyword);
            return;
        }
           
        int lastIndex = searchedItems.indexOf(lastGoods);
           
        /* Jestli�e naposledy byla vyb�r�na posledn� polo�ka pole*/
        if (lastIndex+1 >= searchedItems.size()) {
            resultGoods = searchedItems.get(0); // Vyber prvn� z pole
        } else {
            resultGoods = searchedItems.get(lastIndex + 1); // Vyber n�sleduj�c� 
        }
           
        lastGoods = resultGoods;        
        lastKeyword = keyword;
        
        /* Zkus zbo�� ozna�it, v p��pad� ne�sp�chu vypi� informaci o nenalezen�m zbo�� */
        if (highlightRow(resultGoods.getGoodsID()) == false ) {
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
   
    /**
     *  Poslucha� v�b�ru ��dky v tabulce zbo�� 
     */
    private class SelectRowGoodsTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting()) return;
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // z�skej model v�b�ru
           /* Zjisti index na�ten�ho ��dku */
           int selectedRow;
           if ( (selectedRow = lsm.getMinSelectionIndex()) == -1) {
                priceTable.setVisible(false);
                return;
           }
            priceTable.setVisible(true);
           
           Goods goods = getGoodsTableModel().getGoodsAt(selectedRow); // vyber hodnotu  na p��slu�n�m ��dku

            /* Nastav ceny zbo�� */
            priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph()); 

            /* Nastav text v panelu skladov� karta */
            idLabel.setText(goods.getGoodsID());
            nameLabel.setText(goods.getName()); 
            typeLabel.setText( TypeOfGoods.getTypeOfGoods(goods.getType()).toString() );
            dphLabel.setText( String.valueOf(goods.getDph()) + "%" );
            eanLabel.setText(goods.getEan());
            unitLabel.setText(goods.getUnit());
        }
    }
    
    /**
     *  Poslucha� pro tla��tko nalezen� polo�ky
     */
    private class SearchButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }
    
    /**
     *  Poslucha� zm�ny zobrazen� nulov�ch karet
     */
    private class FiltrActionListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            Settings.setShowZeroCards(filtrCheckBox.isSelected());
            refresh();
        }
    }
    
    /**
     *  Poslucha� stisku tla��tka Nov� zo��
     */
    private class NewButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            newItem(); // Vytbo� nov� zbo��
        }
    }
    
    /**
     *  Poslucha� stisku tla��tka Editace zo��
     */
    private class EditButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            editItem(); // Zm�� zbo��
        }
    }
    
    /**
     *  Poslucha� stisku tla��tka Nov� zo��
     */
    private class DelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            deleteItem(); // Vyma� zbo��
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
     * Nastav� data v tabulce zbo��
     */
    void setGoodsTableData(ArrayList<Goods> goods) {
        getGoodsTableModel().setGoodsData(goods);
    }
    
    /**
     * Vr�t� tabulku se zbo��m
     * @return tabulka s v�pisem skladu 
     */
    JTable getGoodsTable() {
        return goodsTable;
    }

    
    /**
     * Vrac� model tabulky se zbo��m
     * @return model tabulky
     */
    GoodsTableModel getGoodsTableModel() {
        return goodsTableModel;
    }

    /**
     * Vrac� odkaz na sklad
     * @return odkaz na sklad
     */
    public Store getStore() {
        return store;
    }

    /**
     * Vrac� p�ihl�en�ho u�ivatele
     * @return p�ihl�en� u�ivatel 
     */
    public User getUser() {
        return user;
    }
    
    /**
     *  Vrac� sou�asn� limit zobrazen� polo�ek
     *  retrun sou�asn� limit
     */
    public static int getCurrentLimit() {
        return currentLimit;
    }


    public static String getCurrentKeyword() {
        return currentKeyword;
    }

}
