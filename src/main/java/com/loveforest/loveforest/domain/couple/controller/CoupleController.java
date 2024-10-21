package com.loveforest.loveforest.domain.couple.controller;

import com.loveforest.loveforest.domain.couple.service.CoupleService;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
public class CoupleController {

    private final CoupleService coupleService;

    // 커플 코드로 사용자 연결
    @PostMapping("/join")
    public ResponseEntity<String> joinCouple(@RequestParam String coupleCode, @RequestBody UserSignupRequestDTO request) {
        coupleService.joinCouple(coupleCode, request);
        return ResponseEntity.ok("Successfully joined the couple and room assigned.");
    }
}
