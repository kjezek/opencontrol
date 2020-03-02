/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.control.gui;

import cz.control.business.Settings;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Program Control - Skladový systém
 *
 * Tabulka použitá napøíè aplikací
 *
 * @author Kamil Ježek
 *
 * (C) 2011, ver. 1.0
 */
public class CommonTable extends JTable {

    /**
     *
     * @param dm table model
     */
    public CommonTable(TableModel dm) {
        super(dm);
        initHeight();
    }

    /**
     * 
     */
    public CommonTable() {
        super();
        initHeight();
    }

    /**
     * This method initialises a height of
     * the table.
     */
    private void initHeight() {
        this.setRowHeight(
                this.getFontMetrics(
                new Font("Dialog", Font.BOLD, Settings.getMainItemsFontSize()))
                .getHeight()
                );
    }


}
