/*
 * StoreDialog.java
 *
 * Vytvoøeno 7. listopad 2005, 13:49
 *
 
 */

package cz.control.gui;


import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.data.Goods;
import cz.control.business.*;
import cz.control.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.*;
import java.net.*;

/**
 * Program Control - Skladový systém
 *
 * Zobrazí dialog skladu, kde uživatel vybere zboží
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class StoreDialog extends JDialog{
    
    private StorePanel storePanel;
    private Component owner;
    private User user;
    
    private static ArrayList<Goods> result = new ArrayList<Goods>();
    
    /**
     * Vytvoøí nový objekt StoreDialog
     * @param owner Vlastník, který otevøel tento dialog
     */
    private StoreDialog(Dialog owner, User user) {
        super(owner, "Control - Sklad", true);

        this.user = user;
        this.owner = owner;
        setDialog();
        setVisible(true);
    }
    
    /**
     * Vytvoøí nový objekt StoreDialog
     * @param owner Vlastník, který otevøel tento dialog
     */
    private StoreDialog(Frame owner, User user) {
        super(owner, "Control - Sklad", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
        setVisible(true);
        
    }
    
    /**
     * Vytvoøí nový objekt StoreDialog. A vyhledá zboží podle zadaného 
     * klíèového slova
     * 
     * @param keyword klíèové slovo, podle kterého se má hledat
     * @param owner Vlastník, který otevøel tento dialog
     */
    private StoreDialog(Dialog owner, User user, String keyword, boolean showZeroCards) {
        super(owner, "Control - Sklad", true);

        this.user = user;
        this.owner = owner;
        setDialog();

        storePanel.refresh(keyword, showZeroCards);
        setVisible(true);
    } 
    
    
    /**
     * Vytvoøí nový objekt StoreDialog. A vyhledá zboží podle zadaného 
     * klíèového slova
     * 
     * @param keyword klíèové slovo, podle kterého se má hledat
     * @param owner Vlastník, který otevøel tento dialog
     */
    private StoreDialog(Frame owner, User user, String keyword, boolean showZeroCards) {
        super(owner, "Control - Sklad", true);

        this.user = user;
        this.owner = owner;

        storePanel.refresh(keyword, showZeroCards);
        setVisible(true);
    } 
    
    /**
     * Otevøe dialog pro výbìr zboží
     * @param owner Vlastník dialogu
     * @param user Uživatel pro kterého se dialog otevírá
     * @return seznam s nalezeným zbožím
     */
    public static ArrayList<Goods> openDialog(Frame owner, User user) {
        result.clear();
        new StoreDialog(owner, user);
        return result;
    }
    
    /**
     * Otevøe dialog pro výbìr zboží
     * @param owner Vlastník dialogu
     * @param user Uživatel pro kterého se dialog otevírá
     * @return seznam s nalezeným zbožím
     */
    public static ArrayList<Goods> openDialog(Dialog owner, User user) {
        result.clear();
        new StoreDialog(owner, user);
        return result;
    }

    /**
     * Otevøe dialog pro výbìr zboží
     * @param owner Vlastník dialogu
     * @param user Uživatel pro kterého se dialog otevírá
     * @param keyword klíèové skovo, které slouží jako filtr
     * @return seznam s nalezeným zbožím
     */
    public static ArrayList<Goods> openDialog(Frame owner, User user, String keyword, boolean showZeroCards) {
        result.clear();
        new StoreDialog(owner, user, keyword, showZeroCards);
        return result;
    }

    /**
     * Otevøe dialog pro výbìr zboží
     * @param owner Vlastník dialogu
     * @param user Uživatel pro kterého se dialog otevírá
     * @param keyword klíèové skovo, které slouží jako filtr
     * @return seznam s nalezeným zbožím
     */
    public static ArrayList<Goods> openDialog(Dialog owner, User user, String keyword, boolean showZeroCards) {
        result.clear();
        new StoreDialog(owner, user, keyword, showZeroCards);
        return result;
    }

    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(true);
        setMinimumSize(new Dimension(650, 450));

        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
    }
  
    
    /**
     * Vytvoøí obsah okna
     */
    private JComponent getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        URL iconURL;
        Icon imageIcon;
        JButton button;
        
        storePanel = new StorePanel(this, user);
        content.add(storePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzení vybrání položek"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        iconURL = StoreDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zavøít", imageIcon);
        button.setToolTipText("Zavøe okno bez výbìru zboží");
        button.addActionListener( new CancelButtonListener() );
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);
        
        iconURL = StoreDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vybrat", imageIcon);
        button.setToolTipText("Vybere oznaèené zboží.");
        button.addActionListener( new ConfirmButtonListener() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);
        return content;
    }
    
   /**
     *  Posluchaè stisku tlaèítka Potvrzení výbìru  
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            /* Zjisti onzaèené øádky */
            ListSelectionModel listSM = storePanel.getGoodsTable().getSelectionModel();
            int firstRow = listSM.getMinSelectionIndex();
            int lastRow = listSM.getMaxSelectionIndex();
            
            // jestliže není nic vybráno
            if (firstRow == -1) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Vyberte zboží, které chcete vložit do pøíjemky");
                JOptionPane.showMessageDialog(StoreDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            GoodsTableModel goodsTableModel = storePanel.getGoodsTableModel();
            
            /* Veber pøíslušné øádky z tabulky */

            /* Projdi øádky a vymaž odpovídající položky z databáze*/
            for (int i = firstRow; i <= lastRow; i++) {

                /* Zkontroluj jestli je tento øádek oznaèen (mezi prvním a posledním oznaèeným mohou být i neoznaèené */
                if (listSM.isSelectedIndex(i)) {
                    // Vybere z databáze zboží a pošli k pøíjemce 
                    Goods goods = goodsTableModel.getGoodsAt(i);

                    // Doplò zboží mezi výsledky
                    result.add(goods);
                }
            }


            
            StoreDialog.this.dispose();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka zrušení výbìru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            StoreDialog.this.dispose();
        }
    }
    
}
