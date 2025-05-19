package com.tmeras.resellmart.order;

import com.stripe.exception.StripeException;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<Map<String, String>> save(
            @Valid @RequestBody OrderRequest orderRequest,
            Authentication authentication
    ) throws MessagingException, IOException, StripeException {
        String redirectUrl = orderService.save(orderRequest, authentication);
        Map<String, String> response = Map.of("redirectUrl", redirectUrl);

        // Redirect to stripe checkout page
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/orders/stripe-webhook")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws StripeException, MessagingException {
        String response = orderService.handleStripeEvent(payload, sigHeader);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/orders/fulfill")
    public ResponseEntity<?> fulfillOrder(
            @RequestBody Map<String, String> body
    ) throws MessagingException, StripeException {
        orderService.fulfillOrder(body.get("stripeSessionId"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<PageResponse<OrderResponse>> findAll(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageResponse<OrderResponse> foundOrders = orderService.findAll(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(foundOrders, HttpStatus.OK);
    }

    @GetMapping("/users/{user-id}/purchases")
    public ResponseEntity<PageResponse<OrderResponse>> findAllByBuyerId(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @PathVariable(name = "user-id") Integer buyerId,
            Authentication authentication
    ) {
        PageResponse<OrderResponse> foundOrders =
                orderService.findAllByBuyerId(pageNumber, pageSize, sortBy, sortDirection, buyerId, authentication);
        return new ResponseEntity<>(foundOrders, HttpStatus.OK);
    }

    @GetMapping("/users/{user-id}/sales")
    public ResponseEntity<PageResponse<OrderResponse>> findAllByProductSellerId(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @PathVariable(name = "user-id") Integer productSellerId,
            Authentication authentication
    ) {
        PageResponse<OrderResponse> foundOrders =
                orderService.findAllByProductSellerId(pageNumber, pageSize, sortBy, sortDirection, productSellerId, authentication);
        return new ResponseEntity<>(foundOrders, HttpStatus.OK);
    }

    @DeleteMapping("/orders/{order-id}")
    public ResponseEntity<OrderResponse> delete(@PathVariable(name = "order-id") Integer orderId) {
        orderService.delete(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
