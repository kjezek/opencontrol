/*
 * FindGoodsDialog.java
 *
 * Vytvoøeno 1. únor 2006, 12:46
 *
 
 */

package cz.control.gui;

import cz.control.business.*;
import cz.control.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;

import static java.awt.GridBagConstraints.*;
import static java.awt.GridBagConstraints.*;

/**
 * Program Control - Skladový systém
 *
 * Zobrazí dialog pro vyhledání zboží
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public class FindGoodsDialog extends JDialog  {
    private GridBagLayout gbl;
    private GridBagConstraints gbc; 
    
    private JTextField findTextField = new JTextField(15);
    private JButton findButton;
    
    private Component owner;
    
    /** Dialog byl zavøen kliknutím na tlaèítko HLEDAT PRVNÍ */
    public static int FIRST_CLOSE = 1;
    /** Dialog byl zavøen kliknutím na tlaèítko HLEDAT DALŠí */
    public static int NEXT_CLOSE = 2;
    /** Dialog byl zavøen jinak - napø køížkem */
    public static int ANOTHER_CLOSE = 0;
    
    private int typeOfClose = ANOTHER_CLOSE;
    
    private static String lastKeyword = "";
    
    /** Vytvoøí nový objekt FindGoodsDialog */
    private FindGoodsDialog(Frame owner) {
        super(owner, "Nalezení zboží", true);

        this.owner = owner;
        setDialog();
    }
    
    /** Vytvoøí nový objekt FindGoodsDialog */
    private FindGoodsDialog(Dialog owner) {
        super(owner, "Nalezení zboží", true);

        this.owner = owner;
        setDialog();
    }    
    
   /**
     * provede potøebné nastavení 
     */
    private void setDialog() {
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        
        typeOfClose = ANOTHER_CLOSE;
        findTextField.setText(lastKeyword);
        
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(false);
        setPreferredSize(new Dimension(300, 100));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        setVisible(true);
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
     * Vytvoøí obsah okna
     */
    private JComponent getContent() {
        JPanel main = new JPanel();
        JButton button;
        URL iconURL;
        ImageIcon imageIcon;
        JLabel label;
                
        main.add(setComponent( new JLabel("  Najít: "), 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        
        findTextField.addKeyListener( new FindKeyListener() );
        main.add(setComponent(findTextField, 1, 0, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
        main.setLayout(gbl);
        
        iconURL = FindGoodsDialog.class.getResource(Settings.ICON_URL + "Zoom16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        findButton = new JButton("Najít", imageIcon);
        findButton.addActionListener( new FindFirstButtonListener() );
        main.add(setComponent(findButton, 2, 0, 1, 1, 0.0, 0.0, HORIZONTAL, WEST));
        
        button = new JButton("Další");
        button.addActionListener( new FindNextButtonListener() );
        main.add(setComponent(button, 2, 1, 1, 1, 0.0, 0.0, HORIZONTAL, WEST));
        
//        label = new JLabel("Tip: Zadávejte èást skladového èísla, nebo názvu");
//        main.add(setComponent(label, 0, 1, 2, 1, 1.0, 0.0, NONE, WEST));
        
        return main;
    }   
    
    /**
     * Otevøe dialog pro nalezení zboží
     * @param owner Vlastník, který otevøel dialog
     * @return objekt dialogového okna pro nalezení zboží
     */
    public static FindGoodsDialog openFindDialog(JFrame owner) {
        
        return new FindGoodsDialog(owner);
    }
    
   /**
     *  Posluchaè stisku tlaèítka Nalezení zboží 
     */
    private class FindFirstButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
           
           lastKeyword = findTextField.getText();
           typeOfClose = FIRST_CLOSE;
           FindGoodsDialog.this.dispose();
        }
    }
    
   /**
     *  Posluchaè stisku tlaèítka Nalezení dalšího zboží  
     */
    private class FindNextButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
           
           lastKeyword = findTextField.getText();
           typeOfClose = NEXT_CLOSE;
           FindGoodsDialog.this.dispose();
        }
    }

    /**
     * Vrací jakým zpùsobem byl dialog uzavøen
     * @return FIRST_CLOSE - Dialog byl zavøen kliknutím na tlaèítko HLEDAT PRVNÍ 
     *         NEXT_CLOSE -  Dialog byl zavøen kliknutím na tlaèítko HLEDAT DALŠí 
     *         ANOTHER_CLOSE - Dialog byl zavøen jinak - napø. køížkem
     */
    public int getTypeOfClose() {
        return typeOfClose;
    }

    /**
     * Vrací žetìzec zadaný v dialogu
     * @return zadaný øetìzec
     */
    public static String getKeyword() {
        return lastKeyword;
    }
    
    
    private class FindKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    findButton.doClick();
                    break;
                case KeyEvent.VK_ESCAPE :
                    typeOfClose = ANOTHER_CLOSE;
                    FindGoodsDialog.this.dispose();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }    
    
}
