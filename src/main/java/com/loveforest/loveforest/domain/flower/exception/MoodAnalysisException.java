package com.loveforest.loveforest.domain.flower.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MoodAnalysisException extends CustomException {
    public MoodAnalysisException() {
        super(ErrorCode.MOOD_ANALYSIS_FAILED); // ErrorCode에 MOOD_ANALYSIS_FAILED 추가 필요
    }
}