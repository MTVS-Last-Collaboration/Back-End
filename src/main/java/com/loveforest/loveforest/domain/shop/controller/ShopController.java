package com.loveforest.loveforest.domain.shop.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.shop.dto.PurchaseRequestDTO;
import com.loveforest.loveforest.domain.shop.dto.PurchaseResponseDTO;
import com.loveforest.loveforest.domain.shop.dto.PurchasedItemResponseDTO;
import com.loveforest.loveforest.domain.shop.dto.ShopItemDTO;
import com.loveforest.loveforest.domain.shop.entity.ItemType;
import com.loveforest.loveforest.domain.shop.service.ShopService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Tag(name = "상점 API", description = "가구/벽지/바닥 구매 관련 API")
public class ShopController {

    private final ShopService shopService;

    /**
     * 상품 목록 조회
     *
     */
    @GetMapping("/items/{itemType}")
    @Operation(
            summary = "상품 목록 조회",
            description = """
        상품 종류별 구매 가능한 아이템 목록을 조회합니다.
        - FURNITURE: 가구 목록
        - WALLPAPER: 벽지 목록
        - FLOOR: 바닥 목록
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = ShopItemDTO.class)),
                                    examples = @ExampleObject(value = """
                    [
                      {
                        "id": 1,
                        "itemType": "FURNITURE",
                        "name": "클래식 소파",
                        "price": 1000,
                        "imageUrl": "/furniture/classic-sofa.png",
                        "xSize": 2,
                        "zSize": 3
                      }
                    ]
                    """)
                            )
                    )
            }
    )
    public ResponseEntity<List<ShopItemDTO>> getShopItems(@AuthenticationPrincipal LoginInfo loginInfo, @PathVariable("itemType") ItemType itemType) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        return ResponseEntity.ok(shopService.getItemsByType(itemType));
    }


    /**
     * 아이템 구매
     *
     */
    @PostMapping("/purchase")
    @Operation(
            summary = "아이템 구매",
            description = "포인트를 사용하여 가구/벽지/바닥을 구매합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "구매 성공",
                            content = @Content(schema = @Schema(implementation = PurchaseResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "포인트 부족",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 아이템",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<PurchaseResponseDTO> purchaseItem(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody PurchaseRequestDTO request) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        PurchaseResponseDTO response = shopService.purchaseItem(
                request.getItemId(),
                loginInfo.getUserId(),
                loginInfo.getCoupleId()
        );
        return ResponseEntity.ok(response);
    }


    /**
     * 구매한 아이템 목록 조회
     *
     */
    @GetMapping("/purchased-items")
    @Operation(
            summary = "구매한 아이템 목록 조회",
            description = "사용자가 구매한 모든 아이템의 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PurchasedItemResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<PurchasedItemResponseDTO>> getPurchasedItems(
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("구매한 아이템 목록 조회 요청 - 사용자 ID: {}", loginInfo.getUserId());
        List<PurchasedItemResponseDTO> items = shopService.getPurchasedItems(loginInfo.getUserId());
        log.info("구매한 아이템 목록 조회 완료 - 조회된 아이템 수: {}", items.size());

        return ResponseEntity.ok(items);
    }

}
