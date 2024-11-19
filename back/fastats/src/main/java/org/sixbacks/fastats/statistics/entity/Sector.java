package org.sixbacks.fastats.statistics.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

/*
	NOTE: 통계청 기준 '주제별'에서 구분되는 데이터. 통계 주제.
 */
@Table("sector")
@Getter
public class Sector {

	@Id
	private final Long id;

	@Column("code")
	private final String code;

	@Column("description")
	private final String description;

	@PersistenceCreator
	Sector(Long id, String code, String description) {
		this.id = id;
		this.code = code;
		this.description = description;
	}

	public static Sector from(String code, String description) {
		return new Sector(null, code, description);
	}

	@Override
	public String toString() {
		return "Sector{" +
			"id=" + id +
			", code='" + code + '\'' +
			", description='" + description + '\'' +
			'}';
	}
}

