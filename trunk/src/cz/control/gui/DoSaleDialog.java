/*
 * DoSaleDialog.java
 *
 * Vytvoøeno 14. únor 2006, 23:19
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.Customer;
import cz.control.data.Goods;
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
import java.awt.*;
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


import static java.awt.GridBagConstraints.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí dialog pro tvorbu výdejky (Expedice)
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class DoSaleDialog extends JDialog implements  WindowListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private static final int SUPLIER_ROW_COUNT = 7;
    public static final int QUANTITY_ROUND = Settings.QUANTITY_ROUND;

    private DoSale doSale; // tøída pro práci s výdejkami
    
    private ArrayList<Goods> goodsItem = new ArrayList<Goods>(); //položky výdejky
    
    // obìkty horní èásti dialogu
    private JComboBox customersComboBox;  // Pole pro vložení dodavatele
    private JCalendar calendarDialog = new JCalendar();
    private JDateChooser dateChooser = new JDateChooser(calendarDialog);
    
    // hlavní èást dialogu
    private EditableGoodsTableModel goodsTableModel;
    private JTable goodsTable;   // tabulka zboží na výdejce
    
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
    
    private User user;
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private Component owner = null;
    
    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("cs", "CZ"));
    
    /**
     * Vytvoøí nový objekt DoSaleDialog
     * @param owner Vlastník dialogu
     */
    public DoSaleDialog(Frame owner, User user)  {
        super(owner, "Control - Vytvoøení Výdejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(); // Otevøi výdejky

            //Doplò všechny odbìratele
            ArrayList<Customer> customers = user.openCustomers().getAllCustomers();
            customersComboBox = new JComboBox(customers.toArray());
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        calendar.setTimeInMillis(System.currentTimeMillis());
        setDialog();
        
    }
    
    /**
     * Vytvoøí nový objekt DoSaleDialog
     * @param owner Vlastník dialogu
     */
    public DoSaleDialog(Dialog owner, User user)  {
        super(owner, "Control - Vytvoøení Výdejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(); // Otevøi výdejky

            //Doplò všechny odbìratele
            ArrayList<Customer> customers = user.openCustomers().getAllCustomers();
            customersComboBox = new JComboBox(customers.toArray());
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
       
        calendar.setTimeInMillis(System.currentTimeMillis());
        setDialog();
        
    }    
    
    /**
     * Vytvoøí nový objekt DoSaleDialog, který pøedvyplní hodnotami výdejky.
     * @param owner Vlastník dialog
     * @param tradeItemPreview Pøehled výdejky, kterou se má pøedvyplnit dialog
     */
    public DoSaleDialog(Frame owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvoøení Výdejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(tradeItemPreview); // Otevøi výdejky

            //Doplò všechny odbìratele
            ArrayList<Customer> customers = user.openCustomers().getAllCustomers();
            customersComboBox = new JComboBox(customers.toArray());
            
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
  
        // Vyplò formuláø podle pøehledu výdejky
        setDialog();
        
    }
    
    /**
     * Vytvoøí nový objekt DoSaleDialog, který pøedvyplní hodnotami výdejky.
     * @param owner Vlastník dialog
     * @param tradeItemPreview Pøehled výdejky, kterou se má pøedvyplnit dialog
     */
    public DoSaleDialog(Dialog owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvoøení Výdejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(tradeItemPreview); // Otevøi výdejky

            //Doplò všechny odbìratele
            ArrayList<Customer> customers = user.openCustomers().getAllCustomers();
            customersComboBox = new JComboBox(customers.toArray());
            
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
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
        content.add(setComponent(createConfirmBar(), 0, 3, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createStatusBar(), 0, 4, 3, 1, 1.0, 0.0, HORIZONTAL, CENTER));

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
     * Vytvoøí Horní panel s editací odbìratele a datumu
     */
    private Container createTopPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Základní údaje"));
        
        JLabel label = new JLabel("Datum: ");
        content.add(label);

        calendarDialog.setDecorationBackgroundVisible(false);
        calendarDialog.setCalendar( doSale.getDate() );
        content.add(dateChooser);

        label = new JLabel("Odbìratelé: ");
        content.add(label);
        
        customersComboBox.setPreferredSize( new Dimension(230, 20));
        customersComboBox.setMaximumRowCount(SUPLIER_ROW_COUNT);
        Customer cust = doSale.getCustomer();
        if (cust != null)
            customersComboBox.setSelectedItem(cust);
        customersComboBox.addActionListener( new ChangeCustomerListener() );
        content.add(customersComboBox);

        JButton button = new JButton("Odbìratelé");
        button.addActionListener( new SuplierButtonListener() );
        content.add(button);
        
        return content;
    }

    /**
     * Vytvoøí panel s tabulkou, která zobrazuje položky pøíjemky
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Položky výdejky"));
        
        goodsTableModel = new EditableGoodsTableModel(goodsItem);
        goodsTableModel.addTableModelListener( new GoodsTableListener() );
        goodsTable = new CommonTable(goodsTableModel); // vytvoøení tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditelné vertikální linky v tabulce
        goodsTable.addKeyListener( new DoSaleKeyListener() );
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
        /* Nastav zobrazení sloucù */
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new QuantityCellRenderer());
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        JScrollPane scrollPane = new JScrollPane(goodsTable);
        
        content.add(scrollPane, BorderLayout.CENTER);
        
        
        return content;
    }

    /**
     *  Panel s výpisem cen
     */
    private JPanel createPricePanel() {
        JPanel content = new JPanel(new GridLayout(1,1)); // GridLayer proto, aby bylo možno nastavit velikost panelu
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        
        // V budoucnu bude upraveno na editovatelnou tabulku 
        priceTableModel = new SelectablePriceTableModel(0, 0, 0, 0, 0, 0, doSale.getUsePrice());
        //priceTableModel = new PriceTableModel(0, 0, 0, 0, 0, 0);
        priceTableModel.addTableModelListener( new PriceTableListener() );
        
        priceTable = new CommonTable(priceTableModel);
        priceTable.setDefaultRenderer(Float.class, new PriceCellRenderer()); // Zobrazení sloupcù s cenou 
        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        priceTable.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(350, 150)); //Minimalní velikost panelu
        content.add(scrollPane);
        
        /* Nastav editory bunìk pro zmìnu ceny*/
        TableColumnModel columnModel = priceTable.getColumnModel();
        columnModel.getColumn(PriceTableColumns.PRICE.getNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(PriceTableColumns.PRICE_DPH.getNumber()).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(PriceTableColumns.PRICE.getNumber()).setCellEditor(new PriceCellEditor(goodsTable));
        columnModel.getColumn(PriceTableColumns.PRICE_DPH.getNumber()).setCellEditor(new PriceCellEditor(goodsTable));
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());
        
        //columnModel.getColumn(PriceTableColumns.USE_PRICE.getNumber()).setCellRenderer(new UsePriceTableCellRenderer() );
        //columnModel.getColumn(PriceTableColumns.USE_PRICE.getNumber()).setCellEditor(new UsePriceTableCellEditor() );
        
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
        reductionComboBox.setSelectedItem( doSale.getReduction().intValue() >= 0 ? 0 : 1);
        reductionComboBox.addItemListener( new ReductionCBItemListener() );
        kcLabel = new JLabel(" % ");
        reductionTextField = new JTextField(df.format( doSale.getReduction().abs() ));
        reductionTextField.setFont(font);
        reductionTextField.setHorizontalAlignment(JTextField.RIGHT);
        reductionTextField.addActionListener( new ReductionTextFieldActionListener());
        reductionTextField.addFocusListener( new ReductionTextFieldFocusListener() );
        content.add( setComponent(reductionComboBox, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(reductionTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, EAST) );
        content.add( setComponent(kcLabel, 2, 0, 1, 1, 0.0, 0.0, NONE, WEST) );

        label = new JLabel(" Cena bez DPH: ");
        priceLabel = new JLabel(df.format( doSale.getTotalPrice() ) );
        priceLabel.setFont(font);
        kcLabel = new JLabel(" Kè ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" DPH ");
        kcLabel = new JLabel(" Kè ");
        dphLabel = new JLabel(df.format( doSale.getTotalDPH() ) );
        dphLabel.setFont(font);
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" Cena s DPH: ");
        kcLabel = new JLabel(" Kè ");
        totalPriceLabel = new JLabel( df.format( doSale.getTotalPriceDPH()) );
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

        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton(" Nová ", imageIcon);
        newButton.setToolTipText("Doplní novou položku výdejky");
        newButton.addActionListener(new NewGoodsButtonListener());
        content.add(newButton);
        
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        findButton = new JButton(" Sklad ", imageIcon);
        findButton.setToolTipText("Vyhledá zboží ze skladu");
        findButton.addActionListener(new FindGoodsButtonListener());
        content.add(findButton);
        
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vymaže oznaèenou položku výdejky");
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
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení výdejky"));
        
        // Tlaøítko zrušení 
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zrušit", imageIcon);
        button.setToolTipText("Zruší bez uložení vytváøenou výdejku");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        // tlaèítko potvrzení 
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Potvrdit", imageIcon);
        button.setToolTipText("Provede výdej zboží");
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
     *  Projde všechny položky výdejky. Vyhledá ve skladu odpovídající zboží
     *  a vloží ho do nové výdejky
     */
    private void setGoodsItems() {
        Set<TradeItem> tradeItems = doSale.getItems();
        

        for (TradeItem i: tradeItems) {
            int row = goodsTableModel.inserRow(i.getAsGoods());
            doSale.setRowNumber(i, row);
        }
    }
    

    /**
     * Doplní zboží do výdejky - vytvoøí další øádek 
     * 
     * @param goods zboží, které se má doplnit do výdejky
     * @return True, jestliže bylo vložení úspìšné, jinak false
     */
    public boolean addGoods(Goods goods) {
        
        // Nulové skladové karty nevkládej
        if (goods.getQuantity() <= 0) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_ENOUGHT_QUANTITY, 
                    "Položka: <b>" + goods.getGoodsID()+ " - " + goods.getName() + "</b><br>" +
                    "Stav na skladì: <b>" + goods.getQuantity() + "</b><br>" +
                    "Položka nebude zahrnuta do prodeje");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 

            return false;
        }
        
        TradeItem tradeItem = null;
        // Jestliže se podaøilo vložit zboží do výdejky
        if ( (tradeItem = doSale.addTradeItem(goods, goods.getQuantity())) != null) {
            // Jestliže už takové zboží existuje, použij jeho ceny
            int row = goodsTableModel.inserRow(goods); // Doplò zboží
            doSale.setRowNumber(tradeItem, row);
            // Oznaè øádek na který bylo vloženo
            goodsTable.setRowSelectionInterval(row, row);
            // Nastav maximální možné množství
            goodsTableModel.setValueAt(goods.getQuantity() + doSale.getAvailableQuantity(goods.getGoodsID()), row, Columns.QUANTITY.getColumnNumber());
            //setQuantity();
            refreshPrices();
        } else {
            ErrorMessages er = new ErrorMessages(Errors.DUPLICIT_VALUE, "\"<b>" + goods + "\"</b> není možné znovu vložit");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return false;
        }
        
        return true;
    }
    
    /**
     * Nastaví odbìratele zboží 
     * @param customer Odbìratel
     * @return true, jestliže probìhlo nastavení v poøádku
     */
    public boolean setCustomer(Customer customer) {
        
        if (customer == null) 
            return false;
        
        doSale.setCustomer(customer);
        customersComboBox.setSelectedItem(customer);
        refreshPrices(); // Obnov ceny, kvùi pøípadné zmìnì DPH podle odbìratele
        return true;
    }
    
    /**
     * Znovuzobrazí panel s cenami
     */
    private void refreshPrices() {
        // znovu zobraz  ceny
        BigDecimal value = doSale.getTotalPrice();
        priceLabel.setText( df.format(value) );
        
        value = doSale.getTotalDPH();
        dphLabel.setText( df.format(value) );
        
        value = doSale.getTotalPriceDPH();
        totalPriceLabel.setText( df.format(value) );
        
        // zobr. absolutní hodnotu -> pøirážka má záporné znaménko
        value = doSale.getReduction().abs(); 
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
            doSale.setReduction(reduc); 
            refreshPrices();
            
        } catch (Exception ex ) {
            refreshPrices(); // zpùsobí zobrazení pùvodní správné hodnoty 
            ErrorMessages er = new ErrorMessages(Errors.NOT_EXEPT_VALUE, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    /**
     *  Nastaví datum 
     */
    private boolean setDate() {
        java.util.Date date = dateChooser.getDate();
        calendar.setTime(date);
        doSale.setDate(calendar);

        return true; 
    }
    
    /**
     *  Nastaví nové množství 
     */
    private void setQuantity() {
        int row = goodsTable.getSelectedRow(); // Vybraný øádek 

        TableModel model = goodsTableModel;

        if ( row >= model.getRowCount() || row < 0)
            return;

        BigDecimal quantity = new BigDecimal( String.valueOf(model.getValueAt(row, Columns.QUANTITY.getColumnNumber()) ) ); // zjisti množství
        String goodsId = String.valueOf( model.getValueAt(row, Columns.ID.getColumnNumber()) );

        BigDecimal availableQuantity = new BigDecimal(doSale.getAvailableQuantity(goodsId)).add( new BigDecimal(doSale.getTradeItem(row).getQuantity()) );
        availableQuantity = availableQuantity.setScale(Settings.QUANTITY_ROUND, RoundingMode.HALF_UP);
        
        // Nechceme prodat více než je na skladì
        if (quantity.subtract(availableQuantity).signum() == 1 ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_ENOUGHT_QUANTITY, 
                    "Maximální použitelné množství je: <b>" + Settings.getFloatFormat().format(availableQuantity) + "</b>.<br>" +
                    "Opravte prosím zadání.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            
            quantity = availableQuantity;
            goodsTableModel.setValueAt(quantity, row, Columns.QUANTITY.getColumnNumber());
        }        
        
        doSale.setQuantity(doSale.getTradeItem(row), quantity.doubleValue()); // Nastav množství

        refreshPrices();
    }
    
    /**
     *  Zruší vytváøenou výdejku
     */
    private void cancel() {
        String text = "Opravdu chcete zrušit právì vytváøenou výdejku?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                DoSaleDialog.this,
                text,
                "Storno výdejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }
        
        try {
           doSale.update(); // zmìny potvrzujeme i pøi zrušení, pouze se nezapíše nová výdejka
        } catch (SQLException ex) {
            ex.printStackTrace();
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        MainWindow.getInstance().getStockingsPanel().refresh();
        
        doSale = null;
        this.dispose();
        
    }
    
    
    /**
     *  Otevøe sklad a vyhledá skladové karty, které doplní do pøíjemky
     */
    private void openStoreDialog(String s) throws SQLException, InvalidPrivilegException {
        ArrayList<Goods> items = StoreDialog.openDialog(this, user, s, false);
        
        Store store = user.openStore();
        for (Goods i: items) {
            //addGoods(i);
            // Je tøeba zboží znovu vybrat z databáze, aby ho bylo možno uzamknout
            addGoods( store.getGoodsByID(i.getGoodsID(), true));
        }
    }    
    
    /**
     *  Zonrazí dialog, kde se zadá zpùsob platby
     * @return true jestliže byl dialog potvrzen. Jinak false
     */
    private boolean selectPayDialog() {
            boolean result = true;
            
            ConfirmBusinessDialog cd = ConfirmBusinessDialog.openDiaog(this);
            
            if (cd == null) {
                return false;
            }
            
            // Podle toho co uživatel vybral
            switch (ConfirmBusinessDialog.getPayment()) {
                case ConfirmBusinessDialog.CASH_PAYMENT:
                    doSale.makeRound(DoBuy.ROUND_SCALE_TO_100);
                    refreshPrices();
                    
                    if (ConfirmBusinessDialog.isUseCalc()) {
                        result = LitleCalcDialog.openDiaog(this, doSale.getTotalPriceDPH().multiply(Store.CENT).longValue());
                    }
                    
                    break;
                case ConfirmBusinessDialog.NO_CASH_PAYMENT:    
                    doSale.makeRound(DoBuy.ROUND_SCALE_UNNECESSARY);
                    refreshPrices();
                    break;
                default :
                    result = false;
            }

            return result;
    }
    
    /**
     *  Provede kontrolu ztrátových prodejù (tìch kde PC < NC) a zobrazí chybový dialog.
     *  Uživatel bude vyžádan potvrdil, nebo zrušit prodej
     * @return true - jestliže si uživatel pøeje pokraèovat prodejem,
     * false - jestliže chce zrušit potvrzení a opravit zadání
     */
    private boolean  checkLossPriceItems() {
        ArrayList<TradeItem> items = doSale.getLossPriceItems();
        
        if (items.isEmpty()) {
            return true;
        }
        
        String[] option = {"Opravit", "Neopravovat"};
            
        StringBuffer text = new StringBuffer();
        text.append("<html>Následující položky jsou prodávány za cenu nížší než nákupní:<br><br>");
        
        // Vytvoø øetìzec ztrátových položek
        for (TradeItem i: items) {
            text.append("<b>" + i.getGoodsId() + " - " + i.getName() + "</b><br>");
        }    
        text.append("</center></html>");
            
        int i = JOptionPane.showOptionDialog(
                             DoSaleDialog.this,
                            text,
                            "Ztrátový prodej",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            option,
                            null);            

        // Zjisti, jestli uživatel stisknul OK.
        if (i == 0) {
            return false;
        }        
        
        return true;
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
                                 DoSaleDialog.this,
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
                // získej odkaz na sklad
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
                    // Nalezen právì jednu položku ve skladu. Vlož jí 
                    if (items.size() == 1) {
                        // Vlož položku do pøíjemky 
                        // Je tøeba znovu naèíst z databáze kvùli uzamknutí
                        addGoods( store.getGoodsByID(items.get(0).getGoodsID(), true) ); 
                        return;
                    }
                    // Nenalezl žádné zboží ve skladu. Informuj o chybném zadání
                    if (items.size() == 0) {
                        ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Opravte zadání, nebo vyhledejte zboží ve skladu");
                        JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                        return;
                    }
                } catch (InvalidPrivilegException exception) {
                    ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                    JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                    return;
                } catch (SQLException ex) {
                    ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                    JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
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
               JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
           } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            }
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Výbìr odbìratele
     */
    private class SuplierButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            Customer customer = null;
            customer = CustomerDialog.openDialog(DoSaleDialog.this, user);
            
            // Jestliže byl vytvoøen 
            if (customer != null) {
                customersComboBox.addItem(customer);
                setCustomer(customer);
            }
        }
    }
    
   /**
     *  Posluchaè stisku zmìny Výbìr odbìratele  
     */
    private class ChangeCustomerListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setCustomer( (Customer) customersComboBox.getSelectedItem());
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
                    doSale.deleteTradeItem(doSale.getTradeItem(i)); // Vymaž z pøíjemky
                    indexes.add(0, i);  // Ulož, který index se bude vymazávat, na zaèátek seznamu 
                }
            }

            // øádky tabuky vymažeme až nakonec, nebo pøi postupném vymazávání by se 
            // ztratila informace, které øádky jsou oznaèené.
            // Vymazává se od zadu tabulky, aby zùstaly platné zjištìné indexi
            for (Integer i: indexes) {
                    goodsTableModel.deleteRow(i); // Vymaž z tabulky
                    doSale.clearRowNumber(i);
            }

            refreshPrices();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Potvrzení výdejky
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            TradeItemPreview tr = null;
            try {
                if (!setDate())
                    return; // nastav datum
                
                setCustomer( (Customer) customersComboBox.getSelectedItem()); // Nastav dodavatele
                
                doSale.check(); //Zkontroluj, zda je možno potvrdit
                
                if (!checkLossPriceItems()) {
                    return;
                }
                
                // Uživatel zadá zpùsob platby
                if (!selectPayDialog()) {
                    return; // Jestliže si uživatel rozmyslel potvrzení
                }
                
                doSale.storno(); // Jestli byla stará výdejka, stornuj ji
                tr = doSale.confirm(); // potvrd provedení výdejky
                doSale.update(); // Potvrï vše
                
                MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazení skladu v hlavním panelu 
                MainWindow.getInstance().getSalePanel().refresh(); // Obnov zobrazení pøíjemek v hlavním panelu
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (Exception ex) {
                ErrorMessages er = new ErrorMessages(Errors.NOT_POSIBLE_CONFIRM_SALE, ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }

            try {
                // Jestliže se má tisknout
                if (ConfirmBusinessDialog.getResult() != null && ConfirmBusinessDialog.isPrint()) {
                    Print.printSale(tr);
                }        
            } catch (JRException ex) {
                ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoSaleDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }
            
            DoSaleDialog.this.dispose(); // uzavøi okno
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Zrušení výdejky
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
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // získej model výbìru
           /* Zjisti index naèteného øádku */
           int selectedRow;
           if ( (selectedRow = lsm.getMinSelectionIndex()) == -1) {
               priceTable.setVisible(false);
                return;
           }

           TradeItem tradeItem = doSale.getTradeItem(selectedRow);
           // oznaè cenu, která je vybrána
           // Nastav zaškrtávátka
            for (int i = 0; i < priceTableModel.getRowCount(); i++) {
                priceTableModel.setValueAt(i == tradeItem.getUsePrice(), i, 
                        PriceTableColumns.USE_PRICE.getNumber());
            }

           
           priceTable.setVisible(true);
           Goods goods = doSale.getTradeItemAsGoods(selectedRow);
           priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph()); 
        }
    }
    
    /**
     *  Posluchaè zmìny v tabulce cen
     */
    private class PriceTableListener implements TableModelListener {
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();
            
            TableModel model = (TableModel) e.getSource();

            int goodsRow = 0;
            if ( (goodsRow = goodsTable.getSelectedRow()) < 0 ) {
                return;
            }
            
            // jestliže se zmìnila cena bez DPH, nastav novou cenu do výdejky
            if (col == PriceTableColumns.PRICE.getNumber()) {
                if ( (goodsRow = goodsTable.getSelectedRow()) != -1 ) {
                    TradeItem tradeItem = doSale.getTradeItem(goodsRow);
                    int nc = new BigDecimal( String.valueOf(model.getValueAt(0, col)) ).multiply(Store.CENT).intValue();
                    int pcA = new BigDecimal( String.valueOf(model.getValueAt(1, col)) ).multiply(Store.CENT).intValue();
                    int pcB = new BigDecimal( String.valueOf(model.getValueAt(2, col)) ).multiply(Store.CENT).intValue();
                    int pcC = new BigDecimal( String.valueOf(model.getValueAt(3, col)) ).multiply(Store.CENT).intValue();
                    int pcD = new BigDecimal( String.valueOf(model.getValueAt(4, col)) ).multiply(Store.CENT).intValue();
                    // Znovunastav všechny ceny
                    doSale.setNewPrices(tradeItem, nc, pcA, pcB, pcC, pcD, row);
                    refreshPrices();
                }
            }

            // Zmìnu jinde neeviduj
            if (col != PriceTableColumns.USE_PRICE.getNumber())
                return;
            
            Boolean checked = (Boolean) model.getValueAt(row, col);
            
            // Zabrání detekci zmìny pøi nastavení hodnoty
            model.removeTableModelListener(this);
            TradeItem tradeItem = doSale.getTradeItem(goodsRow);

            // pokud se mìnila hodnota na true
            if (checked) {
                // nastav novou cenu
                tradeItem = doSale.setNewUsePrice(tradeItem, row);
            }

            // ostatní øádky nastav na false
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(Boolean.valueOf(i == tradeItem.getUsePrice()), i, col);
            }

            refreshPrices();
            
            // Obnov listener
            model.addTableModelListener(this);
            

          
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
    
    private class DoSaleKeyListener implements KeyListener {
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