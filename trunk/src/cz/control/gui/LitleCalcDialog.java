/*
 * LitleCalcDialog.java
 *
 * Vytvo¯eno 18. ˙nor 2006, 0:49
 *
 * Autor: Kamil Jeûek
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
 * Program Control - Skladov˝ systÈm
 *
 * T¯Ìda vytv·¯Ì dialogovÈ okno s jednoduchou kalkulaËkou,
 * kter· po zad·nÌ danÈ Ë·stky vypoËte, kolik se m· vr·tit
 *
 * @author Kamil Jeûek
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
    
    // Labely pro zobrazenÌ cen
    private JLabel priceLabel;
    private JLabel payPriceLabel;
    private JLabel rePayPriceLabel;
    private JLabel rePayPriceTextLabel;
    private JPanel rePayPricePanel;
    
    // TlaËÌtka pro potvrzenÌ
    private JButton confirmButton;
    private JButton cancelButton;
    
    /** signalizuje, zda byla stisknuta desetin· teËka */
    private boolean dotUsed = false;
    /** signalizuje, ûe desetinou teËku je moûno pouûÌt */
    private boolean dotUsePossible = true;
    
    private static DecimalFormat df =  Settings.getPriceFormat();;
    
    /**
     * Vytvo¯Ì novou instanci LitleCalcDialog
     */
    private LitleCalcDialog(Frame owner, long price) {
        super(owner, "SouËet cen", true);
        this.owner = owner;
        this.price = new BigDecimal(price).divide(Store.CENT);
        setDialog();
    }
    
    /**
     * Vytvo¯Ì novou instanci LitleCalcDialog
     */
    private LitleCalcDialog(Dialog owner, long price) {
        super(owner, "SouËet cen", true);
        this.owner = owner;
        this.price = new BigDecimal(price).divide(Store.CENT);
        setDialog();
    }    
    
    /**
     * Otev¯e dialog
     * 
     * @return true - jestliûe byl dialog potvrzen
     *         false - jestliûe nebyl
     * @param price cena, kterou m· z·kaznÌk zaplatit. PodslednÌ dvÏ mÌsta jsou desetinÈ
     * @param owner vlastnÌk dialogu
     */
    public static boolean openDiaog(Frame owner, long price) {
        result = false;
    
        new LitleCalcDialog(owner, price);
        return result;
    }
    
    /**
     * Otev¯e dialog
     * @param price cena, kterou m· z·kaznÌk zaplatit. PodslednÌ dvÏ mÌsta jsou desetinÈ
     * @param owner vlastnÌk dialogu
     * @return true - jestliûe byl dialog potvrzen
     *         false - jestliûe nebyl
     */
    public static boolean openDiaog(Dialog owner, long price) {
        result = false;
        
        new LitleCalcDialog(owner, price);
        return result;
    }
    
    /**
     * provede pot¯ebnÈ nastavenÌ 
     */
    private void setDialog() {

//        this.addWindowListener(this);

        addKeyListener( new DialogKeyListener() );
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        // Nutno volat p¯ed zjiöùov·nÌm velikosti dialogu
        pack();
        
        // Sou¯adnice posunu nastav na st¯ed
        int translateX = owner.getWidth() / 2 - this.getWidth() / 2;
        int translateY = owner.getHeight() / 2 - this.getHeight() / 2;
        
        setLocation( owner.getX() + translateX, owner.getY() + translateY);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        
        refresch();
        
        setVisible(true);
        
    }    
    
    /**
     *  NastavÌ vlastnosti vkl·danÈ komponenty 
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
     *  Vytvo¯Ì vlastnÌ obsah okna
     */
    private JPanel getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        
        content.setToolTipText("Numerick˝mi kl·vesami zadejte placenou Ë·stku");
        
        content.add(createMainPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);
        
        return content;
    }
    
    /**
     *  Vytvo¯Ì hlavnÌ panel 
     */
    private JPanel createMainPanel() {
        JPanel content = new JPanel(gbl);
        Font font;
        JLabel label;  
        
        content.setPreferredSize( new Dimension(280, 135) );
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Zadejte placenou Ë·stku"));
        
        font  =  new Font("Serif", Font.BOLD | Font.ITALIC, Settings.getMainItemsFontSize());
        label = new JLabel("SouËet: ");
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
        rePayPriceTextLabel = new JLabel("Vr·titt: ");
        rePayPriceTextLabel.setFont(font);
        content.add(setComponent(rePayPriceTextLabel, 0, 2, 2, 1, 1.0, 0.0, HORIZONTAL, NORTHWEST));
        
        font  =  new Font("Serif", Font.BOLD, 35);
        rePayPriceLabel = new JLabel();
        rePayPriceLabel.setFont(font);
        rePayPricePanel = new JPanel( new BorderLayout()); // Panel kv˘li p¯ebarvenÌ pozadÌ a r·meËku
        rePayPricePanel.setBackground( Color.WHITE );
        rePayPricePanel.add(rePayPriceLabel, BorderLayout.EAST);
        content.add(setComponent(rePayPricePanel, 0, 3, 2, 1, 1.0, 1.0, BOTH, SOUTHEAST));

        return content;
    }
    
    /**
     *  Vytvo¯Ì spodnÌ panel s potvrzujÌcÌm tlaËÌtkem
     */
    private JPanel createBottomPanel() {
        JPanel content = new JPanel();
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "PotvrzenÌ"));

        cancelButton = new JButton("Zav¯Ìt");
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
     *  NastavÌ ceny do dialogu
     */
    private void refresch() {
        priceLabel.setText( df.format(price) );
                     
        payPriceLabel.setText( df.format(payPrice) );

        rePayPrice = payPrice.subtract(price);
      
        // Jestliûe je zaplaceno m·lo
        if (rePayPrice.doubleValue() < 0) {
            rePayPriceTextLabel.setText("ChybÌ: ");
            rePayPriceTextLabel.setForeground( Color.RED );
            rePayPriceLabel.setText( df.format(rePayPrice.abs()) );
            rePayPriceLabel.setForeground( Color.RED );
            rePayPricePanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            rePayPriceTextLabel.setText("Vr·tit: ");
            rePayPriceTextLabel.setForeground( new Color(0x065490) );
            rePayPriceLabel.setText( df.format(rePayPrice) );
            rePayPriceLabel.setForeground( new Color(0x065490) );
            rePayPricePanel.setBorder(BorderFactory.createLineBorder( new Color(0x065490)) );
        }
        
    }
    
    /**
     *  PotvrdÌ dialog
     */
    private void confirm() {
    
        // Zkontroluj jestli nenÌ manko
        if (rePayPrice.doubleValue() < 0) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_ENOUGHT_CASH, 
                    "<span style='font:bold 15px Times; color:red'>ChybÌ: <b>" + df.format(rePayPrice.abs()) + " KË.</b></span><br>" +
                    "Opravte prosÌm zad·nÌ.");
            
            Object[] options = {"Opravit", "Neopravovat"};
            
            int n = JOptionPane.showOptionDialog(
                    this, er.getFormatedText() , er.getTextCode(), 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,   // û·dna vlastnÌ ikonka
                    options,
                    options[0]); 
            
            // Jestliûe uûivatel chce upravit zad·nÌ
            if (n == 0)
                return;
        } else {
            JOptionPane.showMessageDialog(this, 
                    "<html><center>" +
                    "<span style='font:bold 12px Times'>K vr·cenÌ: </span><br>" +
                    "<span style='font:bold 25px Times; color : #065490'>" + df.format(rePayPrice) + " KË</span>" +
                    "</center></html>" , 
                    "Prodej potvrzen", 
                    JOptionPane.INFORMATION_MESSAGE); 
        }
        
        result = true;
        this.dispose();
    }
    
    /**
     *  ZruöÌ dialog
     */
    private void cancel() {
    
        result = false;
        this.dispose();
    }    
    
    /**
     *  NastavÌ dalöÌ ËÌslo v poli s placenÌm
     */
    private void setNextNumber(KeyEvent e) {
        boolean shift = false;
                
        String s = payPriceLabel.getText().trim();
        
        BigDecimal resultPrice = payPrice.multiply(Store.CENT);
        
        char c = e.getKeyChar();

        
        // Rosliö co bylo stisknuto
        if (c >= '0' && c <= '9') {

            // Jestliûe byl stisknuta desetin· teËka a jeötÏ se zad·v·
            // vymaû nuly za desetinou teËkou
            if (dotUsed) {
               // Jestliûe jsou na konci dvÏ desetinÈ nuly, bude se posouvat
               if (s.endsWith("00")) {
                   shift = true;
                   // Vymaû DVÃ NULY
                   resultPrice = resultPrice.divide(Store.CENT);
               } else {
                   // Vymaû pouze jednu nulu.. p¯i dalöÌm vol·nÌ se vymaûe druh˘ 
                   resultPrice = resultPrice.divide(BigDecimal.TEN);
                   dotUsed = false;
               }
            }
            
            BigDecimal num = new BigDecimal( Integer.valueOf( String.valueOf(c)) );
            resultPrice = resultPrice.multiply(BigDecimal.TEN).add( num );
            
        } else 
        if ( dotUsePossible && dotUsed == false && (c == '.' || c == ',' ) ) { 
            resultPrice = resultPrice.multiply(Store.CENT);      // rozöi¯ na cel· ËÌsla
            dotUsed = true;
            dotUsePossible = false;
        } else 
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) { 
            resultPrice = resultPrice.divide(BigDecimal.TEN);      // umaû jedno mÌsto
            dotUsePossible = true;
            dotUsed = false;
            shift = false;
        } else 
        if (e.getKeyCode() == KeyEvent.VK_DELETE) { 
            resultPrice = BigDecimal.ZERO;     // Rozöi¯ na desetinnÈ mÌsto
            dotUsePossible = true;
        }
        
       // PosuÚ o jedno desetinÈ mÌsto
        if (shift && dotUsed) {
            resultPrice = resultPrice.multiply(BigDecimal.TEN);
        } 
        
        // ZaokrouhlenÌ na celou Ë·st (poslednÌ dvÏ mÌsta jsou desetin·)
        long tmp = resultPrice.longValue();
        resultPrice = new BigDecimal(tmp);
        
        payPrice = resultPrice.divide(Store.CENT);

        refresch();
    }
    
    /**
     *  PosluchaË stisku tlaËÌtka PotvzenÌ 
     */
    private class ConfirmButtonListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            confirm();
       }
    }   
    
   /**
     *  PosluchaË stisku tlaËÌtka ZruöenÌ 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            cancel();
        }

    }   
    
    /**
     *  PosluchaË stisku kl·vesy
     */
    private class DialogKeyListener implements KeyListener {
        
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER : // P¯i enteru potvrÔ
                    confirmButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE : // P¯i escape zruö
                    cancelButton.doClick();
                    break;
                default :
                    setNextNumber(e); // jinak dalöÌ ËÌslo
            }

        }

        public void keyReleased(KeyEvent e) {
       }
        
    }
    
}
