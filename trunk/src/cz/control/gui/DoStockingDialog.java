/*
 * DoStockingDialog.java
 *
 * Vytvo�eno 2. b�ezen 2006, 14:49
 *
 * Autor: Kamil Je�ek
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
 * T��da vytv��� dialog pro tvorbu B�n� inventury
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class DoStockingDialog extends JDialog implements WindowListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private DoStocking doStocking; // t��da pro pr�ci s inveturami
    private Store store;
    
    private ArrayList<Goods> goodsItem = new ArrayList<Goods>(); //polo�ky inventury
    
    // ob�kty horn� ��sti dialogu
    private JCalendar calendarDialog = new JCalendar();
    private JDateChooser dateChooser = new JDateChooser(calendarDialog);
    
    // hlavn� ��st dialogu
    private EditableGoodsTableModel goodsTableModel;
    private JTable goodsTable;   // tabulka zbo�� v inventu�e
    private JTable priceTable;
    
    private Calendar calendar = new GregorianCalendar(); // kalend��
    private SelectablePriceTableModel priceTableModel;
    
    private JButton findButton;
    private JButton newButton;
    private JButton deleteButton;
    private JLabel statusBarTip;
    
    // plo�ky ok�nka sou�tu cen
    private JLabel priceLabel; // cena zbo��
    private JComboBox usePriceCB; // kter� cena se m� pou��t pro sou�et
    private JLabel totalPriceLabel; // celkov� cena
    private JCheckBox lockCB; // z�mek
    private JCheckBox cleanGoods;
    private JTextField textTF; // textov� pozn�mka
    private JCheckBox printTF; //tisk dokladu
    
    private User user;
    private boolean creatingNewGoods = false; 
    
    private static DecimalFormat df =  Settings.getPriceFormat();;
    
    private Component owner = null;
    
    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("cs", "CZ"));
    
    /**
     * Vytvo�� nov� objekt DoStockingDialog
     * 
     * @param user u�ivatel pro kter�ho byl dialog otev�en
     * @param owner Vlastn�k dialogu
     */
    public DoStockingDialog(Frame owner, User user)  {
        this(owner, user, false);
    }
    
    /**
     * Vytvo�� nov� objekt DoStockingDialog
     * 
     * @param user u�ivatel pro kter�ho byl dialog otev�en
     * @param owner Vlastn�k dialogu
     */
    public DoStockingDialog(Dialog owner, User user)  {
        this(owner, user, false);
    }    
    
    /**
     * Vytvo�� nov� objekt DoStockingDialog
     * 
     * @param user u�ivatel pro kter�ho byl dialog otev�en
     * @param creatingNewGoods ��k�, zda se m� dialog otev��t pro zav�d�n� nov�ho zbo�� 
     * @param owner Vlastn�k dialogu
     */
    public DoStockingDialog(Frame owner, User user, boolean creatingNewGoods)  {
        super(owner, "Control - Inventura", true);
        this.owner = owner;
        this.user = user;
        this.creatingNewGoods = creatingNewGoods;
        
        try {
            doStocking = user.openDoStocking(); // Otev�i v�dejky
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
     * Vytvo�� nov� objekt DoStockingDialog
     * 
     * @param user u�ivatel pro kter�ho byl dialog otev�en
     * @param creatingNewGoods ��k�, zda se m� dialog otev��t pro zav�d�n� nov�ho zbo��
     * @param owner Vlastn�k dialogu
     */
    public DoStockingDialog(Dialog owner, User user, boolean creatingNewGoods)  {
        super(owner, "Control - Inventura", true);
        this.owner = owner;
        this.user = user;
        this.creatingNewGoods = creatingNewGoods;
        
        try {
            doStocking = user.openDoStocking(); // Otev�i v�dejky
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
     * Vytvo�� nov� objekt DoStockingDialog, kter� p�edvypln� hodnotami inventury.
     * 
     * @param user u�ivatel pro kter�ho byl dialog otev�en
     * @param owner Vlastn�k dialogu
     * @param stockingPreview P�ehled inventury, kterou se m� p�edvyplnit dialog
     */
    public DoStockingDialog(Frame owner, User user, StockingPreview stockingPreview)  {
        super(owner, "Control - Inventura", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doStocking = user.openDoStocking(stockingPreview); // Otev�i v�dejky
            store = user.openStore();
            // Nastav jednotliv� polo�ky inventury
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
  
        // Vypl� formul�� podle p�ehledu v�dejky
        setDialog();
        
    }
    
    /**
     * Vytvo�� nov� objekt DoStockingDialog, kter� p�edvypln� hodnotami inventury.
     * 
     * @param user u�ivatel pro kter�ho byl dialog otev�en
     * @param owner Vlastn�k dialogu
     * @param stockingPreview P�ehled inventury, kterou se m� p�edvyplnit dialog
     */
    public DoStockingDialog(Dialog owner, User user, StockingPreview stockingPreview)  {
        super(owner, "control- Inventura", true);
        this.owner = owner;
        this.user = user;
        
        try {
            doStocking = user.openDoStocking(stockingPreview); // Otev�i iventury
            store = user.openStore();
            // Nastav jednotliv� polo�ky p��jemky
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
        
        // Po��te�n� hodnoty
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
        content.add(setComponent(createStatusBar(), 0, 4, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER));

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
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Z�kladn� �daje"));
        
        JLabel label = new JLabel("Datum: ");
        label.setPreferredSize( new Dimension(100, 10) ); 
        label.setHorizontalAlignment(JLabel.RIGHT);
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));

        calendarDialog.setDecorationBackgroundVisible(false);
        calendarDialog.setCalendar( doStocking.getDate() );
        content.add(setComponent(dateChooser, 1, 0, 1, 1, 1.0, 0.0, NONE, WEST));

        // Nastav n�pis podle toho, zda se jedn� o b�nou inventuru,
        // nebo zav�d�c�
        if (creatingNewGoods) {
            label = new JLabel("Zaveden� nov�ho zbo�� na sklad");
            doStocking.setText("Zav�d�c� inventura");
        } else {
            label = new JLabel("Inventura skladov�ch z�sob");
        }
        
        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        label.setFont(font);
        content.add(setComponent(label, 2, 0, 1, 1, 1.0, 0.0, NONE, CENTER));

        return content;
    }

    /**
     * Vytvo�� panel s tabulkou, kter� zobrazuje polo�ky inventury
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ky inventury"));
        
        goodsTableModel = new EditableGoodsTableModel(goodsItem);
        goodsTableModel.addTableModelListener( new GoodsTableListener() );
        goodsTable = new CommonTable(goodsTableModel); // vytvo�en� tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        goodsTable.addKeyListener( new StockingKeyListener() );
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
        priceTableModel = new SelectablePriceTableModel(0, 0, 0, 0, 0, 0, doStocking.getUsePrice() );
        priceTableModel.banEditing();
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
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Souhrn"));
        content.setMinimumSize( new Dimension(200, 170));
        content.setLayout(gbl);

        Font font;
        JLabel label;

        label = new JLabel("  Textov� pozn�mka: ");
        content.add( setComponent(label, 0, 0, 3, 1, 1.0, 0.0, NONE, WEST) );
        
        textTF = new JTextField();
        textTF.setText("B�n� inventura");
        content.add( setComponent(textTF, 0, 1, 3, 1, 0.0, 0.0, HORIZONTAL, CENTER) );
        
        font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());
        label = new JLabel(" Rozd�l bez DPH: ");
        priceLabel = new JLabel(df.format( doStocking.getTotalDiferPrice() ) );
        priceLabel.setFont(font);
        JLabel kcLabel = new JLabel(" K� ");
        content.add( setComponent(label, 0, 2, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(priceLabel, 1, 2, 1, 1, 1.0, 1.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 2, 1, 1, 0.0, 0.0, NONE, WEST) );

        cleanGoods = new JCheckBox("Smazat nepou��van� karty zbo��", true);
        content.add( setComponent(cleanGoods, 1, 3, 3, 1, 1.0, 0.0, NONE, NORTHWEST) );
        
        if (creatingNewGoods) {
            cleanGoods.setEnabled(false);
            cleanGoods.setSelected(false);
        }
        
        lockCB = new JCheckBox("Uzamknout obdob�");
        content.add( setComponent(lockCB, 1, 4, 3, 1, 1.0, 0.0, NONE, NORTHWEST) );
        
        printTF = new JCheckBox("Vytisknout doklad", true);
        content.add( setComponent(printTF, 1, 5, 3, 1, 1.0, 0.0, NONE, NORTHWEST) );
        
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

        // Pou�ij komponenty podle toho, zda se jedn� o b�nou inventuru, nebo zav�d�c�
        if (creatingNewGoods) {
            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "New16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            newButton = new JButton(" Nov� ", imageIcon);
            newButton.setToolTipText("Vytvo�� skladovou kartu pro nov� zbo��");
            newButton.addActionListener(new CreateGoodsButtonListener());
            content.add(newButton);
            
            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Edit16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            findButton = new JButton(" Upravit ", imageIcon);
            findButton.setToolTipText("Uprav� skladovou kartu");
            findButton.addActionListener(new EditGoodsButtonListener());
            content.add(findButton);
        } else {
            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "New16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            newButton = new JButton(" Nov� ", imageIcon);
            newButton.setToolTipText("Dopln� novou polo�ku inventury");
            newButton.addActionListener(new creatingNewGoodsButtonListener());
            content.add(newButton);

            iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
            imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
            findButton = new JButton(" Sklad ", imageIcon);
            findButton.setToolTipText("Vyhled� zbo�� ze skladu");
            findButton.addActionListener(new FindGoodsButtonListener());
            content.add(findButton);
        }
        
        iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Vyma�e ozna�enou polo�ku inventury");
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
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� inventury"));
        
        // Tla��tko zru�en� 
        iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zru�it", imageIcon);
        button.setToolTipText("Zru�� bez ulo�en� vytv��enou inventuru");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        // tla��tko potvrzen� 
        iconURL = DoStockingDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Potvrdit", imageIcon);
        button.setToolTipText("Potvrd� inventuru a aktualizuje stavy zbo�� na sklad�");
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
     *  Projde v�echny polo�ky inventury a vlo�� zbo�� do nov� v�dejky
     *  To je pot�eba p�i editaci star� invetury, kdy se takto napln� 
     *  tabulka zbo��
     */
    private void setGoodsItems() throws InvalidPrivilegException, SQLException {
        TreeSet<Goods> goodsItems = doStocking.getAllGoodsItems();

        Store store = user.openStore();
        for (Goods i: goodsItems) {
            // Vlo� ne zbo�� z p��jemky, ale aktu�ln� ze skladu, kter� ma nastaveno 
            // aktu�ln� mno�stv�
            goodsItem.add( store.getGoodsByID(i.getGoodsID()));
        }
    }
    
    /**
     *  Vrac� zbo�� podle skladov�ho ��sla, kter� je ulo�eno v inventu�e
     *  Jestli�e takov� zbo�� v inventu�e nen�, vr�t� pr�zdn� objekt
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
     * Dopln� zbo�� do inventury - vytvo�� dal�� ��dek 
     * 
     * @param goods zbo��, kter� se m� doplnit 
     * @return True, jestli�e bylo vlo�en� �sp�n�, jinak false
     */
    public boolean addGoods(Goods goods) {

        // Jestli�e se poda�ilo vlo�it zbo�� 
        if (doStocking.addStockingItem(goods) != null) {
            int row = goodsTableModel.inserRow(goods); // Dopl� zbo��
            
            // Ozna� ��dek na kter� bylo vlo�eno
            goodsTable.setRowSelectionInterval(row, row);
            refreshPrices();
        } else {
            ErrorMessages er = new ErrorMessages(Errors.DUPLICIT_VALUE, "\"<b>" + goods + "\"</b> nen� mo�n� znovu vlo�it");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return false;
        }
        
        return true;
    }
    
    /**
     * Znovuzobraz� panel s cenami
     */
    private void refreshPrices() {
        // znovu zobraz  ceny
        BigDecimal value = doStocking.getTotalDiferPrice();
        priceLabel.setText( df.format(value) );
        
    } 
    
   
    /**
     *  Nastav� datum 
     */
    private boolean setDate() {
        java.util.Date date = dateChooser.getDate();
        calendar.setTime(date);
        doStocking.setDate(calendar);

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

        double quantity = Double.parseDouble( String.valueOf(model.getValueAt(row, Columns.QUANTITY.getColumnNumber()) ) ); // zjisti mno�stv�
        String goodsId = String.valueOf( model.getValueAt(row, Columns.ID.getColumnNumber()) );

        
        doStocking.setQuantity(goodsId, quantity); // Nastav mno�stv�

        refreshPrices();
    }
    
    /**
     *  Zru�� vytv��enou inventuru
     */
    private void cancel() {
        String text = "Opravdu chcete p�eru�it inventuru?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                DoStockingDialog.this,
                text,
                "Storno",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }
            
         try {
             
           if (creatingNewGoods) {
               // P�i zav�d�c� inventu�e provedeme rollback, nebo� chceme zru�it 
               // vytvo�en� zbo��
               doStocking.cancel();
           } else { 
               // zm�ny potvrzujeme i p�i zru�en�, pouze se nezap�e nov� inventura.
               // ale budou potvrzeny p��padn� zm�ny ve skladu
               // proto�e nevol�me confirm()
               doStocking.update(); 
           }
           
           
           // V p��pad� zru�en� zav�d�c� inventury vyma� doposavad vytvo�en� karty
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
        
        MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazen� skladu v hlavn�m panelu 
        
        this.dispose();
        
    }
    
    /**
     *  Otev�e sklad a vyhled� skladov� karty, kter� dopln� do inventury
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
     *  P�i zav�d�c� inventu�e ma�e zbo�� ze skladu
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
     *  Poslucha� stisku tla��tka Vytvo�en� zbo��
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
     *  Poslucha� stisku tla��tka Editaci zbo��
     */
    private class EditGoodsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            int firstRow = -1;
            
            ListSelectionModel listSM = goodsTable.getSelectionModel(); /* Zjisti onza�en� ��dky */
            if ( (firstRow = listSM.getMinSelectionIndex()) == -1 ) {
                JOptionPane.showMessageDialog(DoStockingDialog.this, "<html><center>Nejprve ozna�te zbo��, kter� chcete upravit.</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
                return; // jestli�e nebylo nic vybr�no, konec
            }

            Goods oldGogds =  goodsTableModel.getGoodsAt(firstRow);
            Goods goods = EditGoodsDialog.openDialog(DoStockingDialog.this, user, oldGogds);
            
            if (goods == null)
                return;

            // Nahra� zbo��
            doStocking.replaceStockingItem(oldGogds, goods);
            
            // Uchovej si mno�stv� zbo��
            int quantity = Integer.valueOf(
                        String.valueOf( 
                        goodsTableModel.getValueAt(firstRow, Columns.QUANTITY.getColumnNumber()) 
                        )
                    ).intValue();
            //Nastav nov� hodnoty do ��dku
            goodsTableModel.replaceRow(oldGogds, goods);
            
            //ulo� star� mno�stv� do tabulky. 
            // zm�na v tabulce zp�sob� vyvol�n� metody setQuantity(), kter� za��d� pot�ebn� nastaven�
            // mno�stv�
            goodsTable.setValueAt(quantity, firstRow, Columns.QUANTITY.getColumnNumber());
            
        }
    }
    
    /**
     *  Poslucha� stisku tla��tka dopln�n� zbo��
     */
    private class creatingNewGoodsButtonListener implements ActionListener {
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
                                 DoStockingDialog.this,
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
     *  Poslucha� stisku tla��tka Vyhled�n� zbo��  
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
                    
                    // Vyhledej p��slu�n� zbo��
                    Goods goods = getGoods(id);

                    doStocking.deleteStockingItem(goods); // Vyma� z inventury
                    indexes.add(0, i);  // Ulo�, kter� index se bude vymaz�vat, na za��tek seznamu 
                    
                    // Jestli�e se jedn� o zav�d�c� inventuru. Smazan� zbo�� je t�eba vymazat
                    // i ze skladu
                    if (creatingNewGoods)
                        deleteGoodsFromStore(goods);
                }
            }

            // ��dky tabuky vyma�eme a� nakonec, nebo� p�i postupn�m vymaz�v�n� by se 
            // ztratila informace, kter� ��dky jsou ozna�en�.
            // Vymaz�v� se od zadu tabulky, aby z�staly platn� zji�t�n� indexi
            for (Integer i: indexes) {
                    goodsTableModel.deleteRow(i); // Vyma� z tabulky
            }
            
            refreshPrices();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka Potvrzen� v�dejky
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            StockingPreview sp = null;
            try {

                if (!setDate())
                    return; // nastav datum
                
                
                doStocking.setLock(lockCB.isSelected()); // Nastav z�mek
                doStocking.setText(textTF.getText().trim()); // Textov� pozn�mka

                doStocking.storno(); // Jestli byla star� inventura, stornuj ji
                sp = doStocking.confirm(); // potvrd proveden� inventury
                doStocking.update(); // Potvr� v�e
                
                MainWindow.getInstance().getStorePanel().refresh(); // Obnov zobrazen� skladu v hlavn�m panelu 
                MainWindow.getInstance().getStockingsPanel().refresh(); // Obnov zobrazen� inventur v hlavn�m panelu
                
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
                // Jestli�e se m� tisknout
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

            DoStockingDialog.this.dispose(); // uzav�i okno
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
           
            priceTable.setVisible(true);
           
           Object value = goodsTableModel.getValueAt(selectedRow, Columns.ID.getColumnNumber()); // vyber hodnotu v prvn�m sloupci na p��slu�n�m ��dku

           try {
               Store store = user.openStore();
                Goods goods = store.getGoodsByID( (String) value); // Na�ti zbo�� ze skladu podle skladov�ho ��sla
                /* Nastav pot�ebn� hodnoty */
                priceTableModel.setData(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph()); 
                /* Nastav text ve panelu skladov� karta */
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
     *  Poslucha� zm�ny v tabulce cen
     */
    private class PriceTableListener implements TableModelListener {
        private int prewRow = -1;
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();

            // Zm�nu jinde neeviduj
            if (col != PriceTableColumns.USE_PRICE.getNumber())
                return;
            
            
            TableModel model = (TableModel) e.getSource();
            // Zabr�n� detekci zm�ny p�i nastaven� hodnoty
            model.removeTableModelListener(this);

            // Nastav za�krt�v�tka
            for (int i = 0; i < model.getRowCount(); i++) {
                if (i == row) {
                    model.setValueAt(true, i, PriceTableColumns.USE_PRICE.getNumber());
                } else {
                    model.setValueAt(false, i, PriceTableColumns.USE_PRICE.getNumber());
                }
            }
            // Obnov listener
            model.addTableModelListener(this);

            // Zp�dob�, �e p�i zm�n� na stejn�m ��dku se nic ned�je
            if (row == prewRow) 
                return;
            
            // Uchovej si posledn� hodnotu
            prewRow = row;
            
            // Nastav cenu podle toho, kde naposledy nastala zm�na
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
     * Poslucha� zm�ny v�b�ru slevy/p�ir�ky
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
