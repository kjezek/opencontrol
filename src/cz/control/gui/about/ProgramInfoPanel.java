

package cz.control.gui.about;

import cz.control.business.Settings;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel se základními informacemi o programu
 *
 * @author Kamil Ježek
 */
public class ProgramInfoPanel extends JPanel {

    private static final String PROGRAM_INFO = "<html>" +
            "<div style='color:navy; font: bold 30px Tahoma'>OpenControl</div>" +
            "<br>" +
            "<div style='font-size: 10px; font-family: Arial'>" +
            "<div>Verze: " + Settings.getVersion() + "_" + Settings.getRevision() + " </div>" +
            "<br>" +
            "<div>Autor: 2005-2013 Kamil Ježek</div>" +
            "<div>E-mail: opencontrol.info@gmail.com</div>" +
            "<div>WWW: http://www.opencontrol.cz/" +
            "<div>" +
            "<br>" +
            "Poslední aktualizace programu: " + Settings.getLastUpdate() +
            "<br>" +
            "<br>";
    
    private static final String COMM_TEXT = PROGRAM_INFO;
    
    private static final String OS_TEXT = PROGRAM_INFO + 
            "Licence: <b>Creative Commons Attribution</b>" +
            "<div style='font: 8px'>" +
            "" +
            "Dílo smíte: <br>"
            + "<b>Šíøit</b> — kopírovat, distribuovat a sdìlovat dílo veøejnosti; <br>" +
            "<b>Upravovat</b> — pozmìòovat, doplòovat, využívat celé nebo èásteènì v jiných dílech; <br> " +
            "využívat dílo komerènì. <br><br>" +
            "Za tìchto podmínek: <br>" +
            "<b>Uveïte autora</b> —  Máte povinnost uvést údaje o autorovi a tomto díle zpùsobem, "
            + "který stanovil autor nebo poskytovatel licence (ne však tak, "
            + "aby vznikl dojem, že podporují vás nebo zpùsob, jakým dílo užíváte)."
            + "<br><br>" +
            
            "Plné znìní licence: " +
            "<br>" +
            "<a href='http://creativecommons.org/licenses/by/3.0/cz/legalcode'>http://creativecommons.org/licenses/by/3.0/cz/legalcode</a>" +
            "</div>" +
            "</html>";


    private ProgramInfoPanel() {

        setLayout( new BorderLayout() );

        // dva panely na okrajích
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize( new Dimension(50, 30));
        add(emptyPanel, BorderLayout.EAST);

        emptyPanel = new JPanel();
        emptyPanel.setPreferredSize( new Dimension(50, 30));
        add(emptyPanel, BorderLayout.WEST);


    }

    /**
     * Info panel for open source release
     * @return 
     */
    public static JPanel createOpenSourceInfo() {
        
        JPanel panel = new ProgramInfoPanel();
        
        JLabel label = new JLabel(OS_TEXT);
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Info panel for commerical release
     * @return 
     */
    public static JPanel createCommercialInfo(JDialog owner) {
        
        JPanel panel = new ProgramInfoPanel();

        JLabel label = new JLabel(COMM_TEXT);
        JPanel innerPanel = new JPanel();
        innerPanel.add(label);
//        JButton changelogBtn = new JButton("Seznam zmìn");
//        innerPanel.add(changelogBtn);
        innerPanel.add(new LicenseInputPanel(owner));

        panel.add(innerPanel, BorderLayout.CENTER);

        return panel;
    }
}
