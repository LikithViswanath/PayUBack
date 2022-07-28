package com.example.scheduleservice.service;

import com.example.scheduleservice.model.EmailDetails;
import com.example.scheduleservice.model.TransactionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Value("${spring.mail.username}") private String sender;

    public void sendSimpleMail(TransactionDetails transactionDetails)
    {

        Logger logger = LoggerFactory.getLogger(EmailService.class);

        double difference = 0;

        if( transactionDetails.getAmountPayedBack() != null && transactionDetails.getAmountPayed() != null ){
            difference = ( transactionDetails.getAmountPayed() - transactionDetails.getAmountPayedBack() );
        }

        EmailDetails emailDetails = new EmailDetails(
                transactionDetails.getTransactionId().getFromEmail(),
                "The amount yet to pay to " + transactionDetails.getTransactionId().getToEmail()
                + " is : " + ( difference ),
                "Please Pay the loan amount",
                null
        );

        try {
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(emailDetails.getRecipient());
            mailMessage.setText(emailDetails.getMsgBody());
            mailMessage.setSubject(emailDetails.getSubject());

            javaMailSender.send(mailMessage);
            logger.info("Mail Sent Successfully...");
        }

        catch (Exception e) {
            logger.info("Error while Sending Mail: " + e);
        }
    }

}
