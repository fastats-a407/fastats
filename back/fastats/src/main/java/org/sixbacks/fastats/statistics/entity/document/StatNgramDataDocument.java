package org.sixbacks.fastats.statistics.entity.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.UUID;

@Document(indexName = "ngram_index")
@Setting(settingPath = "/elasticsearch/settings/ngram-settings.json") // ngram 설정 파일 경로
@AllArgsConstructor
@Getter
@ToString
public class StatNgramDataDocument {
    @Id
    private String tableId;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer"), // ngram 애널라이저 추가
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword) // keyword 타입 서브필드 추가
            }
    )
    private String sectorName;           // 주제명

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer"), // ngram 애널라이저 추가
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword) // keyword 타입 서브필드 추가
            }
    )
    private String statSurveyName;       // 통계명

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer"), // ngram 애널라이저 추가
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword) // keyword 타입 서브필드 추가
            }
    )
    private String statOrgName;          // 기관명

    @Field(type = FieldType.Text, analyzer = "ngram_analyzer") // ngram 애널라이저 추가
    private String statTableName;        // 통계표명

    @Field(type = FieldType.Text, analyzer = "ngram_analyzer") // ngram 애널라이저 추가
    private String statTableContent;     // 내용

    @Field(type = FieldType.Text, analyzer = "ngram_analyzer") // ngram 애널라이저 추가
    private String statTableComment;     // 주석

    private String statTableKosisViewLink; // kosis 표 보기 링크

    @Field(type = FieldType.Date, format = {}, pattern = "yyyyMMdd")
    private String collInfoStartDate;  // 수록 시작시기

    @Field(type = FieldType.Date, format = {}, pattern = "yyyyMMdd")
    private String collInfoEndDate;    // 수록 종료시기

    public StatNgramDataDocument() {
        this.tableId = UUID.randomUUID().toString();
    }
}
