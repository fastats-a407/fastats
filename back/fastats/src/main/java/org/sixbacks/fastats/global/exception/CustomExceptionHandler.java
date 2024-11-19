package org.sixbacks.fastats.global.exception;

import org.sixbacks.fastats.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CustomExceptionHandler {

	/*
		FIXME: 개발 과정에서는 이용하지 않으나, 추후 배포 시 에러 정보 내보내지 않기 위해 이용 필요.
	 */
	// @ExceptionHandler(Exception.class)
	// public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex, WebRequest request) {
	// 	ApiResponse<String> response = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
	// 		"Internal Server Error");
	// 	return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	// }

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex, WebRequest request) {
		ApiResponse<String> response = ApiResponse.error(ex.getCode(), ex.getMessage());
		return new ResponseEntity<>(response, ex.getHttpStatus());
	}

}


