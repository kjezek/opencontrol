package cz.control.gui;

import java.awt.Window;
import java.sql.SQLException;
import cz.control.data.Goods;
import java.util.List;
import java.util.Calendar;
import java.awt.Font;
import cz.control.business.Settings;
import cz.control.business.Store;
import cz.control.business.User;
import cz.control.errors.ErrorMessages;
import cz.control.errors.InvalidPrivilegException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import static java.awt.GridBagConstraints.*;

/**
 * Dialog pro vymazání nepoužívaných nulových karet.
 * 
 * 
 * @author Kamil Ježek
 */
public class GoodsCleanupDialog extends JDialog {

    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints();
    private Window owner;
    private User user;
    private JTable itemsTable;
    private GoodsCleanupTableModel tableModel;
    private JButton transmitButton;
    private JLabel statusBarTip;
    private Date refDate;
    private Date startDate;

    /** Creates new form GoodsTradesDialog */
    public GoodsCleanupDialog(java.awt.Window parent, User user, Date refDate) {
        super(parent);
        super.setTitle("Pøehled nepoužívaných skladových karet");
        super.setModal(true);
        this.owner = parent;
        this.user = user;
        this.refDate = refDate;

        Calendar cal = new GregorianCalendar();
        cal.setTime(refDate);
        cal.add(Calendar.YEAR, -1);

        this.startDate = cal.getTime();

        setDialog();

    }

    /**
     * provede potøebné nastavení 
     */
    private void setDialog() {

        setLocationRelativeTo(owner);
        setContentPane(getContent());
//        setLocationByPlatform(true);

        if (owner != null) {
            setLocation(owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }

        setResizable(true);
        setMinimumSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));

        setPreferredSize(new Dimension(Settings.getDialogWidth(), Settings.getDialogHeight()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!

        // vlož hodnoty
        setUpValues();

        setVisible(true);
        pack();

    }

    /**
     *  Vytvoøí obsah dialogu 
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
        return new JPanel();
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
     * Vytvoøí Horní panel s editací odbìratele a datumu
     */
    private Container createTopPanel() {
        JPanel content = new JPanel(gbl);

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), ""
                + "Vymazat zboží k datu"));

        Font font = new Font("Times", Font.BOLD, Settings.getMainItemsFontSize());
        JLabel label = new JLabel("Karty s nulovým pohybem mezi daty: "
                + new SimpleDateFormat("d.MM.yyyy").format(startDate)
                + " - " + new SimpleDateFormat("d.MM.yyyy").format(refDate));
        label.setFont(font);
        content.add(setComponent(label, 2, 0, 1, 1, 1.0, 0.0, NONE, CENTER));

        return content;
    }

    /**
     * Vytvoøí panel s tabulkou, která zobrazuje položky pøíjemky
     */
    private Container createItemTablePanel() {
        JPanel content = new JPanel(new BorderLayout());

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), ""
                + "Seznam nevyužívaného zboží"));

        tableModel = new GoodsCleanupTableModel();
        itemsTable = new CommonTable(tableModel); // vytvoøení tabulky

        JScrollPane scrollPane = new JScrollPane(itemsTable);

        content.add(scrollPane, BorderLayout.CENTER);


        return content;
    }

    private Component createConfirmPanel() {
        JPanel content = new JPanel(gbl);

        content.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), ""
                + "Potvrzení"));

        content.setMinimumSize(new Dimension(10, 50));

        JButton button = new JButton("Zavøít");
        button.setMnemonic(KeyEvent.VK_CANCEL);
        button.setToolTipText("Uzavøe dialog");
        content.add(button);
        button.addActionListener(new CancelButtonListener());

        transmitButton = new JButton("Potvrdit");
        transmitButton.setToolTipText("Vymaže oznaèené nepoužívané zboží.");
        content.add(transmitButton);
        transmitButton.addActionListener(new SubmmitButtonListener());
        transmitButton.setMnemonic(KeyEvent.VK_ENTER);


        return content;
    }

    /**
     *  Vytvoøí spodní èást okna s nejèastìji používanými tlaèítky
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(gbl);

        panel.setPreferredSize(new Dimension(50, 15));

        statusBarTip = new JLabel(StatusBarTips.CANCEL_CONFIRM.getText());
        panel.add(setComponent(statusBarTip, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));

        return panel;
    }

    /**
     * Nastaví hodnoty do tabulky
     */
    private void setUpValues() {
        try {
            Store store = user.openStore();
            List<Goods> goods = store.getUnusedGoods(startDate, refDate);
            tableModel.setGoodsData(goods);
        } catch (InvalidPrivilegException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void exit() {
        this.dispose();
    }

    private void submitButton() {

        List<Goods> selectedGoods = tableModel.getSelectedGoods();
        
        if (selectedGoods.isEmpty()) {
            this.dispose();
            return;
        }
        
        String text = "Opravdu chcete vymazat oznaèené skladové karty?"
                + "\n" + selectedGoods.size() + " celkem.";
        Object[] options = {"Ano", "Ne"};


        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Smazání skladových karet",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
        }

        /* Proveï fyzické vymazání */
        try {
            user.deleteGoods(selectedGoods);

            MainWindow.getInstance().getStorePanel().refresh();
            this.dispose(); // Zavøi dialog        
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(GoodsCleanupDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(GoodsCleanupDialog.this, er.getFormatedText(), er.getTextCode(), JOptionPane.ERROR_MESSAGE);
        }
        

    }

    private class CancelButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            exit();
        }
    }

    private class SubmmitButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            submitButton();
        }
    }
}
