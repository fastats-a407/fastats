package org.sixbacks.fastats.chatbot.controller;

import org.sixbacks.fastats.chatbot.dto.request.ChatMessageDTO;
import org.sixbacks.fastats.chatbot.dto.response.AIResponseDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

@Controller
public class ChatbotAIController {

	private RestTemplate restTemplate;

	private static final String AI_SERVER_URL = "http://localhost:8000/";

	public AIResponseDTO processMessage(ChatMessageDTO chatMessageDTO) {
		// FastAPI 서버에 JSON 형식으로 메시지를 전송
		return restTemplate.postForObject(AI_SERVER_URL, chatMessageDTO, AIResponseDTO.class);
	}
}
