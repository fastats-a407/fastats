package org.sixbacks.fastats.global.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
	private int code;
	private String message;
	private T data;

	public ApiResponse(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(200, message, data);
	}

	public static <T> ApiResponse<T> error(int code, String message) {
		return new ApiResponse<>(code, message, null);
	}
}

