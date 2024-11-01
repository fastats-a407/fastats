package org.sixbacks.fastats.statistics.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

@Table("stat_org")
@Getter
public class StatOrganization {
	@Id
	private final Long id;

	@Column("code")
	private final Integer code;

	@Column("name")
	private final String name;

	@PersistenceCreator
	StatOrganization(Long id, Integer code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public static StatOrganization from(Integer code, String name) {
		return new StatOrganization(null, code, name);
	}

	@Override
	public String toString() {
		return "StatOrganization{" +
			"id=" + id +
			", code='" + code + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
