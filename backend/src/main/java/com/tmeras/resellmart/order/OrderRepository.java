package com.tmeras.resellmart.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // TODO: Orders must be paid
    Page<Order> findAllByBuyerId(Pageable pageable, Integer buyerId);
}
