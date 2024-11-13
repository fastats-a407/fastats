package org.sixbacks.fastats.cookie;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/initialize")
public class CookieController {

	@GetMapping("")
	public ResponseEntity<String> initialize(HttpServletResponse response,
		@CookieValue(value = "sessionID", required = false) String sessionId) {
		// sessionID가 없는 경우 새로 설정
		if (sessionId == null) {
			String newSessionId = generateSessionId(); // 새 세션 ID 생성
			String cookieHeader = String.format("sessionID=%s; Path=/; SameSite=None; Secure", newSessionId);
			response.addHeader("Set-Cookie", cookieHeader); // Set-Cookie 헤더에 직접 설정

			// log.info("새 sessionID 쿠키가 설정되었습니다: " + newSessionId);
			return ResponseEntity.ok("sessionID 쿠키가 설정되었습니다.");
		}
		// log.error(sessionId);
		return ResponseEntity.ok("sessionID 쿠키가 이미 존재합니다.");
	}

	// 세션 ID 생성 메서드
	private String generateSessionId() {
		// UUID를 사용해 고유한 세션 ID를 생성
		return UUID.randomUUID().toString();
	}
}
