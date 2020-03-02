/*
 * DoStockingDialog.java
 *
 * Vytvoøeno 2. bøezen 2006, 14:49
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.Goods;
import cz.control.data.StockingPreview;
import cz.control.business.*;
import cz.control.gui.*;
import java.math.BigDecimal;
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
 * Tøída vytváøí dialog pro tvorbu Bìžné inventury
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class DoStockingDialog extends JDialog implements WindowListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private DoStocking doStocking; // tøída pro práci s inveturami
    private Store store;
    
    private ArrayList<Goods> goodsItem = new ArrayList<Goods>(); //položky inventury
    
    // obìkty horní èásti dialogu
    private JCalendar calendarDialog = new JCalendar();
    private JDateChooser dateChooser = new JDateChooser(calendarDialog);
    
    // hlavní èást dialogu
    private EditableGoodsTableModel goodsTableModel;
    private JTable goodsTable;   // tabulka zboží v inventuøe
    private JTable priceTable;
    
    private Calendar calendar = new GregorianCalendar(); // kalendáø
    private SelectablePriceTableModel priceTableModel;
    
    private JButton findButton;
    private JButton newButton;
    private JButton deleteButton;
    private JLabel statusBarTip;
    
    // pložky okénka souètu cen
    private JLabel priceLabel; // cena zboží
    private JComboBox usePriceCB; // která cena se má použít pro souèet
    private JLabel totalPriceLabel; // celková cena
    private JCheckBox lockCB; // zámek
    private JCheckBox cleanGoods;
    private JTextField textTF; // textová poznámka
    private JCheckBox printTF; //tisk dokladu
    
    private User user;
    private boolean creatingNewGoods = false; 
    
    private static DecimalFormat df =  Settings.getPriceFormat();;
    
    private Component owner = null;
    
    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("cs", "CZ"));
    
    /**
     * Vytvoøí nový objekt DoStockingDialog
     * 
     * @param user uživatel pro kterého byl dialog otevøen
     * @param owner Vlastník dialogu
     */
    public DoStockingDialog(Frame owner, User user)  {
        this(owner, user, false);
    }
    
    /**
     * Vytvoøí nový objekt DoStockingDialog
     * 
     * @param user uživatel pro kterého byl dialog otevøen
     * @param owner Vlastník dialogu
     */
    public DoStockingDialog(Dialog owner, User user)  {
        this(owner, user, false);
    }    
    
    /**
     * Vytvoøí nový objekt DoStockingDialog
     * 
     * @param user uživatel pro kterého byl dialog otevøen
     * @param creatingNewGoods øíká, zda se má dialog otevøít pro zavádìní nového zboží 
     * @param owner Vlastník dialogu
     */
    public DoStockingDialog(Frame owner, User user, boolean creatingNewGoods)  {
        super(owner, "Control - Inventura", true);
        this.owner = owner;
        this.user = user;
        this.creatingNewGoods = creatingNewGoods;
        
        try {
            doStocking = user.openDoStocking(); // Otevøi výdejky
            store = user.openStore();
            doStocking.forSumUsePrice(DoSale.USE_NC_FOR_SUM);
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
     * Vytvoøí nový objekt DoStockingDialog
     * 
     * @param user uživatel pro kterého byl dialog otevøen
     * @param creatingNewGoods øíká, zda se má dialog otevøít pro zavádìní nového zboží
     * @param owner Vlastník dialogu
     */
    public DoStockingDialog(Dialog owner, User user, boolean creatingNewGoods)  {
        super(owner, "Control - Inventura", true);
        this.owner = owner;
        this.user = user;
        this.creatingNewGoods = creatingNewGoods;
        
        try {
            doStocking = user.openDoStocking(); // Otevøi výdejky
            store = user.openStore();
            doStocking.forSumUsePrice(DoSale.USE_NC_FOR_SUM);
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
     * Vytvoøí nový objekt DoStockingDialog, který pøedvyplní hodnotami inventury.
     * 
     * @param user uživatel pro kterého byl dialog otevøen
     * @param owner Vlastník dialogu
     * @param stockingPreview Pøehled inventury, kterou se má pøedvyplnit dialog
     */
    public DoStockingDialog(Frame owner, User user, StockingPreview stockingPreview)  {
        super(owner, "Control - Inventura", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doStocking = user.openDoStocking(stockingPreview); // Otevøi výdejky
            store = user.openStore();
            // Nastav jednotlivé položky inventury
            setGoodsItems();
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
     * Vytvoøí nový objekt DoStockingDialog, který pøedvyplní hodnotami inventury.
     * 
     * @param user uživatel pro kterého byl dialog otevøen
     * @param owner Vlastník dialogu
     * @param stockingPreview Pøehled inventury, kterou se má pøedvyplnit dialog
     */
    public DoStockingDialog(Dialog owner, User user, StockingPreview stockingPreview)  {
        super(owner, "control- Inventura", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doStocking = user.openDoStocking(stockingPreview); // Otevøi iventury
            store = user.openStore();
            // Nastav jednotlivé položky pøíjemky
            setGoodsItems();
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
        
        // Poèáteèní hodnoty
        textTF.setText(doStocking.getText());
        lockCB.setSelected(doStocking.isLock());
        
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
        content.add(setComponent(createStatusBar(), 0, 4, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER));

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
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Základní údaje"));
        
        JLabel label = new JLabel("Datum: ");
        label.setPreferredSize( new Dimension(100, 10) ); 
        label.setHorizontalAlignment(JLabel.RIGHT);
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));

        calendarDialog.setDecorationBackgroundVisible(false);
        calendarDialog.setCalendar( doStocking.getDate() );
        content.add(setComponent(dateChooser, 1, 0, 1, 1, 1.0, 0.0, NONE, WEST));

        // Nastav nápis podle toho, zda se jedná o bìžnou inventuru,
        // nebo zavádìcí
        if (creatingNewGoods) {
            label = new JLabel("Zavedení nového zboží na sklad");
            doStocking.setText("Zavádìcí inventura");
        } else {
            label = new JLabel("Inventura skladových zásob");
        }
        
        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        label.setFont(font);
        content.add(setComponent(label, 2, 0, 1, 1, 1.0, 0.0, NONE, CENTER));

        return content;
    }

    /**
     * Vytvoøí panel s tabulkou, která zobrazuje položky inventury
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Položky inventury"));
        
        goodsTableModel = new EditableGoodsTableModel(goodsItem);
        goodsTableModel.addTableModelListener( new GoodsTableListener() );
        goodsTable = new CommonTable(goodsTableModel); // vytvoøení tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditelné vertikální linky v tabulce
        goodsTable.addKeyListener( new StockingKeyListener() );
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
        priceTableModel = new SelectablePriceTableModel(0, 0, 0, 0, 0, 0, doStocking.getUsePrice() );
        priceTableModel.banEditing();
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
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Souhrn"));
        content.setMinimumSize( new Dimension(200, 170));
        content.setLayout(gbl);

        Font font;
        JLabel label;

        label = new JLabel("  Textová poznámka: ");
        content.add( setComponent(label, 0, 0, 3, 1, 1.0, 0.0, NONE, WEST) );
        
        textTF = new JTextField();
        textTF.setText("Bìžná inventura");
        content.add( setComponent(textTF, 0, 1, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER) );
        
        font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());
        label = new JLabel(" Rozdíl bez DPH: ");
        priceLabel = new JLabel(df.format( doStocking.getTotalDiferPrice() ) );
        priceLabel.setFont(font);
        JLabel kcLabel = new JLabel(" Kè ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );

        cleanGoods = new JCheckBox("Smazat nepoužívané karty zboží", true);
        content.add( setComponent(cleanGoods, 1, 3, 3, 1, 1.0, 0.0, NONE, NORTHWEST) );
        
        if (creatingNewGoods) {
            cleanGoods.setEnabled(false);
            cleanGoods.setSelected(false);
        }
        
        lockCB = new JCheckBox("Uzamknout období");
        content.add( setComponent(lockCB, 1, 4, 3, 1, 1.0, 0.0, NONE, NORTHWEST) );
        
        printTF = new JCheckBox("Vytisknout doklad", true);
        content.add( setComponent(printTF, 1, 5, 3, 1, 1.0, 0.0, NONE, NORTHWEST) );
        
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

        // Použij komponenty podle toho, zda se jedná o bìžnou inventuru, nebo zavádìcí
        if (creatingNewGoods) {
            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "New16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            newButton = new JButton(" Nové ", imageIcon);
            newButton.setToolTipText("Vytvoøí skladovou kartu pro nové zboží");
            newButton.addActionListener(new CreateGoodsButtonListener());
            content.add(newButton);
            
            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Edit16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            findButton = new JButton(" Upravit ", imageIcon);
            findButton.setToolTipText("Upraví skladovou kartu");
            findButton.addActionListener(new EditGoodsButtonListener());
            content.add(findButton);
        } else {
            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "New16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            newButton = new JButton(" Nová ", imageIcon);
            newButton.setToolTipText("Doplní novou položku inventury");
            newButton.addActionListener(new creatingNewGoodsButtonListener());
            content.add(newButton);

            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            findButton = new JButton(" Sklad ", imageIcon);
            findButton.setToolTipText("Vyhledá zboží ze skladu");
            findButton.addActionListener(new FindGoodsButtonListener());
            content.add(findButton);
        }
        
        iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vymaže oznaèenou položku inventury");
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
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení inventury"));
        
        // Tlaøítko zrušení 
        iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zrušit", imageIcon);
        button.setToolTipText("Zruší bez uložení vytváøenou inventuru");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        // tlaèítko potvrzení 
        iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Potvrdit", imageIcon);
        button.setToolTipText("Potvrdí inventuru a aktualizuje stavy zboží na skladì");
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
     *  Projde všechny položky inventury a vloží zboží do nové výdejky
     *  To je potøeba pøi editaci staré invetury, kdy se takto naplní 
     *  tabulka zboží
     */
    private void setGoodsItems() throws InvalidPrivilegException, SQLException {
        TreeSet<Goods> goodsItems = doStocking.getAllGoodsItems();

        Store store = user.openStore();
        for (Goods i: goodsItems) {
            // Vlož ne zboží z pøíjemky, ale aktuální ze skladu, které ma nastaveno 
            // aktuální množství
            goodsItem.add( store.getGoodsByID(i.getGoodsID()));
        }
    }
    
    /**
     *  Vrací zboží podle skladového èísla, které je uloženo v inventuøe
     *  Jestliže takové zboží v inventuøe není, vrátí prázdný objekt
     */
    private Goods getGoods(String goodsID) {
        Goods goods = new Goods();
        
        for (Goods goodsItem: doStocking.getAllGoodsItems()) {
            if (goodsItem.getGoodsID().equalsIgnoreCase(goodsID)) {
                goods = goodsItem;
                break;
            }
        }
        
        return goods;
    }
    
    /**
     * Doplní zboží do inventury - vytvoøí další øádek 
     * 
     * @param goods zboží, které se má doplnit 
     * @return True, jestliže bylo vložení úspìšné, jinak false
     */
    public boolean addGoods(Goods goods) {

        // Jestliže se podaøilo vložit zboží 
        if (doStocking.addStockingItem(goods) != null) {
            int row = goodsTableModel.inserRow(goods); // Doplò zboží
            
            // Oznaè øádek na který bylo vloženo
            goodsTable.setRowSelectionInterval(row, row);
            refreshPrices();
        } else {
            ErrorMessages er = new ErrorMessages(Errors.DUPLICIT_VALUE, "\"<b>" + goods + "\"</b> není možné znovu vložit");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return false;
        }
        
        return true;
    }
    
    /**
     * Znovuzobrazí panel s cenami
     */
    private void refreshPrices() {
        // znovu zobraz  ceny
        BigDecimal value = doStocking.getTotalDiferPrice();
        priceLabel.setText( df.format(value) );
        
    } 
    
   
    /**
     *  Nastaví datum 
     */
    private boolean setDate() {
        java.util.Date date = dateChooser.getDate();
        calendar.setTime(date);
        doStocking.setDate(calendar);

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

        double quantity = Double.parseDouble( String.valueOf(model.getValueAt(row, Columns.QUANTITY.getColumnNumber()) ) ); // zjisti množství
        String goodsId = String.valueOf( model.getValueAt(row, Columns.ID.getColumnNumber()) );

        
        doStocking.setQuantity(goodsId, quantity); // Nastav množství

        refreshPrices();
    }
    
    /**
     *  Zruší vytváøenou inventuru
     */
    private void cancel() {
        String text = "Opravdu chcete pøerušit inventuru?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                DoStockingDialog.this,
                text,
                "Storno",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }
            
         try {
             
           if (creatingNewGoods) {
               // Pøi zavádìcí inventuøe provedeme rollback, nebo chceme zrušit 
               // vytvoøené zboží
               doStocking.cancel();
           } else { 
               // zmìny potvrzujeme i pøi zrušení, pouze se nezapíše nová inventura.
               // ale budou potvrzeny pøípadné zmìny ve skladu
               // protože nevoláme confirm()
               doStocking.update(); 
           }
           
           
           // V pøípadì zrušení zavádìcí inventury vymaž doposavad vytvoøené karty
           if (creatingNewGoods) {
               for (Goods i: doStocking.getAllGoodsItems()) {
                   deleteGoodsFromStore(i);
               }    
           }
        
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        
        doStocking = null;
        
        MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazení skladu v hlavním panelu 
        
        this.dispose();
        
    }
    
    /**
     *  Otevøe sklad a vyhledá skladové karty, které doplní do inventury
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
     *  Pøi zavádìcí inventuøe maže zboží ze skladu
     */
    private void deleteGoodsFromStore(Goods goods) {
        try {
            user.deleteGoods(goods);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
    }
    
    /**
     *  Posluchaè stisku tlaèítka Vytvoøení zboží
     */
    private class CreateGoodsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            Goods goods = EditGoodsDialog.openDialog(DoStockingDialog.this, user);
            
            if (goods == null)
                return;
            
            addGoods(goods);
        }
    }
    
    /**
     *  Posluchaè stisku tlaèítka Editaci zboží
     */
    private class EditGoodsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            int firstRow = -1;
            
            ListSelectionModel listSM = goodsTable.getSelectionModel(); /* Zjisti onzaèené øádky */
            if ( (firstRow = listSM.getMinSelectionIndex()) == -1 ) {
                JOptionPane.showMessageDialog(DoStockingDialog.this, "<html><center>Nejprve oznaète zboží, které chcete upravit.</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
                return; // jestliže nebylo nic vybráno, konec
            }

            Goods oldGogds =  goodsTableModel.getGoodsAt(firstRow);
            Goods goods = EditGoodsDialog.openDialog(DoStockingDialog.this, user, oldGogds);
            
            if (goods == null)
                return;

            // Nahraï zboží
            doStocking.replaceStockingItem(oldGogds, goods);
            
            // Uchovej si množství zboží
            int quantity = Integer.valueOf(
                        String.valueOf( 
                        goodsTableModel.getValueAt(firstRow, Columns.QUANTITY.getColumnNumber()) 
                        )
                    ).intValue();
            //Nastav nové hodnoty do øádku
            goodsTableModel.replaceRow(oldGogds, goods);
            
            //ulož staré množství do tabulky. 
            // zmìna v tabulce zpùsobí vyvolání metody setQuantity(), která zaøídí potøebné nastavení
            // množství
            goodsTable.setValueAt(quantity, firstRow, Columns.QUANTITY.getColumnNumber());
            
        }
    }
    
    /**
     *  Posluchaè stisku tlaèítka doplnìní zboží
     */
    private class creatingNewGoodsButtonListener implements ActionListener {
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
                                 DoStockingDialog.this,
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
                        JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                        return;
                    }
                } catch (InvalidPrivilegException exception) {
                    ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                    JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                    return;
                } catch (SQLException ex) {
                    ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                    JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
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
               JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
           } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            }
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
                    
                    // Vyhledej pøíslušné zboží
                    Goods goods = getGoods(id);

                    doStocking.deleteStockingItem(goods); // Vymaž z inventury
                    indexes.add(0, i);  // Ulož, který index se bude vymazávat, na zaèátek seznamu 
                    
                    // Jestliže se jedná o zavádìcí inventuru. Smazané zboží je tøeba vymazat
                    // i ze skladu
                    if (creatingNewGoods)
                        deleteGoodsFromStore(goods);
                }
            }

            // øádky tabuky vymažeme až nakonec, nebo pøi postupném vymazávání by se 
            // ztratila informace, které øádky jsou oznaèené.
            // Vymazává se od zadu tabulky, aby zùstaly platné zjištìné indexi
            for (Integer i: indexes) {
                    goodsTableModel.deleteRow(i); // Vymaž z tabulky
            }
            
            refreshPrices();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Potvrzení výdejky
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            StockingPreview sp = null;
            try {

                if (!setDate())
                    return; // nastav datum
                
                
                doStocking.setLock(lockCB.isSelected()); // Nastav zámek
                doStocking.setText(textTF.getText().trim()); // Textová poznámka

                doStocking.storno(); // Jestli byla stará inventura, stornuj ji
                sp = doStocking.confirm(); // potvrd provedení inventury
                doStocking.update(); // Potvrï vše
                
                MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazení skladu v hlavním panelu 
                MainWindow.getInstance().getStockingsPanel().refresh(); // Obnov zobrazení inventur v hlavním panelu
                
                if (cleanGoods.isSelected()) {
                    new GoodsCleanupDialog(DoStockingDialog.this, user, doStocking.getDate().getTime());
                }
                
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (Exception ex) {
                ErrorMessages er = new ErrorMessages(Errors.NOT_POSIBLE_CONFIRM_STOCKING, ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }
            
            try {
                // Jestliže se má tisknout
                if (printTF.isSelected()) {
                    Print.printStocking(sp);
                }        
            } catch (JRException ex) {
                ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            } catch (SQLException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }

            DoStockingDialog.this.dispose(); // uzavøi okno
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
           
            priceTable.setVisible(true);
           
           Object value = goodsTableModel.getValueAt(selectedRow, Columns.ID.getColumnNumber()); // vyber hodnotu v prvním sloupci na pøíslušném øádku

           try {
               Store store = user.openStore();
                Goods goods = store.getGoodsByID( (String) value); // Naèti zboží ze skladu podle skladového èísla
                /* Nastav potøebné hodnoty */
                priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph()); 
                /* Nastav text ve panelu skladová karta */
           } catch (InvalidPrivilegException exception) {
               ErrorMessages er = ErrorMessages.getErrorMessages(exception);
               JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
               return;
           } catch (SQLException exception) {
                ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
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

            // Zmìnu jinde neeviduj
            if (col != PriceTableColumns.USE_PRICE.getNumber())
                return;
            
            
            TableModel model = (TableModel) e.getSource();
            // Zabrání detekci zmìny pøi nastavení hodnoty
            model.removeTableModelListener(this);

            // Nastav zaškrtávátka
            for (int i = 0; i < model.getRowCount(); i++) {
                if (i == row) {
                    model.setValueAt(true, i, PriceTableColumns.USE_PRICE.getNumber());
                } else {
                    model.setValueAt(false, i, PriceTableColumns.USE_PRICE.getNumber());
                }
            }
            // Obnov listener
            model.addTableModelListener(this);

            // Zpùdobí, že pøi zmìnì na stejném øádku se nic nedìje
            if (row == prewRow) 
                return;
            
            // Uchovej si poslední hodnotu
            prewRow = row;
            
            // Nastav cenu podle toho, kde naposledy nastala zmìna
            try {
                
                switch (row) {
                    case DoBuy.USE_NC_FOR_SUM :
                        doStocking.forSumUsePrice(DoBuy.USE_NC_FOR_SUM);
                        break;
                    case DoBuy.USE_PCA_FOR_SUM :
                        doStocking.forSumUsePrice(DoBuy.USE_PCA_FOR_SUM);
                        break;
                    case DoBuy.USE_PCB_FOR_SUM :
                        doStocking.forSumUsePrice(DoBuy.USE_PCB_FOR_SUM);
                        break;
                    case DoBuy.USE_PCC_FOR_SUM :
                        doStocking.forSumUsePrice(DoBuy.USE_PCC_FOR_SUM);
                        break;
                    case DoBuy.USE_PCD_FOR_SUM :
                        doStocking.forSumUsePrice(DoBuy.USE_PCD_FOR_SUM);
                        break;
                            
                }
                
                refreshPrices();
            } catch (SQLException exception) {
                ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                JOptionPane.showMessageDialog(DoStockingDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                return;
            }
          
        }
        
    }
    
    /**
     * Posluchaè zmìny výbìru slevy/pøirážky
     */
    private class UsePriceCBItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
        }
        
    }

    private class StockingKeyListener implements KeyListener {
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
            if (creatingNewGoods) {
                statusBarTip.setText(StatusBarTips.GOODS_TIP.getText());
            } else {
                statusBarTip.setText(StatusBarTips.DO_TRADE_TIP.getText());
            }
        }

        public void focusLost(FocusEvent e) {
                statusBarTip.setText(StatusBarTips.CANCEL_CONFIRM.getText());
        }
        
    }      
          
}
