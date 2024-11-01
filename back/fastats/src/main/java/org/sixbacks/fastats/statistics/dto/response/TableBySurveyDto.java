package org.sixbacks.fastats.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableBySurveyDto {

	private String surveyTitle;
	private int count;

}
