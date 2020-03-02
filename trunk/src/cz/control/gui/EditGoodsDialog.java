/*
 * EditGoodsDialog.java
 *
 * Created on 7. ��jen 2005, 22:59
 */

package cz.control.gui;

import cz.control.errors.ErrorMessages;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.PriceList;
import cz.control.data.Goods;
import cz.control.business.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*; 

import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladov� syst�m
 *
 * T��da vytv��� dialogov� okno pro editace, nebo zaveden� nov� skladov� karty
 * Dialog otev�r� t��da StorePanel
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public final class EditGoodsDialog extends JDialog  implements WindowListener {
    
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private static int MATH_ROUND = 0;
    
    private Store store;
    private User user;
    private Component owner;
    
    private String[] dphBoxItems = Settings.getDphLayers();
    private String[] typeBoxItems = { "Zbo��", "Komplet"};
    private double initialQuantity = 0; // po��te�n� mno�stv� zbo��
    
    /* Jednotliv� vstupn� pole */
    private JTextField idTextField = new JTextField(); 
    private JTextField nameTextField = new JTextField();
    private JComboBox dphTextField = new JComboBox(dphBoxItems);
    private JTextField unitTextField = new JTextField("ks"); 
    private JTextField eanTextField = new JTextField(); 
    private JComboBox typeTextField = new JComboBox(typeBoxItems); // Bude vyu�ito v dal�� verzi
    private JCheckBox usePriceList = new JCheckBox("Pou��t cen�k", true);
    private JButton openPriceList = new JButton("Cen�k");
    
    private EditablePriceTableModel priceTableModel;
    private JTable priceTable;
    
    private Object dphPrevValue = Settings.getHighLayer();
    private boolean appendMode = true; //uchov�v� zda se bude zbo�� dopl�ovat, �i editovat
    private Goods oldGoods; // zbo��, kter� se bude editovat
    private PriceList priceList = null; // cen�k pro v�po�et PC z NC
    private PriceListEditor priceListEditor;
    
    private static Goods resultGoods = null;
            
    /**
      * Vytvo�� dialog pro editaci nebo vytvo�en� zbo��
      * @param store reference na sklad, do kter�ho m� b�t zbo�� dopln�no
      * @param user reference na p�ihl�en�ho u�ivatele
      * @param goods reference na zbo��, kter� se bude v dialogu editovat
      */
    private EditGoodsDialog(Frame owner, User user, Goods goods) {
        super(owner, "Control - Editace skladov� karty", true);
        
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }      
        
        this.user = user;
        this.owner = owner;
        this.initialQuantity = goods.getQuantity();
        
        appendMode = false; // Zbo�� se bude editovat
        oldGoods = goods;
    
        dphPrevValue = new Integer(goods.getDph());
        
        dphTextField.setEditable(true);
        
        /* Nastav hodnoty do ok�nek */
        idTextField.setText(goods.getGoodsID());
        nameTextField.setText(goods.getName());
        dphTextField.setSelectedItem(dphPrevValue); 
        unitTextField.setText(goods.getUnit());
        eanTextField.setText(goods.getEan());

        /* Nastav ceny */
        priceTableModel = new EditablePriceTableModel(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph());
        priceTableModel.setPriceList(null);
        usePriceList.setSelected(false);
        
        setDialog();
    }
    
    /**
      * Vytvo�� dialog pro editaci nebo vytvo�en� zbo��
      * @param store reference na sklad, do kter�ho m� b�t zbo�� dopln�no
      * @param user reference na p�ihl�en�ho u�ivatele
      * @param goods reference na zbo��, kter� se bude v dialogu editovat
      */
    private EditGoodsDialog(Dialog owner, User user, Goods goods) {
        super(owner, "Control - Editace skladov� karty", true);
    
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }        
        
        this.user = user;
        this.owner = owner;
        this.initialQuantity = goods.getQuantity();
        
        appendMode = false; // Zbo�� se bude editovat
        oldGoods = goods;
    
        dphPrevValue = new Integer(goods.getDph());
        
        dphTextField.setEditable(true);
        
        /* Nastav hodnoty do ok�nek */
        idTextField.setText(goods.getGoodsID());
        nameTextField.setText(goods.getName());
        dphTextField.setSelectedItem(dphPrevValue); 
        unitTextField.setText(goods.getUnit());
        eanTextField.setText(goods.getEan());

        /* Nastav ceny */
        priceTableModel = new EditablePriceTableModel(goods.getNc(), goods.getPcA(), goods.getPcB(), goods.getPcC(), goods.getPcD(), goods.getDph());
        priceTableModel.setPriceList(null);
        usePriceList.setSelected(false);
        
        setDialog();
    }    
    
    /*
     * Implementace metod z window listener
     */
    /**
     * Ud�lost aktivov�n� okna 
     * @param e Ud�lost okna
     */
    public void windowActivated(WindowEvent e) {}
    /**
     * Ud�lost zav�en� okna
     * @param e Ud�lost okna
     */
    public void windowClosed(WindowEvent e) {
    }
    /**
     * Ud�lost vyvolan� p�i zav�r�n� okna
     * Provede ulo�en� nastaven�
     * @param e Ud�lost okna
     */
    public void windowClosing(WindowEvent e) {
        cancel();
    }
    /**
     * Ud�lost deaktivov�n� okna
     * @param e Ud�lost okna
     */
    public void windowDeactivated(WindowEvent e) {}
    /**
     * ??
     * @param e Ud�lost okna
     */
    public void windowDeiconified(WindowEvent e) {}
    /**
     * Okno ikonizov�no
     * @param e Ud�lost okna
     */
    public void windowIconified(WindowEvent e) {}
    /**
     * Ud�lost otev�en� okna 
     * @param e Ud�lost okna
     */
    public void windowOpened(WindowEvent e) {}     
    
     /**
      *  Vytvo�� dialog pro editaci nebo vytvo�en� zbo��
      * @param store reference na sklad, do kter�ho m� b�t zbo�� dopln�no
      * @param user reference na p�ihl�en�ho u�ivatele
      */
    private EditGoodsDialog(Frame owner, User user) {
        super(owner, "Vytvo�en� skladov� karty", true);
        
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }      
        
        this.user = user;
        this.owner = owner;
        
        /* Umo�ni p��m� zad�n� dan�*/
        dphTextField.setEditable(true);
        dphTextField.setSelectedItem(dphPrevValue); //implicitn� nastavena hodnota 19%
        priceTableModel = new EditablePriceTableModel(0, 0, 0, 0, 0, Integer.valueOf( String.valueOf(dphTextField.getSelectedItem())) );
        priceTableModel.setPriceList(priceList);
        usePriceList.setSelected(true);
        
        setDialog();
    }
    
     /**
      *  Vytvo�� dialog pro editaci nebo vytvo�en� zbo��
      * @param store reference na sklad, do kter�ho m� b�t zbo�� dopln�no
      * @param user reference na p�ihl�en�ho u�ivatele
      */
    private EditGoodsDialog(Dialog owner, User user) {
        
        super(owner, "Vytvo�en� skladov� karty", true);
        
        try {
            this.store = user.openStore();
            this.priceListEditor = user.openPriceListEditor();
            this.priceList = priceListEditor.getDefaultPriceList();
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }      
        
        this.user = user;
        this.owner = owner;
        
        /* Umo�ni p��m� zad�n� dan�*/
        dphTextField.setEditable(true);
        dphTextField.setSelectedItem(dphPrevValue); //implicitn� nastavena hodnota 19%
        priceTableModel = new EditablePriceTableModel(0, 0, 0, 0, 0, Integer.valueOf( String.valueOf(dphTextField.getSelectedItem())) );
        priceTableModel.setPriceList(priceList);
        usePriceList.setSelected(true);
        
        setDialog();
    }
    
    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {
        dphTextField.setPreferredSize(new Dimension(40, 20));
        dphTextField.addItemListener(new DphChangeListener());
        
        typeTextField.setSelectedItem("Zbo��"); //implicitn� nastavena hodnota "Zbo��"

        /* Vytvo� tabulku a nastav hodnotu DPH */
        priceTable = new CommonTable(priceTableModel);

        this.addWindowListener(this);
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); 
        pack();
        setVisible(true);
        
    }
    
    /**
     * Otev�e dialog. Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     *
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel, pro kter�ho byl dialog otev�eno
     * @return Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu.
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     *
     */
    public static Goods openDialog(Frame owner, User user) {
        resultGoods = null;
        new EditGoodsDialog(owner, user);
        return resultGoods;
    }
    
    /**
     * Otev�e dialog. Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     *
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel, pro kter�ho byl dialog otev�eno
     * @return Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu.
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     *
     */
    public static Goods openDialog(Dialog owner, User user) {
        resultGoods = null;
        new EditGoodsDialog(owner, user);
        return resultGoods;
    }
    
    /**
     * Otev�e dialog. Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     * 
     * 
     * @return Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     * @param oldGoods Star� zbo��, kter� se m� editovat
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel, pro kter�ho byl dialog otev�eno
     */
    public static Goods openDialog(Frame owner, User user, Goods oldGoods) {
        resultGoods = null;
        new EditGoodsDialog(owner, user, oldGoods);
        return resultGoods;
    }
    
    /**
     * Otev�e dialog. Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     * 
     * 
     * @return Vrac� vytvo�en� zbo��, kter� zapsal do datab�ze p�ed uzav�en�m dialogu
     * Nebo null, jestli�e u�ivatel nepotvrdil dialog
     * @param oldGoods Star� zbo��, kter� se m� editovat
     * @param owner Vlastn�k dialogu
     * @param user U�ivatel, pro kter�ho byl dialog otev�eno
     */
    public static Goods openDialog(Dialog owner, User user, Goods oldGoods) {
        resultGoods = null;
        new EditGoodsDialog(owner, user, oldGoods);
        return resultGoods;
    }
    
    /**
      *  Vytvo�� obsah dialogu 
      */
    private Container getContent() {
        JPanel content = new JPanel(new BorderLayout());
        
        JPanel mainPart = new JPanel(); // Obsah hlavn�ho okna
        mainPart.setPreferredSize(new Dimension(430, 350));
        mainPart.add(createMainPanel()); // Vlo� ��st s edit boxy
        mainPart.add(createTablePanel()); // Vlo� tabulku
        
        content.add(mainPart, BorderLayout.CENTER); // Vlo� panel s hlavn� ��st� okna 
        
        JPanel buttonPanel = new JPanel();
            
        JButton button = new JButton("Zru�it");
        button.addActionListener( new CancelButtonListener());
        button.setMnemonic(KeyEvent.VK_BACK_SPACE);
        buttonPanel.add(button);

        button = new JButton("Potvrdit");
        button.addActionListener( new ConfirmButtonListener());
        button.setMnemonic(KeyEvent.VK_ENTER);
        buttonPanel.add(button);
            
        content.add(buttonPanel, BorderLayout.SOUTH);
        return content;
    
    }
    
    /**
     *  Vytvo�� hlavn� panel dialogu
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(gbl);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "�daje o zbo��"));
        mainPanel.setPreferredSize(new Dimension(430, 136));
        
        idTextField.setPreferredSize( new Dimension(250, 20) );
        nameTextField.setPreferredSize( new Dimension(250, 20) );
        dphTextField.setPreferredSize( new Dimension(50, 20) );
        unitTextField.setPreferredSize( new Dimension(250, 20) );
        eanTextField.setPreferredSize( new Dimension(250, 20) );
        
        idTextField.setMinimumSize( new Dimension(250, 20) );
        nameTextField.setMinimumSize( new Dimension(250, 20) );
        dphTextField.setMinimumSize( new Dimension(50, 20) );
        unitTextField.setMinimumSize( new Dimension(250, 20) );

        mainPanel.add(setComponent(new JLabel("Skladov� ��slo:     "), 0, 0, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(idTextField, 1, 0, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("N�zev: "), 0, 1, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(nameTextField, 1, 1, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("EAN: "), 0, 2, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(eanTextField, 1, 2, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("Jednotka: "), 0, 3, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(unitTextField, 1, 3, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(new JLabel("DPH [%]: "), 0, 4, 1, 1, 0.0, 0.0, NONE, NORTHWEST));
        mainPanel.add(setComponent(dphTextField, 1, 4, 1, 1, 1.0, 1.0, NONE, NORTHWEST));
        
        return mainPanel;
    }
    
    /**
     *  Vytvo�� panel s tabulkou cen
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(gbl);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Ceny"));
        tablePanel.setPreferredSize(new Dimension(420, 200));
        
        /* Nastav editory bun�k pro zm�nu ceny*/
        TableColumnModel columnModel = priceTable.getColumnModel();
        columnModel.getColumn(1).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(2).setCellRenderer(new PriceCellRenderer());
        columnModel.getColumn(1).setCellEditor(new PriceCellEditor());
        columnModel.getColumn(2).setCellEditor(new PriceCellEditor());
        columnModel.getColumn(PriceTableColumns.NAME.getNumber()).setCellRenderer(new CommonItemCellRenderer());

        priceTable.setRowSelectionAllowed(false);
        priceTable.setShowVerticalLines(false);
        JScrollPane scrollPane = new JScrollPane(priceTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(350, 130)); //Minimaln� velikost panelu
        
        tablePanel.add(setComponent(scrollPane, 0, 0, 2, 1, 1.0, 0.0, NONE, NORTH));
        
        openPriceList.addActionListener(new OpenPriceListListener());
        openPriceList.setToolTipText("Umo�n� nastavit v�po�et Prodejn�ch cen pro tuto skladovou kartu");
        tablePanel.add(setComponent(openPriceList, 0, 1, 1, 1, 1.0, 1.0, NONE, EAST));
        usePriceList.addActionListener( new UsePriceListListener() );
        usePriceList.addActionListener( new UsePriceListListener() );
        tablePanel.add(setComponent(usePriceList, 1, 1, 1, 1, 1.0, 1.0, NONE, WEST));
        
        return tablePanel;
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
     *  Zru�� dialog bez uloen� zm�n
     */ 
    private void cancel() {
        String text = "Zav��t okno bez ulo�en� zm�n?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Uzav�en� okna",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        }         
        
        resultGoods = null;

        EditGoodsDialog.this.dispose(); // Zav�i dialog
    }
    
    /**
     *  Potvrzen� zm�n ve formul��i
     */
    private void confirm() {
            String id = idTextField.getText();
            String name = nameTextField.getText();
            int type = 0; //Integer.valueOf( String.valueOf(typeTextField.getSelectedItem()) );
            int dph = Integer.valueOf( String.valueOf(dphTextField.getSelectedItem()) );
            String ean = eanTextField.getText();
            String unit = unitTextField.getText();
            
            int column = PriceTableColumns.PRICE.getNumber();
            
//            float d = (Float) priceTableModel.getValueAt(0, column);
//            float ext = d *  Store.CENT;
//            int ii = Math.round(ext);
            BigDecimal
                nc = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(0, column)) ) ).multiply(Store.CENT), 
                pcA = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(1, column)) ) ).multiply(Store.CENT),     
                pcB = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(2, column)) ) ).multiply(Store.CENT),     
                pcC = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(3, column)) ) ).multiply(Store.CENT),     
                pcD = ( new BigDecimal( String.valueOf( priceTableModel.getValueAt(4, column)) ) ).multiply(Store.CENT);   
            
            /* Vytvo� zbo�� */
            resultGoods = new Goods(id, name, type, dph, unit, ean, 
                    nc.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcA.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcB.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcC.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    pcD.setScale(MATH_ROUND, RoundingMode.HALF_UP).intValue(), 
                    initialQuantity);

            /* Pokus se zapsat zbo�� do datab�ze*/
            try {
                if (appendMode) {
                    user.createGoods(resultGoods); // dopl� zbo��
                } else {
                    user.editGoods(oldGoods, resultGoods); // edituj zbo��
                }

                EditGoodsDialog.this.dispose(); // Uzav�i okno

            } catch (InvalidPrivilegException exception) {
                ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                JOptionPane.showMessageDialog(EditGoodsDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            } catch (SQLException exception) {
                ErrorMessages er = ErrorMessages.getErrorMessages(exception);
                JOptionPane.showMessageDialog(EditGoodsDialog.this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            } 
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
     *  Poslucha� zm�ny hodnoty v DPH
     */
    private class DphChangeListener implements ItemListener {
        
        public void itemStateChanged(ItemEvent e) {
            
            /* Zm�nu p�i "odzna�en�" neber v �vahu*/
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            
            try {
                int dph = Integer.valueOf( String.valueOf(e.getItem())); // na�ti dph
                // Jestli�e byla zad�na z�porn� hodnota 
                if (dph < 0) {
                    throw new NumberFormatException("Z�porn� hodnota DPH");
                }
                priceTableModel.changeDph(dph);
                dphPrevValue = e.getItem(); // Ulo� naposled zadanou hodnotu
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(EditGoodsDialog.this, "<html><center>Chybn� zadan� hodnota \"" + e.getItem() + "\". <br>Zad�vejte cel� ��sla v rozsahu 0 - 100 [%]</center></html>", "Neo�ek�van� hodnota", JOptionPane.ERROR_MESSAGE); 
                dphTextField.setSelectedItem(dphPrevValue); // Nastav na posledn� spr�vnou volbu
            }
        }
    }
    
    /**
     *  Zm�na checkboxu ur�uj�c�, zda se m� pou��t cen�k
     */
    private class UsePriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if (usePriceList.isSelected() == true) {
                priceTableModel.setPriceList(priceList);
                priceTableModel.refreschPrices();
            } else {
                priceTableModel.setPriceList(null);
            }
        }

    }
    
    /**
     *  Stisk tla��tka pro otev�en� cen�ku
     */
    private class OpenPriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PriceList tmp = priceList;
            priceList = PriceListDialog.openPriceListDialog(EditGoodsDialog.this, priceList);
            
            // Jestli�e potvrdil dialog, zapni pou��v�n� cen�ku
            if (priceList != null) {
                usePriceList.setSelected(true); 
                priceTableModel.setPriceList(priceList); //z�rove� nastav pou��v�n� cen�ku
                priceTableModel.refreschPrices();
            } else {
                priceList = tmp;
            }
        }
        
    }
            
}
