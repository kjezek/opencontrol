/*
 * StatistikDialog.java
 *
 * Vytvo�eno 17. prosinec 2005, 11:26
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
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialog ze statistikou 
 *
 * @author Kamil Je�ek
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
     * Vytvo�� nov� objekt StatistikDialog
     * @param owner Vlastn�k, kter� otev�el tento dialog
     */
    public StatistikDialog(Frame owner, User user) {
        super(owner, "Control - Statistiky", true);

        this.user= user;
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� nov� objekt StatistikDialog
     * @param owner Vlastn�k, kter� otev�el tento dialog
     */
    public StatistikDialog(Dialog owner, User user) {
        super(owner, "Control - Statistiky", true);

        this.user= user;
        this.owner = owner;
        setDialog();
    }
    
    
    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {
        
        try {
            statistik = user.openStatistik(); /* Otev�i sklad pro p�ihl�en�ho u�ivatele */
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
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
        setVisible(true);
    }

    /**
     * Vytvo�� obsah okna
     */
    private JComponent getContent() {
        tabbedPane = new JTabbedPane();
    
        tabbedPane.addKeyListener( new StatistikKeyListener() );
        try {
            tabbedPane.addTab("Sklad", null, createStorePanel(), "Statistika skladu");
            tabbedPane.addTab("N�kup", null, createBuyPanel(), "N�kupn� statistiky");
            tabbedPane.addTab("Prodej", null, createSalePanel(), "Prodejn� statistiky");
            
            setStatistik(); // Nastav statistiky
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(StatistikDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
        
        return tabbedPane;
    }    
    
    /**
     *  Vytvo�� panel se statistikou skladu
     */
    private JPanel createStorePanel() throws SQLException {
        JPanel content = new JPanel();
        
        
        // Panel zobrazuj�c� mno�stv� skladov�ch karet - sortiment
        JPanel sortimentPanel = new JPanel( new GridLayout(4, 2));
        sortimentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Po�et skladov�ch karet"));
        sortimentPanel.setPreferredSize(new Dimension(500, 150)); //Minimaln� velikost panelu

        JLabel label = new JLabel("Celkem: ");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getGoodsCardCount()));
        sortimentPanel.add(label);
        
        label = new JLabel("Kladn� mno�stv�: ");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getPositiveCardCount()) );
        sortimentPanel.add(label);
        
        label = new JLabel("Nulov� mno�stv�: ");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getZeroGoodsCardCount()) );
        sortimentPanel.add(label);
        
        content.add(sortimentPanel);
        
        label = new JLabel("<html><span style='color:red'>Z�porn� mno�stv�:</span></html>");
        sortimentPanel.add(label);
        label = new JLabel(String.valueOf(statistik.getNegativeCardCount()) );
        sortimentPanel.add(label);
        
        // Panel zobrazuj�c� statistiky cen zbo��
        JPanel pricePanel = new JPanel( new GridLayout(1, 2));
        pricePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny zbo��"));
        pricePanel.setPreferredSize(new Dimension(500, 45)); //preferovan� velikost panelu
        

        label = new JLabel("Hodnota skladu: ");
        pricePanel.add(label);
        double num = new BigDecimal(statistik.getStorePrice()).divide(Store.CENT).doubleValue();
        label = new JLabel( df.format(num) + " K�" );
        Font font =  new Font("Serif", Font.BOLD, Settings.getMainItemsFontSize());
        label.setFont(font);
        pricePanel.add(label);
        
        content.add(pricePanel);
        
        // Panel zobrazuj�c� statistiky obchodn�k�
        JPanel bussynesPanel = new JPanel( new GridLayout(2, 1));
        bussynesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Obchodn� partne�i"));
        bussynesPanel.setPreferredSize(new Dimension(500, 100)); //preferovan� velikost panelu
        label = new JLabel("Po�et dodavatel�: " );
        bussynesPanel.add(label);
        label = new JLabel( String.valueOf(statistik.getSuplierCount()) );
        bussynesPanel.add(label);
        label = new JLabel("Po�et odb�ratel�: ");
        bussynesPanel.add(label);
        label = new JLabel( String.valueOf(statistik.getCustomerCount()) );
        bussynesPanel.add(label);
        
        content.add(bussynesPanel);
        
        return content;
    }

    /**
     *  Vytvo�� panel se statistikou p��jemek
     */
    private Component createBuyPanel() {
        JPanel content = new JPanel();
        
        // Nejv�ce nakupovan� zbo��
        JPanel topGoodsPanel = new JPanel( new BorderLayout());
        topGoodsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�eb���ek nakupovanosti zbo��"));
        topGoodsPanel.setPreferredSize(new Dimension(500, 240)); //preferovan� velikost panelu

        topBuyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topBuyList.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(topBuyList);
        topBuyList.setToolTipText("��slo v z�vorce ud�v� po�et nakoupen�ch kus�");
        topGoodsPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topGoodsPanel);
        
        // Nejv�t�� dodavatel
        JPanel topSuplierPanel = new JPanel( new BorderLayout());
        topSuplierPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�etnost dodavatel�")); 
        topSuplierPanel.setPreferredSize(new Dimension(500, 240)); //preferovan� velikost panelu
        
        topSupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topSupList.setToolTipText("��slo v z�vorce ud�v� po�et p��jemek");
        topSupList.setFocusable(false);
        scrollPane = new JScrollPane(topSupList);
        topSuplierPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topSuplierPanel);
        
        
        return content;
    }
    
    /**
     *  Vytvo�� panel se statistikou p��jemek
     */
    private Component createSalePanel() {
        JPanel content = new JPanel();
        
        // Nejv�ce prod�van� zbo��
        JPanel topGoodsPanel = new JPanel( new BorderLayout());
        topGoodsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�eb���ek prod�vanosti zbo��"));
        topGoodsPanel.setPreferredSize(new Dimension(500, 240)); //preferovan� velikost panelu
        
        topSaleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(topSaleList);
        topSaleList.setToolTipText("��slo v z�vorce ud�v� po�et prodan�ch kus�");
        topSaleList.setFocusable(false);
        topGoodsPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topGoodsPanel);
        
        // Nejv�t�� odb�ratel
        JPanel topCustomerPanel = new JPanel( new BorderLayout() );
        topCustomerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�etnost odb�ratel�"));
        topCustomerPanel.setPreferredSize(new Dimension(500, 240)); //preferovan� velikost panelu
        
        topCustList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topCustList.setToolTipText("��slo v z�vorce ud�v� po�et v�dejek");
        topCustList.setFocusable(false);
        scrollPane = new JScrollPane(topCustList);
        topCustomerPanel.add(scrollPane, BorderLayout.CENTER);
        
        content.add(topCustomerPanel);
        
        return content;
    }

    /**
     *  Nastav� statistiky o n�kupu a prodeji
     */
    private void setStatistik() throws SQLException {
        
        // Statistika nejnakupovan�j��ho
        ArrayList<ChartItem> chart = statistik.getTopBuyGoods();
        topBuyList.setListData( chart.toArray() );

        // Statistika nej�etn�j��ho dodavatele
        chart = statistik.getTopSuplier();
        topSupList.setListData( chart.toArray() );
        
        // Statistika nej prod�van�j��ho
        chart = statistik.getTopSaleGoods();
        topSaleList.setListData( chart.toArray() );

        // Statistika nej�etn�j��ho odb�ratele
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
