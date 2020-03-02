/*
 * SalePanel.java
 *
 * Vytvoøeno 16. únor 2006, 12:05
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.Customer;
import cz.control.data.TradeItem;
import cz.control.data.TradeItemPreview;
import cz.control.business.*;

import cz.control.data.Goods;
import cz.control.gui.common.DateAndQuantityIntervalPanel;
import cz.control.gui.common.LimitsChangedListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.net.*;

import java.sql.SQLException;
import java.util.*;
import java.text.*;
import java.math.BigDecimal;

import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí panel s pøehledem výdejek
 *
 * (C) 2005, ver. 1.0
 */
public class SalePanel extends JPanel implements LimitsChangedListener {
    
    public static enum PANEL_TYPES {SALE, DISCOUNT};
    
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // pøihlášený uživatel 
    private Sale sale = null; // Ukazatel na výdejky
    private Customers customers = null; // ukazatel na odbìratele
    private Component owner = null;
    
    private TradeItemPreview lastSelectedTrade = null; // Naposledy vybraný pøehled
    
    private JList list = new JList(); // JList s pøehledem všech pøíjemek
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastavenÝ seznamu */ 
    private JTable goodsTable; // Tabulka s jednotlivými položkami pøíjemky
    private BuyTableModel goodsTableModel; // model tabulky 
    
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton printButton;

    
    // položky okénka souètu cen
    private JLabel priceLabel; // cena zboží
    private JLabel dphLabel; // celková daò
    private JLabel reductionTypLabel; // výbìr slevy/pøirážky
    private JLabel reductionLabel; // celková sleva
    private JLabel totalPriceLabel; // celková cena
    
    // položky okénka dodavatel
    private JLabel supNameLabel;    // jméno dodavatele
    private JLabel supPersonLabel;  // jméno kontatní osoby
    private JLabel adressLabel;     // adresa dodavatele
    private JLabel telLabel;        // telefon dodavatele
    
    private Date startDate = new Date();
    private Date endDate = new Date();
    private Integer limit;

    
    
    private DateAndQuantityIntervalPanel limitsPanel;
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private PANEL_TYPES panelType;
    
    /**
     * Vytvoøí panel pro zobrazení výdejek
     * @param owner
     * @param user
     * @return
     */
    public static SalePanel createPanelForSale(Frame owner, User user) {
        return new SalePanel(owner, user, PANEL_TYPES.SALE);
    }
    
    /**
     * Vytvoøí panel pro zobrazení prodejek - vytvoøených maloobcodním prodejem
     * 
     * @param owner
     * @param user
     * @return
     */
    public static SalePanel createPanelForDiscount(Frame owner, User user) {
        return new SalePanel(owner, user, PANEL_TYPES.DISCOUNT);
    }
    
    /** Vytvoøí nový objekt SalePanel */
    private SalePanel(Frame owner, User user, PANEL_TYPES panelType) {
        this.panelType = panelType;
        this.user = user;
        this.owner = owner;
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        try {
            sale = user.openSale(); /* Otevøi výdejky pro pøihlášeného uživatele */
            customers = user.openCustomers(); // otevøi dodavatele
        } catch (InvalidPrivilegException e) {
            // Ukonèi zavádìní okna a nastav záložku jako nepøístupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.SALE.getIndex(), false);
            return;
        }
        
        this.setLayout( new BorderLayout() );
        
        this.add(createFilterPanel() , BorderLayout.NORTH); 
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s pøehledem výdejek
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotlivými položkami výdejky
        this.add(createBottomPanel(), BorderLayout.SOUTH); // spodní panel
        
        refresh(); // Zobraz pøehled
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
    
    private Component createFilterPanel() {
        
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH)); //Zaèátek mìsíce
        startDate.setTime(cal.getTimeInMillis());

        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec mìsíce
        endDate.setTime(cal.getTimeInMillis());
        limit = 1000;
        
        DateAndQuantityIntervalPanel content = new DateAndQuantityIntervalPanel(startDate, endDate, limit);
        content.addLimitsChangedListener(this);
        
        return content;
    }
        
    
    /**
     * Vytvoøí panel s pøehledem všech výdejek
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Pøehled"));
        
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new buyListingLSTListener());
        list.addKeyListener( new SaleKeyListener() );
        list.addFocusListener( new ItemTableListener() );
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 0.0, 1.0, VERTICAL, CENTER));
        
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        buttons.setMinimumSize( new Dimension(150, 115) );
        buttons.setPreferredSize( new Dimension(150, 115) );
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Export16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Vydat", imageIcon);
        newButton.setToolTipText("Vydá zboží ze sklad");
        newButton.addActionListener( new DoBuyListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zmìnu výdejky");
        editButton.addActionListener( new EditBuyListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstraní výdejku");
        deleteButton.addActionListener( new DelBuyListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     * Vytvoøí panel s jednotlivými položkami výdejky 
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Položky zboží"));

        goodsTableModel = new BuyTableModel();
        goodsTable = new CommonTable(goodsTableModel); // vytvoøení tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditelné vertikální linky v tabulce
        goodsTable.addKeyListener( new SaleKeyListener() );
        goodsTable.addFocusListener( new ItemTableListener() );
        
        TableColumnModel columnModel = goodsTable.getColumnModel();
        /* Nastav šíøky sloupcù */
        columnModel.getColumn(BuyColumns.ID.getColumnNumber()).setPreferredWidth(BuyColumns.ID.getColumnWidth()); // šíøka slouce "skladové èíslo 
        columnModel.getColumn(BuyColumns.NAME.getColumnNumber()).setPreferredWidth(BuyColumns.NAME.getColumnWidth()); // šíøka slouce "skladové èíslo 
        columnModel.getColumn(BuyColumns.QUANTITY.getColumnNumber()).setPreferredWidth(BuyColumns.QUANTITY.getColumnWidth()); // šíøka slouce "skladové èíslo 
        columnModel.getColumn(BuyColumns.UNIT.getColumnNumber()).setPreferredWidth(BuyColumns.UNIT.getColumnWidth()); // šíøka slouce "skladové èíslo 
        /* Nastav zobrazení sloucù */
        columnModel.getColumn(BuyColumns.PRICE.getColumnNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(BuyColumns.QUANTITY.getColumnNumber()).setCellRenderer(new QuantityCellRenderer());
        columnModel.getColumn(BuyColumns.NAME.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(BuyColumns.ID.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(BuyColumns.UNIT.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(BuyColumns.DPH.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());

        JScrollPane scrollPane = new JScrollPane(goodsTable);

        content.add(setComponent(scrollPane, 0, 0, 3, 1, 1.0, 1.0, BOTH, CENTER));
        content.add(setComponent(createSumPanel(), 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(createSuplierPanel(), 1, 1, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        
        /* Panel s tlaèítky pro tisk a vyhledání zboží */
        JPanel buttons = new JPanel(); // panel stlaøítky
        buttons.setMinimumSize( new Dimension(140, 115));
        buttons.setPreferredSize( new Dimension(140, 115));
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zboží", imageIcon);
        button.setToolTipText("Vyhledá kartu zboží");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new FindGoodsListener() );
        buttons.add(button);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Odbìratel", imageIcon);
        button.setToolTipText("Vyhledá odìratele");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new FindCustomerListener() );
        buttons.add(button);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        printButton = new JButton("Tisk", imageIcon);
        printButton.setToolTipText("Vytiskne výdejový doklad");
        printButton.setPreferredSize( new Dimension(130, 24) );
        printButton.addActionListener( new PrintBuyListener() );
        buttons.add(printButton);
        
        content.add(setComponent(buttons, 2, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        
        return content;
        
    }
    
    /**
     * Panel možností nastavení slevy a zobrazením souètu cen
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Celková cena"));
        content.setLayout(gbl);
        content.setMinimumSize( new Dimension(300, 115));
        content.setPreferredSize( new Dimension(300, 115));

        JLabel label;
        JLabel kcLabel = new JLabel(" Kè ");
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        reductionTypLabel = new JLabel("Sleva");
        reductionTypLabel.setFont(font);
        kcLabel = new JLabel(" % ");
        reductionLabel = new JLabel("0,00");
        reductionLabel.setFont(font);
        content.add( setComponent(reductionTypLabel, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(reductionLabel, 1, 0, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 0, 1, 1, 0.0, 0.0, NONE, WEST) );

        label = new JLabel(" Cena bez DPH: ");
        priceLabel = new JLabel("0,00");
        priceLabel.setFont(font);
        kcLabel = new JLabel(" Kè ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" DPH ");
        kcLabel = new JLabel(" Kè ");
        dphLabel = new JLabel("0,00");
        dphLabel.setFont(font);
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" Cena s DPH: ");
        kcLabel = new JLabel(" Kè ");
        totalPriceLabel = new JLabel("0,00");
        totalPriceLabel.setFont(font);
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(totalPriceLabel, 1, 3, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvoøí panel s informacemi o dodavateli zboží
     */
    private JPanel createSuplierPanel() {
        JPanel content = new JPanel();
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Odbìratel"));
        content.setMinimumSize( new Dimension(250, 115));
        content.setPreferredSize( new Dimension(250, 115));
        content.setLayout(gbl);
        
        supNameLabel = new JLabel();
        supNameLabel.setFont(font);
        JLabel label = new JLabel(" Odbìratel: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(supNameLabel, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        supPersonLabel = new JLabel();
        supPersonLabel.setFont(font);
        label = new JLabel(" Kontaktní osoba: ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(supPersonLabel, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        adressLabel = new JLabel();
        adressLabel.setFont(font);
        label = new JLabel(" Adresa: ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(adressLabel, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        telLabel = new JLabel();
        telLabel.setFont(font);
        label = new JLabel(" Telefon: ");
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(telLabel, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        return content;
    }
    
    /**
     *  Vytvoøí spodní panel s tlaèítkem pro novou pøíjemku
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        return content;
    }
    
    /**
     *  Zvýrazní konkrétní výdejku
     */
    private void refresh(TradeItemPreview tradeItemPreview) {
        refresh();
        list.setSelectedValue(tradeItemPreview, true);
        refreshGoods(tradeItemPreview);
    }
    
    public void refresh(Date startDate, Date endDate, Integer max) {
        this.startDate.setTime(startDate.getTime());
        this.endDate.setTime(endDate.getTime());
        this.limit = new Integer(max);
        
        refresh();
    }
    
    /**   
     *  Aktualizuje pøehledy výdejek
     *  Tato metoda by mìla být volána, jestliže se zmìní pøehled pøíjemek v databázi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            List<TradeItemPreview> items = null;
            
            switch (panelType) {
                case DISCOUNT :
                    items = sale.getAllDiscountOnly(startDate, endDate, limit ); // Naèti všechny pøehledy
                    break;
                case SALE :
                    items = sale.getAllSaleOnly(startDate, endDate, limit); // Naèti všechny pøehledy
                    break;
            }
            
            list.setListData( items.toArray() );
            list.setSelectedIndex(row); 
            
            if (lastSelectedTrade != null) {
                refreshGoods(lastSelectedTrade);
            }
                
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Aktualizuje hodnoty v tabulce výdejek
     * @param tradeItemPreview pøehled výdejky, která se má zobrazit
     */
    private void refreshGoods(TradeItemPreview tradeItemPreview) {
        List<TradeItem> items;
        int row = goodsTable.getSelectedRow();
        try {
            items = sale.getAllSaleItem(tradeItemPreview);
            goodsTableModel.setData(items);
            
            if (row < goodsTableModel.getRowCount() && row >= 0) {
                goodsTable.setRowSelectionInterval(row, row);
            }
            
            
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        // Nastav ceny výdejky
        if (tradeItemPreview.getReduction() < 0) {
            reductionTypLabel.setText("Pøirážka");
        } else {
            reductionTypLabel.setText("Sleva");
        }
        
        BigDecimal reduction = new BigDecimal(tradeItemPreview.getReduction());
        BigDecimal totalPriceDPH = new BigDecimal(tradeItemPreview.getTotalPriceDPH());
        BigDecimal totalPrice = new BigDecimal(tradeItemPreview.getTotalPrice());
        BigDecimal totalDPH = new BigDecimal(tradeItemPreview.getTotalDPH());
        
        reductionLabel.setText( df.format(reduction.divide(Store.CENT).abs()) );
        priceLabel.setText( df.format(totalPrice.divide(Store.CENT)) );
        dphLabel.setText( df.format(totalDPH.divide(Store.CENT)) );
        totalPriceLabel.setText( df.format( totalPriceDPH.divide(Store.CENT) ) );
        
        try {
            Customer customer = customers.getCustomerByID(tradeItemPreview.getId());

            // Jestliže odbìratel neexistuje (v databázi null),
            // jedná se o maloobchod
            if (customer.getId() == -1) {
                supNameLabel.setText("Malooobchod");
                supPersonLabel.setText("");
                adressLabel.setText("");
                telLabel.setText("");
            } else {
                supNameLabel.setText("" + customer.getName() + "");
                supPersonLabel.setText("" + customer.getPerson() + "");
                adressLabel.setText("" + customer.getSendStreet() + ", " + customer.getSendPsc() + ", " + customer.getSendCity()  + "");
                telLabel.setText("" + customer.getTel() + "");
                
            }
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    /**
     * Provede refresh zobrazení s tím, že oznaèí vstupní doklad a vstupní zboží
     * @param tradeItemPrev vstupní doklad
     * @param goods vstupní zboží
     */
    public void selectGoods(TradeItemPreview tradeItemPrev, Goods goods) {
        
        // Nastav vlastnosti pro zobrazení dokladu
        list.setListData( new Object[] {tradeItemPrev} );
        list.setSelectedIndex(0);
        
        // vylistuj zboží
        refreshGoods(tradeItemPrev);
        
        // zvýrazni pøíslušné zboží
        for (int i = 0; i < goodsTableModel.getRowCount(); i++) {
            
            // V prvním sloupci je 
            if (goodsTableModel.getGoodsIdAt(i).equals(goods.getGoodsID())) {
                goodsTable.setRowSelectionInterval(i, i);
                break;
            }
        }
        
    }    
    
    /**
     *  Vytvoøí novou výdejku -> vydá nové zboží ze skladu
     */
    public void newItem() {
        new DoSaleDialog( (Frame) owner, user); // Otevøi dialog pro vytvoøení výdejky
    }
    
    /**
     *  Vymaže oznaèenou výdejku
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat oznaèené výdejky?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète výdejku, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání výdejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }
        
        DoSale doSale = null;
        /* Proveï fyzické vymazání */
        try {
            // Vyber oznaèené položky 
            Object[] items =  list.getSelectedValues();
            
            // projdi pole a vymaž 
            for (int i = 0; i < items.length; i++) {
                TradeItemPreview tip = (TradeItemPreview) items[i];
                doSale = user.openDoSale(tip);
                doSale.storno(); // pøiprav smazání
            }
            doSale.update();

            refresh(); // Obnov výbìr
            // Nastav pro zobrazení prázdou výdejku
            refreshGoods(new TradeItemPreview()); // Obnov pro prázdný objekt
            MainWindow.getInstance().getStorePanel().refresh();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            try {
                // V pøípadì neúspìchu zruš výdejku
                if (doSale != null)
                    doSale.cancel();
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
     *  Edituje vybranou výdejku
     */
    private void editItem() {
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète výdejku, kterou chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        TradeItemPreview tip = (TradeItemPreview) list.getSelectedValue();
        
        if (tip == null)
            return;
        
        // Podle odbìratele rozhodni, zda editovat velkoobchodní výdejku, nebo 
        // maloobchod
        if (tip.getId() == 0) {
            new DiscountDialog( (Frame) owner, user, tip); // Maloobchod
        } else {
            new DoSaleDialog( (Frame) owner, user, tip); // Velkoobchod
        }
    }
    
    /**
     *  Vyhledá zboží ve skladu, pøepne do záložky sklad a oznaèí nalezené zboží
     */
    public void findGoods() {

        int row = goodsTable.getSelectedRow(); // Naèti první oznaèený øádek
        
        // jestliže není níc oznaèeno
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte zboží, které chcete vyhledat ze skladu.");
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        String goodsId = (String) goodsTable.getValueAt(row, BuyColumns.ID.getColumnNumber());

        // Oznaè zboží ve skladì. Jestliže oznaèil úspìšne pøejdi do skladu
        if (MainWindow.getInstance().getStorePanel().highlightRow(goodsId)) {
            MainWindow.getInstance().getTabbedPane().setSelectedIndex(TabbedPaneItems.STORE.getIndex()); // Zobraz panel se skladem
        } else {
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Skladová karta již zøejmì byla smazána");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    /**
     * Vyhledá odbìratele, pøepne do záložky dodavatele a oznaèí nalezeného dodavatele
     */
    public void findCustomer() {
        
        Object select = list.getSelectedValue();
        if (select == null) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte výdejku, ke které chcete vyhledat odbìratele.");
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        try {
            Customer customer = customers.getCustomerByID( ((TradeItemPreview) select).getId());
            
            // Jestliže odbìratel neexistuje (v databázi null),
            // jedná se o maloobchod
            if (customer.getId() == -1) {
                JOptionPane.showMessageDialog(this, 
                        "<html><center>Tato pøíjemka byla vytvoøena v maloobchodím prodeji,<br>" +
                        "kde se nevedou záznamy o odbìratelích</center></html>", 
                        "Maloobchod" , JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            if (MainWindow.getInstance().getCustomerPanel().highlightRow(customer)) { // zvýrazni dodavatele 
                MainWindow.getInstance().getTabbedPane().setSelectedIndex(TabbedPaneItems.CUSTOMERS.getIndex()); // Zobraz panel se skladem
            } else {
                ErrorMessages er = new ErrorMessages(Errors.NO_CUSTOMER_FOUND, "Odbìratel již zøejmì byl smazán.");
                JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            }
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    /**
     * Vytiskne maloobchodní doklad
     */
    private void printDiscount() {
        
        if (!MainWindow.getInstance()
                .getLicence().checkLicenseWithDialog(
                SalePanel.this)) {
            return;
        }
        
        String text = "Pøejete si vytisknout Prodejku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk prodejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }    
        
        // Tiskni
        try {
            Print.printDiscount(lastSelectedTrade);
        } catch (JRException ex) {
            ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Vytiskne velkoobchodní výdejku
     */
    private void printSale() {
        String text = "Pøejete si vytisknout Výdejku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk výdejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }    
        
        // Tiskni
        try {
            Print.printSale(lastSelectedTrade);
        } catch (JRException ex) {
            ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
   
    }
    
    /**
     *  Vytiskne pøíslušný výdejový doklad pro vybranou výdejku
     */
    private void printItem() {
        if (lastSelectedTrade == null || list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte výdejku, kterou chcete vytisknout.");
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }

        // Maloobchodní tisk
        if (lastSelectedTrade.getId() == 0) {
            printDiscount();
        } else {
            printSale();
        }
        
    }    
    
    /**
     *  Vyhledá pøíslušnou položku v pøíjemce 
     */
//    public void findItem() {
        
//    }
    
    /**
     *  Zmìna v list selection
     */
    private class buyListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // zábrání zdvojenému vyvolání uudálosti (význam mi není pøesnì znám)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastSelectedTrade = (TradeItemPreview) list.getSelectedValue();
           refreshGoods(lastSelectedTrade);
        }
    }     
    
    /**
     *  Stisk tlaèítka pro vytvoøení pøíjemky 
     */
    private class DoBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro vytvoøení pøíjemky 
     */
    private class DelBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro editaci pøíjemky 
     */
    private class EditBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro vyhledání zboží ve skadu
     */
    private class FindGoodsListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            findGoods();
        }
    }

    /**
     *  Stisk tlaèítka pro vyhledání dodavatele
     */
    private class FindCustomerListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            findCustomer();
        }
    }
    
    /**
     *  Stisk tlaèítka pro tisk výdejky
     */
    private class PrintBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            printItem();
        }
    }
    
    private class SaleKeyListener implements KeyListener {
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
                case KeyEvent.VK_LEFT :
                    list.requestFocus();
                    break;
                case KeyEvent.VK_RIGHT :
                    goodsTable.requestFocus();
                    break;
            }
            
            if (altPress && e.getKeyCode() == KeyEvent.VK_INSERT) {
                editButton.doClick();
                altPress = false;
                return;
            }
            
            if (ctrlPress && e.getKeyCode() == KeyEvent.VK_P) {
                printButton.doClick();
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
                case KeyEvent.VK_CONTROL :
                    ctrlPress = false;
                    break;
            }        
        }
        
    } 
    
    private class ItemTableListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.TRADE_TIP);
        }

        public void focusLost(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.EMPTY);
        }
        
    }
    
  

}


