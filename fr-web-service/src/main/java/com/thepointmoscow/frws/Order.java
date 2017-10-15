package com.thepointmoscow.frws;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
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
    private List<Item> items = Collections.emptyList();
    private List<Payment> payments = Collections.emptyList();
    private Boolean isElectronic;

    @Data
    @Accessors(chain = true)
    private static class Firm {
        private String timezone;
    }

    @Data
    @Accessors(chain = true)
    public static class Cashier {
        private String firstName;
        private String lastName;

        @Override
        public String toString() {
            return firstName + " " + lastName;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Customer {
        private String phone;
        private String email;

        public String getId() {
            return !Strings.isNullOrEmpty(email) ? email : phone;
        }
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
