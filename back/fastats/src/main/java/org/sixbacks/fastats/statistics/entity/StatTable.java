package org.sixbacks.fastats.statistics.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

/*
	NOTE: 통계청 기준 '통계표'로 구분되는 데이터. 통계표.
 */
@Table("stat_table")
@Getter
public class StatTable {

	@Id
	private final Long id;

	@Column("survey_id")
	private final AggregateReference<StatSurvey, Long> refSurveyId;

	@Column("name")
	private final String name;

	@Column("content")
	private final String content;

	@Column("comment")
	private final String comment;

	@Column("kosis_tb_id")
	private final String kosisTbId;

	@Column("kosis_view_link")
	private final String kosisViewLink;

	@PersistenceCreator
	public StatTable(Long id, AggregateReference<StatSurvey, Long> refSurveyId, String name, String content,
		String comment, String kosisTbId, String kosisViewLink) {
		this.id = id;
		this.refSurveyId = refSurveyId;
		this.name = name;
		this.content = content;
		this.comment = comment;
		this.kosisTbId = kosisTbId;
		this.kosisViewLink = kosisViewLink;
	}

	public static StatTable from(Long surveyId, String name, String content,
		String comment, String kosisTbId, String kosisViewLink) {

		AggregateReference<StatSurvey, Long> refSurveyId = AggregateReference.to(surveyId);

		return new StatTable(null, refSurveyId, name, content, comment, kosisTbId, kosisViewLink);
	}

	@Override
	public String toString() {
		return "StatTable{" +
			"id=" + id +
			", refSurveyId=" + refSurveyId +
			", name='" + name + '\'' +
			", content='" + content + '\'' +
			", comment='" + comment + '\'' +
			", kosisTbId='" + kosisTbId + '\'' +
			", kosisViewLink='" + kosisViewLink + '\'' +
			'}';
	}
}