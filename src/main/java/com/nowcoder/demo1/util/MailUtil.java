package com.nowcoder.demo1.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
    @Autowired
    private JavaMailSenderImpl mailSender;
//    发送方
    @Value("${spring.mail.username}")
    private String from;
    @Value("${mail.from.name}")
    private String fromName;

    public void sentMail(String to,String subject,String content){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from,fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content,true);
//            别忘记调方法
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
           logger.error("发送邮件失败，原因："+e.getMessage());
        }catch (UnsupportedEncodingException e) {
            logger.error("发件人显示名称异常"+e.getMessage());
        }
    }

}
