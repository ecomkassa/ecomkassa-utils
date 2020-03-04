package com.thepointmoscow.itemsservicefake;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @Column(name = "item_id")
    private Long itemId;
    @Column(name = "name")
    private String name;
    @Column(name = "sku")
    private String sku;

    @Column(name = "price", columnDefinition = "decimal(14,2)")
    private BigDecimal price;

    @Column(name = "vat_type")
    private String vatType;

    @Column(name = "payment_object")
    private String paymentObject;

    @Column(name = "tax_identity")
    private String taxIdentity;
}
