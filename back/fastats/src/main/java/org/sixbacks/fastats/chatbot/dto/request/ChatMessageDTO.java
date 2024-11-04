package org.sixbacks.fastats.chatbot.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatMessageDTO {
	private String message;

	public ChatMessageDTO() {
	}

	public ChatMessageDTO(String message) {
		this.message = message;
	}

}
