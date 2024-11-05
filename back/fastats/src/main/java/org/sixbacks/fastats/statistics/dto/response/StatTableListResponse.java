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

	@Override
	public String toString() {
		return "StatTableListResponse{" +
			"title='" + title + '\'' +
			", statSurveyInfo.orgName=" + statSurveyInfo.getOrgName() + '\'' +
			", statSurveyInfo.statTitle=" + statSurveyInfo.getStatTitle() + '\'' +
			", statSurveyInfo.statLink=" + statSurveyInfo.getStatLink() + '\'' +
			", collStartDate='" + collStartDate + '\'' +
			", collEndDate='" + collEndDate + '\'' +
			", tableLink='" + tableLink + '\'' +
			'}';
	}
}
