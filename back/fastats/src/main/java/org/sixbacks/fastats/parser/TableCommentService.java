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

	public void fillCommentAndContent() {
		long last_id = 0;
		while (true) {
			// 500개를 불러 온다.
			List<StatTable> statTableList = statTableRepository.findTop500ByIdGreaterThanAndCommentIsNullAndContentIsNull(
				last_id);

			if (statTableList == null || statTableList.isEmpty()) {
				break;
			}

			// 비동기 시작
			CompletableFuture<TableCommentDto>[] futures = statTableList.stream()
				.map(statTable -> tableCommentParser.getCommentAndContentsByTableId(statTable.getKosisTbId()))
				.toArray(CompletableFuture[]::new);

			// 비동기 기다림
			CompletableFuture.allOf(futures).join();

			// api 요청 결과 저장
			List<StatTable> resultList = new ArrayList<>(statTableList.size());

			List<TableCommentDto> parseResult = Arrays.stream(futures).map(CompletableFuture::join).toList();

			// 엔티티 업데이트
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

			log.info("테이블 저장 : size : {}, last_idx : {}", resultList.size(), last_id);
			last_id = statTableList.get(statTableList.size() - 1).getId();
			statTableRepository.saveAll(resultList);
		}

		// List<StatTable> statTableList = statTableRepository.findTop500ByCommentAndContent(null, null);
	}

	public void testApi() {
		var v = tableCommentParser.callApi("DT_623002_2022036");
		log.info("API 테스트 결과 : {}", v);
	}

}
