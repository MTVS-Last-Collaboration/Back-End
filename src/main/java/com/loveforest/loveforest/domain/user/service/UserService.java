package com.loveforest.loveforest.domain.user.service;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.auth.jwt.JwtTokenProvider;
import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidRefreshTokenException;
import com.loveforest.loveforest.domain.auth.jwt.refreshToken.RefreshTokenRepository;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.couple.service.CoupleService;
import com.loveforest.loveforest.domain.user.dto.MyInfoResponseDTO;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import com.loveforest.loveforest.domain.user.dto.UserSignupResponseDTO;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.enums.Authority;
import com.loveforest.loveforest.domain.user.enums.Gender;
import com.loveforest.loveforest.domain.user.exception.EmailAlreadyExistsException;
import com.loveforest.loveforest.domain.user.exception.InvalidPasswordException;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CoupleService coupleService;
    private final CoupleRepository coupleRepository;


    /**
     * 회원가입 처리 메서드
     *
     * @param request 사용자로부터 전달받은 회원가입 요청 정보 (이메일, 유저명, 비밀번호, 닉네임, 기념일)
     * @return 등록된 사용자 정보 (User 엔티티)
     * @throws EmailAlreadyExistsException 중복된 이메일이 존재할 경우 예외 발생
     * @explain 회원가입 시 이메일 중복 확인 후 비밀번호를 암호화하여 새로운 사용자로 등록한다.
     */
    public UserSignupResponseDTO signUp(UserSignupRequestDTO request) {
        String email = request.getEmail();
        log.info("회원가입 요청 - 이메일: {}", email);

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("중복된 이메일로 회원가입 시도 - 이메일: {}", email);
            throw new EmailAlreadyExistsException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 암호화 완료 - 이메일: {}", email);

        // 새로운 커플 코드 생성
        String coupleCode = coupleService.generateCoupleCode();

        // 새로운 커플 객체 생성 후 사용자 연결
        Couple couple = new Couple(coupleCode);
        coupleRepository.save(couple);

        // 새로운 유저 생성, role 기본값 USER 설정

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .anniversaryDate(request.getAnniversary())
                .couple(couple)
                .build();
        log.info("새로운 사용자 생성 완료 - 이메일: {}", email);

        // 사용자 정보 저장
        userRepository.save(user);

        return new UserSignupResponseDTO(user.getNickname());
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
    public Map<String, String> login(String email, String password) {
        log.info("로그인 시도 - 이메일: {}", email);

        // 1. 이메일 확인
        User user = userRepository.findByEmail(email)
                /*.orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도 - 이메일: {}", maskedEmail);
                    return new UserNotFoundException();
                })*/;

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("비밀번호 불일치 - 이메일: {}", email);
            throw new InvalidPasswordException();
        }

        Long id = user.getId();
        String nickname = user.getNickname();
        String authorities = user.getAuthority().name(); // User 엔티티의 Authority에서 권한 추출
        Long coupleId = user.getCouple() != null ? user.getCouple().getId() : null; // 커플이 없는 경우 null 처리

        // 액세스 토큰과 리프레시 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), id, nickname, authorities, coupleId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), id, nickname, coupleId);
        log.info("토큰 발급 완료 - 이메일: {}", email);

        // 리프레시 토큰을 Redis에 저장
        refreshTokenRepository.saveRefreshToken(user.getEmail(), refreshToken);
        log.debug("리프레시 토큰 저장 완료 - 이메일: {}", email);

        // 결과를 Map으로 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    public String getPartnerNickname(User user) {
        if (user.getCouple() == null) {
            return null;
        }

        // 커플에서 파트너 찾기
        return user.getCouple().getUsers().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .findFirst()
                .map(User::getNickname)
                .orElse(null);
    }

    public String getMyNickname(User user) {
        if (user.getCouple() == null) {
            return null;
        }

        // 커플에서 파트너 찾기
        return user.getNickname();
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
            LoginInfo loginInfo = jwtTokenProvider.getLoginInfoFromToken(refreshToken);
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            log.debug("리프레시 토큰에서 이메일 추출 - 이메일: {}", email);

            String savedRefreshToken = refreshTokenRepository.getRefreshToken(email);
            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                log.warn("리프레시 토큰이 유효하지 않음 - 이메일: {}", email);
                throw new InvalidRefreshTokenException();
            }

            // 새로운 액세스 토큰 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(email, loginInfo.getUserId(), loginInfo.getNickname(), loginInfo.getAuthorities().name(), loginInfo.getCoupleId());
            log.info("새로운 액세스 토큰 발급 완료 - 이메일: {}", email);
            return newAccessToken;
        } else {
            log.warn("리프레시 토큰이 유효하지 않거나 만료됨");
            throw new InvalidRefreshTokenException();
        }
    }

    public String getCoupleCode(String email) {
        User user = userRepository.findByEmail(email);
        return user.getCouple().getCoupleCode();
    }

    public MyInfoResponseDTO getMyInfo(LoginInfo loginInfo) {
        // loginInfo에서 사용자 ID를 가져와 DB에서 사용자 정보를 조회합니다.
        User user = userRepository.findById(loginInfo.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // MyInfoResponseDTO에 사용자 정보를 매핑하여 반환합니다.
        return new MyInfoResponseDTO(
                user.getNickname(),
                user.getGender(),
                user.getAnniversaryDate(),
                user.getCouple().getCoupleCode()
        );
    }
}
