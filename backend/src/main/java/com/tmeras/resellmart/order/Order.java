package com.tmeras.resellmart.order;

import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "CustomerOrder")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private ZonedDateTime placedAt;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Address billingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Address deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User buyer;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "orderId", nullable = false)
    private List<OrderItem> orderItems;

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;

        // TODO: Verify correctness
        for (OrderItem orderItem : orderItems) {
            BigDecimal productPrice = orderItem.getProduct().getPrice();
            Integer productQuantity = orderItem.getProductQuantity();
            totalPrice = totalPrice.add(productPrice.multiply(BigDecimal.valueOf(productQuantity)));
        }

        return totalPrice;
    }
}
