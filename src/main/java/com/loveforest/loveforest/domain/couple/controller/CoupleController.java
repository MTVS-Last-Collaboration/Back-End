package com.loveforest.loveforest.domain.couple.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.couple.dto.CoupleCodeResponseDTO;
import com.loveforest.loveforest.domain.couple.dto.CoupleJoinRequestDTO;
import com.loveforest.loveforest.domain.couple.dto.CoupleJoinResponseDTO;
import com.loveforest.loveforest.domain.couple.service.CoupleService;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
@Tag(name = "커플 API", description = "커플 관련 API를 제공합니다.")
public class CoupleController {

    private final CoupleService coupleService;

    /**
     * 커플 코드 생성 API
     *
     * @return 커플 코드
     */
    @Operation(summary = "커플 코드 생성", description = "커플 코드는 첫 번째 사용자가 생성하며, 해당 사용자는 나중에 커플로 연동될 다른 사용자와 연결됩니다." +
            "이 요청은 첫 번째 사용자의 ID를 기반으로 커플 코드를 생성하기 위한 요청입니다. " + "이 응답은 커플 코드 생성 API를 호출한 후 반환됩니다. " +
            "첫 번째 사용자가 커플을 생성할 때 부여된 고유 코드를 통해 두 번째 사용자가 연동될 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "커플 코드가 성공적으로 생성되었습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CoupleCodeResponseDTO.class),
                    examples = @ExampleObject(
                            value = "{\"coupleCode\": \"123e4567-e89b-12d3-a456-426614174000\"}"
                    )
            )),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"errorType\": \"UserNotFound\", \"message\": \"해당 사용자를 찾을 수 없습니다.\"}"
                    )
            ))
    })
    @PostMapping("/create")
    public ResponseEntity<CoupleCodeResponseDTO> createCouple(@AuthenticationPrincipal LoginInfo loginInfo) {
        Long userId = loginInfo.getUserId();
        String coupleCode = coupleService.createCouple(userId);
        return ResponseEntity.ok(new CoupleCodeResponseDTO(coupleCode));
    }

    /**
     * 커플 연동 API
     *
     * @param request 커플 코드 연동 요청 DTO
     * @return 커플 연동 완료 메시지
     */
    @Operation(summary = "커플 연동", description ="이 요청은 두 번째 사용자가 커플 코드로 연동하기 위한 요청입니다. " +
            "커플 코드와 사용자의 ID를 함께 제출하여 연동할 수 있습니다. " + "두 번째 사용자가 커플 코드로 연동된 후 성공 메시지를 반환합니다. " +
            "성공적인 커플 연동 후 사용자는 다른 커플과 공유하는 시스템에 연결됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "커플 연동이 성공적으로 완료되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"유효하지 않은 커플 코드입니다.\"}"
                    )
            )),
            @ApiResponse(responseCode = "404", description = "커플 코드를 찾을 수 없습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"errorType\": \"CoupleNotFound\", \"message\": \"해당 커플 코드를 찾을 수 없습니다.\"}"
                    )
            )),
            @ApiResponse(responseCode = "409", description = "이미 두 명의 사용자가 연동된 커플입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 409, \"errorType\": \"CoupleCodeAlreadyUsed\", \"message\": \"이미 두 명의 사용자가 연동된 커플입니다.\"}"
                    )
            ))
    })
    @PostMapping("/join")
    public ResponseEntity<CoupleJoinResponseDTO> joinCouple(@AuthenticationPrincipal LoginInfo loginInfo, @Valid @RequestBody CoupleJoinRequestDTO request) {
        Long userId = loginInfo.getUserId();
        coupleService.joinCouple(userId, request.getCoupleCode());
        return ResponseEntity.ok(new CoupleJoinResponseDTO("커플 연동이 성공적으로 완료되었습니다."));
    }
}
