package org.sixbacks.fastats.statistics.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.sixbacks.fastats.global.error.ErrorCode;
import org.sixbacks.fastats.global.exception.CustomException;
import org.sixbacks.fastats.statistics.builder.MultiMatchQueryCustomBuilder;
import org.sixbacks.fastats.statistics.dto.request.SearchCriteria;
import org.sixbacks.fastats.statistics.dto.response.CategoryListResponse;
import org.sixbacks.fastats.statistics.dto.response.StatSurveyInfoDto;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.dto.response.TableByDto;
import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;
import org.sixbacks.fastats.statistics.entity.document.StatNgramDataDocument;
import org.sixbacks.fastats.statistics.repository.jdbc.ElasticSearchRepository;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
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
	private final ElasticSearchRepository elasticsearchRepository;
	private final ObjectMapper objectMapper;
	private final ElasticsearchOperations elasticsearchOperations;
	private final ResourceLoader resourceLoader;

	public ElasticSearchServiceImpl(
			@Qualifier("statSurveyJdbcRepository") StatSurveyJdbcRepository statSurveyJdbcRepository,
			@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient,
			@Qualifier("elasticSearchRepository") ElasticSearchRepository elasticsearchRepository,
			@Qualifier("objectMapper") ObjectMapper objectMapper,
			ElasticsearchOperations elasticsearchOperations,
			ResourceLoader resourceloader) {
		this.statSurveyJdbcRepository = statSurveyJdbcRepository;
		this.restHighLevelClient = restHighLevelClient;
		this.elasticsearchRepository = elasticsearchRepository;
		this.objectMapper = objectMapper;
		this.elasticsearchOperations = elasticsearchOperations;
		this.resourceLoader = resourceloader;
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

	private String serializeDocument(StatNgramDataDocument document) {
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

	@Override
// TODO : BatchSize와 NumThreads에 대한 효율적인 처리 방식을 채택해야함.
	public void saveDataNgramWithBulkThroughMultiThreads() {
		// 인덱스 이름과 설정 파일 경로
		String indexName = "ngram_index";
		String settingsPath = "/elasticsearch/settings/ngram-settings.json";

		// ngram 애널라이저가 적용된 인덱스를 먼저 생성
		createNgramIndexIfNeeded(indexName, settingsPath);

		// 데이터 가져오기
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
						IndexRequest indexRequest = new IndexRequest(indexName)
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
	/**
	 * @deprecated {@link #searchByKeyword(SearchCriteria)} 를 이용.
	 */
	@Deprecated
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

	/**
	 * @deprecated {@link #searchByKeyword(SearchCriteria, Query)} 를 이용.
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
	public Page<StatTableListResponse> searchByKeyword(SearchCriteria searchCriteria) {

		String keyword = searchCriteria.getKeyword();
		int page = searchCriteria.getPage();
		int size = searchCriteria.getSize();
		String ctg = searchCriteria.getCtg();
		String ctgContent = searchCriteria.getCtgContent();

		Pageable pageable = PageRequest.of(page, size);

		Query query = NativeQuery.builder()
				.withQuery(q -> q
						.bool(b -> {
							// ctg가 null이 아닌 경우에만 term 조건 추가
							if (ctg != null && ctgContent != null) {
								b.filter(m -> m
										.term(t -> t
												.field(ctg)
												.value(ctgContent) // 정확히 일치해야 하는 필드
										)
								);
							}
							// 항상 적용되는 multiMatch 조건 추가
							b.must(m -> m
									.multiMatch(multi -> multi
											.query(keyword)
											.fields("statSurveyName", "statTableName",
													"statTableContent", "statTableComment")
											.type(TextQueryType.BestFields)
											.analyzer("fastats_nori")
									)
							);
							return b;
						})
				)
				.withPageable(pageable)
				.build();

		return searchByKeyword(searchCriteria, query);
	}

	@Override
	public Page<StatTableListResponse> searchByKeyword(SearchCriteria searchCriteria, Query query) {

		int page = searchCriteria.getPage();
		int size = searchCriteria.getSize();

		Pageable pageable = PageRequest.of(page, size);

		SearchHits<StatDataDocument> searchHits = elasticsearchOperations.search(query, StatDataDocument.class);

		// 총 페이지를 넘는 경우, 요청 시 커스텀 에러 던짐
		long totalHits = searchHits.getTotalHits();
		int totalPages = (int)Math.ceil((double)totalHits / size);
		if (page > 0 && page >= totalPages) {
			throw new CustomException(ErrorCode.STAT_ILL_REQUEST);
		}

		// 페이지가 적절한 경우 처리
		List<StatTableListResponse> documents = searchHits.getSearchHits().stream()
				.map(hit -> docToResponse(hit.getContent()))
				.collect(Collectors.toList());

		return new PageImpl<>(documents, pageable, searchHits.getTotalHits());
	}

	/*
		NOTE: 실제 서비스에서 이용되는 카테고리 검색 결과.
	 */
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

	/*
		ElasticSearch 클러스터 전체에서 카테고리 검색 결과를 불러오는 서비스 코드
		TODO: 쿼리가 사실 aggrList에 의존되는 상태로 생성되므로 추후 로직 수정 필요
	 */
	@Override
	public CategoryListResponse getCategoriesByKeyword(String keyword, List<String> aggrList, Query query) {

		SearchHits<StatDataDocument> searchHits = elasticsearchOperations.search(query, StatDataDocument.class);
		Map<String, List<TableByDto>> tableByMap = new HashMap<>();

		if (searchHits.hasAggregations()) {

			// 인터페이스 AggregationsContainer 대신 구현체인 ElasticsearchAggregation 이용해 집계 결과 획득
			ElasticsearchAggregations aggregationResults = (ElasticsearchAggregations)searchHits.getAggregations();
			assert aggregationResults != null;
			aggrList.forEach(aggr -> {
				ElasticsearchAggregation elasticsearchAggregation = aggregationResults.get(aggr);
				// 구현된 검색 로직 상 StringTermsAggregate의 형태로 이용해야 함
				StringTermsAggregate aggregate = elasticsearchAggregation.aggregation().getAggregate().sterms();
				List<TableByDto> tableByDtoList = bucketToDtoList(aggregate.buckets().array());
				System.out.println(tableByDtoList);

				tableByMap.put(aggr, tableByDtoList);
			});
		}

		return tableByMapToCategoryListDto(tableByMap);
	}

	/*
		Map에 저장된 String에 따라 CategoryListResponse를 산출하는 메서드
	 */
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

	/*
		Aggregation 집계 내 bucket에 들어 있는 데이터를 List<TableDto>로 변형
	 */
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


	// ngram 애널라이저가 적용된 인덱스를 생성하는 메서드
	private void createNgramIndexIfNeeded(String indexName, String settingsPath) {
		try {
			// 인덱스 존재 여부 확인
			boolean indexExists = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists();

			if (!indexExists) {
				// 설정 파일 로드
				Resource resource = resourceLoader.getResource("classpath:" + settingsPath);
				String settingsJson = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

				// JSON 파싱
				Map<String, Object> settings = objectMapper.readValue(settingsJson, Map.class);

				// 인덱스 생성
				IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
				indexOperations.create(settings);
				log.info("ngram 인덱스 '{}'가 성공적으로 생성되었습니다.", indexName);
			} else {
				log.info("인덱스 '{}'가 이미 존재합니다.", indexName);
			}
		} catch (IOException e) {
			log.error("ngram 인덱스 생성 중 오류 발생: {}", e.getMessage());
		}
	}

	@Override
	public List<String> getSuggestions(String userInput) {
		// 제안 결과를 저장할 리스트를 생성합니다.
		List<String> suggestions = new ArrayList<>();

		// Elasticsearch에서 "stat_data_index"라는 인덱스로 검색 요청을 초기화합니다.
		SearchRequest searchRequest = new SearchRequest("stat_data_index");

		// 검색에 필요한 설정을 빌드합니다.
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 검색 결과로 최대 1000개까지 가져오도록 설정합니다.
		searchSourceBuilder.size(1000);

		// 여러 검색 조건을 결합하기 위한 Bool 쿼리를 생성합니다.
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
			// "statSurveyName" 필드가 사용자 입력과 일치하는 문서를 찾습니다.
			.should(QueryBuilders.matchQuery("statSurveyName", userInput))
			// "statSurveyName" 필드에 정확한 구문으로 사용자 입력이 포함된 문서를 찾습니다.
			.should(QueryBuilders.matchPhraseQuery("statSurveyName", userInput));

		// 사용자 입력이 3자 이상인 경우
		if (userInput.length() >= 3) {
			// 철자 오류나 유사한 단어를 찾기 위해 퍼지 쿼리를 추가합니다.
			boolQuery.should(QueryBuilders.fuzzyQuery("statSurveyName", userInput)
				.fuzziness(Fuzziness.AUTO)); // 퍼지 정도를 자동으로 설정합니다.
		}

		// Bool 쿼리를 검색 소스에 설정합니다.
		searchSourceBuilder.query(boolQuery);
		// 검색 요청에 검색 소스를 적용합니다.
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = null;

		try {
			// Elasticsearch 클라이언트를 사용하여 검색 요청을 실행합니다.
			searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			// 예외가 발생하면 서버 내부 오류를 나타내는 커스텀 예외를 던집니다.
			throw new CustomException(ErrorCode.SERVER_INTERNAL_ERROR);
		}

		// 순서를 유지하면서 중복을 제거하기 위해 LinkedHashSet을 사용합니다.
		Set<String> uniqueSuggestions = new LinkedHashSet<>();

		// 검색 결과를 순회합니다.
		for (SearchHit hit : searchResponse.getHits()) {
			// 각 결과에서 "statSurveyName" 필드를 가져옵니다.
			String suggestion = hit.getSourceAsMap().get("statSurveyName").toString();
			// 제안 목록에 추가합니다. (중복된 값은 자동으로 제외됩니다)
			uniqueSuggestions.add(suggestion);
			// 상위 5개 제안만 필요하므로 5개를 모으면 반복을 중단합니다.
			if (uniqueSuggestions.size() >= 5) {
				break;
			}
		}

		// 유니크한 제안들을 리스트로 변환합니다.
		suggestions = new ArrayList<>(uniqueSuggestions);

		// 제안 리스트를 반환합니다.
		return suggestions;
	}

}


