# Back-End
# LoveForest - 커플 메타버스 플랫폼

## 📌 프로젝트 소개
LoveForest는 커플들을 위한 메타버스 플랫폼으로, 커플만의 가상 공간에서 다양한 상호작용과 추억을 쌓을 수 있는 서비스입니다.

### 주요 기능
- **커플 연동 시스템**: 고유한 코드를 통한 커플 매칭
- **가상 공간 꾸미기**: 커스터마이징 가능한 개인 공간
- **일일 미션**: AI 기반 커플 간 대화 주제 제공
- **감정 분석 시스템**: 음성 메시지 기반 감정 분석
- **포인트 시스템**: 활동 기반 보상 체계
- **사진첩**: 3D 변환 기능이 있는 추억 저장소

## 🛠 기술 스택

<img width="657" alt="image" src="https://github.com/user-attachments/assets/08ad19aa-022f-4956-b187-51420f074cf0">


## 📐 설계 및 아키텍처
![image](https://github.com/user-attachments/assets/77563ad3-b313-429a-8cab-862387bf82a7)

## 서비스 사용자 플로우차트
<img width="569" alt="image" src="https://github.com/user-attachments/assets/98e7a2cc-ada5-4ee8-8abc-77cfc38cb3c8">



### 시스템 아키텍처
- **Layered Architecture** 적용
  - Controller Layer: 사용자 요청 처리
  - Service Layer: 비즈니스 로직 처리
  - Repository Layer: 데이터 접근 처리
- **Domain-Driven Design (DDD)** 적용
  - 도메인 중심 설계로 비즈니스 로직의 명확한 분리
  - 각 도메인별 독립적인 패키지 구조

### 데이터베이스 설계
- JPA/Hibernate를 활용한 객체-관계 매핑
- 엔티티 간 관계 설정을 통한 효율적인 데이터 구조화
- 낙관적 락(Optimistic Lock)을 통한 동시성 제어

## 🔍 주요 구현 사항

### 1. Security & Authentication
- JWT 기반 인증/인가 시스템 구현
- Access Token과 Refresh Token을 활용한 보안 강화
- Redis를 활용한 토큰 관리 시스템

### 2. 파일 업로드 시스템
- AWS S3 연동을 통한 확장 가능한 파일 저장소 구현
- 이미지 파일의 3D 모델 변환 기능 구현
- UUID 기반의 고유 파일명 생성 시스템

### 3. 실시간 상호작용
- WebClient를 활용한 비동기 통신 구현
- AI 서버와의 실시간 데이터 교환
- 효율적인 에러 핸들링 및 예외 처리

### 4. 포인트 시스템
- 트랜잭션 관리를 통한 안전한 포인트 처리
- 다양한 활동에 대한 포인트 보상 체계
- 동시성 제어를 통한 데이터 정합성 보장

### 5. 모니터링 시스템
- Actuator를 통한 애플리케이션 상태 모니터링
- Prometheus를 활용한 메트릭 수집
- 효율적인 로깅 시스템 구현
