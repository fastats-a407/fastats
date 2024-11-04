package org.sixbacks.fastats.statistics.service;

import java.util.List;

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
		// 3. 반환
	}

	@Override
	// TODO : 메서드 분리 및 for문으로 BulkRequest에 넣어야하는지 고민 필요, APIResponse
	public void saveDataWithBulk() {
		// 1. CRUDRepository를 통해 필요한 데이터를 조회
		List<StatDataDocument> responses = statSurveyJdbcRepository.findAllStatData();
		log.info("데이터베이스에서 {}개의 문서를 조회했습니다.", responses.size());
		// 2. BulkRequest 생성 및 데이터 추가
		BulkRequest bulkRequest = new BulkRequest();
		for (StatDataDocument document : responses) {
			try {
				IndexRequest indexRequest = new IndexRequest("stat_data_index").id(document.getTableId().toString())
					.source(objectMapper.writeValueAsString(document), XContentType.JSON);
				bulkRequest.add(indexRequest);
			} catch (Exception e) {
				log.error("문서 ID {}를 직렬화하는 중 오류 발생: {}", document.getTableId(), e.getMessage());
			}
		}
		try {
			// 3. Bulk 요청 실행
			BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (bulkResponse.hasFailures()) {
				log.error("Bulk 인덱싱에 실패했습니다: {}", bulkResponse.buildFailureMessage());
			} else {
				log.info("Elasticsearch에 데이터가 성공적으로 인덱싱되었습니다.");
			}
		} catch (Exception e) {
			log.error("Bulk 요청을 실행하는 중 오류 발생: {}", e.getMessage());
		}
	}
}
