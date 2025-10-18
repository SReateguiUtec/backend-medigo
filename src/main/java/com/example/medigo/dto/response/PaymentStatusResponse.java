package com.example.medigo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {

    private String status;
    private String paymentStatus;
    private BigDecimal amountTotal;
    private String currency;
    private Map<String, String> metadata;
    private Long citaId;
}