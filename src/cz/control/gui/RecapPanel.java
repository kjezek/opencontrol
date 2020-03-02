/*
 * RecapPanel.java
 *
 * Vytvoøeno 9. bøezen 2006, 14:14
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.MonthRecap;
import cz.control.data.YearRecap;
import cz.control.business.*;
import cz.control.gui.*;

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

/*
 * Pøevzatý balík
 *
 *  © Kai Toedter 1999 - 2004
 *       Version 1.2.2
 *       09/24/04
 *  http://www.toedter.com/en/jcalendar/
 */
import com.toedter.calendar.*;

import static java.awt.GridBagConstraints.*;
import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;
import net.sf.jasperreports.engine.JRException;

/**
 * Program Control - Skladový systém
 *
 * Tøída vytváøí panel s pøehledem rekapitulací 
 *
 * (C) 2005, ver. 1.0
 */
public class RecapPanel extends JPanel {
    
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // pøihlášený uživatel 
    private Recaps recaps = null; // Ukazatel na rekapitulace
    private DoRecap doRecaps = null; // Tøída pro tvorbu rekapitulací
    private Component owner = null;
    
    private JList list = new JList(); // JList s pøehledem všech inventur
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastavenÝ seznamu */ 
    private JTable monthsTable; // Tabulka s jednotlivými položkami inventury
    private MonthsTableModel monthsTableModel; // model tabulky 

    private Calendar toDay = new GregorianCalendar();
    
    private int lastYear = toDay.get(Calendar.YEAR); // Naposledy zobrazený rok
    
    private SpinnerNumberModel spinModel = new SpinnerNumberModel(toDay.get(Calendar.YEAR),
            toDay.get(Calendar.YEAR) - 100, 
            toDay.get(Calendar.YEAR) + 100, 
            1);
    private JSpinner yearChooser = new JSpinner( spinModel );
    private JLabel yearLabel;
    private JComboBox usePriceCB;
    
    private JButton printButton;
    
    private static DecimalFormat df =  Settings.getPriceFormat();
    
    /** Vytvoøí nový objekt RecapPanel */
    public RecapPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        try {
            recaps = user.openRecaps(); /* Otevøi rekapitulace pro pøihlášeného uživatele */
        } catch (InvalidPrivilegException e) {
            // Ukonèi zavádìní okna a nastav záložku jako nepøístupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.RECAP.getIndex(), false);
            return;

        }
        
        this.setLayout( gbl );
        
        this.add(setComponent(createDatePanel(), 0, 0, 2, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        this.add(setComponent(createItemsPanel(), 0, 1, 2, 1, 1.0, 0.0, HORIZONTAL, CENTER)); // Panel s jednotlivými mìsíci
        this.add(setComponent(createListingPanel(), 0, 2, 1, 1, 0.0, 0.0, BOTH, CENTER)); // Panel s pøehledem rekapitulovaných rokù
        this.add(setComponent(createSumPanel(), 1, 2, 1, 1, 1.0, 1.0, BOTH, CENTER)); // Panel se souèty
        
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
     * Vytvoøí panel s pøehledem všech roèních rekapitulací
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        Dimension dim = new Dimension(100, 10);
        content.setPreferredSize( dim );
        content.setMinimumSize( dim );
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Jednotlivé roky"));
        
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new StockingListingLSTListener());
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 1.0, 1.0, BOTH, CENTER));

        return content;
    }
    
    /**
     * Vytvoøí panel s jednotlivými mìsící pøíslušného roku
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        Dimension dim = new Dimension(100, 260);
        content.setPreferredSize( dim );
        content.setMinimumSize( dim );
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Jednotlivé mìsíce"));

        monthsTableModel = new MonthsTableModel();
        monthsTable = new CommonTable(monthsTableModel); // vytvoøení tabulky
        monthsTable.setShowVerticalLines(false);  // Nastav neviditelné vertikální linky v tabulce
        monthsTable.addKeyListener( new RecapKeyListener() );
        monthsTable.addFocusListener( new ItemTableListener() );
        
        TableColumnModel columnModel = monthsTable.getColumnModel();
        /* Nastav zobrazení sloucù */
        columnModel.getColumn(0).setCellRenderer(new CommonItemCellRenderer());
        columnModel.getColumn(1).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(2).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(3).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(4).setCellRenderer(new PriceCellRenderer());

        JScrollPane scrollPane = new JScrollPane(monthsTable);

        content.add(setComponent(scrollPane, 0, 1, 3, 1, 1.0, 1.0, BOTH, CENTER));
        
        ListSelectionModel rowSM = monthsTable.getSelectionModel();
        rowSM.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        rowSM.addListSelectionListener(new SelectRowTableListener());
        monthsTableModel.addTableModelListener( new MonthModelListener() );
        
        return content;
        
    }
    
    /**
     *  Vytvoøí panel s informacemi o roce a s možnotí zmìny
     */
    private JPanel createDatePanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        
        content.setPreferredSize( new Dimension(500, 45) );
        content.setMinimumSize( new Dimension(500, 45) );
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Rok uzávìrky"));
        
        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        
        label = new JLabel("Rok: ");
        label.setPreferredSize( new Dimension(100, 10) ); 
        label.setHorizontalAlignment(JLabel.RIGHT);
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        
        yearChooser.addChangeListener( new ChangeYearListener() );
        yearChooser.setPreferredSize( new Dimension(65, 17) );
        content.add(setComponent(yearChooser, 1, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
        label = new JLabel("Pøehled uzávìrek za rok: ");
        label.setFont(font);
        content.add(setComponent(label, 2, 0, 1, 1, 1.0, 0.0, NONE, EAST));
        yearLabel = new JLabel( String.valueOf(yearChooser.getValue()) );
        yearLabel.setFont(font);
        content.add(setComponent(yearLabel, 3, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
        
        return content;
    }
    
    /**
     *  Vytvoøí panel se souèty
     */
    private JPanel createSumPanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        String[] items = {"Ceny bez DPH", "Ceny s DPH", "DPH"};
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Souhrn"));
        
        usePriceCB = new JComboBox(items);
        usePriceCB.addItemListener( new UsePriceCBListener() );
        content.add(setComponent(usePriceCB, 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));

        iconURL = RecapPanel.class.getResource(ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        printButton = new JButton("Tisk", imageIcon);
        printButton.setToolTipText("Vytiskne doklad uzávìrky");
        printButton.setPreferredSize( new Dimension(130, 24) );
        printButton.addActionListener( new PrintListener() );
        content.add(setComponent(printButton, 1, 0, 1, 1, 1.0, 1.0, NONE, NORTHEAST));
        
        return content;
    }
    
    /**   
     *  Aktualizuje pøehledy rokù
     *  Tato metoda by mìla být volána, jestliže se zmìní nìkterá rekapitulace mìsíce.
     *  Pøed zobrazením znovupøepoèítá všechny rekapitulace
     */
    public void refresh() {
        
        try {
            recaps.computeAllRecaps(user.getUserName()); // Vypoèti rekapitulace
            refreshYear();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Aktualizuje hodnoty pro zmìnì roku.
     *  Rekapitulace však znovu nepøepoèítává. Použije hodnoty vypoètené v pøedchozím 
     *  volání metodou <code>refresh()</code>
     */
    public void refreshYear() {
        //int row = list.getSelectedIndex();
        
        ArrayList<YearRecap> items = recaps.getYearsRacaps(); // Naèti všechny roky
        list.setListData( items.toArray() );

        list.setSelectedValue(lastYear, true); 

        if (lastYear != -1) {
            refreshMonths(lastYear);
        }        
    }
    
    /**
     *  Aktualizuje hodnoty v tabulce mìsícù
     * @param year rok, pro který se má zobrazit mìsíèní rekapitulace
     */
    private void refreshMonths(int year) {
        int row = monthsTable.getSelectedRow();

        // Nastav jednotlivé mìsíce
        ArrayList<MonthRecap> items = recaps.getMonthRecaps(year);
        monthsTableModel.setData(items);

        YearRecap yearRecap = recaps.getYearRacap(year);
        monthsTableModel.setMonth(yearRecap, MonthsTableModel.YEAR_ROW); // Vlož na øádek s roèní rekapitulací
        
        yearChooser.setValue(year);
        yearLabel.setText( String.valueOf(year) );

        if (row < monthsTableModel.getRowCount() && row >= 0)
            monthsTable.setRowSelectionInterval(row, row);
            
    }
    
    /**
     *  Provede uzávìrku jednoho mìsíce
     */
    private void newMonthRecap(int month) {
        Object[] options = {"Ano", "Ne"};
            
        int n = JOptionPane.showOptionDialog(
                this,
                "Opravdu chcete provést uzávìrku mìsíce?" , "Uzavøít", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]); 

        // Jestliže uživatel zruší zadání
        if (n != 0) {
            refreshYear();
            return;  
        }
        
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.YEAR, lastYear);
        calendar.set(Calendar.MONTH, month);
        
        try {
            DoRecap doRecap = user.openDoRecap();
            doRecap.setDate(calendar);
            doRecap.doMonthRecap();
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (Exception ex) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_POSIBLE_CONFIRM_RECAP, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        refresh();        
    }
    
    /**
     *  Provede rekapitulaci celého roku
     */
    private void newYearRecap() {
        Object[] options = {"Ano", "Ne"};
            
        int n = JOptionPane.showOptionDialog(
                this,
                "Opravdu chcete provést uzávìrku za celý rok?" , "Uzavøít", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]); 

        // Jestliže uživatel zruší zadání
        if (n != 0) {
            refreshYear();
            return;  
        }
        
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.YEAR, lastYear);
        calendar.set(Calendar.MONTH, 11);
        
        try {
            DoRecap doRecap = user.openDoRecap();
            
            // Uzavøi všechny mìsíce v roce
            for (int i = 0; i < 12; i++) {
                calendar.set(Calendar.MONTH, i);
                doRecap.setDate(calendar);
                doRecap.doMonthRecap();
            }
            
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (Exception ex) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_POSIBLE_CONFIRM_RECAP, ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        refresh();           
    }
    
    /**
     *  Vytvoøí novou mìsíèní rekapitulaci
     */
    public void newItem() {
        
        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète øádku, kterou chcete uzavøít.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        // Jestliže byl vybrán poslednní øádek tabulky
        if (row == MonthsTableModel.YEAR_ROW) {
            newYearRecap();
        } else {
            newMonthRecap(row);
        }
        

    }

    /**
     *  Vymaže jednu mìsíèní rekapitulaci
     */
    private void deleteMonthRecap(int month) {
        String text = "Opravdu chcete vymazat uzávìrku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání uzávìrky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            refreshYear();
            return; // jestliže nebyl výbìr potvrzen - konec
        }
        
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, lastYear);
        calendar.set(Calendar.MONTH, month);        
        calendar.set(Calendar.DAY_OF_MONTH, 1);        

        try {
            user.deleteMonthRecap(calendar);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        refresh(); // Obnov výbìr
      }
    
    /**
     *  Vymaže jednu roèní uzávìrku
     */
    private void deleteYearRecap() {
        
        JOptionPane.showMessageDialog(this, 
                "Celý rok vymažte odznaèením jednotlivých mìsícù" , 
                "Storno", JOptionPane.INFORMATION_MESSAGE); 
        refreshYear();
        
        /*  Radìji nedovolíme mazat celé roky
         
        String text = "Opravdu chcete vymazat uzávìrky za celý rok?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání uzávìrkek",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            refreshYear();
            return; // jestliže nebyl výbìr potvrzen - konec
        }
        
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, lastYear);

        try {
            DoRecap doRecap = user.openDoRecap();
            doRecap.setDate(calendar);
            doRecap.stornoYear();
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        refresh(); // Obnov výbìr       */ 
    }
    
    /**
     *  Vymaže oznaèenou mìsièní rekapitulaci
     */
    public void deleteItem() {

        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve oznaète øádku, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        // Jestliže byl vybrán poslednní øádek tabulky
        if (row == MonthsTableModel.YEAR_ROW) {
            deleteYearRecap();
        } else {
            deleteMonthRecap(row);
        }
 
    }
    
    /**
     *  Nastaví, která cena se má zobrazit v tabulce
     */
    private void setUsePrice() {
        monthsTableModel.usePrice( usePriceCB.getSelectedIndex() );
        refreshYear();
    }
    
    /**
     *  Vytiskne roèní rekapitulaci
     */
    private void printYearRecap() {
        YearRecap mr = recaps.getYearRacap(lastYear);
        
        // Tiskni
        try {
            Print.printRecapYear(mr);
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
     *  Vytiskne mìsíèní rekapitulaci
     */
    private void printMonthRecap(int row) {
        MonthRecap mr = recaps.getMonthRecap(lastYear, row+1);
        
        // Tiskni
        try {
            Print.printRecap(mr);
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
     *  Vytiskne jednu položku rekapitulace
     */
    private void printItem() {
        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte øádek, který chcete vytisknout.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;            
        }
        
        String text = "Pøejete si vytisknout uzávìrku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk uzávìrky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }          
        
        if (row >= 0 && row < MonthsTableModel.YEAR_ROW) {
            printMonthRecap(row);
        }
        if (row == MonthsTableModel.YEAR_ROW) {
            printYearRecap();
        }
    }
    
    /**
     *  Zmìní stav zaškrtnutí u aktuálního øádku
     */
    private void changeState() {
        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            return;
        }
        
        // Naèti hodnotu a nastav opaènou
        boolean state = (Boolean) monthsTableModel.getValueAt(row, MonthsTableModel.CONFIRM_COLUMN);
        monthsTableModel.setValueAt(!state, row, MonthsTableModel.CONFIRM_COLUMN);
    }
    
    /**
     *  Zmìna v tabulce s rekapitulacemi
     */
    private class MonthModelListener implements TableModelListener {
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();

            // Zmìnu jinde neeviduj
            if (col != MonthsTableModel.CONFIRM_COLUMN)
                return;
            
            TableModel model = (TableModel) e.getSource();
            // Zabrání detekci zmìny pøi nastavení hodnoty
            model.removeTableModelListener(this);
            
            // Jestliže se má vytvoøit rekapitulace 
            if ( (Boolean) model.getValueAt(row, col) == true) {
                newItem();
            } else {
                deleteItem(); //smazat rekapitulaci
            }

            // Obnov listener
            model.addTableModelListener(this);

        }
        
    }
    
    /**
     *  Posluchaè výbìru øádky v tabulce mìsícù 
     */
    private class SelectRowTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // zábrání zdvojenému vyvolání uudálosti (význam mi není pøesnì znám)
           if (e.getValueIsAdjusting()) return;
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // získej model výbìru
           /* Zjisti index naèteného øádku */
           int selectedRow;
           if ( (selectedRow = lsm.getMinSelectionIndex()) == -1) {
                return;
           }
           
        }
    }
    
    
    /**
     *  Zmìna v editoru pro nastavení roku
     */
    private class ChangeYearListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            lastYear = (Integer) yearChooser.getValue();
            refreshMonths( lastYear );
        }
    }
        
    /**
     *  Zmìna v list selection
     */
    private class StockingListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // zábrání zdvojenému vyvolání uudálosti (význam mi není pøesnì znám)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastYear = ((YearRecap) list.getSelectedValue()).getYear();
           yearChooser.setValue(lastYear);
           refreshMonths(lastYear);
        }
    }     
    
    /**
     *  Stisk tlaèítka pro vytvoøení rekapitulace
     */
    private class DoRecapListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tlaèítka pro smazání inventury 
     */
    private class DelRecapListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            deleteItem();
        }
    }
    
    private class UsePriceCBListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setUsePrice();
        }    
    }

    /**
     *  Stisk tlaèítka pro tisk rekapitulace
     */
    private class PrintListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            printItem();
        }
    }
    
    private class RecapKeyListener implements KeyListener {
        private boolean ctrlPress = false;
        private boolean altPress = false;
        
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SPACE :
                    changeState();
                    break;
                case KeyEvent.VK_ALT :
                    altPress = true;
                    break;
                case KeyEvent.VK_CONTROL :
                    ctrlPress = true;
                    break;
            }
            
            if (ctrlPress && e.getKeyCode() == KeyEvent.VK_P) {
                printButton.doClick();
                altPress = false;
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
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.RECAP_TIP);
        }

        public void focusLost(FocusEvent e) {
            MainWindow.getInstance().setStatusBarTip(StatusBarTips.EMPTY);
        }
        
    }    
}
