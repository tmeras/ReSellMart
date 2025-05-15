package com.tmeras.resellmart.order;

import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private PaymentMethod paymentMethod; //TODO: Modify after Stripe integration

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // ID of the associated Stripe Checkout session
    private String stripeCheckoutId;

    private String billingAddress;

    private String deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User buyer;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "orderId", nullable = false)
    private List<OrderItem> orderItems;

    // TODO: Return in response + in cart???
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;

        // TODO: Verify correctness
        for (OrderItem orderItem : orderItems) {
            BigDecimal productPrice = orderItem.getProductPrice();
            Integer productQuantity = orderItem.getProductQuantity();
            totalPrice = totalPrice.add(productPrice.multiply(BigDecimal.valueOf(productQuantity)));
        }

        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
}
