package org.sixbacks.fastats.statistics.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/*
	NOTE: 통계청 기준 '통계표'로 구분되는 데이터. 통계표.
 */
@Table("stat_table")
public class StatTable {

	@Id
	private final Long id;

	@Column("survey_id")
	private final AggregateReference<StatSurvey, Integer> surveyId;

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

	public static StatTable from(AggregateReference<StatSurvey, Integer> surveyId, String name, String content,
		String comment, String kosisTbId, String kosisViewLink) {

		return new StatTable(null, surveyId, name, content, comment, kosisTbId, kosisViewLink);
	}

	@PersistenceCreator
	public StatTable(Long id, AggregateReference<StatSurvey, Integer> surveyId, String name, String content,
		String comment, String kosisTbId, String kosisViewLink) {
		this.id = id;
		this.surveyId = surveyId;
		this.name = name;
		this.content = content;
		this.comment = comment;
		this.kosisTbId = kosisTbId;
		this.kosisViewLink = kosisViewLink;
	}
}