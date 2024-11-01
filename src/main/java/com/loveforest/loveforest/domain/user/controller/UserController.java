package com.loveforest.loveforest.domain.user.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.user.dto.LoginRequestDTO;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import com.loveforest.loveforest.domain.user.dto.UserSignupResponseDTO;
import com.loveforest.loveforest.domain.user.service.UserService;
import com.loveforest.loveforest.exception.ErrorResponse;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "회원 API", description = "회원 API 입니다.")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입이 성공적으로 완료되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"잘못된 입력입니다.\"}"
                    )
            )),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 409, \"errorType\": \"DuplicatedEmail\", \"message\": \"이미 존재하는 이메일입니다.\"}"
                    )
            ))
    })
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDTO> signup(@Valid @RequestBody UserSignupRequestDTO request) {
        log.info("회원가입 요청 시작 - 이메일: {}", request.getEmail());
        try {
            UserSignupResponseDTO reponse = userService.signUp(request);
            log.info("회원가입 성공 - 이메일: {}", request.getEmail());
            return ResponseEntity.ok().body(reponse);
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패 - 유효하지 않은 입력: {}", e.getMessage());
            throw new InvalidInputException();  // 메시지를 사용하지 않고 예외 던짐
        }
    }


    /**
     * 로그인
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 시도합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인이 성공적으로 완료되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"잘못된 입력입니다.\"}"
                    )
            )),
            @ApiResponse(responseCode = "401", description = "이메일이나 비밀번호가 일치하지 않습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 401, \"errorType\": \"Unauthorized\", \"message\": \"잘못된 이메일 또는 비밀번호입니다.\"}"
                    )
            ))
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        try {
            // Map으로 토큰을 받기
            Map<String, String> tokens = userService.login(requestDTO.getEmail(), requestDTO.getPassword());
            log.info("로그인 성공 - 이메일: {}", userService.maskEmail(requestDTO.getEmail()));

            // 헤더에 토큰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + tokens.get("accessToken"));
            headers.set("Refresh-Token", tokens.get("refreshToken"));

            // 응답 시 헤더에 추가된 토큰을 반환
            return ResponseEntity.ok().headers(headers).build();
        } catch (IllegalArgumentException e) {
            log.error("로그인 실패 - 유효하지 않은 회원: {}", userService.maskEmail(requestDTO.getEmail()));
            throw new UnauthorizedException();
        }
    }


    /**
     * 로그아웃
     */
    @Operation(summary = "로그아웃", description = "사용자가 로그아웃하며, Redis에 저장된 리프레시 토큰을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 로그아웃되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"잘못된 요청입니다.\"}"
                    )
            )),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"존재하지 않는 사용자입니다.\"}"
                    )
            ))
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String email) {
        log.info("로그아웃 요청 시작 - 이메일: {}", userService.maskEmail(email));
        userService.logout(email);
        log.info("로그아웃 성공 - 이메일: {}", userService.maskEmail(email));
        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * 리프레시 토큰을 사용해 새로운 액세스 토큰 발급
     */
    @Operation(summary = "새로운 액세스 토큰 발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새로운 액세스 토큰이 성공적으로 발급되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 리프레시 토큰입니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"잘못된 리프레시 토큰입니다.\"}"
                    )
            )),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 만료되었거나 유효하지 않습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = "{\"status\": 401, \"errorType\": \"Unauthorized\", \"message\": \"리프레시 토큰이 유효하지 않거나 만료되었습니다.\"}"
                    )
            ))
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<String> refreshAccessToken(@RequestBody Map<String, String> request) {
        log.info("액세스 토큰 재발급 요청 시작");
        String refreshToken = request.get("refreshToken");
        try {
            String newAccessToken = userService.refreshAccessToken(refreshToken);
            log.info("액세스 토큰 재발급 성공");
            return ResponseEntity.ok(newAccessToken);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
