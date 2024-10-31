package org.sixbacks.fastats.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatTableListResponse {

	private String title;
	private StatSurveyInfoDto statSurveyInfo;
	private String collStartDate;
	private String collEndDate;
	private String tableLink;
	
}
