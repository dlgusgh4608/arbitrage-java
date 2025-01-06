package main.arbitrage.infrastructure.email.service;

import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.infrastructure.email.dto.EmailMessageDTO;
import main.arbitrage.infrastructure.email.exception.SendMailErrorCode;
import main.arbitrage.infrastructure.email.exception.SendMailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailMessageService {
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  public String sendMail(EmailMessageDTO emailMessageDTO, String type) {
    String authNum = generateCode();

    MimeMessage mimeMessage = javaMailSender.createMimeMessage();

    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
      mimeMessageHelper.setTo(emailMessageDTO.getTo());
      mimeMessageHelper.setSubject(emailMessageDTO.getSubject());
      mimeMessageHelper.setText(setContext(authNum, type), true);

      javaMailSender.send(mimeMessage);
      return authNum;

    } catch (MailSendException e) {
      throw new SendMailException(SendMailErrorCode.INVALID_MAIL, e);
    } catch (MessagingException e) {
      throw new SendMailException(SendMailErrorCode.UNKNOWN, e);
    } catch (jakarta.mail.MessagingException e) {
      throw new SendMailException(SendMailErrorCode.UNKNOWN, e);
    }
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
