package com.tmeras.resellmart.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // TODO: Orders for both  must be paid
    Page<Order> findAllByBuyerId(Pageable pageable, Integer buyerId);

    @Query("""
            SELECT distinct o
            FROM Order o
            JOIN o.orderItems oi
            WHERE oi.productSeller.id = :productSellerId
    """)
    Page<Order> findAllByProductSellerId(Pageable pageable, Integer productSellerId);
}
