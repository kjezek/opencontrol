/*
 * Print.java
 *
 * Vytvo¯eno 19. b¯ezen 2006, 21:09
 *
 * Autor: Kamil Jeûek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;

import cz.control.database.DatabaseAccess;
import cz.control.data.MonthRecap;
import cz.control.data.About;
import cz.control.data.StockingPreview;
import cz.control.data.YearRecap;
import cz.control.data.TradeItemPreview;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Program Control - Skladov˝ systÈm
 *
 * T¯Ìda zajiöùuje tisknutÌ v programu. NaËÌt· soubory report˘ vytvo¯enÈ
 * pomocÌ jasperreports
 *
 * @author Kamil Jeûek
 * 
 * (C) 2006, ver. 1.0
 */
public class Print {
    private static final String REPORT_DIR = "reports/";

    private static AboutEditor aboutEditor = new AboutEditor();
    
    /**
     * Enum uchov·vajÌcÌ jmÈna sestav
     */
    public static enum ReportFiles {
        STORE_REPORT("store"),
        BUY_REPORT("buy"),
        SALE_REPORT("sale"),
        DISCOUNT_REPORT("discount"),
        STOCKING_REPORT("stocking"),
        MONTH_RECAP_REPORT("recap_month"),
        YEAR_RECAP_REPORT("recap_year"),
        BUY_RECAP_REPORT("buy_recap"),
        SALE_RECAP_REPORT("sale_recap");
        
        private String fileName;
        private File file;
        
        private ReportFiles(String fileName) {
            this.fileName = fileName;
            this.file = new File(fileName);
        }

        public String getFileName() {
            return fileName;
        }
        
        public String getJasperFileName() {
            return fileName + ".jasper";
        }

        public File getFile() {
            return file;
        }
    }
    
    private static boolean SHOW_PRINT_DIALOG = true;
            
    /**
     * Vytvo¯Ì novou instanci Print
     */
    private Print() {
    }
    
   
    /**
     * Vytiskne soupis skladov˝ch z·sob
     * @throws net.sf.jasperreports.engine.JRException chyba tisku
     */
    public static void printStore(boolean withZeroCards) throws JRException, SQLException {
        
        if ( new Statistik().getGoodsCardCount() == 0) {
            throw new JRException("Ve skladÏ nejsou û·dnÈ skladovÈ karty");
        }
        
        About about = new AboutEditor().getMainAbout();
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("aboutName", about.getCustomer().getName());
        parametrs.put("aboutDIC", about.getCustomer().getDic());
        parametrs.put("aboutICO", about.getCustomer().getIco());
        parametrs.put("zero_cards_condition", 
                withZeroCards ? Integer.MAX_VALUE + "" // poËÌt·me, ûe na skladÏ tolik zboûÌ nikdy nebude
                : " 0 "   // podmÌnka na nenulovÈ mnoûstvÌ
                );
        
        compile(REPORT_DIR + ReportFiles.STORE_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.STORE_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);
        }
        
    }
    
    /**
     * Vytiskne P¯Ìjmov˝ doklad p¯i n·kupu zboûÌ 
     * 
     * @param trad P¯Ìjemka, pro kterou se m· vytisknout doklad
     * @throws net.sf.jasperreports.engine.JRException 
     */
    public static void printBuy(TradeItemPreview trad) throws JRException, SQLException {
        
        if (trad == null) {
            throw new JRException("é·dn· p¯Ìjemka");
        }
                
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("id", trad.getTradeIdListing());
        
        compile(REPORT_DIR + ReportFiles.BUY_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.BUY_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        //viewReport(jasperPrint, "P¯Ìjemka");
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);
        }
    }
    
    /**
     * Vytiskne v˝dejku p¯i prodeji zboûÌ
     * @param trad V˝dejka, kter· se m· vytisknout
     */
    public static void printSale(TradeItemPreview trad) throws JRException, SQLException {
        if (trad == null) {
            throw new JRException("é·dn· v˝dejka");
        }
        
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("id", trad.getTradeIdListing());
        
        compile(REPORT_DIR + ReportFiles.SALE_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.SALE_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);        
        }
        
    }
    
    /**
     * Vytiskne v˝dejku pro maloobchodnÌ prodej
     * @param trad V˝dejka, kter· se m· vytisknout
     */
    public static void printDiscount(TradeItemPreview trad) throws JRException, SQLException {
                
        if (trad == null) {
            throw new JRException("é·dn· prodejka");
        }
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("id", trad.getTradeIdListing());
        parametrs.put("owner", aboutEditor.getMainAbout().getCustomer().getName());
        
        compile(REPORT_DIR + ReportFiles.DISCOUNT_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.DISCOUNT_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        //JasperViewer.viewReport(jasperPrint);
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);        
        }
    }
    
    /**
     * Vytiskne doklad o provedenÈ inventu¯e
     * @param stockingPreview inventura k vytisknutÌ
     */
    public static void printStocking(StockingPreview stockingPreview) throws JRException, SQLException {
        if (stockingPreview == null) {
            throw new JRException("é·dn· inventura");
        }
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("id", stockingPreview.getStockingIdListing());
        parametrs.put("priceName", DoSale.getPriceName(stockingPreview.getUsedPrice()));
        
        compile(REPORT_DIR + ReportFiles.STOCKING_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.STOCKING_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);            
        }
        
    }
    
    /**
     * Vytiskne jednu mÏsÌËnÌ rekapitulaci
     * @param monthRecap mÏsÌËnÌ rekapitulace k vytisknutÌ
     */
    public static void printRecap(MonthRecap monthRecap) throws JRException, SQLException {
        if (monthRecap == null) {
            throw new JRException("é·dn· rekapitulace");
        }
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("month", monthRecap.getDate().get(Calendar.MONTH)+1 );
        parametrs.put("year", monthRecap.getDate().get(Calendar.YEAR) );
        
        parametrs.put("start", new BigDecimal(monthRecap.getStart()).divide(Store.CENT) );
        parametrs.put("startDPH", new BigDecimal(monthRecap.getStartDPH()).divide(Store.CENT) );
        parametrs.put("startAndDPH", new BigDecimal(monthRecap.getStartAndDPH()).divide(Store.CENT) );
        
        parametrs.put("end", new BigDecimal(monthRecap.getEnd()).divide(Store.CENT) );
        parametrs.put("endDPH", new BigDecimal(monthRecap.getEndDPH()).divide(Store.CENT) );
        parametrs.put("endAndDPH", new BigDecimal(monthRecap.getEndAndDPH()).divide(Store.CENT) );
        
        compile(REPORT_DIR + ReportFiles.MONTH_RECAP_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.MONTH_RECAP_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);         
        }
    }
    
    /**
     * Vytiskne jednu roËnÌ rekapitulaci
     * @param monthRecap mÏsÌËnÌ rekapitulace k vytisknutÌ
     */
    public static void printRecapYear(YearRecap monthRecap) throws JRException, SQLException {
        if (monthRecap == null) {
            throw new JRException("é·dn· rekapitulace");
        }
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("month", monthRecap.getDate().get(Calendar.MONTH)+1 );
        parametrs.put("year", monthRecap.getDate().get(Calendar.YEAR) );
        
        parametrs.put("start", new BigDecimal(monthRecap.getStart()).divide(Store.CENT) );
        parametrs.put("startDPH", new BigDecimal(monthRecap.getStartDPH()).divide(Store.CENT) );
        parametrs.put("startAndDPH", new BigDecimal(monthRecap.getStartAndDPH()).divide(Store.CENT) );
        
        parametrs.put("profit", new BigDecimal(monthRecap.getProfit()).divide(Store.CENT) );
        parametrs.put("profitDPH", new BigDecimal(monthRecap.getProfitDPH()).divide(Store.CENT) );
        parametrs.put("profitAndDPH", new BigDecimal(monthRecap.getProfitAndDPH()).divide(Store.CENT) );

        parametrs.put("release", new BigDecimal(monthRecap.getRelease()).divide(Store.CENT) );
        parametrs.put("releaseDPH", new BigDecimal(monthRecap.getReleaseDPH()).divide(Store.CENT) );
        parametrs.put("releaseAndDPH", new BigDecimal(monthRecap.getReleaseAndDPH()).divide(Store.CENT) );

        parametrs.put("end", new BigDecimal(monthRecap.getEnd()).divide(Store.CENT) );
        parametrs.put("endDPH", new BigDecimal(monthRecap.getEndDPH()).divide(Store.CENT) );
        parametrs.put("endAndDPH", new BigDecimal(monthRecap.getEndAndDPH()).divide(Store.CENT) );
        
        parametrs.put("year", monthRecap.getYear() );
        parametrs.put("author", monthRecap.getAuthor() );
        
        compile(REPORT_DIR + ReportFiles.YEAR_RECAP_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.YEAR_RECAP_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);         
        }
        
    }    
    
    /**
     * Vytiskne souhrn p¯Ìjemk za danÈ obdobÌ
     * @param start poË·tek obdobÌ
     * @param end konec obdobÌ
     */
    public static void printBuyRecap(java.util.Date start, java.util.Date end) throws JRException, SQLException {
        
        if (end.getTime() < start.getTime()) {
            throw new JRException("Zad·no koneËnÈ datum menöÌ neû poË·teËnÌ");
        }
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("start", new Timestamp(start.getTime()) );
        parametrs.put("end", new Timestamp(end.getTime()) );
        
        //zÌskej souËty
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(""+
                "SELECT SUM(total_nc), SUM(total_dph), SUM(total_nc_dph) FROM " + DatabaseAccess.BUY_LISITNG_TABLE_NAME + " " +
                "WHERE DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) ");
        pstm.setTimestamp(1, new Timestamp(start.getTime()));
        pstm.setTimestamp(2, new Timestamp(end.getTime()));
        ResultSet rs = pstm.executeQuery();
        
        if (rs.next()) {
            parametrs.put("totalNC", rs.getBigDecimal(1));
            parametrs.put("totalDPH", rs.getBigDecimal(2));
            parametrs.put("totalNCDPH", rs.getBigDecimal(3));
        }        
        
        compile(REPORT_DIR + ReportFiles.BUY_RECAP_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.BUY_RECAP_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);            
        }
        
    }
    
    
    /**
     * Vytiskne souhrn v˝dejek za danÈ obdobÌ
     * @param start poË·tek obdobÌ
     * @param end konec obdobÌ
     */
    public static void printSaleRecap(java.util.Date start, java.util.Date end) throws JRException, SQLException {
        
        
        if (end.getTime() < start.getTime()) {
            throw new JRException("Zad·no koneËnÈ datum menöÌ neû poË·teËnÌ");
        }
        
        // Nastav parametry tisku
        HashMap<String, Object> parametrs = new HashMap<String, Object>();
        parametrs.put("start", new Timestamp(start.getTime()) );
        parametrs.put("end", new Timestamp(end.getTime()) );
        
        //zÌskej souËty
        PreparedStatement pstm = DatabaseAccess.getCurrentConnection().prepareStatement(""+
                "SELECT SUM(total_pc), SUM(total_dph), SUM(total_pc_dph) FROM " + DatabaseAccess.SALE_LISTING_TABLE_NAME + " " +
                "WHERE DATE(date) >= DATE(?) AND DATE(date) <= DATE(?) ");
        pstm.setTimestamp(1, new Timestamp(start.getTime()));
        pstm.setTimestamp(2, new Timestamp(end.getTime()));
        ResultSet rs = pstm.executeQuery();
        
        if (rs.next()) {
            parametrs.put("totalNC", rs.getBigDecimal(1));
            parametrs.put("totalDPH", rs.getBigDecimal(2));
            parametrs.put("totalNCDPH", rs.getBigDecimal(3));
        } 
        
        compile(REPORT_DIR + ReportFiles.SALE_RECAP_REPORT.getFileName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                REPORT_DIR + ReportFiles.SALE_RECAP_REPORT.getJasperFileName(), parametrs, DatabaseAccess.getCurrentConnection());
        
        if (Settings.isShowPrintPreview()) {
            viewReport(jasperPrint, "V˝pis skladov˝ch karet");
        } else {
            //vytiskni
            JasperPrintManager.printReport(jasperPrint, SHOW_PRINT_DIALOG);           
        }
        
    }    
    
    /**
     * Zkompiluje .jrxml soubor do .jasper souboru. Pokud nen? kompilace pot?ebn?, nic neprovede
     * @param sourceFile .jrxml soubor
     * @param compFile v?sledn? .jasper soubor
     * @throws net.sf.jasperreports.engine.JRException 
     */
    public static String compile(File sourceFile, File compFile) throws JRException {
        
        // Pokud zkompilovan? soubor neexistuje, Nebo je star?? ne? zdrojov?,
        // zkompiluj. Jinak pou?ij ji? zkompilovan?
        if (!compFile.exists() || compFile.lastModified() < sourceFile.lastModified()) {
            JasperCompileManager.compileReportToFile(sourceFile.getPath(), compFile.getPath());
        }    
        
        return compFile.getPath();
    }
    
    
    /**
     * Zkompiluje .jrxml soubor do .jasper souboru. Ov?em kompiluje pouze pokud je to pot?eba.
     * Tedy pokud .jasper soubor neexistuje, nebo je star?? ne? .jrxml soubor
     * @param fileName 
     */
    public static String compile(String fileName) throws JRException {
        String partName = fileName;
        
        //O??zni o koncovku pokud je uvedena
        if (fileName.lastIndexOf(".") != -1) {
            partName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        
        File sourceFile = new File(partName + ".jrxml");
        File compFile = new File(partName + ".jasper");
        
        return compile(sourceFile, compFile);        
    }    
    
    public static void viewReport(JasperPrint jprint, String title) {
        JasperViewer view = new JasperViewer(jprint, false);

        view.setVisible(true);
        view.setTitle(title);
    }

}
