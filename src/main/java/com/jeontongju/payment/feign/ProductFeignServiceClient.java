package com.jeontongju.payment.feign;

import io.github.bitbox.bitbox.dto.FeignFormat;
import io.github.bitbox.bitbox.dto.ProductInfoDto;
import io.github.bitbox.bitbox.dto.ProductSearchDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="product-service")
public interface ProductFeignServiceClient {
    // 해당 Feign에서는 프론트가 넘겨준 총가격과 상품들의 총가격을 비교하여 틀리거나 재고가 없으면 예외를 반환
    @PostMapping("/products")
    FeignFormat<List<ProductInfoDto>> getProductInfo(@RequestBody ProductSearchDto productSearchDto);
}