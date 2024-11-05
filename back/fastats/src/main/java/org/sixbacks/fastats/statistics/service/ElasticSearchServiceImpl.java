package org.sixbacks.fastats.statistics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.sixbacks.fastats.global.error.ErrorCode;
import org.sixbacks.fastats.global.exception.CustomException;
import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.dto.response.StatSurveyInfoDto;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

	private final StatSurveyJdbcRepository statSurveyJdbcRepository;
	private final ElasticsearchRepository elasticsearchRepository;
	private final ElasticsearchOperations elasticsearchOperations;

	public ElasticSearchServiceImpl(
		@Qualifier("statSurveyJdbcRepository") StatSurveyJdbcRepository statSurveyJdbcRepository,
		@Qualifier("elasticSearchRepository") ElasticsearchRepository elasticsearchRepository,
		ElasticsearchOperations elasticsearchOperations
	) {
		this.statSurveyJdbcRepository = statSurveyJdbcRepository;
		this.elasticsearchRepository = elasticsearchRepository;
		this.elasticsearchOperations = elasticsearchOperations;
	}

	@Override
	public void saveData() {
		// 1. CRUDRepository를 통해 4개의 테이블에서 필요한 칼럼을 추출
		List<StatDataDocument> responses = statSurveyJdbcRepository.findAllStatData();
		// 2. 각 칼럼을 Elastic Search Document의 형식에 맞게 입력
		elasticsearchRepository.saveAll(responses);
		// 3. 반환
	}

	/*
		NOTE:
		여러 필드에 대해서 MultiMatch를 통해 정확도(relevancy) 기준으로 검색할 예정이므로,
		ElasticSearchRepository 대신 elasticSearchOperations를 이용해 복잡성 해결
	 */
	public Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size) {

		PageRequest pageable = PageRequest.of(page, size);

		// ElasticSearch API 쿼리 작성
		// fields
		Query query = NativeQuery.builder()
			.withQuery(q -> q
				.multiMatch(m -> m
					.query(keyword)
					.fields("statSurveyName", "statOrgName",
						"statTableName", "statTableContent",
						"statTableComment")
					// NOTE: 검색 방식에 따라 TextQueryType 변경 필요
					.type(TextQueryType.BestFields)
				)
			)
			.withPageable(pageable)
			.build();

		SearchHits<StatDataDocument> searchHits = elasticsearchOperations.search(query, StatDataDocument.class);

		// 총 페이지를 넘는 경우, 요청 시 커스텀 에러 던짐
		long totalHits = searchHits.getTotalHits();
		int totalPages = (int)Math.ceil((double)totalHits / size);
		if (page >= totalPages) {
			throw new CustomException(ErrorCode.STAT_ILL_REQUEST);
		}

		// 페이지가 적절한 경우 처리
		List<StatTableListResponse> documents = searchHits.getSearchHits().stream()
			.map(hit -> docToResponse(hit.getContent()))
			.collect(Collectors.toList());

		return new PageImpl<>(documents, pageable, searchHits.getTotalHits());
	}

	private StatTableListResponse docToResponse(StatDataDocument document) {

		// Embedded와 비슷하게 필요한 StatSurveyInfoDto 생성
		StatSurveyInfoDto statSurveyInfo = new StatSurveyInfoDto(document.getStatOrgName(),
			document.getStatSurveyName(),
			null);

		return new StatTableListResponse(
			document.getStatTableName(),  // title
			statSurveyInfo,              // statSurveyInfo
			document.getCollInfoStartDate(),  // collStartDate
			document.getCollInfoEndDate(),    // collEndDate
			document.getStatTableKosisViewLink() // tableLink
		);
	}

}
