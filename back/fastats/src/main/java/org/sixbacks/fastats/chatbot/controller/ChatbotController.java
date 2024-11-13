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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

// 챗봇의 메시지와 SSE를 조정하는 컨트롤러
@Slf4j
@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

	private final Map<String, SseEmitter> activeSessions = new ConcurrentHashMap<>();
	private final ChatClient chatClient;


	public ChatbotController(ChatClient.Builder chatClient) {
		this.chatClient = chatClient.build();
	}

	// @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter startStream(@CookieValue(value = "sessionID" , required = false) String sessionId) {
	// public SseEmitter startStream(@RequestBody String sessionId) {
		if (sessionId == null) {
			log.error("sessionID is null");
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
			sseEmitter.send(SseEmitter.event().name("init").data("연결이 설정되었습니다."));
			// log.error(sessionId + " 연결");
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
		new Thread(() -> {
			try {
				chatClient.prompt()
					.user(chatMessageDTO.getMessage()+ "와 관련된 검색어를 5개만 줘. 형식은 반드시'1.관련 검색어1 2.관련 검색어2 3.관련 검색어3 4.관련 검색어4 5.관련 검\n"
						+ "... 색어5'로 답해줘. 각 키워드는 고유하고 관련성이 있도록 해줘. 한국어로 된 검색어 결과만 주면 돼")
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

	// @PostMapping("/end")
	@GetMapping("/end")
	// public void endStream(@RequestParam String sessionId) {
	public void endStream(@CookieValue(value = "sessionID") String sessionId) {
		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter != null) {
			// log.error("끝");
			sseEmitter.complete(); // 스트림 종료
			activeSessions.remove(sessionId);
		}
	}
}
