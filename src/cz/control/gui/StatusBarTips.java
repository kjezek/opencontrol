/*
 * StatusBarTips.java
 *
 * Vytvoøeno 30. bøezen 2006, 18:04
 *
 * Autor: Kamil Ježek
 * email: kjezek@students.zcu.cz
 */

package cz.control.gui;

/**
 * Program Control - Skladový systém
 *
 * Vyjmenovaný typ, pøedstavující rychlé nápovìdy zobrazované ve stavovém øádku
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public enum StatusBarTips {
    EMPTY(""),
    GOODS_TIP("  Ins - Nová, Alt+Ins - Editace, Del - Smazat, Ctrl+P - Tisk"),
    CUSTOMER_TIP("  Ins - Nová, Alt+Ins - Editace, Del - Smazat"),
    TRADE_TIP("  Ins - Nová, Alt+Ins - Editace, Del - Smazat, Ctrl+P - Tisk, šipky - Výbìr tabulky"),
    STOCKING_TIP("  Ins - Nová, Alt+Ins - Editace, Del - Smazat, Ctrl+P - Tisk, šipky - Výbìr tabulky"),
    RECAP_TIP("  Mezerník - Potvrdit/zrušit, Ctrl+P - Tisk"),
    ACCOUNT_TIP("  Ins - Nová, Alt+Ins - Editace, Del - Smazat"),
    DO_TRADE_TIP("  Ins - Vložit, Alt+Ins - Vložit ze skladu, Del - Smazat, Alt+ESC - Zrušit, Alt+Enter - Potvrdit"),
    CANCEL_CONFIRM("  Alt+ESC - Zrušit, Alt+Enter - Potvrdit")
    
    ;
    
    private String tipText = "";
    
    private StatusBarTips(String tipText) {
        this.tipText = tipText;
    }

    public String getText() {
        return "<html>" + tipText + "</html>";
    }
}
