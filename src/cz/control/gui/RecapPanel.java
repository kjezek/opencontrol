/*
 * RecapPanel.java
 *
 * Vytvo�eno 9. b�ezen 2006, 14:14
 *
 * Autor: Kamil Je�ek
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
 * P�evzat� bal�k
 *
 *  � Kai Toedter 1999 - 2004
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� panel s p�ehledem rekapitulac� 
 *
 * (C) 2005, ver. 1.0
 */
public class RecapPanel extends JPanel {
    
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private User user = null; // p�ihl�en� u�ivatel 
    private Recaps recaps = null; // Ukazatel na rekapitulace
    private DoRecap doRecaps = null; // T��da pro tvorbu rekapitulac�
    private Component owner = null;
    
    private JList list = new JList(); // JList s p�ehledem v�ech inventur
    private DefaultListModel listModel  = new DefaultListModel(); /* Model pro nastaven� seznamu */ 
    private JTable monthsTable; // Tabulka s jednotliv�mi polo�kami inventury
    private MonthsTableModel monthsTableModel; // model tabulky 

    private Calendar toDay = new GregorianCalendar();
    
    private int lastYear = toDay.get(Calendar.YEAR); // Naposledy zobrazen� rok
    
    private SpinnerNumberModel spinModel = new SpinnerNumberModel(toDay.get(Calendar.YEAR),
            toDay.get(Calendar.YEAR) - 100, 
            toDay.get(Calendar.YEAR) + 100, 
            1);
    private JSpinner yearChooser = new JSpinner( spinModel );
    private JLabel yearLabel;
    private JComboBox usePriceCB;
    
    private JButton printButton;
    
    private static DecimalFormat df =  Settings.getPriceFormat();
    
    /** Vytvo�� nov� objekt RecapPanel */
    public RecapPanel(Frame owner, User user) {
        this.user = user;
        this.owner = owner;
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        try {
            recaps = user.openRecaps(); /* Otev�i rekapitulace pro p�ihl�en�ho u�ivatele */
        } catch (InvalidPrivilegException e) {
            // Ukon�i zav�d�n� okna a nastav z�lo�ku jako nep��stupnou
            MainWindow.getInstance().getTabbedPane().setEnabledAt(TabbedPaneItems.RECAP.getIndex(), false);
            return;

        }
        
        this.setLayout( gbl );
        
        this.add(setComponent(createDatePanel(), 0, 0, 2, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        this.add(setComponent(createItemsPanel(), 0, 1, 2, 1, 1.0, 0.0, HORIZONTAL, CENTER)); // Panel s jednotliv�mi m�s�ci
        this.add(setComponent(createListingPanel(), 0, 2, 1, 1, 0.0, 0.0, BOTH, CENTER)); // Panel s p�ehledem rekapitulovan�ch rok�
        this.add(setComponent(createSumPanel(), 1, 2, 1, 1, 1.0, 1.0, BOTH, CENTER)); // Panel se sou�ty
        
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
     * Vytvo�� panel s p�ehledem v�ech ro�n�ch rekapitulac�
     */
    private JPanel createListingPanel() {
        JPanel content = new JPanel(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        JButton button;
        
        Dimension dim = new Dimension(100, 10);
        content.setPreferredSize( dim );
        content.setMinimumSize( dim );
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Jednotliv� roky"));
        
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new StockingListingLSTListener());
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize( new Dimension(190, 100) );
        
        content.add(setComponent(scrollPane, 0, 0, 1, 1, 1.0, 1.0, BOTH, CENTER));

        return content;
    }
    
    /**
     * Vytvo�� panel s jednotliv�mi m�s�c� p��slu�n�ho roku
     */
    private JPanel createItemsPanel() {
        JPanel content = new JPanel(gbl);
        
        Dimension dim = new Dimension(100, 260);
        content.setPreferredSize( dim );
        content.setMinimumSize( dim );
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Jednotliv� m�s�ce"));

        monthsTableModel = new MonthsTableModel();
        monthsTable = new CommonTable(monthsTableModel); // vytvo�en� tabulky
        monthsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        monthsTable.addKeyListener( new RecapKeyListener() );
        monthsTable.addFocusListener( new ItemTableListener() );
        
        TableColumnModel columnModel = monthsTable.getColumnModel();
        /* Nastav zobrazen� slouc� */
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
     *  Vytvo�� panel s informacemi o roce a s mo�not� zm�ny
     */
    private JPanel createDatePanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        
        content.setPreferredSize( new Dimension(500, 45) );
        content.setMinimumSize( new Dimension(500, 45) );
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Rok uz�v�rky"));
        
        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        
        label = new JLabel("Rok: ");
        label.setPreferredSize( new Dimension(100, 10) ); 
        label.setHorizontalAlignment(JLabel.RIGHT);
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        
        yearChooser.addChangeListener( new ChangeYearListener() );
        yearChooser.setPreferredSize( new Dimension(65, 17) );
        content.add(setComponent(yearChooser, 1, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
        label = new JLabel("P�ehled uz�v�rek za rok: ");
        label.setFont(font);
        content.add(setComponent(label, 2, 0, 1, 1, 1.0, 0.0, NONE, EAST));
        yearLabel = new JLabel( String.valueOf(yearChooser.getValue()) );
        yearLabel.setFont(font);
        content.add(setComponent(yearLabel, 3, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
        
        return content;
    }
    
    /**
     *  Vytvo�� panel se sou�ty
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
        printButton.setToolTipText("Vytiskne doklad uz�v�rky");
        printButton.setPreferredSize( new Dimension(130, 24) );
        printButton.addActionListener( new PrintListener() );
        content.add(setComponent(printButton, 1, 0, 1, 1, 1.0, 1.0, NONE, NORTHEAST));
        
        return content;
    }
    
    /**   
     *  Aktualizuje p�ehledy rok�
     *  Tato metoda by m�la b�t vol�na, jestli�e se zm�n� n�kter� rekapitulace m�s�ce.
     *  P�ed zobrazen�m znovup�epo��t� v�echny rekapitulace
     */
    public void refresh() {
        
        try {
            recaps.computeAllRecaps(user.getUserName()); // Vypo�ti rekapitulace
            refreshYear();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
    }
    
    /**
     *  Aktualizuje hodnoty pro zm�n� roku.
     *  Rekapitulace v�ak znovu nep�epo��t�v�. Pou�ije hodnoty vypo�ten� v p�edchoz�m 
     *  vol�n� metodou <code>refresh()</code>
     */
    public void refreshYear() {
        //int row = list.getSelectedIndex();
        
        ArrayList<YearRecap> items = recaps.getYearsRacaps(); // Na�ti v�echny roky
        list.setListData( items.toArray() );

        list.setSelectedValue(lastYear, true); 

        if (lastYear != -1) {
            refreshMonths(lastYear);
        }        
    }
    
    /**
     *  Aktualizuje hodnoty v tabulce m�s�c�
     * @param year rok, pro kter� se m� zobrazit m�s��n� rekapitulace
     */
    private void refreshMonths(int year) {
        int row = monthsTable.getSelectedRow();

        // Nastav jednotliv� m�s�ce
        ArrayList<MonthRecap> items = recaps.getMonthRecaps(year);
        monthsTableModel.setData(items);

        YearRecap yearRecap = recaps.getYearRacap(year);
        monthsTableModel.setMonth(yearRecap, MonthsTableModel.YEAR_ROW); // Vlo� na ��dek s ro�n� rekapitulac�
        
        yearChooser.setValue(year);
        yearLabel.setText( String.valueOf(year) );

        if (row < monthsTableModel.getRowCount() && row >= 0)
            monthsTable.setRowSelectionInterval(row, row);
            
    }
    
    /**
     *  Provede uz�v�rku jednoho m�s�ce
     */
    private void newMonthRecap(int month) {
        Object[] options = {"Ano", "Ne"};
            
        int n = JOptionPane.showOptionDialog(
                this,
                "Opravdu chcete prov�st uz�v�rku m�s�ce?" , "Uzav��t", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]); 

        // Jestli�e u�ivatel zru�� zad�n�
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
     *  Provede rekapitulaci cel�ho roku
     */
    private void newYearRecap() {
        Object[] options = {"Ano", "Ne"};
            
        int n = JOptionPane.showOptionDialog(
                this,
                "Opravdu chcete prov�st uz�v�rku za cel� rok?" , "Uzav��t", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]); 

        // Jestli�e u�ivatel zru�� zad�n�
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
            
            // Uzav�i v�echny m�s�ce v roce
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
     *  Vytvo�� novou m�s��n� rekapitulaci
     */
    public void newItem() {
        
        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te ��dku, kterou chcete uzav��t.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        // Jestli�e byl vybr�n poslednn� ��dek tabulky
        if (row == MonthsTableModel.YEAR_ROW) {
            newYearRecap();
        } else {
            newMonthRecap(row);
        }
        

    }

    /**
     *  Vyma�e jednu m�s��n� rekapitulaci
     */
    private void deleteMonthRecap(int month) {
        String text = "Opravdu chcete vymazat uz�v�rku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� uz�v�rky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            refreshYear();
            return; // jestli�e nebyl v�b�r potvrzen - konec
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
        
        refresh(); // Obnov v�b�r
      }
    
    /**
     *  Vyma�e jednu ro�n� uz�v�rku
     */
    private void deleteYearRecap() {
        
        JOptionPane.showMessageDialog(this, 
                "Cel� rok vyma�te odzna�en�m jednotliv�ch m�s�c�" , 
                "Storno", JOptionPane.INFORMATION_MESSAGE); 
        refreshYear();
        
        /*  Rad�ji nedovol�me mazat cel� roky
         
        String text = "Opravdu chcete vymazat uz�v�rky za cel� rok?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smaz�n� uz�v�rkek",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            refreshYear();
            return; // jestli�e nebyl v�b�r potvrzen - konec
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
        
        refresh(); // Obnov v�b�r       */ 
    }
    
    /**
     *  Vyma�e ozna�enou m�si�n� rekapitulaci
     */
    public void deleteItem() {

        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve ozna�te ��dku, kterou chcete vymazat.");
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        // Jestli�e byl vybr�n poslednn� ��dek tabulky
        if (row == MonthsTableModel.YEAR_ROW) {
            deleteYearRecap();
        } else {
            deleteMonthRecap(row);
        }
 
    }
    
    /**
     *  Nastav�, kter� cena se m� zobrazit v tabulce
     */
    private void setUsePrice() {
        monthsTableModel.usePrice( usePriceCB.getSelectedIndex() );
        refreshYear();
    }
    
    /**
     *  Vytiskne ro�n� rekapitulaci
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
     *  Vytiskne m�s��n� rekapitulaci
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
     *  Vytiskne jednu polo�ku rekapitulace
     */
    private void printItem() {
        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Nejprve vyberte ��dek, kter� chcete vytisknout.");
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
            return;            
        }
        
        String text = "P�ejete si vytisknout uz�v�rku?";
        Object[] options = {"Ano", "Ne"};
        
        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk uz�v�rky",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }          
        
        if (row >= 0 && row < MonthsTableModel.YEAR_ROW) {
            printMonthRecap(row);
        }
        if (row == MonthsTableModel.YEAR_ROW) {
            printYearRecap();
        }
    }
    
    /**
     *  Zm�n� stav za�krtnut� u aktu�ln�ho ��dku
     */
    private void changeState() {
        int row = monthsTable.getSelectedRow();
        
        if (row == -1) {
            return;
        }
        
        // Na�ti hodnotu a nastav opa�nou
        boolean state = (Boolean) monthsTableModel.getValueAt(row, MonthsTableModel.CONFIRM_COLUMN);
        monthsTableModel.setValueAt(!state, row, MonthsTableModel.CONFIRM_COLUMN);
    }
    
    /**
     *  Zm�na v tabulce s rekapitulacemi
     */
    private class MonthModelListener implements TableModelListener {
        
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();

            // Zm�nu jinde neeviduj
            if (col != MonthsTableModel.CONFIRM_COLUMN)
                return;
            
            TableModel model = (TableModel) e.getSource();
            // Zabr�n� detekci zm�ny p�i nastaven� hodnoty
            model.removeTableModelListener(this);
            
            // Jestli�e se m� vytvo�it rekapitulace 
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
     *  Poslucha� v�b�ru ��dky v tabulce m�s�c� 
     */
    private class SelectRowTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting()) return;
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // z�skej model v�b�ru
           /* Zjisti index na�ten�ho ��dku */
           int selectedRow;
           if ( (selectedRow = lsm.getMinSelectionIndex()) == -1) {
                return;
           }
           
        }
    }
    
    
    /**
     *  Zm�na v editoru pro nastaven� roku
     */
    private class ChangeYearListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            lastYear = (Integer) yearChooser.getValue();
            refreshMonths( lastYear );
        }
    }
        
    /**
     *  Zm�na v list selection
     */
    private class StockingListingLSTListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {

           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting() || list.getSelectedValue() == null) 
               return;
           
           lastYear = ((YearRecap) list.getSelectedValue()).getYear();
           yearChooser.setValue(lastYear);
           refreshMonths(lastYear);
        }
    }     
    
    /**
     *  Stisk tla��tka pro vytvo�en� rekapitulace
     */
    private class DoRecapListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            newItem();
        }
    }
    
    /**
     *  Stisk tla��tka pro smaz�n� inventury 
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
     *  Stisk tla��tka pro tisk rekapitulace
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
