package org.sixbacks.fastats.statistics.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.sixbacks.fastats.global.error.ErrorCode;
import org.sixbacks.fastats.global.exception.CustomException;
import org.sixbacks.fastats.statistics.builder.MultiMatchQueryCustomBuilder;
import org.sixbacks.fastats.statistics.dto.response.CategoryListResponse;
import org.sixbacks.fastats.statistics.dto.response.StatSurveyInfoDto;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.dto.response.TableByDto;
import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ElasticSearchServiceImpl implements ElasticSearchService {

	private final StatSurveyJdbcRepository statSurveyJdbcRepository;
	private final RestHighLevelClient restHighLevelClient;
	private final ElasticsearchRepository elasticsearchRepository;
	private final ObjectMapper objectMapper;
	private final ElasticsearchOperations elasticsearchOperations;

	public ElasticSearchServiceImpl(
		@Qualifier("statSurveyJdbcRepository") StatSurveyJdbcRepository statSurveyJdbcRepository,
		@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient,
		@Qualifier("elasticSearchRepository") ElasticsearchRepository elasticsearchRepository,
		@Qualifier("objectMapper") ObjectMapper objectMapper,
		ElasticsearchOperations elasticsearchOperations) {
		this.statSurveyJdbcRepository = statSurveyJdbcRepository;
		this.restHighLevelClient = restHighLevelClient;
		this.elasticsearchRepository = elasticsearchRepository;
		this.objectMapper = objectMapper;
		this.elasticsearchOperations = elasticsearchOperations;
	}

	@Override
	public void saveData() {
		// 1. CRUDRepository를 통해 4개의 테이블에서 필요한 칼럼을 추출
		List<StatDataDocument> responses = statSurveyJdbcRepository.findAllStatData();
		// 2. 각 칼럼을 Elastic Search Document의 형식에 맞게 입력
		elasticsearchRepository.saveAll(responses);
	}

	@Override
	public void saveDataWithBulk() {
		List<StatDataDocument> responses = statSurveyJdbcRepository.findAllStatData();
		log.info("데이터베이스에서 {}개의 문서를 조회했습니다.", responses.size());
		BulkRequest bulkRequest = buildBulkRequest(responses);
		executeBulkRequest(bulkRequest);
	}

	// 1. 데이터 직렬화
	private String serializeDocument(StatDataDocument document) {
		try {
			return objectMapper.writeValueAsString(document);
		} catch (Exception e) {
			log.error("문서 ID {}를 직렬화하는 중 오류 발생: {}", document.getTableId(), e.getMessage());
			return null;
		}
	}

	// 2. Bulk 요청 빌드
	private BulkRequest buildBulkRequest(List<StatDataDocument> documents) {
		BulkRequest bulkRequest = new BulkRequest();
		// for문으로 BulkRequest에 넣어야하는지 고민 필요
		for (StatDataDocument document : documents) {
			String serializedDoc = serializeDocument(document);
			if (serializedDoc != null) {
				IndexRequest indexRequest = new IndexRequest("stat_data_index")
					.id(document.getTableId())
					.source(serializedDoc, XContentType.JSON);
				bulkRequest.add(indexRequest);
			}
		}
		return bulkRequest;
	}

	// 3. Bulk 요청 전송
	private void executeBulkRequest(BulkRequest bulkRequest) {
		try {
			BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (bulkResponse.hasFailures()) {
				log.error("Bulk 요청 처리 중 오류 발생: {}", bulkResponse.buildFailureMessage());
			} else {
				log.info("Bulk 요청 성공적으로 완료!");
			}
		} catch (IOException e) {
			log.error("Bulk 요청 전송 중 IOException 발생: {}", e.getMessage());
		}
	}

	@Override
	// TODO : BatchSize와 NumThreads에 대한 효율적인 처리 방식을 채택해야함.
	public void saveDataWithBulkThroughMultiThreads() {
		List<StatDataDocument> documents = statSurveyJdbcRepository.findAllStatData();
		int batchSize = 500;
		int numThreads = 4; // 병렬로 실행할 스레드 수
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < documents.size(); i += batchSize) {
			final List<StatDataDocument> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
			executorService.submit(() -> {
				BulkRequest batchRequest = new BulkRequest();
				for (StatDataDocument document : batch) {
					String serializedDoc = serializeDocument(document);
					if (serializedDoc != null) {
						IndexRequest indexRequest = new IndexRequest("stat_data_index")
							.id(document.getTableId())
							.source(serializedDoc, XContentType.JSON);
						batchRequest.add(indexRequest);
					}
				}
				try {
					BulkResponse bulkResponse = restHighLevelClient.bulk(batchRequest, RequestOptions.DEFAULT);
					if (bulkResponse.hasFailures()) {
						log.error("Bulk 요청 처리 중 오류 발생: {}", bulkResponse.buildFailureMessage());
					} else {
						log.info("Bulk 요청 성공적으로 완료!");
					}
				} catch (IOException e) {
					log.error("Bulk 요청 전송 중 IOException 발생: {}", e.getMessage());
				}
			});
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			log.error("멀티 스레딩 Bulk 작업 중 인터럽트 발생: {}", e.getMessage());
		}

	}

	/*
		NOTE:
		여러 필드에 대해서 MultiMatch를 통해 정확도(relevancy) 기준으로 검색할 예정이므로,
		ElasticSearchRepository 대신 elasticSearchOperations를 이용해 복잡성 해결
	 */
	@Override
	public Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

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

	/*
		최적의 검색 결과 테스트를 위해 Query를 외부에서 작성해 넘기는 메서드
	 */
	@Override
	public Page<StatTableListResponse> searchByKeyword(String keyword, int page, int size, Query query) {

		Pageable pageable = PageRequest.of(page, size);

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

	@Override
	public CategoryListResponse getCategoriesByKeyword(String keyword) {

		Query query = new MultiMatchQueryCustomBuilder()
			.withKeyword(keyword)
			.addFieldWithBoost("statSurveyName", 1.2f)
			.addFieldWithBoost("statOrgName", 1.0f)
			.addFieldWithBoost("statTableName", 1.6f)
			.addFieldWithBoost("statTableContent", 1.2f)
			.addFieldWithBoost("statTableComment", 1.2f)
			.withAnalyzer("fastats_nori")
			.build();

		List<String> aggrList = List.of("sectorName, statSurveyName");

		return getCategoriesByKeyword(keyword, aggrList, query);
	}

	@Override
	public CategoryListResponse getCategoriesByKeyword(String keyword, List<String> aggrList, Query query) {

		SearchHits<StatDataDocument> searchHits = elasticsearchOperations.search(query, StatDataDocument.class);
		Map<String, List<TableByDto>> tableByMap = new HashMap<>();

		if (searchHits.hasAggregations()) {
			ElasticsearchAggregations aggregationResults = (ElasticsearchAggregations)searchHits.getAggregations();
			assert aggregationResults != null;
			aggrList.forEach(aggr -> {
				ElasticsearchAggregation elasticsearchAggregation = aggregationResults.get(aggr);
				StringTermsAggregate aggregate = elasticsearchAggregation.aggregation().getAggregate().sterms();
				List<TableByDto> tableByDtoList = bucketToDtoList(aggregate.buckets().array());
				System.out.println(tableByDtoList);

				tableByMap.put(aggr, tableByDtoList);
			});
		}

		return tableByMapToCategoryListDto(tableByMap);
	}

	private CategoryListResponse tableByMapToCategoryListDto(Map<String, List<TableByDto>> tableByMap) {

		List<TableByDto> byTheme = null;
		List<TableByDto> bySurvey = null;

		for (Map.Entry<String, List<TableByDto>> entry : tableByMap.entrySet()) {
			String key = entry.getKey();
			List<TableByDto> tableByDtoList = entry.getValue();

			switch (key) {
				case "sectorName" -> {
					byTheme = tableByDtoList;
				}
				case "statSurveyName" -> {
					bySurvey = tableByDtoList;
				}
				default -> {
					throw new CustomException(ErrorCode.STAT_ILL_REQUEST);
				}
			}
		}

		return new CategoryListResponse(byTheme, bySurvey);
	}

	private List<TableByDto> bucketToDtoList(List<StringTermsBucket> array) {

		return array.stream()
			.map(bucket -> new TableByDto(bucket.key().stringValue(), (int)bucket.docCount()))
			.collect(Collectors.toList());

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
