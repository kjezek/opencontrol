/*
 * TypeOfGoods.java
 *
 * Created on 17. ��jen 2005, 0:03
 */

package cz.control.business;

/**
 * Program Control - Skladov� syst�m
 *
 * V��tov� typ pro zbo��. JEdn� o jak� typ zbo�� se jedn� (b�n� zbo��, sada, komplet...)
 * 
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public enum TypeOfGoods {
    /**
     * Signalizuje klasick� zbo��
     */
    GOODS (0),
    /**
     * Chybn� udan� typ zbo��
     */
    ERROR (-1);
    
    private int type;
    
    private TypeOfGoods(int type) {
        this.type = type;
    }
    
    /**
     * Vrac� typ zbo��
     * @param type ��slo p�edstavuj�c� typ
     * @return polo�ku z vyjmenovan�ho typu
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
     * Vrac� �et�zec popisuj�c� p��slu�n� v��et
     * @return �et�zec popisuj�c� v��et
     */
    public String toString() {
        switch (type) {
            case 0 : 
                return "Zbo��";
            default :
                return "?";
        }
    }

    /**
     * Vrac� ��slo reprezentuj�c� tento Typ
     * @return ��slo reprezentuj�c� tento typ
     */
    public int getType() {
        return type;
    }
    
}
