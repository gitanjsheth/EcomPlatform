package com.gitanjsheth.paymentservice.gateways;

import com.gitanjsheth.paymentservice.dtos.PaymentRequestDto;
import com.gitanjsheth.paymentservice.models.Payment;
import com.gitanjsheth.paymentservice.models.PaymentMethod;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BankTransferGateway implements PaymentGatewayInterface {
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequestDto request, Payment payment) {
        // Simulate bank transfer pending state
        PaymentGatewayResponse response = PaymentGatewayResponse.success("BTX-" + System.currentTimeMillis(), "PENDING");
        response.rawResponse = Map.of("type", "bank_transfer");
        response.requiresAction = true;
        response.actionUrl = "https://bank.example.com/transfer?ref=" + response.transactionId;
        return response;
    }

    @Override
    public PaymentMethodResponse createPaymentMethod(PaymentRequestDto request, Long userId) {
        return PaymentMethodResponse.success("BANK-" + System.currentTimeMillis(), null, "BANK", null, null);
    }

    @Override
    public PaymentGatewayResponse processPaymentWithSavedMethod(PaymentMethod paymentMethod, BigDecimal amount, String currency, String orderId) {
        return PaymentGatewayResponse.success("BTX-" + System.currentTimeMillis(), "PENDING");
    }

    @Override
    public RefundResponse refundPayment(String gatewayTransactionId, BigDecimal amount, String reason) {
        return RefundResponse.success("RFD-" + System.currentTimeMillis(), amount, "PROCESSING");
    }

    @Override
    public PaymentGatewayResponse capturePayment(String gatewayTransactionId, BigDecimal amount) {
        return PaymentGatewayResponse.success(gatewayTransactionId, "CAPTURED");
    }

    @Override
    public PaymentGatewayResponse cancelPayment(String gatewayTransactionId) {
        return PaymentGatewayResponse.success(gatewayTransactionId, "CANCELLED");
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        return true;
    }

    @Override
    public WebhookEvent parseWebhookEvent(String payload) {
        return new WebhookEvent("evt_" + System.currentTimeMillis(), "bank.transfer.updated");
    }
}


