/*
 * StoreDialog.java
 *
 * Vytvo�eno 7. listopad 2005, 13:49
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
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialog skladu, kde u�ivatel vybere zbo��
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class StoreDialog extends JDialog{
    
    private StorePanel storePanel;
    private Component owner;
    private User user;
    
    private static ArrayList<Goods> result = new ArrayList<Goods>();
    
    /**
     * Vytvo�� nov� objekt StoreDialog
     * @param owner Vlastn�k, kter� otev�el tento dialog
     */
    private StoreDialog(Dialog owner, User user) {
        super(owner, "Control - Sklad", true);

        this.user = user;
        this.owner = owner;
        setDialog();
        setVisible(true);
    }
    
    /**
     * Vytvo�� nov� objekt StoreDialog
     * @param owner Vlastn�k, kter� otev�el tento dialog
     */
    private StoreDialog(Frame owner, User user) {
        super(owner, "Control - Sklad", true);
        
        this.user = user;
        this.owner = owner;
        setDialog();
        setVisible(true);
        
    }
    
    /**
     * Vytvo�� nov� objekt StoreDialog. A vyhled� zbo�� podle zadan�ho 
     * kl��ov�ho slova
     * 
     * @param keyword kl��ov� slovo, podle kter�ho se m� hledat
     * @param owner Vlastn�k, kter� otev�el tento dialog
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
     * Vytvo�� nov� objekt StoreDialog. A vyhled� zbo�� podle zadan�ho 
     * kl��ov�ho slova
     * 
     * @param keyword kl��ov� slovo, podle kter�ho se m� hledat
     * @param owner Vlastn�k, kter� otev�el tento dialog
     */
    private StoreDialog(Frame owner, User user, String keyword, boolean showZeroCards) {
        super(owner, "Control - Sklad", true);

        this.user = user;
        this.owner = owner;

        storePanel.refresh(keyword, showZeroCards);
        setVisible(true);
    } 
    
    /**
     * Otev�e dialog pro v�b�r zbo��
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel pro kter�ho se dialog otev�r�
     * @return seznam s nalezen�m zbo��m
     */
    public static ArrayList<Goods> openDialog(Frame owner, User user) {
        result.clear();
        new StoreDialog(owner, user);
        return result;
    }
    
    /**
     * Otev�e dialog pro v�b�r zbo��
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel pro kter�ho se dialog otev�r�
     * @return seznam s nalezen�m zbo��m
     */
    public static ArrayList<Goods> openDialog(Dialog owner, User user) {
        result.clear();
        new StoreDialog(owner, user);
        return result;
    }

    /**
     * Otev�e dialog pro v�b�r zbo��
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel pro kter�ho se dialog otev�r�
     * @param keyword kl��ov� skovo, kter� slou�� jako filtr
     * @return seznam s nalezen�m zbo��m
     */
    public static ArrayList<Goods> openDialog(Frame owner, User user, String keyword, boolean showZeroCards) {
        result.clear();
        new StoreDialog(owner, user, keyword, showZeroCards);
        return result;
    }

    /**
     * Otev�e dialog pro v�b�r zbo��
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel pro kter�ho se dialog otev�r�
     * @param keyword kl��ov� skovo, kter� slou�� jako filtr
     * @return seznam s nalezen�m zbo��m
     */
    public static ArrayList<Goods> openDialog(Dialog owner, User user, String keyword, boolean showZeroCards) {
        result.clear();
        new StoreDialog(owner, user, keyword, showZeroCards);
        return result;
    }

    /**
     * provede pot�ebn� nastaven� 
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
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
    }
  
    
    /**
     * Vytvo�� obsah okna
     */
    private JComponent getContent() {
        JPanel content = new JPanel( new BorderLayout() );
        URL iconURL;
        Icon imageIcon;
        JButton button;
        
        storePanel = new StorePanel(this, user);
        content.add(storePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Potvrzen� vybr�n� polo�ek"));
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        iconURL = StoreDialog.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Zav��t", imageIcon);
        button.setToolTipText("Zav�e okno bez v�b�ru zbo��");
        button.addActionListener( new CancelButtonListener() );
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);
        
        iconURL = StoreDialog.class.getResource(Settings.ICON_URL + "Properties16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vybrat", imageIcon);
        button.setToolTipText("Vybere ozna�en� zbo��.");
        button.addActionListener( new ConfirmButtonListener() );
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);
        return content;
    }
    
   /**
     *  Poslucha� stisku tla��tka Potvrzen� v�b�ru  
     */
    private class ConfirmButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            
            /* Zjisti onza�en� ��dky */
            ListSelectionModel listSM = storePanel.getGoodsTable().getSelectionModel();
            int firstRow = listSM.getMinSelectionIndex();
            int lastRow = listSM.getMaxSelectionIndex();
            
            // jestli�e nen� nic vybr�no
            if (firstRow == -1) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Vyberte zbo��, kter� chcete vlo�it do p��jemky");
                JOptionPane.showMessageDialog(StoreDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                return;
            }
            
            GoodsTableModel goodsTableModel = storePanel.getGoodsTableModel();
            
            /* Veber p��slu�n� ��dky z tabulky */

            /* Projdi ��dky a vyma� odpov�daj�c� polo�ky z datab�ze*/
            for (int i = firstRow; i <= lastRow; i++) {

                /* Zkontroluj jestli je tento ��dek ozna�en (mezi prvn�m a posledn�m ozna�en�m mohou b�t i neozna�en� */
                if (listSM.isSelectedIndex(i)) {
                    // Vybere z datab�ze zbo�� a po�li k p��jemce 
                    Goods goods = goodsTableModel.getGoodsAt(i);

                    // Dopl� zbo�� mezi v�sledky
                    result.add(goods);
                }
            }


            
            StoreDialog.this.dispose();
        }
    }
    
   /**
     *  Poslucha� stisku tla��tka zru�en� v�b�ru 
     */
    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            StoreDialog.this.dispose();
        }
    }
    
}
