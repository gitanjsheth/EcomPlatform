package com.gitanjsheth.productservice.messaging;

import com.gitanjsheth.productservice.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "inventory.events", groupId = "product-service")
    public void handleInventoryEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            String orderId = (String) event.get("orderId");
            List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");

            log.info("Received inventory event {} for order {} with {} items", eventType, orderId, items != null ? items.size() : 0);

            if (items == null) {
                return;
            }

            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();
                String action = (String) item.get("action");

                switch (action) {
                    case "RESERVE":
                        // Use orderId hash to create a stable, segregated hold key when userId is unavailable
                        Long holdKeyUserId = orderId != null ? Math.abs(orderId.hashCode()) + 0L : -1L;
                        inventoryService.reserveInventoryForCheckout(productId, quantity, holdKeyUserId);
                        break;
                    case "RELEASE":
                        Long releaseKeyUserId = orderId != null ? Math.abs(orderId.hashCode()) + 0L : -1L;
                        inventoryService.releaseReservedInventory(productId, quantity, releaseKeyUserId);
                        break;
                    case "CONFIRM":
                        Long confirmKeyUserId = orderId != null ? Math.abs(orderId.hashCode()) + 0L : -1L;
                        inventoryService.confirmInventoryUsage(productId, quantity, confirmKeyUserId);
                        break;
                    default:
                        log.warn("Unknown inventory action: {}", action);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process inventory event: {}", event, e);
        }
    }
}


