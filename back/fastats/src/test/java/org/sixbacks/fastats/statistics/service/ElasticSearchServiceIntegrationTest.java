package org.sixbacks.fastats.statistics.service;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sixbacks.fastats.global.error.ErrorCode;
import org.sixbacks.fastats.global.exception.CustomException;
import org.sixbacks.fastats.statistics.builder.MultiMatchQueryCustomBuilder;
import org.sixbacks.fastats.statistics.entity.document.StatDataDocument;
import org.sixbacks.fastats.statistics.dto.response.StatTableListResponse;
import org.sixbacks.fastats.statistics.repository.jdbc.StatSurveyJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.Query;
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
	@DisplayName("ElasticSearch 키워드 기반 검색 정확도 및 성능 테스트")
	public void testSearchQuerySimilarityAndPerformance() {
		// 비교할 키워드와 기대되는 결과 세트 (실제 사이트 기준)
		String keyword = "일반가구";
		List<String> expectedResults = List.of("가구주의 성, 연령 및 거처의 종류별 가구(일반가구) - 시군구",
			"가구주의 성, 연령 및 세대구성별 가구(일반가구) - 시군구",
			"세대구성 및 가구원수별 가구(일반가구) - 시군구",
			"세대구성별 가구 및 가구원(일반가구) - 시군구",
			"고령자(65세이상) 가구(일반가구) - 시군구",
			"거처의 종류 및 가구원수별 가구(일반가구) - 시군구",
			"(일반가구)지역별 소득계층별 점유형태",
			"(일반가구)지역별 소득계층별 주택유형",
			"(일반가구)행정구역별 점유형태",
			"(일반가구)행정구역별 주택유형");

		int page = 0;
		int size = 10;

		Pageable pageable = PageRequest.of(page, size);

		// MultiMatchQueryCustomBuilder()를 이용한 쿼리 생성
		Query query = new MultiMatchQueryCustomBuilder()
			.withKeyword(keyword)
			.addFieldWithBoost("statSurveyName", 1.2f)
			.addFieldWithBoost("statOrgName", 1.0f)
			.addFieldWithBoost("statTableName", 1.6f)
			.addFieldWithBoost("statTableContent", 1.2f)
			.addFieldWithBoost("statTableComment", 1.2f)
			.addPageable(pageable)
			.build();

		long startTime = System.currentTimeMillis();

		// 각 쿼리를 실행하여 결과를 얻음
		Page<StatTableListResponse> result = elasticSearchService.searchByKeyword(keyword, page, size, query);

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Query Execution Time: " + duration + " ms");

		// 유사도를 평가하는 메서드를 사용하여 결과를 비교
		double similarityScore1 = evaluateSimilarity(result, expectedResults);

		// 로그로 각 쿼리의 유사도 점수를 출력
		System.out.println("Similarity Score for Query 1: " + similarityScore1);

		// 가장 높은 유사도 점수를 얻는 쿼리가 있는지 검증 (예: 0.8 이상을 기대)
		assertTrue("Query 1 is similar enough",
			similarityScore1 >= 0.8);
	}

	@Test
	@DisplayName("ElasticSearch 키워드 기반 Nori 적용 검색 정확도 테스트")
	public void testSearchQuerySimilarityAndPerformanceWithNori() {
		// 비교할 키워드와 기대되는 결과 세트 (실제 사이트 기준)
		String keyword = "인구";
		List<String> expectedResults = List.of("가구주의 성, 연령 및 거처의 종류별 가구(일반가구) - 시군구",
			"가구주의 성, 연령 및 세대구성별 가구(일반가구) - 시군구",
			"세대구성 및 가구원수별 가구(일반가구) - 시군구",
			"세대구성별 가구 및 가구원(일반가구) - 시군구",
			"고령자(65세이상) 가구(일반가구) - 시군구",
			"거처의 종류 및 가구원수별 가구(일반가구) - 시군구",
			"(일반가구)지역별 소득계층별 점유형태",
			"(일반가구)지역별 소득계층별 주택유형",
			"(일반가구)행정구역별 점유형태",
			"(일반가구)행정구역별 주택유형");

		int page = 0;
		int size = 10;

		Pageable pageable = PageRequest.of(page, size);

		// MultiMatchQueryCustomBuilder()를 이용한 쿼리 생성
		Query query = new MultiMatchQueryCustomBuilder()
			.withKeyword(keyword)
			.addFieldWithBoost("statSurveyName", 1.2f)
			.addFieldWithBoost("statOrgName", 1.0f)
			.addFieldWithBoost("statTableName", 1.6f)
			.addFieldWithBoost("statTableContent", 1.2f)
			.addFieldWithBoost("statTableComment", 1.2f)
			.addPageable(pageable)
			.withAnalyzer("nori")
			.build();

		long startTime = System.currentTimeMillis();

		// 각 쿼리를 실행하여 결과를 얻음
		Page<StatTableListResponse> result = elasticSearchService.searchByKeyword(keyword, page, size, query);

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Query Execution Time: " + duration + " ms");

		// 유사도를 평가하는 메서드를 사용하여 결과를 비교
		double similarityScore1 = evaluateSimilarity(result, expectedResults);

		// 로그로 각 쿼리의 유사도 점수를 출력
		System.out.println("Similarity Score for Query 1: " + similarityScore1);

		// 가장 높은 유사도 점수를 얻는 쿼리가 있는지 검증 (예: 0.8 이상을 기대)
		assertTrue("Query 1 is similar enough",
			similarityScore1 >= 0.8);
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
