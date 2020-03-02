/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.control.gui.about;

import cz.control.errors.Errors;
import javax.swing.JDialog;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import cz.control.errors.ErrorMessages;
import cz.control.business.Licences;
import cz.control.business.Settings;
import cz.control.errors.ApplicationException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static java.awt.GridBagConstraints.*;

/**
 * Tato tøída vytvoøí panel pro vložení 
 * licenèního klíèe
 *
 * @author Kamil Ježek
 */
public class LicenseInputPanel extends JPanel {

    private GridBagLayout gbl  = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private Licences licences;
    
    private JDialog ownerDialog;
    
    /**
     * constructor
     * 
     */
    public LicenseInputPanel(JDialog owner) {
        
        this.ownerDialog = owner;
        
        try {
            licences = Licences.get();
        } catch (ApplicationException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        createContent();
    }

    /**
     * Main content of this panel
     */
    private void createContent() {
        
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        

        JLabel ownerLabel = new JLabel("Vlastník licence:");
        JLabel keyLabel = new JLabel("Licenèní klíè: ");
        
        
        if (licences.isLicensed()) {
            createLicenseInfoContent();
        } else {
            final Component owner;
            final Component key;           
            final Component submit;
            
            owner = new JTextField();
            owner.setPreferredSize(new Dimension(200, 20));
        
            key= new JTextField(); 
            key.setPreferredSize(new Dimension(200, 20));
            
            submit = new JButton("Registrovat");
            
            ((JButton) submit).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    submitLicense((JTextField) owner, (JTextField) key, submit);
                }

            });
            
            add(setComponent(owner, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
            add(setComponent(key, 1, 1, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
            add(setComponent(submit, 0, 2, 2, 1, 1.0, 0.0, NONE, CENTER));            
        }
        
        setLayout(gbl);
        add(setComponent(ownerLabel, 0, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        add(setComponent(keyLabel, 0, 1, 1, 1, 0.0, 0.0, NONE, WEST));
        
    }
    
    private void createLicenseInfoContent() {
        final Component owner;
        final Component key;           
        Component submit;
        
        owner = new JLabel(Settings.getLicenseOwner());
        key = new JLabel(Settings.getLicenseKey());
        submit = new JLabel("Program je øádnì licencován.");

        add(setComponent(owner, 1, 0, 1, 1, 1.0, 1.0, HORIZONTAL, WEST));
        add(setComponent(key, 1, 1, 1, 1, 1.0, 0.0, HORIZONTAL, WEST));
        add(setComponent(submit, 0, 2, 2, 1, 1.0, 0.0, NONE, CENTER));
    }
    
    /**
     * It verifies and submmits the license
     * @param owner
     * @param key 
     * @param submit
     */
    private void submitLicense(JTextField owner, JTextField key, Component submit) {
        
        String ownerValue = owner.getText();
        String keyValue = key.getText();
        
        try {
            if (licences.license(keyValue, ownerValue)) {
                Settings.setLicenseOwner(ownerValue);
                Settings.setLicenseKey(keyValue);
                
                JOptionPane.showMessageDialog(this, "V poøádku registrováno" , "Registrováno", JOptionPane.INFORMATION_MESSAGE); 
                
                remove(owner);
                remove(key);
                remove(submit);
                
                createLicenseInfoContent();

                try {
                    Settings.saveSettings();
                } catch (Exception e) {
                    ErrorMessages er = new ErrorMessages(Errors.WRITE_SETTINGS, e.getLocalizedMessage());
                    JOptionPane.showMessageDialog(null, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
                }
                
                this.revalidate();
                this.repaint();
//                this.ownerDialog.dispose();
            } else {
                String msg = "<html>Verze programu: " + Settings.getVersion() + "<br> "
                        + "Vlastník: " + ownerValue + "<br>Klíè: " + keyValue + "</html>";
                ErrorMessages er = new ErrorMessages(Errors.INCORECT_LICENSE, msg);
                JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            }
        } catch (ApplicationException ex) {
            ErrorMessages er = ErrorMessages.getErrorMessages(ex);
            JOptionPane.showMessageDialog(this, er.getFormatedText() , er.getTextCode(), JOptionPane.ERROR_MESSAGE); 
            return;
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
    
}
