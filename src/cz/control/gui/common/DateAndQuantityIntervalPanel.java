/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui.common;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import cz.control.business.Settings;
import cz.control.gui.SalePanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import static java.awt.GridBagConstraints.*;

/**
 * Panel zobrazující výbìr Datum Od, Datum Do, Poèet položek
 * @author kamilos
 */
public class DateAndQuantityIntervalPanel extends JPanel {
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints(); 
    
    private List<LimitsChangedListener> callers = new ArrayList<LimitsChangedListener>();

    // panel se filtrem zobrazení
    private JDateChooser startDate;
    private JDateChooser endDate;
    private JSpinner limitSpinner = new JSpinner(
            new SpinnerNumberModel(Settings.LIMIT, 0, Integer.MAX_VALUE, 100));

   
    public  DateAndQuantityIntervalPanel(Date initialStartDate, Date initialEndDate, Integer initialLimit) {
        
        createContent();
        
        startDate.setDate( new Date(initialStartDate.getTime()) );
        endDate.setDate( new Date(initialEndDate.getTime()) );
        limitSpinner.setValue(initialLimit);
    }
    
    public void createContent() {
    
        setLayout(gbl);
        URL iconURL;
        ImageIcon imageIcon;
        
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Filtr zobrazení"));
        //content.setPreferredSize( new Dimension(100, 45));
        
        JCalendar calendar = new JCalendar();
        startDate = new JDateChooser();
        endDate = new JDateChooser();
        
        limitSpinner.setPreferredSize( new Dimension(80, 20) );
        
        iconURL = getClass().getResource(Settings.ICON_URL + "view-refresh16.png");
        imageIcon = (iconURL == null) ? null : new ImageIcon(iconURL);
        JButton confirmButton = new JButton("Obnovit", imageIcon);
        confirmButton.addActionListener( new RefreshButtonListener() );
        
        add( setComponent(new JLabel("  Od: "), 0, 0, 1, 1, 0.0, 0.0, NONE, EAST) );
        add( setComponent(startDate, 1, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        add( setComponent(new JLabel("  Do: ") , 2, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        add( setComponent(endDate, 3, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        add( setComponent(new JLabel("  Max. položek: ") , 4, 0, 1, 1, 0.0, 0.0, NONE, EAST));
        add( setComponent(limitSpinner, 5, 0, 1, 1, 0.0, 0.0, NONE, WEST));
        add( setComponent(new JLabel("   "), 6, 0, 1, 1, 0.0, 0.0, NONE, WEST)); //mezera
        add( setComponent(confirmButton, 7, 0, 1, 1, 1.0, 0.0, NONE, WEST));
        
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
     *  Posluchaè obnovení výbìru
     */
    private class RefreshButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
            doRefresh();
        }
    }    
    
    public void addLimitsChangedListener(LimitsChangedListener list) {
        this.callers.add(list);
    }
    
    private void doRefresh() {
        for (LimitsChangedListener caller: callers) {
            caller.refresh(startDate.getDate(),  endDate.getDate(), (Integer) limitSpinner.getValue()); // obnov volajícího
        }
    }
            
    
}
