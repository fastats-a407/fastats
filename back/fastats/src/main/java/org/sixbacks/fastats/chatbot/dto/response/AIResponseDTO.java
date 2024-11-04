package org.sixbacks.fastats.chatbot.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AIResponseDTO {
	private String response;

	public AIResponseDTO() {
	}

	public AIResponseDTO(String response) {
		this.response = response;
	}

}
