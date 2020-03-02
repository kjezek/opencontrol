/*
 * StockingsPanel.java
 *
 * Vytvoøeno 2. bøezen 2006, 16:00
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.Stocking;
import cz.control.data.StockingPreview;
import cz.control.business.*;
import cz.control.gui.*;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;

import java.sql.SQLException;
import java.util.*;
import java.text.*;
import java.math.BigDecimal;

import static java.awt.GridBagConstraints.*;
import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí panel s pøehledem inventur
 *
 * (C) 2005, ver. 1.0
 */
public class StockingsPanel extends JPanel {
    
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // pøihlášený uživatel 
    private Stockings stockings = null; // Ukazatel na inventury
    private Component owner = null;
    
    private StockingPreview lastStockingPreview = null; // Naposledy vybraný pøehled
    
    private JList list = new JList(); // JList s pøehledem všech inventur
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastavenÝ seznamu */ 
    private JTable goodsTable; // Tabulka s jednotlivými položkami inventury
    private StockingTableModel goodsTableModel; // model tabulky 
    
    // panel se filtrem zobrazení
    private JDateChooser startDate;
    private JDateChooser endDate;
    private JSpinner limitSpinner = new JSpinner(
            new SpinnerNumberModel(Settings.LIMIT, 0, Integer.MAX_VALUE, 100));

    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton printButton;
    
    // pložky okénka souètu cen
    private JLabel totalDiferLabel; // celková cena
    private JCheckBox lockCB; // zámek
    private JLabel textLabel; // textová poznámka
    private JLabel priceNameLabel; //název ceny
    

    private static DecimalFormat df =  Settings.getPriceFormat();;
    
    /** Vytvoøí nový objekt StockingsPanel */
    public StockingsPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        try {
            stockings = user.openStockings(); /* Otevøi inventury pro pøihlášeného uživatele */
        } catch (InvalidPrivilegException e) {
            // Ukonèi zavádìní okna a nastav záložku jako nepøístupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.STOCKING.getIndex(), false);
            return;

        }
        
        this.setLayout( new BorderLayout() );
        
        this.add(createFiltrPanel(), BorderLayout.NORTH); 
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
    
    /**
     *  Panel s filtrem zobrazení
     */
    private JPanel createFiltrPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Filtr zobrazení"));
        //content.setPreferredSize( new Dimension(100, 45));
        
        JCalendar calendar = new JCalendar();
        Calendar date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMinimum(Calendar.DAY_OF_MONTH)); //Zaèátek mìsíce
        date.set(Calendar.MONTH, date.getActualMinimum(Calendar.MONTH)); //Zaèátek mìsíce
        startDate = new JDateChooser(calendar);
        startDate.setDate(date.getTime());
        
        calendar = new JCalendar();
        date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec mìsíce
        date.set(Calendar.MONTH, date.getActualMaximum(Calendar.MONTH)); //Konec mìsíce
        endDate = new JDateChooser(calendar);
        endDate.setDate(date.getTime());
        
        limitSpinner.setPreferredSize( new Dimension(80, 20) );
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "view-refresh16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        JButton confirmButton = new JButton("Obnovit", imageIcon);
        confirmButton.addActionListener( new RefreshButtonListener() );
        
        content.add( setComponent(new JLabel("  Od: "), 0, 0, 1, 1, 0.0, 0.0, NONE, EAST) );
        content.add( setComponent(startDate, 1, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add( setComponent(new JLabel("  Do: ") , 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add( setComponent(endDate, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add( setComponent(new JLabel("  Max. položek: ") , 4, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add( setComponent(limitSpinner, 5, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add( setComponent(new JLabel("   "), 6, 0, 1, 1, 0.0, 0.0, NONE, WEST)); //mezera
        content.add( setComponent(confirmButton, 7, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
        
        return content;
    }
    
    /**
     * Vytvoøí panel s pøehledem všech inventur
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Pøehled inventur"));
        
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new StockingListingLSTListener());
        list.addKeyListener( new StockingKeyListener() );
        list.addFocusListener( new ItemTableListener() );
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 0.0, 1.0, VERTICAL, CENTER));
        
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        buttons.setMinimumSize( new Dimension(150, 115) );
        buttons.setPreferredSize( new Dimension(150, 115) );
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "NewStocking16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        newButton = new JButton("Inventura", imageIcon);
        newButton.setToolTipText("Provede bìžnou inventuru skladu");
        newButton.addActionListener( new DoStockingListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zmìnu inventury");
        editButton.addActionListener( new EditStockingListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstraní inventuru");
        deleteButton.addActionListener( new DelStockingsListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     * Vytvoøí panel s jednotlivými položkami inventury
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Položky inventury"));

        goodsTableModel = new StockingTableModel();
        goodsTable = new CommonTable(goodsTableModel); // vytvoøení tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditelné vertikální linky v tabulce
        goodsTable.addKeyListener( new StockingKeyListener() );
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
        JScrollPane scrollPane = new JScrollPane(goodsTable);

        content.add(setComponent(scrollPane, 0, 0, 3, 1, 1.0, 1.0, BOTH, CENTER));
        content.add(setComponent(createSumPanel(), 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
        
        /* Panel s tlaèítky pro tisk a vyhledání zboží */
        JPanel buttons = new JPanel(); // panel stlaøítky
        buttons.setMinimumSize( new Dimension(140, 115));
        buttons.setPreferredSize( new Dimension(140, 115));
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zboží", imageIcon);
        button.setToolTipText("Vyhledá kartu zboží");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new FindGoodsListener() );
        buttons.add(button);
        
        
        iconURL = null;//StockingsPanel.class.getResource(ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Nové zboží", imageIcon);
        button.setToolTipText("Zavádìcí inventura, zavede další zboží na sklad");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new NewGoodsStockingListener() );
        buttons.add(button);
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        printButton = new JButton("Tisk", imageIcon);
        printButton.setToolTipText("Vytiskne doklad o provedené inventuøe");
        printButton.setPreferredSize( new Dimension(130, 24) );
        printButton.addActionListener( new PrintListener() );
        buttons.add(printButton);

        content.add(setComponent(buttons, 2, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        
        return content;
        
    }
    
    /**
     * Panel se zobrazením souhrných informací
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Souhr"));
        content.setLayout(gbl);
        content.setMinimumSize( new Dimension(200, 115));
        content.setPreferredSize( new Dimension(300, 115));

        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        JLabel label;
        label = new JLabel(" Textová poznámka ");
        textLabel = new JLabel();
        textLabel.setFont(font);
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(textLabel, 1, 0, 2, 1, 0.0, 0.0, NONE, WEST) );

        label = new JLabel(" Celkový rozdíl bez DPH: ");
        totalDiferLabel = new JLabel("0,00");
        totalDiferLabel.setFont(font);
        JLabel kcLabel = new JLabel(" Kè ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(totalDiferLabel, 1, 1, 1, 1, 0.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        priceNameLabel = new JLabel("  Pro souèet byla použita cena: ");
        content.add( setComponent(priceNameLabel, 0, 2, 3, 1, 1.0, 1.0, NONE, WEST) );
        
        lockCB = new JCheckBox("Období uzamèeno");
        lockCB.setEnabled(false);
        content.add( setComponent(lockCB, 0, 3, 3, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvoøí spodní panel s tlaèítkem pro novou inventuru
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        return content;
    }
    
    /**
     *  Zvýrazní konkrétní inventuru
     */
    private void refresh(StockingPreview stockingPreview) {
        refresh();
        list.setSelectedValue(stockingPreview, true);
        refreshStocking(stockingPreview);
    }
    
    
    /**   
     *  Aktualizuje pøehledy inventur
     *  Tato metoda by mìla být volána, jestliže se zmìní pøehled inventur v databázi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<StockingPreview> items = stockings.getAllStocking(
                    startDate.getDate(), endDate.getDate(), (Integer) limitSpinner.getValue()); // Naèti všechny pøehledy
            list.setListData( items.toArray() );
            
            list.setSelectedIndex(row); 
            
            if (lastStockingPreview != null) {
                refreshStocking(lastStockingPreview);
            }
                
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Aktualizuje hodnoty v tabulce výdejek
     * @param stockingPreview pøehled výdejky, která se má zobrazit
     */
    private void refreshStocking(StockingPreview stockingPreview) {
        ArrayList<Stocking> items;
        int row = goodsTable.getSelectedRow();
        try {
            items = stockings.getAllStockingItems(stockingPreview);
            goodsTableModel.setData(items);
            
            if (row < goodsTableModel.getRowCount() && row >= 0)
                goodsTable.setRowSelectionInterval(row, row);
            
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        textLabel.setText(stockingPreview.getText());
        lockCB.setSelected(stockingPreview.isLock());
        priceNameLabel.setText("<html>Pro souèet byla použita cena: " +
                "<b>" + DoBuy.getPriceName(stockingPreview.getUsedPrice()) + "</b></html>" +
                "");
        
        BigDecimal totalDifer = new BigDecimal(stockingPreview.getDifer());
        totalDiferLabel.setText( df.format(totalDifer.divide(Store.CENT)) );
    }
    
    /**
     *  Vytvoøí novou inventuru
     */
    public void newItem() {
        new DoStockingDialog( (Frame) owner, user);
    }
    
    /**
     *  Vymaže oznaèenou inventuru
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat oznaèené inventury?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète inventuru, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání inventury",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }
        
        DoStocking doStocking = null;
        /* Proveï fyzické vymazání */
        try {
            // Vyber oznaèené položky 
            Object[] items =  list.getSelectedValues();
            
            // projdi pole a vymaž 
            for (int i = 0; i < items.length; i++) {
                StockingPreview tip = (StockingPreview) items[i];
                doStocking = user.openDoStocking(tip);
                doStocking.storno(); // pøiprav smazání
                doStocking.update();
            }

            refresh(); // Obnov výbìr
            // Nastav pro zobrazení prázdou výdejku
            refreshStocking(new StockingPreview()); // Obnov pro prázdný objekt
            MainWindow.getInstance().getStorePanel().refresh();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            try {
                // V pøípadì neúspìchu zruš výdejku
                if (doStocking != null)
                    doStocking.cancel();
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
     *  Edituje vybranou inventuru
     */
    private void editItem() {
        
        // jestliže není nic vybráno
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète inventuru, kterou chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        StockingPreview tip = (StockingPreview) list.getSelectedValue();
        
        if (tip == null)
            return;
        
        new DoStockingDialog( (Frame) owner, user, tip); 
    }
    
    /**
     *  Vyhledá zboží ve skladu, pøepne do záložky sklad a oznaèí nalezené zboží
     */
    public void findGoods() {

        int row = goodsTable.getSelectedRow(); // Naèti první oznaèený øádek
        
        // jestliže není níc oznaèeno
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte zboží, které chcete vyhledat ze skladu.");
            JOptionPane.showMessageDialog(StockingsPanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
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
     * Vytiskne inventurní doklad
     */
    private void printItem() {
        if (lastStockingPreview == null || list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte výdejku, kterou chcete vytisknout.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }        
        
        String text = "Pøejete si vytisknout Inventuru?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk inventury",
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
            Print.printStocking(lastStockingPreview);
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
     *  Vyhledá pøíslušnou položku v pøíjemce 
     */
//    public void findItem() {
        
//    }
    
    /**
     *  Zmìna v list selection
     */
    private class StockingListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // zábrání zdvojenému vyvolání uudálosti (význam mi není pøesnì znám)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastStockingPreview = (StockingPreview) list.getSelectedValue();
           refreshStocking(lastStockingPreview);
        }
    }     
    
    /**
     *  Stisk tlaèítka pro vytvoøení inventury
     */
    private class DoStockingListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro smazání inventury 
     */
    private class DelStockingsListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro editaci inventury
     */
    private class EditStockingListener implements ActionListener  {
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
     *  Stisk tlaèítka pro vytvoøení zavádìcí inventury
     */
    private class NewGoodsStockingListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            new DoStockingDialog( (Frame) owner, user, true);
        }
    }    
    
    /**
     *  Stisk tlaèítka pro tisk inventury
     */
    private class PrintListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            printItem();
        }
    }
    
    /**
     *  Posluchaè obnovení výbìru
     */
    private class RefreshButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }    
    
    private class StockingKeyListener implements KeyListener {
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
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.STOCKING_TIP);
        }

        public void focusLost(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.EMPTY);
        }
        
    }    
  
    
}
