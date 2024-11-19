package org.sixbacks.fastats.statistics.entity.document;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Document(indexName = "stat_data_index")
@Setting(settingPath = "/elasticsearch/settings/nori-settings.json")
@AllArgsConstructor
@Getter
@ToString
/*
 * TODO : PK 타입 : String(stat_table 기본키 가져오기(위 방식은 최신화시 전체 데이터에서 최신화된 데이터만 추가 시 유리
 *						/ UUID(위 방식은 최신화시 전체 데이터 삭제 후 다시 전체 데이터 삽입 시 유리할 것으로 판단))
 */
public class StatDataDocument {
	@Id
	private String tableId;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "fastats_nori"), // nori 분석기 추가
		otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword) // keyword 타입 서브필드 추가
		}
	)
	private String sectorName;           // 주제명

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "fastats_nori"), // nori 분석기 추가
		otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword) // keyword 타입 서브필드 추가
		}
	)
	private String statSurveyName;       // 통계명

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "fastats_nori"), // nori 분석기 추가
		otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword) // keyword 타입 서브필드 추가
		}
	)
	private String statOrgName;          // 기관명

	@Field(type = FieldType.Text, analyzer = "fastats_nori")
	private String statTableName;        // 통계표명

	@Field(type = FieldType.Text, analyzer = "fastats_nori")
	private String statTableContent;     // 내용

	@Field(type = FieldType.Text, analyzer = "fastats_nori")
	private String statTableComment;     // 주석

	private String statTableKosisViewLink; // kosis 표 보기 링크

	@Field(type = FieldType.Date, format = {}, pattern = "yyyyMMdd")
	private String collInfoStartDate;  // 수록 시작시기

	@Field(type = FieldType.Date, format = {}, pattern = "yyyyMMdd")
	private String collInfoEndDate;    // 수록 종료시기

	public StatDataDocument() {
		this.tableId = UUID.randomUUID().toString();
	}
}
