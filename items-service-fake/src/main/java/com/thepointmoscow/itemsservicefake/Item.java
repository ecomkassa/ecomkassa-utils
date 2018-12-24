package com.thepointmoscow.itemsservicefake;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @Column(name = "item_id")
    private String itemId;
    @Column(name = "name")
    private String name;
    @Column(name = "sku")
    private String sku;
    @Column(name = "size")
    private String size;

    /**
     * The price is intentionally set equals to a one kopeck.
     */
    // @Column(name = "price")
    @Transient
    private Integer price = 1;

    @Transient
    private final String vatType = "VAT_18PCT";

    public Integer getPrice() {
        return price;
    }
}
