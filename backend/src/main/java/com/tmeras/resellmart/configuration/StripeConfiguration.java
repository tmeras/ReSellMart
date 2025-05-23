package com.tmeras.resellmart.configuration;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfiguration {

    public StripeConfiguration(@Value("${application.stripe.secret-key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }
}
