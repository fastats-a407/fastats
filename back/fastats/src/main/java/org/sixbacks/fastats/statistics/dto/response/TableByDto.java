package org.sixbacks.fastats.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableByDto {

	private String name;
	private int count;

	@Override
	public String toString() {
		return "TableByDto{" +
			"name='" + name + '\'' +
			", count=" + count +
			'}';
	}
}
