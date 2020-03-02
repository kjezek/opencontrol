/*
 * MainWindow.java
 *
 * Created on 13. z��� 2005, 20:59
 */

package cz.control.gui;

import java.util.Date;
import cz.control.errors.ApplicationException;
import javax.swing.plaf.FontUIResource;
import cz.control.gui.dph.DphGlobalChangeDialog;
import cz.control.gui.about.AboutDialog;
import cz.control.gui.tradeshistory.GoodsTradesDialog;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import cz.control.errors.InvalidPrivilegException;
import cz.control.data.PriceList;
import cz.control.data.ClientType;
import cz.control.data.About;
import cz.control.data.Goods;
import cz.control.business.*;
import cz.control.gui.about.WelcomeDialog;
import java.sql.SQLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 

import java.net.URL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.sf.jasperreports.engine.JRException;

import static java.awt.GridBagConstraints.*;
/**
 * Program Control - Skladov� syst�m
 *
 * Hlavn� t��da prezen�n� logiky. Zobrazuje hlavn� okno programu a vyvol�v� dal�� 
 * dialogov� okna 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class MainWindow extends JFrame implements WindowListener {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
   
    private static String loginUserName = ""; // jm�no p�ihl�en�ho u�ivatele 
    private static User user = null; // Ukazatel na pr�v� p�ihl�en�ho u�ivatele 
    private static MainWindow thisInstance = null; //ukazatel na pr�v� otev�en� okno
    
    private StorePanel storePanel; // panel skladu
    private SuplierPanel suplierPanel; // panel dodavatel�
    private CustomerPanel customerPanel; // panel odb�ratel�
    private BuyPanel buyPanel; // panel p��jemkek
    private SalePanel salePanel; // panel v�dejek
    private SalePanel discountPanel; // panel prodejek
    private JTabbedPane tabbedPane; // panel s obsahem
    private JPanel accountPanel; // panel s u�ivatelsk�mi ��ty
    private StockingsPanel stockingsPanel; //panel s inventurou
    private RecapPanel recapsPanel; // panel s rekapitulac�
    
    private JLabel statusBarTip;
    
    private String lastSearchedGoodsKeyword = ""; // Naposledy hledan� kl��ov� slovo ve skladu
    
    private Licences licence;

    /**
     *  Vr�t� instanci pr�v� otev�en�ho okna 
     *  @return vrac� instanci pr�v� vytvo�en�ho okna, nebo null jestli�e nen�
     *  ��dn� okno vytvo�en�
     */
    static public MainWindow getInstance() { // p��stupn� v r�mci bal�ku, proto m��ou b�t instan�n� metody public 
        return thisInstance;
    }
        
    /**
     *  Otev�e hlavn� okno programu
     */
    public static void openMainWindow() {
        
        try {

            Login login;

            if (Account.getUsersCount() == 0) {
                login = new Login();
            } else {
                /* Provede p�ihl�en� u�ivatele */
                login = LoginDialog.openDialog((Frame) null);
            }

            // Jestli�e stornoval dialog, KONEC proramu
            if (login == null) {
                return;
            }

            //init default font sizes
            Font textFieldFont = new Font("Dialog", Font.PLAIN, Settings.getTextFieldsFontSize());
            UIManager.put("TextField.font", new FontUIResource(textFieldFont));

            Font labelFont = new Font("Dialog", Font.BOLD, Settings.getLabelsFontSize());
            UIManager.put("Label.font", new FontUIResource(labelFont));
            UIManager.put("List.font", new FontUIResource(labelFont));
            UIManager.put("Button.font", new FontUIResource(labelFont));

            loginUserName = login.getUserName();
            user = login.getUser(); // Na�ti p�ihl�en�ho u�ivatele
            /* Otev�i hlan� okno programu */
            thisInstance = new MainWindow();

            // otev�i welcome screen, pokud je po�adov�no
            if (Settings.isShowWelcomeScreen()) {
                new WelcomeDialog(thisInstance);
            }
                        
        } catch (SQLException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
    }
    
    /** 
     *  Vytv��� nov� okno.
     */
    private MainWindow() { // instanci nen� pot�eba vytv��et zvenku 
        this.setTitle("OpenControl - Skladov� syst�m " + Settings.getVersion());
        thisInstance = this;

        this.setPreferredSize(new Dimension(Settings.getMainWindowWidth(), Settings.getMainWindowHeight()));
        this.setMinimumSize(new Dimension(640, 480)); // Toto nastaven� funguje pouze pro Default look and feel
        this.setContentPane(createContent());
        this.setJMenuBar(createMenuBar());
        this.setLocationByPlatform(true);
        this.pack(); 
        
        this.setVisible(true);
        this.addWindowListener(this);
        
        try {
            licence = Licences.get();
        } catch (ApplicationException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
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
        saveSettings();
        MainWindow.this.dispose(); /* Konec programu */
        return;
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
     *  Provede ulo�en� nastaven� 
     */
    private void saveSettings() {
        try {
            Settings.saveSettings();
        } catch (Exception e) {
            ErrorMessages er = new ErrorMessages(Errors.WRITE_SETTINGS, e.getLocalizedMessage());
            JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }
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
    
    /*
     *  Vytvo�� hlavn� menu
     */
    private JMenuBar createMenuBar() {
        JMenuBar mainMenuBar = new JMenuBar();
        JMenu menu, subMenu;
        JMenuItem menuItem, subMenuItem;
        URL iconURL;
        ImageIcon imageIcon;
        
        menu = new JMenu("Soubor");
        menu.setMnemonic(KeyEvent.VK_F);
        mainMenuBar.add(menu);
        /* Plo�ky menu SOUBOR */

        
        subMenu = new JMenu("Tisk");
        menu.add(subMenu);
        /* Plo�ky menu Tisk */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Tisk stavu zbo��", imageIcon);
        subMenuItem.addActionListener( new PrintGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("P�ehled obchod�", imageIcon);
        subMenuItem.addActionListener( new PrintTrades() );
        //subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        menu.addSeparator();
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Stop16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Konec", imageIcon);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        menuItem.addActionListener( new ExitListener() );
        menu.add(menuItem);        
        
        /* Menu Hledat */
        menu = new JMenu("Hledat");
        menu.setMnemonic(KeyEvent.VK_S);
        mainMenuBar.add(menu);
        /* Plo�ky menu Hledat */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Naj�t", imageIcon);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
        menuItem.addActionListener( new FindFirstGoodsListener() );
        menu.add(menuItem);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Naj�t dal��", imageIcon);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("F3"));
        menuItem.addActionListener( new FindNextGoodsListener() );
        menu.add(menuItem);
        
        /*
         *  Menu Funkce
         */
        menu = new JMenu("Funkce");
        menu.setMnemonic(KeyEvent.VK_A);
        mainMenuBar.add(menu);
        
        subMenu = new JMenu("Sklad");
        menu.add(subMenu);
        /* Plo�ky menu sklad */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Import16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("P��jem zbo��", imageIcon);
        subMenuItem.addActionListener( new BuyGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        subMenu.add(subMenuItem);

        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Export16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("V�dej zbo��", imageIcon);
        subMenuItem.addActionListener( new SaleGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
        subMenu.add(subMenuItem);

        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Discount16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Maloobchodn� prodej", imageIcon);
        subMenuItem.addActionListener( new DiscountGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F7"));
        subMenu.add(subMenuItem);
        
        subMenu.addSeparator();
        subMenuItem = new JMenuItem("Nov� skladov� karta", null);
        subMenuItem.addActionListener( new NewGoodsListener());
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        
//        subMenu.addSeparator();
//        subMenuItem = new JMenuItem("Historie skladov� karty", null);
//        subMenuItem.addActionListener( new RecoveryGoodsListener() );
//        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
//        subMenu.add(subMenuItem);
//        
//        subMenuItem = new JMenuItem("Smazan� skladov� karty", null);
//        subMenuItem.addActionListener( new RecoveryDeletedGoodsListener() );
//        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
//        subMenu.add(subMenuItem);

        menu.addSeparator();
        
        menuItem = new JMenuItem("Nov� dodavatel", null);
        menuItem.addActionListener( new NewSuplierListener());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nov� odb�ratel", null);
        menuItem.addActionListener( new NewCustomerListener());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, ActionEvent.CTRL_MASK));
        menu.add(menuItem);

        menu.addSeparator();

        subMenu = new JMenu("Inventura");
        menu.add(subMenu);
        /* Podmenu inventura */
        subMenuItem = new JMenuItem("Zav�d�c� inventura", null);
        subMenuItem.addActionListener( new DoStockingListener(true));
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        iconURL = null;//MainWindow.class.getResource(Settings.ICON_URL + "NewStocking16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("B�n� inventura", imageIcon);
        subMenuItem.addActionListener( new DoStockingListener());
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F8"));
        subMenu.add(subMenuItem);
        subMenuItem = new JMenuItem("Zru�en� nepou��van�ch karet");
        subMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new GoodsCleanupDialog(MainWindow.this, user, new Date());
            }
        });
        
        subMenu.add(subMenuItem);
        

        menu.addSeparator();

        menuItem = new JMenuItem("Hromadn� zm�na DPH");
        menuItem.addActionListener( new DphGlobalChangeListener() );
        menu.add(menuItem);

        menu.addSeparator();
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "x-office-spreadsheet16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Statistiky", imageIcon);
        menuItem.addActionListener( new StatistikListener() );
        menu.add(menuItem);
        
        menu = new JMenu("Volby");
        menu.setMnemonic(KeyEvent.VK_A);
        mainMenuBar.add(menu);
        /* Polo�ky menu Volby */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Preferences16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Nastaveni", imageIcon);
        menuItem.addActionListener( new SettingsListener() );
        menu.add(menuItem);
        
        iconURL = null;//MainWindow.class.getResource(Settings.ICON_URL + "Preferences16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Tvorba cen", imageIcon);
        menuItem.addActionListener( new PriceListListener() );
        menu.add(menuItem);
        
        iconURL = null;//MainWindow.class.getResource(Settings.ICON_URL + "Preferences16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Informace o n�s", imageIcon);
        menuItem.addActionListener( new AboutUsListener() );
        menu.add(menuItem);
        
        /*
         *  Menu Panely
         */
        menu = new JMenu("Panely");
        menu.setMnemonic(KeyEvent.VK_P);
        mainMenuBar.add(menu);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        menuItem = new JRadioButtonMenuItem("Sklad", true);
        menuItem.addActionListener( new ShowStoreListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Dodavatel�");
        menuItem.addActionListener( new ShowSuplierListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Odb�ratel�");
        menuItem.addActionListener( new ShowCustomerListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("P��jemky");
        menuItem.addActionListener( new ShowBuyListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("V�dejky");
        menuItem.addActionListener( new ShowSaleListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Prodejky");
        menuItem.addActionListener( new ShowDiscountListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Inventury");
        menuItem.addActionListener( new ShowStockingListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Uz�v�rky");
        menuItem.addActionListener( new ShowRecapListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("U�ivatelsk� ��ty");
        menuItem.addActionListener( new ShowAccountListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        
        menu = new JMenu("N�pov�da");
        menu.setMnemonic(KeyEvent.VK_N);
        mainMenuBar.add(menu);
        /* Polo�ky menu n�pov�da */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "About16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("O programu", imageIcon);
        menuItem.addActionListener( new AboutListener() );
        menu.add(menuItem);
        
        return mainMenuBar;
    }
    
    /* 
     *  vytvo�� obsah hlavn�ho okna
     */
    private Container createContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        mainPanel.add(createToolBar(), BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        // Nejd��ve nastav z�lo�ky pro pr�zdn� panely
        tabbedPane.addTab("Sklad", null, new JPanel(), "Prohl�en� a editace zbo��");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Dodavatel�", null, new JPanel(), "Prohl�en� a editace dodavatele");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addTab("Odb�ratel�", null, new JPanel(), "Prohl�en� a editace odb�ratele");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.addTab("P��jemky", null, new JPanel(), "Prohl�en� a editace p��jemek");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);        
        
        tabbedPane.addTab("V�dejky", null, new JPanel(), "Prohl�en� a editace v�dejek");
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        tabbedPane.addTab("Prodejky", null, new JPanel(), "Prohl�en� a editace prodejek");
        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
        
        tabbedPane.addTab("Inventury", null, new JPanel(), "Prohl�en�, editace a proveden� inventur");
        tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);

        tabbedPane.addTab("Uz�v�rky", null, new JPanel(), "M�s��n� a ro�n� uz�v�rky skladu");
        tabbedPane.setMnemonicAt(7, KeyEvent.VK_8);

        tabbedPane.addTab("U�ivatelsk� ��ty", null, new JPanel(), "Nastaven� u�ivatelsk�ch ��t�");
        tabbedPane.setMnemonicAt(8, KeyEvent.VK_9);    
        
        //Do pr�zdn�ch panel� vlo� konkr�tn� panely
        //to je pot�eba proto, aby byly napln�ny i panely, kter� posd�ji
        //nebudou zp��stupn�ny ur�it�m u�ivatel�m
        JPanel storePanel = createStorePanel();
        suplierPanel = new SuplierPanel(this, user);
        customerPanel = new CustomerPanel(this, user);
        buyPanel = new BuyPanel(this, user);
        salePanel = SalePanel.createPanelForSale(this, user);
        discountPanel = SalePanel.createPanelForDiscount(this, user);
        stockingsPanel = new StockingsPanel(this, user);
        recapsPanel = new RecapPanel(this, user);
        accountPanel = createAccountPanel();
        
        tabbedPane.setComponentAt(TabbedPaneItems.STORE.getIndex(), storePanel);
        tabbedPane.setComponentAt(TabbedPaneItems.SUPLIERS.getIndex(), suplierPanel);
        tabbedPane.setComponentAt(TabbedPaneItems.CUSTOMERS.getIndex(), customerPanel);
        tabbedPane.setComponentAt(TabbedPaneItems.BUY.getIndex(), buyPanel);
        tabbedPane.setComponentAt(TabbedPaneItems.SALE.getIndex(),getSalePanel());
        tabbedPane.setComponentAt(TabbedPaneItems.DISCOUNT.getIndex(), discountPanel );
        tabbedPane.setComponentAt(TabbedPaneItems.STOCKING.getIndex(), stockingsPanel);
        tabbedPane.setComponentAt(TabbedPaneItems.RECAP.getIndex(), recapsPanel);
        tabbedPane.setComponentAt(TabbedPaneItems.ACCOUNT.getIndex(), accountPanel);
        
        //Z�ejm� budu v budoucnu �e�it refresh jinak 
        //tabbedPane.addChangeListener( new TabbedPaneChangeListener() );
        
        return mainPanel;
    }
    
    /*
     *  Vytvo�� n�strojovou li�tu 
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        toolBar.setFocusable(false);
        
        /* Tla��tko Nov� polo�ka */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "New22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Vytvo�� novou skladovou kartu");
        button.addActionListener( new NewGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Editace polo�ky */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Edit22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Uprav� skladovou kartu ozna�en�ho zbo��");
        button.addActionListener( new EditGoodsListener());
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Smaz�n� polo�ky */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Delete22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Vyma�e vybran� skladov� karty");
        button.addActionListener( new DelGoodsListener());
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Nalezen� */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Zoom22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Nalezne zbo�� ve skladu");
        button.addActionListener( new FindGoodsListener());
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Tisk */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Print22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Vytiskne p�ehled skladov�ch z�sob");
        button.addActionListener( new PrintGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko p�ehled obchodov�n� */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "bookwi-22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Zobraz� p�ehled obchodov�n� se zbo��m");
        button.addActionListener( new TradesGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Obnovit */
//        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "face-smile22.png");
//        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
//        button = new JButton(imageIcon);
//        button.setToolTipText("Zobraz� historii zbo��");
//        button.addActionListener( new RecoveryGoodsListener() );
//        button.setFocusable(false);
//        toolBar.add(button);
        
        toolBar.addSeparator();
        
        /* Tla��tko P��jem */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Import22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("P��jem zbo�� na sklad");
        button.addActionListener( new BuyGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Nastaven� */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Export22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("V�dej zbo�� odb�rateli");
        button.addActionListener( new SaleGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tla��tko Nastaven� */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Discount22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Maloobchodn� prodej");
        button.addActionListener( new DiscountGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        /* Tla��tko Nastaven� */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Preferences22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Otev�e okno s nastaven�m");
        button.addActionListener( new SettingsListener());
        button.setFocusable(false);
        toolBar.add(button);
                
        /* Tla��tko O programu */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "About24.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.addActionListener( new AboutListener() );
        button.setToolTipText("Informace o autorovi");
        button.setFocusable(false);
        toolBar.add(button);
        
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        return toolBar;
    }
    
    /*
     *  Vytvo�� obsah z�lo�ky Sklad
     */
    private JPanel createStorePanel() {
        JPanel content = new JPanel(new BorderLayout());
        JPanel bottomPart = new JPanel(); // Panel s tla��tky na spodu okna
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Import16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("P��jmout", imageIcon);
        button.setToolTipText("P�ijme zbo�� na sklad");
        button.addActionListener( new BuyGoodsListener() );
        bottomPart.add(button);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Export16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vydat", imageIcon);
        button.setToolTipText("V�d� zbo�� ze skladu");
        button.addActionListener( new SaleGoodsListener() );
        bottomPart.add(button);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Discount16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Maloobchod", imageIcon);
        button.setToolTipText("Maloobchodn� prodej zbo��");
        button.addActionListener( new DiscountGoodsListener() );
        bottomPart.add(button);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Tisk stavu", imageIcon);
        button.setToolTipText("Vytiskne seznam zbo�� na sklad�");
        button.addActionListener( new PrintGoodsListener() );
        bottomPart.add(button);
        
        storePanel = new StorePanel(this, user);
        content.add(storePanel, BorderLayout.CENTER);
        content.add(bottomPart, BorderLayout.SOUTH);
            
        return content;
    }
    
    
    /**
     *  Vytvo�� obsah z�lo�ky U�ivatelsk� ��ty 
     */
    private JPanel createAccountPanel() {
        // Podle p�ihl�en�ho u�ivatel vytvo�� p��slu�n� panel s u�ivatelsk�mi ��ty
        if (user.getClient().getType() == ClientType.MANAGER.getOrdinal()) {
            accountPanel = new AccountManagerPanel(this, user);
        } else {
            accountPanel = new AccountCommonPanel(this, user);
        }
        
        return accountPanel;
    }
    
    /**
     *  Stavov� ��dek
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(gbl);
        
        panel.setPreferredSize( new Dimension(100, 15));
        
        statusBarTip = new JLabel();
        panel.add(setComponent(statusBarTip, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        JLabel label = new JLabel("P�ihl�en: ");
        panel.add(setComponent(label, 1, 0, 1, 1, 0.0, 0.0, HORIZONTAL, EAST));
                
        JLabel loginLabel = new JLabel(loginUserName + " ");
        Font font = new Font("Tahoma", Font.ITALIC | Font.BOLD, Settings.getMainItemsFontSize());
        loginLabel.setForeground( new Color(0x0942A1) );
        loginLabel.setFont(font);
        panel.add(setComponent(loginLabel, 2, 0, 1, 1, 0.0, 0.0, HORIZONTAL, EAST));

        return panel;
    }
    
    /*
     * Vrac� ukazatel na panel, kter� je vytvo�en v hlavn�m okn�
     */
    /**
     * Vrac� panel se skalem, kter� byl vytvo�en v hlavn�m okn�
     * @return Panel se skladem
     */
    public StorePanel getStorePanel() {
        return storePanel;
    }
    

    /**
     *  Vrac� panel s dodavateli, kter� byl vytvo�en v hlavn�m okn�
     */
    public SuplierPanel getSuplierPanel() {
        return suplierPanel;
    }

    /**
     *  Vrac� panel s odb�rateli, kter� byl vytvo�en v hlavn�m okn�
     */
    public CustomerPanel getCustomerPanel() {
        return customerPanel;
    }
    
    /**
     * Vrac� panel s p��jemkami, kter� byl vytvo�en v hlavn�m okn� 
     * @return Panel s p��jemkami
     */
    public BuyPanel getBuyPanel() {
        return buyPanel;
    } 
    
    /**
     * Vrac� panel s v�dejkami, kter� byl vytvo�en v hlavn�m okn� 
     * @return Panel s v�dejkami
     */
    public SalePanel getSalePanel() {
        return salePanel;
    } 
    
    /**
     * Vrac� panel s inveturami, kter� byl vytvo�en v hlavn�m okn� 
     * @return Panel s inventurami
     */
    public StockingsPanel getStockingsPanel() {
        return stockingsPanel;
    }

    /**
     *  Vrac� objekt tvo��c� z�lo�ky s obsahem 
     *  @return objek z�lo�ek
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
    
    /**
     *  Otev�e dialog s nastaven�m programu
     */
//    private void openSettingsDialog() {
//        SettingsDialog.openSettingsDialog(this);
//    }
    
    /**
     *  Vyhled� zbo�� ve sklad�
     */ 
    private void findGoods() {
        // P�epni na panel sklad
        tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
        
        FindGoodsDialog fgd = FindGoodsDialog.openFindDialog(MainWindow.this);
        String keyword = FindGoodsDialog.getKeyword();
        lastSearchedGoodsKeyword = keyword;
        
        // Jestli�e u�ivatel nic nezadal, nic nevyhled�vej
        if (keyword.trim().length() == 0) {
            return;
        }

        // Podle zp�dobu zav�en� prove� p��slu�nou akci
        if (fgd.getTypeOfClose() == FindGoodsDialog.FIRST_CLOSE) {
            storePanel.findFirstGoods(keyword);
        }
        if (fgd.getTypeOfClose() == FindGoodsDialog.NEXT_CLOSE) {
            storePanel.findNextGoods(keyword);
        }
        
    }
    
    /**
     *  Vytvo�� nov� cen�k. Na�te z datab�ze v�choz� cen�k,
     *  vyvol� dialog pro zm�nu cen�ku a p��padnou zm�nu zap�e do datab�ze
     */
    private void editPriceList() {
        
        try {
            PriceList oldPriceList = user.openPriceListEditor().getDefaultPriceList();
            PriceList result = PriceListDialog.openPriceListDialog(MainWindow.this, oldPriceList);
            
            // Jestli�e u�ivatel potvrdil dialog, zapi� do datab�ze cen�k
            if (result != null) {
                // Jestli�e je star� cen�k "pr�zdn�""
                if (oldPriceList.getId() == -1) {
                    user.createPriceList(result); // Vytvo� nov� cen�k
                } else {
                    user.editPriceList(oldPriceList, result); // jinak nahra� za star�
                }
                
                JOptionPane.showMessageDialog(this, "<html><center>Zm�na cen�ku provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
            }
            
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 

            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }        
    }
    
    /**
     *  Zm�na informac� o n�s
     */
    private void editAbout() {
        
        try {
            AboutEditor aboutEditor = user.openAboutEditor();
            About about = aboutEditor.getMainAbout();
            About result = EditAboutDialog.openDialog(this, about);
            
            if (result == null) {
                return;
            }
            
            // Rozhodni zda ji� existuje v datab�zi star� z�znam
            if (about.getCustomer().getId() == -1) {
                user.createAbout(result); // neexistuje - vytvo� nov�
            } else {
                user.editAbout(about, result); //existuje - prove� zm�nu
            }

            JOptionPane.showMessageDialog(this, "<html><center>Zm�na �daj� provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 

            return;
        } catch (InvalidPrivilegException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        } 
    }
    
    /**
     * Provede tisk skladov�ch z�sob
     */
    public void printStore() {
        String text = "Vytisknout p�ehled skladov�ch z�sob?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // ��dna vlastn� ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestli�e nebyl v�b�r potvrzen - konec
        } 
        
        try {
            Print.printStore(Settings.isShowZeroCards());
        } catch (JRException ex) {
            ErrorMessages er = new ErrorMessages(Errors.PRINT_ERROR, ex.getLocalizedMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        } catch (SQLException exception) {
            ErrorMessages er = ErrorMessages.getErrorMessages(exception);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
        }

    }
    
    /**
     * Zobraz� dialog s histori� obchodov�n� se zbo��m
     * @param goods
     */
    private void showTradesWithGoods(Goods goods) {
        
        //�zobraz� mod�ln� dialog
        new GoodsTradesDialog(this, user, goods);
    }
    
    
    /**
     *  Otev�e dialog a vytiskne p�ehled p��jemek/nebo v�dejek za zvolen� obdob�
     */
    private void printTrades() {
       PrintTradesRecapDialog.openDiaog(this);
    }

    public SalePanel getDiscountPanel() {
        return discountPanel;
    }
    
    /**
     *  Poslucha� vb�ru Konec 
     */
    private class ExitListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            MainWindow.this.dispose();
        }
        
    }
    
    /**
     * Poslucha� v�b�ru About z menu
     */
    private class AboutListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            new AboutDialog(MainWindow.this);
        }
        
    }
    
    /**
     *  Poslucha� v�b�ru vytvo�en� p��jemky
     */
    private class BuyGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            buyPanel.newItem();
            //new DoBuyDialog(MainWindow.this, user);
        }
    }
    
    /**
     *  Poslucha� v�b�ru vytvo�en� v�dejky
     */
    private class SaleGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            getSalePanel().newItem();
            //new DoSaleDialog(MainWindow.this, user);
        }
    }
    
    /**
     *  Poslucha� v�b�ru maloobchod
     */
    private class DiscountGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            new DiscountDialog(MainWindow.this, user);
        }
    }
    
    /**
     *  Poslucha� v�b�ru inventuru
     */
    private class DoStockingListener implements  ActionListener {
        private boolean newGoods = false;
        
        public DoStockingListener() {
            newGoods = false;
        }
        
        public DoStockingListener(boolean newGoods) {
            this.newGoods = newGoods;
        }
        
        public void actionPerformed(ActionEvent e) {
            new DoStockingDialog(MainWindow.this, user, newGoods);
        }
    }
    
    /**
     *  Poslucha� v�b�ru Nov� dodavatel
     */
    private class NewCustomerListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            customerPanel.newItem(); // Vytvo� nov�ho dodavatele
        }
    }    
    
    /**
     *  Poslucha� v�b�ru Nov� odb�ratel
     */
    private class NewSuplierListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            suplierPanel.newItem(); // Vytvo� nov�ho dodavatele
        }
    }   
    
    /**
     *  Poslucha� v�b�ru Nov� skladov� karta
     */
    private class NewGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Vytvo� novou polo�ku
            storePanel.newItem();
        }
    }
    
    /**
     *  Poslucha� v�b�ru Editace skladov� karty
     */
    private class EditGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // P�epni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
            // Vytvo� novou polo�ku
            storePanel.editItem();
        }
    }
    
    /**
     *  Poslucha� v�b�ru Smaz�n� skladov� karty
     */
    private class DelGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // P�epni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
            // Vytvo� novou polo�ku
            storePanel.deleteItem();
        }
    }
    
    /**
     *  Poslucha� v�b�ru Nalezen� skladov� karty
     */
    private class FindGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // P�epni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
            
            findGoods(); // Vyhledej zbo�� 
        }
    }
    
    /**
     *  Poslucha� v�b�ru Nalezen� skladov� karty
     */
    private class FindFirstGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            findGoods();
        }
    }
    
    /**
     *  Poslucha� v�b�ru Nalezen� skladov� karty
     */
    private class FindNextGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Jestli�e u�ivatel nic nezadal, nic nevyhled�vej
            if (lastSearchedGoodsKeyword.trim().length() == 0) {
                return;
            }
            
            // P�epni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());

            storePanel.findNextGoods(lastSearchedGoodsKeyword);
        }
    }
    
    /**
     *  Poslucha� v�b�ru tisk
     */
    private class PrintGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            printStore();
        }
    }
    
    /**
     *  Poslucha� v�b�ru p�ehledu obchodov�n� se zbo��m
     */
    private class TradesGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            Goods selected = storePanel.getFirstSelectedGoods();
            if (selected == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Vyberte zbo�� u kter�ho chcete zobrazit historii obchodov�n�");
                JOptionPane.showMessageDialog(MainWindow.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                
                return;
            }
            
            showTradesWithGoods(selected);
        }
    }
    
 

    /**
     * Hromadn� zm�na DPH
     */
    private class DphGlobalChangeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            new DphGlobalChangeDialog(MainWindow.this, user);
        }

    }

    /**
     *  Poslucha� vytvo�en� statistiky
     */
    private class StatistikListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new StatistikDialog(MainWindow.this, user);
        }
        
    }
    
    /**
     *  Poslucha� v�b�ru Nastaven�
     */
    private class SettingsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SettingsDialog.openSettingsDialog(MainWindow.this);
        }
        
    }
    
    /**
     *  Poslucha� v�b�ru Tvorba cen
     */
    private class PriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            editPriceList();
        }
    }
        
    
    /**
     *  Poslucha� v�b�ru Informace o n�s
     */
    private class AboutUsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            editAbout();
        }
    }
    
    /**
     *  Poslucha� v�b�ru Tisk obchod�
     */
    private class PrintTrades implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            printTrades();
        }
    }    
    
    /**
     *  Poslucha� zm�ny v tabbedpane
     */
    private class TabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            int tabIndex = tabbedPane.getSelectedIndex();
            
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.STORE.getIndex()) {
                storePanel.refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.CUSTOMERS.getIndex()) {
                customerPanel.refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.SUPLIERS.getIndex()) {
                suplierPanel.refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.BUY.getIndex()) {
                buyPanel.refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.SALE.getIndex()) {
                getSalePanel().refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.STOCKING.getIndex()) {
                stockingsPanel.refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.RECAP.getIndex()) {
                recapsPanel.refresh();
            }
            // Rozhodni, kter� z�lo�ka je zobrazena a tu znovuna�ti
            if (tabIndex == TabbedPaneItems.ACCOUNT.getIndex()) {
                if (accountPanel instanceof AccountCommonPanel) {
                    ((AccountCommonPanel) accountPanel).refresh();
                }
                if (accountPanel instanceof AccountManagerPanel)
                    ((AccountManagerPanel) accountPanel).refresh();
            }
        }
    }
    
    private class ShowStoreListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
        }
    }

    private class ShowCustomerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.CUSTOMERS.getIndex());
        }
    }

    private class ShowSuplierListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.SUPLIERS.getIndex());
        }
    }

    private class ShowBuyListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.BUY.getIndex());
        }
    }

    private class ShowSaleListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.SALE.getIndex());
        }
    }
    
    private class ShowDiscountListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.DISCOUNT.getIndex());
        }
    }    

    private class ShowStockingListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.STOCKING.getIndex());
        }
    }

    private class ShowRecapListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.RECAP.getIndex());
        }
    }

    private class ShowAccountListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(TabbedPaneItems.ACCOUNT.getIndex());
        }
    }

    public void setStatusBarTip(StatusBarTips tips) {
        this.statusBarTip.setText(tips.getText());
    }

    public Licences getLicence() {
        return licence;
    }


      
}
 