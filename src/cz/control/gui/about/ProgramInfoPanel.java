

package cz.control.gui.about;

import cz.control.business.Settings;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel se z�kladn�mi informacemi o programu
 *
 * @author Kamil Je�ek
 */
public class ProgramInfoPanel extends JPanel {

    private static final String PROGRAM_INFO = "<html>" +
            "<div style='color:navy; font: bold 30px Tahoma'>OpenControl</div>" +
            "<br>" +
            "<div style='font-size: 10px; font-family: Arial'>" +
            "<div>Verze: " + Settings.getVersion() + "_" + Settings.getRevision() + " </div>" +
            "<br>" +
            "<div>Autor: 2005-2013 Kamil Je�ek</div>" +
            "<div>E-mail: opencontrol.info@gmail.com</div>" +
            "<div>WWW: http://www.opencontrol.cz/" +
            "<div>" +
            "<br>" +
            "Posledn� aktualizace programu: " + Settings.getLastUpdate() +
            "<br>" +
            "<br>";
    
    private static final String COMM_TEXT = PROGRAM_INFO;
    
    private static final String OS_TEXT = PROGRAM_INFO + 
            "Licence: <b>Creative Commons Attribution</b>" +
            "<div style='font: 8px'>" +
            "" +
            "D�lo sm�te: <br>"
            + "<b>���it</b> � kop�rovat, distribuovat a sd�lovat d�lo ve�ejnosti; <br>" +
            "<b>Upravovat</b> � pozm��ovat, dopl�ovat, vyu��vat cel� nebo ��ste�n� v jin�ch d�lech; <br> " +
            "vyu��vat d�lo komer�n�. <br><br>" +
            "Za t�chto podm�nek: <br>" +
            "<b>Uve�te autora</b> �  M�te povinnost uv�st �daje o autorovi a tomto d�le zp�sobem, "
            + "kter� stanovil autor nebo poskytovatel licence (ne v�ak tak, "
            + "aby vznikl dojem, �e podporuj� v�s nebo zp�sob, jak�m d�lo u��v�te)."
            + "<br><br>" +
            
            "Pln� zn�n� licence: " +
            "<br>" +
            "<a href='http://creativecommons.org/licenses/by/3.0/cz/legalcode'>http://creativecommons.org/licenses/by/3.0/cz/legalcode</a>" +
            "</div>" +
            "</html>";


    private ProgramInfoPanel() {

        setLayout( new BorderLayout() );

        // dva panely na okraj�ch
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
//        JButton changelogBtn = new JButton("Seznam zm�n");
//        innerPanel.add(changelogBtn);
        innerPanel.add(new LicenseInputPanel(owner));

        panel.add(innerPanel, BorderLayout.CENTER);

        return panel;
    }
}
