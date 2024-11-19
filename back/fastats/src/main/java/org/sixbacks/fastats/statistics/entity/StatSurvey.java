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
	private final AggregateReference<Sector, Long> refSectorId;

	@Column("org_id")
	private final AggregateReference<StatOrganization, Long> refOrgId;

	@Column("name")
	private final String name;

	@PersistenceCreator
	StatSurvey(Long id, AggregateReference<Sector, Long> refSectorId,
		AggregateReference<StatOrganization, Long> refOrgId,
		String name) {
		this.id = id;
		this.refSectorId = refSectorId;
		this.refOrgId = refOrgId;
		this.name = name;
	}

	public static StatSurvey from(Long sectorId, Long orgId,
		String name) {

		AggregateReference<Sector, Long> refSectorId = AggregateReference.to(sectorId);
		AggregateReference<StatOrganization, Long> refOrgId = AggregateReference.to(orgId);
		return new StatSurvey(null, refSectorId, refOrgId, name);
	}

	@Override
	public String toString() {
		return "StatSurvey{" +
			"id=" + id +
			", refSectorId=" + refSectorId +
			", refOrgId=" + refOrgId +
			", name='" + name + '\'' +
			'}';
	}
}
