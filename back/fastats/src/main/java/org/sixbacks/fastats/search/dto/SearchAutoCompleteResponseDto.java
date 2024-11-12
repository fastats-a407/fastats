package org.sixbacks.fastats.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SearchAutoCompleteResponseDto {
	private String keyword;
}

