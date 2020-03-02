package cz.control.gui.tradeshistory;

import cz.control.gui.CommonTable;
import cz.control.business.Settings;
import cz.control.business.Store;
import cz.control.business.User;
import cz.control.data.Goods;
import cz.control.data.GoodsTradesHistory;
import cz.control.data.TradeItemPreview;
import cz.control.errors.ErrorMessages;
import cz.control.errors.InvalidPrivilegException;
import cz.control.gui.MainWindow;
import cz.control.gui.StatusBarTips;
import cz.control.gui.TabbedPaneItems;
import cz.control.gui.common.DateAndQuantityIntervalPanel;
import cz.control.gui.common.LimitsChangedListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author kamilos
 */
public class GoodsTradesDialog extends JDialog implements LimitsChangedListener {
    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 

    private Frame owner;
    private Goods selectedGoods;
    private User user;
    private JTable itemsTable;
    private TradesHistoryTableModel tableModel;
    private JButton transmitButton;
    
    private Date startDate = new Date();
    private Date endDate = new Date();
    private Integer limit;
    private JLabel statusBarTip;

    
    
    /** Creates new form GoodsTradesDialog */
    public GoodsTradesDialog(java.awt.Frame parent, User user, Goods selectedGoods) {
        super(parent, "P�ehled obrat� zbo��", true);
        this.owner = parent;
        this.selectedGoods = selectedGoods;
        this.user = user;
        
        setDialog();
        
    }

    /**
     * provede pot�ebn� nastaven� 
     */
    private void setDialog() {
        
        setLocationRelativeTo(owner);
        setContentPane(getContent());
//        setLocationByPlatform(true);
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(true);
        setMinimumSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));

        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!

        // vlo� hodnoty
        setUpValues();
        
        setVisible(true);
        pack();

    }
    
    /**
      *  Vytvo�� obsah dialogu 
      */
    private Container getContent() {
        JPanel content = new JPanel();

        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        content.setLayout(gbl);
        content.add(setComponent(createTopPanel(), 0, 0, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createFiltrPanel(), 0, 1, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createItemTablePanel(), 0, 2, 1, 1, 1.0, 1.0, BOTH, NORTH));
        content.add(setComponent(createConfirmPanel(), 0, 3, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));
        content.add(setComponent(createStatusBar(), 0, 4, 1, 1, 1.0, 0.0, HORIZONTAL, CENTER));

        return content;
    }    
        
    private Component createFiltrPanel() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH)); //Za��tek
        cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH)); // o rok d��ve
        startDate.setTime(cal.getTimeInMillis());

        
        cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //Konec 
        endDate.setTime(cal.getTimeInMillis());
        limit = 1000;
        
        DateAndQuantityIntervalPanel content = new DateAndQuantityIntervalPanel(startDate, endDate, limit);
        content.addLimitsChangedListener(this);
        
        return content;
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
     * Vytvo�� Horn� panel s editac� odb�ratele a datumu
     */
    private Container createTopPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "" +
                "Zbo��"));
        
        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label = new JLabel(selectedGoods + "");
        label.setFont(font);
        content.add(setComponent(label, 2, 0, 1, 1, 1.0, 0.0, NONE, CENTER));

        return content;
    }
    
    /**
     * Vytvo�� panel s tabulkou, kter� zobrazuje polo�ky p��jemky
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "" +
                "Historie obchodov�n�"));
        
        tableModel = new TradesHistoryTableModel();
        itemsTable = new CommonTable(tableModel); // vytvo�en� tabulky
        itemsTable.setShowVerticalLines(false);  // Nastav neviditeln� vertik�ln� linky v tabulce
        
        ListSelectionModel rowSM = itemsTable.getSelectionModel();
        rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowSM.addListSelectionListener(new SelectRowGoodsHistoryTableListener());
       
        
        TableColumnModel columnModel = itemsTable.getColumnModel();
        /* Nastav ���ky sloupc� */
        for (TradesTableColumns columnMeta: TradesTableColumns.values()) {
            columnModel.getColumn(columnMeta.getColumnNumber()).setPreferredWidth(columnMeta.getWidth()); 
            columnModel.getColumn(columnMeta.getColumnNumber()).setCellRenderer( new TradesHistoryCellRenderer() );
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        
        content.add(scrollPane, BorderLayout.CENTER);
        
        
        return content;
    }
    
    private Component createConfirmPanel() {
        JPanel content = new JPanel(gbl);
        
        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "" +
                "Potvrzen�"));
        
        content.setMinimumSize( new Dimension(10, 50) );
        
        JButton button = new JButton("Zav��t");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        button.setToolTipText("Uzav�e dialog");
        content.add(button);
        button.addActionListener(new CancelButtonListener());
        
        transmitButton = new JButton("Potvrdit a zobrazit");
        transmitButton.setToolTipText("P�eje k p��jemce/v�dejce ke kter� n�le�� v�bran� ��dek");
        content.add(transmitButton);
        transmitButton.addActionListener(new ShowButtonListener());
        transmitButton.setEnabled(false);
        transmitButton.setMnemonic(KeyEvent.VK_ENTER);
        
        
        return content;
    }

    /**
     *  Vytvo�� spodn� ��st okna s nej�ast�ji pou��van�mi tla��tky
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(gbl);
        
        panel.setPreferredSize( new Dimension(50, 15));
        
        statusBarTip = new JLabel(StatusBarTips.CANCEL_CONFIRM.getText());
        panel.add(setComponent(statusBarTip, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        return panel;
    }
    
    
    
    public void refresh(Date startDate, Date endDate, Integer max) {
        this.startDate.setTime(startDate.getTime());
        this.endDate.setTime(endDate.getTime());
        this.limit = new Integer(max);
        
        setUpValues();
    }
    
    /**
     * Nastav� hodnoty do tabulky
     */
    private void setUpValues() {
        try {
            Store store = user.openStore();
            
            List<GoodsTradesHistory> items = store.getTradesHistory(selectedGoods.getGoodsID(), startDate, endDate, limit);
            tableModel.clean();
            tableModel.addRows(items);
            
        } catch (InvalidPrivilegException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
    }    
    
    /**
     * Vol�no p�i zm�ne ozna�en� v tabulce
     * @param selectedRow
     */
    public void refreshInfo(int selectedRow) {
        transmitButton.setEnabled(true);
    }
           
    private void exit() {
        this.dispose();
    }
    
    private void showItemInMainWindow() {

        int selectedRow = itemsTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        GoodsTradesHistory selectedItem = tableModel.getItemAt(selectedRow);

        TradeItemPreview tradeItemPrev;

        try {
            
            int redirectIndex = 0;
            
            //�P�epni na p��slu�n� panel
            switch (selectedItem.getItemType()) {
                case BUY:
                    tradeItemPrev = user.openBuy().getBuy(selectedItem.getItemId());
                    MainWindow.getInstance().getBuyPanel().selectGoods(tradeItemPrev, selectedGoods);
                    redirectIndex = TabbedPaneItems.BUY.getIndex();
                    break;
                case DISCOUNT:
                    tradeItemPrev = user.openSale().getSale(selectedItem.getItemId());
                    MainWindow.getInstance().getDiscountPanel().selectGoods(tradeItemPrev, selectedGoods);
                    redirectIndex = TabbedPaneItems.DISCOUNT.getIndex();
                    break;
                case SALE:
                    tradeItemPrev = user.openSale().getSale(selectedItem.getItemId());
                    MainWindow.getInstance().getSalePanel().selectGoods(tradeItemPrev, selectedGoods);
                    redirectIndex = TabbedPaneItems.SALE.getIndex();
                    break;
            }

            MainWindow.getInstance().getTabbedPane().setSelectedIndex(redirectIndex);
            this.dispose();
            
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
    }
    
    
    /**
     *  Poslucha� v�b�ru ��dky v tabulce zbo�� 
     */
    private class SelectRowGoodsHistoryTableListener implements ListSelectionListener {
        
        public void valueChanged(ListSelectionEvent e) {
           // z�br�n� zdvojen�mu vyvol�n� uud�losti (v�znam mi nen� p�esn� zn�m)
           if (e.getValueIsAdjusting()) return;
            
           ListSelectionModel lsm = (ListSelectionModel) e.getSource(); // z�skej model v�b�ru
           /* Zjisti index na�ten�ho ��dku */
           int selectedRow = lsm.getMinSelectionIndex();
           if ( selectedRow == -1) {
                return;
           }
           
           refreshInfo(selectedRow);
        }
    }    
    
    private class CancelButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            exit();
        }

        
    }
    
    private class ShowButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            showItemInMainWindow();
        }

        
    }

}
