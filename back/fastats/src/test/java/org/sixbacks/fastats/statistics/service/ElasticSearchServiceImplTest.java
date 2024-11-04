package org.sixbacks.fastats.statistics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:.env")
public class ElasticSearchServiceImplTest {

	@Autowired
	private ElasticSearchService elasticSearchService;

	@MockBean
	private StatSurveyJdbcRepository statSurveyJdbcRepository;

	@Autowired
	private ElasticsearchRepository<StatDataDocument, Long> elasticsearchRepository;

	@Test
	public void testSaveData() {
		// saveData 호출
		elasticSearchService.saveData();

		// Elasticsearch에서 데이터가 저장되었는지 확인
		Iterable<StatDataDocument> responses = elasticsearchRepository.findAll();
		System.out.println(responses.iterator().next());
	}
}
