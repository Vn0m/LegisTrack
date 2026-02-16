package com.legistrack.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@SuppressWarnings("null")
public class AwsSqsService {
    private static final Logger logger = LoggerFactory.getLogger(AwsSqsService.class);
    
    private final SqsTemplate sqsTemplate;
    private final String queueUrl;
    private final ObjectMapper mapper;

    public AwsSqsService(SqsTemplate sqsTemplate,
                         @Value("${app.aws.sqs-queue-url}") String queueUrl,
                         ObjectMapper mapper) {
        this.sqsTemplate = sqsTemplate;
        this.queueUrl = queueUrl;
        this.mapper = mapper;
    }

    @Async
    public void publishBillStatusChange(String userId, String billId, String billTitle, 
                                         String oldStatus, String newStatus) {
        try {
            Map<String, Object> message = Map.of(
                "type", "BILL_STATUS_CHANGED",
                "userId", userId,
                "billId", billId,
                "billTitle", billTitle,
                "oldStatus", oldStatus != null ? oldStatus : "unknown",
                "newStatus", newStatus,
                "timestamp", System.currentTimeMillis()
            );

            String payload = mapper.writeValueAsString(message);

            sqsTemplate.send(to -> to.queue(queueUrl).payload(payload));
            
            logger.info("Published bill status change to SQS: bill={} status={} -> {}", 
                billId, oldStatus, newStatus);
        } catch (Exception e) {
            logger.error("Failed to publish to SQS for bill {}: {}", billId, e.getMessage(), e);
        }
    }
}
