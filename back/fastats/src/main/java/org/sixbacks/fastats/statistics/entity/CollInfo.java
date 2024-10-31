package org.sixbacks.fastats.statistics.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

@Table("coll_info")
@Getter
public class CollInfo {

	@Id
	private final Long id;

	@Column("stat_table_id")
	private final AggregateReference<StatTable, Long> refStatTableId;

	@Column("start_date")
	private final String startDate;

	@Column("end_date")
	private final String endDate;

	@Column("period")
	private final String period;

	public static CollInfo from(Long id, Long statTableId, String startDate, String endDate,
		String period) {

		AggregateReference<StatTable, Long> refStatTableId = AggregateReference.to(statTableId);

		return new CollInfo(id, refStatTableId, startDate, endDate, period);
	}

	@PersistenceCreator
	public CollInfo(Long id, AggregateReference<StatTable, Long> refStatTableId, String startDate, String endDate,
		String period) {
		this.id = id;
		this.refStatTableId = refStatTableId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.period = period;
	}
}
