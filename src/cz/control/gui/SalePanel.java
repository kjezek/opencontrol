/*
 * SalePanel.java
 *
 * Vytvo�eno 16. �nor 2006, 12:05
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� panel s p�ehledem v�dejek
 *
 * (C) 2005, ver. 1.0
 */
public class SalePanel extends JPanel implements LimitsChangedListener {
    
    public static enum PANEL_TYPES {SALE, DISCOUNT};
    
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // p�ihl�en� u�ivatel 
    private Sale sale = null; // Ukazatel na v�dejky
    private Customers customers = null; // ukazatel na odb�ratele
    private Component owner = null;
    
    private TradeItemPreview lastSelectedTrade = null; // Naposledy vybran� p�ehled
    
    private JList list = new JList(); // JList s p�ehledem v�ech p��jemek
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastaven� seznamu */ 
    private JTable goodsTable; // Tabulka s jednotliv�mi polo�kami p��jemky
    private BuyTableModel goodsTableModel; // model tabulky 
    
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton printButton;

    
    // polo�ky ok�nka sou�tu cen
    private JLabel priceLabel; // cena zbo��
    private JLabel dphLabel; // celkov� da�
    private JLabel reductionTypLabel; // v�b�r slevy/p�ir�ky
    private JLabel reductionLabel; // celkov� sleva
    private JLabel totalPriceLabel; // celkov� cena
    
    // polo�ky ok�nka dodavatel
    private JLabel supNameLabel;    // jm�no dodavatele
    private JLabel supPersonLabel;  // jm�no kontatn� osoby
    private JLabel adressLabel;     // adresa dodavatele
    private JLabel telLabel;        // telefon dodavatele
    
    private Date startDate = new Date();
    private Date endDate = new Date();
    private Integer limit;

    
    
    private DateAndQuantityIntervalPanel limitsPanel;
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private PANEL_TYPES panelType;
    
    /**
     * Vytvo�� panel pro zobrazen� v�dejek
     * @param owner
     * @param user
     * @return
     */
    public static SalePanel createPanelForSale(Frame owner, User user) {
        return new SalePanel(owner, user, PANEL_TYPES.SALE);
    }
    
    /**
     * Vytvo�� panel pro zobrazen� prodejek - vytvo�en�ch maloobcodn�m prodejem
     * 
     * @param owner
     * @param user
     * @return
     */
    public static SalePanel createPanelForDiscount(Frame owner, User user) {
        return new SalePanel(owner, user, PANEL_TYPES.DISCOUNT);
    }
    
    /** Vytvo�� nov� objekt SalePanel */
    private SalePanel(Frame owner, User user, PANEL_TYPES panelType) {
        this.panelType = panelType;
        this.user = user;
        this.owner = owner;
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        try {
            sale = user.openSale(); /* Otev�i v�dejky pro p�ihl�en�ho u�ivatele */
            customers = user.openCustomers(); // otev�i dodavatele
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.SALE.getIndex(), false);
            return;
        }
        
        this.setLayout( new BorderLayout() );
        
        this.add(createFilterPanel() , BorderLayout.NORTH); 
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s p�ehledem v�dejek
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotliv�mi polo�kami v�dejky
        this.add(createBottomPanel(), BorderLayout.SOUTH); // spodn� panel
        
        refresh(); // Zobraz p�ehled
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
    
    private Component createFilterPanel() {
        
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH)); //Za��tek m�s�ce
        startDate.setTime(cal.getTimeInMillis());

        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec m�s�ce
        endDate.setTime(cal.getTimeInMillis());
        limit = 1000;
        
        DateAndQuantityIntervalPanel content = new DateAndQuantityIntervalPanel(startDate, endDate, limit);
        content.addLimitsChangedListener(this);
        
        return content;
    }
        
    
    /**
     * Vytvo�� panel s p�ehledem v�ech v�dejek
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ehled"));
        
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
        newButton.setToolTipText("Vyd� zbo�� ze sklad");
        newButton.addActionListener( new DoBuyListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zm�nu v�dejky");
        editButton.addActionListener( new EditBuyListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstran� v�dejku");
        deleteButton.addActionListener( new DelBuyListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     * Vytvo�� panel s jednotliv�mi polo�kami v�dejky 
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ky zbo��"));

        goodsTableModel = new BuyTableModel();
        goodsTable = new CommonTable(goodsTableModel); // vytvo�en� tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        goodsTable.addKeyListener( new SaleKeyListener() );
        goodsTable.addFocusListener( new ItemTableListener() );
        
        TableColumnModel columnModel = goodsTable.getColumnModel();
        /* Nastav ���ky sloupc� */
        columnModel.getColumn(BuyColumns.ID.getColumnNumber()).setPreferredWidth(BuyColumns.ID.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(BuyColumns.NAME.getColumnNumber()).setPreferredWidth(BuyColumns.NAME.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(BuyColumns.QUANTITY.getColumnNumber()).setPreferredWidth(BuyColumns.QUANTITY.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(BuyColumns.UNIT.getColumnNumber()).setPreferredWidth(BuyColumns.UNIT.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        /* Nastav zobrazen� slouc� */
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
        
        /* Panel s tla��tky pro tisk a vyhled�n� zbo�� */
        JPanel buttons = new JPanel(); // panel stla��tky
        buttons.setMinimumSize( new Dimension(140, 115));
        buttons.setPreferredSize( new Dimension(140, 115));
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zbo��", imageIcon);
        button.setToolTipText("Vyhled� kartu zbo��");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new FindGoodsListener() );
        buttons.add(button);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Odb�ratel", imageIcon);
        button.setToolTipText("Vyhled� od�ratele");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new FindCustomerListener() );
        buttons.add(button);
        
        iconURL = SalePanel.class.getResource(ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        printButton = new JButton("Tisk", imageIcon);
        printButton.setToolTipText("Vytiskne v�dejov� doklad");
        printButton.setPreferredSize( new Dimension(130, 24) );
        printButton.addActionListener( new PrintBuyListener() );
        buttons.add(printButton);
        
        content.add(setComponent(buttons, 2, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        
        return content;
        
    }
    
    /**
     * Panel mo�nost� nastaven� slevy a zobrazen�m sou�tu cen
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Celkov� cena"));
        content.setLayout(gbl);
        content.setMinimumSize( new Dimension(300, 115));
        content.setPreferredSize( new Dimension(300, 115));

        JLabel label;
        JLabel kcLabel = new JLabel(" K� ");
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
        kcLabel = new JLabel(" K� ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" DPH ");
        kcLabel = new JLabel(" K� ");
        dphLabel = new JLabel("0,00");
        dphLabel.setFont(font);
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" Cena s DPH: ");
        kcLabel = new JLabel(" K� ");
        totalPriceLabel = new JLabel("0,00");
        totalPriceLabel.setFont(font);
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(totalPriceLabel, 1, 3, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvo�� panel s informacemi o dodavateli zbo��
     */
    private JPanel createSuplierPanel() {
        JPanel content = new JPanel();
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Odb�ratel"));
        content.setMinimumSize( new Dimension(250, 115));
        content.setPreferredSize( new Dimension(250, 115));
        content.setLayout(gbl);
        
        supNameLabel = new JLabel();
        supNameLabel.setFont(font);
        JLabel label = new JLabel(" Odb�ratel: ");
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(supNameLabel, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST) );
        
        supPersonLabel = new JLabel();
        supPersonLabel.setFont(font);
        label = new JLabel(" Kontaktn� osoba: ");
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
     *  Vytvo�� spodn� panel s tla��tkem pro novou p��jemku
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        return content;
    }
    
    /**
     *  Zv�razn� konkr�tn� v�dejku
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
     *  Aktualizuje p�ehledy v�dejek
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� p�ehled p��jemek v datab�zi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            List<TradeItemPreview> items = null;
            
            switch (panelType) {
                case DISCOUNT :
                    items = sale.getAllDiscountOnly(startDate, endDate, limit ); // Na�ti v�echny p�ehledy
                    break;
                case SALE :
                    items = sale.getAllSaleOnly(startDate, endDate, limit); // Na�ti v�echny p�ehledy
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
     *  Aktualizuje hodnoty v tabulce v�dejek
     * @param tradeItemPreview p�ehled v�dejky, kter� se m� zobrazit
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
        
        // Nastav ceny v�dejky
        if (tradeItemPreview.getReduction() < 0) {
            reductionTypLabel.setText("P�ir�ka");
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

            // Jestli�e odb�ratel neexistuje (v datab�zi null),
            // jedn� se o maloobchod
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
     * Provede refresh zobrazen� s t�m, �e ozna�� vstupn� doklad a vstupn� zbo��
     * @param tradeItemPrev vstupn� doklad
     * @param goods vstupn� zbo��
     */
    public void selectGoods(TradeItemPreview tradeItemPrev, Goods goods) {
        
        // Nastav vlastnosti pro zobrazen� dokladu
        list.setListData( new Object[] {tradeItemPrev} );
        list.setSelectedIndex(0);
        
        // vylistuj zbo��
        refreshGoods(tradeItemPrev);
        
        // zv�razni p��slu�n� zbo��
        for (int i = 0; i < goodsTableModel.getRowCount(); i++) {
            
            // V prvn�m sloupci je 
            if (goodsTableModel.getGoodsIdAt(i).equals(goods.getGoodsID())) {
                goodsTable.setRowSelectionInterval(i, i);
                break;
            }
        }
        
    }    
    
    /**
     *  Vytvo�� novou v�dejku -> vyd� nov� zbo�� ze skladu
     */
    public void newItem() {
        new DoSaleDialog( (Frame) owner, user); // Otev�i dialog pro vytvo�en� v�dejky
    }
    
    /**
     *  Vyma�e ozna�enou v�dejku
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� v�dejky?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te v�dejku, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� v�dejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }
        
        DoSale doSale = null;
        /* Prove� fyzick� vymaz�n� */
        try {
            // Vyber ozna�en� polo�ky 
            Object[] items =  list.getSelectedValues();
            
            // projdi pole a vyma� 
            for (int i = 0; i < items.length; i++) {
                TradeItemPreview tip = (TradeItemPreview) items[i];
                doSale = user.openDoSale(tip);
                doSale.storno(); // p�iprav smaz�n�
            }
            doSale.update();

            refresh(); // Obnov v�b�r
            // Nastav pro zobrazen� pr�zdou v�dejku
            refreshGoods(new TradeItemPreview()); // Obnov pro pr�zdn� objekt
            MainWindow.getInstance().getStorePanel().refresh();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            try {
                // V p��pad� ne�sp�chu zru� v�dejku
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
     *  Edituje vybranou v�dejku
     */
    private void editItem() {
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te v�dejku, kterou chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        TradeItemPreview tip = (TradeItemPreview) list.getSelectedValue();
        
        if (tip == null)
            return;
        
        // Podle odb�ratele rozhodni, zda editovat velkoobchodn� v�dejku, nebo 
        // maloobchod
        if (tip.getId() == 0) {
            new DiscountDialog( (Frame) owner, user, tip); // Maloobchod
        } else {
            new DoSaleDialog( (Frame) owner, user, tip); // Velkoobchod
        }
    }
    
    /**
     *  Vyhled� zbo�� ve skladu, p�epne do z�lo�ky sklad a ozna�� nalezen� zbo��
     */
    public void findGoods() {

        int row = goodsTable.getSelectedRow(); // Na�ti prvn� ozna�en� ��dek
        
        // jestli�e nen� n�c ozna�eno
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte zbo��, kter� chcete vyhledat ze skladu.");
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        String goodsId = (String) goodsTable.getValueAt(row, BuyColumns.ID.getColumnNumber());

        // Ozna� zbo�� ve sklad�. Jestli�e ozna�il �sp�ne p�ejdi do skladu
        if (MainWindow.getInstance().getStorePanel().highlightRow(goodsId)) {
            MainWindow.getInstance().getTabbedPane().setSelectedIndex(TabbedPaneItems.STORE.getIndex()); // Zobraz panel se skladem
        } else {
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Skladov� karta ji� z�ejm� byla smaz�na");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    /**
     * Vyhled� odb�ratele, p�epne do z�lo�ky dodavatele a ozna�� nalezen�ho dodavatele
     */
    public void findCustomer() {
        
        Object select = list.getSelectedValue();
        if (select == null) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte v�dejku, ke kter� chcete vyhledat odb�ratele.");
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        try {
            Customer customer = customers.getCustomerByID( ((TradeItemPreview) select).getId());
            
            // Jestli�e odb�ratel neexistuje (v datab�zi null),
            // jedn� se o maloobchod
            if (customer.getId() == -1) {
                JOptionPane.showMessageDialog(this, 
                        "<html><center>Tato p��jemka byla vytvo�ena v maloobchod�m prodeji,<br>" +
                        "kde se nevedou z�znamy o odb�ratel�ch</center></html>", 
                        "Maloobchod" , JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            if (MainWindow.getInstance().getCustomerPanel().highlightRow(customer)) { // zv�razni dodavatele 
                MainWindow.getInstance().getTabbedPane().setSelectedIndex(TabbedPaneItems.CUSTOMERS.getIndex()); // Zobraz panel se skladem
            } else {
                ErrorMessages er = new ErrorMessages(Errors.NO_CUSTOMER_FOUND, "Odb�ratel ji� z�ejm� byl smaz�n.");
                JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            }
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    /**
     * Vytiskne maloobchodn� doklad
     */
    private void printDiscount() {
        
        if (!MainWindow.getInstance()
                .getLicence().checkLicenseWithDialog(
                SalePanel.this)) {
            return;
        }
        
        String text = "P�ejete si vytisknout Prodejku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk prodejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
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
     *  Vytiskne velkoobchodn� v�dejku
     */
    private void printSale() {
        String text = "P�ejete si vytisknout V�dejku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk v�dejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
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
     *  Vytiskne p��slu�n� v�dejov� doklad pro vybranou v�dejku
     */
    private void printItem() {
        if (lastSelectedTrade == null || list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte v�dejku, kterou chcete vytisknout.");
            JOptionPane.showMessageDialog(SalePanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }

        // Maloobchodn� tisk
        if (lastSelectedTrade.getId() == 0) {
            printDiscount();
        } else {
            printSale();
        }
        
    }    
    
    /**
     *  Vyhled� p��slu�nou polo�ku v p��jemce 
     */
//    public void findItem() {
        
//    }
    
    /**
     *  Zm�na v list selection
     */
    private class buyListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastSelectedTrade = (TradeItemPreview) list.getSelectedValue();
           refreshGoods(lastSelectedTrade);
        }
    }     
    
    /**
     *  Stisk tla��tka pro vytvo�en� p��jemky 
     */
    private class DoBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro vytvo�en� p��jemky 
     */
    private class DelBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro editaci p��jemky 
     */
    private class EditBuyListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro vyhled�n� zbo�� ve skadu
     */
    private class FindGoodsListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            findGoods();
        }
    }

    /**
     *  Stisk tla��tka pro vyhled�n� dodavatele
     */
    private class FindCustomerListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            findCustomer();
        }
    }
    
    /**
     *  Stisk tla��tka pro tisk v�dejky
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


