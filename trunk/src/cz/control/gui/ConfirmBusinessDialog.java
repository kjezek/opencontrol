/*
 * ConfirmBusinessDialog.java
 *
 * Vytvo�eno 22. b�ezen 2006, 10:41
 *
 * Autor: Kamil Je�ek
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
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialogov� okno, kter� se zobrazuje p�i potvrzen�
 * P��jemky/V�dejky, �i maloobchodu. Slou�� pro nastaven� posledn�ch voleb
 * p�ed potvrzen�m prodeje
 *
 * @author Kamil Je�ek
 *
 * (C) 2006, ver. 1.0
 */
public class ConfirmBusinessDialog extends JDialog {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    /**
     * Konstanta ��kaj�c�, �e se m� pou��t platba v hotovosti
     */
    public final static int CASH_PAYMENT = 0;
    /**
     * Konstanta ��kaj�c�, �e se m� pou��t bezhotovostn� platba
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
     * Vytvo�� novou instanci ConfirmBusinessDialog
     */
    private ConfirmBusinessDialog(Frame owner) {
        super(owner, "Potvrzen�", true);
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� novou instanci ConfirmBusinessDialog
     */
    private ConfirmBusinessDialog(Dialog owner) {
        super(owner, "Potvrzen�", true);
        this.owner = owner;
        setDialog();
    }    
    
    /**
     * Otev�e dialog
     * 
     * @param owner vlastn�k dialogu
     * @return vytvo�en� objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Frame owner) {
        new ConfirmBusinessDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog
     * @param owner vlastn�k dialogu
     * @return vytvo�en� objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Dialog owner) {
        new ConfirmBusinessDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog
     * 
     * @param owner vlastn�k dialogu
     * @return vytvo�en� objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Frame owner, int defaultPayment) {
        payment = defaultPayment;
        new ConfirmBusinessDialog(owner);
        return result;
    }
    
    /**
     * Otev�e dialog
     * @param owner vlastn�k dialogu
     * @return vytvo�en� objekt dialogu
     */
    public static ConfirmBusinessDialog openDiaog(Dialog owner, int defaultPayment) {
        payment = defaultPayment;
        new ConfirmBusinessDialog(owner);
        return result;
    }    
    
    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {

//        this.addWindowListener(this);

        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        // Nutno volat p�ed zji��ov�n�m velikosti dialogu
        pack();
        
        // Sou�adnice posunu nastav na st�ed
        int translateX = owner.getWidth() / 2 - this.getWidth() / 2;
        int translateY = owner.getHeight() / 2 - this.getHeight() / 2;
        
        setLocation( owner.getX() + translateX, owner.getY() + translateY);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        
        setVisible(true);
        
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
     *  Vytvo�� vlastn� obsah okna
     */
    private JPanel getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.add(createMainPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);
        
        return content;
    }
    
    /**
     *  Vytvo�� hlavn� panel 
     */
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        Font font;
        JLabel label;  
        
        //content.setPreferredSize( new Dimension(280, 135) );
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Volby"));

        notCashRB = new JRadioButton("Platba bezhotovostn�", (payment == NO_CASH_PAYMENT) ? true : false);
        notCashRB.addActionListener( new CashRBListener() );
        content.add(setComponent(notCashRB, 0, 0, 1, 1, 0.0, 1.0, NONE, NORTHWEST));

        cashRB = new JRadioButton("Platba hotov�", (payment == CASH_PAYMENT) ? true : false);
        cashRB.addActionListener( new CashRBListener() );
        content.add(setComponent(cashRB, 0, 1, 1, 1, 0.0, 1.0, NONE, NORTHWEST));

        useCalcCB = new JCheckBox("Pou��t kalkula�ku", useCalc);
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
     *  Vytvo�� spodn� panel s potvrzuj�c�m tla��tkem
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen�"));

        cancelButton = new JButton("Zru�it");
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
     *  Potvrd� dialog
     */
    private void confirm() {
    
        result = this;
        this.dispose();
    }
    
    /**
     *  Zru�� dialog
     */
    private void cancel() {
    
        result = null;
        this.dispose();
    }    
    
  
    /**
     *  Poslucha� stisku tla��tka Potvzen� 
     */
    private class ConfirmButtonListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            confirm();
       }
    }   
    
   /**
     *  Poslucha� stisku tla��tka Zru�en� 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            cancel();
        }

    }   
    
    /**
     *  Zm�na ve v�b�ru platby
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
     *  Zm�na ve v�b�ru pou�it� kalkula�ky
     */
    private class UseCalcListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            useCalc = useCalcCB.isSelected();
        }
    }

    /**
     *  Zm�na ve v�b�ru Tisk dokladu
     */
    private class PrintListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            print = printCB.isSelected();
        }
    }    

    /**
     * Vrac� jak� se m� pou��t �latba
     * @return jak� se m� pou��t platba
     */
    public static int getPayment() {
        return payment;
    }

    /**
     * Nastavuje jak� se m� pou��t �latba
     * @param aPayment jak� se m� pou��t �latba
     * Pou�ijte konstanty CASH_PAYMENT, nebo NO_CASH_PAYMENT
     */
    public static void setPayment(int aPayment) {
        payment = aPayment;
    }

    /**
     * Vrac�, zda se m� tisknout p��jmov�/v�dejov� doklad
     * @return zda se m� tisknout p��jmov�/v�dejov� doklad
     */
    public static boolean isPrint() {
        return print;
    }

    /**
     * Nastavuje, zda se m� tisknout v�dejov� doklad
     * @param aPrint zda se m� tisknout v�dejov� doklad
     */
    public static void setPrint(boolean aPrint) {
        print = aPrint;
    }

    /**
     * Vrac�, zda se m� zobrait kalkula�ka pro v�po�et vr�cen� pen�z
     * @return true jestli�e se m� zobrazit kalkula�ka, jinak false
     */
    public static boolean isUseCalc() {
        return useCalc;
    }

    /**
     * Nastavuje, zda se m� zobrazit kalkula�ka
     * @param aUseCalc true - jestli�e se m� zobrazit kalkula�ka
     */
    public static void setUseCalc(boolean aUseCalc) {
        useCalc = aUseCalc;
    }

    /**
     * Vrac� vytvo�en� dialog, nebo null, jestli�e u�ivatel dialog nepotvrdil
     * @return vytvo�en� dialog, nebo null, jestli�e u�ivatel dialog nepotvrdil
     */
    public static ConfirmBusinessDialog getResult() {
        return result;
    }
}

