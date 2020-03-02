/*
 */

package cz.control.data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Tøída reprezentuje historickou hodnotu obchodování se zbožím
 * 
 * Obsahuje informace z pøíjemky/výdejky a odkaz na dodavatele/odbìratele
 * @author kamilos
 */
public class GoodsTradesHistory implements Comparable<GoodsTradesHistory>{

    private String suplier;
    
    private Integer itemId;
    private Integer number;
    private Date date;
    private Double quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal dph;
    private ItemTypes itemType;
    
    public GoodsTradesHistory() {
    }

    public GoodsTradesHistory(Integer itemId, String suplier, Integer number, Date date, Double quantity, String unit, BigDecimal price, BigDecimal dph) {
        this.suplier = suplier;
        this.number = number;
        this.date = date;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.dph = dph;
        this.itemId = itemId;
    }

    public GoodsTradesHistory(Integer itemId, String suplier, Integer number, Date date, Double quantity, String unit, BigDecimal price, BigDecimal dph, ItemTypes itemType) {
        this.suplier = suplier;
        this.number = number;
        this.date = date;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.dph = dph;
        this.itemType = itemType;
        this.itemId = itemId;
    }
    
    /**
     * Nastaví pøíslušný typ položky
     * @param type
     */
    public void setItemType(int type) {
        for (ItemTypes item: ItemTypes.values()) {
            if (item.getType() == type) {
                this.itemType = item;
                break;
            }
        }
    }

    
    public int compareTo(GoodsTradesHistory o) {
        int result = number - o.getNumber();
        
        if (result == 0) {
            result = date.compareTo(o.getDate());
        }
        
        if (result == 0) {
            result = suplier.compareTo(o.getSuplier());
        }
        
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoodsTradesHistory other = (GoodsTradesHistory) obj;
        if (this.suplier != other.suplier && (this.suplier == null || !this.suplier.equals(other.suplier))) {
            return false;
        }
        if (this.number != other.number && (this.number == null || !this.number.equals(other.number))) {
            return false;
        }
        if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
            return false;
        }
        if (this.quantity != other.quantity && (this.quantity == null || !this.quantity.equals(other.quantity))) {
            return false;
        }
        if (this.unit != other.unit && (this.unit == null || !this.unit.equals(other.unit))) {
            return false;
        }
        if (this.price != other.price && (this.price == null || !this.price.equals(other.price))) {
            return false;
        }
        if (this.dph != other.dph && (this.dph == null || !this.dph.equals(other.dph))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = quantity.intValue();
        return hash;
    }

    public ItemTypes getItemType() {
        return (itemType == ItemTypes.SALE && suplier == null) ? ItemTypes.DISCOUNT : itemType;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
    
    public enum ItemTypes {
        SALE(1, "Výdejka"),
        BUY(2, "Pøíjemka"),
        DISCOUNT(3, "Prodejka");
        
        private int type;
        private String description;
        
        private ItemTypes(int type, String description) {
            this.type = type;
            this.description = description;
        }

        public int getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

    }
    

    public String getSuplier() {
        return suplier;
    }
    
    /**
     * Vrací jméno dodavatele, nebo text "Maloobchod" pokud je dodavatel null
     * @return
     */
    public String getSuplierRealName() {
        return suplier == null ? "Maloobchod" : suplier;
    }

    public void setSuplier(String suplier) {
        this.suplier = suplier;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getQuantity() {
        return quantity;
    }
    
    /**
     * Vrací množství se znaménkem. Pokud se jedná o nákup, znaménko je kladné
     * pokud se jedná o prodej je znaménko záporné
     * @return
     */
    public Double getQuantityWithSign() {
        return (itemType == ItemTypes.BUY) ? quantity : -quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
    

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDph() {
        return dph;
    }

    public void setDph(BigDecimal dph) {
        this.dph = dph;
    }

    @Override
    public String toString() {
        return itemId + " " + number + " " + date;
    }
    
    
}
