package org.sixbacks.fastats.statistics.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ElasticSearchServiceImpl implements ElasticSearchService {

	private final StatSurveyJdbcRepository statSurveyJdbcRepository;
	private final ElasticsearchRepository elasticsearchRepository;
	private final RestHighLevelClient restHighLevelClient;
	private final ObjectMapper objectMapper;

	public ElasticSearchServiceImpl(
		@Qualifier("statSurveyJdbcRepository") StatSurveyJdbcRepository statSurveyJdbcRepository,
		@Qualifier("elasticSearchRepository") ElasticsearchRepository elasticsearchRepository,
		@Qualifier("restHighLevelClient") RestHighLevelClient restHighLevelClient,
		@Qualifier("objectMapper") ObjectMapper objectMapper) {
		this.statSurveyJdbcRepository = statSurveyJdbcRepository;
		this.restHighLevelClient = restHighLevelClient;
		this.elasticsearchRepository = elasticsearchRepository;
		this.objectMapper = objectMapper;
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
}
