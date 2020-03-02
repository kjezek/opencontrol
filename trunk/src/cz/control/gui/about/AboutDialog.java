/*
 * AboutDialog.java
 *
 * Vytvo�eno 1. listopad 2005, 10:02
 *
 
 */

package cz.control.gui.about;

import cz.control.business.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;


/**
 * Program Control - Skladov� syst�m
 *
 * Zobraz� dialogov� okno obsahuj�c� informace o programu
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public class AboutDialog extends JDialog{

    private Window owner;
    
    /**
     * Vytvo�� nov� objekt AboutDialog
     * @param owner Vlastn�k dialogu
     */
    public AboutDialog(Frame owner) {
        super(owner, "O programu", true);

        this.owner = owner;
        setDialog();
    }
    
    /**
     * Vytvo�� nov� objekt AboutDialog
     * @param owner Vlastn�k dialogu
     */
    public AboutDialog(Dialog owner) {
        super(owner, "O programu", true);

        this.owner = owner;
        setDialog();
    }
    
    /**
     * Nastav� pot�ebn� vlastnosti dialogu
     */
    private void setDialog() {
        setContentPane(ProgramInfoPanel.createOpenSourceInfo());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        
        this.addKeyListener( new AboutKeyListener() );
        
        if (owner != null) {
            setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }
        
        setResizable(false);
        setPreferredSize(new Dimension(550, 400));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na mod�ln� dialog!!
        pack();
        setVisible(true);          
    }
   
    
    private class AboutKeyListener implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER :
                    AboutDialog.this.dispose();
                    break;
                case KeyEvent.VK_ESCAPE :
                    AboutDialog.this.dispose();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
        
    }     
}


