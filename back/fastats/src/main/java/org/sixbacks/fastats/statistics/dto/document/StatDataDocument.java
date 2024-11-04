package org.sixbacks.fastats.statistics.dto.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Document(indexName = "stat_data_index")
@AllArgsConstructor
@Getter
@ToString
public class StatDataDocument {
	@Id
	private Long tableId;
	private String statSurveyName;     // 통계명
	private String statOrgName;        // 기관명
	private String statTableName;      // 통계표명
	private String statTableContent;   // 내용
	private String statTableComment;   // 주석
	private String statTableKosisViewLink; // kosis 표 보기 링크
	private String collInfoStartDate;  // 수록 시작시기
	private String collInfoEndDate;    // 수록 종료시기
}
