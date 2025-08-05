package com.gitanjsheth.ordermanagementservice.messaging;

import com.gitanjsheth.ordermanagementservice.events.InventoryEvent;
import com.gitanjsheth.ordermanagementservice.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Topic names
    private static final String ORDER_EVENTS_TOPIC = "order.events";
    private static final String INVENTORY_EVENTS_TOPIC = "inventory.events";
    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";
    
    public void publishOrderEvent(OrderEvent event) {
        try {
            log.info("Publishing order event: {}", event);
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId(), event);
        } catch (Exception e) {
            log.error("Failed to publish order event: {}", event, e);
        }
    }
    
    public void publishInventoryEvent(InventoryEvent event) {
        try {
            log.info("Publishing inventory event: {}", event);
            kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event.getOrderId(), event);
        } catch (Exception e) {
            log.error("Failed to publish inventory event: {}", event, e);
        }
    }
    
    public void publishPaymentEvent(String orderId, Object paymentData) {
        try {
            log.info("Publishing payment event for order: {}", orderId);
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, orderId, paymentData);
        } catch (Exception e) {
            log.error("Failed to publish payment event for order: {}", orderId, e);
        }
    }
}