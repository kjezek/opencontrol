/*
 * MainWindow.java
 *
 * Created on 13. záøí 2005, 20:59
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
 * Program Control - Skladový systém
 *
 * Hlavní tøída prezenèní logiky. Zobrazuje hlavní okno programu a vyvolává další 
 * dialogová okna 
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class MainWindow extends JFrame implements WindowListener {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
   
    private static String loginUserName = ""; // jméno pøihlášeného uživatele 
    private static User user = null; // Ukazatel na právì pøihlášeného uživatele 
    private static MainWindow thisInstance = null; //ukazatel na právì otevøené okno
    
    private StorePanel storePanel; // panel skladu
    private SuplierPanel suplierPanel; // panel dodavatelù
    private CustomerPanel customerPanel; // panel odbìratelù
    private BuyPanel buyPanel; // panel pøíjemkek
    private SalePanel salePanel; // panel výdejek
    private SalePanel discountPanel; // panel prodejek
    private JTabbedPane tabbedPane; // panel s obsahem
    private JPanel accountPanel; // panel s uživatelskými úèty
    private StockingsPanel stockingsPanel; //panel s inventurou
    private RecapPanel recapsPanel; // panel s rekapitulací
    
    private JLabel statusBarTip;
    
    private String lastSearchedGoodsKeyword = ""; // Naposledy hledané klíèové slovo ve skladu
    
    private Licences licence;

    /**
     *  Vrátí instanci právì otevøeného okna 
     *  @return vrací instanci právì vytvoøeného okna, nebo null jestliže není
     *  žádné okno vytvoøené
     */
    static public MainWindow getInstance() { // pøístupnì v rámci balíku, proto mùžou být instanèní metody public 
        return thisInstance;
    }
        
    /**
     *  Otevøe hlavní okno programu
     */
    public static void openMainWindow() {
        
        try {

            Login login;

            if (Account.getUsersCount() == 0) {
                login = new Login();
            } else {
                /* Provede pøihlášení uživatele */
                login = LoginDialog.openDialog((Frame) null);
            }

            // Jestliže stornoval dialog, KONEC proramu
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
            user = login.getUser(); // Naèti pøihlášeného uživatele
            /* Otevøi hlaní okno programu */
            thisInstance = new MainWindow();

            // otevøi welcome screen, pokud je požadováno
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
     *  Vytváøí nové okno.
     */
    private MainWindow() { // instanci není potøeba vytváøet zvenku 
        this.setTitle("OpenControl - Skladový systém " + Settings.getVersion());
        thisInstance = this;

        this.setPreferredSize(new Dimension(Settings.getMainWindowWidth(), Settings.getMainWindowHeight()));
        this.setMinimumSize(new Dimension(640, 480)); // Toto nastavení funguje pouze pro Default look and feel
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
     * Událost aktivování okna 
     * @param e Událost okna
     */
    public void windowActivated(WindowEvent e) {}
    /**
     * Událost zavøení okna
     * @param e Událost okna
     */
    public void windowClosed(WindowEvent e) {
    }
    /**
     * Událost vyvolaná pøi zavírání okna
     * Provede uložení nastavení
     * @param e Událost okna
     */
    public void windowClosing(WindowEvent e) {
        saveSettings();
        MainWindow.this.dispose(); /* Konec programu */
        return;
    }
    /**
     * Událost deaktivování okna
     * @param e Událost okna
     */
    public void windowDeactivated(WindowEvent e) {}
    /**
     * ??
     * @param e Událost okna
     */
    public void windowDeiconified(WindowEvent e) {}
    /**
     * Okno ikonizováno
     * @param e Událost okna
     */
    public void windowIconified(WindowEvent e) {}
    /**
     * Událost otevøení okna 
     * @param e Událost okna
     */
    public void windowOpened(WindowEvent e) {}     
    
    /**
     *  Provede uložení nastavení 
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
    
    /*
     *  Vytvoøí hlavní menu
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
        /* Pložky menu SOUBOR */

        
        subMenu = new JMenu("Tisk");
        menu.add(subMenu);
        /* Pložky menu Tisk */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Tisk stavu zboží", imageIcon);
        subMenuItem.addActionListener( new PrintGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Pøehled obchodù", imageIcon);
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
        /* Pložky menu Hledat */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Najít", imageIcon);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
        menuItem.addActionListener( new FindFirstGoodsListener() );
        menu.add(menuItem);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("Najít další", imageIcon);
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
        /* Pložky menu sklad */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Import16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Pøíjem zboží", imageIcon);
        subMenuItem.addActionListener( new BuyGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        subMenu.add(subMenuItem);

        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Export16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Výdej zboží", imageIcon);
        subMenuItem.addActionListener( new SaleGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
        subMenu.add(subMenuItem);

        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Discount16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Maloobchodní prodej", imageIcon);
        subMenuItem.addActionListener( new DiscountGoodsListener() );
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F7"));
        subMenu.add(subMenuItem);
        
        subMenu.addSeparator();
        subMenuItem = new JMenuItem("Nová skladová karta", null);
        subMenuItem.addActionListener( new NewGoodsListener());
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        
//        subMenu.addSeparator();
//        subMenuItem = new JMenuItem("Historie skladové karty", null);
//        subMenuItem.addActionListener( new RecoveryGoodsListener() );
//        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
//        subMenu.add(subMenuItem);
//        
//        subMenuItem = new JMenuItem("Smazané skladové karty", null);
//        subMenuItem.addActionListener( new RecoveryDeletedGoodsListener() );
//        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
//        subMenu.add(subMenuItem);

        menu.addSeparator();
        
        menuItem = new JMenuItem("Nový dodavatel", null);
        menuItem.addActionListener( new NewSuplierListener());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nový odbìratel", null);
        menuItem.addActionListener( new NewCustomerListener());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, ActionEvent.CTRL_MASK));
        menu.add(menuItem);

        menu.addSeparator();

        subMenu = new JMenu("Inventura");
        menu.add(subMenu);
        /* Podmenu inventura */
        subMenuItem = new JMenuItem("Zavádìcí inventura", null);
        subMenuItem.addActionListener( new DoStockingListener(true));
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, ActionEvent.CTRL_MASK));
        subMenu.add(subMenuItem);
        iconURL = null;//MainWindow.class.getResource(Settings.ICON_URL + "NewStocking16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        subMenuItem = new JMenuItem("Bìžná inventura", imageIcon);
        subMenuItem.addActionListener( new DoStockingListener());
        subMenuItem.setAccelerator(KeyStroke.getKeyStroke("F8"));
        subMenu.add(subMenuItem);
        subMenuItem = new JMenuItem("Zrušení nepoužívaných karet");
        subMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new GoodsCleanupDialog(MainWindow.this, user, new Date());
            }
        });
        
        subMenu.add(subMenuItem);
        

        menu.addSeparator();

        menuItem = new JMenuItem("Hromadná zmìna DPH");
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
        /* Položky menu Volby */
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
        menuItem = new JMenuItem("Informace o nás", imageIcon);
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
        
        menuItem = new JRadioButtonMenuItem("Dodavatelé");
        menuItem.addActionListener( new ShowSuplierListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Odbìratelé");
        menuItem.addActionListener( new ShowCustomerListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Pøíjemky");
        menuItem.addActionListener( new ShowBuyListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Výdejky");
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
        
        menuItem = new JRadioButtonMenuItem("Uzávìrky");
        menuItem.addActionListener( new ShowRecapListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Uživatelské úèty");
        menuItem.addActionListener( new ShowAccountListener() );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.CTRL_MASK));
        buttonGroup.add(menuItem);
        menu.add(menuItem);
        
        
        menu = new JMenu("Nápovìda");
        menu.setMnemonic(KeyEvent.VK_N);
        mainMenuBar.add(menu);
        /* Položky menu nápovìda */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "About16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        menuItem = new JMenuItem("O programu", imageIcon);
        menuItem.addActionListener( new AboutListener() );
        menu.add(menuItem);
        
        return mainMenuBar;
    }
    
    /* 
     *  vytvoøí obsah hlavního okna
     */
    private Container createContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        mainPanel.add(createToolBar(), BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        // Nejdøíve nastav záložky pro prázdné panely
        tabbedPane.addTab("Sklad", null, new JPanel(), "Prohlížení a editace zboží");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Dodavatelé", null, new JPanel(), "Prohlížení a editace dodavatele");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addTab("Odbìratelé", null, new JPanel(), "Prohlížení a editace odbìratele");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.addTab("Pøíjemky", null, new JPanel(), "Prohlížení a editace pøíjemek");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);        
        
        tabbedPane.addTab("Výdejky", null, new JPanel(), "Prohlížené a editace výdejek");
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        tabbedPane.addTab("Prodejky", null, new JPanel(), "Prohlížené a editace prodejek");
        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
        
        tabbedPane.addTab("Inventury", null, new JPanel(), "Prohlížení, editace a provedení inventur");
        tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);

        tabbedPane.addTab("Uzávìrky", null, new JPanel(), "Mìsíèní a roèní uzávìrky skladu");
        tabbedPane.setMnemonicAt(7, KeyEvent.VK_8);

        tabbedPane.addTab("Uživatelské úèty", null, new JPanel(), "Nastavení uživatelských úètù");
        tabbedPane.setMnemonicAt(8, KeyEvent.VK_9);    
        
        //Do prázdných panelù vlož konkrétní panely
        //to je potøeba proto, aby byly naplnìny i panely, které posdìji
        //nebudou zpøístupnìny urèitým uživatelùm
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
        
        //Zøejmì budu v budoucnu øešit refresh jinak 
        //tabbedPane.addChangeListener( new TabbedPaneChangeListener() );
        
        return mainPanel;
    }
    
    /*
     *  Vytvoøí nástrojovou lištu 
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        toolBar.setFocusable(false);
        
        /* Tlaèítko Nová položka */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "New22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Vytvoøí novou skladovou kartu");
        button.addActionListener( new NewGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Editace položky */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Edit22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Upraví skladovou kartu oznaèeného zboží");
        button.addActionListener( new EditGoodsListener());
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Smazání položky */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Delete22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Vymaže vybrané skladové karty");
        button.addActionListener( new DelGoodsListener());
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Nalezení */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Zoom22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Nalezne zboží ve skladu");
        button.addActionListener( new FindGoodsListener());
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Tisk */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Print22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Vytiskne pøehled skladových zásob");
        button.addActionListener( new PrintGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko pøehled obchodování */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "bookwi-22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Zobrazí pøehled obchodování se zbožím");
        button.addActionListener( new TradesGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Obnovit */
//        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "face-smile22.png");
//        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
//        button = new JButton(imageIcon);
//        button.setToolTipText("Zobrazí historii zboží");
//        button.addActionListener( new RecoveryGoodsListener() );
//        button.setFocusable(false);
//        toolBar.add(button);
        
        toolBar.addSeparator();
        
        /* Tlaèítko Pøíjem */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Import22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Pøíjem zboží na sklad");
        button.addActionListener( new BuyGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Nastavení */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Export22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Výdej zboží odbìrateli");
        button.addActionListener( new SaleGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        /* Tlaèítko Nastavení */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Discount22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Maloobchodní prodej");
        button.addActionListener( new DiscountGoodsListener() );
        button.setFocusable(false);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        /* Tlaèítko Nastavení */
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Preferences22.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton(imageIcon);
        button.setToolTipText("Otevøe okno s nastavením");
        button.addActionListener( new SettingsListener());
        button.setFocusable(false);
        toolBar.add(button);
                
        /* Tlaèítko O programu */
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
     *  Vytvoøí obsah záložky Sklad
     */
    private JPanel createStorePanel() {
        JPanel content = new JPanel(new BorderLayout());
        JPanel bottomPart = new JPanel(); // Panel s tlaèítky na spodu okna
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Import16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Pøíjmout", imageIcon);
        button.setToolTipText("Pøijme zboží na sklad");
        button.addActionListener( new BuyGoodsListener() );
        bottomPart.add(button);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Export16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Vydat", imageIcon);
        button.setToolTipText("Výdá zboží ze skladu");
        button.addActionListener( new SaleGoodsListener() );
        bottomPart.add(button);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Discount16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Maloobchod", imageIcon);
        button.setToolTipText("Maloobchodní prodej zboží");
        button.addActionListener( new DiscountGoodsListener() );
        bottomPart.add(button);
        
        iconURL = MainWindow.class.getResource(Settings.ICON_URL + "Print16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        button = new JButton("Tisk stavu", imageIcon);
        button.setToolTipText("Vytiskne seznam zboží na skladì");
        button.addActionListener( new PrintGoodsListener() );
        bottomPart.add(button);
        
        storePanel = new StorePanel(this, user);
        content.add(storePanel, BorderLayout.CENTER);
        content.add(bottomPart, BorderLayout.SOUTH);
            
        return content;
    }
    
    
    /**
     *  Vytvoøí obsah záložky Uživatelské úèty 
     */
    private JPanel createAccountPanel() {
        // Podle pøihlášeného uživatel vytvoøí pøíslušný panel s uživatelskými úèty
        if (user.getClient().getType() == ClientType.MANAGER.getOrdinal()) {
            accountPanel = new AccountManagerPanel(this, user);
        } else {
            accountPanel = new AccountCommonPanel(this, user);
        }
        
        return accountPanel;
    }
    
    /**
     *  Stavový øádek
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(gbl);
        
        panel.setPreferredSize( new Dimension(100, 15));
        
        statusBarTip = new JLabel();
        panel.add(setComponent(statusBarTip, 0, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        
        JLabel label = new JLabel("Pøihlášen: ");
        panel.add(setComponent(label, 1, 0, 1, 1, 0.0, 0.0, HORIZONTAL, EAST));
                
        JLabel loginLabel = new JLabel(loginUserName + " ");
        Font font = new Font("Tahoma", Font.ITALIC | Font.BOLD, Settings.getMainItemsFontSize());
        loginLabel.setForeground( new Color(0x0942A1) );
        loginLabel.setFont(font);
        panel.add(setComponent(loginLabel, 2, 0, 1, 1, 0.0, 0.0, HORIZONTAL, EAST));

        return panel;
    }
    
    /*
     * Vrací ukazatel na panel, který je vytvoøen v hlavním oknì
     */
    /**
     * Vrací panel se skalem, který byl vytvoøen v hlavním oknì
     * @return Panel se skladem
     */
    public StorePanel getStorePanel() {
        return storePanel;
    }
    

    /**
     *  Vrací panel s dodavateli, který byl vytvoøen v hlavním oknì
     */
    public SuplierPanel getSuplierPanel() {
        return suplierPanel;
    }

    /**
     *  Vrací panel s odbìrateli, který byl vytvoøen v hlavním oknì
     */
    public CustomerPanel getCustomerPanel() {
        return customerPanel;
    }
    
    /**
     * Vrací panel s pøíjemkami, který byl vytvoøen v hlavním oknì 
     * @return Panel s pøíjemkami
     */
    public BuyPanel getBuyPanel() {
        return buyPanel;
    } 
    
    /**
     * Vrací panel s výdejkami, který byl vytvoøen v hlavním oknì 
     * @return Panel s výdejkami
     */
    public SalePanel getSalePanel() {
        return salePanel;
    } 
    
    /**
     * Vrací panel s inveturami, který byl vytvoøen v hlavním oknì 
     * @return Panel s inventurami
     */
    public StockingsPanel getStockingsPanel() {
        return stockingsPanel;
    }

    /**
     *  Vrací objekt tvoøící záložky s obsahem 
     *  @return objek záložek
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
    
    /**
     *  Otevøe dialog s nastavením programu
     */
//    private void openSettingsDialog() {
//        SettingsDialog.openSettingsDialog(this);
//    }
    
    /**
     *  Vyhledá zboží ve skladì
     */ 
    private void findGoods() {
        // Pøepni na panel sklad
        tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
        
        FindGoodsDialog fgd = FindGoodsDialog.openFindDialog(MainWindow.this);
        String keyword = FindGoodsDialog.getKeyword();
        lastSearchedGoodsKeyword = keyword;
        
        // Jestliže uživatel nic nezadal, nic nevyhledávej
        if (keyword.trim().length() == 0) {
            return;
        }

        // Podle zpùdobu zavøení proveï pøíslušnou akci
        if (fgd.getTypeOfClose() == FindGoodsDialog.FIRST_CLOSE) {
            storePanel.findFirstGoods(keyword);
        }
        if (fgd.getTypeOfClose() == FindGoodsDialog.NEXT_CLOSE) {
            storePanel.findNextGoods(keyword);
        }
        
    }
    
    /**
     *  Vytvoøí nový ceník. Naète z databáze výchozí ceník,
     *  vyvolá dialog pro zmìnu ceníku a pøípadnou zmìnu zapíše do databáze
     */
    private void editPriceList() {
        
        try {
            PriceList oldPriceList = user.openPriceListEditor().getDefaultPriceList();
            PriceList result = PriceListDialog.openPriceListDialog(MainWindow.this, oldPriceList);
            
            // Jestliže uživatel potvrdil dialog, zapiš do databáze ceník
            if (result != null) {
                // Jestliže je starý ceník "prázdný""
                if (oldPriceList.getId() == -1) {
                    user.createPriceList(result); // Vytvoø nový ceník
                } else {
                    user.editPriceList(oldPriceList, result); // jinak nahraï za starý
                }
                
                JOptionPane.showMessageDialog(this, "<html><center>Zmìna ceníku provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
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
     *  Zmìna informací o nás
     */
    private void editAbout() {
        
        try {
            AboutEditor aboutEditor = user.openAboutEditor();
            About about = aboutEditor.getMainAbout();
            About result = EditAboutDialog.openDialog(this, about);
            
            if (result == null) {
                return;
            }
            
            // Rozhodni zda již existuje v databázi starý záznam
            if (about.getCustomer().getId() == -1) {
                user.createAbout(result); // neexistuje - vytvoø nový
            } else {
                user.editAbout(about, result); //existuje - proveï zmìnu
            }

            JOptionPane.showMessageDialog(this, "<html><center>Zmìna údajù provedena</center></html>", "Ok", JOptionPane.INFORMATION_MESSAGE); 
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
     * Provede tisk skladových zásob
     */
    public void printStore() {
        String text = "Vytisknout pøehled skladových zásob?";
        Object[] options = {"Ano", "Ne"};

        int n = JOptionPane.showOptionDialog(
                this,
                text,
                "Tisk",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,   // žádna vlastní ikonka
                options,
                options[0]);

        if (n != 0) {
            return; // jestliže nebyl výbìr potvrzen - konec
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
     * Zobrazí dialog s historií obchodování se zbožím
     * @param goods
     */
    private void showTradesWithGoods(Goods goods) {
        
        // zobrazí modální dialog
        new GoodsTradesDialog(this, user, goods);
    }
    
    
    /**
     *  Otevøe dialog a vytiskne pøehled pøíjemek/nebo výdejek za zvolené období
     */
    private void printTrades() {
       PrintTradesRecapDialog.openDiaog(this);
    }

    public SalePanel getDiscountPanel() {
        return discountPanel;
    }
    
    /**
     *  Posluchaè vbìru Konec 
     */
    private class ExitListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            MainWindow.this.dispose();
        }
        
    }
    
    /**
     * Posluchaè výbìru About z menu
     */
    private class AboutListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            new AboutDialog(MainWindow.this);
        }
        
    }
    
    /**
     *  Posluchaè výbìru vytvoøení pøíjemky
     */
    private class BuyGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            buyPanel.newItem();
            //new DoBuyDialog(MainWindow.this, user);
        }
    }
    
    /**
     *  Posluchaè výbìru vytvoøení výdejky
     */
    private class SaleGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            getSalePanel().newItem();
            //new DoSaleDialog(MainWindow.this, user);
        }
    }
    
    /**
     *  Posluchaè výbìru maloobchod
     */
    private class DiscountGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            new DiscountDialog(MainWindow.this, user);
        }
    }
    
    /**
     *  Posluchaè výbìru inventuru
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
     *  Posluchaè výbìru Nový dodavatel
     */
    private class NewCustomerListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            customerPanel.newItem(); // Vytvoø nového dodavatele
        }
    }    
    
    /**
     *  Posluchaè výbìru Nový odbìratel
     */
    private class NewSuplierListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            suplierPanel.newItem(); // Vytvoø nového dodavatele
        }
    }   
    
    /**
     *  Posluchaè výbìru Nová skladová karta
     */
    private class NewGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Vytvoø novou položku
            storePanel.newItem();
        }
    }
    
    /**
     *  Posluchaè výbìru Editace skladové karty
     */
    private class EditGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Pøepni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
            // Vytvoø novou položku
            storePanel.editItem();
        }
    }
    
    /**
     *  Posluchaè výbìru Smazání skladové karty
     */
    private class DelGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Pøepni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
            // Vytvoø novou položku
            storePanel.deleteItem();
        }
    }
    
    /**
     *  Posluchaè výbìru Nalezení skladové karty
     */
    private class FindGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Pøepni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());
            
            findGoods(); // Vyhledej zboží 
        }
    }
    
    /**
     *  Posluchaè výbìru Nalezení skladové karty
     */
    private class FindFirstGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            findGoods();
        }
    }
    
    /**
     *  Posluchaè výbìru Nalezení skladové karty
     */
    private class FindNextGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Jestliže uživatel nic nezadal, nic nevyhledávej
            if (lastSearchedGoodsKeyword.trim().length() == 0) {
                return;
            }
            
            // Pøepni na panel sklad
            tabbedPane.setSelectedIndex(TabbedPaneItems.STORE.getIndex());

            storePanel.findNextGoods(lastSearchedGoodsKeyword);
        }
    }
    
    /**
     *  Posluchaè výbìru tisk
     */
    private class PrintGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            printStore();
        }
    }
    
    /**
     *  Posluchaè výbìru pøehledu obchodování se zbožím
     */
    private class TradesGoodsListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            Goods selected = storePanel.getFirstSelectedGoods();
            if (selected == null) {
                ErrorMessages er = new ErrorMessages(Errors.NO_SELECTED_VALUE, "Vyberte zboží u kterého chcete zobrazit historii obchodování");
                JOptionPane.showMessageDialog(MainWindow.this, er.getFormatedText(), er.getTextCode(), JOptionPane.INFORMATION_MESSAGE); 
                
                return;
            }
            
            showTradesWithGoods(selected);
        }
    }
    
 

    /**
     * Hromadná zmìna DPH
     */
    private class DphGlobalChangeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            new DphGlobalChangeDialog(MainWindow.this, user);
        }

    }

    /**
     *  Posluchaè vytvoøení statistiky
     */
    private class StatistikListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new StatistikDialog(MainWindow.this, user);
        }
        
    }
    
    /**
     *  Posluchaè výbìru Nastavení
     */
    private class SettingsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SettingsDialog.openSettingsDialog(MainWindow.this);
        }
        
    }
    
    /**
     *  Posluchaè výbìru Tvorba cen
     */
    private class PriceListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            editPriceList();
        }
    }
        
    
    /**
     *  Posluchaè výbìru Informace o nás
     */
    private class AboutUsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            editAbout();
        }
    }
    
    /**
     *  Posluchaè výbìru Tisk obchodù
     */
    private class PrintTrades implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            printTrades();
        }
    }    
    
    /**
     *  Posluchaè zmìny v tabbedpane
     */
    private class TabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            int tabIndex = tabbedPane.getSelectedIndex();
            
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.STORE.getIndex()) {
                storePanel.refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.CUSTOMERS.getIndex()) {
                customerPanel.refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.SUPLIERS.getIndex()) {
                suplierPanel.refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.BUY.getIndex()) {
                buyPanel.refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.SALE.getIndex()) {
                getSalePanel().refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.STOCKING.getIndex()) {
                stockingsPanel.refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
            if (tabIndex == TabbedPaneItems.RECAP.getIndex()) {
                recapsPanel.refresh();
            }
            // Rozhodni, která záložka je zobrazena a tu znovunaèti
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
 