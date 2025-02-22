package com.tmeras.resellmart.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> save(
            @Valid @RequestBody OrderRequest orderRequest,
            Authentication authentication
    ) {
        OrderResponse orderResponse = orderService.save(orderRequest, authentication);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }
}
