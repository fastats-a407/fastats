package org.sixbacks.fastats.chatbot.controller;

import static co.elastic.clients.elasticsearch.snapshot.SnapshotSort.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sixbacks.fastats.chatbot.dto.request.ChatMessageDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter startStream(@CookieValue(value = "sessionID") String sessionId) {
	// public SseEmitter startStream(@RequestBody String sessionId) {
	// 	System.out.println(sessionId);
		if (sessionId == null) {
			System.out.println("SessionID 쿠키가 없습니다.");
			return null; // 오류 처리를 위한 코드
		}

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
	@PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<String> handleChat(@RequestBody ChatMessageDTO chatMessageDTO, @CookieValue("sessionID") String sessionId) {

		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found or closed");
		}
		// 연결된 Stream이 없을 경우 세션이 없다고 반응

		// new Thread(() -> {
		// 	try {
		// 		String responseText = chatClient.prompt()
		// 			// .user("날씨에 관련한 통계를 검색할 수 있는 한국어 검색어만 4개를 list형식으로 줘")
		// 			.user(chatMessageDTO.getMessage())
		// 			.call()
		// 			.content();
		// 		System.out.println(responseText);
		// 		// 확인용
		// 		sseEmitter.send(responseText);
		// 	}catch (IOException e) {
		// 		sseEmitter.completeWithError(e);
		// 	}
		// }).start();
		// thread는 생성되고 다 끝나면 자동으로 삭제가 됨
		new Thread(() -> {
			try {
				chatClient.prompt()
					.user(chatMessageDTO.getMessage())
					.stream()
					.content()
					.subscribe(
						content -> {
							try {
								sseEmitter.send(SseEmitter.event().name("message").data(content));
							} catch (IOException e) {
								sseEmitter.completeWithError(e);
							}
						},
						error -> sseEmitter.completeWithError(error),
						() -> {
							try {
								sseEmitter.send(SseEmitter.event().name("complete").data("모든 메시지 전송 완료"));
							} catch (IOException e) {
								sseEmitter.completeWithError(e);
							}
						}
					);
			} catch (Exception e) {
				sseEmitter.completeWithError(e);
			}
		}).start();

		return ResponseEntity.ok("Message processing started");
	}

	@PostMapping("/end")
	// public void endStream(@RequestParam String sessionId) {
	public void endStream(@CookieValue(value = "sessionID") String sessionId) {
		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter != null) {
			sseEmitter.complete(); // 스트림 종료
			activeSessions.remove(sessionId);
		}
	}
}
