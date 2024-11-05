package org.sixbacks.fastats.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.sixbacks.fastats.statistics.dto.preprocessing.TableCommentDto;
import org.sixbacks.fastats.statistics.entity.StatTable;
import org.sixbacks.fastats.statistics.repository.StatTableRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TableCommentService {
	private final StatTableRepository statTableRepository;
	private final TableCommentParser tableCommentParser;

	public TableCommentService(@Qualifier("statTableJdbcRepository") StatTableRepository statTableRepository,
		TableCommentParser tableCommentParser) {
		this.statTableRepository = statTableRepository;
		this.tableCommentParser = tableCommentParser;
	}

	@Transactional
	public void fillCommentAndContent() {
		// 500개를 불러 온다.
		List<StatTable> statTableList = statTableRepository.findTop500ByCommentAndContent(null, null);
		CompletableFuture<TableCommentDto>[] futures = statTableList.stream()
			.map(statTable -> tableCommentParser.getCommentAndContentsByTableId(statTable.getKosisTbId()))
			.toArray(CompletableFuture[]::new);

		CompletableFuture.allOf(futures).join();
		List<StatTable> resultList = new ArrayList<>(statTableList.size());
		List<TableCommentDto> parseResult = Arrays.stream(futures).map(CompletableFuture::join).toList();
		for (int i = 0; i < statTableList.size(); i++) {
			var statTable = statTableList.get(i);
			var dto = parseResult.get(i);
			resultList.add(
				new StatTable(
					statTable.getId(),
					statTable.getRefSurveyId(),
					statTable.getName(),
					dto.content(),
					dto.comment(),
					statTable.getKosisTbId(),
					statTable.getKosisViewLink()
				)
			);
		}

		statTableRepository.saveAll(resultList);
	}

}
