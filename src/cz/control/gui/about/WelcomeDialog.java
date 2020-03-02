/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.control.gui.about;

import cz.control.business.Settings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Uvítací dialog
 * @author Kamil Ježek
 */
public class WelcomeDialog extends JDialog {

    private Window owner;
    private JCheckBox disableShowingChB;

    /**
     * Vytvoøí nový objekt
     * @param owner Vlastník dialogu
     */
    public WelcomeDialog(Frame owner) {
        super(owner, "O programu", true);

        this.owner = owner;
        setDialog();
    }

    /**
     * Nastaví potøebné vlastnosti dialogu
     */
    private void setDialog() {
        setContentPane(getContent());
        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);

        this.addKeyListener(new AboutKeyListener());

        if (owner != null) {
            setLocation(owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);
        }

        setResizable(false);
        setPreferredSize(new Dimension(550, 500));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE nefunguje na modální dialog!!
        pack();
        setVisible(true);
    }

    private JPanel getContent() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(ProgramInfoPanel.createOpenSourceInfo(), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        disableShowingChB = new JCheckBox("Pøístì již nezobrazovat");
        buttons.add(disableShowingChB);
        JButton closeButton = new JButton("Ok");
        closeButton.addActionListener(new CloseActionListener());
        buttons.add(closeButton);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void close() {

        boolean dissable = disableShowingChB.isSelected();

        Settings.setShowWelcomeScreen(!dissable);

        WelcomeDialog.this.dispose();
    }

    private class CloseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            close();
        }
    }

    private class AboutKeyListener implements KeyListener {

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_ESCAPE:
                    close();
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }
}
