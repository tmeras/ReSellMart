package com.tmeras.resellmart.email;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.order.Order;
import com.tmeras.resellmart.order.OrderItem;
import com.tmeras.resellmart.product.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendActivationEmail(
            String to,
            String username,
            String activationUrl,
            String activationCode
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("activationUrl", activationUrl);
        variables.put("activationCode", activationCode);
        Context context = new Context();
        context.setVariables(variables);
        String template = templateEngine.process(AppConstants.USER_ACTIVATION_TEMPLATE, context);

        mimeMessageHelper.setFrom("resellmart@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("ReSellMart Account Activation");
        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }

    public void sendPurchaseConfirmationEmail(
            String to,
            Order order
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("order", order);
        Context context = new Context();
        context.setVariables(variables);
        String template = templateEngine.process(AppConstants.PURCHASE_CONFIRMATION_TEMPLATE, context);

        mimeMessageHelper.setFrom("resellmart@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("ReSellMart Order Confirmation");
        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }

    public void sendOrderCancellationEmail(
            String to,
            Order order,
            Product product,
            Integer requestedQuantity
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("order", order);
        variables.put("product", product);
        variables.put("requestedQuantity", requestedQuantity);
        Context context = new Context();
        context.setVariables(variables);
        String template = templateEngine.process(AppConstants.ORDER_CANCELLATION_TEMPLATE, context);

        mimeMessageHelper.setFrom("resellmart@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("ReSellMart Order Cancellation");
        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }

    public void sendSaleConfirmationEmail(
            String to,
            String sellerName,
            Order order,
            List<OrderItem> orderItems,
            BigDecimal saleTotal
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("order", order);
        variables.put("sellerName", sellerName);
        variables.put("orderItems", orderItems);
        variables.put("saleTotal", saleTotal);
        Context context = new Context();
        context.setVariables(variables);
        String template = templateEngine.process(AppConstants.SALE_CONFIRMATION_TEMPLATE, context);

        mimeMessageHelper.setFrom("resellmart@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("ReSellMart Sale Notification");
        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
