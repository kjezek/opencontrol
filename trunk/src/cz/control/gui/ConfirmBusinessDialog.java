/*
 * ConfirmBusinessDialog.java
 *
 * Vytvoøeno 22. bøezen 2006, 10:41
 *
 * Autor: Kamil Jeek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.business.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;
import static cz.control.business.Settings.*;

/**
 * Program Control - Skladovı systém
 *
 * Tøída vytváøí dialogové okno, které se zobrazuje pøi potvrzení
 * Pøíjemky/Vıdejky, èi maloobchodu. Slouí pro nastavení posledních voleb
 * pøed potvrzením prodeje
 *
 * @author Kamil Jeek
 *
 * (C) 2006, ver. 1.0
 */
public class ConfirmBusinessDialog extends JDialog {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    /**
     * Konstanta øíkající, e se má pouít platba v hotovosti
     */
    public final static int CASH_PAYMENT = 0;
    /**
     * Konstanta øíkající, e se má pouít bezhotovostní platba
     */
    public final static int NO_CASH_PAYMENT = 1;
    
    private Component owner;
    private static ConfirmBusinessDialog result;
    
    private JButton confirmButton;
    private JButton cancelButton;
    
    private JRadioButton cashRB;
    private JRadioButton notCashRB;
    private JCheckBox useCalcCB;
    private JCheckBox printCB;
    
    private static int payment = NO_CASH_PAYMENT;
    private static boolean print = true;
    private static boolean useCalc = true;
    
    
    /**
     * Vytvoøí novou instanci ConfirmBusinessDialog
     */
    private ConfirmBusinessDialog(Frame owner) {
        super(owner, "Potvrzení", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvoøí novou instanci ConfirmBusinessDialog
     */
    private ConfirmBusinessDialog(Dialog owner) {
        super(owner, "Potvrzení", true);
        this.owner = owner;
        setDialog();
    }    
    
    /**
     * Otevøe dialog
     * 
     * @param owner vlastník dialogu
     * @return vytvoøenı objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Frame owner) {
        new ConfirmBusinessDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog
     * @param owner vlastník dialogu
     * @return vytvoøenı objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Dialog owner) {
        new ConfirmBusinessDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog
     * 
     * @param owner vlastník dialogu
     * @return vytvoøenı objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Frame owner, int defaultPayment) {
        payment = defaultPayment;
        new ConfirmBusinessDialog(owner);
        return result;
    }
    
    /**
     * Otevøe dialog
     * @param owner vlastník dialogu
     * @return vytvoøenı objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Dialog owner, int defaultPayment) {
        payment = defaultPayment;
        new ConfirmBusinessDialog(owner);
        return result;
    }    
    
    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {

//        this.addWindowListener(this);

        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        // Nutno volat pøed zjišováním velikosti dialogu
        pack();
        
        // Souøadnice posunu nastav na støed
        int translateX = owner.getWidth() / 2 - this.getWidth() / 2;
        int translateY = owner.getHeight() / 2 - this.getHeight() / 2;
        
        setLocation( owner.getX() + translateX, owner.getY() + translateY);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        
        setVisible(true);
        
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
     *  Vytvoøí vlastní obsah okna
     */
    private JPanel getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.add(createMainPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);
        
        return content;
    }
    
    /**
     *  Vytvoøí hlavní panel 
     */
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        Font font;
        JLabel label;  
        
        //content.setPreferredSize( new Dimension(280, 135) );
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));

        notCashRB = new JRadioButton("Platba bezhotovostnì", (payment == NO_CASH_PAYMENT) ? true : false);
        notCashRB.addActionListener( new CashRBListener() );
        content.add(setComponent(notCashRB, 0, 0, 1, 1, 0.0, 1.0, NONE, NORTHWEST));

        cashRB = new JRadioButton("Platba hotovì", (payment == CASH_PAYMENT) ? true : false);
        cashRB.addActionListener( new CashRBListener() );
        content.add(setComponent(cashRB, 0, 1, 1, 1, 0.0, 1.0, NONE, NORTHWEST));

        useCalcCB = new JCheckBox("Pouít kalkulaèku", useCalc);
        useCalcCB.setEnabled( cashRB.isSelected() );
        useCalcCB.addActionListener( new UseCalcListener() );
        content.add(setComponent(useCalcCB, 0, 2, 1, 1, 0.0, 1.0, NONE, NORTH));
        
        printCB = new JCheckBox("Vytisknout doklad", print);
        printCB.addActionListener( new PrintListener() );
        content.add(setComponent(printCB, 0, 3, 1, 1, 0.0, 1.0, NONE, NORTHWEST));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(cashRB);
        buttonGroup.add(notCashRB);
        
        return content;
    }
    
    /**
     *  Vytvoøí spodní panel s potvrzujícím tlaèítkem
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení"));

        cancelButton = new JButton("Zrušit");
        cancelButton.addActionListener( new CancelButtonListener());
        cancelButton.setMnemonic(KeyEvent.VK_BACK_SPACE);
        content.add(cancelButton);
        
        confirmButton = new JButton("Potvrdit");
        confirmButton.addActionListener( new ConfirmButtonListener());
        confirmButton.setMnemonic(KeyEvent.VK_ENTER);
        content.add(confirmButton);
        
        confirmButton.requestFocus();
                
        return content;
    }
    
    
    /**
     *  Potvrdí dialog
     */
    private void confirm() {
    
        result = this;
        this.dispose();
    }
    
    /**
     *  Zruší dialog
     */
    private void cancel() {
    
        result = null;
        this.dispose();
    }    
    
  
    /**
     *  Posluchaè stisku tlaèítka Potvzení 
     */
    private class ConfirmButtonListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            confirm();
       }
    }   
    
   /**
     *  Posluchaè stisku tlaèítka Zrušení 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            cancel();
        }

    }   
    
    /**
     *  Zmìna ve vıbìru platby
     */
    private class CashRBListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (cashRB.isSelected()) {
                payment = CASH_PAYMENT;
            } else if (notCashRB.isSelected()) {
                payment = NO_CASH_PAYMENT;
            }
            useCalcCB.setEnabled( cashRB.isSelected() );
        }

    }

    /**
     *  Zmìna ve vıbìru pouití kalkulaèky
     */
    private class UseCalcListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            useCalc = useCalcCB.isSelected();
        }
    }

    /**
     *  Zmìna ve vıbìru Tisk dokladu
     */
    private class PrintListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            print = printCB.isSelected();
        }
    }    

    /**
     * Vrací jaká se má pouít úlatba
     * @return jaká se má pouít platba
     */
    public static int getPayment() {
        return payment;
    }

    /**
     * Nastavuje jaká se má pouít úlatba
     * @param aPayment jaká se má pouít úlatba
     * Pouijte konstanty CASH_PAYMENT, nebo NO_CASH_PAYMENT
     */
    public static void setPayment(int aPayment) {
        payment = aPayment;
    }

    /**
     * Vrací, zda se má tisknout pøíjmovı/vıdejovı doklad
     * @return zda se má tisknout pøíjmovı/vıdejovı doklad
     */
    public static boolean isPrint() {
        return print;
    }

    /**
     * Nastavuje, zda se má tisknout vıdejovı doklad
     * @param aPrint zda se má tisknout vıdejovı doklad
     */
    public static void setPrint(boolean aPrint) {
        print = aPrint;
    }

    /**
     * Vrací, zda se má zobrait kalkulaèka pro vıpoèet vrácení penìz
     * @return true jestlie se má zobrazit kalkulaèka, jinak false
     */
    public static boolean isUseCalc() {
        return useCalc;
    }

    /**
     * Nastavuje, zda se má zobrazit kalkulaèka
     * @param aUseCalc true - jestlie se má zobrazit kalkulaèka
     */
    public static void setUseCalc(boolean aUseCalc) {
        useCalc = aUseCalc;
    }

    /**
     * Vrací vytvoøenı dialog, nebo null, jestlie uivatel dialog nepotvrdil
     * @return vytvoøenı dialog, nebo null, jestlie uivatel dialog nepotvrdil
     */
    public static ConfirmBusinessDialog getResult() {
        return result;
    }
}

