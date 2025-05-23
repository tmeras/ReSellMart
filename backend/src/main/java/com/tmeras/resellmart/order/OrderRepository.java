package com.tmeras.resellmart.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("""
                    SELECT o
                    FROM Order o
                    WHERE o.buyer.id = :buyerId
                    AND o.status = 'PAID'
            """)
    Page<Order> findAllPaidByBuyerId(Pageable pageable, Integer buyerId);

    @Query("""
            SELECT distinct o
            FROM Order o
            JOIN o.orderItems oi
            WHERE oi.productSeller.id = :productSellerId
                            AND o.status = 'PAID'
    """)
    Page<Order> findAllPaidByProductSellerId(Pageable pageable, Integer productSellerId);

    @EntityGraph(attributePaths = {"buyer", "orderItems.product", "orderItems.productSeller"})
    Optional<Order> findWithProductsAndBuyerDetailsByStripeCheckoutId(String stripeCheckoutId);

    @EntityGraph(attributePaths = {"buyer", "orderItems.product", "orderItems.productSeller"})
    Optional<Order> findWithProductsAndBuyerDetailsById(Integer orderId);
}
