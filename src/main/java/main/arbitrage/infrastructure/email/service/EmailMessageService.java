package main.arbitrage.infrastructure.email.service;

import java.util.Random;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.email.dto.EmailMessageDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailMessageService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    public String sendMail(EmailMessageDto emailMessageDto, String type) throws Exception {
        String authNum = generateCode();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper =
                    new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessageDto.getTo());
            mimeMessageHelper.setSubject(emailMessageDto.getSubject());
            mimeMessageHelper.setText(setContext(authNum, type), true);

            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            // Gmail open smtp server에서는 잘못된 수신자에게 발송시 Error를 반환하지 않는다 한다.
            // Client딴에서 처리하자
            log.error("Failed to send email. Invalid email address: {}", emailMessageDto.getTo(),
                    e);
            throw new IllegalArgumentException("Invalid email address.", e);
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email.", e);
        }
        return authNum;
    }

    public String generateCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4); // 0 ~ 3

            // ascii 코드표 참조
            switch (index) {
                case 1:
                    key.append((char) ((int) random.nextInt(26) + 97)); // 소문자
                    break;
                case 2:
                    key.append((char) ((int) random.nextInt(26) + 65)); // 대문자
                    break;
                default:
                    key.append(random.nextInt(10)); // 숫자
            }
        }

        return key.toString();
    }

    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }
}
