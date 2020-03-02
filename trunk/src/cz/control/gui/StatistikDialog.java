/*
 * StatistikDialog.java
 *
 * Vytvoøeno 17. prosinec 2005, 11:26
 *
 
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.ChartItem;
import cz.control.business.*;
import cz.control.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.*;
import java.net.*;

/**
 * Program Control - Skladový systém
 *
 * Zobrazí dialog ze statistikou 
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class StatistikDialog extends JDialog {
    
    private Component owner;
    private JTabbedPane tabbedPane;
    private Statistik statistik;
    private User user;
    
    private JList topBuyList = new JList();
    private JList topSupList = new JList();
    private JList topSaleList = new JList();
    private JList topCustList = new JList();
    
    private static DecimalFormat df = Settings.getPriceFormat();
       
    
    /**
     * Vytvoøí nový objekt StatistikDialog
     * @param owner Vlastník, který otevøel tento dialog
     */
    public StatistikDialog(Frame owner, User user) {
        super(owner, "Control - Statistiky", true);

        this.user= user;
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvoøí nový objekt StatistikDialog
     * @param owner Vlastník, který otevøel tento dialog
     */
    public StatistikDialog(Dialog owner, User user) {
        super(owner, "Control - Statistiky", true);

        this.user= user;
        this.owner = owner;
        setDialog();
    }
    
    
    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {
        
        try {
            statistik = user.openStatistik(); /* Otevøi sklad pro pøihlášeného uživatele */
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(StatistikDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
                
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        if (owner  != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(false);
        setPreferredSize(new Dimension(530, 570));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        setVisible(true);
    }

    /**
     * Vytvoøí obsah okna
     */
    private JComponent getContent() {
        tabbedPane = new JTabbedPane();
    
        tabbedPane.addKeyListener( new StatistikKeyListener() );
        try {
            tabbedPane.addTab("Sklad", null, createStorePanel(), "Statistika skladu");
            tabbedPane.addTab("Nákup", null, createBuyPanel(), "Nákupní statistiky");
            tabbedPane.addTab("Prodej", null, createSalePanel(), "Prodejní statistiky");
            
            setStatistik(); // Nastav statistiky
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(StatistikDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        return tabbedPane;
    }    
    
    /**
     *  Vytvoøí panel se statistikou skladu
     */
    private JPanel createStorePanel() throws SQLException {
        JPanel content = new JPanel();
        
        
        // Panel zobrazující množství skladových karet - sortiment
        JPanel sortimentPanel = new JPanel( new GridLayout(4, 2));
        sortimentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Poèet skladových karet"));
        sortimentPanel.setPreferredSize(new Dimension(500, 150)); //Minimalní velikost panelu

        JLabel label = new JLabel("Celkem: ");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getGoodsCardCount()));
        sortimentPanel.add(label);
        
        label = new JLabel("Kladné množství: ");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getPositiveCardCount()) );
        sortimentPanel.add(label);
        
        label = new JLabel("Nulové množství: ");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getZeroGoodsCardCount()) );
        sortimentPanel.add(label);
        
        content.add(sortimentPanel);
        
        label = new JLabel("<html><span style='color:red'>Záporné množství:</span></html>");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getNegativeCardCount()) );
        sortimentPanel.add(label);
        
        // Panel zobrazující statistiky cen zboží
        JPanel pricePanel = new JPanel( new GridLayout(1, 2));
        pricePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny zboží"));
        pricePanel.setPreferredSize(new Dimension(500, 45)); //preferovaná velikost panelu
        

        label = new JLabel("Hodnota skladu: ");
        pricePanel.add(label);
        double num = new BigDecimal(statistik.getStorePrice()).divide(Store.CENT).doubleValue();
        label = new JLabel( df.format(num) + " Kè" );
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());
        label.setFont(font);
        pricePanel.add(label);
        
        content.add(pricePanel);
        
        // Panel zobrazující statistiky obchodníkù
        JPanel bussynesPanel = new JPanel( new GridLayout(2, 1));
        bussynesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Obchodní partneøi"));
        bussynesPanel.setPreferredSize(new Dimension(500, 100)); //preferovaná velikost panelu
        label = new JLabel("Poèet dodavatelù: " );
        bussynesPanel.add(label);
        label = new JLabel( String.valueOf(statistik.getSuplierCount()) );
        bussynesPanel.add(label);
        label = new JLabel("Poèet odbìratelù: ");
        bussynesPanel.add(label);
        label = new JLabel( String.valueOf(statistik.getCustomerCount()) );
        bussynesPanel.add(label);
        
        content.add(bussynesPanel);
        
        return content;
    }

    /**
     *  Vytvoøí panel se statistikou pøíjemek
     */
    private Component createBuyPanel() {
        JPanel content = new JPanel();
        
        // Nejvíce nakupované zboží
        JPanel topGoodsPanel = new JPanel( new BorderLayout());
        topGoodsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Žebøíèek nakupovanosti zboží"));
        topGoodsPanel.setPreferredSize(new Dimension(500, 240)); //preferovaná velikost panelu

        topBuyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topBuyList.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(topBuyList);
        topBuyList.setToolTipText("Èíslo v závorce udává poèet nakoupených kusù");
        topGoodsPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topGoodsPanel);
        
        // Nejvìtší dodavatel
        JPanel topSuplierPanel = new JPanel( new BorderLayout());
        topSuplierPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Èetnost dodavatelù")); 
        topSuplierPanel.setPreferredSize(new Dimension(500, 240)); //preferovaná velikost panelu
        
        topSupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topSupList.setToolTipText("Èíslo v závorce udává poèet pøíjemek");
        topSupList.setFocusable(false);
        scrollPane = new JScrollPane(topSupList);
        topSuplierPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topSuplierPanel);
        
        
        return content;
    }
    
    /**
     *  Vytvoøí panel se statistikou pøíjemek
     */
    private Component createSalePanel() {
        JPanel content = new JPanel();
        
        // Nejvíce prodávané zboží
        JPanel topGoodsPanel = new JPanel( new BorderLayout());
        topGoodsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Žebøíèek prodávanosti zboží"));
        topGoodsPanel.setPreferredSize(new Dimension(500, 240)); //preferovaná velikost panelu
        
        topSaleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(topSaleList);
        topSaleList.setToolTipText("Èíslo v závorce udává poèet prodaných kusù");
        topSaleList.setFocusable(false);
        topGoodsPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topGoodsPanel);
        
        // Nejvìtší odbìratel
        JPanel topCustomerPanel = new JPanel( new BorderLayout() );
        topCustomerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Èetnost odbìratelù"));
        topCustomerPanel.setPreferredSize(new Dimension(500, 240)); //preferovaná velikost panelu
        
        topCustList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topCustList.setToolTipText("Èíslo v závorce udává poèet výdejek");
        topCustList.setFocusable(false);
        scrollPane = new JScrollPane(topCustList);
        topCustomerPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topCustomerPanel);
        
        return content;
    }

    /**
     *  Nastaví statistiky o nákupu a prodeji
     */
    private void setStatistik() throws SQLException {
        
        // Statistika nejnakupovanìjšího
        ArrayList<ChartItem> chart = statistik.getTopBuyGoods();
        topBuyList.setListData( chart.toArray() );

        // Statistika nejèetnìjšího dodavatele
        chart = statistik.getTopSuplier();
        topSupList.setListData( chart.toArray() );
        
        // Statistika nej prodávanìjšího
        chart = statistik.getTopSaleGoods();
        topSaleList.setListData( chart.toArray() );

        // Statistika nejèetnìjšího odbìratele
        chart = statistik.getTopCustomer();
        topCustList.setListData( chart.toArray() );
    }
    
    private class StatistikKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    break;
                case KeyEvent.VK_ESCAPE :
                    StatistikDialog.this.dispose();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }      
}
