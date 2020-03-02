/*
 * PriceListDialog.java
 *
 * Vytvo�eno 11. b�ezen 2006, 19:53
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;
import cz.control.data.PriceList;
import cz.control.business.*;
import cz.control.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

import javax.swing.*;

import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialog s tvorbou cen�ku. Umo��uje zad�vat mar�i prodejn�ch cen
 * vzhledem k n�kupn�cen�
 *
 * @author Kamil Je�ek
 *
 * (C) 2006, ver. 1.0
 */
public class PriceListDialog extends JDialog  {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private Component owner;
    
    private static final double MAX_VALUE = 9999;
    private static final double STEP = 1.0;
    
    private JSpinner pcA = new JSpinner(
            new SpinnerNumberModel(0.00, -MAX_VALUE, MAX_VALUE, STEP));
    private JSpinner pcB = new JSpinner(
            new SpinnerNumberModel(0.00, -MAX_VALUE, MAX_VALUE, STEP));
    private JSpinner pcC = new JSpinner(
            new SpinnerNumberModel(0.00, -MAX_VALUE, MAX_VALUE, STEP));
    private JSpinner pcD = new JSpinner(
            new SpinnerNumberModel(0.00, -MAX_VALUE, MAX_VALUE, STEP));
    
    private JLabel priceListTypeLabel = new JLabel("V�choz� cen�k  ");

    private JButton confirmButton;
    private JButton cancelButton;
    
    private PriceList oldPriceList; 
    private static PriceList dialogResult = null;
    
    /**
     * Vytvo�� nov� objekt PriceListDialog
     * @param owner Vlastn�k dialogu
     * @param oldPriceList cen�k, kter� se m� zobrazit v dialogu a bude editov�n. Jestli�e je
     * na vstupu null, vytvo�� se v�choz� cen�k
     */
    private PriceListDialog(Frame owner, PriceList oldPriceList) {
        super(owner, "Control - Tvorba cen�ku", true);

        this.oldPriceList = oldPriceList;
        
        if (this.oldPriceList == null) {
            this.oldPriceList = new PriceList();
        }
        
        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� nov� objekt PriceListDialog
     * @param owner Vlastn�k dialogu
     * @param oldPriceList cen�k, kter� se m� zobrazit v dialogu a bude editov�n. Jestli�e je
     * na vstupu null, vytvo�� se v�choz� cen�k
     */
    private PriceListDialog(Dialog owner, PriceList oldPriceList) {
        super(owner, "Control - Tvorba cen�ku", true);

        this.oldPriceList = oldPriceList;
        
        if (this.oldPriceList == null) {
            this.oldPriceList = new PriceList();
        }
        
        this.owner = owner;
        setDialog();
    }  

    /**
     * Otev�e dialog a vrac�, zda byl dialog potvrzen
     * @return true jestli�e byl dialog potvrzen stiskem tla��tka "Potvrdit", jinak vrac� false
     * @param owner vlastn�k dialogu
     * @param oldPriceList cen�k, kter� se m� zobrazit v dialogu a bude editov�n. Jestli�e je
     * na vstupu null, vytvo�� se v�choz� cen�k
     */
    public static PriceList openPriceListDialog(Frame owner, PriceList oldPriceList) {
        
        new PriceListDialog(owner, oldPriceList);
        return dialogResult;
    }
    
    /**
     * Otev�e dialog a vrac�, zda byl dialog potvrzen
     * @return true jestli�e byl dialog potvrzen stiskem tla��tka "Potvrdit", jinak vrac� false
     * @param owner vlastn�k dialogu
     * @param oldPriceList cen�k, kter� se m� zobrazit v dialogu a bude editov�n. Jestli�e je
     * na vstupu null, vytvo�� se v�choz� cen�k
     */
    public static PriceList openPriceListDialog(Dialog owner, PriceList oldPriceList) {
        
        new PriceListDialog(owner, oldPriceList);
        return dialogResult;
    }    
    
    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {
        dialogResult = null;
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        setInitialValues();
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(false);
        setPreferredSize(new Dimension(280, 250));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
        setVisible(true);
    }
    
    /**
     *  Nastav� hodnoty ze star�ho cen�ku
     */
    private void setInitialValues() {
        
        if (oldPriceList == null) 
            return;
        
        pcA.setValue( new BigDecimal(oldPriceList.getPcA()).divide(Store.CENT) );
        pcB.setValue( new BigDecimal(oldPriceList.getPcB()).divide(Store.CENT) );
        pcC.setValue( new BigDecimal(oldPriceList.getPcC()).divide(Store.CENT) );
        pcD.setValue( new BigDecimal(oldPriceList.getPcD()).divide(Store.CENT) );
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
     * Vytvo�� obsah okna
     */
    private JComponent getContent() {
        JPanel main = new JPanel(new BorderLayout());

        main.add(createTopPanel(), BorderLayout.NORTH);
        main.add(createMainPanel(), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
            
        confirmButton = new JButton("Zru�it");
        confirmButton.addActionListener( new CancelButtonListener());
        confirmButton.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(confirmButton);

        cancelButton = new JButton("Potvrdit");
        cancelButton.addActionListener( new ConfirmButtonListener());
        cancelButton.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(cancelButton);
        
        main.add(buttonPanel, BorderLayout.SOUTH);
        
        return main;
    }   
    
    /**
     *  Vytvo�� hlavn� obsah okna
     */ 
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        JLabel label;
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Tvorba cen�ku"));
        
        label = new JLabel("<html>" +
                "Zadejte mar�i prodejn�ch cen:" +
                "</html>");
        content.add(setComponent(label, 0, 0, 3, 1, 1.0, 0.0, NONE, WEST));

        Font font = new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize());
        
        label = new JLabel(" " + Settings.getPcAName() + ": ");
        label.setFont(font);
        content.add(setComponent(label, 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, EAST));
        pcA.addKeyListener( new PriceListKeyListener() );
        content.add(setComponent(pcA, 1, 1, 1, 1, 0.0, 0.0, NONE, EAST));
        label = new JLabel("% ");
        content.add(setComponent(label, 2, 1, 1, 1, 0.0, 1.0, NONE, WEST));
        
        label = new JLabel(" " + Settings.getPcBName() + ": ");
        label.setFont(font);
        content.add(setComponent(label, 0, 2, 1, 1, 1.0, 0.0, HORIZONTAL, EAST));
        pcB.addKeyListener( new PriceListKeyListener() );
        content.add(setComponent(pcB, 1, 2, 1, 1, 0.0, 0.0, NONE, EAST));
        label = new JLabel("% ");
        content.add(setComponent(label, 2, 2, 1, 1, 0.0, 1.0, NONE, WEST));
        
        label = new JLabel(" " + Settings.getPcCName() + ": ");
        label.setFont(font);
        content.add(setComponent(label, 0, 3, 1, 1, 1.0, 0.0, HORIZONTAL, EAST));
        pcC.addKeyListener( new PriceListKeyListener() );
        content.add(setComponent(pcC, 1, 3, 1, 1, 0.0, 0.0, NONE, EAST));
        label = new JLabel("% ");
        content.add(setComponent(label, 2, 3, 1, 1, 0.0, 1.0, NONE, WEST));
        
        label = new JLabel(" " + Settings.getPcDName() + ": ");
        label.setFont(font);
        content.add(setComponent(label, 0, 4, 1, 1, 1.0, 0.0, HORIZONTAL, EAST));
        pcD.addKeyListener( new PriceListKeyListener() );
        content.add(setComponent(pcD, 1, 4, 1, 1, 0.0, 0.0, NONE, EAST));
        label = new JLabel("% ");
        content.add(setComponent(label, 2, 4, 1, 1, 0.0, 1.0, NONE, WEST));
        
        return content;
    }
    
    /**
     *  Vytvo�� horn� panel dialogu s nadpisem
     */
    private JPanel createTopPanel() {
        JPanel content = new JPanel(gbl);

        //content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Typ cen�ku"));
        
        content.setPreferredSize( new Dimension(200, 30));
        
        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        
        priceListTypeLabel.setFont(font);
        //priceListTypeLabel.setHorizontalAlignment(JLabel.RIGHT);
        content.add(setComponent(priceListTypeLabel, 0, 0, 1, 1, 1.0, 1.0, NONE, CENTER));
        
        return content;
    }
    
    /**
     *  Zru�� dialog bez uloen� zm�n
     */ 
    private void cancel() {

        dialogResult = null;
        this.dispose(); // Zav�i dialog
    }
    
    /**
     *  Provede nastaven� zmn�n po potvrzen� dialogu 
     */
    private void confirm() {
        
        dialogResult = new PriceList(0,
                oldPriceList.getSupID(),
                new BigDecimal( String.valueOf(pcA.getValue()) ).multiply(Store.CENT).intValue(),
                new BigDecimal( String.valueOf(pcB.getValue()) ).multiply(Store.CENT).intValue(),
                new BigDecimal( String.valueOf(pcC.getValue()) ).multiply(Store.CENT).intValue(),
                new BigDecimal( String.valueOf(pcD.getValue()) ).multiply(Store.CENT).intValue()
                );
        
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
    
    private class PriceListKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    confirmButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE :
                    cancelButton.doClick();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }     
}
     
