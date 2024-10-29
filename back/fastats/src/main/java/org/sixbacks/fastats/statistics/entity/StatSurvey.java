package org.sixbacks.fastats.statistics.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

/*
	NOTE: 통계청 기준 '통계별'에서 구분되는 데이터. 통계 조사.
 */
@Table("stat_survey")
@Getter
public class StatSurvey {

	@Id
	private final Long id;

	@Column("sector_id")
	private final AggregateReference<Sector, Long> sectorId;

	@Column("org_code")
	private final Integer orgCode;

	@Column("org_name")
	private final String orgName;

	@Column("name")
	private final String name;

	@Column("coll_start_date")
	private final String collStartDate;

	@Column("coll_end_date")
	private final String collEndDate;

	@Column("coll_period")
	private final String collPeriod;

	public static StatSurvey from(AggregateReference<Sector, Long> sectorId, Integer orgCode, String orgName,
		String name, String collStartDate, String collEndDate, String collPeriod) {
		return new StatSurvey(null, sectorId, orgCode, orgName,
			name, collStartDate, collEndDate, collPeriod);
	}

	@PersistenceCreator
	StatSurvey(Long id, AggregateReference<Sector, Long> sectorId, Integer orgCode, String orgName,
		String name, String collStartDate, String collEndDate, String collPeriod) {
		this.id = id;
		this.sectorId = sectorId;
		this.orgCode = orgCode;
		this.orgName = orgName;
		this.name = name;
		this.collStartDate = collStartDate;
		this.collEndDate = collEndDate;
		this.collPeriod = collPeriod;
	}
}
