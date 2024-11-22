package main.arbitrage.domain.email.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.email.entity.EmailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Random;

import static org.springframework.security.core.context.SecurityContextHolder.setContext;

@Service
@RequiredArgsConstructor
public class EmailMessageService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    public String sendMail(EmailMessage emailMessage, String type) throws Exception {
        String authNum = generateCode();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
        mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
        mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
        javaMailSender.send(mimeMessage);
        
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