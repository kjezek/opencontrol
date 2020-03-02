/*
 * DoSaleDialog.java
 *
 * Vytvo�eno 14. �nor 2006, 23:19
 *
 * Autor: Kamil Je�ek
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


import static java.awt.GridBagConstraints.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialog pro tvorbu v�dejky (Expedice)
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class DoSaleDialog extends JDialog implements  WindowListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private static final int SUPLIER_ROW_COUNT = 7;
    public static final int QUANTITY_ROUND = Settings.QUANTITY_ROUND;

    private DoSale doSale; // t��da pro pr�ci s v�dejkami
    
    private ArrayList<Goods> goodsItem = new ArrayList<Goods>(); //polo�ky v�dejky
    
    // ob�kty horn� ��sti dialogu
    private JComboBox customersComboBox;  // Pole pro vlo�en� dodavatele
    private JCalendar calendarDialog = new JCalendar();
    private JDateChooser dateChooser = new JDateChooser(calendarDialog);
    
    // hlavn� ��st dialogu
    private EditableGoodsTableModel goodsTableModel;
    private JTable goodsTable;   // tabulka zbo�� na v�dejce
    
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
    
    private User user;
    
    private static DecimalFormat df = Settings.getPriceFormat();
    
    private Component owner = null;
    
    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("cs", "CZ"));
    
    /**
     * Vytvo�� nov� objekt DoSaleDialog
     * @param owner Vlastn�k dialogu
     */
    public DoSaleDialog(Frame owner, User user)  {
        super(owner, "Control - Vytvo�en� V�dejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(); // Otev�i v�dejky

            //Dopl� v�echny odb�ratele
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
     * Vytvo�� nov� objekt DoSaleDialog
     * @param owner Vlastn�k dialogu
     */
    public DoSaleDialog(Dialog owner, User user)  {
        super(owner, "Control - Vytvo�en� V�dejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(); // Otev�i v�dejky

            //Dopl� v�echny odb�ratele
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
     * Vytvo�� nov� objekt DoSaleDialog, kter� p�edvypln� hodnotami v�dejky.
     * @param owner Vlastn�k dialog
     * @param tradeItemPreview P�ehled v�dejky, kterou se m� p�edvyplnit dialog
     */
    public DoSaleDialog(Frame owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvo�en� V�dejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(tradeItemPreview); // Otev�i v�dejky

            //Dopl� v�echny odb�ratele
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
  
        // Vypl� formul�� podle p�ehledu v�dejky
        setDialog();
        
    }
    
    /**
     * Vytvo�� nov� objekt DoSaleDialog, kter� p�edvypln� hodnotami v�dejky.
     * @param owner Vlastn�k dialog
     * @param tradeItemPreview P�ehled v�dejky, kterou se m� p�edvyplnit dialog
     */
    public DoSaleDialog(Dialog owner, User user, TradeItemPreview tradeItemPreview)  {
        super(owner, "Control - Vytvo�en� V�dejky", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doSale = user.openDoSale(tradeItemPreview); // Otev�i v�dejky

            //Dopl� v�echny odb�ratele
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
        content.add(setComponent(createConfirmBar(), 0, 3, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createStatusBar(), 0, 4, 3, 1, 1.0, 0.0, HORIZONTAL, CENTER));

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
     * Vytvo�� Horn� panel s editac� odb�ratele a datumu
     */
    private Container createTopPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Z�kladn� �daje"));
        
        JLabel label = new JLabel("Datum: ");
        content.add(label);

        calendarDialog.setDecorationBackgroundVisible(false);
        calendarDialog.setCalendar( doSale.getDate() );
        content.add(dateChooser);

        label = new JLabel("Odb�ratel�: ");
        content.add(label);
        
        customersComboBox.setPreferredSize( new Dimension(230, 20));
        customersComboBox.setMaximumRowCount(SUPLIER_ROW_COUNT);
        Customer cust = doSale.getCustomer();
        if (cust != null)
            customersComboBox.setSelectedItem(cust);
        customersComboBox.addActionListener( new ChangeCustomerListener() );
        content.add(customersComboBox);

        JButton button = new JButton("Odb�ratel�");
        button.addActionListener( new SuplierButtonListener() );
        content.add(button);
        
        return content;
    }

    /**
     * Vytvo�� panel s tabulkou, kter� zobrazuje polo�ky p��jemky
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ky v�dejky"));
        
        goodsTableModel = new EditableGoodsTableModel(goodsItem);
        goodsTableModel.addTableModelListener( new GoodsTableListener() );
        goodsTable = new CommonTable(goodsTableModel); // vytvo�en� tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        goodsTable.addKeyListener( new DoSaleKeyListener() );
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
        /* Nastav zobrazen� slouc� */
        columnModel.getColumn(Columns.QUANTITY.getColumnNumber()).setCellRenderer(new QuantityCellRenderer());
        columnModel.getColumn(Columns.NAME.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.ID.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(Columns.UNIT.getColumnNumber()).setCellRenderer(new CommonItemCellRenderer());
        JScrollPane scrollPane = new JScrollPane(goodsTable);
        
        content.add(scrollPane, BorderLayout.CENTER);
        
        
        return content;
    }

    /**
     *  Panel s v�pisem cen
     */
    private JPanel createPricePanel() {
        JPanel content = new JPanel(new GridLayout(1,1)); // GridLayer proto, aby bylo mo�no nastavit velikost panelu
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        
        // V budoucnu bude upraveno na editovatelnou tabulku 
        priceTableModel = new SelectablePriceTableModel(0, 0, 0, 0, 0, 0, doSale.getUsePrice());
        //priceTableModel = new PriceTableModel(0, 0, 0, 0, 0, 0);
        priceTableModel.addTableModelListener( new PriceTableListener() );
        
        priceTable = new CommonTable(priceTableModel);
        priceTable.setDefaultRenderer(Float.class, new PriceCellRenderer()); // Zobrazen� sloupc� s cenou 
        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        priceTable.setVisible(false);
        
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(350, 150)); //Minimaln� velikost panelu
        content.add(scrollPane);
        
        /* Nastav editory bun�k pro zm�nu ceny*/
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
        kcLabel = new JLabel(" K� ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 1, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" DPH ");
        kcLabel = new JLabel(" K� ");
        dphLabel = new JLabel(df.format( doSale.getTotalDPH() ) );
        dphLabel.setFont(font);
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(dphLabel, 1, 2, 1, 1, 1.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        
        label = new JLabel(" Cena s DPH: ");
        kcLabel = new JLabel(" K� ");
        totalPriceLabel = new JLabel( df.format( doSale.getTotalPriceDPH()) );
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

        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton(" Nov� ", imageIcon);
        newButton.setToolTipText("Dopln� novou polo�ku v�dejky");
        newButton.addActionListener(new NewGoodsButtonListener());
        content.add(newButton);
        
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        findButton = new JButton(" Sklad ", imageIcon);
        findButton.setToolTipText("Vyhled� zbo�� ze skladu");
        findButton.addActionListener(new FindGoodsButtonListener());
        content.add(findButton);
        
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vyma�e ozna�enou polo�ku v�dejky");
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
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� v�dejky"));
        
        // Tla��tko zru�en� 
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zru�it", imageIcon);
        button.setToolTipText("Zru�� bez ulo�en� vytv��enou v�dejku");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        // tla��tko potvrzen� 
        iconURL = DoSaleDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Potvrdit", imageIcon);
        button.setToolTipText("Provede v�dej zbo��");
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
     *  Projde v�echny polo�ky v�dejky. Vyhled� ve skladu odpov�daj�c� zbo��
     *  a vlo�� ho do nov� v�dejky
     */
    private void setGoodsItems() {
        Set<TradeItem> tradeItems = doSale.getItems();
        

        for (TradeItem i: tradeItems) {
            int row = goodsTableModel.inserRow(i.getAsGoods());
            doSale.setRowNumber(i, row);
        }
    }
    

    /**
     * Dopln� zbo�� do v�dejky - vytvo�� dal�� ��dek 
     * 
     * @param goods zbo��, kter� se m� doplnit do v�dejky
     * @return True, jestli�e bylo vlo�en� �sp�n�, jinak false
     */
    public boolean addGoods(Goods goods) {
        
        // Nulov� skladov� karty nevkl�dej
        if (goods.getQuantity() <= 0) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_ENOUGHT_QUANTITY, 
                    "Polo�ka: <b>" + goods.getGoodsID()+ " - " + goods.getName() + "</b><br>" +
                    "Stav na sklad�: <b>" + goods.getQuantity() + "</b><br>" +
                    "Polo�ka nebude zahrnuta do prodeje");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 

            return false;
        }
        
        TradeItem tradeItem = null;
        // Jestli�e se poda�ilo vlo�it zbo�� do v�dejky
        if ( (tradeItem = doSale.addTradeItem(goods, goods.getQuantity())) != null) {
            // Jestli�e u� takov� zbo�� existuje, pou�ij jeho ceny
            int row = goodsTableModel.inserRow(goods); // Dopl� zbo��
            doSale.setRowNumber(tradeItem, row);
            // Ozna� ��dek na kter� bylo vlo�eno
            goodsTable.setRowSelectionInterval(row, row);
            // Nastav maxim�ln� mo�n� mno�stv�
            goodsTableModel.setValueAt(goods.getQuantity() + doSale.getAvailableQuantity(goods.getGoodsID()), row, Columns.QUANTITY.getColumnNumber());
            //setQuantity();
            refreshPrices();
        } else {
            ErrorMessages er = new ErrorMessages(Errors.DUPLICIT_VALUE, "\"<b>" + goods + "\"</b> nen� mo�n� znovu vlo�it");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return false;
        }
        
        return true;
    }
    
    /**
     * Nastav� odb�ratele zbo�� 
     * @param customer Odb�ratel
     * @return true, jestli�e prob�hlo nastaven� v po��dku
     */
    public boolean setCustomer(Customer customer) {
        
        if (customer == null) 
            return false;
        
        doSale.setCustomer(customer);
        customersComboBox.setSelectedItem(customer);
        refreshPrices(); // Obnov ceny, kv�i p��padn� zm�n� DPH podle odb�ratele
        return true;
    }
    
    /**
     * Znovuzobraz� panel s cenami
     */
    private void refreshPrices() {
        // znovu zobraz  ceny
        BigDecimal value = doSale.getTotalPrice();
        priceLabel.setText( df.format(value) );
        
        value = doSale.getTotalDPH();
        dphLabel.setText( df.format(value) );
        
        value = doSale.getTotalPriceDPH();
        totalPriceLabel.setText( df.format(value) );
        
        // zobr. absolutn� hodnotu -> p�ir�ka m� z�porn� znam�nko
        value = doSale.getReduction().abs(); 
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
            doSale.setReduction(reduc); 
            refreshPrices();
            
        } catch (Exception ex ) {
            refreshPrices(); // zp�sob� zobrazen� p�vodn� spr�vn� hodnoty 
            ErrorMessages er = new ErrorMessages(Errors.NOT_EXEPT_VALUE, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    /**
     *  Nastav� datum 
     */
    private boolean setDate() {
        java.util.Date date = dateChooser.getDate();
        calendar.setTime(date);
        doSale.setDate(calendar);

        return true; 
    }
    
    /**
     *  Nastav� nov� mno�stv� 
     */
    private void setQuantity() {
        int row = goodsTable.getSelectedRow(); // Vybran� ��dek 

        TableModel model = goodsTableModel;

        if ( row >= model.getRowCount() || row < 0)
            return;

        BigDecimal quantity = new BigDecimal( String.valueOf(model.getValueAt(row, Columns.QUANTITY.getColumnNumber()) ) ); // zjisti mno�stv�
        String goodsId = String.valueOf( model.getValueAt(row, Columns.ID.getColumnNumber()) );

        BigDecimal availableQuantity = new BigDecimal(doSale.getAvailableQuantity(goodsId)).add( new BigDecimal(doSale.getTradeItem(row).getQuantity()) );
        availableQuantity = availableQuantity.setScale(Settings.QUANTITY_ROUND, RoundingMode.HALF_UP);
        
        // Nechceme prodat v�ce ne� je na sklad�
        if (quantity.subtract(availableQuantity).signum() == 1 ) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_ENOUGHT_QUANTITY, 
                    "Maxim�ln� pou�iteln� mno�stv� je: <b>" + Settings.getFloatFormat().format(availableQuantity) + "</b>.<br>" +
                    "Opravte pros�m zad�n�.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            
            quantity = availableQuantity;
            goodsTableModel.setValueAt(quantity, row, Columns.QUANTITY.getColumnNumber());
        }        
        
        doSale.setQuantity(doSale.getTradeItem(row), quantity.doubleValue()); // Nastav mno�stv�

        refreshPrices();
    }
    
    /**
     *  Zru�� vytv��enou v�dejku
     */
    private void cancel() {
        String text = "Opravdu chcete zru�it pr�v� vytv��enou v�dejku?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                DoSaleDialog.this,
                text,
                "Storno v�dejky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }
        
        try {
           doSale.update(); // zm�ny potvrzujeme i p�i zru�en�, pouze se nezap�e nov� v�dejka
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
     *  Otev�e sklad a vyhled� skladov� karty, kter� dopln� do p��jemky
     */
    private void openStoreDialog(String s) throws SQLException, InvalidPrivilegException {
        ArrayList<Goods> items = StoreDialog.openDialog(this, user, s, false);
        
        Store store = user.openStore();
        for (Goods i: items) {
            //addGoods(i);
            // Je t�eba zbo�� znovu vybrat z datab�ze, aby ho bylo mo�no uzamknout
            addGoods( store.getGoodsByID(i.getGoodsID(), true));
        }
    }    
    
    /**
     *  Zonraz� dialog, kde se zad� zp�sob platby
     * @return true jestli�e byl dialog potvrzen. Jinak false
     */
    private boolean selectPayDialog() {
            boolean result = true;
            
            ConfirmBusinessDialog cd = ConfirmBusinessDialog.openDiaog(this);
            
            if (cd == null) {
                return false;
            }
            
            // Podle toho co u�ivatel vybral
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
     *  Provede kontrolu ztr�tov�ch prodej� (t�ch kde PC < NC) a zobraz� chybov� dialog.
     *  U�ivatel bude vy��dan potvrdil, nebo zru�it prodej
     * @return true - jestli�e si u�ivatel p�eje pokra�ovat prodejem,
     * false - jestli�e chce zru�it potvrzen� a opravit zad�n�
     */
    private boolean  checkLossPriceItems() {
        ArrayList<TradeItem> items = doSale.getLossPriceItems();
        
        if (items.isEmpty()) {
            return true;
        }
        
        String[] option = {"Opravit", "Neopravovat"};
            
        StringBuffer text = new StringBuffer();
        text.append("<html>N�sleduj�c� polo�ky jsou prod�v�ny za cenu n힚� ne� n�kupn�:<br><br>");
        
        // Vytvo� �et�zec ztr�tov�ch polo�ek
        for (TradeItem i: items) {
            text.append("<b>" + i.getGoodsId() + " - " + i.getName() + "</b><br>");
        }    
        text.append("</center></html>");
            
        int i = JOptionPane.showOptionDialog(
                             DoSaleDialog.this,
                            text,
                            "Ztr�tov� prodej",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            option,
                            null);            

        // Zjisti, jestli u�ivatel stisknul OK.
        if (i == 0) {
            return false;
        }        
        
        return true;
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
                                 DoSaleDialog.this,
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
                // z�skej odkaz na sklad
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
                    // Nalezen pr�v� jednu polo�ku ve skladu. Vlo� j� 
                    if (items.size() == 1) {
                        // Vlo� polo�ku do p��jemky 
                        // Je t�eba znovu na��st z datab�ze kv�li uzamknut�
                        addGoods( store.getGoodsByID(items.get(0).getGoodsID(), true) ); 
                        return;
                    }
                    // Nenalezl ��dn� zbo�� ve skladu. Informuj o chybn�m zad�n�
                    if (items.size() == 0) {
                        ErrorMessages er = new ErrorMessages(Errors.NO_GOODS_FOUND, "Opravte zad�n�, nebo vyhledejte zbo�� ve skladu");
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
     *  Poslucha� stisku tla��tka Vyhled�n� zbo��  
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
     *  Poslucha� stisku tla��tka V�b�r odb�ratele
     */
    private class SuplierButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            Customer customer = null;
            customer = CustomerDialog.openDialog(DoSaleDialog.this, user);
            
            // Jestli�e byl vytvo�en 
            if (customer != null) {
                customersComboBox.addItem(customer);
                setCustomer(customer);
            }
        }
    }
    
   /**
     *  Poslucha� stisku zm�ny V�b�r odb�ratele  
     */
    private class ChangeCustomerListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            setCustomer( (Customer) customersComboBox.getSelectedItem());
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
                    doSale.deleteTradeItem(doSale.getTradeItem(i)); // Vyma� z p��jemky
                    indexes.add(0, i);  // Ulo�, kter� index se bude vymaz�vat, na za��tek seznamu 
                }
            }

            // ��dky tabuky vyma�eme a� nakonec, nebo� p�i postupn�m vymaz�v�n� by se 
            // ztratila informace, kter� ��dky jsou ozna�en�.
            // Vymaz�v� se od zadu tabulky, aby z�staly platn� zji�t�n� indexi
            for (Integer i: indexes) {
                    goodsTableModel.deleteRow(i); // Vyma� z tabulky
                    doSale.clearRowNumber(i);
            }

            refreshPrices();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Potvrzen� v�dejky
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            TradeItemPreview tr = null;
            try {
                if (!setDate())
                    return; // nastav datum
                
                setCustomer( (Customer) customersComboBox.getSelectedItem()); // Nastav dodavatele
                
                doSale.check(); //Zkontroluj, zda je mo�no potvrdit
                
                if (!checkLossPriceItems()) {
                    return;
                }
                
                // U�ivatel zad� zp�sob platby
                if (!selectPayDialog()) {
                    return; // Jestli�e si u�ivatel rozmyslel potvrzen�
                }
                
                doSale.storno(); // Jestli byla star� v�dejka, stornuj ji
                tr = doSale.confirm(); // potvrd proveden� v�dejky
                doSale.update(); // Potvr� v�e
                
                MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazen� skladu v hlavn�m panelu 
                MainWindow.getInstance().getSalePanel().refresh(); // Obnov zobrazen� p��jemek v hlavn�m panelu
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
                // Jestli�e se m� tisknout
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
            
            DoSaleDialog.this.dispose(); // uzav�i okno
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Zru�en� v�dejky
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
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // z�skej model v�b�ru
           /* Zjisti index na�ten�ho ��dku */
           int selectedRow;
           if ( (selectedRow = lsm.getMinSelectionIndex()) == -1) {
               priceTable.setVisible(false);
                return;
           }

           TradeItem tradeItem = doSale.getTradeItem(selectedRow);
           // ozna� cenu, kter� je vybr�na
           // Nastav za�krt�v�tka
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
     *  Poslucha� zm�ny v tabulce cen
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
            
            // jestli�e se zm�nila cena bez DPH, nastav novou cenu do v�dejky
            if (col == PriceTableColumns.PRICE.getNumber()) {
                if ( (goodsRow = goodsTable.getSelectedRow()) != -1 ) {
                    TradeItem tradeItem = doSale.getTradeItem(goodsRow);
                    int nc = new BigDecimal( String.valueOf(model.getValueAt(0, col)) ).multiply(Store.CENT).intValue();
                    int pcA = new BigDecimal( String.valueOf(model.getValueAt(1, col)) ).multiply(Store.CENT).intValue();
                    int pcB = new BigDecimal( String.valueOf(model.getValueAt(2, col)) ).multiply(Store.CENT).intValue();
                    int pcC = new BigDecimal( String.valueOf(model.getValueAt(3, col)) ).multiply(Store.CENT).intValue();
                    int pcD = new BigDecimal( String.valueOf(model.getValueAt(4, col)) ).multiply(Store.CENT).intValue();
                    // Znovunastav v�echny ceny
                    doSale.setNewPrices(tradeItem, nc, pcA, pcB, pcC, pcD, row);
                    refreshPrices();
                }
            }

            // Zm�nu jinde neeviduj
            if (col != PriceTableColumns.USE_PRICE.getNumber())
                return;
            
            Boolean checked = (Boolean) model.getValueAt(row, col);
            
            // Zabr�n� detekci zm�ny p�i nastaven� hodnoty
            model.removeTableModelListener(this);
            TradeItem tradeItem = doSale.getTradeItem(goodsRow);

            // pokud se m�nila hodnota na true
            if (checked) {
                // nastav novou cenu
                tradeItem = doSale.setNewUsePrice(tradeItem, row);
            }

            // ostatn� ��dky nastav na false
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(Boolean.valueOf(i == tradeItem.getUsePrice()), i, col);
            }

            refreshPrices();
            
            // Obnov listener
            model.addTableModelListener(this);
            

          
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