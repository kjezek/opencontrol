/*
 * DoBuyDialog.java
 *
 * Vytvo�eno 6. listopad 2005, 21:26
 *
 
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.PriceList;
import cz.control.data.Goods;
import cz.control.data.Suplier;
import cz.control.data.TradeItem;
import cz.control.data.TradeItemPreview;
import cz.control.business.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

import java.sql.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*; 

/*
 * P�evzat� bal�k
 *
 *  � Kai Toedter 1999 - 2004
 *       Version 1.2.2
 *       09/24/04
 *  http://www.toedter.com/en/jcalendar/
 */
import com.toedter.calendar.*;

/* sta�eno z: http://www.jgoodies.com/ */
//import com.jgoodies.looks.plastic.*;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import static java.awt.GridBagConstraints.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialog pro vytvo�en� p��jemky
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class DoBuyDialog extends JDialog implements WindowListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private static int SUPLIER_ROW_COUNT = 7;
    
    private static int MATH_ROUND = 0;

    private DoBuy doBuy; // t��da pro pr�ci s p��jemkami
    
    private ArrayList<Goods> goodsItem = new ArrayList<Goods>(); //polo�ky p��jemky 
    
    // ob�kty horn� ��sti dialogu
    private JComboBox suplierComboBox;  // Pole pro vlo�en� dodavatele
    private JCalendar calendarDialog = new JCalendar();
    private JDateChooser dateChooser = new JDateChooser(calendarDialog);
    
    // hlavn� ��st dialogu
    private EditableGoodsTableModel goodsTableModel;
    private JTable goodsTable;   // tabulka zbo�� na p��jemce 
    
    private JButton findButton;
    private JButton newButton;
    private JButton deleteButton;
    
    private JLabel statusBarTip;
    
    private Calendar calendar = new GregorianCalendar(); // kalend��
    private PriceTableModel priceTableModel;
    private JTable priceTable;
    
    // plo�ky ok�nka sou�tu cen
    private JLabel priceLabel; // cena zbo��
    private JLabel dphLabel; // celkov� da�
    private JComboBox reductionComboBox; // v�b�r slevy/p�ir�ky
    private JTextField reductionTextField; // celkov� sleva
    private JLabel totalPriceLabel; // celkov� cena

    private JCheckBox usePriceList = new JCheckBox("Pou��t cen�k", false);
    private JButton openPriceList = new JButton("Cen�k");
    private JComboBox computePrice;
    
    private PriceList defaultPriceList = null; // cen�k pro v�po�et PC z NC
    private PriceListEditor priceListEditor;
    private JTextField fakturaNumber;
    
    private User user;
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private Component owner = null;
    
    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("cs", "CZ"));
    
    /**
     * Vytvo�� nov� objekt DoBuyDialog
     * @param owner Vlastn�k dialog
     */
    public DoBuyDialog(Frame owner, User user)  {
        super(owner, "Control - Vytvo�en� P��jemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(); // Otev�i p��jemky 
            //Dopl� v�echny dodavatele

            // Nastav v�choz� cen�k
            this.priceListEditor = user.openPriceListEditor();
            this.defaultPriceList = priceListEditor.getDefaultPriceList();
            
            List<Suplier> supliers = user.openSupliers().getAllSupliers();
            suplierComboBox = new JComboBox(supliers.toArray());
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        calendar.setTimeInMillis(System.currentTimeMillis());
        setDialog();
        
    }
    
    /**
     * Vytvo�� nov� objekt DoBuyDialog
     * @param owner Vlastn�k dialog
     */
    public DoBuyDialog(Dialog owner, User user)  {
        super(owner, "Control - Vytvo�en� P��jemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(); // Otev�i p��jemky 

            // Nastav v�choz� cen�k
            this.priceListEditor = user.openPriceListEditor();
            this.defaultPriceList = priceListEditor.getDefaultPriceList();
            
            //Dopl� v�echny dodavatele
            ArrayList<Suplier> supliers = user.openSupliers().getAllSupliers();
            suplierComboBox = new JComboBox(supliers.toArray());
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        calendar.setTimeInMillis(System.currentTimeMillis());
        setDialog();
        
    }    
    
    /**
     * Vytvo�� nov� objekt DoBuyDialog, kter� p�edvypln� hodnotami p��jemky.
     * @param owner Vlastn�k dialog
     * @param tradeItemPreview P�ehled p��jemky, kterou se m� p�edvyplnit dialog
     */
    public DoBuyDialog(Frame owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvo�en� P��jemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(tradeItemPreview); // Otev�i p��jemky 
            //Dopl� v�echny dodavatele
            ArrayList<Suplier> supliers = user.openSupliers().getAllSupliers();
            suplierComboBox = new JComboBox(supliers.toArray());
            
            // Nastav v�choz� cen�k
            this.priceListEditor = user.openPriceListEditor();
            this.defaultPriceList = priceListEditor.getDefaultPriceList();

        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        // Vypl� formul�� podle p�ehledu p��jemky
        setDialog();
        
    }
    
    /**
     * Vytvo�� nov� objekt DoBuyDialog, kter� p�edvypln� hodnotami p��jemky.
     * @param owner Vlastn�k dialog
     * @param tradeItemPreview P�ehled p��jemky, kterou se m� ��edvyplnit dialog
     */
    public DoBuyDialog(Dialog owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvo�en� P��jemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(tradeItemPreview); // Otev�i p��jemky 
            //Dopl� v�echny dodavatele
            ArrayList<Suplier> supliers = user.openSupliers().getAllSupliers();
            suplierComboBox = new JComboBox(supliers.toArray());

            // Nastav v�choz� cen�k
            this.priceListEditor = user.openPriceListEditor();
            this.defaultPriceList = priceListEditor.getDefaultPriceList();

        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        // Vypl� formul�� podle p�ehledu p��jemky
        setDialog();
        
    }    
    
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
    /**
     * provede pot�ebn� nastaven� 
     */
    
    /**
     *  Nastav� z�kladn� vlastnosti dialogu
     */
    private void setDialog() {
        
        this.addWindowListener(this);
        setContentPane(getContent());
            
        // Nastav jednotliv� polo�ky p��jemky
        setGoodsItems();
        
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        setResizable(true);
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); 
        pack();
        setVisible(true);
    }
    
    /**
      *  Vytvo�� obsah dialogu 
      */
    private Container getContent() {
        JPanel content = new JPanel();

        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        content.setLayout(gbl);
        content.add(setComponent(createTopPanel(), 0, 0, 3, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createItemTablePanel(), 0, 1, 3, 1, 1.0, 1.0, BOTH, NORTH));
        content.add(setComponent(createPricePanel(), 0, 2, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add(setComponent(createSumPanel(), 1, 2, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createEditButtonPanel(), 2, 2, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add(setComponent(createFakturaPanel(), 0, 3, 3, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createConfirmBar(), 0, 4, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createStatusBar(), 0, 5, 3, 1, 1.0, 0.0, HORIZONTAL, WEST));

        return content;
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
     * Vytvo�� Horn� panel s editac� dodavatele a datumu
     */
    private Container createTopPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Z�kladn� �daje"));
        
        JLabel label = new JLabel("Datum: ");
        content.add(label);

        calendarDialog.setDecorationBackgroundVisible(false);
        calendarDialog.setCalendar( doBuy.getDate() );
        content.add(dateChooser);

        label = new JLabel("Dodavatel: ");
        content.add(label);

        suplierComboBox.setPreferredSize( new Dimension(230, 20));
        suplierComboBox.setMaximumRowCount(SUPLIER_ROW_COUNT);
        Suplier sup = doBuy.getSuplier();
        if (sup != null)
            suplierComboBox.setSelectedItem(sup);
        suplierComboBox.addActionListener( new ChangeSuplierListener() );
        content.add(suplierComboBox);

        JButton button = new JButton("Dodavatel�");
        button.addActionListener( new SuplierButtonListener() );
        content.add(button);
        
        
        return content;
    }
    
    /**
     *  Mal� pan�lek pro ��slo faktury
     */
    public JPanel createFakturaPanel() {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        JPanel content = new JPanel(layout);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Dal�� �daje"));
        
        JLabel label = new JLabel("��slo faktury: ");
        content.add(label);
        
        fakturaNumber = new JTextField(doBuy.getBillNumber());
        fakturaNumber.setPreferredSize( new Dimension(100, 20));
        content.add(fakturaNumber);
        
        return content;
    }

    /**
     * Vytvo�� panel s tabulkou, kter� zobrazuje polo�ky p��jemky
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ky p��jemky"));
        
        goodsTableModel = new EditableGoodsTableModel(goodsItem);
        goodsTableModel.addTableModelListener( new GoodsTableListener() );
        goodsTable = new CommonTable(goodsTableModel); // vytvo�en� tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        goodsTable.addKeyListener( new DoBuyKeyListener() );
        goodsTable.addFocusListener( new ItemTableListener() );
        
        // Poslucha� v�b�ru ��dky 
        ListSelectionModel rowSM = goodsTable.getSelectionModel();
        rowSM.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rowSM.addListSelectionListener(new SelectRowGoodsTableListener());
        
        TableColumnModel columnModel = goodsTable.getColumnModel();
        /* Nastav ���ky sloupc� */
        columnModel.getColumn(Columns.ID.getColumnNumber()).setPreferredWidth(Columns.ID.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setPreferredWidth(Columns.NAME.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setPreferredWidth(Columns.QUANTITY.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setPreferredWidth(Columns.UNIT.getColumnWidth()); // ���ka slouce "skladov� ��slo 
        
        /* Nastav editory bun�k pro zm�nu  mno�stv� */
//        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(new PriceCellRenderer());
//        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new PriceCellRenderer());
        QuantityCellEditor quantityCellEditor = new QuantityCellEditor();
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellEditor( quantityCellEditor );
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        
        /* Nastav zobrazen� slouc� */
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new QuantityCellRenderer());
        JScrollPane scrollPane = new JScrollPane(goodsTable);
        
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }

    /**
     *  Panel s v�pisem cen
     */
    private JPanel createPricePanel() {
        JPanel content = new JPanel(gbl); 
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        
        priceTableModel = new EditablePriceTableModel(0, 0, 0, 0, 0, 0);
        //priceTableModel.setPriceList(defaultPriceList);
        priceTableModel.addTableModelListener( new PriceTableListener());
        
        priceTable = new CommonTable(priceTableModel);
        priceTable.setDefaultRenderer(Float.class, new PriceCellRenderer()); // Zobrazen� sloupc� s cenou 
        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        priceTable.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(350, 150)); //Minimaln� velikost panelu
        content.add(setComponent(scrollPane, 0, 0, 3, 1, 1.0, 1.0, BOTH, CENTER));
        
        /* Nastav editory bun�k pro zm�nu ceny*/
        TableColumnModel columnModel = priceTable.getColumnModel();
        columnModel.getColumn(1).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(2).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(1).setCellEditor(new PriceCellEditor(goodsTable));
        columnModel.getColumn(2).setCellEditor(new PriceCellEditor(goodsTable));
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());

        Object[] value = {"Posledn� N�kupn� cena", "Pr�m�rn� N�kupn� cena", "Dra��� N�kupn� cena", "Levn�j�� N�kupn� cena", "Star� N�kupn� cena"};
        computePrice = new JComboBox(value);
        computePrice.setToolTipText("Ur�uje, jak se m� vypo��tat Prodejn� cena z N�kupn�ch cen");
        computePrice.addItemListener( new ComputePriceCBListener() );
        computePrice.setEnabled(false);
        content.add(setComponent(computePrice, 0, 1, 1, 1, 0.0, 1.0, NONE, WEST));
        
        openPriceList.addActionListener(new OpenPriceListListener());
        openPriceList.setToolTipText("Umo�n� nastavit v�po�et Prodejn�ch cen pro tuto p��jemku");
        openPriceList.setEnabled(false);
        content.add(setComponent(openPriceList, 1, 1, 1, 1, 0.0, 1.0, NONE, EAST));
        
        usePriceList.addActionListener( new UsePriceListListener() );
        usePriceList.setToolTipText("Nastavuje zda se maj� po��tat Prodejn� ceny podle cen�ku");
        usePriceList.setEnabled(false);
        content.add(setComponent(usePriceList, 2, 1, 1, 1, 0.0, 1.0, NONE, WEST));
        
        return content;
    }
    
    /**
     * Panel mo�nost� nastaven� slevy a zobrazen�m sou�tu cen
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Celkov� cena"));
        content.setMinimumSize( new Dimension(200, 170));
        content.setLayout(gbl);
        
        JLabel label;
        JLabel kcLabel = new JLabel(" K� ");
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        Object[] items = {"Sleva", "P�ir�ka"};
        reductionComboBox = new JComboBox(items);
        reductionComboBox.setSelectedItem( doBuy.getReduction().intValue() >= 0 ? 0 : 1);
        reductionComboBox.addItemListener( new ReductionCBItemListener() );
        kcLabel = new JLabel(" % ");
        reductionTextField = new JTextField(df.format( doBuy.getReduction().abs() ));
        reductionTextField.setFont(font);
        reductionTextField.setHorizontalAlignment(JTextField.RIGHT);
        reductionTextField.addActionListener( new ReductionTextFieldActionListener());
        reductionTextField.addFocusListener( new ReductionTextFieldFocusListener() );
        content.add( setComponent(reductionComboBox, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(reductionTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, EAST) );
        content.add( setComponent(kcLabel, 2, 0, 1, 1, 0.0, 0.0, NONE, WEST) );

        label = new JLabel(" Cena bez DPH: ");
        priceLabel = new JLabel(df.format( doBuy.getTotalPrice() ) );
        priceLabel.setFont(font);
        kcLabel = new JLabel(" K� ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" DPH ");
        kcLabel = new JLabel(" K� ");
        dphLabel = new JLabel(df.format( doBuy.getTotalDPH() ) );
        dphLabel.setFont(font);
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" Cena s DPH: ");
        kcLabel = new JLabel(" K� ");
        totalPriceLabel = new JLabel( df.format( doBuy.getTotalPriceDPH()) );
        totalPriceLabel.setFont(font);
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(totalPriceLabel, 1, 3, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }
    
    /**
     * Tla��tka pro editace
     */
    private JPanel createEditButtonPanel () {
        JPanel content = new JPanel();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ka"));
        content.setMinimumSize(new Dimension(140, 170)); //Minimaln� velikost panelu

        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton(" Nov� ", imageIcon);
        newButton.setToolTipText("Dopln� novou polo�ku p��jemky");
        newButton.addActionListener(new NewGoodsButtonListener());
        content.add(newButton);
        
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        findButton = new JButton(" Sklad ", imageIcon);
        findButton.setToolTipText("Vyhled� zbo�� ze skladu");
        findButton.addActionListener(new FindGoodsButtonListener());
        content.add(findButton);
        
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vyma�e ozna�enou polo�ku p��jemky");
        deleteButton.addActionListener(new DeleteButtonListener());
        content.add(deleteButton);
        
        return content;
    }    
    
    /**
     *  Vytvo�� panel s tal��tky pro potvrzen� a zru�en�
     */
    private JPanel createConfirmBar() {
        JPanel content = new JPanel();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� p��jemky"));
        
        // Tla��tko zru�en� 
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zru�it", imageIcon);
        button.setToolTipText("Zru�� bez ulo�en� vytv��enou p��jemku");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        // tla��tko potvrzen� 
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Potvrdit", imageIcon);
        button.setToolTipText("Provede p��jem zbo��");
        button.setMnemonic(KeyEvent.VK_ENTER);
        content.add(button);
        button.addActionListener(new ConfirmButtonListener());
        
        return content;
    }
    
    /**
     *  Vytvo�� spodn� ��st okna s nej�ast�ji pou��van�mi tla��tky
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(gbl);
        
        panel.setPreferredSize( new Dimension(50, 15));
        
        statusBarTip = new JLabel(StatusBarTips.CANCEL_CONFIRM.getText());
        panel.add(setComponent(statusBarTip, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        return panel;
    }
    
    

    
    /**
     *  Projde v�echny polo�ky p��jemky. Vyhled� ve skladu odpov�daj�c� zbo��
     *  a vlo�� ho do nov� p��jemky
     */
    private void setGoodsItems() {
        Set<TradeItem> tradeItems = doBuy.getItems();
        
        int index = 0;
        for (TradeItem i: tradeItems) {
            int row = goodsTableModel.inserRow(i.getAsGoods());
            doBuy.setRowNumber(i, row);
        }
    }
    

    /**
     * Dopln� zbo�� do p��jemky - vytvo�� dal�� ��dek p��jemky
     * 
     * @param goods zbo��, kter� se m� doplnit do p��jemky
     * @return True, jestli�e bylo vlo�en� �sp�n�, jinak false
     */
    public boolean addGoods(Goods goods) {
        Goods insertGoods = goods;
        
        // Jestli�e na sklad� nen� ��dn� mno�stv�, nastav �e se nakoup� jeden kus
        if ( goods.getQuantity() <= 0){
            insertGoods = new Goods(goods.getGoodsID(), goods.getName(), goods.getType(),
                    goods.getDph(), goods.getUnit(), goods.getEan(), goods.getNc(), 
                    goods.getPcA(),goods.getPcB(), goods.getPcC(), goods.getPcD(), 
                    1);
        }

        TradeItem insItem = null;
        // Jestli�e se poda�ilo vlo�it zbo�� do p��jemky 
        if ( (insItem = doBuy.addTradeItem(goods, insertGoods.getQuantity())) != null) {
            int row = goodsTableModel.inserRow(insertGoods); // Dopl� zbo��
            doBuy.setRowNumber(insItem, row);
            goodsTable.setRowSelectionInterval(row, row);
            refreshPrices();
        } else {
            ErrorMessages er = new ErrorMessages(Errors.DUPLICIT_VALUE, "\"<b>" + goods + "\"</b> nen� mo�n� znovu vlo�it");
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return false;
        }
        
        return true;
    }
    
    /**
     * Nastav� dodavatele zbo�� 
     * @param suplier dodavatel
     * @return true, jestli�e prob�hlo nastaven� v po��dku
     */
    public boolean setSuplier(Suplier suplier) {
        
        if (suplier == null) 
            return false;
        
        doBuy.setSuplier(suplier);
        suplierComboBox.setSelectedItem(suplier);
        refreshPrices(); // Obnov ceny, kv�i p��padn� zm�n� DPH podle dodavatele
        return true;
    }
    
    /**
     * Znovuzobraz� panel s cenami
     */
    private void refreshPrices() {
        // znovu zobraz  ceny
        BigDecimal value = doBuy.getTotalPrice();
        priceLabel.setText( df.format(value) );
        
        value = doBuy.getTotalDPH();
        dphLabel.setText( df.format(value) );
        
        value = doBuy.getTotalPriceDPH();
        totalPriceLabel.setText( df.format(value) );
        
        // zobr. absolutn� hodnotu -> p�ir�ka m� z�porn� znam�nko
        value = doBuy.getReduction().abs(); 
        reductionTextField.setText( df.format(value) );
        
    } 
    
    /**
     *  Nastav� slevu 
     */
    private void setReduction() {
        String s = reductionTextField.getText().trim();
        
        try {
            Number number = df.parse(s, new ParsePosition(0)); 
            
            // Nedovol zad�n� nepovolen�ch hodnot
            if (number == null || number.doubleValue() < 0)
                throw new Exception("Hodnota \"" + reductionTextField.getText() + "\" je chybn�. <br> Zad�vejte pouze nez�porn� ��sla.");

            int reduc = (new BigDecimal(number.toString()).multiply(Store.CENT)).intValue();
            // zjisti, zda se jedn� o slevu, nebo o p�ir�ku
            if (reductionComboBox.getSelectedIndex() == 1) {
                reduc = -reduc; // oto� znam�nko
            }
            // Nastav novou slevu 
            doBuy.setReduction(reduc); 
            refreshPrices();
            
        } catch (Exception ex ) {
            refreshPrices(); // zp�sob� zobrazen� p�vodn� spr�vn� hodnoty 
            ErrorMessages er = new ErrorMessages(Errors.NOT_EXEPT_VALUE, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    /**
     *  Nastav� datum p��jemky
     */
    private boolean setDate() {
        java.util.Date date = dateChooser.getDate();
        calendar.setTime(date);
        doBuy.setDate(calendar);

        return true; 
    }
    
    /**
     *  Nastav� nov� mno�stv� na p��jemce
     */
    private void setQuantity() {
        int row = goodsTable.getSelectedRow(); // Vybran� ��dek 

        TableModel model = goodsTableModel;

        if ( row >= model.getRowCount() || row < 0)
            return;

        double quantity = Double.parseDouble( String.valueOf(model.getValueAt(row, Columns.QUANTITY.getColumnNumber()) ) ); // zjisti mno�stv�
        String goodsId = String.valueOf( model.getValueAt(row, Columns.ID.getColumnNumber()) );
        doBuy.setQuantity(doBuy.getTradeItem(row), quantity); // Nastav mno�stv�
        
        //Jestli�e je nastaven v�po�et pr�m�rn� ceny aktualizuj v�po�et v tabulce
        //Zp�sob� znovup�epo�ten� tabulky
        Object value = priceTableModel.getValueAt(0, PriceTableColumns.PRICE.getNumber());
        priceTableModel.setValueAt(value, 0, PriceTableColumns.PRICE.getNumber());
            

        refreshPrices();
    }
    
    /**
     *  Zru�� vytv��enou p��jemku 
     */
    private void cancel() {
        String text = "Opravdu chcete zru�it pr�v� vytv��enou p��jemku?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                DoBuyDialog.this,
                text,
                "Storno p��jemky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }

        try {
           doBuy.update(); // zm�ny potvrzujeme i p�i zru�en�, pouze se nezap�e nov� v�dejka
        } catch (SQLException ex) {
            ex.printStackTrace();
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        
        MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazen� skladu v hlavn�m panelu 
        
        doBuy = null;
        DoBuyDialog.this.dispose();
        
    }
    
    /**
     *  Otev�e sklad a vyhled� skladov� karty, kter� dopln� do p��jemky
     */
    private void openStoreDialog(String s) throws SQLException, InvalidPrivilegException {
        ArrayList<Goods> items = StoreDialog.openDialog(this, user, s, true);
        
        Store store = user.openStore();
        for (Goods i: items) {
            //addGoods(i);
            // Je t�eba zbo�� znovu vybrat z datab�ze, aby ho bylo mo�no uzamknout
            addGoods( store.getGoodsByID(i.getGoodsID(), true));

        }
    }
    
    /**
     *  Obnov� zobrazen� v tabulce cen
     */
    private void refreschPriceTable() {
       /* Zjisti index na�ten�ho ��dku */
       int selectedRow;
       if ( (selectedRow = goodsTable.getSelectedRow()) == -1) {
            return;
        }

       Goods goods = doBuy.getTradeItemAsGoods(selectedRow);

        /* Nastav pot�ebn� hodnoty */
        priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph());         
    }
    
    /**
     *  Zonraz� dialog, kde se zad� zp�sob platby
     * @return true jestli�e byl dialog potvrzen. Jinak false
     */
    private boolean selectPayDialog() {
            boolean result = true;
            
            int payment = (doBuy.isIsCash()) ? ConfirmBusinessDialog.CASH_PAYMENT : ConfirmBusinessDialog.NO_CASH_PAYMENT;
            ConfirmBusinessDialog cd = ConfirmBusinessDialog.openDiaog(this, payment);
            
            if (cd == null) {
                return false;
            }
            
            // Podle toho co u�ivatel vybral
            switch (ConfirmBusinessDialog.getPayment()) {
                case ConfirmBusinessDialog.CASH_PAYMENT:
                    doBuy.makeRound(DoBuy.ROUND_SCALE_TO_100);
                    doBuy.setIsCash(true);
                    refreshPrices();
                    
                    if (ConfirmBusinessDialog.isUseCalc()) {
                        result = LitleCalcDialog.openDiaog(this, doBuy.getTotalPriceDPH().multiply(Store.CENT).longValue());
                    }
                    
                    break;
                case ConfirmBusinessDialog.NO_CASH_PAYMENT:    
                    doBuy.makeRound(DoBuy.ROUND_SCALE_UNNECESSARY);
                    doBuy.setIsCash(false);
                    refreshPrices();
                    break;
                default :
                    result = false;
            }

            return result;
    }
    
    /**
     *  Poslucha� stisku t�a��tka dopln�n� zbo��
     */
    private class NewGoodsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            String[] option = {"Ok", "Zp�t"};
            
            JPanel dialogContent = new JPanel(new GridLayout(2, 1));
            JTextField textField = new JTextField(10);
            dialogContent.add(new JLabel("" +
                    "<html><center>" +
                    "Zadejte skladov� ��slo, nebo n�zev zbo��." +
                    "</center></html>"));
            dialogContent.add(textField);
            
            int i = JOptionPane.showOptionDialog(
                                 DoBuyDialog.this,
                                dialogContent,
                                "Nov� polo�ka",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                option,
                                null);            
            
            // Zjisti, jestli u�ivatel stisknul OK.
            if (i == 0) {
            
                String s = textField.getText().trim();
                // z�skej odkaz na sklad, kter� vyvolal panel StorePanel z hlavn�ho okna
                try {
                    Store store = user.openStore(); 
                    java.util.List<Goods> items = store.getGoodsByKeyword(s,
                            StorePanel.getCurrentLimit());

                    // Nalezl v�ce polo�ek zbo��. Zobraz� skladov� panel
                    if (items.size() > 1) { 
                        //Pokus se zbo�� vyhledat ve skladu
                        openStoreDialog(s);
                        return;
                    }
                    // Nalezen pr�v� jednu polo�ku ve skladu. Vlo� j� do p��jemky
                    if (items.size() == 1) {
                        // Vlo� polo�ku do p��jemky 
                        // Je t�eba znovu na��st z datab�ze kv�li uzamknut�
                        addGoods( store.getGoodsByID(items.get(0).getGoodsID(), true) ); 
                        return;
                    }
                    // Nenalezl ��dn� zbo�� ve skladu. Informuj o chybn�m zad�n�
                    if (items.size() == 0) {
                        ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Opravte zad�n�, nebo vyhledejte zbo�� ve skladu");
                        JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                        return;
                    }
                } catch (InvalidPrivilegException exception) {
                    ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                    JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                    return;
                } catch (SQLException ex) {
                    ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                    JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                }
            }
            
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Vyhled�n� zbo��  
     */
    private class FindGoodsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            try {
                openStoreDialog(StorePanel.getCurrentKeyword());
           } catch (InvalidPrivilegException exception) {
               ErrorMessages er = ErrorMessages.getErrorMessages(exception);
               JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
           } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            }
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka V�b�r dodavatele  
     */
    private class SuplierButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            Suplier suplier = null;

            suplier = SuplierDialog.openDialog(DoBuyDialog.this, user);
            
            // Jestli�e byl vytvo�en dodavatel
            if (suplier != null) {
                suplierComboBox.addItem(suplier);
                setSuplier(suplier);
            }
        }
    }
    
   /**
     *  Poslucha� stisku zm�ny V�b�r dodavatele  
     */
    private class ChangeSuplierListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setSuplier( (Suplier) suplierComboBox.getSelectedItem());
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Vymaz�n� polo�ky 
     */
    private class DeleteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            /* Zjisti onza�en� ��dky */
            ListSelectionModel listSM = goodsTable.getSelectionModel();
            int firstRow = listSM.getMinSelectionIndex();
            int lastRow = listSM.getMaxSelectionIndex();
            
            if (firstRow == -1) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te zbo��, kter� chcete vymazat.");
                JOptionPane.showMessageDialog(owner, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            // Pole index�, kter� se vyma�ou z tabulky 
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            
            /* Veber p��slu�n� ��dky z tabulky */
            /* Projdi ��dky a vyma� odpov�daj�c� polo�ky z datab�ze*/
            for (int i = firstRow; i <= lastRow; i++) {

                /* Zkontroluj jestli je tento ��dek ozna�en (mezi prvn�m a posledn�m ozna�en�m mohou b�t i neozna�en� */
                // 
                if (listSM.isSelectedIndex(i)) {
                    // Vyma� p��jemky 
                    String id = String.valueOf( goodsTableModel.getValueAt(i, Columns.ID.getColumnNumber()) );
                    doBuy.deleteTradeItem(doBuy.getTradeItem(i)); // Vyma� z p��jemky
                    indexes.add(0, i);  // Ulo�, kter� index se bude vymaz�vat, na za��tek seznamu 
                }
            }

            // ��dky tabuky vyma�eme a� nakonec, nebo� p�i postupn�m vymaz�v�n� by se 
            // ztratila informace, kter� ��dky jsou ozna�en�.
            // Vymaz�v� se od zadu tabulky, aby z�staly platn� zji�t�n� indexi
            for (Integer i: indexes) {
                    goodsTableModel.deleteRow(i); // Vyma� z tabulky
                    doBuy.clearRowNumber(i);
            }

            refreshPrices();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Potvrzen� p��jemky
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            TradeItemPreview tr = null;
            try {
                if (!setDate())
                    return; // nastav datum
                setSuplier( (Suplier) suplierComboBox.getSelectedItem()); // Nastav dodavatele
                
                String billNumber = fakturaNumber.getText();
                // trim to null
                if (billNumber != null && billNumber.trim().length() == 0) {
                    billNumber = null;
                }
                doBuy.setBillNumber(billNumber);
                
                doBuy.check(); //Zkontroluj, zda je mo�no potvrdit
                
                // U�ivatel zad� zp�sob platby
                if (!selectPayDialog()) {
                    return; // Jestli�e si u�ivatel rozmyslel potvrzen�
                }
                
                doBuy.storno(); // Jestli byla star� p��jemka, stornuj ji
                tr = doBuy.confirm(); // potvrd proveden� p��jemky
                doBuy.update(); // Potvr� v�e

                MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazen� skladu v hlavn�m panelu 
                MainWindow.getInstance().getBuyPanel().refresh(); // Obnov zobrazen� p��jemek v hlavn�m panelu
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (Exception ex) {
                ErrorMessages er = new ErrorMessages(Errors.NOT_POSIBLE_CONFIRM_BUY, ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }
            
            try {
                // Jestli�e se m� tisknout
                if (ConfirmBusinessDialog.getResult() != null && ConfirmBusinessDialog.isPrint()) {
                    Print.printBuy(tr);
                }        
            } catch (JRException ex) {
                ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }
            
            DoBuyDialog.this.dispose(); // uzav�i okno
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Zru�en� p��jemky
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            cancel();
        }
    }
    
    
    /**
     * Poslucha� tabulky zbo��. Zm�na v tabulce
     */
    private class GoodsTableListener implements TableModelListener {
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow(); // Vybran� ��dek 
            
            TableModel model = (TableModel) e.getSource();
            
            if (model.getRowCount() <= row)
                return;
            
            setQuantity();
        }
        
    }

    /**
     *  Poslucha� v�b�ru ��dky v tabulce zbo�� 
     */
    private class SelectRowGoodsTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting()) return;
           
           int rowInGoodsTable = goodsTable.getSelectedRow();
           
           if (rowInGoodsTable == -1) {
               priceTable.setVisible(false);
               openPriceList.setEnabled(false);
               computePrice.setEnabled(false);
               usePriceList.setEnabled(false);
               return;
           }
           priceTable.setVisible(true);
           openPriceList.setEnabled(true);
           computePrice.setEnabled(true);
           usePriceList.setEnabled(true);
           
           //Obnov nastaven� zp�sobu v�po�tu
           ItemAttributes iteAttr = doBuy.getTradeItemAttributes(doBuy.getTradeItem(rowInGoodsTable));
           computePrice.setSelectedIndex(iteAttr.getComputePrices());
           priceTableModel.setPriceList(iteAttr.getPriceList()); //Nastav cen�k
           
           // Podle toho zda se pou��v� cen�k nastav za�krt�v�tko
           if (iteAttr.getPriceList() == null) {
               usePriceList.setSelected(false);
           } else {
               usePriceList.setSelected(true);
           }
           
           refreschPriceTable();
        }
    }
    
    /**
     *  Poslucha� zm�ny slevy 
     */
    private class ReductionTextFieldActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
        
            setReduction();
        }
    }
    
    /**
     *  Poslucha� zm�ny fokusu u pol��ka s nastaven�m slevy 
     */
    private class ReductionTextFieldFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            // jestli�e z�sk� fokus, nic ned�lej
        }

        public void focusLost(FocusEvent e) {
            // jestli�e ztr�c� fokus, nastav slevu
            setReduction();
        }
        
    }
    
    /**
     * Poslucha� zm�ny v�b�ru slevy/p�ir�ky
     */
    private class ReductionCBItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setReduction();
        }
        
    }
    
    /**
     *  Zm�na checkboxu ur�uj�c�, zda se m� pou��t cen�k
     */
    private class UsePriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

           int rowInGoodsTable = goodsTable.getSelectedRow();
           
           ItemAttributes iteAttr = doBuy.getTradeItemAttributes(doBuy.getTradeItem(rowInGoodsTable));
           
           if (rowInGoodsTable == -1 || iteAttr == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te ��dek, u kter�ho chcete nastavit cen�k.");
                JOptionPane.showMessageDialog(owner, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                // Nastav opa�n� za�krtnut�
                usePriceList.setSelected( !usePriceList.isSelected());
                return;
           }
           
           // Jestli�e je za�krtnuto, nastav pou�it� cen�ku
           if (usePriceList.isSelected() == true) {
               // Jestli�e nebyl d��ve cen�k asociov�n, nastav v�choz�, jinak nastav ten asociovan�
               if (iteAttr.getPriceList() == null) {
                    priceTableModel.setPriceList(defaultPriceList);
                    iteAttr.setPriceList(defaultPriceList);
               } else {
                    priceTableModel.setPriceList(iteAttr.getPriceList());
               }
               priceTableModel.refreschPrices();
            } else {
                priceTableModel.setPriceList(null);
            }
        }

    }
    
    /**
     *  Stisk tla��tka pro otev�en� cen�ku
     */
    private class OpenPriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

           int rowInGoodsTable = goodsTable.getSelectedRow();
           ItemAttributes iteAttr = doBuy.getTradeItemAttributes(doBuy.getTradeItem(rowInGoodsTable));

           if (rowInGoodsTable == -1 || iteAttr == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te ��dek, u kter�ho chcete nastavit cen�k.");
                JOptionPane.showMessageDialog(owner, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
           }
           
           PriceList tmp;
           // Jestli�e nebyl d��ve asociov�n cen�k, pou�ij v�choz�, jinak pou�ij ten asociovan�
            if (iteAttr == null || iteAttr.getPriceList() == null) {
                tmp = defaultPriceList;
            } else {
                tmp = iteAttr.getPriceList();
            }
           
           //Prove� zm�nu cen�ku
            tmp = PriceListDialog.openPriceListDialog(DoBuyDialog.this, tmp);
            
            // Jestli�e potvrdil dialog, zapni pou��v�n� cen�ku
            if (tmp != null) {
                usePriceList.setSelected(true); 
                priceTableModel.setPriceList(tmp); //z�rove� nastav pou��v�n� cen�ku
                priceTableModel.refreschPrices();
                iteAttr.setPriceList(tmp);
            } 
        }
        
    }  
    
    /**
     *  Poslucha� zm�ny v tabulce cen
     */
    private class PriceTableListener implements TableModelListener {
        private int prewRow = -1;
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();

            int rowInGoodsTable = goodsTable.getSelectedRow();
            if (rowInGoodsTable == -1) {
                return;
            }
            
            TableModel model = (TableModel) e.getSource();
            // Zabr�n� detekci zm�ny p�i nastaven� hodnoty
            model.removeTableModelListener(this);
            
            // Na�ti ceny s tabulky
            int nc = new BigDecimal( 
                    String.valueOf( model.getValueAt(0, PriceTableColumns.PRICE.getNumber()) ))
                    .multiply(Store.CENT).setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue();
            
            TradeItem tradeItem = doBuy.getTradeItem(rowInGoodsTable);
            ItemAttributes itemAttr = doBuy.getTradeItemAttributes(tradeItem);
            // Jestli�e do�lo ke zm�n� v NC, Nech vypo��tat NC
            if (row == 0) {
                doBuy.computeNC(tradeItem, nc);
                
                double computedNC = new BigDecimal(itemAttr.getComputedNc())
                    .divide(Store.CENT).setScale(2).doubleValue();
                
                //Nastav�me vypo�tenou NC do tabulky -> P�epo�tou se podle n� PC
                model.setValueAt(computedNC, 0, PriceTableColumns.PRICE.getNumber());
                
                //Do�asn� vypneme cen�k a nastav�me p�vodn� NC (tu u�ivatelem zadanou)
                priceTableModel.setOnlyNC(nc);
            }
            
            int pcA = new BigDecimal( 
                    String.valueOf( model.getValueAt(1, PriceTableColumns.PRICE.getNumber())) )
                    .multiply(Store.CENT).setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue();
            int pcB = new BigDecimal( 
                    String.valueOf( model.getValueAt(2, PriceTableColumns.PRICE.getNumber())) )
                    .multiply(Store.CENT).setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue();
            int pcC = new BigDecimal( 
                    String.valueOf( model.getValueAt(3, PriceTableColumns.PRICE.getNumber())) )
                    .multiply(Store.CENT).setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue();
            int pcD = new BigDecimal( 
                    String.valueOf( model.getValueAt(4, PriceTableColumns.PRICE.getNumber())) )
                    .multiply(Store.CENT).setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue();

            //Nastav tool tip s n�kupn�mi cenami
            BigDecimal lastNC = new BigDecimal(itemAttr.getInputGoods().getNc()).divide(Store.CENT); 
            BigDecimal newNC = new BigDecimal(itemAttr.getNewNc()).divide(Store.CENT); 
            BigDecimal computedNC = new BigDecimal(itemAttr.getComputedNc()).divide(Store.CENT); 
            priceTable.setToolTipText("<html>" +
                    "Star� N�kupn� cena: <b>" + df.format(lastNC) + "</b><br>" +
                    "Nov� n�kupn� cena: <b>" + df.format(newNC) + "</b><br>" +
                    "Pro v�po�et pou�ita N�kupn� cena: <b>" + df.format(computedNC) + "</b>" +
                    "</html>");
            
            //Nastav nov� ceny
            doBuy.setNewPcPrices(doBuy.getTradeItem(rowInGoodsTable), pcA, pcB, pcC, pcD);
            refreshPrices();
            
            // Obnov listener
            model.addTableModelListener(this);
        }  
    }
    
    /**
     * Poslucha� zm�ny v�b�ru slevy/p�ir�ky
     */
    private class ComputePriceCBListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            
            int rowInGoodsTable = goodsTable.getSelectedRow();
            if (rowInGoodsTable == -1 || computePrice.getSelectedIndex() == -1) {
                return;
            }   
            
            TradeItem tradeItem = doBuy.getTradeItem(rowInGoodsTable);
            doBuy.setComputeNCPrice(tradeItem, computePrice.getSelectedIndex());
         
            // Obnov kv�li p�echodu z ��dku na ��dek, aby bylo zobrazen� aktu�ln�
            refreschPriceTable();
            
            //Zp�sob� znovup�epo�ten� tabulky
            Object value = priceTableModel.getValueAt(0, PriceTableColumns.PRICE.getNumber());
            priceTableModel.setValueAt(value, 0, PriceTableColumns.PRICE.getNumber());
            
            refreshPrices(); // obnov sou�ty
        }
        
    }    

    private class DoBuyKeyListener implements KeyListener {
        private boolean altPress = false;
                
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
            }
            
            if (altPress && e.getKeyCode() == KeyEvent.VK_INSERT) {
                findButton.doClick();
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
            }        
        }
        
    }     
    
    private class ItemTableListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            statusBarTip.setText(StatusBarTips.DO_TRADE_TIP.getText());
        }

        public void focusLost(FocusEvent e) {
            statusBarTip.setText(StatusBarTips.CANCEL_CONFIRM.getText());
        }
        
    }    
        
}
