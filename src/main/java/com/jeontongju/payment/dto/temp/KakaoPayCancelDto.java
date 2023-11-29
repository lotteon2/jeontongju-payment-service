package com.jeontongju.payment.dto.temp;

import lombok.Builder;
import lombok.Getter;

/**
     domain : order, consumer, payment
     detail : kakao 결제 취소시 사용되는 DTO
     method : Kafka, Feign
     comment :
 */
@Builder
@Getter
public class KakaoPayCancelDto {
    private String tid;
    private Long cancelAmount;
    private Long cancelTaxFreeAmount;
}