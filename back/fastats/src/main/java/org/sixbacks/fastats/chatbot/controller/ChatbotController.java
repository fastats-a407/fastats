package org.sixbacks.fastats.chatbot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sixbacks.fastats.chatbot.dto.request.ChatMessageDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 챗봇의 메시지와 SSE를 조정하는 컨트롤러
@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

	private final Map<String, SseEmitter> activeSessions = new ConcurrentHashMap<>();
	private final ChatClient chatClient;


	public ChatbotController(ChatClient.Builder chatClient) {
		this.chatClient = chatClient.build();
	}

	@PostMapping("/stream")
	public SseEmitter startStream(@CookieValue("sessionID") String sessionId) {
		// 중복 sessionID 확인
		if (activeSessions.containsKey(sessionId)) {
			throw new RuntimeException("Session already exists");
		}

		SseEmitter sseEmitter = new SseEmitter(300000L);
		activeSessions.put(sessionId, sseEmitter);

		// 연결되었음을 알리는 메시지 전송
		try {
			sseEmitter.send("연결이 되었습니다.");
		} catch (IOException e) {
			sseEmitter.completeWithError(e);
		}

		sseEmitter.onCompletion(() -> activeSessions.remove(sessionId));
		sseEmitter.onTimeout(() -> activeSessions.remove(sessionId));

		return sseEmitter;
	}

	// 요청은 post 요청으로 보낸다
	@PostMapping("/message")
	public ResponseEntity<String> handleChat(@RequestBody ChatMessageDTO chatMessageDTO, @CookieValue("sessionID") String sessionId) {

		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found or closed");
		}
		// 연결된 Stream이 없을 경우 세션이 없다고 반응

		new Thread(() -> {
			try {
				String responseText = chatClient.prompt()
					// .user("날씨에 관련한 통계를 검색할 수 있는 한국어 검색어만 4개를 list형식으로 줘")
					.user(chatMessageDTO.getMessage())
					.call()
					.content();
				System.out.println(responseText);
				// 확인용
				sseEmitter.send(responseText);
			}catch (IOException e) {
				sseEmitter.completeWithError(e);
			}
		}).start();
		// thread는 생성되고 다 끝나면 자동으로 삭제가 됨

		return ResponseEntity.ok("Message processing started");
	}

	@GetMapping("")
	public String mess(){
		return chatClient.prompt()
			.user("날씨에 관련한 통계를 검색할 수 있는 한국어 검색어만 4개를 list형식으로 줘")
			.call()
			.content();
	}
	// 내일도 확인용


	@PostMapping("/end")
	public void endStream(@RequestParam String sessionId) {
		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter != null) {
			sseEmitter.complete(); // 스트림 종료
			activeSessions.remove(sessionId);
		}
	}
}
