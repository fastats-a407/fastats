package org.sixbacks.fastats.global.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	/*
		TODO: 개발 과정에서 지속적인 추가 필요
		NOTE: {NAME}(status, code, message)
	 */

	STAT_ILL_REQUEST(HttpStatus.BAD_REQUEST, 1000, "유효하지 않은 검색 요청입니다"),
	SERVER_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final int code;
	private final String message;

}


