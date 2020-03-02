/*
 * StockingsPanel.java
 *
 * Vytvo�eno 2. b�ezen 2006, 16:00
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� panel s p�ehledem inventur
 *
 * (C) 2005, ver. 1.0
 */
public class StockingsPanel extends JPanel {
    
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // p�ihl�en� u�ivatel 
    private Stockings stockings = null; // Ukazatel na inventury
    private Component owner = null;
    
    private StockingPreview lastStockingPreview = null; // Naposledy vybran� p�ehled
    
    private JList list = new JList(); // JList s p�ehledem v�ech inventur
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastaven� seznamu */ 
    private JTable goodsTable; // Tabulka s jednotliv�mi polo�kami inventury
    private StockingTableModel goodsTableModel; // model tabulky 
    
    // panel se filtrem zobrazen�
    private JDateChooser startDate;
    private JDateChooser endDate;
    private JSpinner limitSpinner = new JSpinner(
            new SpinnerNumberModel(Settings.LIMIT, 0, Integer.MAX_VALUE, 100));

    private JButton editButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton printButton;
    
    // plo�ky ok�nka sou�tu cen
    private JLabel totalDiferLabel; // celkov� cena
    private JCheckBox lockCB; // z�mek
    private JLabel textLabel; // textov� pozn�mka
    private JLabel priceNameLabel; //n�zev ceny
    

    private static DecimalFormat df =  Settings.getPriceFormat();;
    
    /** Vytvo�� nov� objekt StockingsPanel */
    public StockingsPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        try {
            stockings = user.openStockings(); /* Otev�i inventury pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.STOCKING.getIndex(), false);
            return;

        }
        
        this.setLayout( new BorderLayout() );
        
        this.add(createFiltrPanel(), BorderLayout.NORTH); 
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
    
    /**
     *  Panel s filtrem zobrazen�
     */
    private JPanel createFiltrPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Filtr zobrazen�"));
        //content.setPreferredSize( new Dimension(100, 45));
        
        JCalendar calendar = new JCalendar();
        Calendar date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMinimum(Calendar.DAY_OF_MONTH)); //Za��tek m�s�ce
        date.set(Calendar.MONTH, date.getActualMinimum(Calendar.MONTH)); //Za��tek m�s�ce
        startDate = new JDateChooser(calendar);
        startDate.setDate(date.getTime());
        
        calendar = new JCalendar();
        date = new GregorianCalendar();
        date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec m�s�ce
        date.set(Calendar.MONTH, date.getActualMaximum(Calendar.MONTH)); //Konec m�s�ce
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
        content.add( setComponent(new JLabel("  Max. polo�ek: ") , 4, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        content.add( setComponent(limitSpinner, 5, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        content.add( setComponent(new JLabel("   "), 6, 0, 1, 1, 0.0, 0.0, NONE, WEST)); //mezera
        content.add( setComponent(confirmButton, 7, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
        
        return content;
    }
    
    /**
     * Vytvo�� panel s p�ehledem v�ech inventur
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "P�ehled inventur"));
        
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
        newButton.setToolTipText("Provede b�nou inventuru skladu");
        newButton.addActionListener( new DoStockingListener() );
        newButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(newButton);
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Edit16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        editButton = new JButton("Upravit", imageIcon);
        editButton.setToolTipText("Provede zm�nu inventury");
        editButton.addActionListener( new EditStockingListener() );
        editButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(editButton);
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Delete16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        deleteButton = new JButton("Smazat", imageIcon);
        deleteButton.setToolTipText("Trvale odstran� inventuru");
        deleteButton.addActionListener( new DelStockingsListener() );
        deleteButton.setPreferredSize( new Dimension(130, 24) );
        buttons.add(deleteButton);

        content.add(setComponent(buttons, 0, 1, 1, 1, 1.0, 0.0, BOTH, WEST));
        
        return content;
    }
    
    /**
     * Vytvo�� panel s jednotliv�mi polo�kami inventury
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Polo�ky inventury"));

        goodsTableModel = new StockingTableModel();
        goodsTable = new CommonTable(goodsTableModel); // vytvo�en� tabulky
        goodsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        goodsTable.addKeyListener( new StockingKeyListener() );
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
        JScrollPane scrollPane = new JScrollPane(goodsTable);

        content.add(setComponent(scrollPane, 0, 0, 3, 1, 1.0, 1.0, BOTH, CENTER));
        content.add(setComponent(createSumPanel(), 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
        
        /* Panel s tla��tky pro tisk a vyhled�n� zbo�� */
        JPanel buttons = new JPanel(); // panel stla��tky
        buttons.setMinimumSize( new Dimension(140, 115));
        buttons.setPreferredSize( new Dimension(140, 115));
        buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zbo��", imageIcon);
        button.setToolTipText("Vyhled� kartu zbo��");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new FindGoodsListener() );
        buttons.add(button);
        
        
        iconURL = null;//StockingsPanel.class.getResource(ICON_URL + "New16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Nov� zbo��", imageIcon);
        button.setToolTipText("Zav�d�c� inventura, zavede dal�� zbo�� na sklad");
        button.setPreferredSize( new Dimension(130, 24) );
        button.addActionListener( new NewGoodsStockingListener() );
        buttons.add(button);
        
        iconURL = StockingsPanel.class.getResource(ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        printButton = new JButton("Tisk", imageIcon);
        printButton.setToolTipText("Vytiskne doklad o proveden� inventu�e");
        printButton.setPreferredSize( new Dimension(130, 24) );
        printButton.addActionListener( new PrintListener() );
        buttons.add(printButton);

        content.add(setComponent(buttons, 2, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        
        return content;
        
    }
    
    /**
     * Panel se zobrazen�m souhrn�ch informac�
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Souhr"));
        content.setLayout(gbl);
        content.setMinimumSize( new Dimension(200, 115));
        content.setPreferredSize( new Dimension(300, 115));

        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());

        JLabel label;
        label = new JLabel(" Textov� pozn�mka ");
        textLabel = new JLabel();
        textLabel.setFont(font);
        content.add( setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(textLabel, 1, 0, 2, 1, 0.0, 0.0, NONE, WEST) );

        label = new JLabel(" Celkov� rozd�l bez DPH: ");
        totalDiferLabel = new JLabel("0,00");
        totalDiferLabel.setFont(font);
        JLabel kcLabel = new JLabel(" K� ");
        content.add( setComponent(label, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST) );
        content.add( setComponent(totalDiferLabel, 1, 1, 1, 1, 0.0, 0.0, NONE, EAST) );
        content.add( setComponent(kcLabel, 2, 1, 1, 1, 1.0, 1.0, NONE, WEST) );
        
        priceNameLabel = new JLabel("  Pro sou�et byla pou�ita cena: ");
        content.add( setComponent(priceNameLabel, 0, 2, 3, 1, 1.0, 1.0, NONE, WEST) );
        
        lockCB = new JCheckBox("Obdob� uzam�eno");
        lockCB.setEnabled(false);
        content.add( setComponent(lockCB, 0, 3, 3, 1, 0.0, 0.0, NONE, WEST) );
        
        return content;
    }    
    
    /**
     *  Vytvo�� spodn� panel s tla��tkem pro novou inventuru
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        return content;
    }
    
    /**
     *  Zv�razn� konkr�tn� inventuru
     */
    private void refresh(StockingPreview stockingPreview) {
        refresh();
        list.setSelectedValue(stockingPreview, true);
        refreshStocking(stockingPreview);
    }
    
    
    /**   
     *  Aktualizuje p�ehledy inventur
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� p�ehled inventur v datab�zi
     */
    public void refresh() {
        int row = list.getSelectedIndex();
        
        try {
            ArrayList<StockingPreview> items = stockings.getAllStocking(
                    startDate.getDate(), endDate.getDate(), (Integer) limitSpinner.getValue()); // Na�ti v�echny p�ehledy
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
     *  Aktualizuje hodnoty v tabulce v�dejek
     * @param stockingPreview p�ehled v�dejky, kter� se m� zobrazit
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
        priceNameLabel.setText("<html>Pro sou�et byla pou�ita cena: " +
                "<b>" + DoBuy.getPriceName(stockingPreview.getUsedPrice()) + "</b></html>" +
                "");
        
        BigDecimal totalDifer = new BigDecimal(stockingPreview.getDifer());
        totalDiferLabel.setText( df.format(totalDifer.divide(Store.CENT)) );
    }
    
    /**
     *  Vytvo�� novou inventuru
     */
    public void newItem() {
        new DoStockingDialog( (Frame) owner, user);
    }
    
    /**
     *  Vyma�e ozna�enou inventuru
     */
    public void deleteItem() {
        String text = "Opravdu chcete vymazat ozna�en� inventury?";
        Object[] options = {"Ano", "Ne"};
        
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te inventuru, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� inventury",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }
        
        DoStocking doStocking = null;
        /* Prove� fyzick� vymaz�n� */
        try {
            // Vyber ozna�en� polo�ky 
            Object[] items =  list.getSelectedValues();
            
            // projdi pole a vyma� 
            for (int i = 0; i < items.length; i++) {
                StockingPreview tip = (StockingPreview) items[i];
                doStocking = user.openDoStocking(tip);
                doStocking.storno(); // p�iprav smaz�n�
                doStocking.update();
            }

            refresh(); // Obnov v�b�r
            // Nastav pro zobrazen� pr�zdou v�dejku
            refreshStocking(new StockingPreview()); // Obnov pro pr�zdn� objekt
            MainWindow.getInstance().getStorePanel().refresh();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            try {
                // V p��pad� ne�sp�chu zru� v�dejku
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
        
        // jestli�e nen� nic vybr�no
        if (list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te inventuru, kterou chcete upravit.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        StockingPreview tip = (StockingPreview) list.getSelectedValue();
        
        if (tip == null)
            return;
        
        new DoStockingDialog( (Frame) owner, user, tip); 
    }
    
    /**
     *  Vyhled� zbo�� ve skladu, p�epne do z�lo�ky sklad a ozna�� nalezen� zbo��
     */
    public void findGoods() {

        int row = goodsTable.getSelectedRow(); // Na�ti prvn� ozna�en� ��dek
        
        // jestli�e nen� n�c ozna�eno
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte zbo��, kter� chcete vyhledat ze skladu.");
            JOptionPane.showMessageDialog(StockingsPanel.this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
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
     * Vytiskne inventurn� doklad
     */
    private void printItem() {
        if (lastStockingPreview == null || list.isSelectionEmpty()) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte v�dejku, kterou chcete vytisknout.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }        
        
        String text = "P�ejete si vytisknout Inventuru?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk inventury",
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
     *  Vyhled� p��slu�nou polo�ku v p��jemce 
     */
//    public void findItem() {
        
//    }
    
    /**
     *  Zm�na v list selection
     */
    private class StockingListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastStockingPreview = (StockingPreview) list.getSelectedValue();
           refreshStocking(lastStockingPreview);
        }
    }     
    
    /**
     *  Stisk tla��tka pro vytvo�en� inventury
     */
    private class DoStockingListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro smaz�n� inventury 
     */
    private class DelStockingsListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro editaci inventury
     */
    private class EditStockingListener implements ActionListener  {
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
     *  Stisk tla��tka pro vytvo�en� zav�d�c� inventury
     */
    private class NewGoodsStockingListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            new DoStockingDialog( (Frame) owner, user, true);
        }
    }    
    
    /**
     *  Stisk tla��tka pro tisk inventury
     */
    private class PrintListener implements ActionListener  {
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
