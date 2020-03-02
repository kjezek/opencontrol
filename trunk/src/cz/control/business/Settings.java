/*
 * Settings.java
 *
 * Created on 25. z��� 2005, 15:12
 */

package cz.control.business;

import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * T��da slou��c� pro ulo�en� a nastaven� parametr� programu.
 * T��da poskytuje tyto hodnoty pro dal�� pou�it� ostatn�m modul�m
 * T��da uchov�v� nastaven� programu
 *
 * D�le t��da na��t� hodnoty z .XML souboru s nastaven�m. A ukl�d� nastaven� do
 * souboru
 *
 * @author Kamil Je�ek
 */
public class Settings {

    /** relativn� velkost okna vzhledem k rozli�en� u�ivatelsk� obrazovky. */
    private static final double WINDOW_SIZE = 0.85;

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    private static final String DEFAULT_DB_NAME = "control_100";
    
    private static String databaseURL = "localhost/" + DEFAULT_DB_NAME;
    private static String databaseUserName = "root";
    private static String databaseUserPassword = "mysql";
    
    private static String ncName = "NC";
    private static String pcAName = "PC A";
    private static String pcBName = "PC B";
    private static String pcCName = "PC C";
    private static String pcDName = "PC D";
    private static boolean showZeroCards = true;
    
    private static int defaultSalePrice = DoBuy.USE_PCA_FOR_SUM;
    private static int defaultDiscountPrice = DoBuy.USE_PCD_FOR_SUM;
    
    private static String lastUpdate = "20.8.2018";
    private static String version = "1.3";
    private static String revision = "0";
    
    private static Cryptographic crypto;
    private static String databaseUserPasswordCrypted = "";
    
    private static boolean showPrintPreview = true;

        //zjisti klientsk� rozli�en�
    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private final static int mainWindowWidth = (int) (screenSize.width * WINDOW_SIZE);
    private final static int mainWindowHeight = (int) (screenSize.height * WINDOW_SIZE);

    private static int dialogWidth = (int) (mainWindowWidth * WINDOW_SIZE);
    private static int dialogHeight = (int) (mainWindowHeight * WINDOW_SIZE);

    private static int mainItemsFontSize = 12;
    private static int labelsFontSize = 12;
    private static int textFieldsFontSize = 12;
    
    private static String licenseKey = "";
    private static String licenseOwner = "";

    private static String databaseLocation = "local";
    
    /**
     * Jm�no adres��e ve kter�m jsou ulo�eny ikony programu
     */
    public static final String ICON_URL = "/cz/control/icons/";
    
    public static final int QUANTITY_ROUND = 5;
    
    public static final int LIMIT = 1000;
    
    /** Low Dph. */
    private static int lowLayer = 15;
    
    /** High Dph. */
    private static int highLayer = 21;
    
    
    /**
     * Konstanta ud�vaj�c� posun dialogov�ch oken od
     * horn�ho lev�ho rohu rodi�ovsk�ho okna
     */
    public static int DIALOG_TRANSLATE = 25;
    
    private static final String SETTINGS_FILE_NAME = "settings.xml"; // jm�no souboru s nastaven�m
    
    private static String file = SETTINGS_FILE_NAME;
    
    private static DecimalFormat df;
    private static boolean showWelcomeScreen = true;

    public static boolean isShowPrintPreview() {
        return showPrintPreview;
    }

    public static void setShowPrintPreview(boolean aShowPrintPreview) {
        showPrintPreview = aShowPrintPreview;
    }

    /**
     * @return the showWelcomeScreen
     */
    public static boolean isShowWelcomeScreen() {
        return showWelcomeScreen;
    }

    /**
     * @param aShowWelcomeScreen the showWelcomeScreen to set
     */
    public static void setShowWelcomeScreen(boolean aShowWelcomeScreen) {
        showWelcomeScreen = aShowWelcomeScreen;
    }

    /**
     * @return the mainWindowWidth
     */
    public static int getMainWindowWidth() {
        return mainWindowWidth;
    }

    /**
     * @return the mainWindowHeight
     */
    public static int getMainWindowHeight() {
        return mainWindowHeight;
    }

    /**
     * @return the dialogWidth
     */
    public static int getDialogWidth() {
        return dialogWidth;
    }

    /**
     * @return the dialogHeight
     */
    public static int getDialogHeight() {
        return dialogHeight;
    }

    /**
     * @return the mainItemsFontSize
     */
    public static int getMainItemsFontSize() {
        return mainItemsFontSize;
    }

    /**
     * @param aCommonFontSize the mainItemsFontSize to set
     */
    public static void setMainItemsFontSize(int aCommonFontSize) {
        mainItemsFontSize = aCommonFontSize;
    }

    /**
     * @return the labelsFontSize
     */
    public static int getLabelsFontSize() {
        return labelsFontSize;
    }

    /**
     * @return the textFieldsFontSize
     */
    public static int getTextFieldsFontSize() {
        return textFieldsFontSize;
    }

    /**
     * @param aLabelsFontSize the labelsFontSize to set
     */
    public static void setLabelsFontSize(int aLabelsFontSize) {
        labelsFontSize = aLabelsFontSize;
    }

    /**
     * @param aTextFieldsFontSize the textFieldsFontSize to set
     */
    public static void setTextFieldsFontSize(int aTextFieldsFontSize) {
        textFieldsFontSize = aTextFieldsFontSize;
    }

    public static String getDatabaseLocation() {
        return databaseLocation;
    }

    public static void setDatabaseLocation(String databaseLocation) {
        Settings.databaseLocation = databaseLocation;
    }

    /* Insatance nebudou pot�eba */
    private Settings() {
    }
    
    /**
     * Ulo�� nastaven� programu do .XML souboru
     * @throws java.lang.Exception Vyvol� jestli�e se nepovede ulo�it nastaven�
     */
    public static void saveSettings() throws Exception {
        
        try {
            crypto = Cryptographic.getInstance(Cryptographic.DES);
            
            Document zdroj = createDocument();
            
            //validateDokument(zdroj); /*Zvaliduj */
            
            TransformerFactory tf = TransformerFactory.newInstance();
//            N�sleduj�c� ��dku jsem zakomentoval nebo� nefunguje a nem�m tu�en� pro�
//            tf.setAttribute("indent-number", 2);
            Transformer writer = tf.newTransformer();
            writer.setOutputProperty(OutputKeys.INDENT, "yes");
            writer.setOutputProperty("encoding", "utf-8");
            writer.transform(new DOMSource(zdroj),
                    new StreamResult(
                    new OutputStreamWriter(
                    new FileOutputStream(
                    new File(file)),
                    Charset.forName("utf-8"))));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     *  Vytvo�� dokument s konfigurac�
     */
    private static Document createDocument() throws Exception {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false); /* Proat�m validace nen� ��dn� */
        DocumentBuilder builder = dbf.newDocumentBuilder();
        DOMImplementation imp = builder.getDOMImplementation();
        
        Document doc = imp.createDocument(null, "settings", null); /* Nov� domkument*/
        Node base = doc.getDocumentElement();   /* Z�skej ukazatel na z�kladn� nod*/
        
        /* Vytvo�en� element� */
        
        Element element = doc.createElement("database");  /* element database */

        Element databaseElement = doc.createElement("location");
        Text databaseLocationText = doc.createTextNode(databaseLocation);
        databaseElement.appendChild(databaseLocationText);
        element.appendChild(databaseElement);

        /* Vytvo�n� element� database */
        databaseElement = doc.createElement("hostName");
        Text databaseNameText = doc.createTextNode(databaseURL);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        databaseElement = doc.createElement("userName");
        databaseNameText = doc.createTextNode(databaseUserName);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        // Z�k�duj heslo k datab�zi a ulo�
        String cryptedString = crypto.encryption(databaseUserPassword);
        
        databaseElement = doc.createElement("userPassword");
        //databaseNameText = doc.createTextNode(databaseUserPassword);
        //databaseNameText = doc.createTextNode(cryptedString);
        //databaseElement.appendChild(databaseNameText);
        //Je t�eba pou��t CDATA, nebo� nen� jist�, jak� znaky bude obsahovat �et�zec
        CDATASection databasePasswordCDATA = doc.createCDATASection(cryptedString);
        databaseElement.appendChild(databasePasswordCDATA);
        element.appendChild(databaseElement);
        
        base.appendChild(element);
        
        element = doc.createElement("priceName");
        
        /* Vytvo�en� element� n�zvy cen*/
        databaseElement = doc.createElement("NC");
        databaseNameText = doc.createTextNode(ncName);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        databaseElement = doc.createElement("PCA");
        databaseNameText = doc.createTextNode(pcAName);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        databaseElement = doc.createElement("PCB");
        databaseNameText = doc.createTextNode(pcBName);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        databaseElement = doc.createElement("PCC");
        databaseNameText = doc.createTextNode(pcCName);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        databaseElement = doc.createElement("PCD");
        databaseNameText = doc.createTextNode(pcDName);
        databaseElement.appendChild(databaseNameText);
        element.appendChild(databaseElement);
        
        base.appendChild(element);
        
        element = doc.createElement("showZeroCards");
        element.setAttribute("show", String.valueOf(showZeroCards) );
        
        base.appendChild(element);
        
        element = doc.createElement("showWelcomeScreen");
        element.setAttribute("show", String.valueOf(showWelcomeScreen) );

        base.appendChild(element);

        element = doc.createElement("fonts");

        Element fontElement = doc.createElement("mainItems");
        fontElement.setAttribute("size", String.valueOf(mainItemsFontSize));
        element.appendChild(fontElement);

        fontElement = doc.createElement("labels");
        fontElement.setAttribute("size", String.valueOf(labelsFontSize));
        element.appendChild(fontElement);

        fontElement = doc.createElement("textFields");
        fontElement.setAttribute("size", String.valueOf(textFieldsFontSize));
        element.appendChild(fontElement);

        base.appendChild(element);

        Element licenseKeyElement = doc.createElement("licenseKey");
        licenseKeyElement.setAttribute("licenseOwner", licenseOwner);
        Text licenseKeyValue = doc.createTextNode(licenseKey);
        licenseKeyElement.appendChild(licenseKeyValue);
        base.appendChild(licenseKeyElement);
        
        return doc;
    }
    
    /**
     * Na�te z .xml souboru nastaven� programu
     * @throws java.lang.Exception Vyvol� jestli�e se nepovede na��st nastaven�
     */
    public static void loadSettings() throws Exception {
        
        try {
            
            // if the settings file does not exist,
            // it rely on defaults
            if (!(new File(file).exists())) {
                return;
            }
            
            crypto = Cryptographic.getInstance(Cryptographic.DES);
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); /* Vytvor obalku pro parser*/
            dbf.setValidating(false);   /* Prozat�m bez validace */
            DocumentBuilder builder = dbf.newDocumentBuilder(); /* Nastav ze parser bude DOM */
            builder.setErrorHandler(new ParserError()); /* Nastav obsluhu chyb */
            
            Document doc  = builder.parse(file); /* Nacti .xml soubor do stromu */
            
//            validateDokument(doc); /*Zvaliduj */
            
            readSettings(doc); /* Nacti hodnoty z dokumentu */
            
            // Roz�ifrujheslo k datab�zi
            databaseUserPassword = crypto.decryption(databaseUserPasswordCrypted);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     *  Na�te z parsovan�ho dokumentu pot�ebn� hodnoty
     */
    private static void readSettings(Document doc) {
        NodeList parent = doc.getChildNodes();
        NodeList nl = null;
        
        /* Projdi nody a nalezni ko�enov� nod Settings */
        for (int i = 0; i < parent.getLength(); i++) {
            Node node = parent.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("settings")) {
                nl = parent.item(i).getChildNodes(); /* Vyber v�echn n�sledovn�ky */
                break;
            }
        }
        
        for (int i = 0; i < nl.getLength(); i++) { /* Projdi podstrom*/
            Node node = nl.item(i); /* Vyber prvek ze stromu */
            
            /* Jestli�e na�el nod p�estavuj�c� element  */
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("database")) {
                readDatabase(node); // na�ti p��slu�nou hodnotu
            }
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("priceName")) {
                readPriceName(node); // na�ti p��slu�nou hodnotu
            }
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("showZeroCards")) {
                readZeroCards(node); // na�ti p��slu�nou hodnotu
            }
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("showWelcomeScreen")) {
                readShowWelcomeScreen(node);
            }
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("fonts")) {
                readFonts(node);
            }
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("licenseKey")) {
                readLicense(node);
            }
            
        }
        
    }
    
    /*
     *  Na�te z pod��sti stromu nod odpov�daj�c�ho n�zvu
     */
    private static void readDatabase(Node node) {
        NodeList nl = node.getChildNodes(); /* Na�ti n�sledovn�ky */
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i); /* Vyber jeden nod ze seznamu */
            
            /* Jestli�e se jedn� od Element a souhlas� n�zev  */
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("location")) {
                databaseLocation = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("hostName")) {
                databaseURL = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("userName")) {
                databaseUserName = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("userPassword")) {
                databaseUserPasswordCrypted = readSubElement(n);
            }
        }
    }
    
    /*
     *  Na�te z pod��sti stromu nod odpov�daj�c�ho n�zvu
     */
    private static void readPriceName(Node node) {
        NodeList nl = node.getChildNodes(); /* Na�ti n�sledovn�ky */
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i); /* Vyber jeden nod ze seznamu */
            
            /* Jestli�e se jedn� od Element a souhlas� n�zev  */
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("NC")) {
                ncName = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("PCA")) {
                pcAName = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("PCB")) {
                pcBName = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("PCC")) {
                pcCName = readSubElement(n);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("PCD")) {
                pcDName = readSubElement(n);
            }
        }
    }
    
    /**
     *  Na�te hodnotu z elementu "showZeroCards"
     */
    private static void readZeroCards(Node node)  {
        
        NamedNodeMap nnm = node.getAttributes();
        
        String atrib = nnm.getNamedItem("show").getNodeValue();
        showZeroCards = Boolean.valueOf(atrib).booleanValue();
    }

    /**
     * Na�te hodnotu z elementu "showWelcomeScreen"
     * @param node
     */
    private static void readShowWelcomeScreen(Node node) {

        NamedNodeMap nnm = node.getAttributes();
        String atrib = nnm.getNamedItem("show").getNodeValue();
        setShowWelcomeScreen(Boolean.valueOf(atrib).booleanValue());
    }

    private static void readFonts(Node node) {

        NodeList nl = node.getChildNodes(); /* Na�ti n�sledovn�ky */

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i); /* Vyber jeden nod ze seznamu */
            NamedNodeMap nnm = n.getAttributes();

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("mainItems")) {
                String attrib = nnm.getNamedItem("size").getNodeValue();
                mainItemsFontSize = Integer.parseInt(attrib);
            }

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("labels")) {
                String attrib = nnm.getNamedItem("size").getNodeValue();
                labelsFontSize = Integer.parseInt(attrib);
            }

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("textFields")) {
                String attrib = nnm.getNamedItem("size").getNodeValue();
                textFieldsFontSize = Integer.parseInt(attrib);
            }

        }

    }
    
    private static void readLicense(Node node) {

        licenseKey = readSubElement(node);

        NamedNodeMap nnm = node.getAttributes();
        licenseOwner = nnm.getNamedItem("licenseOwner").getNodeValue();

    }
    


    
    /*
     *  Na�te z pod��sti stromu nod odpov�daj�c�ho n�zvu
     */
    private static String readSubElement(Node node) {
        NodeList nl = node.getChildNodes(); /* Na�ti n�sledovn�ky */
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i); /* Vyber jeden nod ze seznamu */
            
            /* Jestli�e nalezl textov� nod*/
            if (n.getNodeType() == Node.TEXT_NODE) {
                return n.getNodeValue().trim(); /* Vyber text */
            }
            
            /* Jestli�e nalezl CDATA nod*/
            if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                return n.getNodeValue().trim(); /* Vyber text */
            }
        }
        return "";
    }
    
    /**
     * Adresu ulo�en� datab�ze
     * @return adresa po��ta�e, kde je naistalov�na datab�ze
     */
    public static String getDatabaseURL() {
        return Settings.databaseURL;
    }
    
    /**
     * Nastav� adresu po��ta�e, kde je nainstalov�na datab�ze
     * @param databaseURL adresa datab�zov�ho serveru
     */
    public static void setDatabaseURL(final String databaseURL) {
        Settings.databaseURL = databaseURL;
    }
    
    /**
     * Vrac� u�ivatelsk� jm�no pro p��stup k datab�zi. To m��e b�t nap� "root"
     * @return U�ivatelsk� jm�no
     */
    public static String getDatabaseUserName() {
        return Settings.databaseUserName;
    }
    
    /**
     * Nastav� U�ivatelsk� jm�no pro p��stup do datab�ze
     * @param databaseUserName u�ivatelsk� jm�no pro p��stup do datab�ze
     */
    public static void setDatabaseUserName(final String databaseUserName) {
        Settings.databaseUserName = databaseUserName;
    }
    
    /**
     * Vrac� heslo pro p��stup so satab�ze
     * @return heslo pro p��stup do datab�ze
     */
    public static String getDatabaseUserPassword() {
        return Settings.databaseUserPassword;
    }
    
    /**
     * Nastavuje heslo pro p��stup do datab�ze
     * @param databaseUserPassword heslo pro p��stup do datab�ze
     */
    public static void setDatabaseUserPassword(final String databaseUserPassword) {
        Settings.databaseUserPassword = databaseUserPassword;
    }
    
    /**
     * Vrac� n�zev ceny
     * @return n�zev ceny
     */
    public static String getNcName() {
        return Settings.ncName;
    }
    
    /**
     * Vrac� n�zev ceny
     * @return n�zev ceny
     */
    public static String getPcAName() {
        return Settings.pcAName;
    }
    
    /**
     * Vrac� n�zev ceny
     * @return n�zev ceny
     */
    public static String getPcBName() {
        return Settings.pcBName;
    }
    
    /**
     * Vrac� n�zev ceny
     * @return n�zev ceny
     */
    public static String getPcCName() {
        return Settings.pcCName;
    }
    
    /**
     * Vrac� n�zev ceny
     * @return n�zev ceny
     */
    public static String getPcDName() {
        return Settings.pcDName;
    }
    
    /**
     * Vrac� �et�zcovou podobu posledn� aktualizace
     * @return Posledn� aktualizace
     */
    public static String getLastUpdate() {
        return lastUpdate;
    }
    
    /**
     * Vrac� �et�zec obsahuj�c� verzi programu
     * @return �et�zcov� hodnota verze programu
     */
    public static String getVersion() {
        return version;
    }
    
    /**
     * Vrac� form�tovac� pravidla pro v�pis cen
     * @return form�tovac� pravidla pro v�pis cen
     */
    public static DecimalFormat getPriceFormat() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("cs", "CZ"));
        df = (DecimalFormat) nf;
        df.applyPattern("#,##0.00");
        return df;
    }
    
    /**
     * Vrac� form�tovac� pravidla pro desetin�ch m�st v mno�stv�
     * @return form�tovac� pravidla pro v�pis cen
     */
    public static DecimalFormat getFloatFormat() {
        DecimalFormat df;
        NumberFormat nf = NumberFormat.getInstance(new Locale("cs", "CZ"));
        df = (DecimalFormat) nf;
        df.applyPattern("#,###.#####");
        return df;
    }
    
    public static String getDatabaseName() {
        String[] tmp = databaseURL.split("/");
        
        if (tmp.length > 1) {
            return tmp[1];
        }
        
        // fallback for backward compatibility
        return DEFAULT_DB_NAME;
    }
    
    public static String getDatabaseHost() {
        String[] tmp = databaseURL.split("/");
        
        if (tmp.length > 0) {
            return tmp[0];
        }
        
        // fallback for backward compatibility
        return "127.0.0.1";
    }
    
    
    /**
     * Vrac�, zda je povoleno zobrazen� nulov�ch karet ve skladu
     * @return true - budou zobrazeny i nulov� karty
     * false - budou zobrazeny pouze nenulov� karty
     */
    public static boolean isShowZeroCards() {
        return showZeroCards;
    }
    
    /**
     * Nastav� zda se maj� zobrazovat i nulov� karty
     * @param aShowZeroCards true - budou zobrazeny i nulov� karty
     * false - budou zobrazeny pouze nenulov� karty
     */
    public static void setShowZeroCards(boolean aShowZeroCards) {
        showZeroCards = aShowZeroCards;
    }
    
    /**
     * Nastavuje jm�no zobrazovan� pro n�kupn� cenu
     * @param aNcName Jm�no n�kupn� ceny
     */
    public static void setNcName(String aNcName) {
        ncName = aNcName;
    }
    
    /**
     * Nastavuje jm�no zobrazovan� pro prodejn� cenu A
     * @param aPcAName Jm�no prodejn� ceny A
     */
    public static void setPcAName(String aPcAName) {
        pcAName = aPcAName;
    }
    
    /**
     * Nastavuje jm�no zobrazovan� pro prodejn� cenu B
     * @param aPcBName Jm�no prodejn� ceny B
     */
    public static void setPcBName(String aPcBName) {
        pcBName = aPcBName;
    }
    
    /**
     * Nastavuje jm�no zobrazovan� pro prodejn� cenu C
     * @param aPcCName Jm�no prodejn� ceny C
     */
    public static void setPcCName(String aPcCName) {
        pcCName = aPcCName;
    }
    
    /**
     * Nastavuje jm�no zobrazovan� pro prodejn� cenu D
     * @param aPcDName Jm�no prodejn� ceny D
     */
    public static void setPcDName(String aPcDName) {
        pcDName = aPcDName;
    }
    
    public static int getDefaultSalePrice() {
        return defaultSalePrice;
    }
    
    public static int getDefaultDiscountPrice() {
        return defaultDiscountPrice;
    }

    public static String getLicenseKey() {
        return licenseKey;
    }

    public static void setLicenseKey(String licenseKey) {
        Settings.licenseKey = licenseKey;
    }

    public static String getLicenseOwner() {
        return licenseOwner;
    }

    public static void setLicenseOwner(String licenseOwner) {
        Settings.licenseOwner = licenseOwner;
    }

    public static String getRevision() {
        return revision;
    }

    public static int getLowLayer() {
        return lowLayer;
    }

    public static int getHighLayer() {
        return highLayer;
    }

    public static String[] getDphLayers() {
        
        return new String[] {
            "0",
            String.valueOf(getLowLayer()),
            String.valueOf(getHighLayer())
        };
    }
  
}
