package com.tmeras.resellmart.order;

import java.math.BigDecimal;

public interface OrderStatsResponse {
    Long getMonthlyOrderCount();

    Integer getMonthlyProductSales();

    BigDecimal getMonthlyRevenue();
}
