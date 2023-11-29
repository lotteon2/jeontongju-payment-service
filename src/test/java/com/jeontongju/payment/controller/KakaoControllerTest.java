package com.jeontongju.payment.controller;

import com.jeontongju.payment.ControllerTestUtil;
import com.jeontongju.payment.dto.controller.MemberCreditChargeDto;
import com.jeontongju.payment.dto.temp.PaymentCreationDto;
import com.jeontongju.payment.dto.temp.ProductDto;
import com.jeontongju.payment.enums.temp.MemberRoleEnum;
import com.jeontongju.payment.enums.temp.PaymentMethodEnum;
import com.jeontongju.payment.enums.temp.PaymentTypeEnum;
import com.jeontongju.payment.service.PaymentService;
import com.jeontongju.payment.util.KakaoPayUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class KakaoControllerTest extends ControllerTestUtil {
    @MockBean
    protected KakaoPayUtil kakaoPayUtil;
    @MockBean
    protected PaymentService paymentService;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    
    @Test
    void seller는_주문이_불가능하다() throws Exception{
        checkControllerFailConditions("/api/order", 1L, MemberRoleEnum.seller,
                createPaymentCreationDto(PaymentTypeEnum.ORDER, PaymentMethodEnum.KAKAO, 0L, "123",
                        "test","test","test","test","","12345",10L,
                        "test","10",10L),
                "주문은 소비자만 할 수 있습니다.",status().isBadRequest()
        );
    }

    @Test
    void admin은_주문이_불가능하다() throws Exception{
        checkControllerFailConditions("/api/order", 1L, MemberRoleEnum.admin,
                createPaymentCreationDto(PaymentTypeEnum.ORDER, PaymentMethodEnum.KAKAO, 0L, "123",
                        "test","test","test","test","","12345",10L,
                        "test","10",10L),
                "주문은 소비자만 할 수 있습니다.",status().isBadRequest()
        );
    }

    @Test
    void consumer는_주문이_가능하다() throws Exception{
        checkControllerSuccessConditions("/api/order", 1L, MemberRoleEnum.admin,
                createPaymentCreationDto(PaymentTypeEnum.ORDER, PaymentMethodEnum.KAKAO, 0L, "123",
                        "test","test","test","test","","12345",10L,
                        "test","10",10L),
                status().isBadRequest()
        );
    }

    @Test
    void seller는_크레딧충전이_불가능하다() throws Exception {
        checkControllerFailConditions("/api/credit", 1L, MemberRoleEnum.seller,
                createMemberCreditChargeDto(10L, PaymentTypeEnum.CREDIT, "test",PaymentMethodEnum.KAKAO),
                "크레딧 충전은 소비자만 할 수 있습니다.",status().isBadRequest()
        );
    }

    @Test
    void admin은_크레딧충전이_불가능하다() throws Exception {
        checkControllerFailConditions("/api/credit", 1L, MemberRoleEnum.admin,
                createMemberCreditChargeDto(10L, PaymentTypeEnum.CREDIT, "test",PaymentMethodEnum.NAVER),
                "크레딧 충전은 소비자만 할 수 있습니다.",status().isBadRequest()
        );
    }

    @Test
    void consumer는_크레딧충전이_가능하다() throws Exception {
        checkControllerSuccessConditions("/api/credit", 1L, MemberRoleEnum.consumer,
                createMemberCreditChargeDto(10L, PaymentTypeEnum.CREDIT, "test",PaymentMethodEnum.NAVER),
                status().isOk()
        );
    }

    @Test
    void 크레딧_충전액이_0이거나_음수면_크레딧_충전이_불가능하다() throws Exception {
        checkControllerFailConditions("/api/credit",1L, MemberRoleEnum.consumer,
                createMemberCreditChargeDto(0L, PaymentTypeEnum.CREDIT, "test",PaymentMethodEnum.KAKAO),
                "최소 크레딧은 1이상입니다.",status().isBadRequest()
        );
        checkControllerFailConditions("/api/credit",1L, MemberRoleEnum.consumer,
                createMemberCreditChargeDto(-1L, PaymentTypeEnum.CREDIT, "test",PaymentMethodEnum.KAKAO),
                "최소 크레딧은 1이상입니다.",status().isBadRequest()
        );
    }

    @Test
    void 크레딧_충전액이_양수면_크레딧_충전이_가능하다() throws Exception {
        checkControllerSuccessConditions("/api/credit",1L, MemberRoleEnum.consumer,
                createMemberCreditChargeDto(1000L, PaymentTypeEnum.CREDIT, "test",PaymentMethodEnum.KAKAO),
                status().isOk()
        );
    }


    private PaymentCreationDto createPaymentCreationDto(PaymentTypeEnum paymentType, PaymentMethodEnum paymentMethod,
                                                        Long pointUsageAmount, String couponCode, String productImg,
                                                        String recipientName, String recipientPhoneNumber, String basicAddress,
                                                        String addressDetail, String zoneCode, Long totalAmount,
                                                        String titleName, String productId, Long productCount
                                                        )
    {
        List<ProductDto> list = Arrays.asList(ProductDto.builder().productId(productId).productCount(productCount).build());

        return PaymentCreationDto.builder()
                .paymentType(paymentType)
                .paymentMethod(paymentMethod)
                .pointUsageAmount(pointUsageAmount)
                .couponCode(couponCode)
                .productImg(productImg)
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .basicAddress(basicAddress)
                .addressDetail(addressDetail)
                .zoneCode(zoneCode)
                .totalAmount(totalAmount)
                .titleName(titleName)
                .products(list)
                .build();
    }

    private MemberCreditChargeDto createMemberCreditChargeDto(Long chargeCredit, PaymentTypeEnum paymentType,
                                                              String itemName, PaymentMethodEnum paymentMethodEnum){
        return MemberCreditChargeDto.builder()
                .chargeCredit(chargeCredit)
                .paymentType(paymentType)
                .itemName(itemName)
                .paymentMethod(paymentMethodEnum)
                .build();
    }
}