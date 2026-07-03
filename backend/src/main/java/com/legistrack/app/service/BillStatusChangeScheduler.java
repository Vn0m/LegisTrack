package com.legistrack.app.service;

import com.legistrack.app.model.Bill;
import com.legistrack.app.model.SavedBill;
import com.legistrack.app.repository.BillRepository;
import com.legistrack.app.repository.SavedBillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BillStatusChangeScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BillStatusChangeScheduler.class);
    
    private final BillRepository billRepository;
    private final BillService billService;
    private final SavedBillRepository savedBillRepository;
    private final NotificationService notificationService;

    public BillStatusChangeScheduler(BillRepository billRepository,
                                     BillService billService,
                                     SavedBillRepository savedBillRepository,
                                     NotificationService notificationService) {
        this.billRepository = billRepository;
        this.billService = billService;
        this.savedBillRepository = savedBillRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkBillStatusChanges() {
        logger.info("Starting daily bill status check scheduler");

        try {
            List<SavedBill> allSavedBills = savedBillRepository.findAllWithBill();
            Set<UUID> checkedBillIds = new HashSet<>();
            int updatedCount = 0;

            for (SavedBill savedBill : allSavedBills) {
                Bill bill = savedBill.getBill();
                
                if (!checkedBillIds.add(bill.getId())) {
                    continue;
                }

                try {
                    String previousStatus = bill.getStatus();
                    

                    String basePrintNoStr = bill.getBasePrintNoStr();
                    int dashIndex = basePrintNoStr.lastIndexOf('-');
                    if (dashIndex == -1) {
                        logger.warn("Invalid basePrintNoStr format: {}", basePrintNoStr);
                        continue;
                    }
                    String billId = basePrintNoStr.substring(0, dashIndex);
                    String year = basePrintNoStr.substring(dashIndex + 1);


                    String latestStatus = billService.fetchFreshBillStatus(year, billId);

                    if (latestStatus != null && !latestStatus.equals(previousStatus)) {
                        bill.setStatus(latestStatus);
                        billRepository.save(bill);
                        
                        notificationService.checkAndNotifyBillStatusChanges(bill, previousStatus);
                        updatedCount++;
                        
                        logger.info("Bill {} status updated: {} -> {}", 
                            basePrintNoStr, previousStatus, latestStatus);
                    }
                } catch (RestClientException e) {
                    logger.warn("Failed to fetch status for bill {}: {}",
                        bill.getBasePrintNoStr(), e.getMessage());
                }
            }

            logger.info("Completed daily bill status check. Checked {} bills, updated {}", 
                checkedBillIds.size(), updatedCount);
        } catch (Exception e) {
            logger.error("Error in bill status change scheduler: {}", e.getMessage(), e);
        }
    }
}
