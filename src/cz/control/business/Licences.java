package cz.control.business;

import cz.control.errors.ApplicationException;
import cz.control.errors.ErrorMessages;
import cz.control.errors.Errors;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Program Control - Skladový systém
 *
 * Tøída slouží pro ovìøení a vložení licenèních údajù.
 *
 * @author Kamil Ježek
 *
 * (C) 2011
 */
public class Licences {
 
    private static final String PRIVATE_KEY = 
            "Open Control - Skladove hospodarstvi efektivne - " + Settings.getVersion();
    
    private static Licences self = null;
    
    /** a flag signaling that the application has been licensed. */
    private boolean licensed = false;
    
    private Licences() throws ApplicationException { 
        try {
            String owner = Settings.getLicenseOwner();
            String key = Settings.getLicenseKey();

            String assumendKey = SHA1Decoder.SHA1(owner + PRIVATE_KEY);

            licensed = (key != null && key.length() > 0
                    && key.equals(assumendKey));
            
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        }
    }
    
    /**
     * It creates an instance. 
     * @return 
     */
    public static Licences get() throws ApplicationException {
        
        if (self == null) {
            self = new Licences();
        }
        
        return self;
    }
    
    /**
     * It tries to license the application.
     * @param licenseKey the license key
     * @param owner owner
     * @return true if the licensing has been successfull
     */
    public boolean license(String licenseKey, String owner) 
            throws ApplicationException {
        
        try {
            String assumendKey = SHA1Decoder.SHA1(owner + PRIVATE_KEY);
            
            boolean tmp = licenseKey != null && licenseKey.length() > 0
                    && licenseKey.equals(assumendKey);
            
            // if successfuly licensed, change iner state
            if (!isLicensed() && tmp) {
                licensed = true;
            }
            
            return tmp;
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        }
    }  
    
    /**
     * 
     * @return  true if the progam is successfuly licensed
     */
    public boolean isLicensed() {
//        return licensed;
        return true; // we now have CC license
    }
    
    /**
     * Zkontroluje licenci a zobrazí chybové hlášení v pøípadì
     * neplatné licence
     * @param owner 
     * @return false pokud není program øádnì licencován
     */
    public boolean checkLicenseWithDialog(Component owner) {
        
        if (!isLicensed()) {
            ErrorMessages er = new ErrorMessages(Errors.NOT_LICENSED, "");
            JOptionPane.showMessageDialog(owner, er.getFormatedText() , er.getTextCode(), JOptionPane.WARNING_MESSAGE); 
        }
        
        return isLicensed();
    }
}
