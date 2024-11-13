package org.sixbacks.fastats.statistics.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class StatTablePageResponse {

	private List<StatTableListResponse> content;
	private int size;
	private int totalPages;

}
