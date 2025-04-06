package com.tmeras.resellmart.order;

import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    private LocalDateTime placedAt;

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

    public Double getTotalPrice() {
        Double totalPrice = 0.0;

        // TODO: Fix by multiplying by quantity
        for (OrderItem orderItem : orderItems)
            totalPrice += orderItem.getProduct().getPrice();

        return totalPrice;
    }
}
