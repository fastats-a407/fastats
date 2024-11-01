package org.sixbacks.fastats.statistics.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryListResponse {

	List<TableByThemeDto> byTheme;
	List<TableBySurveyDto> bySurvey;

}
