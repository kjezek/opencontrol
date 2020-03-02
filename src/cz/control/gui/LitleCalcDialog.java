/*
 * LitleCalcDialog.java
 *
 * Vytvo�eno 18. �nor 2006, 0:49
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.business.*;
import java.math.BigDecimal;
import java.text.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialogov� okno s jednoduchou kalkula�kou,
 * kter� po zad�n� dan� ��stky vypo�te, kolik se m� vr�tit
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class LitleCalcDialog extends JDialog {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private Component owner;
    private static boolean result = false;
    private BigDecimal price;
    private BigDecimal payPrice = new BigDecimal(0);
    private BigDecimal rePayPrice;
    
    // Labely pro zobrazen� cen
    private JLabel priceLabel;
    private JLabel payPriceLabel;
    private JLabel rePayPriceLabel;
    private JLabel rePayPriceTextLabel;
    private JPanel rePayPricePanel;
    
    // Tla��tka pro potvrzen�
    private JButton confirmButton;
    private JButton cancelButton;
    
    /** signalizuje, zda byla stisknuta desetin� te�ka */
    private boolean dotUsed = false;
    /** signalizuje, �e desetinou te�ku je mo�no pou��t */
    private boolean dotUsePossible = true;
    
    private static DecimalFormat df =  Settings.getPriceFormat();;
    
    /**
     * Vytvo�� novou instanci LitleCalcDialog
     */
    private LitleCalcDialog(Frame owner, long price) {
        super(owner, "Sou�et cen", true);
        this.owner = owner;
        this.price = new BigDecimal(price).divide(Store.CENT);
        setDialog();
    }
    
    /**
     * Vytvo�� novou instanci LitleCalcDialog
     */
    private LitleCalcDialog(Dialog owner, long price) {
        super(owner, "Sou�et cen", true);
        this.owner = owner;
        this.price = new BigDecimal(price).divide(Store.CENT);
        setDialog();
    }    
    
    /**
     * Otev�e dialog
     * 
     * @return true - jestli�e byl dialog potvrzen
     *         false - jestli�e nebyl
     * @param price cena, kterou m� z�kazn�k zaplatit. Podsledn� dv� m�sta jsou desetin�
     * @param owner vlastn�k dialogu
     */
    public static boolean openDiaog(Frame owner, long price) {
        result = false;
    
        new LitleCalcDialog(owner, price);
        return result;
    }
    
    /**
     * Otev�e dialog
     * @param price cena, kterou m� z�kazn�k zaplatit. Podsledn� dv� m�sta jsou desetin�
     * @param owner vlastn�k dialogu
     * @return true - jestli�e byl dialog potvrzen
     *         false - jestli�e nebyl
     */
    public static boolean openDiaog(Dialog owner, long price) {
        result = false;
        
        new LitleCalcDialog(owner, price);
        return result;
    }
    
    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {

//        this.addWindowListener(this);

        addKeyListener( new DialogKeyListener() );
        
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
        
        refresch();
        
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
        
        content.setToolTipText("Numerick�mi kl�vesami zadejte placenou ��stku");
        
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
        
        content.setPreferredSize( new Dimension(280, 135) );
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Zadejte placenou ��stku"));
        
        font  =  new Font("Serif", Font.BOLD | Font.ITALIC, Settings.getMainItemsFontSize());
        label = new JLabel("Sou�et: ");
        label.setFont(font);
        content.add(setComponent(label, 0, 0, 1, 1, 0.0, 1.0, NONE, NORTHWEST));
        
        font  =  new Font("Serif", Font.BOLD, 23);
        priceLabel = new JLabel();
        priceLabel.setFont(font);
        content.add(setComponent(priceLabel, 1, 0, 1, 1, 1.0, 1.0, NONE, SOUTHEAST));
        
        font  =  new Font("Serif", Font.BOLD | Font.ITALIC, Settings.getMainItemsFontSize());
        label = new JLabel("Placeno: ");
        label.setFont(font);
        content.add(setComponent(label, 0, 1, 1, 1, 0.0, 1.0, NONE, NORTHWEST));
        
        font  =  new Font("Serif", Font.BOLD, 23);
        payPriceLabel = new JLabel();
        payPriceLabel.setFont(font);
        content.add(setComponent(payPriceLabel, 1, 1, 1, 1, 1.0, 1.0, NONE, SOUTHEAST));
        
        font  =  new Font("Serif", Font.BOLD | Font.ITALIC, 17);
        rePayPriceTextLabel = new JLabel("Vr�titt: ");
        rePayPriceTextLabel.setFont(font);
        content.add(setComponent(rePayPriceTextLabel, 0, 2, 2, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        
        font  =  new Font("Serif", Font.BOLD, 35);
        rePayPriceLabel = new JLabel();
        rePayPriceLabel.setFont(font);
        rePayPricePanel = new JPanel( new BorderLayout()); // Panel kv�li p�ebarven� pozad� a r�me�ku
        rePayPricePanel.setBackground( Color.WHITE );
        rePayPricePanel.add(rePayPriceLabel, BorderLayout.EAST);
        content.add(setComponent(rePayPricePanel, 0, 3, 2, 1, 1.0, 1.0, BOTH, SOUTHEAST));

        return content;
    }
    
    /**
     *  Vytvo�� spodn� panel s potvrzuj�c�m tla��tkem
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen�"));

        cancelButton = new JButton("Zav��t");
        cancelButton.addActionListener( new CancelButtonListener());
        //cancelButton.addKeyListener( new DialogKeyListener() );
        cancelButton.setFocusable(false); 
        cancelButton.setMnemonic(KeyEvent.VK_BACK_SPACE);
        content.add(cancelButton);
        
        confirmButton = new JButton("Potvrdit");
        confirmButton.addActionListener( new ConfirmButtonListener());
        //confirmButton.addKeyListener( new DialogKeyListener() );
        confirmButton.setFocusable(false);
        confirmButton.setMnemonic(KeyEvent.VK_ENTER);
        content.add(confirmButton);
        
        //confirmButton.requestFocus();
                
        return content;
    }
    
    /**
     *  Nastav� ceny do dialogu
     */
    private void refresch() {
        priceLabel.setText( df.format(price) );
                     
        payPriceLabel.setText( df.format(payPrice) );

        rePayPrice = payPrice.subtract(price);
      
        // Jestli�e je zaplaceno m�lo
        if (rePayPrice.doubleValue() < 0) {
            rePayPriceTextLabel.setText("Chyb�: ");
            rePayPriceTextLabel.setForeground( Color.RED );
            rePayPriceLabel.setText( df.format(rePayPrice.abs()) );
            rePayPriceLabel.setForeground( Color.RED );
            rePayPricePanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            rePayPriceTextLabel.setText("Vr�tit: ");
            rePayPriceTextLabel.setForeground( new Color(0x065490) );
            rePayPriceLabel.setText( df.format(rePayPrice) );
            rePayPriceLabel.setForeground( new Color(0x065490) );
            rePayPricePanel.setBorder(BorderFactory.createLineBorder( new Color(0x065490)) );
        }
        
    }
    
    /**
     *  Potvrd� dialog
     */
    private void confirm() {
    
        // Zkontroluj jestli nen� manko
        if (rePayPrice.doubleValue() < 0) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_ENOUGHT_CASH, 
                    "<span style='font:bold 15px Times; color:red'>Chyb�: <b>" + df.format(rePayPrice.abs()) + " K�.</b></span><br>" +
                    "Opravte pros�m zad�n�.");
            
            Object[] options = {"Opravit", "Neopravovat"};
            
            int n = JOptionPane.showOptionDialog(
                    this, er.getFormatedText() , er.getTextCode(), 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,   // ��dna vlastn� ikonka
                    options,
                    options[0]); 
            
            // Jestli�e u�ivatel chce upravit zad�n�
            if (n == 0)
                return;
        } else {
            JOptionPane.showMessageDialog(this, 
                    "<html><center>" +
                    "<span style='font:bold 12px Times'>K vr�cen�: </span><br>" +
                    "<span style='font:bold 25px Times; color : #065490'>" + df.format(rePayPrice) + " K�</span>" +
                    "</center></html>" , 
                    "Prodej potvrzen", 
                    JOptionPane.INFORMATION_MESSAGE); 
        }
        
        result = true;
        this.dispose();
    }
    
    /**
     *  Zru�� dialog
     */
    private void cancel() {
    
        result = false;
        this.dispose();
    }    
    
    /**
     *  Nastav� dal�� ��slo v poli s placen�m
     */
    private void setNextNumber(KeyEvent e) {
        boolean shift = false;
                
        String s = payPriceLabel.getText().trim();
        
        BigDecimal resultPrice = payPrice.multiply(Store.CENT);
        
        char c = e.getKeyChar();

        
        // Rosli� co bylo stisknuto
        if (c >= '0' && c <= '9') {

            // Jestli�e byl stisknuta desetin� te�ka a je�t� se zad�v�
            // vyma� nuly za desetinou te�kou
            if (dotUsed) {
               // Jestli�e jsou na konci dv� desetin� nuly, bude se posouvat
               if (s.endsWith("00")) {
                   shift = true;
                   // Vyma� DV� NULY
                   resultPrice = resultPrice.divide(Store.CENT);
               } else {
                   // Vyma� pouze jednu nulu.. p�i dal��m vol�n� se vyma�e druh� 
                   resultPrice = resultPrice.divide(BigDecimal.TEN);
                   dotUsed = false;
               }
            }
            
            BigDecimal num = new BigDecimal( Integer.valueOf( String.valueOf(c)) );
            resultPrice = resultPrice.multiply(BigDecimal.TEN).add( num );
            
        } else 
        if ( dotUsePossible && dotUsed == false && (c == '.' || c == ',' ) ) { 
            resultPrice = resultPrice.multiply(Store.CENT);      // roz�i� na cel� ��sla
            dotUsed = true;
            dotUsePossible = false;
        } else 
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) { 
            resultPrice = resultPrice.divide(BigDecimal.TEN);      // uma� jedno m�sto
            dotUsePossible = true;
            dotUsed = false;
            shift = false;
        } else 
        if (e.getKeyCode() == KeyEvent.VK_DELETE) { 
            resultPrice = BigDecimal.ZERO;     // Roz�i� na desetinn� m�sto
            dotUsePossible = true;
        }
        
       // Posu� o jedno desetin� m�sto
        if (shift && dotUsed) {
            resultPrice = resultPrice.multiply(BigDecimal.TEN);
        } 
        
        // Zaokrouhlen� na celou ��st (posledn� dv� m�sta jsou desetin�)
        long tmp = resultPrice.longValue();
        resultPrice = new BigDecimal(tmp);
        
        payPrice = resultPrice.divide(Store.CENT);

        refresch();
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
     *  Poslucha� stisku kl�vesy
     */
    private class DialogKeyListener implements KeyListener {
        
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER : // P�i enteru potvr�
                    confirmButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE : // P�i escape zru�
                    cancelButton.doClick();
                    break;
                default :
                    setNextNumber(e); // jinak dal�� ��slo
            }

        }

        public void keyReleased(KeyEvent e) {
       }
        
    }
    
}
