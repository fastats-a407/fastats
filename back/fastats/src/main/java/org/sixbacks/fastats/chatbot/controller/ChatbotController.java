package org.sixbacks.fastats.chatbot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sixbacks.fastats.chatbot.dto.request.ChatMessageDTO;
import org.sixbacks.fastats.chatbot.dto.response.AIResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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

	// autowired 나중에 확인하기
	// @Autowired
	private ChatbotAIController chatbotAIController;

	private final Map<String, SseEmitter> activeSessions = new ConcurrentHashMap<>();

	@PostMapping("/stream")
	public SseEmitter startStream(@RequestParam String sessionId) {
		// 중복 sessionID 확인
		if (activeSessions.containsKey(sessionId)) {
			throw new RuntimeException("Session already exists");
		}

		SseEmitter sseEmitter = new SseEmitter();
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

	// 일단 문장을 받고 map에 넣어놓고, application/json 형식으로 보낸다
	// 요청은 post 요청으로 보낸다
	@PostMapping
	public ResponseEntity<String> handleChat(@RequestBody ChatMessageDTO chatMessageDTO, @CookieValue("sessionID") String sessionId) {

		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found or closed");
		}

		// String message = chatMessageDTO.getMessage();

		new Thread(() -> {
			try {
				AIResponseDTO aiResponseDTO = chatbotAIController.processMessage(chatMessageDTO);
				String responseText = aiResponseDTO.getResponse();
				sseEmitter.send(responseText);
			}catch (IOException e) {
				sseEmitter.completeWithError(e);
			}
		}).start();

		return ResponseEntity.ok("Message processing started");
	}

	@PostMapping("/end")
	public void endStream(@RequestParam String sessionId) {
		SseEmitter sseEmitter = activeSessions.get(sessionId);
		if (sseEmitter != null) {
			sseEmitter.complete(); // 스트림 종료
			activeSessions.remove(sessionId);
		}
	}

}
