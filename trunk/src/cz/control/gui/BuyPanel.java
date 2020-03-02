/*
 * BuyPanel.java
 *
 * Vytvo�eno 12. listopad 2005, 22:14
 *

 */
package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.Suplier;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import static java.awt.GridBagConstraints.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� panel s p�ehledem p��jemek
 *
 * (C) 2005, ver. 1.0
 */
public class BuyPanel extends JPanel implements LimitsChangedListener {

    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    private User user = null; // p�ihl�en� u�ivatel 
    private Buy buy = null; // Ukazatel na p��jemky 
    private Supliers supliers = null; // ukazatel na dodavatele
    private Component owner = null;
    private TradeItemPreview lastSelectedTrade = null; // Naposledy vybran� p�ehled
    private JList list = new JList(); // JList s p�ehledem v�ech p��jemek
    private DefaultListModel listModel = new DefaultListModel(); /* Model pro nastaven� seznamu */

    private JTable goodsTable; // Tabulka s jednotliv�mi polo�kami p��jemky
    private BuyTableModel goodsTableModel; // model tabulky 
    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton printButton;    // plo�ky ok�nka sou�tu cen
    private JLabel priceLabel; // cena zbo��
    private JLabel dphLabel; // celkov� da�
    private JLabel reductionTypLabel; // v�b�r slevy/p�ir�ky
    private JLabel reductionLabel; // celkov� sleva
    private JLabel totalPriceLabel; // celkov� cena
    private JCheckBox cashCheckBox; // platba v hotovosti
    private JLabel fakturaLabel;    // polo�ky ok�nka dodavatel
    private JLabel supNameLabel;    // jm�no dodavatele
    private JLabel supPersonLabel;  // jm�no kontatn� osoby
    private JLabel adressLabel;     // adresa dodavatele
    private JLabel telLabel;        // telefon dodavatele
    private Date startDate = new Date();
    private Date endDate = new Date();
    private Integer limit;
    /**
     * Jm�no adres��e ve kter�m jsou ulo�eny ikony programu
     */
    public static final String ICON_URL = Settings.ICON_URL;
    private static DecimalFormat df = Settings.getPriceFormat();

    /** Vytvo�� nov� objekt BuyPanel */
    public BuyPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;

        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();

        try {
            buy = user.openBuy(); /* Otev�i p��jemky pro p�ihl�en�ho u�ivatele */
            supliers = user.openSupliers(); // otev�i p��jemky
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.BUY.getIndex(), false);
            return;

        }

        this.setLayout(new BorderLayout());

        this.add(createFiltrPanel(), BorderLayout.NORTH);
        this.add(createListingPanel(), BorderLayout.WEST); // Panel s p�ehledem p��jemek
        this.add(createItemsPanel(), BorderLayout.CENTER); // Panel s jednotliv�mi polo�kami p��jemky 
        this.add(createBottomPanel(), BorderLayout.SOUTH); // spodn� panel

        refresh(); // Zobraz p�ehled p��jemek 
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
     *  Panel s filtrem zobrazen�
     */
    private JPanel createFiltrPanel() {
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
     * Vytvo�� panel s p�ehledem v�ech p��jemek 
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ehled p��jemek"));

        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener(new buyListingLSTListener());
        list.addKeyListener(new BuyKeyListener());
        list.addFocusListener(new ItemTableListener());

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(190, 100));

        content.add(setComponent(scrollPane, 0, 0, 1, 1, 0.0, 1.0, VERTICAL, CENTER));

        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        buttons.setMinimumSize(new Dimension(150, 115));
        buttons.setPreferredSize(new Dimension(150, 115));

        iconURL = BuyPanel.class.getResource(ICON_URL + "Import16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("P�ijmout", imageIcon);
        newButton.setToolTipText("P�ijme zbo�� na sklad");
        newButton.addActionListener(new DoBuyListener());
        newButton.setPreferredSize(new Dimension(130, 24));
        buttons.add(newButton);

        iconURL = BuyPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zm�nu p��jemky");
        editButton.addActionListener(new EditBuyListener());
        editButton.setPreferredSize(new Dimension(130, 24));
        buttons.add(editButton);

        iconURL = BuyPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstran� p��jemku");
        deleteButton.addActionListener(new DelBuyListener());
        deleteButton.setPreferredSize(new Dimension(130, 24));
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));

        return content;
    }

    /**
     * Vytvo�� panel s jednotliv�mi polo�kami p��jemky 
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ky p��jemky"));

        goodsTableModel = new BuyTableModel();
        goodsTable = new CommonTable(goodsTableModel); // vytvo�en� tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        goodsTable.addKeyListener(new BuyKeyListener());
        goodsTable.addFocusListener(new ItemTableListener());

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
        content.add(setComponent(createAdditionalInfoPanel(), 0, 1, 3, 1, 1.0, 0.0, HORIZONTAL, WEST));
        content.add(setComponent(createSumPanel(), 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(createSuplierPanel(), 1, 2, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));

        
        /* Panel s tla��tky pro tisk a vyhled�n� zbo�� */
        JPanel buttons = new JPanel(); // panel stla��tky
        buttons.setMinimumSize(new Dimension(140, 115));
        buttons.setPreferredSize(new Dimension(140, 115));
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;

        iconURL = BuyPanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zbo��", imageIcon);
        button.setToolTipText("Vyhled� kartu zbo��");
        button.setPreferredSize(new Dimension(130, 24));
        button.addActionListener(new FindGoodsListener());
        buttons.add(button);

        iconURL = BuyPanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Dodavatel", imageIcon);
        button.setToolTipText("Vyhled� dodavatele");
        button.setPreferredSize(new Dimension(130, 24));
        button.addActionListener(new FindSuplierListener());
        buttons.add(button);

        iconURL = BuyPanel.class.getResource(ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        printButton = new JButton("Tisk", imageIcon);
        printButton.setToolTipText("Vytiskne p��jmov� doklad");
        printButton.setPreferredSize(new Dimension(130, 24));
        printButton.addActionListener(new PrintBuyListener());
        buttons.add(printButton);

        content.add(setComponent(buttons, 2, 2, 1, 1, 0.0, 0.0, NONE, EAST));

        return content;

    }

    /**
     * Vytvo�� panel s dodate�n�ma informace o p��jemce
     * @return
     */
    private JPanel createAdditionalInfoPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "" +
                "Platba"));
        content.setLayout(gbl);
        content.setMinimumSize(new Dimension(200, 50));
        content.setPreferredSize(new Dimension(300, 50));

        JLabel label = new JLabel("Placeno hotov� ");
        cashCheckBox = new JCheckBox();
        cashCheckBox.setEnabled(false);
        content.add(setComponent(cashCheckBox, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(label, 1, 0, 1, 1, 0.0, 0.0, NONE, WEST));

        label = new JLabel(" | ��slo faktury: ");
        fakturaLabel = new JLabel("");
        fakturaLabel.setForeground(Color.BLUE);
        content.add(setComponent(label, 2, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(fakturaLabel, 3, 0, 1, 1, 1.0, 0.0, NONE, WEST));

        return content;
    }

    /**
     * Panel mo�nost� nastaven� slevy a zobrazen�m sou�tu cen
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Celkov� cena"));
        content.setLayout(gbl);
        content.setMinimumSize(new Dimension(300, 115));
        content.setPreferredSize(new Dimension(300, 115));

        JLabel label;
        JLabel kcLabel = new JLabel(" K� ");
        Font font = new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        reductionTypLabel = new JLabel("Sleva");
        reductionTypLabel.setFont(font);
        kcLabel = new JLabel(" % ");
        reductionLabel = new JLabel("0,00");
        reductionLabel.setFont(font);
        content.add(setComponent(reductionTypLabel, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(reductionLabel, 1, 0, 1, 1, 1.0, 0.0, NONE, EAST));
        content.add(setComponent(kcLabel, 2, 0, 1, 1, 0.0, 0.0, NONE, WEST));

        label = new JLabel("Cena bez DPH: ");
        priceLabel = new JLabel("0,00");
        priceLabel.setFont(font);
        kcLabel = new JLabel(" K� ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST));
        content.add(setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST));

        label = new JLabel("DPH ");
        kcLabel = new JLabel(" K� ");
        dphLabel = new JLabel("0,00");
        dphLabel.setFont(font);
        content.add(setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST));
        content.add(setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST));

        label = new JLabel("Cena s DPH: ");
        kcLabel = new JLabel(" K� ");
        totalPriceLabel = new JLabel("0,00");
        totalPriceLabel.setFont(font);
        content.add(setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(totalPriceLabel, 1, 3, 1, 1, 1.0, 0.0, NONE, EAST));
        content.add(setComponent(kcLabel, 2, 3, 1, 1, 0.0, 0.0, NONE, WEST));


        return content;
    }

    /**
     *  Vytvo�� panel s informacemi o dodavateli zbo��
     */
    private JPanel createSuplierPanel() {
        JPanel content = new JPanel();
        Font font = new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Dodavatel"));
        content.setMinimumSize(new Dimension(250, 115));
        content.setPreferredSize(new Dimension(250, 115));
        content.setLayout(gbl);

        supNameLabel = new JLabel();
        supNameLabel.setFont(font);
        JLabel label = new JLabel(" Dodavatel: ");
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(supNameLabel, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));

        supPersonLabel = new JLabel();
        supPersonLabel.setFont(font);
        label = new JLabel(" Kontaktn� osoba: ");
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(supPersonLabel, 1, 1, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));

        adressLabel = new JLabel();
        adressLabel.setFont(font);
        label = new JLabel(" Adresa: ");
        content.add(setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(adressLabel, 1, 2, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));

        telLabel = new JLabel();
        telLabel.setFont(font);
        label = new JLabel(" Telefon: ");
        content.add(setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(telLabel, 1, 3, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));

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
     *  Zv�razn� konkr�tn� p��jemku
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
     *  Aktualizuje p�ehledy p��jemek
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� p�ehled p��jemek v datab�zi
     */
    public void refresh() {
        int row = list.getSelectedIndex();

        try {
            List<TradeItemPreview> items = buy.getAllBuy(startDate, endDate, limit); // Na�ti v�echny p�ehledy
            list.setListData(items.toArray());

            list.setSelectedIndex(row);

            if (lastSelectedTrade != null) {
                refreshGoods(lastSelectedTrade);
            }

        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        }

    }

    /**
     *  Aktualizuje hodnoty v tabulce p��jemky
     * @param tradeItemPreview p�ehled p��jemky, kter� se m� zobrazit
     */
    private void refreshGoods(TradeItemPreview tradeItemPreview) {
        ArrayList<TradeItem> items;
        int row = goodsTable.getSelectedRow();
        try {
            items = buy.getAllBuyItem(tradeItemPreview);
            goodsTableModel.setData(items);

            if (row < goodsTableModel.getRowCount() && row >= 0) {
                goodsTable.setRowSelectionInterval(row, row);
            }
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Nastav ceny p��jemky
        if (tradeItemPreview.getReduction() < 0) {
            reductionTypLabel.setText("P�ir�ka");
        } else {
            reductionTypLabel.setText("Sleva");
        }

        BigDecimal reduction = new BigDecimal(tradeItemPreview.getReduction());
        BigDecimal totalPriceDPH = new BigDecimal(tradeItemPreview.getTotalPriceDPH());
        BigDecimal totalPrice = new BigDecimal(tradeItemPreview.getTotalPrice());
        BigDecimal totalDPH = new BigDecimal(tradeItemPreview.getTotalDPH());

        reductionLabel.setText(df.format(reduction.divide(Store.CENT).abs()));
        priceLabel.setText(df.format(totalPrice.divide(Store.CENT)));
        dphLabel.setText(df.format(totalDPH.divide(Store.CENT)));
        totalPriceLabel.setText(df.format(totalPriceDPH.divide(Store.CENT)));
        cashCheckBox.setSelected(tradeItemPreview.isCash());
        fakturaLabel.setText(tradeItemPreview.getBillNumber());

        try {
            Suplier suplier = supliers.getSuplierByID(tradeItemPreview.getId());
            supNameLabel.setText("" + suplier.getName() + "");
            supPersonLabel.setText("" + suplier.getPerson() + "");
            adressLabel.setText("" + suplier.getSendStreet() + ", " + suplier.getSendPsc() + ", " + suplier.getSendCity() + "");
            telLabel.setText("" + suplier.getTel() + "");
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Provede refresh zobrazen� s t�m, �e ozna�� vstupn� doklad a vstupn� zbo��
     * @param tradeItemPrev vstupn� doklad
     * @param goods vstupn� zbo��
     */
    public void selectGoods(TradeItemPreview tradeItemPrev, Goods goods) {

        // Nastav vlastnosti pro zobrazen� dokladu
        list.setListData(new Object[]{tradeItemPrev});
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
     *  Vytvo�� novou p��jemku -> p�ijme nov� zbo�� na sklad
     */
    public void newItem() {
        new DoBuyDialog((Frame) owner, user); // Otev�i dialog pro vytvo�en� p��jemky 
    }

    /**
     *  Vyma�e ozna�enou p��jemku 
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� p��jemky?";
        Object[] options = {"Ano", "Ne"};


        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te p��jemku, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� p��jemek",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }

        DoBuy doBuy = null;
        /* Prove� fyzick� vymaz�n� */
        try {
            // Vyber ozna�en� polo�ky 
            Object[] items = list.getSelectedValues();

            // projdi pole a vyma� p��jemky
            for (int i = 0; i < items.length; i++) {
                TradeItemPreview tip = (TradeItemPreview) items[i];
                doBuy = user.openDoBuy(tip);
                doBuy.storno(); // p�iprav smaz�n�
            }
            doBuy.update();

            refresh(); // Obnov v�b�r
            // Nastav pro zobrazen� pr�zdou p��jemku
            refreshGoods(new TradeItemPreview()); // Obnov pro pr�zdn� objekt
            MainWindow.getInstance().getStorePanel().refresh();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            try {
                // V p��pad� ne�sp�chu zru� p��jemku
                if (doBuy != null) {
                    doBuy.cancel();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        }

    }

    /**
     *  Edituje vybranou p��jemku
     */
    private void editItem() {

        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te p��jemku, kterou chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TradeItemPreview tip = (TradeItemPreview) list.getSelectedValue();

        if (tip == null) {
            return;
        }
        new DoBuyDialog((Frame) owner, user, tip);
    }

    /**
     *  Vyhled� zbo�� ve skladu, p�epne do z�lo�ky sklad a ozna�� nalezen� zbo��
     */
    public void findGoods() {

        int row = goodsTable.getSelectedRow(); // Na�ti prvn� ozna�en� ��dek

        // jestli�e nen� n�c ozna�eno
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte zbo��, kter� chcete vyhledat ze skladu.");
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String goodsId = (String) goodsTable.getValueAt(row, BuyColumns.ID.getColumnNumber());

        // Ozna� zbo�� ve sklad�. Jestli�e ozna�il �sp�ne p�ejdi do skladu
        if (MainWindow.getInstance().getStorePanel().highlightRow(goodsId)) {
            MainWindow.getInstance().getTabbedPane().setSelectedIndex(TabbedPaneItems.STORE.getIndex()); // Zobraz panel se skladem
        } else {
            ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Skladov� karta ji� z�ejm� byla smaz�na");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Vyhled� dodavatele, p�epne do z�lo�ky dodavatele a ozna�� nalezen�ho dodavatele
     */
    public void findSuplier() {

        Object select = list.getSelectedValue();
        if (select == null) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte p��jemku, ke kter� chcete vyhledat dodavatele.");
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Suplier suplier = supliers.getSuplierByID(((TradeItemPreview) select).getId());
            if (MainWindow.getInstance().getSuplierPanel().highlightRow(suplier)) { // zv�razni dodavatele 
                MainWindow.getInstance().getTabbedPane().setSelectedIndex(TabbedPaneItems.SUPLIERS.getIndex()); // Zobraz panel se skladem
            } else {
                ErrorMessages er = new ErrorMessages(Errors.NO_SUPLIER_FOUND, "Dodavatel ji� z�ejm� byl smaz�n.");
                JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     *  Vytiskne p��slu�n� p��jmov� doklad pro vybranou p��jemku
     */
    private void printItem() {
        if (lastSelectedTrade == null || list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte p��jemku, kterou chcete vytisknout.");
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String text = "P�ejete si vytisknout P��jemku?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk p��jemky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }

        // Tiskni
        try {
            Print.printBuy(lastSelectedTrade);
        } catch (JRException ex) {
            ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(BuyPanel.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
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
            if (e.getValueIsAdjusting() || list.getSelectedValue() == null) {
                return;
            }
            lastSelectedTrade = (TradeItemPreview) list.getSelectedValue();
            refreshGoods(lastSelectedTrade);
        }
    }

    /**
     *  Stisk tla��tka pro vytvo�en� p��jemky 
     */
    private class DoBuyListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }

    /**
     *  Stisk tla��tka pro vytvo�en� p��jemky 
     */
    private class DelBuyListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }

    /**
     *  Stisk tla��tka pro editaci p��jemky 
     */
    private class EditBuyListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    }

    /**
     *  Stisk tla��tka pro vyhled�n� zbo�� ve skadu
     */
    private class FindGoodsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            findGoods();
        }
    }

    /**
     *  Stisk tla��tka pro vyhled�n� dodavatele
     */
    private class FindSuplierListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            findSuplier();
        }
    }

    /**
     *  Stisk tla��tka pro tisk p��jemky
     */
    private class PrintBuyListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            printItem();
        }
    }

    /**
     *  Poslucha� obnoven� v�b�ru
     */
    private class RefreshButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }

    private class BuyKeyListener implements KeyListener {

        private boolean altPress = false;
        private boolean ctrlPress = false;

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DELETE:
                    deleteButton.doClick();
                    break;
                case KeyEvent.VK_ALT:
                    altPress = true;
                    break;
                case KeyEvent.VK_CONTROL:
                    ctrlPress = true;
                    break;
                case KeyEvent.VK_LEFT:
                    list.requestFocus();
                    break;
                case KeyEvent.VK_RIGHT:
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
                case KeyEvent.VK_ALT:
                    altPress = false;
                    break;
                case KeyEvent.VK_CONTROL:
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

