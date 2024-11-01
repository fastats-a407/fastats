package org.sixbacks.fastats.statistics.service;

import java.util.List;

import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.dto.response.StatDataDto;
import org.sixbacks.fastats.statistics.repository.jdbc.CollInfoJdbcRepository;
import org.sixbacks.fastats.statistics.repository.jdbc.ElasticSearchJdbcRepository;
import org.sixbacks.fastats.statistics.repository.jdbc.ElasticSearchRepository;
import org.sixbacks.fastats.statistics.repository.jdbc.SectorJdbcRepository;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.sixbacks.fastats.statistics.repository.jdbc.StatTableJdbcRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

	private final StatSurveyJdbcRepository statSurveyJdbcRepository;
	private final ElasticsearchRepository elasticsearchRepository;

	public ElasticSearchServiceImpl(
		@Qualifier("statSurveyJdbcRepository") StatSurveyJdbcRepository statSurveyJdbcRepository,
		@Qualifier("elasticSearchRepository") ElasticsearchRepository elasticsearchRepository
		) {
		this.statSurveyJdbcRepository = statSurveyJdbcRepository;
		this.elasticsearchRepository = elasticsearchRepository;
	}

	@Override
	public void saveData() {
		// 1. CRUDRepository를 통해 4개의 테이블에서 필요한 칼럼을 추출
		List<StatDataDocument> responses = statSurveyJdbcRepository.findAllStatData();
		// 2. 각 칼럼을 Elastic Search Document의 형식에 맞게 입력
		elasticsearchRepository.saveAll(responses);
		// 3. 반환
	}

}
