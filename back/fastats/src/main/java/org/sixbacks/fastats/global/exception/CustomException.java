package org.sixbacks.fastats.global.exception;

import org.sixbacks.fastats.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
	private final ErrorCode errorCode;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public int getCode() {
		return errorCode.getCode();
	}

	@Override
	public String getMessage() {
		return errorCode.getMessage();
	}

	public HttpStatus getHttpStatus() {
		return errorCode.getStatus();
	}
}

