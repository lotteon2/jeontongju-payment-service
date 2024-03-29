package com.jeontongju.payment.kafka;

import com.jeontongju.payment.service.PaymentService;
import com.jeontongju.payment.util.KakaoPayUtil;
import io.github.bitbox.bitbox.dto.KakaoPayCancelDto;
import io.github.bitbox.bitbox.dto.MemberInfoForNotificationDto;
import io.github.bitbox.bitbox.dto.OrderCancelDto;
import io.github.bitbox.bitbox.dto.SubscriptionBatchDto;
import io.github.bitbox.bitbox.enums.NotificationTypeEnum;
import io.github.bitbox.bitbox.enums.RecipientTypeEnum;
import io.github.bitbox.bitbox.util.KafkaTopicNameInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class KafkaListenerProcessor {
    private final KakaoPayUtil kakaoPayUtil;
    private final PaymentService paymentService;
    private final KafkaTemplate<String, OrderCancelDto> orderCancelDtoKafkaTemplate;
    private final KafkaTemplate<String, MemberInfoForNotificationDto> memberInfoForNotificationDtoKafkaTemplate;

    @KafkaListener(topics = KafkaTopicNameInfo.CANCEL_KAKAOPAY)
    public void cancelKakaoPay(KakaoPayCancelDto kakaoPayCancelDto) {
        kakaoPayUtil.callKakaoCancelApi(kakaoPayCancelDto);
    }

    @KafkaListener(topics = KafkaTopicNameInfo.CANCEL_ORDER_PAYMENT)
    public void cancelPayment(OrderCancelDto orderCancelDto){
        try {
            paymentService.cancelPayment(orderCancelDto);
            orderCancelDtoKafkaTemplate.send(KafkaTopicNameInfo.CANCEL_ORDER_STOCK, orderCancelDto);
        }catch(Exception e){
            orderCancelDtoKafkaTemplate.send(sendOrderCancel(orderCancelDto), orderCancelDto);
            memberInfoForNotificationDtoKafkaTemplate.send(KafkaTopicNameInfo.SEND_ERROR_CANCELING_ORDER_NOTIFICATION,
                    MemberInfoForNotificationDto.builder()
                            .recipientId(orderCancelDto.getConsumerId())
                            .recipientType(RecipientTypeEnum.ROLE_CONSUMER)
                            .notificationType(NotificationTypeEnum.INTERNAL_PAYMENT_SERVER_ERROR)
                            .createdAt(LocalDateTime.now())
                    .build());

        }
    }

    @KafkaListener(topics = KafkaTopicNameInfo.PAYMENT_SUBSCRIPTION)
    public void renewSubscription(SubscriptionBatchDto subscriptionBatchDto){
        kakaoPayUtil.renewSubscription(subscriptionBatchDto.getSubscriptionBatchInterface());
    }

    private String sendOrderCancel(OrderCancelDto orderCancelDto) {
        String name;
        if (orderCancelDto.getCouponCode() != null) {
            name = KafkaTopicNameInfo.RECOVER_CANCEL_ORDER_COUPON;
        } else if (orderCancelDto.getPoint() != null) {
            name = KafkaTopicNameInfo.RECOVER_CANCEL_ORDER_POINT;
        } else {
            name = KafkaTopicNameInfo.RECOVER_CANCEL_ORDER;
        }
        return name;
    }
}
