package com.loveforest.loveforest.domain.user.service;

import com.loveforest.loveforest.domain.auth.jwt.JwtTokenProvider;
import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidRefreshTokenException;
import com.loveforest.loveforest.domain.auth.jwt.refreshToken.RefreshTokenRepository;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.user.dto.LoginResponseDTO;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import com.loveforest.loveforest.domain.user.dto.UserSignupResponseDTO;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.EmailAlreadyExistsException;
import com.loveforest.loveforest.domain.user.exception.InvalidPasswordException;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CoupleRepository coupleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository, CoupleRepository coupleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.coupleRepository = coupleRepository;
    }

    public String maskEmail(String email) {
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(3, parts[0].length())) + "***@" + parts[1];
    }

    /**
     * 회원가입 처리 메서드
     *
     * @param request 사용자로부터 전달받은 회원가입 요청 정보 (이메일, 유저명, 비밀번호, 닉네임)
     * @return 등록된 사용자 정보 (User 엔티티)
     * @throws EmailAlreadyExistsException 중복된 이메일이 존재할 경우 예외 발생
     * @explain 회원가입 시 이메일 중복 확인 후 비밀번호를 암호화하여 새로운 사용자로 등록한다.
     */
    public UserSignupResponseDTO signUpWithCoupleCode(UserSignupRequestDTO request) {
        String maskedEmail = maskEmail(request.getEmail());
        log.info("회원가입 요청 - 이메일: {}", maskedEmail);

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("중복된 이메일로 회원가입 시도 - 이메일: {}", maskedEmail);
            throw new EmailAlreadyExistsException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 암호화 완료 - 이메일: {}", maskedEmail);

        // 새로운 유저 생성, role 기본값 USER 설정
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .build();
        log.info("새로운 사용자 생성 완료 - 이메일: {}", maskedEmail);

        // 커플 코드 생성
        String coupleCode = UUID.randomUUID().toString();

        // 커플 엔티티 생성 후 유저와 연결
        Couple couple = new Couple(coupleCode);
        couple.addUser(user);

        // 커플 및 유저 저장
        coupleRepository.save(couple);

        // 응답 시 커플 코드를 함께 반환
        return new UserSignupResponseDTO(user.getNickname(), coupleCode);
    }


    /**
     * 로그인 처리 메서드
     *
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @return 액세스 토큰과 리프레시 토큰이 담긴 Map
     * @throws UserNotFoundException 사용자가 존재하지 않을 경우 예외 발생
     * @throws InvalidPasswordException 비밀번호가 일치하지 않을 경우 예외 발생
     * @explain 사용자 로그인 시 이메일과 비밀번호를 확인한 후, 유효할 경우 액세스 토큰과 리프레시 토큰을 발급하여 반환합니다.
     */
    public LoginResponseDTO login(String email, String password) {
        String maskedEmail = maskEmail(email);
        log.info("로그인 시도 - 이메일: {}", maskedEmail);

        // 1. 이메일 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도 - 이메일: {}", maskedEmail);
                    return new UserNotFoundException();
                });

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("비밀번호 불일치 - 이메일: {}", maskedEmail);
            throw new InvalidPasswordException();
        }

        Long id = user.getId();
        // 액세스 토큰과 리프레시 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        log.info("토큰 발급 완료 - 이메일: {}", maskedEmail);

        // 리프레시 토큰을 Redis에 저장
        refreshTokenRepository.saveRefreshToken(user.getEmail(), refreshToken);
        log.debug("리프레시 토큰 저장 완료 - 이메일: {}", maskedEmail);

        // 응답 DTO 반환
        return new LoginResponseDTO(accessToken, refreshToken, id);
    }

    /**
     * 로그아웃 처리 메서드
     *
     * @param email 로그아웃할 사용자의 이메일
     * @expain Redis에서 해당 사용자의 리프레시 토큰을 삭제하여 로그아웃 처리
     */
    public void logout(String email) {
        log.info("로그아웃 요청 - 이메일: {}", email);
        refreshTokenRepository.deleteRefreshToken(email);
        log.debug("리프레시 토큰 삭제 완료 - 이메일: {}", email);
    }

    /**
     * 리프레시 토큰을 통해 새로운 액세스 토큰 발급
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급된 액세스 토큰
     * @throws InvalidRefreshTokenException 리프레시 토큰이 유효하지 않거나 만료된 경우 예외 발생
     * @explain 리프레시 토큰의 유효성을 확인한 후, 유효할 경우 새로운 액세스 토큰을 발급합니다. 리프레시 토큰이 유효하지 않으면 예외를 발생시킵니다.
     */
    public String refreshAccessToken(String refreshToken) {
        log.info("리프레시 토큰을 통한 액세스 토큰 재발급 요청");
        if (jwtTokenProvider.validateToken(refreshToken) && !jwtTokenProvider.isTokenExpired(refreshToken)) {
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            log.debug("리프레시 토큰에서 이메일 추출 - 이메일: {}", email);

            String savedRefreshToken = refreshTokenRepository.getRefreshToken(email);
            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                log.warn("리프레시 토큰이 유효하지 않음 - 이메일: {}", email);
                throw new InvalidRefreshTokenException();
            }

            // 새로운 액세스 토큰 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(email);
            log.info("새로운 액세스 토큰 발급 완료 - 이메일: {}", email);
            return newAccessToken;
        } else {
            log.warn("리프레시 토큰이 유효하지 않거나 만료됨");
            throw new InvalidRefreshTokenException();
        }
    }
}
