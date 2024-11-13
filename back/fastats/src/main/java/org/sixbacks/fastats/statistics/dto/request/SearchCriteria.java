package org.sixbacks.fastats.statistics.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SearchCriteria {

	private String keyword;
	private int page;
	private int size;
	private String ctg;
	private String ctgContent;

}
