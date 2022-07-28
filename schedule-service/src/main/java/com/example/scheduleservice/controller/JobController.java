package com.example.scheduleservice.controller;

import com.example.scheduleservice.model.JobRunData;
import com.example.scheduleservice.model.TransactionDetails;
import com.example.scheduleservice.service.EmailJobService;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping( produces="application/json")
public class JobController {

    private final JobScheduler jobScheduler;

    private final EmailJobService emailJobService;

    @Autowired
    public JobController( JobScheduler jobScheduler, EmailJobService emailJobService) {
        this.jobScheduler = jobScheduler;
        this.emailJobService = emailJobService;
    }

    @PostMapping("/run-job")
    public JobRunData runJob(@RequestBody TransactionDetails transactionDetails ) {

        jobScheduler.scheduleRecurrently( "job"+transactionDetails.getUniqueId(),Cron.minutely(),
                () -> emailJobService.execute(transactionDetails)
        );

        return new JobRunData("Job is enqueued.");

    }

}
