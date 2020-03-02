/*
 * TypeOfGoods.java
 *
 * Created on 17. øíjen 2005, 0:03
 */

package cz.control.business;

/**
 * Program Control - Skladový systém
 *
 * Výètový typ pro zboží. JEdná o jaký typ zboží se jedná (bìžné zboží, sada, komplet...)
 * 
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public enum TypeOfGoods {
    /**
     * Signalizuje klasické zboží
     */
    GOODS (0),
    /**
     * Chybnì udaný typ zboží
     */
    ERROR (-1);
    
    private int type;
    
    private TypeOfGoods(int type) {
        this.type = type;
    }
    
    /**
     * Vrací typ zboží
     * @param type èíslo pøedstavující typ
     * @return položku z vyjmenovaného typu
     */
    public static TypeOfGoods getTypeOfGoods(int type) {
        switch (type) {
            case 0 : 
                return GOODS;
            default :
                return ERROR;
        }
    }
    
    /**
     * Vrací øetìzec popisující pøíslušný výèet
     * @return øetìzec popisující výèet
     */
    public String toString() {
        switch (type) {
            case 0 : 
                return "Zboží";
            default :
                return "?";
        }
    }

    /**
     * Vrací èíslo reprezentující tento Typ
     * @return èíslo reprezentující tento typ
     */
    public int getType() {
        return type;
    }
    
}
