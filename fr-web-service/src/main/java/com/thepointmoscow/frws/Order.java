package com.thepointmoscow.frws;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Order {
    private Long _id;
    private String orderType;
    private String status;
    private String saleCharge;
    private Firm firm;
    private Cashier cashier;
    private Customer customer;
    private List<Item> items;
    private List<Payment> payments;
    private Boolean isElectronic;

    @Data
    @Accessors(chain = true)
    public static class Firm {
        private String timezone;
    }

    @Data
    @Accessors(chain = true)
    public static class Cashier {
        private String firstName;
        private String lastName;
    }

    @Data
    @Accessors(chain = true)
    public static class Customer {
        private String phone;
        private String email;
    }

    @Data
    @Accessors(chain = true)
    public static class Item {
        private String name;
        private Long price;
        private Long amount;
        private String vatType;
    }

    @Data
    @Accessors(chain = true)
    public static class Payment {
        private String paymentType;
        private Long amount;
    }
}
