/*
 *
 * Vytvoøeno 3. Leden, 2010
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui.dph;

import cz.control.business.Settings;
import cz.control.business.Store;
import cz.control.business.User;
import cz.control.business.dph.StaticDphSrenarios;
import cz.control.business.dph.scenarios.Scenario;
import cz.control.errors.ApplicationException;
import cz.control.errors.ErrorMessages;
import cz.control.errors.InvalidPrivilegException;
import cz.control.gui.MainWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Program Control - Skladový systém
 *
 * Dialog pro hromadnou zmìnu dph
 *
 * @author Kamil Ježek
 *
 * (C) 2010, ver. 1.1
 */
public class DphGlobalChangeDialog extends JDialog {

    private Component owner;
    private User user;
    private JList scenariosList;
    private JLabel descriptionLabel;

    private static List<Scenario> scenarios;

    public DphGlobalChangeDialog(Component owner, User user) {

        this.owner = owner;
        this.owner = owner;

        setTitle("Open Control - hromadná úprava DPH");

        try {
            Store store = user.openStore();
            scenarios = StaticDphSrenarios.scenarios(store);

        } catch (InvalidPrivilegException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        setDialog();
    }

    /**
     * provede potøebné nastavení
     */
    private void setDialog() {

        setContentPane(createContent());

        setLocationRelativeTo(owner);
//        setLocationByPlatform(true);
        setLocation( owner.getX() + Settings.DIALOG_TRANSLATE, owner.getY() + Settings.DIALOG_TRANSLATE);

        setResizable(true);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);

        refreshList();
    }

    /**
      *  Vytvoøí obsah dialogu
      */
    private Container createContent() {

        JPanel content = new JPanel(new BorderLayout());

        content.setPreferredSize(new Dimension(320, 270));

        content.add(createMainPanel(), BorderLayout.CENTER);

        JPanel listPanel = new JPanel( new BorderLayout() );
        listPanel.setPreferredSize( new Dimension(150, 270) );
       listPanel.setBorder(
                BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Seznam scénáøù"));

        scenariosList = new JList();
        scenariosList.setListData(scenarios.toArray());
        scenariosList.addListSelectionListener( new ListingLSTListener());

        listPanel.add(scenariosList, BorderLayout.CENTER);

        content.add(listPanel, BorderLayout.WEST);

        return content;

    }

    /**
     * Hlavní panel s popisem zmìny a volbou pro provedení zmìny
     *
     * @return panel
     */
    private JPanel createMainPanel() {

        JPanel panel = new JPanel( new BorderLayout() );

        panel.setBorder(
                BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Popis zmìny"));

       descriptionLabel = new JLabel();

        panel.add(descriptionLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("Provést");
        buttonPanel.add(confirmButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        confirmButton.addActionListener( new ConfirmButtonListener() );

        return panel;
    }


    private void refreshList() {

        Scenario sc = (Scenario) scenariosList.getSelectedValue();

        if (sc != null) {

            descriptionLabel.setText(sc.getDescription());
        }
    }
    
    private void procced() {
        
        Scenario sc = (Scenario) scenariosList.getSelectedValue();

        if (sc != null) {
            try {
                sc.procced();
                
                JOptionPane.showMessageDialog(
                        this, 
                        "Zmìna DPH '" + sc.getLabel() + "' provedena. ", 
                        "OK", 
                        JOptionPane.INFORMATION_MESSAGE
                        );

                MainWindow.getInstance().getStorePanel().refresh();
                
            } catch (ApplicationException ex) {
                ErrorMessages er = ErrorMessages.getErrorMessages(ex);
                JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ConfirmButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {

            procced();
        }

    }


    /**
     *  Zmìna v list selection
     */
    private class ListingLSTListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {

            refreshList();
        }
    }

}
