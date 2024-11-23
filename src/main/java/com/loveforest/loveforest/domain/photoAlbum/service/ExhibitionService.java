package com.loveforest.loveforest.domain.photoAlbum.service;

import com.loveforest.loveforest.domain.photoAlbum.dto.ExhibitionRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.ExhibitionResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoDetailsDTO;
import com.loveforest.loveforest.domain.photoAlbum.entity.Exhibition;
import com.loveforest.loveforest.domain.photoAlbum.entity.PhotoAlbum;
import com.loveforest.loveforest.domain.photoAlbum.exception.ExhibitionAlreadyExistsException;
import com.loveforest.loveforest.domain.photoAlbum.exception.ExhibitionNotFoundException;
import com.loveforest.loveforest.domain.photoAlbum.exception.ModelNotReadyException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoNotFoundException;
import com.loveforest.loveforest.domain.photoAlbum.repository.ExhibitionRepository;
import com.loveforest.loveforest.domain.photoAlbum.repository.PhotoAlbumRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final PhotoAlbumRepository photoAlbumRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExhibitionResponseDTO createExhibition(ExhibitionRequestDTO request, Long userId) {
        // 1. 사진 존재 확인 및 권한 검증
        PhotoAlbum photo = photoAlbumRepository.findById(request.getPhotoId())
                .orElseThrow(PhotoNotFoundException::new);
        validateUserAccess(photo, userId);

        // 2. 3D 모델 존재 여부 확인
        if (photo.getObjectUrl() == null || photo.getPngUrl() == null || photo.getMaterialUrl() == null) {
            throw new ModelNotReadyException("3D 모델이 아직 생성되지 않았습니다.");
        }

        // 3. 이미 전시된 사진인지 확인
        validateExistingExhibition(photo.getId());

        // 4. 새로운 전시 생성
        Exhibition exhibition = Exhibition.builder()
                .photo(photo)
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .exhibitedAt(LocalDateTime.now())
                .build();

        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return convertToDTO(savedExhibition);
    }

    @Transactional(readOnly = true)
    public ExhibitionResponseDTO getExhibition(Long exhibitionId, Long userId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(ExhibitionNotFoundException::new);

        validateUserAccess(exhibition, userId);

        return convertToDTO(exhibition);
    }


    @Transactional
    public void deleteExhibition(Long exhibitionId, Long userId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(ExhibitionNotFoundException::new);

        validateUserAccess(exhibition, userId);
        exhibitionRepository.delete(exhibition);

        // 관련된 PhotoAlbum의 전시 상태 초기화는 필요하지 않음
        // 3D 모델 파일들은 유지됨
    }

    @Transactional(readOnly = true)
    public List<ExhibitionResponseDTO> getExhibitionList(Long userId) {
        // 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 사용자의 커플 ID로 전시 목록 조회
        return exhibitionRepository.findAllByCoupleId(user.getCouple().getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateExistingExhibition(Long photoId) {
        if (exhibitionRepository.existsByPhotoId(photoId)) {
            throw new ExhibitionAlreadyExistsException();
        }
    }

    public boolean isExhibitionExists(Long photoId) {
        return exhibitionRepository.existsByPhotoId(photoId);
    }

    private void validateUserAccess(Exhibition exhibition, Long userId) {
        if (!exhibition.getPhoto().getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }
    }

    private void validateUserAccess(PhotoAlbum photo, Long userId) {
        if (!photo.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }
    }

    private ExhibitionResponseDTO convertToDTO(Exhibition exhibition) {
        return ExhibitionResponseDTO.builder()
                .exhibitionId(exhibition.getId())
                .objectUrl(exhibition.getPhoto().getObjectUrl())
                .textureUrl(exhibition.getPhoto().getPngUrl())
                .materialUrl(exhibition.getPhoto().getMaterialUrl())
                .positionX(exhibition.getPositionX())
                .positionY(exhibition.getPositionY())
                .exhibitedAt(exhibition.getExhibitedAt())
                .photo(convertToPhotoDTO(exhibition.getPhoto()))
                .build();
    }

    private PhotoDetailsDTO convertToPhotoDTO(PhotoAlbum photo) {
        return PhotoDetailsDTO.builder()
                .photoId(photo.getId())
                .title(photo.getTitle())
                .imageUrl(photo.getImageUrl())
                .photoDate(photo.getPhotoDate())
                .description(photo.getContent())
                .build();
    }
}