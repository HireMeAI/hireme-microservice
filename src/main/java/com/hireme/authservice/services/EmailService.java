package com.hireme.authservice.services;

import com.hireme.authservice.dtos.EmailDto;
import com.hireme.authservice.exception.ApiException;
import com.hireme.authservice.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.mail.from.address}")
    private String fromAddress;

    @Value("${app.mail.from.name}")
    private String fromName;
    private static final String ENCODING= "UTF-8";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    public void sendEmail(EmailDto emailDto) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING);

        helper.setTo(emailDto.getSentTo());
        helper.setSubject(emailDto.getSubject());
        helper.setText(emailDto.getTextBody(), true); // true = HTML
        helper.setFrom(fromAddress, fromName);

        mailSender.send(message);
    }

    public void sendHtmlEmailWithTemplate(String to, String[] cc, String subject, String templateName, Map<String, Object> variables) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING);

        try {
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setTo(to);
            if (cc != null && cc.length > 0) {
                helper.setBcc(cc);
            }
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromAddress, fromName);

            mailSender.send(message);
        } catch (TemplateInputException e) {
            throw new ApiException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND, "Template not found: " + templateName);
        }
    }
}
