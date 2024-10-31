package org.sixbacks.fastats.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableByThemeDto {

	private String code;
	private String themeDesc;
	private int count;

}
