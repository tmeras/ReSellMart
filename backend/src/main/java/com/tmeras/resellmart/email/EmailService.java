package com.tmeras.resellmart.email;

import com.tmeras.resellmart.common.AppConstants;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendActivationEmail(
            String to,
            String subject,
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
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
