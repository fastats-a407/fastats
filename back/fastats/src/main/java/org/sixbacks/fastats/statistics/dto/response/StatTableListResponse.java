package org.sixbacks.fastats.statistics.dto.response;

import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;

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

	public static StatTableListResponse from(StatDataDocument document) {
		// StatSurveyInfoDto 객체 생성
		StatSurveyInfoDto statSurveyInfo = new StatSurveyInfoDto(
			document.getStatOrgName(),
			document.getStatSurveyName(),
			document.getStatTableKosisViewLink()
		);

		return new StatTableListResponse(
			document.getStatTableName(),
			statSurveyInfo,
			document.getCollInfoStartDate(),
			document.getCollInfoEndDate(),
			document.getStatTableKosisViewLink()
		);
	}

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
