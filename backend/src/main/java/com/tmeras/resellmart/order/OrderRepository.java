package com.tmeras.resellmart.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
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

    @Query(value = """
                    SELECT
                        COUNT(DISTINCT o.id) AS monthlyOrderCount,
                        SUM(oi.product_quantity) AS monthlyProductSales,
                        SUM(oi.product_price * oi.product_quantity) AS monthlyRevenue
                    FROM customer_order o
                    JOIN order_item oi ON o.id = oi.order_id
                    WHERE o.status = 'PAID'
                    AND o.placed_at >= :from AND o.placed_at < :to
            """, nativeQuery = true)
    OrderStatsResponse calculateStatistics(ZonedDateTime from, ZonedDateTime to);
}
