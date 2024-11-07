package com.loveforest.loveforest.domain.couple.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.couple.dto.CoupleCodeResponseDTO;
import com.loveforest.loveforest.domain.couple.dto.CoupleJoinRequestDTO;
import com.loveforest.loveforest.domain.couple.dto.CoupleJoinResponseDTO;
import com.loveforest.loveforest.domain.couple.service.CoupleService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
@Tag(name = "커플 API", description = "커플 관련 API를 제공합니다.")
public class CoupleController {

    private final CoupleService coupleService;

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

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        coupleService.joinCouple(loginInfo.getUserId(), request.getCoupleCode());
        return ResponseEntity.ok(new CoupleJoinResponseDTO("커플 연동이 성공적으로 완료되었습니다."));
    }


    @Operation(summary = "나의 커플 코드 조회", description = "사용자의 커플 코드를 조회하는 API입니다. 로그인한 사용자가 속한 커플의 코드를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "커플 코드 조회 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CoupleCodeResponseDTO.class),
                    examples = @ExampleObject(
                            value = "{\"coupleCode\": \"ABCDE\"}"
                    )
            )),
            @ApiResponse(responseCode = "404", description = "커플을 찾을 수 없습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"errorType\": \"CoupleNotFound\", \"message\": \"해당 커플을 찾을 수 없습니다.\"}"
                    )
            ))
    })
    @GetMapping("/my-code")
    public ResponseEntity<CoupleCodeResponseDTO> getMyCoupleCode(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        CoupleCodeResponseDTO coupleCode = coupleService.getMyCoupleCode(loginInfo.getUserId());
        return ResponseEntity.ok(coupleCode);
    }
}
