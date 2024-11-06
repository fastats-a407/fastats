package org.sixbacks.fastats.statistics.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sixbacks.fastats.global.error.ErrorCode;
import org.sixbacks.fastats.global.exception.CustomException;
import org.sixbacks.fastats.statistics.dto.document.StatDataDocument;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/*
	TODO: 현재 테스트 환경이 아닌 로컬 개발 환경의 ElasticSearch를 이용하고 있으므로 분리 필요
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:.env")
public class ElasticSearchServiceIntegrationTest {

	@Autowired
	private ElasticSearchService elasticSearchService;

	@Autowired
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

	@Test
	@DisplayName("ElasticSearch 키워드 기반 검색 테스트 - 긍정 결과")
	public void searchByKeyword_ReturnsResults_WhenPageRequestIsValid() {
		// Given: Local 환경에서 해당 필드에 대해 keyword를 포함하는 데이터가 있다는 가정
		String keyword = "Data";
		int page = 0;
		int size = 5;

		// When: searchByKeyword() 메서드 검사
		Page<StatTableListResponse> responses = elasticSearchService.searchByKeyword(keyword, page, size);

		// Then: 검색 결과가 비어있지 않아야 함
		assertThat(responses).isNotNull();

		responses.forEach(response -> {
			assertThat(response.toString()).containsIgnoringCase(keyword);
		});
	}

	@Test
	@DisplayName("ElasticSearch 키워드 기반 검색 테스트 - 잘못된 페이지 요청")
	public void searchByKeyword_EmptyOrException_WhenPageOutOfRange() {
		// Given: Local 환경에서 해당 필드에 대해 keyword를 포함하는 데이터가 있다는 가정
		String keyword = "Data";
		int page = 10;
		int size = 500;

		// When & Then: searchByKeyword() 메서드 호출 시 결과가 비어있거나 예외가 발생하는지 확인
		try {
			Page<StatTableListResponse> responses = elasticSearchService.searchByKeyword(keyword, page, size);

			// Then: 결과가 비어 있는 경우 검증
			assertThat(responses).isNotNull();
			assertThat(responses.getContent()).isEmpty();
		} catch (CustomException e) {
			// Then: 존재하지 않는 페이지 요청 시 예외가 발생하는지 확인
			assertThat(e.getCode()).isEqualTo(ErrorCode.STAT_ILL_REQUEST.getCode());
		}
	}

	@Test
	@DisplayName("ElasticSearch 키워드 기반 검색 정확도 테스트")
	public void searchByKeyword_With_Custom() {
		// Given: Local 환경에서 해당 필드에 대해 keyword를 포함하는 데이터가 있다는 가정
		String keyword = "Data";
		int page = 10;
		int size = 500;

		// When & Then: searchByKeyword() 메서드 호출 시 결과가 비어있거나 예외가 발생하는지 확인
		try {
			Page<StatTableListResponse> responses = elasticSearchService.searchByKeyword(keyword, page, size);

			// Then: 결과가 비어 있는 경우 검증
			assertThat(responses).isNotNull();
			assertThat(responses.getContent()).isEmpty();
		} catch (CustomException e) {
			// Then: 존재하지 않는 페이지 요청 시 예외가 발생하는지 확인
			assertThat(e.getCode()).isEqualTo(ErrorCode.STAT_ILL_REQUEST.getCode());
		}
	}

	private double evaluateSimilarity(Page<StatTableListResponse> response, List<String> expectedTitleResults) {

		List<String> actualResults = response.getContent().stream()
			.map(StatTableListResponse::getTitle) // 검색 결과의 제목 추출
			.toList();

		int maxLength = Math.min(expectedTitleResults.size(), actualResults.size());
		int matchCount = 0;

		// 각 위치 별로 기대 결과와 일치 확인
		for (int i = 0; i < maxLength; i++) {
			if (expectedTitleResults.get(i).equals(actualResults.get(i))) {
				matchCount++;
			}
		}

		// 위치별 일치율 계산
		return (double)matchCount / maxLength;
	}
}
