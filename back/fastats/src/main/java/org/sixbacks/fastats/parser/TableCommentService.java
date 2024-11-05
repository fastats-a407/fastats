package org.sixbacks.fastats.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.sixbacks.fastats.statistics.dto.preprocessing.TableCommentDto;
import org.sixbacks.fastats.statistics.entity.StatTable;
import org.sixbacks.fastats.statistics.repository.StatTableRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
		int pageSize = 5;
		int pageNumber = 0;
		Pageable page = PageRequest.of(pageNumber, pageSize);
		Page<StatTable> tablePage = statTableRepository.findByCommentAndContent(null, null, page);
		int maxPage = tablePage.getTotalPages();
		maxPage = 3;

		for (pageNumber = 0; pageNumber < maxPage; pageNumber++) {
			page = PageRequest.of(pageNumber, pageSize);
			tablePage = statTableRepository.findByCommentAndContent(null, null, page);
			List<StatTable> statTableList = tablePage.getContent();
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

		// List<StatTable> statTableList = statTableRepository.findTop500ByCommentAndContent(null, null);
	}

}
