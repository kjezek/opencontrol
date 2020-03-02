/*
 * StatusBarTips.java
 *
 * Vytvo�eno 30. b�ezen 2006, 18:04
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

/**
 * Program Control - Skladov� syst�m
 *
 * Vyjmenovan� typ, p�edstavuj�c� rychl� n�pov�dy zobrazovan� ve stavov�m ��dku
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public enum StatusBarTips {
    EMPTY(""),
    GOODS_TIP("  Ins - Nov�, Alt+Ins - Editace, Del - Smazat, Ctrl+P - Tisk"),
    CUSTOMER_TIP("  Ins - Nov�, Alt+Ins - Editace, Del - Smazat"),
    TRADE_TIP("  Ins - Nov�, Alt+Ins - Editace, Del - Smazat, Ctrl+P - Tisk, �ipky - V�b�r tabulky"),
    STOCKING_TIP("  Ins - Nov�, Alt+Ins - Editace, Del - Smazat, Ctrl+P - Tisk, �ipky - V�b�r tabulky"),
    RECAP_TIP("  Mezern�k - Potvrdit/zru�it, Ctrl+P - Tisk"),
    ACCOUNT_TIP("  Ins - Nov�, Alt+Ins - Editace, Del - Smazat"),
    DO_TRADE_TIP("  Ins - Vlo�it, Alt+Ins - Vlo�it ze skladu, Del - Smazat, Alt+ESC - Zru�it, Alt+Enter - Potvrdit"),
    CANCEL_CONFIRM("  Alt+ESC - Zru�it, Alt+Enter - Potvrdit")
    
    ;
    
    private String tipText = "";
    
    private StatusBarTips(String tipText) {
        this.tipText = tipText;
    }

    public String getText() {
        return "<html>" + tipText + "</html>";
    }
}
