package com.example.scheduleservice.service;

import com.example.scheduleservice.model.TransactionDetails;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailJobService {

    private final EmailService emailService;

    private final RestTemplate restTemplate;

    @Autowired
    public EmailJobService(EmailService emailService) {
        this.emailService = emailService;
        this.restTemplate = new RestTemplate();
    }

    @Job( name = "email job")
    public void execute(TransactionDetails transactionDetails) {

        emailService.sendSimpleMail(transactionDetails);

        Map<String, String> vars = new HashMap<>();
        vars.put("uniqueId", transactionDetails.getUniqueId());

        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

        System.out.println("Doing the Job");

        ResponseEntity<TransactionDetails> responseEntity = restTemplate
                .exchange( "http://transaction-service:9004/transaction/{uniqueId}"
                        , HttpMethod.GET, null, TransactionDetails.class,vars);

        TransactionDetails transactionDetailsResponse = responseEntity.getBody();

        transactionDetails = transactionDetailsResponse;

        System.out.println(transactionDetailsResponse);

        if( transactionDetailsResponse!=null ){

           if( transactionDetailsResponse.getAmountPayedBack() != null && transactionDetailsResponse.getAmountPayed() != null ) {

               System.out.println(transactionDetailsResponse.getAmountPayedBack());
               System.out.println(transactionDetailsResponse.getAmountPayed());

               if (transactionDetailsResponse.getAmountPayedBack().equals(transactionDetails.getAmountPayed()) || (
                       transactionDetailsResponse.getAmountPayedBack().equals(transactionDetails.getAmountPayed())
                               && transactionDetailsResponse.getAmountPayed() != 0
               )) {
                   BackgroundJob.delete("job"+transactionDetails.getUniqueId());

                   System.out.println("both paid");

               }
           }
       }

    }

}
