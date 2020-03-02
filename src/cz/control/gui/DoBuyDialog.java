/*
 * DoBuyDialog.java
 *
 * Vytvoøeno 6. listopad 2005, 21:26
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
 * Pøevzatý balík
 *
 *  © Kai Toedter 1999 - 2004
 *       Version 1.2.2
 *       09/24/04
 *  http://www.toedter.com/en/jcalendar/
 */
import com.toedter.calendar.*;

/* staženo z: http://www.jgoodies.com/ */
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
 * Program Control - Skladový systém
 *
 * Tøída vytváøí dialog pro vytvoøení pøíjemky
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class DoBuyDialog extends JDialog implements WindowListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private static int SUPLIER_ROW_COUNT = 7;
    
    private static int MATH_ROUND = 0;

    private DoBuy doBuy; // tøída pro práci s pøíjemkami
    
    private ArrayList<Goods> goodsItem = new ArrayList<Goods>(); //položky pøíjemky 
    
    // obìkty horní èásti dialogu
    private JComboBox suplierComboBox;  // Pole pro vložení dodavatele
    private JCalendar calendarDialog = new JCalendar();
    private JDateChooser dateChooser = new JDateChooser(calendarDialog);
    
    // hlavní èást dialogu
    private EditableGoodsTableModel goodsTableModel;
    private JTable goodsTable;   // tabulka zboží na pøíjemce 
    
    private JButton findButton;
    private JButton newButton;
    private JButton deleteButton;
    
    private JLabel statusBarTip;
    
    private Calendar calendar = new GregorianCalendar(); // kalendáø
    private PriceTableModel priceTableModel;
    private JTable priceTable;
    
    // pložky okénka souètu cen
    private JLabel priceLabel; // cena zboží
    private JLabel dphLabel; // celková daò
    private JComboBox reductionComboBox; // výbìr slevy/pøirážky
    private JTextField reductionTextField; // celková sleva
    private JLabel totalPriceLabel; // celková cena

    private JCheckBox usePriceList = new JCheckBox("Použít ceník", false);
    private JButton openPriceList = new JButton("Ceník");
    private JComboBox computePrice;
    
    private PriceList defaultPriceList = null; // ceník pro výpoèet PC z NC
    private PriceListEditor priceListEditor;
    private JTextField fakturaNumber;
    
    private User user;
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private Component owner = null;
    
    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("cs", "CZ"));
    
    /**
     * Vytvoøí nový objekt DoBuyDialog
     * @param owner Vlastník dialog
     */
    public DoBuyDialog(Frame owner, User user)  {
        super(owner, "Control - Vytvoøení Pøíjemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(); // Otevøi pøíjemky 
            //Doplò všechny dodavatele

            // Nastav výchozí ceník
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
     * Vytvoøí nový objekt DoBuyDialog
     * @param owner Vlastník dialog
     */
    public DoBuyDialog(Dialog owner, User user)  {
        super(owner, "Control - Vytvoøení Pøíjemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(); // Otevøi pøíjemky 

            // Nastav výchozí ceník
            this.priceListEditor = user.openPriceListEditor();
            this.defaultPriceList = priceListEditor.getDefaultPriceList();
            
            //Doplò všechny dodavatele
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
     * Vytvoøí nový objekt DoBuyDialog, který pøedvyplní hodnotami pøíjemky.
     * @param owner Vlastník dialog
     * @param tradeItemPreview Pøehled pøíjemky, kterou se má pøedvyplnit dialog
     */
    public DoBuyDialog(Frame owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvoøení Pøíjemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(tradeItemPreview); // Otevøi pøíjemky 
            //Doplò všechny dodavatele
            ArrayList<Suplier> supliers = user.openSupliers().getAllSupliers();
            suplierComboBox = new JComboBox(supliers.toArray());
            
            // Nastav výchozí ceník
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
        
        // Vyplò formuláø podle pøehledu pøíjemky
        setDialog();
        
    }
    
    /**
     * Vytvoøí nový objekt DoBuyDialog, který pøedvyplní hodnotami pøíjemky.
     * @param owner Vlastník dialog
     * @param tradeItemPreview Pøehled pøíjemky, kterou se má úøedvyplnit dialog
     */
    public DoBuyDialog(Dialog owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvoøení Pøíjemky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doBuy = user.openDoBuy(tradeItemPreview); // Otevøi pøíjemky 
            //Doplò všechny dodavatele
            ArrayList<Suplier> supliers = user.openSupliers().getAllSupliers();
            suplierComboBox = new JComboBox(supliers.toArray());

            // Nastav výchozí ceník
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
        
        // Vyplò formuláø podle pøehledu pøíjemky
        setDialog();
        
    }    
    
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
     * provede potøebné nastavení 
     */
    
    /**
     *  Nastaví základní vlastnosti dialogu
     */
    private void setDialog() {
        
        this.addWindowListener(this);
        setContentPane(getContent());
            
        // Nastav jednotlivé položky pøíjemky
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
      *  Vytvoøí obsah dialogu 
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
     * Vytvoøí Horní panel s editací dodavatele a datumu
     */
    private Container createTopPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Základní údaje"));
        
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

        JButton button = new JButton("Dodavatelé");
        button.addActionListener( new SuplierButtonListener() );
        content.add(button);
        
        
        return content;
    }
    
    /**
     *  Malý panýlek pro èíslo faktury
     */
    public JPanel createFakturaPanel() {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        JPanel content = new JPanel(layout);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Další údaje"));
        
        JLabel label = new JLabel("Èíslo faktury: ");
        content.add(label);
        
        fakturaNumber = new JTextField(doBuy.getBillNumber());
        fakturaNumber.setPreferredSize( new Dimension(100, 20));
        content.add(fakturaNumber);
        
        return content;
    }

    /**
     * Vytvoøí panel s tabulkou, která zobrazuje položky pøíjemky
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Položky pøíjemky"));
        
        goodsTableModel = new EditableGoodsTableModel(goodsItem);
        goodsTableModel.addTableModelListener( new GoodsTableListener() );
        goodsTable = new CommonTable(goodsTableModel); // vytvoøení tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditelné vertikální linky v tabulce
        goodsTable.addKeyListener( new DoBuyKeyListener() );
        goodsTable.addFocusListener( new ItemTableListener() );
        
        // Posluchaè výbìru øádky 
        ListSelectionModel rowSM = goodsTable.getSelectionModel();
        rowSM.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rowSM.addListSelectionListener(new SelectRowGoodsTableListener());
        
        TableColumnModel columnModel = goodsTable.getColumnModel();
        /* Nastav šíøky sloupcù */
        columnModel.getColumn(Columns.ID.getColumnNumber()).setPreferredWidth(Columns.ID.getColumnWidth()); // šíøka slouce "skladové èíslo 
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setPreferredWidth(Columns.NAME.getColumnWidth()); // šíøka slouce "skladové èíslo 
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setPreferredWidth(Columns.QUANTITY.getColumnWidth()); // šíøka slouce "skladové èíslo 
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setPreferredWidth(Columns.UNIT.getColumnWidth()); // šíøka slouce "skladové èíslo 
        
        /* Nastav editory bunìk pro zmìnu  množství */
//        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(new PriceCellRenderer());
//        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new PriceCellRenderer());
        QuantityCellEditor quantityCellEditor = new QuantityCellEditor();
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellEditor( quantityCellEditor );
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        
        /* Nastav zobrazení sloucù */
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new QuantityCellRenderer());
        JScrollPane scrollPane = new JScrollPane(goodsTable);
        
        content.add(scrollPane, BorderLayout.CENTER);
        
        return content;
    }

    /**
     *  Panel s výpisem cen
     */
    private JPanel createPricePanel() {
        JPanel content = new JPanel(gbl); 
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        
        priceTableModel = new EditablePriceTableModel(0, 0, 0, 0, 0, 0);
        //priceTableModel.setPriceList(defaultPriceList);
        priceTableModel.addTableModelListener( new PriceTableListener());
        
        priceTable = new CommonTable(priceTableModel);
        priceTable.setDefaultRenderer(Float.class, new PriceCellRenderer()); // Zobrazení sloupcù s cenou 
        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        priceTable.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(350, 150)); //Minimalní velikost panelu
        content.add(setComponent(scrollPane, 0, 0, 3, 1, 1.0, 1.0, BOTH, CENTER));
        
        /* Nastav editory bunìk pro zmìnu ceny*/
        TableColumnModel columnModel = priceTable.getColumnModel();
        columnModel.getColumn(1).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(2).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(1).setCellEditor(new PriceCellEditor(goodsTable));
        columnModel.getColumn(2).setCellEditor(new PriceCellEditor(goodsTable));
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());

        Object[] value = {"Poslední Nákupní cena", "Prùmìrná Nákupní cena", "Dražší Nákupní cena", "Levnìjší Nákupní cena", "Stará Nákupní cena"};
        computePrice = new JComboBox(value);
        computePrice.setToolTipText("Urèuje, jak se má vypoèítat Prodejní cena z Nákupních cen");
        computePrice.addItemListener( new ComputePriceCBListener() );
        computePrice.setEnabled(false);
        content.add(setComponent(computePrice, 0, 1, 1, 1, 0.0, 1.0, NONE, WEST));
        
        openPriceList.addActionListener(new OpenPriceListListener());
        openPriceList.setToolTipText("Umožní nastavit výpoèet Prodejních cen pro tuto pøíjemku");
        openPriceList.setEnabled(false);
        content.add(setComponent(openPriceList, 1, 1, 1, 1, 0.0, 1.0, NONE, EAST));
        
        usePriceList.addActionListener( new UsePriceListListener() );
        usePriceList.setToolTipText("Nastavuje zda se mají poèítat Prodejní ceny podle ceníku");
        usePriceList.setEnabled(false);
        content.add(setComponent(usePriceList, 2, 1, 1, 1, 0.0, 1.0, NONE, WEST));
        
        return content;
    }
    
    /**
     * Panel možností nastavení slevy a zobrazením souètu cen
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Celková cena"));
        content.setMinimumSize( new Dimension(200, 170));
        content.setLayout(gbl);
        
        JLabel label;
        JLabel kcLabel = new JLabel(" Kè ");
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        Object[] items = {"Sleva", "Pøirážka"};
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
        kcLabel = new JLabel(" Kè ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" DPH ");
        kcLabel = new JLabel(" Kè ");
        dphLabel = new JLabel(df.format( doBuy.getTotalDPH() ) );
        dphLabel.setFont(font);
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" Cena s DPH: ");
        kcLabel = new JLabel(" Kè ");
        totalPriceLabel = new JLabel( df.format( doBuy.getTotalPriceDPH()) );
        totalPriceLabel.setFont(font);
        content.add( setComponent(label, 0, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(totalPriceLabel, 1, 3, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 3, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }
    
    /**
     * Tlaèítka pro editace
     */
    private JPanel createEditButtonPanel () {
        JPanel content = new JPanel();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Položka"));
        content.setMinimumSize(new Dimension(140, 170)); //Minimalní velikost panelu

        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton(" Nová ", imageIcon);
        newButton.setToolTipText("Doplní novou položku pøíjemky");
        newButton.addActionListener(new NewGoodsButtonListener());
        content.add(newButton);
        
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        findButton = new JButton(" Sklad ", imageIcon);
        findButton.setToolTipText("Vyhledá zboží ze skladu");
        findButton.addActionListener(new FindGoodsButtonListener());
        content.add(findButton);
        
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vymaže oznaèenou položku pøíjemky");
        deleteButton.addActionListener(new DeleteButtonListener());
        content.add(deleteButton);
        
        return content;
    }    
    
    /**
     *  Vytvoøí panel s talèítky pro potvrzení a zrušení
     */
    private JPanel createConfirmBar() {
        JPanel content = new JPanel();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení pøíjemky"));
        
        // Tlaøítko zrušení 
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zrušit", imageIcon);
        button.setToolTipText("Zruší bez uložení vytváøenou pøíjemku");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        // tlaèítko potvrzení 
        iconURL = DoBuyDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Potvrdit", imageIcon);
        button.setToolTipText("Provede pøíjem zboží");
        button.setMnemonic(KeyEvent.VK_ENTER);
        content.add(button);
        button.addActionListener(new ConfirmButtonListener());
        
        return content;
    }
    
    /**
     *  Vytvoøí spodní èást okna s nejèastìji používanými tlaèítky
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(gbl);
        
        panel.setPreferredSize( new Dimension(50, 15));
        
        statusBarTip = new JLabel(StatusBarTips.CANCEL_CONFIRM.getText());
        panel.add(setComponent(statusBarTip, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        return panel;
    }
    
    

    
    /**
     *  Projde všechny položky pøíjemky. Vyhledá ve skladu odpovídající zboží
     *  a vloží ho do nové pøíjemky
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
     * Doplní zboží do pøíjemky - vytvoøí další øádek pøíjemky
     * 
     * @param goods zboží, které se má doplnit do pøíjemky
     * @return True, jestliže bylo vložení úspìšné, jinak false
     */
    public boolean addGoods(Goods goods) {
        Goods insertGoods = goods;
        
        // Jestliže na skladì není žádné množství, nastav že se nakoupí jeden kus
        if ( goods.getQuantity() <= 0){
            insertGoods = new Goods(goods.getGoodsID(), goods.getName(), goods.getType(),
                    goods.getDph(), goods.getUnit(), goods.getEan(), goods.getNc(), 
                    goods.getPcA(),goods.getPcB(), goods.getPcC(), goods.getPcD(), 
                    1);
        }

        TradeItem insItem = null;
        // Jestliže se podaøilo vložit zboží do pøíjemky 
        if ( (insItem = doBuy.addTradeItem(goods, insertGoods.getQuantity())) != null) {
            int row = goodsTableModel.inserRow(insertGoods); // Doplò zboží
            doBuy.setRowNumber(insItem, row);
            goodsTable.setRowSelectionInterval(row, row);
            refreshPrices();
        } else {
            ErrorMessages er = new ErrorMessages(Errors.DUPLICIT_VALUE, "\"<b>" + goods + "\"</b> není možné znovu vložit");
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return false;
        }
        
        return true;
    }
    
    /**
     * Nastaví dodavatele zboží 
     * @param suplier dodavatel
     * @return true, jestliže probìhlo nastavení v poøádku
     */
    public boolean setSuplier(Suplier suplier) {
        
        if (suplier == null) 
            return false;
        
        doBuy.setSuplier(suplier);
        suplierComboBox.setSelectedItem(suplier);
        refreshPrices(); // Obnov ceny, kvùi pøípadné zmìnì DPH podle dodavatele
        return true;
    }
    
    /**
     * Znovuzobrazí panel s cenami
     */
    private void refreshPrices() {
        // znovu zobraz  ceny
        BigDecimal value = doBuy.getTotalPrice();
        priceLabel.setText( df.format(value) );
        
        value = doBuy.getTotalDPH();
        dphLabel.setText( df.format(value) );
        
        value = doBuy.getTotalPriceDPH();
        totalPriceLabel.setText( df.format(value) );
        
        // zobr. absolutní hodnotu -> pøirážka má záporné znaménko
        value = doBuy.getReduction().abs(); 
        reductionTextField.setText( df.format(value) );
        
    } 
    
    /**
     *  Nastaví slevu 
     */
    private void setReduction() {
        String s = reductionTextField.getText().trim();
        
        try {
            Number number = df.parse(s, new ParsePosition(0)); 
            
            // Nedovol zadání nepovolených hodnot
            if (number == null || number.doubleValue() < 0)
                throw new Exception("Hodnota \"" + reductionTextField.getText() + "\" je chybná. <br> Zadávejte pouze nezáporná èísla.");

            int reduc = (new BigDecimal(number.toString()).multiply(Store.CENT)).intValue();
            // zjisti, zda se jedná o slevu, nebo o pøirážku
            if (reductionComboBox.getSelectedIndex() == 1) {
                reduc = -reduc; // otoè znamánko
            }
            // Nastav novou slevu 
            doBuy.setReduction(reduc); 
            refreshPrices();
            
        } catch (Exception ex ) {
            refreshPrices(); // zpùsobí zobrazení pùvodní správné hodnoty 
            ErrorMessages er = new ErrorMessages(Errors.NOT_EXEPT_VALUE, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    /**
     *  Nastaví datum pøíjemky
     */
    private boolean setDate() {
        java.util.Date date = dateChooser.getDate();
        calendar.setTime(date);
        doBuy.setDate(calendar);

        return true; 
    }
    
    /**
     *  Nastaví nové množství na pøíjemce
     */
    private void setQuantity() {
        int row = goodsTable.getSelectedRow(); // Vybraný øádek 

        TableModel model = goodsTableModel;

        if ( row >= model.getRowCount() || row < 0)
            return;

        double quantity = Double.parseDouble( String.valueOf(model.getValueAt(row, Columns.QUANTITY.getColumnNumber()) ) ); // zjisti množství
        String goodsId = String.valueOf( model.getValueAt(row, Columns.ID.getColumnNumber()) );
        doBuy.setQuantity(doBuy.getTradeItem(row), quantity); // Nastav množství
        
        //Jestliže je nastaven výpoèet prùmìrné ceny aktualizuj výpoèet v tabulce
        //Zpùsobí znovupøepoètení tabulky
        Object value = priceTableModel.getValueAt(0, PriceTableColumns.PRICE.getNumber());
        priceTableModel.setValueAt(value, 0, PriceTableColumns.PRICE.getNumber());
            

        refreshPrices();
    }
    
    /**
     *  Zruší vytváøenou pøíjemku 
     */
    private void cancel() {
        String text = "Opravdu chcete zrušit právì vytváøenou pøíjemku?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                DoBuyDialog.this,
                text,
                "Storno pøíjemky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }

        try {
           doBuy.update(); // zmìny potvrzujeme i pøi zrušení, pouze se nezapíše nová výdejka
        } catch (SQLException ex) {
            ex.printStackTrace();
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(DoBuyDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        
        MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazení skladu v hlavním panelu 
        
        doBuy = null;
        DoBuyDialog.this.dispose();
        
    }
    
    /**
     *  Otevøe sklad a vyhledá skladové karty, které doplní do pøíjemky
     */
    private void openStoreDialog(String s) throws SQLException, InvalidPrivilegException {
        ArrayList<Goods> items = StoreDialog.openDialog(this, user, s, true);
        
        Store store = user.openStore();
        for (Goods i: items) {
            //addGoods(i);
            // Je tøeba zboží znovu vybrat z databáze, aby ho bylo možno uzamknout
            addGoods( store.getGoodsByID(i.getGoodsID(), true));

        }
    }
    
    /**
     *  Obnoví zobrazení v tabulce cen
     */
    private void refreschPriceTable() {
       /* Zjisti index naèteného øádku */
       int selectedRow;
       if ( (selectedRow = goodsTable.getSelectedRow()) == -1) {
            return;
        }

       Goods goods = doBuy.getTradeItemAsGoods(selectedRow);

        /* Nastav potøebné hodnoty */
        priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph());         
    }
    
    /**
     *  Zonrazí dialog, kde se zadá zpùsob platby
     * @return true jestliže byl dialog potvrzen. Jinak false
     */
    private boolean selectPayDialog() {
            boolean result = true;
            
            int payment = (doBuy.isIsCash()) ? ConfirmBusinessDialog.CASH_PAYMENT : ConfirmBusinessDialog.NO_CASH_PAYMENT;
            ConfirmBusinessDialog cd = ConfirmBusinessDialog.openDiaog(this, payment);
            
            if (cd == null) {
                return false;
            }
            
            // Podle toho co uživatel vybral
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
     *  Posluchaè stisku tùaèítka doplnìní zboží
     */
    private class NewGoodsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            String[] option = {"Ok", "Zpìt"};
            
            JPanel dialogContent = new JPanel(new GridLayout(2, 1));
            JTextField textField = new JTextField(10);
            dialogContent.add(new JLabel("" +
                    "<html><center>" +
                    "Zadejte skladové èíslo, nebo název zboží." +
                    "</center></html>"));
            dialogContent.add(textField);
            
            int i = JOptionPane.showOptionDialog(
                                 DoBuyDialog.this,
                                dialogContent,
                                "Nová položka",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                option,
                                null);            
            
            // Zjisti, jestli uživatel stisknul OK.
            if (i == 0) {
            
                String s = textField.getText().trim();
                // získej odkaz na sklad, který vyvolal panel StorePanel z hlavního okna
                try {
                    Store store = user.openStore(); 
                    java.util.List<Goods> items = store.getGoodsByKeyword(s,
                            StorePanel.getCurrentLimit());

                    // Nalezl více položek zboží. Zobrazí skladový panel
                    if (items.size() > 1) { 
                        //Pokus se zboží vyhledat ve skladu
                        openStoreDialog(s);
                        return;
                    }
                    // Nalezen právì jednu položku ve skladu. Vlož jí do pøíjemky
                    if (items.size() == 1) {
                        // Vlož položku do pøíjemky 
                        // Je tøeba znovu naèíst z databáze kvùli uzamknutí
                        addGoods( store.getGoodsByID(items.get(0).getGoodsID(), true) ); 
                        return;
                    }
                    // Nenalezl žádné zboží ve skladu. Informuj o chybném zadání
                    if (items.size() == 0) {
                        ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Opravte zadání, nebo vyhledejte zboží ve skladu");
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
     *  Posluchaè stisku tlaèítka Vyhledání zboží  
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
     *  Posluchaè stisku tlaèítka Výbìr dodavatele  
     */
    private class SuplierButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            Suplier suplier = null;

            suplier = SuplierDialog.openDialog(DoBuyDialog.this, user);
            
            // Jestliže byl vytvoøen dodavatel
            if (suplier != null) {
                suplierComboBox.addItem(suplier);
                setSuplier(suplier);
            }
        }
    }
    
   /**
     *  Posluchaè stisku zmìny Výbìr dodavatele  
     */
    private class ChangeSuplierListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setSuplier( (Suplier) suplierComboBox.getSelectedItem());
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Vymazání položky 
     */
    private class DeleteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            /* Zjisti onzaèené øádky */
            ListSelectionModel listSM = goodsTable.getSelectionModel();
            int firstRow = listSM.getMinSelectionIndex();
            int lastRow = listSM.getMaxSelectionIndex();
            
            if (firstRow == -1) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète zboží, které chcete vymazat.");
                JOptionPane.showMessageDialog(owner, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            // Pole indexù, které se vymažou z tabulky 
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            
            /* Veber pøíslušné øádky z tabulky */
            /* Projdi øádky a vymaž odpovídající položky z databáze*/
            for (int i = firstRow; i <= lastRow; i++) {

                /* Zkontroluj jestli je tento øádek oznaèen (mezi prvním a posledním oznaèeným mohou být i neoznaèené */
                // 
                if (listSM.isSelectedIndex(i)) {
                    // Vymaž pøíjemky 
                    String id = String.valueOf( goodsTableModel.getValueAt(i, Columns.ID.getColumnNumber()) );
                    doBuy.deleteTradeItem(doBuy.getTradeItem(i)); // Vymaž z pøíjemky
                    indexes.add(0, i);  // Ulož, který index se bude vymazávat, na zaèátek seznamu 
                }
            }

            // øídky tabuky vymažeme až nakonec, nebo pøi postupném vymazávání by se 
            // ztratila informace, které øádky jsou oznaèené.
            // Vymazává se od zadu tabulky, aby zùstaly platné zjištìné indexi
            for (Integer i: indexes) {
                    goodsTableModel.deleteRow(i); // Vymaž z tabulky
                    doBuy.clearRowNumber(i);
            }

            refreshPrices();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Potvrzení pøíjemky
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
                
                doBuy.check(); //Zkontroluj, zda je možno potvrdit
                
                // Uživatel zadá zpùsob platby
                if (!selectPayDialog()) {
                    return; // Jestliže si uživatel rozmyslel potvrzení
                }
                
                doBuy.storno(); // Jestli byla stará pøíjemka, stornuj ji
                tr = doBuy.confirm(); // potvrd provedení pøíjemky
                doBuy.update(); // Potvrï vše

                MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazení skladu v hlavním panelu 
                MainWindow.getInstance().getBuyPanel().refresh(); // Obnov zobrazení pøíjemek v hlavním panelu
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
                // Jestliže se má tisknout
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
            
            DoBuyDialog.this.dispose(); // uzavøi okno
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Zrušení pøíjemky
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            cancel();
        }
    }
    
    
    /**
     * Posluchaè tabulky zboží. Zmìna v tabulce
     */
    private class GoodsTableListener implements TableModelListener {
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow(); // Vybraný øádek 
            
            TableModel model = (TableModel) e.getSource();
            
            if (model.getRowCount() <= row)
                return;
            
            setQuantity();
        }
        
    }

    /**
     *  Posluchaè výbìru øádky v tabulce zboží 
     */
    private class SelectRowGoodsTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // zábrání zdvojenému vyvolání uudálosti (význam mi není pøesnì znám)
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
           
           //Obnov nastavení zpùsobu výpoètu
           ItemAttributes iteAttr = doBuy.getTradeItemAttributes(doBuy.getTradeItem(rowInGoodsTable));
           computePrice.setSelectedIndex(iteAttr.getComputePrices());
           priceTableModel.setPriceList(iteAttr.getPriceList()); //Nastav ceník
           
           // Podle toho zda se používá ceník nastav zaškrtávátko
           if (iteAttr.getPriceList() == null) {
               usePriceList.setSelected(false);
           } else {
               usePriceList.setSelected(true);
           }
           
           refreschPriceTable();
        }
    }
    
    /**
     *  Posluchaè zmìny slevy 
     */
    private class ReductionTextFieldActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
        
            setReduction();
        }
    }
    
    /**
     *  Posluchaè zmìny fokusu u políèka s nastavením slevy 
     */
    private class ReductionTextFieldFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            // jestliže získá fokus, nic nedìlej
        }

        public void focusLost(FocusEvent e) {
            // jestliže ztrácí fokus, nastav slevu
            setReduction();
        }
        
    }
    
    /**
     * Posluchaè zmìny výbìru slevy/pøirážky
     */
    private class ReductionCBItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setReduction();
        }
        
    }
    
    /**
     *  Zmìna checkboxu urèující, zda se má použít ceník
     */
    private class UsePriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

           int rowInGoodsTable = goodsTable.getSelectedRow();
           
           ItemAttributes iteAttr = doBuy.getTradeItemAttributes(doBuy.getTradeItem(rowInGoodsTable));
           
           if (rowInGoodsTable == -1 || iteAttr == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète øádek, u kterého chcete nastavit ceník.");
                JOptionPane.showMessageDialog(owner, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                // Nastav opaèné zaškrtnutí
                usePriceList.setSelected( !usePriceList.isSelected());
                return;
           }
           
           // Jestliže je zaškrtnuto, nastav pouøití ceníku
           if (usePriceList.isSelected() == true) {
               // Jestliže nebyl døíve ceník asociován, nastav výchozí, jinak nastav ten asociovaný
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
     *  Stisk tlaèítka pro otevøení ceníku
     */
    private class OpenPriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

           int rowInGoodsTable = goodsTable.getSelectedRow();
           ItemAttributes iteAttr = doBuy.getTradeItemAttributes(doBuy.getTradeItem(rowInGoodsTable));

           if (rowInGoodsTable == -1 || iteAttr == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète øádek, u kterého chcete nastavit ceník.");
                JOptionPane.showMessageDialog(owner, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
           }
           
           PriceList tmp;
           // Jestliže nebyl døíve asociován ceník, použij výchozí, jinak použij ten asociovaný
            if (iteAttr == null || iteAttr.getPriceList() == null) {
                tmp = defaultPriceList;
            } else {
                tmp = iteAttr.getPriceList();
            }
           
           //Proveï zmìnu ceníku
            tmp = PriceListDialog.openPriceListDialog(DoBuyDialog.this, tmp);
            
            // Jestliže potvrdil dialog, zapni používání ceníku
            if (tmp != null) {
                usePriceList.setSelected(true); 
                priceTableModel.setPriceList(tmp); //zároveò nastav používání ceníku
                priceTableModel.refreschPrices();
                iteAttr.setPriceList(tmp);
            } 
        }
        
    }  
    
    /**
     *  Posluchaè zmìny v tabulce cen
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
            // Zabrání detekci zmìny pøi nastavení hodnoty
            model.removeTableModelListener(this);
            
            // Naèti ceny s tabulky
            int nc = new BigDecimal( 
                    String.valueOf( model.getValueAt(0, PriceTableColumns.PRICE.getNumber()) ))
                    .multiply(Store.CENT).setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue();
            
            TradeItem tradeItem = doBuy.getTradeItem(rowInGoodsTable);
            ItemAttributes itemAttr = doBuy.getTradeItemAttributes(tradeItem);
            // Jestliže došlo ke zmìnì v NC, Nech vypoèítat NC
            if (row == 0) {
                doBuy.computeNC(tradeItem, nc);
                
                double computedNC = new BigDecimal(itemAttr.getComputedNc())
                    .divide(Store.CENT).setScale(2).doubleValue();
                
                //Nastavíme vypoètenou NC do tabulky -> Pøepoètou se podle ní PC
                model.setValueAt(computedNC, 0, PriceTableColumns.PRICE.getNumber());
                
                //Doèasnì vypneme ceník a nastavíme pùvodní NC (tu uživatelem zadanou)
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

            //Nastav tool tip s nákupními cenami
            BigDecimal lastNC = new BigDecimal(itemAttr.getInputGoods().getNc()).divide(Store.CENT); 
            BigDecimal newNC = new BigDecimal(itemAttr.getNewNc()).divide(Store.CENT); 
            BigDecimal computedNC = new BigDecimal(itemAttr.getComputedNc()).divide(Store.CENT); 
            priceTable.setToolTipText("<html>" +
                    "Stará Nákupní cena: <b>" + df.format(lastNC) + "</b><br>" +
                    "Nová nákupní cena: <b>" + df.format(newNC) + "</b><br>" +
                    "Pro výpoèet použita Nákupní cena: <b>" + df.format(computedNC) + "</b>" +
                    "</html>");
            
            //Nastav nové ceny
            doBuy.setNewPcPrices(doBuy.getTradeItem(rowInGoodsTable), pcA, pcB, pcC, pcD);
            refreshPrices();
            
            // Obnov listener
            model.addTableModelListener(this);
        }  
    }
    
    /**
     * Posluchaè zmìny výbìru slevy/pøirážky
     */
    private class ComputePriceCBListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            
            int rowInGoodsTable = goodsTable.getSelectedRow();
            if (rowInGoodsTable == -1 || computePrice.getSelectedIndex() == -1) {
                return;
            }   
            
            TradeItem tradeItem = doBuy.getTradeItem(rowInGoodsTable);
            doBuy.setComputeNCPrice(tradeItem, computePrice.getSelectedIndex());
         
            // Obnov kvùli pøechodu z øádku na øádek, aby bylo zobrazení aktuální
            refreschPriceTable();
            
            //Zpùsobí znovupøepoètení tabulky
            Object value = priceTableModel.getValueAt(0, PriceTableColumns.PRICE.getNumber());
            priceTableModel.setValueAt(value, 0, PriceTableColumns.PRICE.getNumber());
            
            refreshPrices(); // obnov souèty
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
