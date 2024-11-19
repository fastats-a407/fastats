package org.sixbacks.fastats.statistics.dto.response;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SearchByKeywordDto {
	private Page<StatTableListResponse> pages;
	private long totalCounts;
}
